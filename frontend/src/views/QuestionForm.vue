<!--QuestionForm.vue-->
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
          <option value="option">选择题</option>
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

      <!-- 选项区域（根据题型动态显示：匹配下拉框的 "option" 值） -->
      <div v-if="form.type === 'option'" class="options-group">
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

      <!-- 填空题答案区域（根据题型动态显示：匹配下拉框的 "fill" 值） -->
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
import {computed, ref} from 'vue';
import { defineProps, defineEmits } from 'vue';

// 计算属性：自动判断是否为编辑模式（根据是否有ID）
const isEdit = computed(() => !!form.value.id);

// 接收外部参数
const props = defineProps({
  // 接收父组件传递的edit-data参数（关键修复）
  editData: {
    type: Object,
    default: () => ({})
  },
  // 接收题库ID（父组件传递的question-set-id）
  questionSetId: {
    type: [String, Number],
    required: true
  }
});

// 定义事件
const emit = defineEmits(['save', 'cancel']);

// 表单数据初始化（修正后）
const form = ref({
  id: props.editData.id || null,  // 从editData获取ID（编辑时存在）
  questionSetId: props.questionSetId,  // 关联当前题库ID
  content: props.editData.content || '',
  // 类型转换：后端1→选择题(option)，2→填空题(fill)
  type: props.editData.type === 1 ? 'option' : (props.editData.type === 2 ? 'fill' : ''),
  // 难度转换：后端1→easy，2→medium，3→hard
  difficulty: props.editData.difficulty === 1 ? 'easy' :
      props.editData.difficulty === 2 ? 'medium' :
          props.editData.difficulty === 3 ? 'hard' : '',
  // 选择题选项（编辑时从editData获取，新增时默认2个选项）
  options: props.editData.options ? [...props.editData.options] : [
    { content: '', isCorrect: 0, sortOrder: 1 },
    { content: '', isCorrect: 0, sortOrder: 2 }
  ],
  // 填空题答案（编辑时从editData获取，新增时默认1个空）
  fillAnswers: props.editData.fillAnswers ? [...props.editData.fillAnswers] : [
    { answer: '', sortOrder: 1 }
  ]
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

  // 2. 校验选择题选项（至少2个，且必须有正确答案）
  if (form.value.type === 'option') {
    if (form.value.options.length < 2) {
      alert('选择题至少需要2个选项');
      return;
    }
    const hasCorrect = form.value.options.some(opt => opt.isCorrect === 1);
    if (!hasCorrect) {
      alert('选择题必须设置正确答案');
      return;
    }
    // 校验选项内容不能为空
    const hasEmptyOption = form.value.options.some(opt => !opt.content.trim());
    if (hasEmptyOption) {
      alert('选择题选项内容不能为空');
      return;
    }
  }

  // 3. 校验填空题答案（至少1个，且内容不为空）
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

  // 4. 转换 type 为后端需要的整数
  let typeInt;
  switch (form.value.type) {
    case 'option':
      typeInt = 1; // 选择题对应后端整数1
      break;
    case 'fill':
      typeInt = 2; // 填空题对应后端整数2
      break;
    default:
      alert('题目类型错误');
      return;
  }

  // 5. 转换 difficulty 为后端需要的整数
  let difficultyInt;
  switch (form.value.difficulty) {
    case 'easy':
      difficultyInt = 1; // 简单对应后端整数1
      break;
    case 'medium':
      difficultyInt = 2; // 中等对应后端整数2
      break;
    case 'hard':
      difficultyInt = 3; // 困难对应后端整数3
      break;
    default:
      alert('难度级别错误');
      return;
  }

  // 6. 构建提交数据（替换为转换后的整数，清理无关字段）
  const submitData = {
    ...form.value,
    type: typeInt,       // 替换为整数类型
    difficulty: difficultyInt, // 替换为整数类型
    // 清除无关字段（选择题不需要fillAnswers，填空题不需要options）
    ...(form.value.type !== 'fill' && { fillAnswers: null }),
    ...(form.value.type === 'fill' && { options: null })
  };

  // 调试：打印提交数据
  console.log('最终提交数据：', submitData);

  // 7. 提交给父组件
  console.log('提交数据是否包含ID（编辑时必须有）：', submitData.id);
  emit('save', submitData);
};

// 取消操作
const handleCancel = () => {
  emit('cancel');
};
</script>

<style scoped>
/* 样式部分保持不变 */
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