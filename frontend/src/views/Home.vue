<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <nav class="main-nav">
      <div class="logo">自主学习平台</div>
      <div class="user-info">
        <router-link
          v-if="userInfo.id"
          class="user-name-link"
          :to="'/home/user/center/' + userInfo.id"
        >
          {{ userInfo.nickname || userInfo.username }}
        </router-link>
        <span v-else class="user-name-plain">{{ userInfo.nickname || userInfo.username }}</span>
        <div
          v-if="userInfo.id"
          class="mail-wrap"
          @mouseenter="onMailEnter"
          @mouseleave="onMailLeave"
        >
          <router-link to="/home/messages" class="mail-link" @click="showMailPreview = false">
            <span class="mail-icon" aria-hidden="true">✉</span>
            <span v-if="badgeText" class="mail-badge">{{ badgeText }}</span>
          </router-link>
          <div
            v-show="showMailPreview"
            class="mail-dropdown"
            @mouseenter="onMailEnter"
            @mouseleave="onMailLeave"
          >
            <div v-if="!previewItems.length" class="drop-empty">暂无未读消息</div>
            <div
              v-for="m in previewItems"
              :key="m.id"
              class="drop-item"
              :class="{ unread: !m.readAt }"
              @mouseenter="markPreviewRead(m)"
            >
              <div class="drop-title">{{ m.title }}</div>
              <div class="drop-preview">{{ m.contentPreview }}</div>
            </div>
            <router-link to="/home/messages" class="drop-more" @click="showMailPreview = false">进入消息中心 →</router-link>
          </div>
        </div>
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
          <router-link to="/home/online-bank">在线题库</router-link>
        </li>
        <li>
          <router-link to="/home/online-paper">在线试卷</router-link>
        </li>
        <li>
          <router-link to="/home/study-plan">学习计划</router-link>
        </li>
        <li>
          <router-link v-if="userInfo.id" :to="'/home/user/center/' + userInfo.id">用户中心</router-link>
        </li>
        <li>
          <router-link to="/home/messages">消息中心</router-link>
        </li>
        <li>
          <router-link to="/home/user/profile">个人信息</router-link>
        </li>
      </ul>

      <!-- 主内容区：固定最大宽度，避免切换路由时横向位置跳动 -->
      <div class="main-content">
        <div class="app-page-shell">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watchEffect, computed, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const userInfo = ref({});

const unreadCount = ref(0);
const showMailPreview = ref(false);
const previewItems = ref([]);
let mailHideTimer = null;

const badgeText = computed(() => {
  const n = unreadCount.value;
  if (!n) return '';
  return n > 99 ? '99+' : String(n);
});

const fetchUnreadCount = async () => {
  const id = userInfo.value?.id;
  if (!id) return;
  try {
    const r = await fetch(`/api/messages/unread-count?userId=${id}`);
    const j = await r.json();
    if (j.code === 0) {
      unreadCount.value = Number(j.data) || 0;
    }
  } catch {
    /* ignore */
  }
};

const fetchPreview = async () => {
  const id = userInfo.value?.id;
  if (!id) return;
  try {
    const r = await fetch(`/api/messages/preview?userId=${id}`);
    const j = await r.json();
    previewItems.value = j.code === 0 ? (j.data || []) : [];
  } catch {
    previewItems.value = [];
  }
};

const onMailEnter = () => {
  if (mailHideTimer) {
    clearTimeout(mailHideTimer);
    mailHideTimer = null;
  }
  showMailPreview.value = true;
  fetchUnreadCount();
  fetchPreview();
};

const onMailLeave = () => {
  mailHideTimer = setTimeout(() => {
    showMailPreview.value = false;
  }, 220);
};

const markPreviewRead = async (m) => {
  if (m.readAt) return;
  const id = userInfo.value?.id;
  if (!id) return;
  try {
    const res = await fetch(`/api/messages/${m.id}/read?userId=${id}`, { method: 'POST' });
    const j = await res.json();
    if (j.code === 0) {
      m.readAt = new Date().toISOString();
      await fetchUnreadCount();
    }
  } catch {
    /* ignore */
  }
};

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

// 使用watchEffect监控路由变化和localStorage变化
watchEffect(() => {
  const storedUser = localStorage.getItem('userInfo');
  if (storedUser) {
    userInfo.value = JSON.parse(storedUser);
    fetchUnreadCount();
  } else if (route.path !== '/login' && route.path !== '/register') {
    // 未登录且不在登录/注册页时跳转
    router.push('/login');
  }
});

watch(
  () => route.path,
  () => {
    if (userInfo.value?.id) {
      fetchUnreadCount();
    }
  }
);

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

.user-name-link {
  color: #fff;
  text-decoration: none;
  font-weight: 600;
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
  padding-bottom: 2px;
}

.user-name-link:hover {
  border-bottom-color: #fff;
}

.user-name-plain {
  font-weight: 500;
}

.mail-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.mail-link {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  color: #fff;
  text-decoration: none;
  border-radius: 8px;
  transition: background 0.2s;
}

.mail-link:hover {
  background: rgba(255, 255, 255, 0.15);
}

.mail-icon {
  font-size: 22px;
  line-height: 1;
}

.mail-badge {
  position: absolute;
  right: 0;
  bottom: 2px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  font-size: 11px;
  font-weight: 700;
  line-height: 18px;
  text-align: center;
  color: #fff;
  background: #f56c6c;
  border-radius: 9px;
  border: 2px solid #42b983;
}

.mail-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 6px;
  width: 320px;
  max-height: 360px;
  overflow-y: auto;
  background: #fff;
  color: #303133;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.18);
  z-index: 1000;
  padding: 8px 0;
}

.drop-empty {
  padding: 16px;
  text-align: center;
  color: #909399;
  font-size: 13px;
}

.drop-item {
  padding: 10px 14px;
  border-bottom: 1px solid #f0f0f0;
  cursor: default;
}

.drop-item.unread {
  background: #fff7e6;
}

.drop-item:last-of-type {
  border-bottom: none;
}

.drop-title {
  font-weight: 600;
  font-size: 13px;
  margin-bottom: 4px;
  color: #303133;
}

.drop-preview {
  font-size: 12px;
  color: #606266;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.drop-more {
  display: block;
  padding: 10px 14px;
  text-align: center;
  font-size: 13px;
  color: #409eff;
  text-decoration: none;
  border-top: 1px solid #eee;
}

.drop-more:hover {
  background: #f5f9ff;
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

/* 主内容区：仅负责滚动与背景，内边距交给 app-page-shell */
.main-content {
  flex: 1;
  padding: 0;
  overflow-y: auto;
  overflow-x: hidden;
  height: 100%;
  box-sizing: border-box;
  background: #eef1f5;
}

.app-page-shell {
  width: 100%;
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px 24px 32px;
  box-sizing: border-box;
  min-height: 100%;
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
    height: calc(100% - 200px);
  }

  .app-page-shell {
    padding: 16px 14px 24px;
  }
}
</style>