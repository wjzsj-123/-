<!--QuestionSet.vue-->
<template>
  <div class="question-set-detail">
    <!-- 题库基本信息 -->
    <div class="set-header">
      <h2>{{ questionSet.name }}</h2>
      <p>{{ questionSet.description || '无描述' }}</p>
      <div class="set-meta">
        <span>分类：{{ questionSet.category }}</span>
        <span>创建时间：{{ formatTime(questionSet.createTime) }}</span>
      </div>
    </div>

    <!-- 题目操作区 -->
    <div class="question-actions">
      <button class="add-question-btn" @click="handleAddQuestion">
        + 新增题目
      </button>
    </div>

    <!-- 题目列表 -->
    <div class="question-list">
      <div v-if="questions.length === 0" class="empty-tip">
        该题库暂无题目，点击"新增题目"添加
      </div>
      <div v-else class="question-item" v-for="(q, index) in questions" :key="q.id">
        <div class="question-header">
          <span class="question-index">{{ index + 1 }}.</span>
          <span class="question-type">
          {{ q.type === 1 ? '单选题' : q.type === 3 ? '多选题' : '填空题' }}
          </span>
          <span class="question-difficulty">
            {{ getDifficultyText(q.difficulty) }}
          </span>
        </div>
        <div class="question-content">{{ q.content }}</div>

        <!-- 选择题选项展示 -->
        <div v-if="q.type === 1 || q.type === 3" class="question-options">
          <div v-for="(opt, i) in q.options" :key="opt.id">
            <span class="option-letter">{{ String.fromCharCode(65 + i) }}.</span>
            <span class="option-content">{{ opt.content }}</span>
            <span v-if="opt.isCorrect === 1" class="correct-tag">正确答案</span>
          </div>
        </div>

        <!-- 填空题答案展示 -->
        <div v-if="q.type === 2" class="fill-answers">
          <div>答案：
            <span v-for="(ans, i) in q.fillAnswers" :key="ans.id">
              第{{ i + 1 }}空：{{ ans.answer }} {{ i < q.fillAnswers.length - 1 ? '；' : '' }}
            </span>
          </div>
        </div>

        <div class="question-actions">
          <button @click="handleEdit(q)">编辑</button>
          <button @click="handleDelete(q.id)">删除</button>
        </div>
      </div>
    </div>

    <!-- 题目表单弹窗 -->
    <div v-if="showQuestionForm" class="form-modal">
      <div class="modal-overlay" @click="showQuestionForm = false"></div>
      <div class="modal-content">
        <QuestionForm
            :question-set-id="questionSetId"
            :edit-data="currentEditQuestion"
            @save="onQuestionSaved"
            @cancel="showQuestionForm = false"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import QuestionForm from './QuestionForm.vue'; // 引入题目表单组件

// 路由参数（获取当前题库ID）
const route = useRoute();
const questionSetId = ref(route.params.id); // 假设路由为 /home/question-set/:id

// 题库信息
const questionSet = ref({});

// 题目列表
const questions = ref([]);
console.log('获取到的题目列表数据:', questions)

// 表单控制
const showQuestionForm = ref(false);
const currentEditQuestion = ref(null);

// 新增方法：处理新增题目逻辑
const handleAddQuestion = () => {
  currentEditQuestion.value = null; // 关键：清空编辑数据
  showQuestionForm.value = true;    // 显示表单
};

// 获取题库详情
const getQuestionSetDetail = async () => {
  try {
    const response = await fetch(`/api/question-set/${questionSetId.value}`);
    const result = await response.json();
    if (result.code === 0) {
      questionSet.value = result.data;
    }
  } catch (err) {
    console.error('获取题库详情失败', err);
  }
};

// 获取题库下的所有题目
const getQuestions = async () => {
  try {
    const response = await fetch(`/api/question/question-set/${questionSetId.value}`);
    const result = await response.json();
    if (result.code === 0) {
      questions.value = result.data;
    }
  } catch (err) {
    console.error('获取题目列表失败', err);
  }
};

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  return new Date(timeStr).toLocaleString();
};

// 转换难度文本
const getDifficultyText = (difficulty) => {
  switch (difficulty) {
    case 1: return '简单';
    case 2: return '中等';
    case 3: return '困难';
    default: return '未知';
  }
};

// 编辑题目
const handleEdit = async (question) => {
  try {
    // 1. 先从后端获取最新的题目详情（确保编辑的数据是最新的）
    const response = await fetch(`/api/question/${question.id}`);
    const result = await response.json();

    if (result.code === 0) {
      // 2. 成功获取后打开编辑表单，并填充数据
      // 深拷贝避免直接修改原数据
      currentEditQuestion.value = JSON.parse(JSON.stringify(result.data));
      showQuestionForm.value = true;
    } else {
      alert(result.message || '获取题目详情失败，无法编辑');
      console.error('获取编辑数据失败:', result.message);
    }
  } catch (err) {
    console.error('编辑操作失败:', err);
    alert('网络错误，无法加载编辑数据');
  }
};

// 删除题目
const handleDelete = async (id) => {
  if (confirm('确定要删除这道题目吗？')) {
    try {
      const response = await fetch(`/api/question/${id}`, { method: 'DELETE' });
      const result = await response.json();
      if (result.code === 0) {
        getQuestions(); // 重新加载题目列表
      } else {
        alert(result.message || '删除失败');
      }
    } catch (err) {
      console.error('删除题目失败', err);
    }
  }
};

// 题目保存成功后回调（修正后）
const onQuestionSaved = async (questionData) => {
  try {
    // 1. 发送请求到后端新增/编辑题目接口（直接透传子组件处理后的questionData，不做二次转换）
    const response = await fetch('/api/question', {
      method: questionData.id ? 'PUT' : 'POST', // 有id则为编辑（PUT），无id则为新增（POST）
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        ...questionData,
        questionSetId: questionSetId.value // 仅确保题库ID（若子组件已传递，可省略，此处保留兜底）
        // 关键：删除type和difficulty的重复转换逻辑！
      })
    });

    const result = await response.json();
    if (result.code === 0) {
      // 2. 操作成功后关闭弹窗并刷新列表
      showQuestionForm.value = false;
      currentEditQuestion.value = null;
      // 3. 局部刷新：重新加载题库详情和题目列表（核心修改）
      await getQuestionSetDetail(); // 刷新题库信息
      await getQuestions(); // 刷新题目列表
      alert(questionData.id ? '编辑成功' : '新增成功');
    } else {
      alert(result.message || '操作失败');
    }
  } catch (err) {
    console.error('题目保存失败:', err);
    alert('网络错误，保存失败');
  }
};

// 页面加载时初始化数据
onMounted(() => {
  getQuestionSetDetail();
  getQuestions();
});
</script>

<style scoped>
.set-header {
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.set-meta {
  margin-top: 0.5rem;
  color: #666;
  display: flex;
  gap: 20px;
}

.question-actions {
  margin-bottom: 1rem;
  text-align: right;
}

.add-question-btn {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

.question-list {
  border: 1px solid #eee;
  border-radius: 4px;
  overflow: hidden;
}

.empty-tip {
  text-align: center;
  padding: 2rem;
  color: #999;
}

.question-item {
  padding: 1rem;
  border-bottom: 1px solid #eee;
}

.question-item:last-child {
  border-bottom: none;
}

.question-header {
  display: flex;
  gap: 10px;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
}

.question-index {
  font-weight: bold;
}

.question-type, .question-difficulty {
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.8rem;
  color: white;
}

.question-type {
  background-color: #42b983;
}

.question-difficulty {
  background-color: #2196f3;
}

.question-content {
  margin-bottom: 1rem;
  font-size: 1.05rem;
}

.question-options {
  margin: 0.5rem 0 1rem 1rem;
}

.option-item {
  margin-bottom: 0.3rem;
}

.option-letter {
  font-weight: bold;
  margin-right: 5px;
}

.correct-tag {
  margin-left: 10px;
  color: #ff4444;
  font-weight: bold;
  font-size: 0.8rem;
}

.fill-answers {
  margin: 0.5rem 0 1rem 0;
  color: #666;
}

.form-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1000;
}

.modal-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
}

.modal-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 90%;
  max-width: 600px;
  background-color: white;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 20px rgba(0, 0, 0, 0.2);
}
</style>