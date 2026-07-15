<template>
  <div class="home-content">
    <h2>欢迎使用题库系统
      <span v-if="displayUserName(userInfo)">，{{ displayUserName(userInfo) }}</span>
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

    <div class="study-plan-entry">
      <div class="entry-card">
        <h3>学习计划</h3>
        <p>制定目标题库和每日刷题数，按计划高效学习</p>
        <button class="practice-btn" @click="goStudyPlan">进入学习计划</button>
      </div>
    </div>

    <!-- 每日一题区域 -->
    <div class="daily-question-section">
      <div class="daily-question-card">
        <h3 class="daily-title">📖 每日一题</h3>

        <!-- 题库选择区域 -->
        <div class="question-set-selector" v-if="questionSets.length > 0">
          <label>选择学习题库：</label>
          <select v-model="selectedQuestionSetId" class="set-select">
            <option value="">-- 请选择题库 --</option>
            <option v-for="set in questionSets" :key="set.id" :value="set.id">
              {{ set.name }}
            </option>
          </select>
        </div>

        <!-- 题目展示/占位区域 -->
        <div class="question-placeholder" v-if="!currentQuestion">
          <p class="question-tip" v-if="questionSets.length === 0">
            暂无可用题库，请先创建题库后再使用每日一题功能
          </p>
          <p class="question-tip" v-else-if="!selectedQuestionSetId">
            请先选择一个学习题库，点击下方按钮抽取今日练习题目
          </p>
          <p class="question-tip" v-else>
            点击下方按钮从【{{ getSelectedSetName() }}】题库中随机抽取一题
          </p>
          <div class="question-footer">
            <button
                class="practice-btn"
                @click="generateDailyQuestion"
                :disabled="!selectedQuestionSetId || questionSets.length === 0"
            >
              抽取题目
            </button>
          </div>
        </div>

        <!-- 已抽取到题目展示（答题模式） -->
        <div class="question-content" v-if="currentQuestion">
          <div class="question-header">
            <span class="question-type">[{{ getQuestionTypeName(currentQuestion.type) }}]</span>
            <span class="question-set-name">来自：{{ getSelectedSetName() }}</span>
          </div>
          <div class="question-body">
            <p class="question-text">{{ currentQuestion.content }}</p>

            <!-- 单选题选项（支持选择） -->
            <div class="option-list" v-if="currentQuestion.type === 1 && currentQuestion.options.length > 0">
              <div
                  class="option-item"
                  v-for="(option, index) in currentQuestion.options"
                  :key="option.id || index"
                  :class="{ selected: selectedOptionId === option.id, correct: showAnswer && option.isCorrect === 1, wrong: showAnswer && selectedOptionId === option.id && option.isCorrect === 0 }"
                  @click="selectOption(option.id)"
              >
                <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
                <span class="option-content">{{ option.content }}</span>
                <!-- 判分标识 -->
                <span class="option-tag" v-if="showAnswer && option.isCorrect === 1">✓ 正确</span>
                <span class="option-tag wrong-tag" v-if="showAnswer && selectedOptionId === option.id && option.isCorrect === 0">✗ 错误</span>
              </div>
            </div>

            <!-- 多选题选项（支持多选） -->
            <div class="option-list" v-if="currentQuestion.type === 3 && currentQuestion.options.length > 0">
              <div
                  class="option-item"
                  v-for="(option, index) in currentQuestion.options"
                  :key="option.id || index"
                  :class="{
                    selected: selectedMultiOptionIds.includes(option.id),
                    correct: showAnswer && option.isCorrect === 1,
                    wrong: showAnswer && selectedMultiOptionIds.includes(option.id) && option.isCorrect === 0
                  }"
                  @click="toggleMultiOption(option.id)"
              >
                <span class="option-label">{{ String.fromCharCode(65 + index) }}.</span>
                <span class="option-content">{{ option.content }}</span>
                <!-- 判分标识 -->
                <span class="option-tag" v-if="showAnswer && option.isCorrect === 1">✓ 正确</span>
                <span class="option-tag wrong-tag" v-if="showAnswer && selectedMultiOptionIds.includes(option.id) && option.isCorrect === 0">✗ 错误</span>
              </div>
            </div>

            <!-- 填空题（输入答案） -->
            <div class="fill-blank-area" v-if="currentQuestion.type === 2">
              <p class="fill-tip">请在下方输入答案（第{{ currentQuestion.sortOrder || 1 }}空）：</p>
              <input
                  type="text"
                  v-model="userAnswer"
                  class="fill-input"
                  placeholder="请输入答案"
                  :disabled="showAnswer"
              >
            </div>
          </div>

          <!-- 答题操作区 -->
          <div class="question-footer" v-if="!showAnswer">
            <button class="practice-btn" @click="submitAnswer" :disabled="!isAnswerValid">提交答案</button>
            <button class="reset-btn" @click="resetDailyQuestion" style="margin-left: 10px;">重新抽取</button>
          </div>

          <!-- 答题结果区（提交后展示） -->
          <div class="question-footer" v-if="showAnswer">
            <div class="score-result" :class="{ pass: isAnswerCorrect, fail: !isAnswerCorrect }">
              {{ isAnswerCorrect ? '回答正确！' : '回答错误！' }}
            </div>
            <button class="reset-btn" @click="resetDailyQuestion" style="margin-left: 10px;">重新抽取</button>
          </div>

          <!-- 答案解析区域 -->
          <div class="answer-analysis" v-if="showAnswer">
            <h4>参考答案：</h4>
            <!-- 单选题解析 -->
            <div v-if="currentQuestion.type === 1">
              <p class="correct-answer">
                正确选项：
                <span v-for="option in currentQuestion.options.filter(opt => opt.isCorrect === 1)" :key="option.id">
                  {{ String.fromCharCode(65 + currentQuestion.options.findIndex(o => o.id === option.id)) }}、
                </span>
              </p>
            </div>
            <!-- 多选题解析 -->
            <div v-if="currentQuestion.type === 3">
              <p class="correct-answer">
                正确选项：
                <span v-for="option in currentQuestion.options.filter(opt => opt.isCorrect === 1)" :key="option.id">
                  {{ String.fromCharCode(65 + currentQuestion.options.findIndex(o => o.id === option.id)) }}、
                </span>
              </p>
              <p class="user-answer" v-if="selectedMultiOptionIds.length > 0">
                你的选择：
                <span v-for="option in currentQuestion.options.filter(opt => selectedMultiOptionIds.includes(opt.id))" :key="option.id">
                  {{ String.fromCharCode(65 + currentQuestion.options.findIndex(o => o.id === option.id)) }}、
                </span>
              </p>
            </div>
            <!-- 填空题解析 -->
            <div v-if="currentQuestion.type === 2">
              <p class="correct-answer">正确答案：{{ currentQuestion.correctAnswer || '暂无答案' }}</p>
              <p class="user-answer" v-if="userAnswer">你的答案：{{ userAnswer }}</p>
            </div>
            <p class="analysis-text" v-if="currentQuestion.analysis">解析：{{ currentQuestion.analysis }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { displayUserName } from '@/utils/userDisplay';

// 统计数据变量
const questionSetCount = ref(0);
const paperCount = ref(0);
const questionCount = ref(0);
const userInfo = ref({});

// 每日一题相关变量
const questionSets = ref([]); // 所有可用题库
const selectedQuestionSetId = ref(''); // 选中的题库ID
const currentQuestion = ref(null); // 当前抽取的题目
const loading = ref(false); // 加载状态

// 答题核心变量
const showAnswer = ref(false); // 是否显示答案/解析
const selectedOptionId = ref(null); // 单选题选中的选项ID
const selectedMultiOptionIds = ref([]); // 多选题选中的选项ID列表
const userAnswer = ref(''); // 填空题用户输入答案
const isAnswerCorrect = ref(false); // 答题是否正确

const router = useRouter();

const goStudyPlan = () => {
  router.push('/home/study-plan');
};

// 校验答案是否有效（用于禁用提交按钮）
const isAnswerValid = computed(() => {
  if (!currentQuestion.value) return false;
  // 单选题：必须选中选项
  if (currentQuestion.value.type === 1) {
    return selectedOptionId.value !== null;
  }
  // 多选题：至少选中一个选项
  if (currentQuestion.value.type === 3) {
    return selectedMultiOptionIds.value.length > 0;
  }
  // 填空题：输入框不能为空
  if (currentQuestion.value.type === 2) {
    return userAnswer.value.trim() !== '';
  }
  return false;
});

// 获取当前登录用户信息
const getCurrentUser = () => {
  const userStr = localStorage.getItem('userInfo');
  if (userStr) {
    return JSON.parse(userStr);
  }
  return null;
};

// 获取题库列表
const fetchQuestionSets = async (userId) => {
  try {
    const response = await fetch(`/api/question-set/user/${userId}`);
    const result = await response.json();
    if (result.code === 0) {
      questionSets.value = result.data;
    }
  } catch (err) {
    console.error('获取题库列表失败:', err);
  }
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

const generateDailyQuestion = async () => {
  if (!selectedQuestionSetId.value) {
    alert('请先选择一个学习题库！');
    return;
  }

  loading.value = true;
  showAnswer.value = false;
  userAnswer.value = '';
  selectedOptionId.value = null;
  selectedMultiOptionIds.value = [];
  isAnswerCorrect.value = false;

  try {
    const userId = userInfo.value.id;
    // 请求随机题目接口
    const response = await fetch(`/api/question/random?questionSetId=${selectedQuestionSetId.value}&limit=1`);
    const result = await response.json();
    // console.log(result);

    if (result.code === 0 && result.data && result.data.length > 0) {
      // 从数组中提取第一个元素
      const question = result.data[0];

      // 如果是单选题/多选题，获取选项列表
      if (question.type === 1 || question.type === 3) {
        const optionRes = await fetch(`/api/question-option/question/${question.id}`);
        const optionResult = await optionRes.json();
        if (optionResult.code === 0) {
          question.options = optionResult.data;
        }
      }

      // 如果是填空题，获取正确答案
      if (question.type === 2) {
        const answerRes = await fetch(`/api/fill-answer/question/${question.id}/sort/1`);
        const answerResult = await answerRes.json();
        if (answerResult.code === 0) {
          // 兼容：如果答案也是数组，同样取第一个
          question.correctAnswer = Array.isArray(answerResult.data)
              ? answerResult.data[0]?.answer
              : answerResult.data?.answer;
        }
      }

      currentQuestion.value = question;
      // console.log(currentQuestion)
    } else {
      alert('该题库暂无可用题目！');
      currentQuestion.value = null;
    }
  } catch (err) {
    console.error('抽取题目失败:', err);
    alert('抽取题目失败，请稍后重试！');
  } finally {
    loading.value = false;
  }
};

// 单选题：选择选项
const selectOption = (optionId) => {
  if (showAnswer.value) return; // 已判分后不可修改
  selectedOptionId.value = optionId;
};

// 多选题：切换选项选中状态
const toggleMultiOption = (optionId) => {
  if (showAnswer.value) return; // 已判分后不可修改
  if (selectedMultiOptionIds.value.includes(optionId)) {
    // 取消选中
    selectedMultiOptionIds.value = selectedMultiOptionIds.value.filter(id => id !== optionId);
  } else {
    // 选中选项
    selectedMultiOptionIds.value.push(optionId);
  }
};

// 提交答案并判分
const submitAnswer = () => {
  showAnswer.value = true;
  // 判分逻辑
  if (currentQuestion.value.type === 1) {
    // 单选题判分
    const correctOption = currentQuestion.value.options.find(opt => opt.isCorrect === 1);
    isAnswerCorrect.value = selectedOptionId.value === correctOption?.id;
  } else if (currentQuestion.value.type === 3) {
    // 多选题判分（全对才正确）
    const correctOptionIds = currentQuestion.value.options
        .filter(opt => opt.isCorrect === 1)
        .map(opt => opt.id);
    // 排序后比较（避免顺序问题）
    const selectedSorted = [...selectedMultiOptionIds.value].sort();
    const correctSorted = [...correctOptionIds].sort();
    isAnswerCorrect.value = JSON.stringify(selectedSorted) === JSON.stringify(correctSorted);
  } else if (currentQuestion.value.type === 2) {
    // 填空题判分（忽略大小写/空格）
    isAnswerCorrect.value = userAnswer.value.trim().toLowerCase() === currentQuestion.value.correctAnswer?.trim().toLowerCase();
  }
};

// 重置每日一题（清空当前题目/答题状态）
const resetDailyQuestion = () => {
  currentQuestion.value = null;
  showAnswer.value = false;
  userAnswer.value = '';
  selectedOptionId.value = null;
  selectedMultiOptionIds.value = [];
  isAnswerCorrect.value = false;
};

// 获取选中题库的名称
const getSelectedSetName = () => {
  const selectedSet = questionSets.value.find(set => set.id === selectedQuestionSetId.value);
  return selectedSet?.name || '未知题库';
};

// 获取题目类型名称
const getQuestionTypeName = (type) => {
  switch (type) {
    case 1: return '单选题';
    case 2: return '填空题';
    case 3: return '多选题';
    case 4: return '简答题';
    default: return '未知题型';
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

  // 并行请求数据
  await Promise.all([
    fetchQuestionSetCount(user.id),
    fetchPaperCount(user.id),
    fetchQuestionCount(user.id),
    fetchQuestionSets(user.id)
  ]);
});
</script>

<style scoped>
.home-content {
  width: 100%;
}

h2 {
  color: #333;
  margin-bottom: 30px;
  font-size: 1.8rem;
}

/* 统计数据外层容器 */
.stats-wrapper {
  width: 100%;
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

/* 每日一题区域 */
.daily-question-section {
  width: 100%;
  margin: 0 auto;
}

.study-plan-entry {
  width: 100%;
  margin: 0 auto 24px;
}

.entry-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  text-align: center;
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

/* 题库选择器样式 */
.question-set-selector {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
}

.question-set-selector label {
  color: #666;
  margin-right: 10px;
  font-weight: 500;
}

.set-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  min-width: 200px;
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

/* 题目内容样式 */
.question-content {
  padding: 20px 0;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #eee;
}

.question-type {
  background: #42b983;
  color: white;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 0.9rem;
}

.question-set-name {
  color: #999;
  font-size: 0.9rem;
}

.question-body {
  margin-bottom: 20px;
}

.question-text {
  font-size: 1.2rem;
  line-height: 1.6;
  margin-bottom: 20px;
  color: #333;
}

/* 选择题选项样式（支持点击选择） */
.option-list {
  margin-left: 20px;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  line-height: 1.5;
  padding: 10px 15px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
  border: 1px solid #eee;
}

.option-item:hover {
  background-color: #f8f9fa;
}

.option-item.selected {
  background-color: #e8f4ea;
  border-color: #42b983;
}

.option-item.correct {
  background-color: #e8f4ea;
  border-color: #42b983;
}

.option-item.wrong {
  background-color: #fff2f0;
  border-color: #ff4d4f;
}

.option-label {
  font-weight: bold;
  color: #42b983;
  margin-right: 8px;
  min-width: 20px;
}

.option-content {
  color: #666;
  flex: 1;
}

.option-tag {
  color: #42b983;
  font-size: 0.8rem;
  margin-left: 10px;
  font-weight: bold;
}

.wrong-tag {
  color: #ff4d4f;
}

/* 填空题样式 */
.fill-blank-area {
  margin: 20px 0;
}

.fill-tip {
  color: #666;
  margin-bottom: 10px;
}

.fill-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.fill-input:disabled {
  background-color: #f8f9fa;
  cursor: not-allowed;
}

/* 答题结果样式 */
.score-result {
  font-size: 1.1rem;
  font-weight: bold;
  margin-right: 15px;
}

.score-result.pass {
  color: #42b983;
}

.score-result.fail {
  color: #ff4d4f;
}

/* 答案解析样式 */
.answer-analysis {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
}

.answer-analysis h4 {
  color: #333;
  margin-bottom: 10px;
  font-size: 1.1rem;
}

.correct-answer {
  color: #42b983;
  font-weight: bold;
  margin-bottom: 10px;
}

.user-answer {
  color: #666;
  margin-bottom: 10px;
}

.analysis-text {
  color: #666;
  line-height: 1.6;
}

/* 按钮样式 */
.question-footer {
  margin-top: 20px;
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
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

.practice-btn:disabled {
  background-color: #999;
  cursor: not-allowed;
}

.practice-btn:hover:not(:disabled) {
  background-color: #36a371;
}

.reset-btn {
  background-color: #666;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 10px 20px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s;
}

.reset-btn:hover {
  background-color: #444;
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

  .question-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .question-set-name {
    margin-top: 5px;
  }

  .question-footer {
    flex-direction: column;
    gap: 10px;
  }

  .score-result {
    margin-bottom: 10px;
  }
}
</style>