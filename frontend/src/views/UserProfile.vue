<template>
  <div class="profile-container">
    <h3>个人信息页面</h3>
    <div class="profile-content">
      <div v-if="loading" class="state">加载中…</div>
      <form v-else class="profile-form">
        <div class="form-group">
          <label for="nickname">昵称</label>
          <input type="text" id="nickname" v-model="profile.nickname" placeholder="请输入昵称">
        </div>
        <div class="form-group">
          <label for="email">邮箱</label>
          <input type="email" id="email" v-model="profile.email" placeholder="请输入邮箱">
        </div>
        <div class="form-group">
          <label for="username">用户名</label>
          <input type="text" id="username" v-model="profile.username" disabled placeholder="用户名不可修改">
        </div>
        <button type="button" class="save-btn" :disabled="saving || loading" @click="saveProfile">
          {{ saving ? '保存中…' : '保存修改' }}
        </button>
        <p v-if="errorMsg" class="error-msg">{{ errorMsg }}</p>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(true)
const saving = ref(false)
const errorMsg = ref('')

// 个人信息数据
const profile = ref({
  id: null,
  username: '',
  nickname: '',
  email: ''
})

const applyProfile = (user) => {
  profile.value = {
    id: user.id ?? null,
    username: user.username || '',
    nickname: user.nickname || '',
    email: user.email || ''
  }
}

const syncLocalUserInfo = (user) => {
  const oldUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const newUserInfo = { ...oldUserInfo, ...user }
  localStorage.setItem('userInfo', JSON.stringify(newUserInfo))
  window.dispatchEvent(new CustomEvent('user-info-updated', { detail: newUserInfo }))
}

// 页面加载时从服务端获取最新用户信息
onMounted(async () => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  if (!userInfo.id) {
    errorMsg.value = '未找到登录信息，请重新登录'
    loading.value = false
    router.push('/login')
    return
  }

  try {
    const result = await request.get(`/api/user/${userInfo.id}`)
    if (result.code === 0 && result.data) {
      applyProfile(result.data)
      syncLocalUserInfo(result.data)
    } else {
      applyProfile(userInfo)
      errorMsg.value = result.message || '加载用户信息失败'
    }
  } catch (err) {
    applyProfile(userInfo)
    errorMsg.value = '网络错误，已显示本地缓存信息'
    console.error('加载用户信息失败:', err)
  } finally {
    loading.value = false
  }
})

// 保存个人信息
const saveProfile = async () => {
  if (!profile.value.id) {
    errorMsg.value = '未找到用户 ID，请重新登录'
    return
  }

  saving.value = true
  errorMsg.value = ''

  try {
    const result = await request.put('/api/user', {
      id: profile.value.id,
      username: profile.value.username,
      nickname: profile.value.nickname.trim(),
      email: profile.value.email.trim()
    })

    if (result.code === 0) {
      const updatedUser = {
        id: profile.value.id,
        username: profile.value.username,
        nickname: profile.value.nickname.trim(),
        email: profile.value.email.trim()
      }
      syncLocalUserInfo(updatedUser)
      alert('个人信息保存成功')
    } else {
      errorMsg.value = result.message || '保存失败'
    }
  } catch (err) {
    errorMsg.value = '网络错误，请稍后重试'
    console.error('保存个人信息失败:', err)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-container {
  width: 100%;
  min-height: 0;
  padding: 0;
  background-color: transparent;
}

.profile-content {
  margin-top: 0;
  padding: 2rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  max-width: 520px;
  margin-left: auto;
  margin-right: auto;
}

.profile-form {
  width: 100%;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #666;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

input:focus {
  outline: none;
  border-color: #42b983;
}

.save-btn {
  width: 100%;
  padding: 0.8rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.save-btn:hover:not(:disabled) {
  background-color: #359e75;
}

.save-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.error-msg {
  margin-top: 1rem;
  color: #ff4d4f;
  text-align: center;
  font-size: 0.95rem;
}

h3 {
  color: #333;
  margin-bottom: 0.5rem;
}

.state {
  text-align: center;
  color: #666;
  padding: 2rem 0;
}
</style>