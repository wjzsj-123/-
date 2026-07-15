<template>
  <div class="study-plan-page">
    <div class="page-header">
      <h2>学习计划</h2>
      <p class="page-desc">选择题库并设定每日刷题量，按进度完成学习目标</p>
    </div>

    <section class="panel create-panel">
      <h3 class="panel-title">新建学习计划</h3>
      <div class="form-grid">
        <div class="form-row">
          <label>目标题库</label>
          <select v-model="form.questionSetId" class="field-control">
            <option value="">请选择题库</option>
            <option v-for="set in questionSets" :key="set.id" :value="set.id">{{ set.name }}</option>
          </select>
        </div>
        <div class="form-row">
          <label>每日刷题数</label>
          <input class="field-control" type="number" min="1" v-model="form.dailyCount" />
        </div>
      </div>
      <div class="panel-actions">
        <button type="button" class="btn-primary" :disabled="loading" @click="savePlan">保存学习计划</button>
      </div>
    </section>

    <section class="panel list-panel">
      <h3 class="panel-title">我的学习计划</h3>
      <div v-if="loading" class="state-block muted">加载中…</div>
      <div v-else-if="plans.length === 0" class="state-block empty">
        <p>暂无学习计划</p>
        <p class="hint">在上方选择题库并保存即可开始</p>
      </div>
      <div v-else class="plan-cards">
        <div v-for="item in plans" :key="item.id" class="plan-card" :class="{ completed: item.remainingCount === 0 && item.totalCount > 0 }">
          <div class="plan-main">
            <div class="plan-title-row">
              <span class="plan-title">{{ item.questionSet?.name || '未命名题库' }}</span>
              <span v-if="item.remainingCount === 0 && item.totalCount > 0" class="badge-done">已全部完成</span>
            </div>
            <ul class="plan-meta">
              <li>每日 <strong>{{ item.dailyCount }}</strong> 题</li>
              <li>共 {{ item.totalCount }} 题，已学 {{ item.learnedCount }}，剩余 {{ item.remainingCount }}</li>
              <li v-if="item.remainingCount > 0">预计还需 <strong>{{ item.remainingDays }}</strong> 天</li>
              <li v-else-if="item.totalCount > 0">本计划题目已全部学完</li>
            </ul>
          </div>
          <div class="plan-actions">
            <button type="button" class="btn-primary btn-go" @click="goPractice(item.id)">进入学习</button>
            <button type="button" class="btn-secondary" @click="resetPlan(item)">重新学习</button>
            <button type="button" class="btn-danger" @click="deletePlan(item)">删除计划</button>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const loading = ref(false)
const plans = ref([])
const questionSets = ref([])
const form = ref({
  questionSetId: '',
  dailyCount: 10
})
const router = useRouter()

const getCurrentUserId = () => {
  const userInfo = localStorage.getItem('userInfo')
  return userInfo ? JSON.parse(userInfo).id : null
}

const loadQuestionSets = async () => {
  const userId = getCurrentUserId()
  const response = await fetch(`/api/question-set/user/${userId}`)
  const result = await response.json()
  if (result.code === 0) questionSets.value = result.data || []
}

const loadPlan = async () => {
  const userId = getCurrentUserId()
  loading.value = true
  try {
    const response = await fetch(`/api/study-plan?userId=${userId}`)
    const result = await response.json()
    if (result.code === 0) {
      plans.value = result.data || []
    } else {
      ElMessage.error(result.message || '加载学习计划失败')
    }
  } finally {
    loading.value = false
  }
}

const savePlan = async () => {
  const userId = getCurrentUserId()
  if (!form.value.questionSetId) {
    ElMessage.warning('请选择目标题库')
    return
  }
  if (!form.value.dailyCount || Number(form.value.dailyCount) <= 0) {
    ElMessage.warning('每日刷题数量必须大于0')
    return
  }
  const response = await fetch('/api/study-plan', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      userId,
      questionSetId: Number(form.value.questionSetId),
      dailyCount: Number(form.value.dailyCount)
    })
  })
  const result = await response.json()
  if (result.code === 0) {
    ElMessage.success('学习计划保存成功')
    form.value.questionSetId = ''
    form.value.dailyCount = 10
    await loadPlan()
  } else {
    ElMessage.error(result.message || '保存失败')
  }
}

const goPractice = (planId) => {
  router.push(`/home/study-plan/practice?planId=${planId}`)
}

const resetPlan = async (item) => {
  const userId = getCurrentUserId()
  if (!confirm(`确定将「${item.questionSet?.name || '该题库'}」的学习进度清零并重新开始吗？`)) {
    return
  }
  const response = await fetch(`/api/study-plan/${item.id}/reset?userId=${userId}`, { method: 'POST' })
  const result = await response.json()
  if (result.code === 0) {
    ElMessage.success('已重置，可重新学习')
    await loadPlan()
  } else {
    ElMessage.error(result.message || '重置失败')
  }
}

const deletePlan = async (item) => {
  const userId = getCurrentUserId()
  if (!confirm(`确定删除「${item.questionSet?.name || '该题库'}」的学习计划吗？删除后不可恢复。`)) {
    return
  }
  const response = await fetch(`/api/study-plan/${item.id}?userId=${userId}`, { method: 'DELETE' })
  const result = await response.json()
  if (result.code === 0) {
    ElMessage.success('学习计划已删除')
    await loadPlan()
  } else {
    ElMessage.error(result.message || '删除失败')
  }
}

onMounted(async () => {
  await loadQuestionSets()
  await loadPlan()
})
</script>

<style scoped>
.study-plan-page {
  width: 100%;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px;
  font-size: 1.5rem;
  color: #303133;
}

.page-desc {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.panel {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 20px 24px;
  margin-bottom: 20px;
}

.panel-title {
  margin: 0 0 16px;
  font-size: 1.05rem;
  color: #303133;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.form-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  max-width: 480px;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.form-row label {
  min-width: 96px;
  color: #606266;
  font-weight: 500;
  font-size: 14px;
}

.field-control {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  min-width: 0;
}

.panel-actions {
  margin-top: 20px;
}

.btn-primary {
  background: #42b983;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 10px 28px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background: #36a371;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.state-block {
  text-align: center;
  padding: 32px 16px;
  border-radius: 6px;
}

.state-block.muted {
  color: #909399;
}

.state-block.empty {
  background: #fafafa;
  color: #909399;
}

.state-block.empty p {
  margin: 0 0 8px;
}

.state-block .hint {
  font-size: 13px;
  margin: 0;
}

.plan-cards {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.plan-card {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fafcff;
}

.plan-card.completed {
  border-color: #b3e5c8;
  background: #f6fffa;
}

.plan-main {
  flex: 1;
  min-width: 0;
}

.plan-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 10px;
}

.plan-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.badge-done {
  font-size: 12px;
  color: #fff;
  background: #42b983;
  padding: 2px 8px;
  border-radius: 4px;
}

.plan-meta {
  margin: 0;
  padding-left: 18px;
  color: #606266;
  font-size: 14px;
  line-height: 1.7;
}

.plan-meta strong {
  color: #42b983;
}

.plan-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
  justify-content: center;
}

.btn-go {
  min-width: 120px;
}

.btn-secondary {
  background: #fff;
  color: #606266;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
}

.btn-secondary:hover {
  color: #42b983;
  border-color: #42b983;
}

.btn-danger {
  background: #fff;
  color: #f56c6c;
  border: 1px solid #fbc4c4;
  border-radius: 6px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
}

.btn-danger:hover {
  background: #fef0f0;
}
</style>
