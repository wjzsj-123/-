<template>
  <div class="user-center-page">
    <div v-if="loading" class="state">加载中…</div>
    <div v-else-if="errorMsg" class="state error">{{ errorMsg }}</div>
    <template v-else-if="center">
      <div class="hero-card">
        <div class="hero-main">
          <h2 class="display-name">{{ center.nickname || center.username }}</h2>
          <p class="sub">@{{ center.username }}</p>
          <p v-if="center.ownProfile && center.email" class="email">邮箱：{{ center.email }}</p>
          <p v-if="!center.ownProfile" class="hint">用户 ID：{{ center.id }}</p>
          <p v-else class="hint">用户 ID：{{ center.id }}</p>
        </div>
        <div class="hero-side" v-if="!center.ownProfile && currentUserId">
          <button
            v-if="!center.viewerFollows"
            type="button"
            class="btn-primary"
            :disabled="followBusy"
            @click="doFollow"
          >
            关注
          </button>
          <button
            v-else
            type="button"
            class="btn-outline"
            :disabled="followBusy"
            @click="doUnfollow"
          >
            取消关注
          </button>
        </div>
      </div>

      <div class="stats-row">
        <button type="button" class="stat-btn" @click="openFollowing">
          <span class="num">{{ center.followingCount }}</span>
          <span class="lbl">关注</span>
        </button>
        <button type="button" class="stat-btn" @click="openFollowers">
          <span class="num">{{ center.followerCount }}</span>
          <span class="lbl">粉丝</span>
        </button>
      </div>

      <section class="panel">
        <h3>公开题库</h3>
        <div v-if="!center.publicQuestionSets?.length" class="empty">暂无公开题库</div>
        <ul v-else class="item-list">
          <li v-for="s in center.publicQuestionSets" :key="s.id">
            <span class="item-title">{{ s.name }}</span>
            <span class="item-meta">{{ s.category }} · {{ formatTime(s.publishTime) }}</span>
          </li>
        </ul>
      </section>

      <section class="panel">
        <h3>公开试卷</h3>
        <div v-if="!center.publicPapers?.length" class="empty">暂无公开试卷</div>
        <ul v-else class="item-list">
          <li v-for="p in center.publicPapers" :key="p.id">
            <span class="item-title">{{ p.title }}</span>
            <span class="item-meta">{{ p.totalQuestions || 0 }} 题 · {{ formatTime(p.createTime) }}</span>
          </li>
        </ul>
      </section>
    </template>

    <div v-if="listModal.open" class="modal-mask" @click.self="listModal.open = false">
      <div class="modal">
        <div class="modal-head">
          <h4>{{ listModal.title }}</h4>
          <button type="button" class="modal-close" @click="listModal.open = false">×</button>
        </div>
        <div class="modal-body">
          <div v-if="listModal.loading" class="state">加载中…</div>
          <ul v-else-if="listModal.users.length" class="user-list">
            <li v-for="u in listModal.users" :key="u.id">
              <router-link :to="'/home/user/center/' + u.id" @click="listModal.open = false">
                {{ u.nickname || u.username }}
              </router-link>
              <span class="uname">@{{ u.username }}</span>
            </li>
          </ul>
          <div v-else class="empty">暂无数据</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const route = useRoute()
const loading = ref(true)
const errorMsg = ref('')
const center = ref(null)
const followBusy = ref(false)

const currentUserId = computed(() => {
  const raw = localStorage.getItem('userInfo')
  if (!raw) return null
  try {
    return JSON.parse(raw).id ?? null
  } catch {
    return null
  }
})

const listModal = reactive({
  open: false,
  title: '',
  loading: false,
  users: []
})

const formatTime = (t) => {
  if (!t) return '—'
  return new Date(t).toLocaleString()
}

const loadCenter = async () => {
  const userId = Number(route.params.userId)
  if (!Number.isFinite(userId)) {
    errorMsg.value = '无效的用户'
    center.value = null
    loading.value = false
    return
  }
  loading.value = true
  errorMsg.value = ''
  try {
    const q = new URLSearchParams()
    if (currentUserId.value) q.append('viewerId', String(currentUserId.value))
    const res = await fetch(`/api/user-center/${userId}?${q.toString()}`)
    const json = await res.json()
    if (json.code === 0) {
      center.value = json.data
    } else {
      errorMsg.value = json.message || '加载失败'
      center.value = null
    }
  } catch {
    errorMsg.value = '网络错误'
    center.value = null
  } finally {
    loading.value = false
  }
}

const doFollow = async () => {
  if (!currentUserId.value || !center.value) return
  followBusy.value = true
  try {
    const res = await fetch(
      `/api/user-center/follow?followerId=${currentUserId.value}&followeeId=${center.value.id}`,
      { method: 'POST' }
    )
    const json = await res.json()
    if (json.code === 0) {
      ElMessage.success('已关注')
      await loadCenter()
    } else {
      ElMessage.error(json.message || '关注失败')
    }
  } catch {
    ElMessage.error('网络错误')
  } finally {
    followBusy.value = false
  }
}

const doUnfollow = async () => {
  if (!currentUserId.value || !center.value) return
  followBusy.value = true
  try {
    const res = await fetch(
      `/api/user-center/follow?followerId=${currentUserId.value}&followeeId=${center.value.id}`,
      { method: 'DELETE' }
    )
    const json = await res.json()
    if (json.code === 0) {
      ElMessage.success('已取消关注')
      await loadCenter()
    } else {
      ElMessage.error(json.message || '操作失败')
    }
  } catch {
    ElMessage.error('网络错误')
  } finally {
    followBusy.value = false
  }
}

const openFollowing = async () => {
  const uid = center.value?.id
  if (!uid) return
  listModal.open = true
  listModal.title = '关注列表'
  listModal.loading = true
  listModal.users = []
  try {
    const res = await fetch(`/api/user-center/${uid}/following`)
    const json = await res.json()
    listModal.users = json.code === 0 ? json.data || [] : []
  } finally {
    listModal.loading = false
  }
}

const openFollowers = async () => {
  const uid = center.value?.id
  if (!uid) return
  listModal.open = true
  listModal.title = '粉丝列表'
  listModal.loading = true
  listModal.users = []
  try {
    const res = await fetch(`/api/user-center/${uid}/followers`)
    const json = await res.json()
    listModal.users = json.code === 0 ? json.data || [] : []
  } finally {
    listModal.loading = false
  }
}

onMounted(loadCenter)
watch(
  () => route.params.userId,
  () => loadCenter()
)
</script>

<style scoped>
.user-center-page {
  width: 100%;
}
.state {
  text-align: center;
  padding: 40px;
  color: #909399;
}
.state.error {
  color: #f56c6c;
}
.hero-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 16px;
}
.display-name {
  margin: 0 0 6px;
  font-size: 1.35rem;
  color: #303133;
}
.sub {
  margin: 0 0 8px;
  color: #909399;
  font-size: 14px;
}
.email,
.hint {
  margin: 4px 0 0;
  font-size: 14px;
  color: #606266;
}
.hero-side {
  flex-shrink: 0;
}
.btn-primary {
  background: #42b983;
  color: #fff;
  border: none;
  border-radius: 6px;
  padding: 10px 22px;
  font-weight: 600;
  cursor: pointer;
}
.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn-outline {
  background: #fff;
  color: #42b983;
  border: 1px solid #42b983;
  border-radius: 6px;
  padding: 10px 22px;
  font-weight: 600;
  cursor: pointer;
}
.stats-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.stat-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 14px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s;
}
.stat-btn:hover {
  border-color: #42b983;
  background: #f6fffb;
}
.stat-btn .num {
  font-size: 1.25rem;
  font-weight: 700;
  color: #303133;
}
.stat-btn .lbl {
  font-size: 13px;
  color: #909399;
}
.panel {
  background: #fff;
  border-radius: 8px;
  padding: 18px 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.panel h3 {
  margin: 0 0 12px;
  font-size: 1rem;
  color: #303133;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}
.empty {
  color: #909399;
  font-size: 14px;
  padding: 8px 0;
}
.item-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.item-list li {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.item-list li:last-child {
  border-bottom: none;
}
.item-title {
  font-weight: 500;
  color: #303133;
}
.item-meta {
  font-size: 13px;
  color: #909399;
}
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 16px;
}
.modal {
  background: #fff;
  border-radius: 8px;
  width: 100%;
  max-width: 420px;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}
.modal-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #ebeef5;
}
.modal-head h4 {
  margin: 0;
  font-size: 16px;
}
.modal-close {
  border: none;
  background: transparent;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  color: #909399;
}
.modal-body {
  padding: 12px 16px 16px;
  overflow-y: auto;
}
.user-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.user-list li {
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.user-list a {
  color: #409eff;
  font-weight: 500;
  text-decoration: none;
}
.user-list a:hover {
  text-decoration: underline;
}
.uname {
  font-size: 12px;
  color: #909399;
}
</style>
