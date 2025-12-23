<template>
  <div class="question-form">
    <h3>{{ isEdit ? '编辑题目' : '新增题目' }}</h3>
    <form @submit.prevent="handleSubmit">
      <!-- 题目内容 -->
      <div class="form-group">
        <label>题目内容 <span class="required">*</span></label>
        <textarea
            v-model="form.content"
            rows="3"
            placeholder="请输入题目内容"
            required
        ></textarea>
      </div>

      <!-- 题目类型 -->
      <div class="form-group">
        <label>题目类型 <span class="required">*</span></label>
        <select v-model="form.type" @change="handleTypeChange" required>
          <option value="">请选择类型</option>
          <option value="1">选择题</option>
          <option value="2">填空题</option>
        </select>
      </div>

      <!-- 难度等级 -->
      <div class="form-group">
        <label>难度等级 <span class="required">*</span></label>
        <select v-model="form.difficulty" required>
          <option value="">请选择难度</option>
          <option value="1">简单</option>
          <option value="2">中等</option>
          <option value="3">困难</option>
        </select>
      </div>

      <!-- 选择题选项（动态生成） -->
      <div v-if="form.type === 1" class="options-group">
        <label>选项（至少2个，勾选正确答案）</label>
        <div v-for="(option, index) in form.options" :key="index" class="option-item">
          <input
              type="text"
              v-model="option.content"
              placeholder="选项内容"
              required
          >
          <label class="correct-checkbox">
            <input
                type="checkbox"
                v-model="option.isCorrect"
                true-value="1"
                false-value="0"
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

      <!-- 填空题答案（动态生成） -->
      <div v-if="form.type === 2" class="fill-answers-group">
        <label>答案（按空顺序填写）</label>
        <div v-for="(answer, index) in form.fillAnswers" :key="index" class="answer-item">
          <input
              type="text"
              v-model="answer.answer"
              placeholder="第{{ index + 1 }}个空的答案"
              required
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

      <div class="form-actions">
        <button type="submit" class="submit-btn">保存</button>
        <button type="button" class="cancel-btn" @click="handleCancel">取消</button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, watch } from 'vue';

// 接收父组件传入的题库ID和编辑时的题目数据
const props = defineProps({
  questionSetId: {
    type: Number,
    required: true
  },
  editData: {
    type: Object,
    default: null
  }
});

// 向父组件传递事件（保存/取消）
const emit = defineEmits(['save', 'cancel']);

// 表单数据
const form = ref({
  id: null,
  questionSetId: props.questionSetId, // 关联的题库ID
  content: '',
  type: '', // 1:选择题, 2:填空题
  difficulty: '',
  options: [], // 选择题选项
  fillAnswers: [] // 填空题答案
});

// 判断是否为编辑模式
const isEdit = ref(!!props.editData);

// 初始化表单数据
watch(() => props.editData, (newVal) => {
  if (newVal) {
    isEdit.value = true;
    form.value = { ...newVal };
    // 确保选项和答案数组存在
    form.value.options = form.value.options || [];
    form.value.fillAnswers = form.value.fillAnswers || [];
  }
}, { immediate: true });

// 切换题目类型时重置选项/答案
const handleTypeChange = () => {
  if (form.value.type === 1) {
    form.value.options = [{ content: '', isCorrect: 0 }];
    form.value.fillAnswers = [];
    addOption(); // 初始2个选项
  } else if (form.value.type === 2) {
    form.value.fillAnswers = [{ answer: '', sortOrder: 1 }];
    form.value.options = [];
  }
};

// 选择题相关方法
const addOption = () => {
  form.value.options.push({
    content: '',
    isCorrect: 0,
    sortOrder: form.value.options.length + 1
  });
};

const removeOption = (index) => {
  form.value.options.splice(index, 1);
  // 重新排序
  form.value.options.forEach((opt, i) => opt.sortOrder = i + 1);
};

// 填空题相关方法
const addAnswer = () => {
  form.value.fillAnswers.push({
    answer: '',
    sortOrder: form.value.fillAnswers.length + 1
  });
};

const removeAnswer = (index) => {
  form.value.fillAnswers.splice(index, 1);
  // 重新排序
  form.value.fillAnswers.forEach((ans, i) => ans.sortOrder = i + 1);
};

// 提交表单
const handleSubmit = async () => {
  // 前端校验
  if (!form.value.content || !form.value.type || !form.value.difficulty) {
    alert('请完善题目基本信息');
    return;
  }
  if (form.value.type === 1 && form.value.options.length < 2) {
    alert('选择题至少需要2个选项');
    return;
  }
  if (form.value.type === 1 && !form.value.options.some(opt => opt.isCorrect === 1)) {
    alert('请选择正确答案');
    return;
  }
  if (form.value.type === 2 && form.value.fillAnswers.length < 1) {
    alert('填空题至少需要1个空');
    return;
  }

  // 提交到后端
  try {
    const response = await fetch('/api/question', {
      method: isEdit.value ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(form.value)
    });
    const result = await response.json();
    if (result.code === 0) {
      emit('save', result.data); // 通知父组件保存成功
    } else {
      alert(result.message || '操作失败');
    }
  } catch (err) {
    alert('网络错误，请重试');
    console.error(err);
  }
};

// 取消操作
const handleCancel = () => {
  emit('cancel');
};
</script>

<style scoped>
.form-group {
  margin-bottom: 1rem;
}

.required {
  color: #ff4444;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
}

input, select, textarea {
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
  border: 1px dashed #ccc;
  border-radius: 4px;
}

.option-item, .answer-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 0.5rem;
}

.option-item input[type="text"], .answer-item input {
  flex: 1;
}

.correct-checkbox {
  display: flex;
  align-items: center;
  gap: 5px;
  white-space: nowrap;
}

.add-btn, .remove-btn {
  padding: 0.3rem 0.8rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.add-btn {
  background-color: #42b983;
  color: white;
}

.remove-btn {
  background-color: #ff4444;
  color: white;
}

.remove-btn:disabled {
  background-color: #ffaaaa;
  cursor: not-allowed;
}

.form-actions {
  margin-top: 2rem;
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.submit-btn {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 4px;
  cursor: pointer;
}

.cancel-btn {
  background-color: #666;
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 4px;
  cursor: pointer;
}
</style>