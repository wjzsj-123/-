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
      // 登录成功后先清除可能的旧数据
      localStorage.removeItem('userInfo')
      // 存储用户信息
      localStorage.setItem('userInfo', JSON.stringify(result.data))
      // 强制刷新路由以确保Home组件重新加载
      await router.push('/home')
      // 可选：如果仍然有问题，可以添加页面刷新
      // window.location.reload()
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
  max-width: 400px; /* 核心：缩小最大宽度（原650px） */
  padding: 2rem; /* 核心：减少内边距（原3.5rem） */
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  width: 90vw; /* 占屏幕宽度90% */
  max-width: 400px; /* 核心：统一最大宽度 */
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem; /* 减少标题下方间距（原2.5rem） */
  color: #1a1a1a;
  font-size: 1.8rem; /* 核心：缩小标题字体（原2.2rem） */
  font-weight: 600;
}

.form-group {
  margin-bottom: 1.2rem; /* 减少表单项间距（原2rem） */
}

label {
  display: block;
  margin-bottom: 0.6rem; /* 减少标签下方间距（原1rem） */
  color: #4e5969;
  font-size: 1rem; /* 核心：缩小标签字体（原1.3rem） */
  font-weight: 500;
}

input {
  width: 100%;
  padding: 0.8rem; /* 核心：减少输入框内边距（原1.2rem） */
  border: 1px solid #d0d5dd;
  border-radius: 8px;
  font-size: 1rem; /* 核心：缩小输入框字体（原1.3rem） */
  transition: all 0.3s ease;
  height: 45px; /* 核心：降低输入框高度（原60px） */
}

.error-message {
  color: #ff4d4f;
  margin: 1rem 0; /* 减少错误提示间距（原1.5rem） */
  padding: 0.8rem; /* 减少错误提示内边距（原1rem） */
  border-radius: 6px;
  background-color: #fff2f0;
  text-align: center;
  font-size: 1rem; /* 核心：缩小错误提示字体（原1.2rem） */
}

.auth-btn {
  width: 100%;
  padding: 0.8rem; /* 核心：减少按钮内边距（原1.2rem） */
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem; /* 核心：缩小按钮字体（原1.3rem） */
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-top: 1rem; /* 减少按钮上方间距（原1.5rem） */
  height: 45px; /* 核心：降低按钮高度（原60px） */
}

.auth-switch {
  text-align: center;
  margin-top: 1.2rem; /* 减少切换文字间距（原2rem） */
  color: #667085;
  font-size: 1rem; /* 核心：缩小切换文字字体（原1.2rem） */
}

.auth-switch a {
  color: #42b983;
  text-decoration: none;
  font-weight: 500;
  margin-left: 6px;
  font-size: 1rem; /* 核心：缩小链接字体（原1.2rem） */
}

/* 响应式调整 */
@media (max-width: 768px) {
  .auth-card {
    padding: 1.5rem 1rem; /* 进一步缩小移动端内边距（原2.5rem 1.5rem） */
    width: 95vw; /* 小屏幕占95%宽度 */
  }

  h2 {
    font-size: 1.6rem; /* 缩小移动端标题（原2rem） */
  }

  input, .auth-btn {
    padding: 0.7rem; /* 缩小移动端输入框/按钮内边距（原1rem） */
    font-size: 0.95rem; /* 缩小移动端字体（原1.2rem） */
    height: 40px; /* 降低移动端高度（原55px） */
  }
}
</style>