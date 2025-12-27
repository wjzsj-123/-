<template>
  <div class="paper-result-container">
    <!-- 试卷基础信息 -->
    <div class="paper-header">
      <h2>{{ paper.title }} - 答题结果</h2>
      <div class="score-info">
        <p>总分：<span class="total-score">{{ totalScore }}</span> 分</p>
        <p>你的得分：<span class="user-score">{{ userScore }}</span> 分</p>
        <p>正确率：<span class="accuracy">{{ accuracy }}%</span></p>
      </div>
    </div>

    <!-- 题目列表 -->
    <div class="questions-container">
      <div v-if="loading" class="loading-state">
        <p>正在加载答题结果...</p>
      </div>

      <div v-else-if="questions.length === 0" class="empty-state">
        <p>暂无答题记录</p>
      </div>

      <!-- 单个题目展示 -->
      <div
          class="question-card"
          v-for="(question, index) in questions"
          :key="question.id"
          :class="{
          'correct': question.isCorrect,
          'incorrect': !question.isCorrect && question.userAnswer
        }"
      >
        <div class="question-header">
          <span class="question-index">第 {{ index + 1 }} 题</span>
          <span class="question-type">
            {{ question.type === 1 ? '选择题' : '填空题' }}
            ({{ question.score }} 分)
          </span>
          <span class="answer-status" v-if="question.isCorrect">
            ✔ 回答正确
          </span>
          <span class="answer-status error" v-else-if="question.userAnswer">
            ✘ 回答错误
          </span>
          <span class="answer-status empty" v-else>
            — 未作答
          </span>
        </div>

        <div class="question-content">
          {{ question.content }}
        </div>

        <!-- 选择题答案展示 -->
        <div v-if="question.type === 1" class="choice-answers">
          <div class="answer-title">选项：</div>
          <div
              class="option-item"
              v-for="option in question.options"
              :key="option.id"
              :class="{
              'correct-option': option.isCorrect,
              'user-option': option.id === question.userAnswer
            }"
          >
            <span class="option-letter">{{ getOptionLetter(option.sortOrder) }}</span>
            <span class="option-content">{{ option.content }}</span>
            <span class="option-tag" v-if="option.isCorrect">正确答案</span>
            <span class="option-tag user-tag" v-if="option.id === question.userAnswer">你的答案</span>
          </div>
        </div>

        <!-- 填空题答案展示 -->
        <div v-if="question.type === 2" class="fill-answers">
          <div class="answer-title">正确答案：</div>
          <div class="correct-fill">
            <span v-for="(ans, idx) in question.correctAnswer" :key="idx">
              第 {{ idx + 1 }} 空：{{ ans }}
              <span v-if="idx < question.correctAnswer.length - 1"> | </span>
            </span>
          </div>

          <div class="answer-title mt-2">你的答案：</div>
          <div class="user-fill" v-if="question.userAnswer && question.userAnswer.length">
            <span v-for="(ans, idx) in question.userAnswer" :key="idx">
              第 {{ idx + 1 }} 空：{{ ans || '未作答' }}
              <span v-if="idx < question.userAnswer.length - 1"> | </span>
            </span>
          </div>
          <div class="user-fill empty" v-else>
            未作答
          </div>
        </div>
      </div>
    </div>

    <!-- 返回按钮 -->
    <div class="action-buttons">
      <button class="back-btn" @click="goBack">返回试卷列表</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// 路由相关
const route = useRoute();
const router = useRouter();
const paperId = route.params.paperId; // 从路由参数获取试卷ID

// 数据存储
const paper = ref({}); // 试卷基础信息
const questions = ref([]); // 题目及答案列表
const loading = ref(true);
const totalScore = ref(0); // 总分
const userScore = ref(0); // 用户得分

// 计算正确率
const accuracy = computed(() => {
  if (totalScore.value === 0) return 0;
  return ((userScore.value / totalScore.value) * 100).toFixed(1);
});

// 页面初始化
onMounted(async () => {
  try {
    await Promise.all([
      fetchPaperInfo(),
      fetchAnswerResult()
    ]);
    calculateScores(); // 计算总分和用户得分
  } catch (err) {
    console.error('加载答题结果失败：', err);
    alert('网络错误，无法加载答题结果');
  } finally {
    loading.value = false;
  }
});

// 获取试卷基础信息
const fetchPaperInfo = async () => {
  const response = await fetch(`/api/paper/${paperId}`);
  const result = await response.json();
  if (result.code === 0) {
    paper.value = result.data;
  } else {
    throw new Error(result.message || '获取试卷信息失败');
  }
};

// 获取答题结果（包含题目、正确答案、用户答案）
const fetchAnswerResult = async () => {
  const response = await fetch(`/api/answer/result/${paperId}`);
  const result = await response.json();
  if (result.code === 0) {
    questions.value = result.data || [];
  } else {
    throw new Error(result.message || '获取答题结果失败');
  }
};

// 计算总分和用户得分
const calculateScores = () => {
  // 计算总分
  totalScore.value = questions.value.reduce((sum, q) => sum + (q.score || 0), 0);

  // 计算用户得分
  userScore.value = questions.value.reduce((sum, q) => {
    return sum + (q.isCorrect ? (q.score || 0) : 0);
  }, 0);
};

// 选项字母映射（A,B,C,D...）
const getOptionLetter = (sortOrder) => {
  return String.fromCharCode(65 + (sortOrder - 1));
};

// 返回试卷列表
const goBack = () => {
  router.push('/home/paper');
};
</script>

<style scoped>
.paper-result-container {
  width: 100%;
  max-width: 1000px;
  margin: 0 auto;
  padding: 2rem;
  background-color: #f8f9fa;
  min-height: 100vh;
}

.paper-header {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e9ecef;
}

.paper-header h2 {
  color: #2c3e50;
  margin-bottom: 1rem;
}

.score-info {
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
}

.score-info p {
  margin: 0;
  font-size: 1.1rem;
  color: #333;
}

.total-score {
  color: #3498db;
  font-weight: bold;
}

.user-score {
  color: #2ecc71;
  font-weight: bold;
}

.accuracy {
  color: #e67e22;
  font-weight: bold;
}

.questions-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 4rem 0;
  color: #999;
  background-color: white;
  border-radius: 8px;
}

.question-card {
  background-color: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  border-left: 4px solid #ddd;
}

.question-card.correct {
  border-left-color: #2ecc71;
  background-color: rgba(46, 204, 113, 0.03);
}

.question-card.incorrect {
  border-left-color: #e74c3c;
  background-color: rgba(231, 76, 60, 0.03);
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 1rem;
  gap: 0.5rem;
}

.question-index {
  font-weight: bold;
  color: #2c3e50;
}

.question-type {
  color: #666;
  font-size: 0.9rem;
}

.answer-status {
  padding: 0.2rem 0.6rem;
  border-radius: 4px;
  font-size: 0.85rem;
  color: white;
  background-color: #2ecc71;
}

.answer-status.error {
  background-color: #e74c3c;
}

.answer-status.empty {
  background-color: #95a5a6;
}

.question-content {
  font-size: 1.05rem;
  color: #333;
  line-height: 1.6;
  margin-bottom: 1.5rem;
}

.choice-answers, .fill-answers {
  margin-top: 1rem;
}

.answer-title {
  font-weight: bold;
  color: #444;
  margin-bottom: 0.8rem;
  font-size: 0.95rem;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 0.8rem 1rem;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  margin-bottom: 0.8rem;
  transition: all 0.2s;
}

.option-item.correct-option {
  border-color: #2ecc71;
  background-color: rgba(46, 204, 113, 0.1);
}

.option-item.user-option {
  border-color: #3498db;
  background-color: rgba(52, 152, 219, 0.1);
}

.option-letter {
  display: inline-block;
  width: 22px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  background-color: #ddd;
  color: white;
  border-radius: 50%;
  margin-right: 0.8rem;
  font-size: 0.85rem;
}

.option-item.correct-option .option-letter {
  background-color: #2ecc71;
}

.option-item.user-option .option-letter {
  background-color: #3498db;
}

.option-content {
  flex: 1;
  color: #444;
}

.option-tag {
  padding: 0.1rem 0.4rem;
  background-color: #2ecc71;
  color: white;
  border-radius: 3px;
  font-size: 0.75rem;
  margin-left: 0.5rem;
}

.option-tag.user-tag {
  background-color: #3498db;
}

.fill-answers {
  line-height: 1.8;
}

.correct-fill {
  color: #2ecc71;
  font-weight: 500;
  margin-bottom: 1rem;
}

.user-fill {
  color: #3498db;
}

.user-fill.empty {
  color: #95a5a6;
  font-style: italic;
}

.mt-2 {
  margin-top: 0.5rem;
}

.action-buttons {
  margin-top: 2rem;
  text-align: center;
}

.back-btn {
  padding: 0.6rem 1.5rem;
  border: none;
  border-radius: 4px;
  background-color: #3498db;
  color: white;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.back-btn:hover {
  background-color: #2980b9;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .paper-result-container {
    padding: 1rem;
  }

  .score-info {
    gap: 1rem;
  }

  .question-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>