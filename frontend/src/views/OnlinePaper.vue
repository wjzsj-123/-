<template>
  <div class="online-bank-container">
    <div class="search-bar">
      <div class="search-item">
        <label>试卷名称：</label>
        <input
          type="text"
          v-model="searchParams.name"
          placeholder="请输入试卷名称模糊搜索"
          @keyup.enter="fetchPublicPapers"
        />
      </div>
      <div class="search-item">
        <label>排序方式：</label>
        <select v-model="searchParams.sortBy" @change="onSortChange">
          <option value="publishTime">按发布时间</option>
          <option value="hot">按热度</option>
        </select>
      </div>
      <button class="search-btn" @click="fetchPublicPapers">搜索</button>
      <button class="reset-btn" @click="resetSearch">重置</button>
    </div>

    <div class="bank-list">
      <div class="bank-card" v-for="paper in papers" :key="paper.id">
        <div class="bank-header">
          <p><span class="label">试卷ID：</span>{{ paper.id }}</p>
          <div class="publisher-line" v-if="paper.userId">
            <router-link class="publisher-name" :to="'/home/user/center/' + paper.userId">
              {{ publisherDisplayName(paper) }}
            </router-link>
            <button
              v-if="canFollowPublisher(paper)"
              type="button"
              class="follow-pub-btn"
              @click.stop="toggleFollowPublisher(paper)"
            >
              {{ paper.viewerFollowsPublisher ? '取消关注' : '关注' }}
            </button>
          </div>
          <span v-else class="publisher">发布者：—</span>
        </div>
        <div class="bank-body">
          <h3>{{ paper.title }}</h3>
          <p><span class="label">总题数：</span>{{ paper.totalQuestions || 0 }}</p>
          <p><span class="label">发布时间：</span>{{ formatTime(paper.createTime) }}</p>
          <p><span class="label">热度：</span>{{ paper.hotScore || 0 }}（作答 {{ paper.answerCount || 0 }} / 复制 {{ paper.copyCount || 0 }}）</p>
          <div class="tag-list">
            <span class="label">题目标签：</span>
            <template v-if="paper.questionTags && paper.questionTags.length">
              <span
                v-for="tag in paper.questionTags"
                :key="`${paper.id}-${tag}`"
                class="tag-item"
              >
                {{ tag }}
              </span>
            </template>
            <span v-else class="tag-empty">暂无标签</span>
          </div>
        </div>
        <div class="bank-footer">
          <button class="import-btn" @click="copyPaper(paper.id)">复制为我的试卷</button>
        </div>
      </div>
    </div>

    <div class="empty-tip" v-if="papers.length === 0 && !loading">暂无公开试卷数据</div>
    <div class="loading-tip" v-if="loading">加载中...</div>

    <div v-if="total > 0" class="pagination">
      <button :disabled="page <= 1 || loading" @click="changePage(page - 1)">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页（共 {{ total }} 条）</span>
      <button :disabled="page >= totalPages || loading" @click="changePage(page + 1)">下一页</button>
      <span>每页</span>
      <select v-model="size" :disabled="loading" @change="onPageSizeChange">
        <option value="5">5</option>
        <option value="10">10</option>
        <option value="20">20</option>
        <option value="50">50</option>
      </select>
      <span>条</span>
      <input
          v-model="jumpPageInput"
          type="number"
          min="1"
          :max="totalPages"
          class="jump-input"
          :disabled="loading"
          @keyup.enter="jumpToPage"
      />
      <button :disabled="loading" @click="jumpToPage">跳转</button>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { displayUserName } from '@/utils/userDisplay'

const papers = ref([])
const loading = ref(false)
const searchParams = ref({
  name: '',
  sortBy: 'publishTime'
})
const currentUserId = ref('')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const totalPages = ref(1)
const jumpPageInput = ref('1')

const getCurrentUserId = () => {
  const userInfo = localStorage.getItem('userInfo')
  return userInfo ? JSON.parse(userInfo).id : ''
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString()
}

const fetchPublicPapers = async () => {
  loading.value = true
  try {
    const params = new URLSearchParams()
    if (searchParams.value.name.trim()) params.append('name', searchParams.value.name.trim())
    params.append('sortBy', searchParams.value.sortBy)
    params.append('page', String(page.value))
    params.append('size', String(size.value))
    if (currentUserId.value) params.append('currentUserId', String(currentUserId.value))
    const response = await fetch(`/api/paper/public?${params.toString()}`)
    const result = await response.json()
    if (result.code === 0) {
      papers.value = result.data?.list || []
      total.value = result.data?.total || 0
      totalPages.value = Math.max(1, Math.ceil(total.value / size.value))
      if (page.value > totalPages.value) {
        page.value = totalPages.value
      }
      jumpPageInput.value = String(page.value)
    } else {
      ElMessage.error(result.message || '查询在线试卷失败')
      papers.value = []
      total.value = 0
      totalPages.value = 1
      jumpPageInput.value = '1'
    }
  } catch (err) {
    ElMessage.error('网络错误，查询在线试卷失败')
    papers.value = []
    total.value = 0
    totalPages.value = 1
  } finally {
    loading.value = false
  }
}

const copyPaper = async (paperId) => {
  if (!currentUserId.value) {
    ElMessage.error('请先登录')
    return
  }
  try {
    const response = await fetch(`/api/paper/${paperId}/copy?userId=${currentUserId.value}`, {
      method: 'POST'
    })
    const result = await response.json()
    if (result.code === 0) {
      ElMessage.success('复制成功，已加入我的试卷')
      await fetchPublicPapers()
    } else {
      ElMessage.error(result.message || '复制失败')
    }
  } catch (err) {
    ElMessage.error('网络错误，复制失败')
  }
}

const resetSearch = () => {
  searchParams.value.name = ''
  searchParams.value.sortBy = 'publishTime'
  page.value = 1
  size.value = 10
  jumpPageInput.value = '1'
  fetchPublicPapers()
}

const changePage = async (targetPage) => {
  if (targetPage < 1 || targetPage > totalPages.value) return
  page.value = targetPage
  await fetchPublicPapers()
}

const onPageSizeChange = async () => {
  const parsed = Number(size.value)
  size.value = [5, 10, 20, 50].includes(parsed) ? parsed : 10
  page.value = 1
  await fetchPublicPapers()
}

const jumpToPage = async () => {
  const target = Number(jumpPageInput.value)
  if (!Number.isFinite(target)) {
    ElMessage.warning('请输入有效页码')
    jumpPageInput.value = String(page.value)
    return
  }
  const normalized = Math.min(Math.max(Math.floor(target), 1), totalPages.value)
  if (normalized === page.value) {
    jumpPageInput.value = String(page.value)
    return
  }
  await changePage(normalized)
}

const onSortChange = async () => {
  page.value = 1
  await fetchPublicPapers()
}

const publisherDisplayName = (paper) => displayUserName(paper)

const canFollowPublisher = (paper) => {
  if (!paper.userId || !currentUserId.value) return false
  return String(paper.userId) !== String(currentUserId.value)
}

const toggleFollowPublisher = async (paper) => {
  if (!canFollowPublisher(paper)) return
  const uid = paper.userId
  const following = !!paper.viewerFollowsPublisher
  try {
    const url = `/api/user-center/follow?followerId=${currentUserId.value}&followeeId=${uid}`
    const response = await fetch(url, { method: following ? 'DELETE' : 'POST' })
    const result = await response.json()
    if (result.code === 0) {
      ElMessage.success(following ? '已取消关注' : '关注成功')
      await fetchPublicPapers()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch {
    ElMessage.error('网络错误')
  }
}

onMounted(async () => {
  currentUserId.value = getCurrentUserId()
  await fetchPublicPapers()
})
</script>

<style scoped>
.online-bank-container {
  width: 100%;
}
.search-bar {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.search-item { display: flex; align-items: center; gap: 8px; }
.search-item label { color: #333; font-weight: 500; }
.search-item select, .search-item input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  min-width: 180px;
}
.search-btn, .reset-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}
.search-btn { background: #42b983; color: #fff; }
.reset-btn { background: #f5f5f5; color: #666; }
.bank-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}
.bank-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.bank-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 10px;
}
.label { color: #666; font-weight: 500; }
.publisher { font-size: 12px; color: #999; }
.publisher-line {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}
.publisher-name {
  color: #409eff;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
}
.publisher-name:hover { text-decoration: underline; }
.follow-pub-btn {
  padding: 4px 12px;
  font-size: 13px;
  border: 1px solid #42b983;
  background: #fff;
  color: #42b983;
  border-radius: 4px;
  cursor: pointer;
}
.follow-pub-btn:hover { background: #f0fff6; }
.tag-list { margin-top: 8px; line-height: 1.8; }
.tag-item {
  display: inline-block;
  margin-right: 6px;
  margin-bottom: 6px;
  padding: 2px 8px;
  border-radius: 12px;
  background: #eef6ff;
  color: #409eff;
  font-size: 12px;
}
.tag-empty { color: #999; font-size: 12px; }
.bank-footer {
  text-align: right;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
.import-btn {
  padding: 8px 18px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}
.import-btn:hover { background: #66b1ff; }
.empty-tip, .loading-tip {
  text-align: center;
  padding: 50px;
  color: #999;
  font-size: 16px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.pagination button {
  padding: 4px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}
.pagination select {
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
}
.jump-input {
  width: 70px;
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
</style>
