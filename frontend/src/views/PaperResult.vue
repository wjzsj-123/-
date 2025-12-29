<template>
  <div class="paper-result-container">
    <!-- 试卷基础信息 -->
    <div class="paper-header" v-if="paper && paper.title">
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

      <div v-else-if="!paper || questions.length === 0" class="empty-state">
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
          v-if="paper"
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

        <!-- 填空题答案展示（优化后） -->
        <div v-if="question.type === 2" class="fill-answers">
          <div class="answer-title">答案详情：</div>
          <div class="fill-detail-container">
            <!-- 每个空单独显示 -->
            <div
                class="fill-item"
                v-for="(correctAns, idx) in question.correctAnswer"
                :key="idx"
                :class="{
                'fill-correct': question.userAnswer[idx] && correctAns === question.userAnswer[idx],
                'fill-incorrect': question.userAnswer[idx] && correctAns !== question.userAnswer[idx],
                'fill-empty': !question.userAnswer[idx]
              }"
            >
              <div class="fill-label">第 {{ idx + 1 }} 空：</div>
              <div class="fill-answers-wrap">
                <div class="fill-correct-answer">
                  <span class="label">正确答案：</span>
                  <span class="content">{{ correctAns }}</span>
                </div>
                <div class="fill-user-answer">
                  <span class="label">你的答案：</span>
                  <span class="content">{{ question.userAnswer[idx] || '未作答' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 返回按钮 -->
    <div class="action-buttons">
      <button class="back-btn" @click="goBack">返回试卷列表</button>
    </div>

    <!-- 错误状态显示 -->
    <div v-if="error" class="error-state">
      <p>{{ error }}</p>
      <button class="retry-btn" @click="reloadData">重试</button>
    </div>
  </div>
</template>

<script setup>
// 脚本部分保持不变，无需修改
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// 路由相关
const route = useRoute();
const router = useRouter();
const paperId = route.params.paperId; // 从路由参数获取试卷ID

// 数据存储 - 初始化为空对象避免null问题
const paper = ref({}); // 试卷基础信息
const questions = ref([]); // 题目及答案列表
const loading = ref(true);
const totalScore = ref(0); // 总分
const userScore = ref(0); // 用户得分

// 添加错误状态管理
const error = ref('');

// 计算正确率
const accuracy = computed(() => {
  if (totalScore.value === 0) return 0;
  return ((userScore.value / totalScore.value) * 100).toFixed(1);
});

// 重新加载数据方法
const reloadData = async () => {
  loading.value = true;
  error.value = '';
  try {
    await Promise.all([fetchPaperInfo(), fetchAnswerResult()]);
    calculateScores();
  } catch (err) {
    error.value = err.message || '加载失败，请稍后重试';
  } finally {
    loading.value = false;
  }
};

// 页面初始化
onMounted(async () => {
  try {
    await Promise.all([fetchPaperInfo(), fetchAnswerResult()]);
    calculateScores();
  } catch (err) {
    error.value = err.message || '网络错误，无法加载答题结果';
  } finally {
    loading.value = false;
  }
});

// 获取试卷基础信息
const fetchPaperInfo = async () => {
  try {
    const response = await fetch(`/api/paper/${paperId}`);
    const result = await response.json();
    if (result.code === 0) {
      paper.value = result.data || {};
    } else {
      throw new Error(result.message || '获取试卷信息失败');
    }
  } catch (error) {
    console.error('获取试卷信息出错:', error);
    paper.value = {};
    throw error;
  }
};

// 获取答题结果（包含题目、正确答案、用户答案）
const fetchAnswerResult = async () => {
  try {
    // 从本地存储获取当前登录用户ID
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    const userId = userInfo.id;

    if (!userId) {
      throw new Error('未获取到用户信息，请重新登录');
    }

    const response = await fetch(`/api/paper/${paperId}/result?userId=${userId}`);
    const result = await response.json();

    console.log("原始返回数据:", result.data);

    if (!response.ok) {
      throw new Error(`HTTP错误: ${response.status}`);
    }

    if (result.code === 0) {
      const { paper, userAnswers } = result.data;
      questions.value = formatQuestionResult(paper.questions, userAnswers);
    } else {
      throw new Error(result.message || '获取答题结果失败');
    }
  } catch (error) {
    console.error('获取答题结果出错:', error);
    throw error;
  }
};

// 格式化后端返回数据为前端所需结构
const formatQuestionResult = (questions, userAnswers) => {
  const answerMap = questions.reduce((map, question) => {
    if (question.type === 2) {
      // 填空题：收集当前题目的所有空的答案，按sortOrder排序
      const questionAnswers = userAnswers
          .filter(ans => ans.questionId === question.id)
          .sort((a, b) => a.sortOrder - b.sortOrder)
          .map(ans => ans.fillContent || ''); // 使用fillContent字段
      map[question.id] = questionAnswers;
    } else {
      // 选择题：直接映射选项ID
      const questionAnswer = userAnswers.find(ans => ans.questionId === question.id);
      map[question.id] = questionAnswer ? questionAnswer.choiceOptionId : null;
    }
    return map;
  }, {});

  return questions.map(question => {
    let isCorrect = false;
    let userAnswerData = answerMap[question.id] || [];
    let correctAnswerData = [];

    if (question.type === 1) {
      // 选择题逻辑
      const correctOption = question.options.find(opt => opt.isCorrect === 1);
      const correctOptionId = correctOption ? correctOption.id : null;
      isCorrect = userAnswerData === correctOptionId;
      correctAnswerData = correctOption ? [correctOption.content] : [];
    } else if (question.type === 2) {
      // 填空题逻辑
      correctAnswerData = question.fillAnswers
          ? question.fillAnswers
              .filter(fill => fill.questionId === question.id)
              .sort((a, b) => a.sortOrder - b.sortOrder)
              .map(fill => fill.answer)
              .filter(Boolean)
          : [];

      userAnswerData = answerMap[question.id] || [];

      // 判分逻辑（与后端保持一致）
      isCorrect = correctAnswerData.length === userAnswerData.length &&
          correctAnswerData.every((correct, index) =>
              correct === userAnswerData[index]
          );
    }

    return {
      id: question.id,
      content: question.content,
      type: question.type,
      score: question.score || 5,
      isCorrect,
      options: question.options || [],
      correctAnswer: correctAnswerData,
      userAnswer: userAnswerData
    };
  });
};

// 计算总分和用户得分
const calculateScores = () => {
  totalScore.value = questions.value.reduce((sum, q) => sum + (q.score || 0), 0);
  userScore.value = questions.value.reduce((sum, q) => {
    return sum + (q.isCorrect ? (q.score || 0) : 0);
  }, 0);
};

// 选项字母映射（A,B,C,D...）
const getOptionLetter = (sortOrder) => {
  if (!sortOrder) return '';
  return String.fromCharCode(65 + (sortOrder - 1));
};

// 返回试卷列表
const goBack = () => {
  router.push('/home/paper');
};
</script>

<style scoped>
/* 添加错误状态样式 */
.error-state {
  text-align: center;
  padding: 4rem 0;
  color: #e74c3c;
  background-color: white;
  border-radius: 8px;
}

.retry-btn {
  margin-top: 1rem;
  padding: 0.5rem 1.2rem;
  border: none;
  border-radius: 4px;
  background-color: #3498db;
  color: white;
  cursor: pointer;
}

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

/* 填空题优化样式 */
.fill-answers {
  margin-top: 1rem;
}

.fill-detail-container {
  border: 1px solid #e9ecef;
  border-radius: 6px;
  overflow: hidden;
}

.fill-item {
  padding: 1rem;
  border-bottom: 1px solid #f1f1f1;
}

.fill-item:last-child {
  border-bottom: none;
}

.fill-item.fill-correct {
  background-color: rgba(46, 204, 113, 0.05);
  border-left: 3px solid #2ecc71;
}

.fill-item.fill-incorrect {
  background-color: rgba(231, 76, 60, 0.05);
  border-left: 3px solid #e74c3c;
}

.fill-item.fill-empty {
  background-color: rgba(149, 165, 166, 0.05);
  border-left: 3px solid #95a5a6;
}

.fill-label {
  font-weight: bold;
  color: #444;
  margin-bottom: 0.5rem;
  display: inline-block;
}

.fill-answers-wrap {
  margin-left: 1.5rem;
}

.fill-correct-answer, .fill-user-answer {
  margin-bottom: 0.3rem;
  font-size: 0.95rem;
}

.fill-correct-answer .label, .fill-user-answer .label {
  display: inline-block;
  width: 80px;
  color: #666;
}

.fill-correct-answer .content {
  color: #2ecc71;
  font-weight: 500;
}

.fill-user-answer .content {
  color: #3498db;
}

.fill-item.fill-empty .fill-user-answer .content {
  color: #95a5a6;
  font-style: italic;
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
  .fill-answers-wrap {
    margin-left: 0;
  }

  .fill-correct-answer .label, .fill-user-answer .label {
    width: 70px;
    font-size: 0.9rem;
  }
}
</style>