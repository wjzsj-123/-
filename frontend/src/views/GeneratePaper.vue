<template>
  <div class="paper-generate-container">
    <h2>从题库生成试卷</h2>

    <form @submit.prevent="handleGeneratePaper" class="generate-form">
      <!-- 用户ID（实际场景可能从登录信息获取） -->
      <input type="hidden" v-model="form.userId" />

      <!-- 题库选择 -->
      <div class="form-group">
        <label for="questionSetId">选择题库 <span class="required">*</span></label>
        <select
            id="questionSetId"
            v-model="form.questionSetId"
            required
            @change="handleQuestionSetChange"
        >
          <option value="">-- 请选择题库 --</option>
          <option
              v-for="set in questionSets"
              :key="set.id"
              :value="set.id"
          >
            {{ set.name }}（选择题: {{ set.choiceCount }} 道 | 填空题: {{ set.fillCount }} 道）
          </option>
        </select>
      </div>

      <!-- 试卷名称 -->
      <div class="form-group">
        <label for="paperName">试卷名称 <span class="required">*</span></label>
        <input
            type="text"
            id="paperName"
            v-model="form.paperName"
            required
            placeholder="请输入试卷名称"
        >
      </div>

      <!-- 题目数量设置 -->
      <div class="form-row">
        <div class="form-group">
          <label for="choiceCount">选择题数量 <span class="required">*</span></label>
          <input
              type="number"
              id="choiceCount"
              v-model="form.choiceCount"
              required
              min="0"
              :max="maxChoiceCount"
              @input="validateTotalCount"
          >
          <span class="hint">最多选择 {{ maxChoiceCount }} 道</span>
        </div>

        <div class="form-group">
          <label for="multiCount">多选题数量 <span class="required">*</span></label>
          <input
              type="number"
              id="multiCount"
              v-model="form.multiCount"
              required
              min="0"
              :max="maxMultiCount"
              @input="validateTotalCount"
          >
          <span class="hint">最多选择 {{ maxFillCount }} 道</span>
        </div>

        <div class="form-group">
          <label for="fillCount">填空题数量 <span class="required">*</span></label>
          <input
              type="number"
              id="fillCount"
              v-model="form.fillCount"
              required
              min="0"
              :max="maxFillCount"
              @input="validateTotalCount"
          >
          <span class="hint">最多选择 {{ maxFillCount }} 道</span>
        </div>
      </div>

      <div class="error-message" v-if="errorMsg">{{ errorMsg }}</div>
      <div class="total-count" v-if="form.choiceCount || form.fillCount">
        试卷总题数: {{ form.choiceCount + form.fillCount }} 道
        <br>
        预计总分: {{ form.choiceCount * 5 + form.fillCount * 10 }} 分
      </div>

      <button type="submit" class="generate-btn" :disabled="isSubmitDisabled">
        生成试卷
      </button>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';

// 路由实例
const router = useRouter();

// 表单数据
const form = ref({
  userId: '', // 实际项目中从登录用户信息获取
  questionSetId: '',
  paperName: '',
  choiceCount: 0,
  fillCount: 0,
  multiCount: 0
});

// 状态变量
const questionSets = ref([]);
const errorMsg = ref('');
const maxChoiceCount = ref(0);
const maxFillCount = ref(0);
const maxMultiCount = ref(0);
const loading = ref(false);

// 提交按钮状态
const isSubmitDisabled = computed(() => {
  return loading.value ||
      !form.value.questionSetId ||
      !form.value.paperName.trim() ||
      (form.value.choiceCount <= 0 && form.value.fillCount <= 0 && form.value.multiCount <= 0);
});

// 初始化 - 获取用户ID和题库列表
onMounted(async () => {
  // 1. 获取当前登录用户ID（实际项目中从localStorage或全局状态获取）
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
  form.value.userId = userInfo.id;

  // 2. 获取所有题库列表
  try {
    const response = await fetch(`/api/question-set?userId=${form.value.userId}`);
    const result = await response.json();
    if (result.code === 0) {
      questionSets.value = result.data || [];
    } else {
      errorMsg.value = '获取题库列表失败: ' + result.message;
    }
  } catch (err) {
    errorMsg.value = '网络错误，无法获取题库列表';
    console.error(err);
  }
});

// 题库选择变化时更新最大可选数量
const handleQuestionSetChange = async (e) => {
  const setId = e.target.value;
  if (!setId) {
    maxChoiceCount.value = 0;
    maxFillCount.value = 0;
    maxMultiCount.value = 0;
    form.value.choiceCount = 0;
    form.value.fillCount = 0;
    form.value.multiCount = 0;
    return;
  }

  // 获取选中题库的题目数量信息（假设后端有此接口）
  try {
    const response = await fetch(`/api/question-set/${setId}/count`);
    const result = await response.json();
    if (result.code === 0) {
      console.log(result);
      maxChoiceCount.value = result.data.choiceCount || 0;
      maxFillCount.value = result.data.fillCount || 0;
      maxMultiCount.value = result.data.multiCount || 0;
    }
  } catch (err) {
    console.error('获取题库题目数量失败', err);
  }
};

// 验证总题数
const validateTotalCount = () => {
  errorMsg.value = '';
  if (form.value.choiceCount > maxChoiceCount.value) {
    form.value.choiceCount = maxChoiceCount.value;
  }
  if (form.value.fillCount > maxFillCount.value) {
    form.value.fillCount = maxFillCount.value;
  }
  if (form.value.multiCount > maxMultiCount.value) {
    form.value.multiCount = maxMultiCount.value;
  }
  if (form.value.choiceCount + form.value.fillCount + form.value.multiCount <= 0) {
    errorMsg.value = '题目总数不能小于0';
  }
};

// 提交生成试卷
const handleGeneratePaper = async () => {
  errorMsg.value = '';
  loading.value = true;

  try {
    // 构建查询参数
    const params = new URLSearchParams();
    params.append('userId', form.value.userId);
    params.append('questionSetId', form.value.questionSetId);
    params.append('paperName', form.value.paperName);
    params.append('choiceCount', form.value.choiceCount);
    params.append('fillCount', form.value.fillCount);
    params.append('multiCount', form.value.multiCount);

    // 调用后端生成接口
    const response = await fetch('/api/paper/generate', {
      method: 'POST',
      body: params
    });

    const result = await response.json();
    if (result.code === 0) {
      // 生成成功，跳转到试卷详情页
      alert('试卷生成成功！');
      router.push(`/home/paper/answer/${result.data.id}`);
    } else {
      errorMsg.value = result.message || '生成试卷失败';
    }
  } catch (err) {
    errorMsg.value = '网络错误，生成试卷失败';
    console.error(err);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.paper-generate-container {
  max-width: 800px;
  margin: 2rem auto;
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

h2 {
  text-align: center;
  color: #333;
  margin-bottom: 2rem;
}

.generate-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-row {
  display: flex;
  gap: 1rem;
}

.form-row .form-group {
  flex: 1;
}

label {
  font-weight: 500;
  color: #555;
}

.required {
  color: #ff4d4f;
}

input, select {
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

input:focus, select:focus {
  outline: none;
  border-color: #42b983;
  box-shadow: 0 0 0 2px rgba(66, 185, 131, 0.2);
}

.hint {
  font-size: 0.85rem;
  color: #888;
}

.error-message {
  color: #ff4d4f;
  padding: 0.8rem;
  background: #fff2f0;
  border-radius: 4px;
  text-align: center;
}

.total-count {
  padding: 1rem;
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 4px;
  color: #2a9d54;
  text-align: center;
}

.generate-btn {
  padding: 1rem;
  background: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.3s;
}

.generate-btn:hover:not(:disabled) {
  background: #359e69;
}

.generate-btn:disabled {
  background: #a0d995;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .form-row {
    flex-direction: column;
    gap: 1.5rem;
  }
}
</style>