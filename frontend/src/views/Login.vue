<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>用户登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名</label>
          <input
              type="text"
              id="username"
              v-model="form.username"
              required
              placeholder="请输入用户名"
          >
        </div>
        <div class="form-group">
          <label for="password">密码</label>
          <input
              type="password"
              id="password"
              v-model="form.password"
              required
              placeholder="请输入密码"
          >
        </div>
        <div class="error-message" v-if="errorMsg">{{ errorMsg }}</div>
        <button type="submit" class="auth-btn">登录</button>
        <div class="auth-switch">
          还没有账号？<a href="#" @click.prevent="goToRegister">立即注册</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router' // 导入路由钩子

// 获取路由实例
const router = useRouter()

// 表单数据
const form = ref({
  username: '',
  password: ''
})

// 错误信息
const errorMsg = ref('')

// 跳转到注册页（替代直接使用 $router）
const goToRegister = () => {
  router.push('/register')
}

// 登录处理
const handleLogin = async () => {
  try {
    const response = await fetch('/api/user/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form.value)
    })
    const result = await response.json()

    if (result.code === 0) {
      // 登录成功，存储用户信息并跳转首页
      localStorage.setItem('userInfo', JSON.stringify(result.data))
      await router.push('/') // 使用路由实例跳转
    } else {
      errorMsg.value = result.message || '登录失败'
    }
  } catch (err) {
    errorMsg.value = '网络错误，请稍后重试'
    console.error('登录失败:', err)
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
  padding: 15px; /* 减少外间距 */
}

.auth-card {
  width: 100%;
  max-width: 600px; /* 增大最大宽度 */
  padding: 3.5rem; /* 增加内边距 */
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  width: 90vw; /* 占屏幕宽度90% */
  max-width: 650px;
}

h2 {
  text-align: center;
  margin-bottom: 2.5rem;
  color: #1a1a1a;
  font-size: 2.2rem; /* 增大标题 */
  font-weight: 600;
}

.form-group {
  margin-bottom: 2rem; /* 增加表单间距 */
}

label {
  display: block;
  margin-bottom: 1rem;
  color: #4e5969;
  font-size: 1.3rem; /* 增大标签 */
  font-weight: 500;
}

input {
  width: 100%;
  padding: 1.2rem; /* 增大输入框内边距 */
  border: 1px solid #d0d5dd;
  border-radius: 8px;
  font-size: 1.3rem; /* 增大输入文字 */
  transition: all 0.3s ease;
  height: 60px; /* 固定高度增加 */
}

.error-message {
  color: #ff4d4f;
  margin: 1.5rem 0;
  padding: 1rem;
  border-radius: 6px;
  background-color: #fff2f0;
  text-align: center;
  font-size: 1.2rem; /* 增大错误提示 */
}

.auth-btn {
  width: 100%;
  padding: 1.2rem; /* 增大按钮内边距 */
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1.3rem; /* 增大按钮文字 */
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 1.5rem;
  height: 60px; /* 固定按钮高度 */
}

.auth-switch {
  text-align: center;
  margin-top: 2rem;
  color: #667085;
  font-size: 1.2rem; /* 增大切换文字 */
}

.auth-switch a {
  color: #42b983;
  text-decoration: none;
  font-weight: 500;
  margin-left: 6px;
  font-size: 1.2rem;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .auth-card {
    padding: 2.5rem 1.5rem;
    width: 95vw; /* 小屏幕占95%宽度 */
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