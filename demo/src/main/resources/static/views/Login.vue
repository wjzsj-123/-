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
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

// 组件中获取路由实例（如需编程式导航）
const router = VueRouter.useRouter();
const form = ref({
  username: '',
  password: ''
});
const errorMsg = ref('');

const handleLogin = async () => {
  try {
    errorMsg.value = '';
    const response = await axios.post('/api/user/login', form.value);

    if (response.data.code === 0) {
      // 登录成功，存储用户信息
      localStorage.setItem('userInfo', JSON.stringify(response.data.data));
      await router.push('/'); // 跳转到首页
    } else {
      errorMsg.value = response.data.message || '登录失败';
    }
  } catch (err) {
    errorMsg.value = '网络错误，请稍后重试';
    console.error('登录失败:', err);
  }
};
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #666;
}

input {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

input:focus {
  outline: none;
  border-color: #42b983;
}

.error-message {
  color: #ff4444;
  margin: 1rem 0;
  text-align: center;
}

.auth-btn {
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

.auth-btn:hover {
  background-color: #359e75;
}

.auth-switch {
  text-align: center;
  margin-top: 1rem;
  color: #666;
}

.auth-switch a {
  color: #42b983;
  text-decoration: none;
}

.auth-switch a:hover {
  text-decoration: underline;
}
</style>