<template>
  <div class="profile-container">
    <h3>个人信息页面</h3>
    <div class="profile-content">
      <form class="profile-form">
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
        <button type="button" class="save-btn" @click="saveProfile">保存修改</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 个人信息数据
const profile = ref({
  username: '',
  nickname: '',
  email: ''
})

// 页面加载时获取本地存储的用户信息
onMounted(() => {
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  profile.value = {
    username: userInfo.username || '',
    nickname: userInfo.nickname || '',
    email: userInfo.email || ''
  }
  console.log('个人信息组件已加载');
})

// 保存个人信息
const saveProfile = () => {
  const oldUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const newUserInfo = {
    ...oldUserInfo,
    nickname: profile.value.nickname,
    email: profile.value.email
  }
  localStorage.setItem('userInfo', JSON.stringify(newUserInfo))
  alert('个人信息保存成功')
}
</script>

<style scoped>
.profile-container {
  width: 100%;
  height: 100%;
  padding: 2rem;
  background-color: #f8f9fa;
}

.profile-content {
  margin-top: 1rem;
  padding: 2rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  max-width: 500px;
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

.save-btn:hover {
  background-color: #359e75;
}

h3 {
  color: #333;
  margin-bottom: 0.5rem;
}
</style>