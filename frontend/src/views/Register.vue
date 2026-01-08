<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>用户注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="username">用户名</label>
          <input
              type="text"
              id="username"
              v-model="form.username"
              required
              placeholder="请设置用户名"
          >
        </div>

        <div class="form-group">
          <label for="password">密码</label>
          <input
              type="password"
              id="password"
              v-model="form.password"
              required
              placeholder="请设置密码"
          >
        </div>

        <div class="form-group">
          <label for="nickname">昵称</label>
          <input
              type="text"
              id="nickname"
              v-model="form.nickname"
              placeholder="请设置昵称（选填）"
          >
        </div>

        <div class="form-group">
          <label for="email">邮箱</label>
          <input
              type="email"
              id="email"
              v-model="form.email"
              placeholder="请输入邮箱（选填）"
          >
        </div>

        <div class="error-message" v-if="errorMsg">{{ errorMsg }}</div>

        <button type="submit" class="auth-btn">注册</button>

        <div class="auth-switch">
          已有账号？<a href="#" @click.prevent="goToLogin">立即登录</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

// 获取路由实例
const router = useRouter()

// 表单数据 - 明确初始值为字符串类型，避免undefined导致trim()警告
const form = ref({
  username: '',
  password: '',
  nickname: '',
  email: ''
})

// 错误信息
const errorMsg = ref('')

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}

// 注册处理
const handleRegister = async () => {
  // 修复：先将值转为字符串再调用trim()，避免类型不确定导致的警告
  const username = String(form.value.username || '').trim()
  const password = String(form.value.password || '').trim()

  // 前端校验
  if (!username) {
    errorMsg.value = '用户名不能为空'
    return
  }
  if (!password) {
    errorMsg.value = '密码不能为空'
    return
  }

  try {
    const response = await fetch('/api/user/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        ...form.value,
        username: username, // 使用处理后的用户名
        password: password  // 使用处理后的密码
      })
    })
    const result = await response.json()

    if (result.code === 0) {
      // 注册成功，跳转到登录页
      errorMsg.value = ''
      alert('注册成功，请登录')
      await router.push('/login')
    } else {
      errorMsg.value = result.message || '注册失败'
    }
  } catch (err) {
    errorMsg.value = '网络错误，请稍后重试'
    console.error('注册失败:', err)
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
  padding: 15px;
}

.auth-card {
  width: 100%;
  max-width: 400px; /* 核心：缩小最大宽度 */
  padding: 2rem; /* 核心：减少内边距 */
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  width: 90vw;
  max-width: 400px;
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem; /* 减少间距 */
  color: #1a1a1a;
  font-size: 1.8rem; /* 核心：缩小字体 */
  font-weight: 600;
}

.form-group {
  margin-bottom: 1.2rem; /* 减少间距 */
}

label {
  display: block;
  margin-bottom: 0.6rem; /* 减少间距 */
  color: #4e5969;
  font-size: 1rem; /* 核心：缩小字体 */
  font-weight: 500;
}

input {
  width: 100%;
  padding: 0.8rem; /* 核心：减少内边距 */
  border: 1px solid #d0d5dd;
  border-radius: 8px;
  font-size: 1rem; /* 核心：缩小字体 */
  transition: all 0.3s ease;
  height: 45px; /* 核心：降低高度 */
}

.error-message {
  color: #ff4d4f;
  margin: 1rem 0; /* 减少间距 */
  padding: 0.8rem; /* 减少内边距 */
  border-radius: 6px;
  background-color: #fff2f0;
  text-align: center;
  font-size: 1rem; /* 核心：缩小字体 */
}

.auth-btn {
  width: 100%;
  padding: 0.8rem; /* 核心：减少内边距 */
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem; /* 核心：缩小字体 */
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 1rem; /* 减少间距 */
  height: 45px; /* 核心：降低高度 */
}

.auth-switch {
  text-align: center;
  margin-top: 1.2rem; /* 减少间距 */
  color: #667085;
  font-size: 1rem; /* 核心：缩小字体 */
}

.auth-switch a {
  color: #42b983;
  text-decoration: none;
  font-weight: 500;
  margin-left: 6px;
  font-size: 1rem; /* 核心：缩小字体 */
}

/* 响应式调整 */
@media (max-width: 768px) {
  .auth-card {
    padding: 1.5rem 1rem; /* 进一步缩小移动端内边距 */
    width: 95vw;
  }

  h2 {
    font-size: 1.6rem; /* 缩小移动端标题 */
  }

  input, .auth-btn {
    padding: 0.7rem; /* 缩小移动端内边距 */
    font-size: 0.95rem; /* 缩小移动端字体 */
    height: 40px; /* 降低移动端高度 */
  }
}
</style>