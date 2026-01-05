<template>
  <div class="home-content">
    <h2>欢迎使用题库系统
      <span v-if="userInfo.nickname">，{{ userInfo.nickname }}</span>
    </h2>

    <!-- 统计数据容器 -->
    <div class="stats-wrapper">
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

    <!-- 每日一题区域 -->
    <div class="daily-question-section">
      <div class="daily-question-card">
        <h3 class="daily-title">📖 每日一题</h3>
        <div class="question-placeholder">
          <p class="question-tip">今日学习任务尚未发布，敬请期待...</p>
          <div class="question-footer">
            <button class="practice-btn">开始练习</button>
          </div>
        </div>
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

/* 统计数据外层容器 - 限制宽度 */
.stats-wrapper {
  max-width: 800px;
  margin: 0 auto 40px;
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

/* 统计卡片容器 - 保持网格布局但在有限宽度内 */
.stats-container {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.stat-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
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

/* 每日一题区域样式 */
.daily-question-section {
  max-width: 800px;
  margin: 0 auto;
}

.daily-question-card {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.daily-title {
  color: #333;
  font-size: 1.4rem;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.question-placeholder {
  text-align: center;
  padding: 40px 20px;
}

.question-tip {
  color: #666;
  font-size: 1.1rem;
  margin-bottom: 30px;
}

.question-footer {
  margin-top: 20px;
}

.practice-btn {
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 10px 30px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.practice-btn:hover {
  background-color: #36a371;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .stats-container {
    grid-template-columns: 1fr;
  }

  .stats-wrapper, .daily-question-card {
    padding: 15px;
  }

  .daily-title {
    font-size: 1.2rem;
  }

  .question-placeholder {
    padding: 20px 10px;
  }
}
</style>