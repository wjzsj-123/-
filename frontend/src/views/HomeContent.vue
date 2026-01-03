<template>
  <div class="home-content">
    <h2>欢迎      欢迎使用题库系统
      <span v-if="userInfo.nickname">，{{ userInfo.nickname }}</span>
    </h2>
    <div class="stats-container">
      <div class="stat-card">
        <h3>题库数量</h3>
        <p>{{ questionSetCount }}</p>
      </div>
      <div class="stat-card">
        <h3>试卷数量</h3>
        <p>{{ paperCount }}</p>
      </div>
      <div class="stat-card">
        <h3>题目总数</h3>
        <p>{{ questionCount }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

// 统计数据变量
const questionSetCount = ref(0);
const paperCount = ref(0);
const questionCount = ref(0);
const userInfo = ref({});

const router = useRouter();

// 获取当前登录用户信息
const getCurrentUser = () => {
  const userStr = localStorage.getItem('userInfo');
  if (userStr) {
    return JSON.parse(userStr);
  }
  return null;
};

// 获取题库数量
const fetchQuestionSetCount = async (userId) => {
  try {
    const response = await fetch(`/api/question-set/count?userId=${userId}`);
    const result = await response.json();
    if (result.code === 0) {
      questionSetCount.value = result.data;
    }
  } catch (err) {
    console.error('获取题库数量失败:', err);
  }
};

// 获取试卷数量
const fetchPaperCount = async (userId) => {
  try {
    const response = await fetch(`/api/paper/count?userId=${userId}`);
    const result = await response.json();
    if (result.code === 0) {
      paperCount.value = result.data;
    }
  } catch (err) {
    console.error('获取试卷数量失败:', err);
  }
};

// 获取题目总数
const fetchQuestionCount = async (userId) => {
  try {
    const response = await fetch(`/api/question/count?userId=${userId}`);
    const result = await response.json();
    if (result.code === 0) {
      questionCount.value = result.data;
    }
  } catch (err) {
    console.error('获取题目总数失败:', err);
  }
};

onMounted(async () => {
  // 检查登录状态
  const user = getCurrentUser();
  if (!user) {
    // 未登录则跳转到登录页
    router.push('/login');
    return;
  }
  userInfo.value = user;

  // 并行请求三个统计接口
  await Promise.all([
    fetchQuestionSetCount(user.id),
    fetchPaperCount(user.id),
    fetchQuestionCount(user.id)
  ]);
});
</script>

<style scoped>
.home-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

h2 {
  color: #333;
  margin-bottom: 30px;
  font-size: 1.8rem;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  text-align: center;
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-card h3 {
  color: #666;
  margin-bottom: 15px;
  font-size: 1.1rem;
}

.stat-card p {
  font-size: 2rem;
  font-weight: bold;
  color: #42b983;
  margin: 0;
}
</style>