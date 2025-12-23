<template>
  <div class="question-form">
    <h4>{{ isEdit ? '编辑题目' : '新增题目' }}</h4>

    <form @submit.prevent="handleSubmit">
      <!-- 题目内容 -->
      <div class="form-group">
        <label for="content">题目内容 <span class="required">*</span></label>
        <textarea
            id="content"
            v-model="form.content"
            rows="3"
            placeholder="请输入题目内容"
            required
        ></textarea>
      </div>

      <!-- 题目类型 -->
      <div class="form-group">
        <label for="type">题目类型 <span class="required">*</span></label>
        <select id="type" v-model="form.type" required>
          <option value="">请选择类型</option>
          <option value="single">单选题</option>
          <option value="multiple">多选题</option>
          <option value="fill">填空题</option>
        </select>
      </div>

      <!-- 难度级别 -->
      <div class="form-group">
        <label for="difficulty">难度级别 <span class="required">*</span></label>
        <select id="difficulty" v-model="form.difficulty" required>
          <option value="">请选择难度</option>
          <option value="easy">简单</option>
          <option value="medium">中等</option>
          <option value="hard">困难</option>
        </select>
      </div>

      <!-- 选项区域（根据题型动态显示） -->
      <div v-if="form.type === 'single' || form.type === 'multiple'" class="options-group">
        <label>选项设置</label>
        <div v-for="(option, index) in form.options" :key="index" class="option-item">
          <input
              type="text"
              v-model="option.content"
              placeholder="选项内容"
          >
          <label class="checkbox-label">
            <input
                type="checkbox"
                v-model="option.isCorrect"
                :true-value="1"
                :false-value="0"
            >
            正确答案
          </label>
          <button
              type="button"
              class="remove-btn"
              @click="removeOption(index)"
              :disabled="form.options.length <= 2"
          >
            删除
          </button>
        </div>
        <button type="button" class="add-btn" @click="addOption">+ 添加选项</button>
      </div>

      <!-- 填空题答案区域 -->
      <div v-if="form.type === 'fill'" class="fill-answers-group">
        <label>答案设置</label>
        <div v-for="(answer, index) in form.fillAnswers" :key="index" class="answer-item">
          <input
              type="text"
              v-model="answer.answer"
              placeholder="第{{ index + 1 }}个空的答案"
          >
          <button
              type="button"
              class="remove-btn"
              @click="removeAnswer(index)"
              :disabled="form.fillAnswers.length <= 1"
          >
            删除
          </button>
        </div>
        <button type="button" class="add-btn" @click="addAnswer">+ 添加空</button>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <button type="button" class="cancel-btn" @click="handleCancel">取消</button>
        <button type="submit" class="submit-btn">保存</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, toRefs } from 'vue';
import { defineProps, defineEmits } from 'vue';

// 接收外部参数
const props = defineProps({
  // 编辑时传入的题目数据
  question: {
    type: Object,
    default: () => ({})
  },
  // 是否为编辑模式
  isEdit: {
    type: Boolean,
    default: false
  }
});

// 定义事件
const emit = defineEmits(['save', 'cancel']);

// 表单数据
const form = ref({
  id: props.question.id || null,
  questionSetId: props.question.questionSetId || null, // 关联的题库ID
  content: props.question.content || '',
  type: props.question.type || '',
  difficulty: props.question.difficulty || '',
  options: props.question.options || [{ content: '', isCorrect: 0 }],
  fillAnswers: props.question.fillAnswers || [{ answer: '', sortOrder: 1 }]
});

// 添加选项
const addOption = () => {
  form.value.options.push({
    content: '',
    isCorrect: 0,
    sortOrder: form.value.options.length + 1
  });
};

// 删除选项
const removeOption = (index) => {
  form.value.options.splice(index, 1);
  // 重新排序
  form.value.options.forEach((opt, i) => {
    opt.sortOrder = i + 1;
  });
};

// 添加填空题答案
const addAnswer = () => {
  form.value.fillAnswers.push({
    answer: '',
    sortOrder: form.value.fillAnswers.length + 1
  });
};

// 删除填空题答案
const removeAnswer = (index) => {
  form.value.fillAnswers.splice(index, 1);
  // 重新排序
  form.value.fillAnswers.forEach((ans, i) => {
    ans.sortOrder = i + 1;
  });
};

// 提交表单
const handleSubmit = () => {
  // 简单校验
  if (!form.value.content.trim()) {
    alert('请输入题目内容');
    return;
  }
  if (!form.value.type) {
    alert('请选择题目类型');
    return;
  }
  if (!form.value.difficulty) {
    alert('请选择难度级别');
    return;
  }

  // 提交数据给父组件
  emit('save', { ...form.value });
};

// 取消操作
const handleCancel = () => {
  emit('cancel');
};
</script>

<style scoped>
.question-form {
  padding: 1.5rem;
  border-radius: 8px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #666;
  font-weight: 500;
}

.required {
  color: #ff4444;
}

textarea, select, input {
  width: 100%;
  padding: 0.6rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

textarea {
  resize: vertical;
}

.options-group, .fill-answers-group {
  margin: 1rem 0;
  padding: 1rem;
  border: 1px dashed #ddd;
  border-radius: 4px;
}

.option-item, .answer-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.8rem;
}

.option-item input[type="text"],
.answer-item input[type="text"] {
  flex: 1;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  cursor: pointer;
}

.add-btn, .remove-btn {
  padding: 0.3rem 0.8rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.add-btn {
  background-color: #42b983;
  color: white;
  margin-top: 0.5rem;
}

.remove-btn {
  background-color: #ff4444;
  color: white;
}

.remove-btn:disabled {
  background-color: #ffcccc;
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1.5rem;
}

.submit-btn, .cancel-btn {
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.submit-btn {
  background-color: #42b983;
  color: white;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #333;
}
</style>