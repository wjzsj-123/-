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
/* 与Login.vue样式完全一致，确保布局统一 */
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
  padding: 3.5rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  width: 90vw;
  max-width: 650px;
}

h2 {
  text-align: center;
  margin-bottom: 2.5rem;
  color: #1a1a1a;
  font-size: 2.2rem;
  font-weight: 600;
}

.form-group {
  margin-bottom: 2rem;
}

label {
  display: block;
  margin-bottom: 1rem;
  color: #4e5969;
  font-size: 1.3rem;
  font-weight: 500;
}

input {
  width: 100%;
  padding: 1.2rem;
  border: 1px solid #d0d5dd;
  border-radius: 8px;
  font-size: 1.3rem;
  transition: all 0.3s ease;
  height: 60px;
}

input:focus {
  outline: none;
  border-color: #42b983;
  box-shadow: 0 0 0 3px rgba(66, 185, 131, 0.2);
}

.error-message {
  color: #ff4d4f;
  margin: 1.5rem 0;
  padding: 1rem;
  border-radius: 6px;
  background-color: #fff2f0;
  text-align: center;
  font-size: 1.2rem;
}

.auth-btn {
  width: 100%;
  padding: 1.2rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.3rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 1.5rem;
  height: 60px;
}

.auth-btn:hover {
  background-color: #359e75;
}

.auth-switch {
  text-align: center;
  margin-top: 2rem;
  color: #667085;
  font-size: 1.2rem;
}

.auth-switch a {
  color: #42b983;
  text-decoration: none;
  font-weight: 500;
  margin-left: 6px;
  font-size: 1.2rem;
}

.auth-switch a:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .auth-card {
    padding: 2.5rem 1.5rem;
    width: 95vw;
  }

  h2 {
    font-size: 2rem;
  }

  input, .auth-btn {
    padding: 1rem;
    font-size: 1.2rem;
    height: 55px;
  }
}
</style>