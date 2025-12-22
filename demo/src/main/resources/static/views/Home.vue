<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <nav class="main-nav">
      <div class="logo">题库系统</div>
      <div class="user-info">
        <span>{{ userInfo.nickname || userInfo.username }}</span>
        <button @click="handleLogout" class="logout-btn">退出登录</button>
      </div>
    </nav>

    <!-- static/views/Home.vue 侧边导航部分 -->
    <ul class="nav-list">
      <li>
        <router-link to="/home">首页</router-link>
      </li>
      <li>
        <router-link to="/home/question-set">题库管理</router-link>
      </li>
      <li>
        <router-link to="/home/paper">试卷管理</router-link>
      </li>
      <li>
        <router-link to="/home/user/profile">个人信息</router-link>
      </li>
    </ul>

    <!-- 主内容区 -->
    <div class="main-content">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const userInfo = ref({});

// 页面加载时获取用户信息
onMounted(() => {
  const storedUser = localStorage.getItem('userInfo');
  if (storedUser) {
    userInfo.value = JSON.parse(storedUser);
  } else {
    // 如果未登录，跳转回登录页
    router.push('/login');
  }
});

// 退出登录
const handleLogout = () => {
  localStorage.removeItem('userInfo');
  router.push('/login');
};
</script>

<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background-color: #42b983;
  color: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.logo {
  font-size: 1.5rem;
  font-weight: bold;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logout-btn {
  background-color: rgba(255,255,255,0.2);
  border: none;
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background-color: rgba(255,255,255,0.3);
}

.sidebar {
  width: 200px;
  background-color: #f5f5f5;
  border-right: 1px solid #ddd;
  height: calc(100vh - 60px);
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-list li {
  border-bottom: 1px solid #ddd;
}

.nav-list a {
  display: block;
  padding: 12px 20px;
  color: #333;
  text-decoration: none;
  transition: background-color 0.3s;
}

.nav-list a:hover,
.nav-list a.router-link-exact-active {
  background-color: #e0f2f1;
  color: #42b983;
  font-weight: bold;
}

.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  height: calc(100vh - 60px);
}
</style>