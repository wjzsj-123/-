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
          <option value="option">单选题</option>
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

      <!-- 选项区域（添加滚动容器） -->
      <div v-if="form.type === 'option' || form.type === 'multiple'" class="options-container">
        <div class="options-header">
          <label>选项设置</label>
          <span class="option-count-tip">
            当前{{ form.options.length }}/4个选项
          </span>
        </div>
        <!-- 滚动区域 -->
        <div class="options-scroll-wrapper">
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
                  :disabled="form.type === 'option' && hasCorrectOption && option.isCorrect === 0"
              >
              正确答案
              <template v-if="form.type === 'option'">
                <span class="single-hint">（单选题只能选择一个正确答案）</span>
              </template>
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
        </div>
        <button
            type="button"
            class="add-btn"
            @click="addOption"
            :disabled="form.options.length >= 4"
        >
          + 添加选项
          <span v-if="form.options.length >= 4" class="max-tip">（最多4个选项）</span>
        </button>
      </div>

      <!-- 填空题答案区域（添加滚动容器） -->
      <div v-if="form.type === 'fill'" class="fill-answers-container">
        <div class="fill-answers-header">
          <label>答案设置</label>
        </div>
        <!-- 滚动区域 -->
        <div class="fill-answers-scroll-wrapper">
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
import {computed, ref, watch} from 'vue';
import { defineProps, defineEmits } from 'vue';

// 计算属性：自动判断是否为编辑模式
const isEdit = computed(() => !!form.value.id);

// 接收外部参数
const props = defineProps({
  editData: {
    type: Object,
    default: () => ({})
  },
  questionSetId: {
    type: [String, Number],
    required: true
  }
});

// 定义事件
const emit = defineEmits(['save', 'cancel']);

// 表单数据初始化
const form = ref({
  id: props.editData ? props.editData.id : null,
  questionSetId: props.questionSetId,
  content: props.editData ? props.editData.content : '',
  // 类型转换：后端1→单选题(option)，2→填空题(fill)，3→多选题(multiple)
  type: props.editData
      ? (props.editData.type === 1 ? 'option'
          : props.editData.type === 2 ? 'fill'
              : props.editData.type === 3 ? 'multiple' : '')
      : '',
  difficulty: props.editData
      ? (props.editData.difficulty === 1 ? 'easy'
          : props.editData.difficulty === 2 ? 'medium'
              : props.editData.difficulty === 3 ? 'hard' : '')
      : '',
  // 选择题/多选题选项（初始化最多2个，限制最大4个）
  options: props.editData && props.editData.options
      ? [...props.editData.options.slice(0, 4)]  // 编辑时最多保留4个选项
      : [
        { content: '', isCorrect: 0, sortOrder: 1 },
        { content: '', isCorrect: 0, sortOrder: 2 }
      ],
  // 填空题答案
  fillAnswers: props.editData && props.editData.fillAnswers ? [...props.editData.fillAnswers] : [
    { answer: '', sortOrder: 1 }
  ]
});

// 单选题已选择正确答案的标记（用于控制单选逻辑）
const hasCorrectOption = computed(() => {
  return form.value.options.some(opt => opt.isCorrect === 1);
});

// 监听题目类型变化，重置正确答案但保留答案项
watch(() => form.value.type, (newType, oldType) => {
  if (!newType || newType === oldType) return;

  // 当切换到选择题类型（单选/多选）时，重置选项的正确答案标记
  if (newType === 'option' || newType === 'multiple') {
    form.value.options.forEach(option => {
      option.isCorrect = 0;
    });
    // 切换类型时如果选项数超过4个，自动截断到4个
    if (form.value.options.length > 4) {
      form.value.options = form.value.options.slice(0, 4);
    }
  }
  // 当切换到填空题时，不重置答案内容（保留填空文本）
  // 如需清空可取消注释以下代码
  // else if (newType === 'fill') {
  //   form.value.fillAnswers.forEach(answer => {
  //     answer.answer = '';
  //   });
  // }
}, { immediate: false });

// 监听单选题选项变化，确保只有一个正确答案
watch(() => form.value.options, (newOptions) => {
  if (form.value.type === 'option') {
    const correctCount = newOptions.filter(opt => opt.isCorrect === 1).length;
    if (correctCount > 1) {
      // 保留最后一个选中的正确答案，其他置为错误
      let lastCorrectIndex = -1;
      newOptions.forEach((opt, index) => {
        if (opt.isCorrect === 1) lastCorrectIndex = index;
      });
      newOptions.forEach((opt, index) => {
        if (index !== lastCorrectIndex) opt.isCorrect = 0;
      });
    }
  }
}, { deep: true });

// 添加选项（限制最多4个）
const addOption = () => {
  if (form.value.options.length >= 4) return;

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
  // 1. 基础校验
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

  // 2. 校验选择题/多选题选项
  if (form.value.type === 'option' || form.value.type === 'multiple') {
    if (form.value.options.length < 2) {
      alert(`${form.value.type === 'option' ? '单选' : '多选'}题至少需要2个选项`);
      return;
    }
    const hasCorrect = form.value.options.some(opt => opt.isCorrect === 1);
    if (!hasCorrect) {
      alert(`${form.value.type === 'option' ? '单选' : '多选'}题必须设置正确答案`);
      return;
    }
    // 单选题额外校验：只能有一个正确答案
    if (form.value.type === 'option') {
      const correctCount = form.value.options.filter(opt => opt.isCorrect === 1).length;
      if (correctCount !== 1) {
        alert('单选题只能设置一个正确答案');
        return;
      }
    }
    // 多选题额外校验：至少有两个正确答案
    if (form.value.type === 'multiple') {
      const correctCount = form.value.options.filter(opt => opt.isCorrect === 1).length;
      if (correctCount < 2) {
        alert('多选题至少需要设置两个正确答案');
        return;
      }
    }
    // 校验选项内容不能为空
    const hasEmptyOption = form.value.options.some(opt => !opt.content.trim());
    if (hasEmptyOption) {
      alert('选项内容不能为空');
      return;
    }
  }

  // 3. 校验填空题答案
  if (form.value.type === 'fill') {
    if (form.value.fillAnswers.length < 1) {
      alert('填空题至少需要1个空');
      return;
    }
    const hasEmptyAnswer = form.value.fillAnswers.some(ans => !ans.answer.trim());
    if (hasEmptyAnswer) {
      alert('填空题答案不能为空');
      return;
    }
  }

  // 4. 转换 type 为后端需要的整数（1:单选, 3:多选, 2:填空）
  let typeInt;
  switch (form.value.type) {
    case 'option':
      typeInt = 1;
      break;
    case 'multiple':
      typeInt = 3;  // 新增多选题类型标识
      break;
    case 'fill':
      typeInt = 2;
      break;
    default:
      alert('题目类型错误');
      return;
  }

  // 5. 转换 difficulty 为后端需要的整数
  let difficultyInt;
  switch (form.value.difficulty) {
    case 'easy':
      difficultyInt = 1;
      break;
    case 'medium':
      difficultyInt = 2;
      break;
    case 'hard':
      difficultyInt = 3;
      break;
    default:
      alert('难度级别错误');
      return;
  }

  // 6. 构造提交数据
  const submitData = {
    id: form.value.id,
    questionSetId: form.value.questionSetId,
    content: form.value.content,
    type: typeInt,
    difficulty: difficultyInt,
    options: form.value.options,
    fillAnswers: form.value.fillAnswers
  };

  // 7. 触发保存事件
  emit('save', submitData);
};

// 取消操作
const handleCancel = () => {
  emit('cancel');
};
</script>

<style scoped>
.question-form {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

h4 {
  color: #333;
  margin-bottom: 20px;
  font-size: 1.4rem;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  margin-bottom: 8px;
  color: #666;
  font-weight: 500;
}

.required {
  color: #ff4d4f;
}

textarea, select, input[type="text"] {
  width: 100%;
  padding: 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

textarea {
  resize: vertical;
}

/* 选项容器样式 - 新增滚动功能 */
.options-container, .fill-answers-container {
  margin-bottom: 20px;
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  padding: 15px;
}

.options-header, .fill-answers-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.option-count-tip {
  font-size: 12px;
  color: #999;
}

/* 选项滚动区域 */
.options-scroll-wrapper, .fill-answers-scroll-wrapper {
  max-height: 200px;
  overflow-y: auto;
  margin-bottom: 15px;
  padding-right: 5px;
}

/* 滚动条样式优化 */
.options-scroll-wrapper::-webkit-scrollbar,
.fill-answers-scroll-wrapper::-webkit-scrollbar {
  width: 6px;
}

.options-scroll-wrapper::-webkit-scrollbar-track,
.fill-answers-scroll-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.options-scroll-wrapper::-webkit-scrollbar-thumb,
.fill-answers-scroll-wrapper::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.options-scroll-wrapper::-webkit-scrollbar-thumb:hover,
.fill-answers-scroll-wrapper::-webkit-scrollbar-thumb:hover {
  background: #999;
}

.option-item, .answer-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;
  gap: 10px;
}

.option-item input[type="text"] {
  flex: 1;
}

.checkbox-label {
  display: flex;
  align-items: center;
  margin: 0 10px;
  cursor: pointer;
  white-space: nowrap;
}

.checkbox-label input {
  width: auto;
  margin-right: 5px;
}

.single-hint {
  font-size: 12px;
  color: #999;
  margin-left: 5px;
}

.remove-btn {
  padding: 6px 10px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.remove-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.remove-btn:hover:not(:disabled) {
  background: #f5f5f5;
  color: #ff4d4f;
  border-color: #ff4d4f;
}

.add-btn {
  padding: 8px 16px;
  background: #f5f5f5;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #666;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.add-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.add-btn:hover:not(:disabled) {
  background: #e8e8e8;
}

.max-tip {
  font-size: 12px;
  color: #ff4d4f;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 30px;
}

.cancel-btn {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #666;
  cursor: pointer;
}

.cancel-btn:hover {
  background: #f5f5f5;
}

.submit-btn {
  padding: 8px 16px;
  background: #42b983;
  border: none;
  border-radius: 4px;
  color: #fff;
  cursor: pointer;
}

.submit-btn:hover {
  background: #36a371;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .option-item, .answer-item {
    flex-direction: column;
    align-items: stretch;
  }

  .checkbox-label {
    margin: 10px 0;
  }

  .options-scroll-wrapper, .fill-answers-scroll-wrapper {
    max-height: 150px;
  }
}
</style>