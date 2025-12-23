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

    <!-- 主体内容区：侧边栏 + 主内容 -->
    <div class="main-container">
      <!-- 侧边导航 -->
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
/* 基础布局优化 */
.home-container {
  display: flex;
  flex-direction: column;
  height: 100vh; /* 占满整个视口高度 */
  overflow: hidden; /* 防止整体滚动 */
}

/* 顶部导航栏 */
.main-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2rem;
  height: 60px;
  background-color: #42b983;
  color: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  z-index: 10; /* 确保导航栏在最上层 */
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

/* 主体内容容器 - 关键优化点 */
.main-container {
  display: flex; /* 横向布局 */
  flex: 1; /* 占据剩余全部高度 */
  overflow: hidden; /* 防止容器自身滚动 */
}

/* 侧边栏 */
.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
  width: 200px;
  background-color: #f5f5f5;
  border-right: 1px solid #ddd;
  height: 100%; /* 占满父容器高度 */
  overflow-y: auto; /* 侧边栏内容过多时可滚动 */
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

/* 主内容区 - 关键优化点 */
.main-content {
  flex: 1; /* 占据剩余全部宽度 */
  padding: 2rem;
  overflow-y: auto; /* 内容过多时可滚动 */
  height: 100%; /* 确保高度充满容器 */
  box-sizing: border-box; /* 防止内边距导致溢出 */
}

/* 响应式调整 */
@media (max-width: 768px) {
  .main-container {
    flex-direction: column; /* 小屏幕纵向布局 */
  }

  .nav-list {
    width: 100%;
    height: auto;
    max-height: 200px; /* 限制侧边栏高度 */
    border-right: none;
    border-bottom: 1px solid #ddd;
  }

  .main-content {
    padding: 1rem;
    height: calc(100% - 200px); /* 减去侧边栏高度 */
  }
}
</style>