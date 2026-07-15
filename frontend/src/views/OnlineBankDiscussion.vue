<template>
  <div class="discussion-page">
    <div class="page-header">
      <button class="back-btn" @click="goBack">返回在线题库</button>
      <h2>{{ questionSet.name || '题库讨论区' }}</h2>
      <p class="desc">{{ questionSet.description || '欢迎留下你对这个题库的评价' }}</p>
    </div>

    <div class="comment-editor">
      <div class="editor-row">
        <label>评价：</label>
        <select v-model="newComment.sentiment">
          <option value="1">好评</option>
          <option value="-1">差评</option>
        </select>
      </div>
      <textarea
          v-model="newComment.content"
          rows="4"
          maxlength="500"
          placeholder="请输入你的评论（最多500字）"
      />
      <div class="editor-actions">
        <span class="count">{{ newComment.content.length }}/500</span>
        <button class="submit-btn" @click="submitComment">发布评论</button>
      </div>
    </div>

    <div class="comment-toolbar">
      <label>排序方式：</label>
      <select v-model="sortBy" @change="onSortChange">
        <option value="latest">最新发布</option>
        <option value="hot">点赞最多</option>
      </select>
    </div>

    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="comments.length === 0" class="empty">暂无评论，来当第一个评论的人吧</div>
    <div v-else class="comment-list">
      <div v-for="item in comments" :key="item.id" :id="'comment-' + item.id" class="comment-card">
        <div class="meta">
          <router-link class="user user-link" :to="'/home/user/center/' + item.userId">
            {{ displayUserName(item) }}
          </router-link>
          <span :class="['sentiment', item.sentiment === 1 ? 'good' : 'bad']">
            {{ item.sentiment === 1 ? '好评' : '差评' }}
          </span>
          <span class="time">{{ formatTime(item.createTime) }}</span>
        </div>
        <div class="content">{{ item.content }}</div>
        <div class="actions">
          <button
              class="like-btn"
              :class="{ liked: item.likedByCurrentUser }"
              @click="toggleLike(item)"
          >
            {{ item.likedByCurrentUser ? '取消点赞' : '点赞' }} ({{ item.likeCount || 0 }})
          </button>
          <button
              v-if="canDelete(item)"
              class="delete-btn"
              @click="deleteComment(item)"
          >
            删除
          </button>
        </div>
      </div>
    </div>

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
import { onMounted, ref, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { displayUserName } from '@/utils/userDisplay'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const setId = route.params.id

const questionSet = ref({})
const comments = ref([])
const loading = ref(false)
const sortBy = ref('latest')
const page = ref(1)
const size = ref(10)
const total = ref(0)
const totalPages = ref(1)
const currentUserId = ref(null)
const jumpPageInput = ref('1')

const newComment = ref({
  sentiment: '1',
  content: ''
})

const getCurrentUserId = () => {
  const userInfo = localStorage.getItem('userInfo')
  return userInfo ? JSON.parse(userInfo).id : null
}

const loadQuestionSet = async () => {
  try {
    const result = await request.get(`/api/question-set/${setId}`)
    if (result.code === 0) {
      questionSet.value = result.data || {}
      if (questionSet.value.isPublic !== 1) {
        ElMessage.warning('该题库当前为私有状态，讨论区内容已隐藏')
      }
    } else {
      ElMessage.error(result.message || '题库信息获取失败')
    }
  } catch (e) {
    ElMessage.error('题库信息获取失败')
  }
}

const loadComments = async () => {
  loading.value = true
  try {
    currentUserId.value = getCurrentUserId()
    if (route.query.highlightCommentId) {
      page.value = 1
    }
    const params = new URLSearchParams()
    params.append('sortBy', sortBy.value)
    params.append('page', String(page.value))
    params.append('size', String(size.value))
    if (route.query.highlightCommentId) {
      params.set('size', '50')
      params.set('page', '1')
    }
    if (currentUserId.value) {
      params.append('currentUserId', currentUserId.value)
    }
    const result = await request.get(`/api/question-set/public/${setId}/comments?${params.toString()}`)
    if (result.code === 0) {
      comments.value = result.data?.list || []
      total.value = result.data?.total || 0
      totalPages.value = Math.max(1, Math.ceil(total.value / size.value))
      if (page.value > totalPages.value) {
        page.value = totalPages.value
      }
      jumpPageInput.value = String(page.value)
    } else {
      comments.value = []
      total.value = 0
      totalPages.value = 1
      jumpPageInput.value = '1'
      ElMessage.error(result.message || '获取评论失败')
    }
  } catch (e) {
    ElMessage.error('网络错误，获取评论失败')
  } finally {
    loading.value = false
  }
  await scrollToHighlight()
}

const scrollToHighlight = async () => {
  const cid = route.query.highlightCommentId
  if (!cid) return
  await nextTick()
  const el = document.getElementById('comment-' + cid)
  if (el) {
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    el.classList.add('highlight-flash')
    setTimeout(() => el.classList.remove('highlight-flash'), 2600)
  }
}

const submitComment = async () => {
  const userId = getCurrentUserId()
  if (!userId) {
    ElMessage.error('请先登录')
    return
  }
  if (!newComment.value.content.trim()) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  try {
    const result = await request.post(`/api/question-set/public/${setId}/comments`, {
      sentiment: Number(newComment.value.sentiment),
      content: newComment.value.content.trim()
    })
    if (result.code === 0) {
      ElMessage.success('评论发布成功')
      newComment.value.content = ''
      page.value = 1
      await loadComments()
    } else {
      ElMessage.error(result.message || '评论发布失败')
    }
  } catch (e) {
    ElMessage.error('网络错误，评论发布失败')
  }
}

const toggleLike = async (item) => {
  const userId = currentUserId.value || getCurrentUserId()
  if (!userId) {
    ElMessage.error('请先登录')
    return
  }
  try {
    const result = await request.post(`/api/question-set/public/${setId}/comments/${item.id}/like`)
    if (result.code === 0) {
      await loadComments()
    } else {
      ElMessage.error(result.message || '点赞失败')
    }
  } catch (e) {
    ElMessage.error('网络错误，点赞失败')
  }
}

const canDelete = (item) => {
  const uid = currentUserId.value || getCurrentUserId()
  if (!uid) return false
  return item.userId === uid || questionSet.value.userId === uid
}

const deleteComment = async (item) => {
  const uid = currentUserId.value || getCurrentUserId()
  if (!uid) {
    ElMessage.error('请先登录')
    return
  }
  if (!confirm('确认删除该评论吗？')) {
    return
  }
  try {
    const result = await request.delete(`/api/question-set/public/${setId}/comments/${item.id}`)
    if (result.code === 0) {
      ElMessage.success('评论删除成功')
      if (comments.value.length === 1 && page.value > 1) {
        page.value -= 1
      }
      await loadComments()
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (e) {
    ElMessage.error('网络错误，删除失败')
  }
}

const changePage = async (targetPage) => {
  if (targetPage < 1 || targetPage > totalPages.value) return
  page.value = targetPage
  await loadComments()
}

const onSortChange = async () => {
  page.value = 1
  await loadComments()
}

const onPageSizeChange = async () => {
  const parsed = Number(size.value)
  if (![5, 10, 20, 50].includes(parsed)) {
    size.value = 10
  } else {
    size.value = parsed
  }
  page.value = 1
  await loadComments()
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

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString()
}

const goBack = () => {
  router.push('/home/online-bank')
}

onMounted(async () => {
  currentUserId.value = getCurrentUserId()
  await loadQuestionSet()
  await loadComments()
})

watch(
  () => route.query.highlightCommentId,
  async () => {
    await loadComments()
  }
)
</script>

<style scoped>
.discussion-page {
  width: 100%;
}

.page-header {
  margin-bottom: 20px;
}

.back-btn {
  margin-bottom: 12px;
  padding: 6px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}

.desc {
  color: #666;
}

.comment-editor {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.editor-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

textarea {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  resize: vertical;
  box-sizing: border-box;
}

.editor-actions {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.count {
  color: #999;
  font-size: 12px;
}

.submit-btn {
  padding: 8px 14px;
  border: none;
  border-radius: 4px;
  background: #409eff;
  color: #fff;
  cursor: pointer;
}

.comment-toolbar {
  margin-bottom: 12px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-card {
  background: #fff;
  border-radius: 8px;
  padding: 14px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  font-size: 13px;
}

.user {
  font-weight: 600;
}

.user-link {
  color: #409eff;
  text-decoration: none;
}

.user-link:hover {
  text-decoration: underline;
}

.comment-card.highlight-flash {
  animation: hl 1.2s ease 2;
  outline: 2px solid #409eff;
}

@keyframes hl {
  0% { box-shadow: 0 0 0 0 rgba(64, 158, 255, 0.6); }
  50% { box-shadow: 0 0 0 6px rgba(64, 158, 255, 0.2); }
  100% { box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); }
}

.sentiment {
  padding: 2px 8px;
  border-radius: 10px;
  color: #fff;
}

.sentiment.good {
  background: #67c23a;
}

.sentiment.bad {
  background: #f56c6c;
}

.time {
  color: #999;
}

.content {
  line-height: 1.6;
  margin-bottom: 10px;
  white-space: pre-wrap;
}

.actions {
  text-align: right;
}

.like-btn {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  padding: 4px 10px;
  cursor: pointer;
}

.like-btn.liked {
  color: #409eff;
  border-color: #409eff;
}

.delete-btn {
  margin-left: 10px;
  border: 1px solid #f56c6c;
  color: #f56c6c;
  border-radius: 4px;
  background: #fff;
  padding: 4px 10px;
  cursor: pointer;
}

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
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

.loading, .empty {
  text-align: center;
  color: #999;
  padding: 30px 0;
}
</style>
