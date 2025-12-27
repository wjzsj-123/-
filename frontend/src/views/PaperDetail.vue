<template>
  <div class="paper-detail-container">
    <!-- 试卷标题区域 -->
    <div class="paper-header">
      <h2>{{ paper.title }}</h2>
      <p class="paper-meta">
        总题数：{{ totalQuestions }} 题 |
        当前第 {{ currentIndex + 1 }} 题
      </p>
    </div>

    <!-- 题目展示区域 -->
    <div class="question-card" v-if="currentQuestion">
      <div class="question-header">
        <span class="question-type">
          {{ currentQuestion.type === 1 ? '选择题' : '填空题' }}
        </span>
        <h3 class="question-content">{{ currentQuestion.content }}</h3>
      </div>

      <!-- 选择题选项 -->
      <div v-if="currentQuestion.type === 1" class="options-container">
        <div
            class="option-item"
            v-for="option in currentQuestion.options"
            :key="option.id"
            @click="selectOption(option.id)"
            :class="{ 'selected': userAnswers[currentQuestion.id] === option.id }"
        >
          <span class="option-letter">{{ getOptionLetter(option.sortOrder) }}</span>
          <span class="option-content">{{ option.content }}</span>
        </div>
      </div>

      <!-- 填空题答案输入 -->
      <div v-if="currentQuestion.type === 2" class="fill-container">
        <div
            class="fill-item"
            v-for="(_, index) in currentQuestion.fillAnswers.length"
            :key="index"
        >
          <span class="fill-label">第 {{ index + 1 }} 空：</span>
          <input
              type="text"
              v-model="fillAnswers[index]"
              class="fill-input"
              placeholder="请输入答案"
              @input="updateFillAnswer(index, $event.target.value)"
          >
        </div>
      </div>
    </div>

    <!-- 无题目提示 -->
    <div class="empty-state" v-if="!currentQuestion && totalQuestions > 0">
      <p>加载题目失败，请刷新页面重试</p>
    </div>

    <!-- 操作按钮区域 -->
    <div class="action-buttons">
      <button
          class="btn prev-btn"
          @click="goToPrev"
          :disabled="currentIndex === 0"
      >
        上一题
      </button>

      <button
          class="btn save-btn"
          @click="saveAnswers"
      >
        保存并退出
      </button>

      <button
          class="btn submit-btn"
          @click="submitAnswers"
          :disabled="currentIndex !== totalQuestions - 1"
      >
        提交试卷
      </button>

      <button
          class="btn next-btn"
          @click="goToNext"
          :disabled="currentIndex === totalQuestions - 1"
      >
        下一题
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// 路由相关
const route = useRoute();
const router = useRouter();
const paperId = route.params.paperId; // 从路由参数获取试卷ID

// 试卷数据
const paper = ref({});
const questions = ref([]); // 按类型排序后的题目列表（先选择后填空）
const totalQuestions = ref(0);
const currentIndex = ref(0);
const currentQuestion = ref(null);

// 用户答案存储
const userAnswers = ref({}); // 选择题答案 { questionId: optionId }
const fillAnswers = ref([]); // 当前填空题的答案输入
const fillAnswersMap = ref({}); // 填空题答案 { questionId: [answer1, answer2] }

// 初始化页面
onMounted(async () => {
  if (!paperId) {
    alert('试卷ID不存在，请返回列表页选择试卷');
    router.push('/home/paper'); // 跳回列表页
    return;
  }
  await fetchPaperDetail();
  await fetchQuestions();
  if (questions.value.length > 0) {
    loadQuestion(currentIndex.value);
    // 尝试加载已保存的答案
    loadSavedAnswers();
  }
});

// 获取试卷详情
const fetchPaperDetail = async () => {
  try {
    const response = await fetch(`/api/paper/${paperId}`);
    const result = await response.json();
    if (result.code === 0) {
      paper.value = result.data;
    } else {
      alert('获取试卷信息失败：' + result.message);
    }
  } catch (err) {
    console.error('加载试卷详情失败：', err);
    alert('网络错误，无法加载试卷信息');
  }
};

// 获取题目列表（按类型排序）
const fetchQuestions = async () => {
  try {
    const response = await fetch(`/api/paper/${paperId}/questions`);
    const result = await response.json();
    if (result.code === 0) {
      // 后端返回的是包含questions属性的paper对象
      const paperData = result.data;
      // 分离选择题（1）和填空题（2），并按类型排序
      const choices = paperData.questions.filter(q => q.type === 1);
      const fills = paperData.questions.filter(q => q.type === 2);
      questions.value = [...choices, ...fills];
      totalQuestions.value = questions.value.length;
    } else {
      alert('获取题目失败：' + result.message);
    }
  } catch (err) {
    console.error('加载题目列表失败：', err);
    alert('网络错误，无法加载题目');
  }
};

// 加载当前题目
const loadQuestion = (index) => {
  if (index < 0 || index >= questions.value.length) return;

  currentQuestion.value = questions.value[index];

  // 如果是填空题，初始化输入框
  if (currentQuestion.value.type === 2) {
    // 从存储中获取已有答案或初始化
    fillAnswers.value = fillAnswersMap.value[currentQuestion.value.id] ||
        Array(currentQuestion.value.fillAnswers.length).fill('');
  }
};

// 选择题选项选择
const selectOption = (optionId) => {
  userAnswers.value[currentQuestion.value.id] = optionId;
};

// 填空题答案更新
const updateFillAnswer = (index, value) => {
  fillAnswers.value[index] = value;
  // 同步更新到存储对象
  fillAnswersMap.value[currentQuestion.value.id] = [...fillAnswers.value];
};

// 上一题
const goToPrev = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
    loadQuestion(currentIndex.value);
  }
};

// 下一题
const goToNext = () => {
  if (currentIndex.value < totalQuestions.value - 1) {
    currentIndex.value++;
    loadQuestion(currentIndex.value);
  }
};

// 构建答案提交数据
const buildAnswerData = () => {
  // 转换为后端需要的QuestionAnswer格式数组
  const answers = [];

  // 处理选择题答案
  Object.entries(userAnswers.value).forEach(([questionId, optionId]) => {
    answers.push({
      questionId: Number(questionId),
      choiceAnswer: optionId,
      fillAnswers: null
    });
  });

  // 处理填空题答案
  Object.entries(fillAnswersMap.value).forEach(([questionId, answersArr]) => {
    answers.push({
      questionId: Number(questionId),
      choiceAnswer: null,
      fillAnswers: answersArr
    });
  });

  return {
    paperId: Number(paperId),
    answers: answers
  };
};

// 保存答案（临时保存）
const saveAnswers = async () => {
  const answerData = {
    ...buildAnswerData(),
    isTemporary: true
  };

  try {
    const response = await fetch('/api/answer/save', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(answerData)
    });

    const result = await response.json();
    if (result.code === 0) {
      alert('答案已保存');
      router.push('/home/paper'); // 返回试卷列表页
    } else {
      alert('保存失败：' + result.message);
    }
  } catch (err) {
    console.error('保存答案失败：', err);
    alert('网络错误，保存失败');
  }
};

// 提交试卷（最终提交）
const submitAnswers = async () => {
  if (!confirm('确定要提交试卷吗？提交后无法修改答案。')) {
    return;
  }

  const answerData = {
    ...buildAnswerData(),
    isTemporary: false
  };

  try {
    const response = await fetch('/api/answer/submit', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(answerData)
    });

    const result = await response.json();
    if (result.code === 0) {
      alert('提交成功！即将跳转到成绩页面');
      router.push(`/paper/result/${paperId}`); // 跳转到成绩页
    } else {
      alert('提交失败：' + result.message);
    }
  } catch (err) {
    console.error('提交答案失败：', err);
    alert('网络错误，提交失败');
  }
};

// 加载已保存的答案
const loadSavedAnswers = async () => {
  try {
    const response = await fetch(`/api/answer/paper/${paperId}/temp`);
    const result = await response.json();
    if (result.code === 0 && result.data) {
      const saved = result.data;

      // 恢复选择题答案
      saved.answers.forEach(answer => {
        if (answer.choiceAnswer) {
          userAnswers.value[answer.questionId] = answer.choiceAnswer;
        }
      });

      // 恢复填空题答案
      saved.answers.forEach(answer => {
        if (answer.fillAnswers) {
          fillAnswersMap.value[answer.questionId] = answer.fillAnswers;
        }
      });
    }
  } catch (err) {
    console.log('无已保存答案或加载失败：', err);
  }
};

// 选项字母映射（A,B,C,D...）
const getOptionLetter = (sortOrder) => {
  return String.fromCharCode(65 + (sortOrder - 1));
};

// 监听当前题目变化，用于调试
watch(currentQuestion, (newVal) => {
  if (newVal) {
    console.log('当前题目：', newVal.id, newVal.type === 1 ? '选择题' : '填空题');
  }
});
</script>

<style scoped>
.submit-btn {
  background-color: #e74c3c;
  color: white;
}

.submit-btn:hover:not(:disabled) {
  background-color: #c0392b;
}

.submit-btn:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
}

.paper-detail-container {
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
  margin-bottom: 0.5rem;
}

.paper-meta {
  color: #666;
  font-size: 0.95rem;
}

.question-card {
  background-color: white;
  border-radius: 8px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  margin-bottom: 2rem;
  min-height: 300px;
  display: flex;
  flex-direction: column;
}

.question-header {
  margin-bottom: 1.5rem;
}

.question-type {
  display: inline-block;
  padding: 0.3rem 0.6rem;
  background-color: #ebf5fb;
  color: #3498db;
  border-radius: 4px;
  font-size: 0.8rem;
  margin-bottom: 0.8rem;
}

.question-content {
  color: #333;
  font-size: 1.1rem;
  line-height: 1.6;
  margin: 0;
}

/* 选择题选项样式 */
.options-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-top: 1rem;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 1rem;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.option-item:hover {
  border-color: #3498db;
  background-color: #f8fafc;
}

.option-item.selected {
  border-color: #3498db;
  background-color: #ebf5fb;
}

.option-letter {
  display: inline-block;
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  background-color: #3498db;
  color: white;
  border-radius: 50%;
  margin-right: 1rem;
  font-size: 0.9rem;
}

.option-content {
  flex: 1;
  color: #444;
}

/* 填空题样式 */
.fill-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  margin-top: 1rem;
}

.fill-item {
  display: flex;
  align-items: center;
}

.fill-label {
  min-width: 80px;
  color: #666;
  font-size: 0.95rem;
}

.fill-input {
  flex: 1;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.fill-input:focus {
  outline: none;
  border-color: #3498db;
}

/* 按钮区域样式 */
.action-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 2rem;
}

.btn {
  padding: 0.6rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.2s;
}

.prev-btn, .next-btn {
  background-color: #3498db;
  color: white;
}

.prev-btn:disabled, .next-btn:disabled {
  background-color: #bdc3c7;
  cursor: not-allowed;
}

.save-btn {
  background-color: #2ecc71;
  color: white;
}

.save-btn:hover {
  background-color: #27ae60;
}

.prev-btn:hover:not(:disabled), .next-btn:hover:not(:disabled) {
  background-color: #2980b9;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 4rem 0;
  color: #999;
  background-color: white;
  border-radius: 8px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .paper-detail-container {
    padding: 1rem;
  }

  .question-card {
    padding: 1.5rem 1rem;
  }

  .action-buttons {
    flex-direction: column;
    gap: 1rem;
  }

  .btn {
    width: 100%;
  }
}
</style>