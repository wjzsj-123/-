<template>
  <div class="message-center">
    <h2>消息中心</h2>
    <p class="hint">将鼠标移入某条消息即可标记为已读。未读消息以高亮背景显示。</p>
    <div v-if="loading" class="state">加载中…</div>
    <div v-else-if="!items.length" class="state">暂无消息</div>
    <ul v-else class="msg-list">
      <li
        v-for="m in items"
        :key="m.id"
        class="msg-item"
        :class="{ unread: !m.readAt }"
        @mouseenter="markRead(m)"
      >
        <div class="msg-head">
          <span class="msg-title">{{ m.title }}</span>
          <span class="msg-time">{{ formatTime(m.createdAt) }}</span>
        </div>
        <p class="msg-preview">{{ m.contentPreview }}</p>
        <div class="msg-actions">
          <button
            v-if="m.type === 'FOLLOW_PUBLISH_SET' && m.refQuestionSetId"
            type="button"
            class="btn-copy"
            @click.stop="oneClickImportSet(m)"
          >
            一键复制题库
          </button>
          <button
            v-if="m.type === 'FOLLOW_PUBLISH_PAPER' && m.refPaperId"
            type="button"
            class="btn-copy"
            @click.stop="oneClickCopyPaper(m)"
          >
            一键复制试卷
          </button>
          <button
            v-if="m.type === 'DISCUSSION_COMMENT' && m.refQuestionSetId"
            type="button"
            class="btn-link"
            @click.stop="goDiscussion(m)"
          >
            查看评论
          </button>
        </div>
      </li>
    </ul>
    <div v-if="total > size" class="pager">
      <button type="button" :disabled="page <= 1" @click="changePage(page - 1)">上一页</button>
      <span>第 {{ page }} 页 / 共 {{ totalPages }} 页</span>
      <button type="button" :disabled="page >= totalPages" @click="changePage(page + 1)">下一页</button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(true)
const items = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(20)

const userId = () => {
  const raw = localStorage.getItem('userInfo')
  if (!raw) return null
  try {
    return JSON.parse(raw).id
  } catch {
    return null
  }
}

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / size.value)))

const formatTime = (t) => (t ? new Date(t).toLocaleString() : '')

const load = async () => {
  const uid = userId()
  if (!uid) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const res = await fetch(`/api/messages?userId=${uid}&page=${page.value}&size=${size.value}`)
    const json = await res.json()
    if (json.code === 0) {
      items.value = json.data?.list || []
      total.value = json.data?.total || 0
    } else {
      ElMessage.error(json.message || '加载失败')
    }
  } catch {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const markRead = async (m) => {
  if (m.readAt) return
  const uid = userId()
  if (!uid) return
  try {
    const res = await fetch(`/api/messages/${m.id}/read?userId=${uid}`, { method: 'POST' })
    const json = await res.json()
    if (json.code === 0) {
      m.readAt = new Date().toISOString()
    }
  } catch {
    /* ignore */
  }
}

const oneClickImportSet = async (m) => {
  const uid = userId()
  if (!uid) return
  try {
    const res = await fetch(`/api/question-set/public/import/${m.refQuestionSetId}?userId=${uid}`, { method: 'POST' })
    const json = await res.json()
    if (json.code === 0) {
      ElMessage.success('已复制到你的题库')
    } else {
      ElMessage.error(json.message || '复制失败')
    }
  } catch {
    ElMessage.error('网络错误')
  }
}

const oneClickCopyPaper = async (m) => {
  const uid = userId()
  if (!uid) return
  try {
    const res = await fetch(`/api/paper/${m.refPaperId}/copy?userId=${uid}`, { method: 'POST' })
    const json = await res.json()
    if (json.code === 0) {
      ElMessage.success('已复制到我的试卷')
    } else {
      ElMessage.error(json.message || '复制失败')
    }
  } catch {
    ElMessage.error('网络错误')
  }
}

const goDiscussion = (m) => {
  router.push({
    path: `/home/online-bank/${m.refQuestionSetId}/discussion`,
    query: { highlightCommentId: String(m.refCommentId) }
  })
}

const changePage = (p) => {
  page.value = p
  load()
}

onMounted(load)
</script>

<style scoped>
.message-center {
  width: 100%;
}
h2 {
  margin: 0 0 8px;
  color: #303133;
}
.hint {
  margin: 0 0 16px;
  color: #909399;
  font-size: 13px;
}
.state {
  padding: 32px;
  text-align: center;
  color: #909399;
}
.msg-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.msg-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 14px 16px;
  margin-bottom: 12px;
  background: #fff;
  transition: background 0.15s;
}
.msg-item.unread {
  background: linear-gradient(90deg, #fff7e6 0%, #fff 40%);
  border-color: #ffd591;
}
.msg-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}
.msg-title {
  font-weight: 600;
  color: #303133;
}
.msg-time {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}
.msg-preview {
  margin: 0 0 10px;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}
.msg-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.btn-copy,
.btn-link {
  padding: 6px 14px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 13px;
}
.btn-copy {
  background: #42b983;
  color: #fff;
}
.btn-link {
  background: #409eff;
  color: #fff;
}
.pager {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.pager button {
  padding: 6px 12px;
  cursor: pointer;
}
</style>
