<template>
  <div class="paper-container">
    <div class="paper-header">
      <h3>我的试卷</h3>
      <button class="generate-btn" @click="goToGeneratePaper">生成试卷</button>
    </div>
    <div class="paper-content">
      <!-- 试卷列表区域 -->
      <div class="paper-list">
        <!-- 空状态提示 -->
        <div v-if="papers.length === 0" class="empty-state">
          <p>暂无试卷，点击"生成试卷"创建新试卷</p>
        </div>

        <!-- 试卷卡片 -->
        <div class="paper-card" v-for="paper in papers" :key="paper.id">
          <div class="paper-info">
            <h4>{{ paper.title }}</h4>
            <p>总题数：{{ paper.totalQuestions }} 题</p>
            <p>创建时间：{{ formatTime(paper.createTime) }}</p>
            <p v-if="paper.lastAnswerTime" class="answered-mark">
              <span>已作答</span> | 最后作答：{{ formatTime(paper.lastAnswerTime) }}
            </p>
          </div>
          <div class="paper-actions">
            <!-- 根据作答状态显示不同按钮 -->
            <button
                class="action-btn start-btn"
                @click="handleStartAnswer(paper.id)"
                v-if="!paper.isAnswered"
            >
              开始答题
            </button>
            <button
                class="action-btn view-btn"
                @click="handleViewAnswer(paper.id)"
                v-else
            >
              查看答案
            </button>
            <!-- 删除按钮 -->
            <button
                class="action-btn delete-btn"
                @click="handleDelete(paper.id)"
            >
              删除
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';

// 路由实例
const router = useRouter();
// 试卷列表数据
const papers = ref([]);

// 获取当前登录用户信息
const getUserInfo = () => {
  const userStr = localStorage.getItem('userInfo');
  return userStr ? JSON.parse(userStr) : null;
};

// 格式化时间显示
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 获取用户试卷列表
const fetchUserPapers = async () => {
  const user = getUserInfo();
  if (!user || !user.id) {
    // 未登录则跳转到登录页
    router.push('/login');
    return;
  }

  try {
    const response = await fetch(`/api/paper/user/${user.id}`);
    const result = await response.json();

    if (result.code === 0) {
      // 假设后端返回的数据包含 isAnswered（是否作答）和 lastAnswerTime（最后作答时间）
      papers.value = result.data || [];
    } else {
      alert('获取试卷失败：' + result.message);
    }
  } catch (err) {
    console.error('获取试卷列表失败：', err);
    alert('网络错误，无法加载试卷列表');
  }
};

// 跳转到生成试卷页面
const goToGeneratePaper = () => {
  router.push('/home/paper/generate'); // 保持原路由跳转逻辑
};

// 开始答题（修改为扁平路由路径）
const handleStartAnswer = (paperId) => {
  router.push(`/home/paper/answer/${paperId}`);
};

// 查看答案（同步修改，可选）
const handleViewAnswer = (paperId) => {
  router.push(`/home/paper/result/${paperId}`);
};

// 删除试卷
const handleDelete = async (paperId) => {
  if (!confirm('确定要删除该试卷吗？此操作不可恢复')) {
    return;
  }

  try {
    const response = await fetch(`/api/paper/${paperId}`, {
      method: 'DELETE'
    });
    const result = await response.json();

    if (result.code === 0) {
      alert('删除成功');
      // 重新加载试卷列表
      fetchUserPapers();
    } else {
      alert('删除失败：' + result.message);
    }
  } catch (err) {
    console.error('删除试卷失败：', err);
    alert('网络错误，删除失败');
  }
};

// 页面加载时获取试卷列表
onMounted(() => {
  fetchUserPapers();
});

console.log('试卷管理组件已加载');
</script>

<style scoped>
.paper-container {
  width: 100%;
  min-height: 100vh; /* 确保占满视口高度 */
  padding: 2rem;
  background-color: #f8f9fa;
}

.paper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.generate-btn {
  padding: 0.6rem 1.2rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
}

.generate-btn:hover {
  background-color: #359e69;
}

.paper-content {
  padding: 1.5rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

h3 {
  color: #333;
  margin: 0;
  font-size: 1.4rem;
}

/* 试卷列表样式 */
.paper-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 1.5rem;
  margin-top: 1rem;
}

.paper-card {
  border: 1px solid #e9ecef;
  border-radius: 6px;
  padding: 1.2rem;
  transition: box-shadow 0.3s;
}

.paper-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
}

.paper-info h4 {
  margin: 0 0 0.8rem 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.paper-info p {
  margin: 0.4rem 0;
  color: #666;
  font-size: 0.9rem;
  line-height: 1.5;
}

.answered-mark {
  color: #3498db;
  font-weight: 500;
}

.answered-mark span {
  display: inline-block;
  padding: 0.1rem 0.4rem;
  background-color: #ebf5fb;
  border-radius: 3px;
  font-size: 0.8rem;
}

/* 按钮区域样式 */
.paper-actions {
  display: flex;
  gap: 0.8rem;
  margin-top: 1rem;
  justify-content: flex-end;
}

.action-btn {
  padding: 0.4rem 0.8rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: opacity 0.3s;
}

.action-btn:hover {
  opacity: 0.9;
}

.start-btn {
  background-color: #42b983;
  color: white;
}

.view-btn {
  background-color: #3498db;
  color: white;
}

.delete-btn {
  background-color: #e74c3c;
  color: white;
}

/* 空状态样式 */
.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 4rem 0;
  color: #999;
  border: 1px dashed #ddd;
  border-radius: 8px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .paper-container {
    padding: 1rem;
  }

  .paper-list {
    grid-template-columns: 1fr;
  }
}
</style>