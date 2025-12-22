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
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';

const router = VueRouter.useRouter();
const form = ref({
  username: '',
  password: '',
  nickname: '',
  email: ''
});
const errorMsg = ref('');

const handleRegister = async () => {
  try {
    const response = await axios.post('/api/user/register', form.value); // 对应后端路径
    if (response.data.code === 0) {
      await router.push('/login'); // 注册成功跳登录
    } else {
      errorMsg.value = response.data.message;
    }
  } catch (err) {
    errorMsg.value = '注册失败，请重试';
  }
};
</script>

<style scoped>
/* 样式与Login.vue共享，可提取到公共CSS中 */
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