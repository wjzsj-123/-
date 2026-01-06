<template>
  <div class="question-set-detail">
    <!-- 题库基本信息 -->
    <div class="set-header">
      <h2>{{ questionSet.name }}</h2>
      <p>{{ questionSet.description || '无描述' }}</p>
      <div class="set-meta">
        <span>分类：{{ questionSet.category }}</span>
        <span>创建时间：{{ formatTime(questionSet.createTime) }}</span>
        <span class="public-status">
          状态：{{ isPublic ? '公有' : '私有' }}
        </span>
      </div>
    </div>

    <!-- 搜索筛选区 -->
    <div class="search-filter">
      <div class="search-input">
        <input
            type="text"
            v-model="searchContent"
            placeholder="输入题目内容搜索..."
            @input="handleSearch"
        >
      </div>
      <div class="filter-options">
        <select v-model="selectedType" @change="handleSearch">
          <option value="">所有题型</option>
          <option value="1">单选题</option>
          <option value="3">多选题</option>
          <option value="2">填空题</option>
        </select>
        <select v-model="selectedDifficulty" @change="handleSearch">
          <option value="">所有难度</option>
          <option value="1">简单</option>
          <option value="2">中等</option>
          <option value="3">困难</option>
        </select>
      </div>
    </div>

    <!-- 题目操作区 -->
    <div class="question-actions">
      <button class="add-question-btn" @click="handleAddQuestion">
        + 新增题目
      </button>
      <el-upload
          class="import-btn"
          :action="`/api/question-set/import/${questionSetId}`"
          :on-success="handleImportSuccess"
          :on-error="handleImportError"
          :before-upload="beforeUpload"
          accept=".xlsx,.xls"
          :show-file-list="true"
      >
        <button class="import-question-btn">
          ↓ 导入题目
        </button>
      </el-upload>

      <!-- 显示模式切换按钮 -->
      <div class="display-mode-switch">
        <button
            :class="{ active: displayMode === 'full' }"
            @click="displayMode = 'full'"
        >
          完整模式
        </button>
        <button
            :class="{ active: displayMode === 'simple' }"
            @click="displayMode = 'simple'"
        >
          简化模式
        </button>
      </div>

      <!-- 公有/私有切换按钮 -->
      <button
          class="public-switch-btn"
          :class="{ public: isPublic, private: !isPublic }"
          @click="togglePublicStatus"
          :disabled="isSwitchLoading"
      >
        <span v-if="isSwitchLoading">切换中...</span>
        <span v-else>{{ isPublic ? '设为私有' : '设为公有' }}</span>
      </button>
    </div>

    <!-- 题目列表 -->
    <div class="question-list">
      <!-- 空状态区分：无题目 / 有题目但无匹配结果 -->
      <div v-if="questions.length === 0" class="empty-tip">
        该题库暂无题目，点击"新增题目"添加
      </div>
      <div v-else-if="filteredQuestions.length === 0" class="empty-tip">
        没有找到匹配的题目，请调整搜索条件
      </div>
      <!-- 核心修改：使用 filteredQuestions 渲染筛选后的列表 -->
      <div v-else class="question-item" v-for="(q, index) in filteredQuestions" :key="q.id">
        <!-- 通用头部（两种模式都显示） -->
        <div class="question-header">
          <span class="question-index">{{ index + 1 }}.</span>
          <span class="question-type">
            {{ q.type === 1 ? '单选题' : q.type === 3 ? '多选题' : '填空题' }}
          </span>
          <span class="question-difficulty">
            {{ getDifficultyText(q.difficulty) }}
          </span>
        </div>

        <!-- 完整模式显示内容 -->
        <div v-if="displayMode === 'full'">
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
        </div>

        <!-- 简化模式显示内容（只显示题目内容和操作按钮） -->
        <div v-if="displayMode === 'simple'" class="simple-content">
          <div class="question-content">{{ q.content }}</div>
        </div>

        <!-- 操作按钮（两种模式都显示） -->
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
import { ref, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import QuestionForm from "@/views/QuestionForm.vue";
import { ElMessage } from 'element-plus'; // 导入消息提示

// 显示模式（默认完整模式）
const displayMode = ref('full'); // 'full' 完整模式, 'simple' 简化模式

// 搜索筛选相关变量
const searchContent = ref('');
const selectedType = ref('');
const selectedDifficulty = ref('');
const filteredQuestions = ref([]);

// 路由参数（获取当前题库ID）
const route = useRoute();
const questionSetId = ref(route.params.id); // 假设路由为 /home/question-set/:id
// 题库信息
const questionSet = ref({});
// 题目列表
const questions = ref([]);

// 公有/私有状态管理
const isPublic = ref(false); // 当前是否为公有题库
const isSwitchLoading = ref(false); // 切换状态loading

// 表单控制
const showQuestionForm = ref(false);
const currentEditQuestion = ref(null);

// 筛选逻辑
const handleSearch = () => {
  // 空数组保护
  if (!questions.value || questions.value.length === 0) {
    filteredQuestions.value = [];
    return;
  }

  filteredQuestions.value = questions.value.filter(question => {
    // 内容筛选（兼容content为空的情况）
    const contentMatch = !searchContent.value.trim()
        || (question.content && question.content.toLowerCase().includes(searchContent.value.toLowerCase().trim()));

    // 类型筛选（兼容type为空的情况）
    const typeMatch = !selectedType.value
        || (question.type !== undefined && question.type === Number(selectedType.value));

    // 难度筛选（兼容difficulty为空的情况）
    const difficultyMatch = !selectedDifficulty.value
        || (question.difficulty !== undefined && question.difficulty === Number(selectedDifficulty.value));

    return contentMatch && typeMatch && difficultyMatch;
  });
};

// 监听筛选条件变化，自动触发搜索（增强体验）
watch([searchContent, selectedType, selectedDifficulty], () => {
  handleSearch();
}, { immediate: true });

// 导入相关方法
const beforeUpload = (file) => {
  // 校验文件类型
  const isExcel = file.type === 'application/vnd.ms-excel'
      || file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
  if (!isExcel) {
    ElMessage.error('只能上传Excel文件（.xlsx/.xls）');
    return false;
  }
  // 校验文件大小（不超过2MB）
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    ElMessage.error('文件大小不能超过2MB');
    return false;
  }
  return true;
};

const handleImportSuccess = (response) => {
  if (response.code === 0) {
    ElMessage.success(`导入成功，共导入${response.data}道题目`);
    // 重新加载题目列表并触发筛选
    getQuestions();
  } else {
    ElMessage.error(response.message || '导入失败');
  }
};

const handleImportError = () => {
  ElMessage.error('文件上传失败，请检查网络或文件格式');
};

// 处理新增题目逻辑
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
      isPublic.value = result.data.isPublic === 1;
    }
  } catch (err) {
    console.error('获取题库详情失败', err);
  }
};

// 切换公有/私有状态
const togglePublicStatus = async () => {
  try {
    isSwitchLoading.value = true;
    // 1. 获取当前登录用户ID（从localStorage中读取，需和登录逻辑保持一致）
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    const publisherId = userInfo.id;

    // 2. 构造请求参数：isPublic（目标状态） + publisherId（发布人ID）
    const targetStatus = !isPublic.value;
    const params = new URLSearchParams();
    params.append('isPublic', targetStatus);
    // 公有状态时必传发布人ID，私有状态不传
    if (targetStatus) {
      if (!publisherId) {
        ElMessage.error('请先登录后再将题库设为公有');
        isSwitchLoading.value = false;
        return;
      }
      params.append('publisherId', publisherId);
    }

    // 3. 调用后端PUT接口（适配后端路径和请求方式）
    const response = await fetch(`/api/question-set/${questionSetId.value}/public-status`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded', // 适配form-data参数格式
      },
      body: params
    });

    const result = await response.json();
    if (result.code === 0) {
      // 4. 更新前端状态
      isPublic.value = targetStatus;
      ElMessage.success(`题库已${isPublic.value ? '设为公有' : '设为私有'}`);
      // 重新获取题库详情（同步最新状态）
      getQuestionSetDetail();
    } else {
      ElMessage.error(result.message || '切换状态失败');
    }
  } catch (err) {
    ElMessage.error('网络错误，切换状态失败');
    console.error('切换公有/私有失败', err);
  } finally {
    isSwitchLoading.value = false;
  }
};

// 获取题目列表后初始化筛选
const getQuestions = async () => {
  try {
    const response = await fetch(`/api/question/question-set/${questionSetId.value}`);
    const result = await response.json();
    if (result.code === 0) {
      questions.value = result.data;
      // 初始化筛选
      handleSearch();
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
      ElMessage.error(result.message || '获取题目详情失败，无法编辑');
      console.error('获取编辑数据失败:', result.message);
    }
  } catch (err) {
    console.error('编辑操作失败:', err);
    ElMessage.error('网络错误，无法加载编辑数据');
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
        ElMessage.success('删除成功');
      } else {
        ElMessage.error(result.message || '删除失败');
      }
    } catch (err) {
      console.error('删除题目失败', err);
      ElMessage.error('网络错误，删除失败');
    }
  }
};

// 题目保存成功后回调
const onQuestionSaved = async (questionData) => {
  try {
    // 1. 发送请求到后端新增/编辑题目接口
    const response = await fetch('/api/question', {
      method: questionData.id ? 'PUT' : 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        ...questionData,
        questionSetId: questionSetId.value
      })
    });

    const result = await response.json();
    if (result.code === 0) {
      // 2. 操作成功后关闭弹窗并刷新列表
      showQuestionForm.value = false;
      currentEditQuestion.value = null;
      // 3. 局部刷新：重新加载题库详情和题目列表
      await getQuestionSetDetail();
      await getQuestions();
      ElMessage.success(questionData.id ? '编辑成功' : '新增成功');
    } else {
      ElMessage.error(result.message || '操作失败');
    }
  } catch (err) {
    console.error('题目保存失败:', err);
    ElMessage.error('网络错误，保存失败');
  }
};

// 页面加载时初始化数据
onMounted(() => {
  getQuestionSetDetail();
  getQuestions();
});
</script>

<style scoped>
.set-meta .public-status {
  margin-left: 15px;
  color: #42b983;
  font-weight: 500;
}

.question-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 20px 0;
  flex-wrap: wrap;
}

.public-switch-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.public-switch-btn.public {
  background-color: #ff7875;
  color: white;
}

.public-switch-btn.private {
  background-color: #42b983;
  color: white;
}

.public-switch-btn:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.public-switch-btn:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-2px);
}

/* 搜索筛选样式 */
.search-filter {
  display: flex;
  gap: 15px;
  margin-bottom: 1.5rem;
  align-items: center;
  flex-wrap: wrap;
}

.search-input input {
  width: 300px;
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.filter-options {
  display: flex;
  gap: 10px;
}

.filter-options select {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: white;
  cursor: pointer;
}

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
  display: flex;
  gap: 10px;
  justify-content: flex-end; /* 右对齐 */
  align-items: center;
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

.import-btn {
  display: inline-block;
}

.import-question-btn {
  background-color: #ff9800; /* 橙色标识导入 */
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
}

/* 显示模式切换按钮样式 */
.display-mode-switch {
  display: flex;
  margin-left: 15px;
}

.display-mode-switch button {
  padding: 0.5rem 1rem;
  border: 1px solid #42b983;
  background: transparent;
  color: #42b983;
  cursor: pointer;
}

.display-mode-switch button:first-child {
  border-radius: 4px 0 0 4px;
}

.display-mode-switch button:last-child {
  border-radius: 0 4px 4px 0;
}

.display-mode-switch button.active {
  background-color: #42b983;
  color: white;
}

/* 新增：简化模式样式 */
.simple-content .simple-hint {
  color: #666;
  font-size: 0.9rem;
  margin: 0.5rem 0;
  font-style: italic;
}
</style>