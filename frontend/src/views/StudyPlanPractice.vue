<template>
  <div class="practice-page">
    <h2>学习计划做题</h2>
    <button class="back-btn" @click="goBack">返回学习计划</button>

    <div v-if="phase === 'practice' || phase === 'review'" class="question-card">
      <div class="q-header">
        <span>
          {{ phase === 'review' ? '错题复习' : '今日学习' }}
          第 {{ currentIndex + 1 }} / {{ questions.length }} 题
        </span>
        <span>{{ typeText(currentQuestion.type) }}</span>
      </div>
      <p class="q-content">{{ currentQuestion.content }}</p>

      <div v-if="currentQuestion.type === 1 || currentQuestion.type === 3" class="option-list">
        <div
            v-for="(opt, idx) in currentQuestion.options || []"
            :key="opt.id || idx"
            class="option-item"
            :class="{ selected: isSelected(opt.id) }"
            @click="chooseOption(opt.id)"
        >
          {{ String.fromCharCode(65 + idx) }}. {{ opt.content }}
        </div>
      </div>

      <div v-if="currentQuestion.type === 2" class="fill-wrap">
        <input v-model="fillAnswer" type="text" placeholder="请输入答案" />
      </div>

      <div class="actions">
        <button @click="submitAnswer" :disabled="submitted">提交答案</button>
        <button @click="nextQuestion" :disabled="!submitted">下一题</button>
      </div>

      <div v-if="submitted" class="result" :class="{ pass: answerCorrect, fail: !answerCorrect }">
        {{ answerCorrect ? '答对了，已标记为已学会' : '答错了，已加入错题复习' }}
      </div>
    </div>

    <div v-else-if="phase === 'completed'" class="state-card">
      <h3>已完成今日学习计划</h3>
      <p>你可以选择继续复习错题，或先离开。</p>
      <div class="actions">
        <button @click="startWrongReview">复习错题</button>
        <button @click="goBack">离开</button>
      </div>
    </div>

    <div v-else-if="phase === 'all-done'" class="state-card">
      <h3>今日学习已完成</h3>
      <p>当前没有错题需要复习。</p>
      <div class="actions">
        <button @click="goBack">离开</button>
      </div>
    </div>

    <!-- 全部题目学完：弹层选择 -->
    <div v-if="phase === 'plan-finished'" class="finish-overlay">
      <div class="finish-dialog">
        <h3>恭喜！本学习计划已全部完成</h3>
        <p class="finish-desc">
          题库「{{ planSummary.questionSetName }}」共 {{ planSummary.totalCount }} 题均已学完。
          你可以重新学习、删除计划，或稍后再来处理。
        </p>
        <div class="finish-actions">
          <button type="button" class="btn-reset" @click="handleResetFromDialog">重新学习</button>
          <button type="button" class="btn-delete" @click="handleDeleteFromDialog">删除计划</button>
          <button type="button" class="btn-later" @click="goBack">稍后再说</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const questions = ref([])
const currentIndex = ref(0)
const selectedOptionId = ref(null)
const selectedMulti = ref([])
const fillAnswer = ref('')
const submitted = ref(false)
const answerCorrect = ref(false)
const phase = ref('practice')
const planId = ref(null)
const planSummary = ref({
  totalCount: 0,
  remainingCount: 0,
  questionSetName: ''
})

const currentQuestion = computed(() => questions.value[currentIndex.value] || {})

const getCurrentUserId = () => {
  const userInfo = localStorage.getItem('userInfo')
  return userInfo ? JSON.parse(userInfo).id : null
}

const fetchPlanDetail = async () => {
  const userId = getCurrentUserId()
  if (!userId || !planId.value) return null
  const result = await request.get(`/api/study-plan/${planId.value}?userId=${userId}`)
  if (result.code !== 0 || !result.data) return null
  const plan = result.data
  planSummary.value = {
    totalCount: plan.totalCount || 0,
    remainingCount: plan.remainingCount || 0,
    questionSetName: plan.questionSet?.name || '未命名题库'
  }
  return plan
}

const isPlanFullyComplete = (plan) => {
  if (!plan) return false
  const total = plan.totalCount || 0
  const remaining = plan.remainingCount || 0
  return total > 0 && remaining === 0
}

const showPlanFinishedIfNeeded = async () => {
  const plan = await fetchPlanDetail()
  if (isPlanFullyComplete(plan)) {
    phase.value = 'plan-finished'
    return true
  }
  return false
}

const loadQuestions = async () => {
  const userId = getCurrentUserId()
  if (!userId) {
    ElMessage.error('请先登录')
    return
  }
  if (!planId.value) {
    ElMessage.error('学习计划不存在')
    goBack()
    return
  }
  const result = await request.get(`/api/study-plan/questions?userId=${userId}&planId=${planId.value}`)
  if (result.code === 0) {
    questions.value = result.data || []
    currentIndex.value = 0
    resetAnswerState()
    if (questions.value.length > 0) {
      phase.value = 'practice'
      return
    }
    if (await showPlanFinishedIfNeeded()) {
      return
    }
    phase.value = 'completed'
  } else {
    ElMessage.error(result.message || '加载题目失败')
  }
}

const loadWrongQuestions = async () => {
  const userId = getCurrentUserId()
  if (!userId) {
    ElMessage.error('请先登录')
    return
  }
  const result = await request.get(`/api/study-plan/wrong-questions?userId=${userId}&planId=${planId.value}`)
  if (result.code !== 0) {
    ElMessage.error(result.message || '加载错题失败')
    return
  }
  questions.value = result.data || []
  currentIndex.value = 0
  resetAnswerState()
  if (questions.value.length === 0) {
    if (await showPlanFinishedIfNeeded()) {
      return
    }
    phase.value = 'all-done'
    return
  }
  phase.value = 'review'
}

const resetAnswerState = () => {
  selectedOptionId.value = null
  selectedMulti.value = []
  fillAnswer.value = ''
  submitted.value = false
  answerCorrect.value = false
}

const isSelected = (optionId) => {
  if (currentQuestion.value.type === 3) {
    return selectedMulti.value.includes(optionId)
  }
  return selectedOptionId.value === optionId
}

const chooseOption = (optionId) => {
  if (submitted.value) return
  if (currentQuestion.value.type === 3) {
    if (selectedMulti.value.includes(optionId)) {
      selectedMulti.value = selectedMulti.value.filter(id => id !== optionId)
    } else {
      selectedMulti.value.push(optionId)
    }
    return
  }
  selectedOptionId.value = optionId
}

const calcCorrect = () => {
  const q = currentQuestion.value
  if (!q) return false
  if (q.type === 1) {
    const correct = (q.options || []).find(o => o.isCorrect === 1)
    return !!correct && correct.id === selectedOptionId.value
  }
  if (q.type === 3) {
    const correctIds = (q.options || []).filter(o => o.isCorrect === 1).map(o => o.id).sort()
    const userIds = [...selectedMulti.value].sort()
    return JSON.stringify(correctIds) === JSON.stringify(userIds)
  }
  if (q.type === 2) {
    const first = (q.fillAnswers || [])[0]
    if (!first || !first.answer) return false
    return fillAnswer.value.trim().toLowerCase() === first.answer.trim().toLowerCase()
  }
  return false
}

const submitAnswer = async () => {
  if (submitted.value) return
  const uid = getCurrentUserId()
  if (!uid) {
    ElMessage.error('请先登录')
    return
  }
  answerCorrect.value = calcCorrect()
  const result = await request.post(`/api/study-plan/questions/${currentQuestion.value.id}/submit?userId=${uid}&planId=${planId.value}&correct=${answerCorrect.value}`)
  if (result.code !== 0) {
    ElMessage.error(result.message || '提交失败')
    return
  }
  submitted.value = true
}

const finishCurrentBatch = async () => {
  ElMessage.success(phase.value === 'review' ? '错题复习已完成' : '今日题目已完成')
  if (await showPlanFinishedIfNeeded()) {
    return
  }
  phase.value = 'completed'
}

const nextQuestion = async () => {
  if (!submitted.value) return
  if (currentIndex.value >= questions.value.length - 1) {
    await finishCurrentBatch()
    return
  }
  currentIndex.value += 1
  resetAnswerState()
}

const typeText = (type) => {
  if (type === 1) return '单选题'
  if (type === 2) return '填空题'
  if (type === 3) return '多选题'
  return '题目'
}

const startWrongReview = async () => {
  await loadWrongQuestions()
}

const resetPlanProgress = async () => {
  const userId = getCurrentUserId()
  const result = await request.post(`/api/study-plan/${planId.value}/reset?userId=${userId}`)
  if (result.code !== 0) {
    ElMessage.error(result.message || '重置失败')
    return false
  }
  ElMessage.success('已重置学习进度')
  return true
}

const deletePlan = async () => {
  const userId = getCurrentUserId()
  const result = await request.delete(`/api/study-plan/${planId.value}?userId=${userId}`)
  if (result.code !== 0) {
    ElMessage.error(result.message || '删除失败')
    return false
  }
  ElMessage.success('学习计划已删除')
  return true
}

const handleResetFromDialog = async () => {
  if (!confirm('确定清空本计划全部学习进度并重新开始吗？')) {
    return
  }
  const ok = await resetPlanProgress()
  if (ok) {
    await loadQuestions()
  }
}

const handleDeleteFromDialog = async () => {
  if (!confirm('确定删除本学习计划吗？删除后不可恢复。')) {
    return
  }
  const ok = await deletePlan()
  if (ok) {
    goBack()
  }
}

const goBack = () => router.push('/home/study-plan')

onMounted(async () => {
  planId.value = Number(route.query.planId)
  await loadQuestions()
})
</script>

<style scoped>
.practice-page { width: 100%; position: relative; }
.back-btn { margin-bottom: 12px; cursor: pointer; }
.question-card, .state-card { background: #fff; border-radius: 8px; padding: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.q-header { display: flex; justify-content: space-between; margin-bottom: 10px; color: #666; }
.q-content { font-size: 18px; margin-bottom: 12px; }
.option-item { border: 1px solid #eee; border-radius: 6px; padding: 8px; margin-bottom: 8px; cursor: pointer; }
.option-item.selected { border-color: #42b983; background: #f0fbf6; }
.fill-wrap input { width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
.actions { margin-top: 12px; display: flex; gap: 10px; flex-wrap: wrap; }
.actions button { padding: 8px 16px; border-radius: 6px; border: 1px solid #dcdfe6; background: #fff; cursor: pointer; }
.actions button:first-child { background: #42b983; color: #fff; border-color: #42b983; }
.actions button:disabled { opacity: 0.5; cursor: not-allowed; }
.result { margin-top: 12px; font-weight: 600; }
.result.pass { color: #42b983; }
.result.fail { color: #f56c6c; }

.finish-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.finish-dialog {
  background: #fff;
  border-radius: 12px;
  padding: 28px 32px;
  max-width: 420px;
  width: 100%;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  text-align: center;
}

.finish-dialog h3 {
  margin: 0 0 12px;
  font-size: 1.25rem;
  color: #303133;
}

.finish-desc {
  margin: 0 0 24px;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.finish-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.finish-actions button {
  padding: 12px 20px;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
  border: none;
}

.btn-reset {
  background: #42b983;
  color: #fff;
}

.btn-reset:hover {
  background: #36a371;
}

.btn-delete {
  background: #fff;
  color: #f56c6c;
  border: 1px solid #fbc4c4 !important;
}

.btn-delete:hover {
  background: #fef0f0;
}

.btn-later {
  background: #f5f7fa;
  color: #606266;
  border: 1px solid #dcdfe6 !important;
}

.btn-later:hover {
  background: #ebeef5;
}
</style>
