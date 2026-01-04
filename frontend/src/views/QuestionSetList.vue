<template>
  <div class="question-set-list">
    <div class="header-actions">
      <h2>题库管理</h2>
      <button class="add-btn" @click="handleAdd">新增题库</button>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="filter-bar">
      <input
          type="text"
          v-model="searchName"
          placeholder="请输入题库名称搜索"
          class="search-input"
          @keyup.enter="fetchQuestionSets"
      >
      <select v-model="selectedCategory" class="category-select" @change="fetchQuestionSets">
        <option value="">全部分类</option>
        <option v-for="category in categories" :key="category" :value="category">
          {{ category }}
        </option>
      </select>
      <button class="search-btn" @click="fetchQuestionSets">搜索</button>
      <button class="reset-btn" @click="resetFilter">重置</button>
    </div>

    <!-- 题库列表表格 -->
    <table class="set-table">
      <thead>
      <tr>
        <th>ID</th>
        <th>题库名称</th>
        <th>分类</th>
        <th>创建时间</th>
        <th>操作</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="set in questionSets" :key="set.id">
        <td>{{ set.id }}</td>
        <td>{{ set.name }}</td>
        <td>{{ set.category }}</td>
        <td>{{ formatTime(set.createTime) }}</td>
        <td class="action-buttons">
          <button @click="handleEdit(set)" class="edit-btn">编辑</button>
          <button @click="handleDelete(set.id)" class="delete-btn">删除</button>
          <router-link :to="`/home/question-set/${set.id}`" class="detail-btn">
            查看题目
          </router-link>
          <button
              @click="exportQuestionSet(set.id)"
              class="export-btn"
              :disabled="loading"
          >
            导出题库
          </button>
        </td>
      </tr>
      <tr v-if="questionSets.length === 0">
        <td colspan="5" class="empty-text">暂无题库数据</td>
      </tr>
      </tbody>
    </table>

    <!-- 编辑题库弹窗 -->
    <div v-if="showEditModal" class="modal-overlay">
      <div class="modal-content">
        <h3>编辑题库</h3>
        <form @submit.prevent="handleSaveEdit">
          <div class="form-group">
            <label>题库名称 <span class="required">*</span></label>
            <input
                type="text"
                v-model="editForm.name"
                required
                placeholder="请输入题库名称"
            >
          </div>

          <div class="form-group">
            <label>分类 <span class="required">*</span></label>
            <select v-model="editForm.category" required>
              <option value="">请选择分类</option>
              <option v-for="category in categories" :key="category" :value="category">
                {{ category }}
              </option>
            </select>
          </div>

          <div class="form-group">
            <label>描述</label>
            <textarea
                v-model="editForm.description"
                rows="4"
                placeholder="请输入题库描述（可选）"
            ></textarea>
          </div>

          <div class="form-actions">
            <button type="button" class="cancel-btn" @click="showEditModal = false">取消</button>
            <button type="submit" class="save-btn">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

const router = useRouter();
const questionSets = ref([]);
const searchName = ref('');
const selectedCategory = ref('');
const categories = ref(['编程语言', '数据库', '操作系统', '计算机网络']);
const loading = ref(false);  // 新增：加载状态

// 编辑相关状态
const showEditModal = ref(false);
const editForm = ref({
  id: null,
  name: '',
  category: '',
  description: ''
});

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  return new Date(timeStr).toLocaleString();
};

// 重置筛选条件
const resetFilter = () => {
  searchName.value = '';
  selectedCategory.value = '';
  fetchQuestionSets();  // 重置后重新加载
};

// 导出题库方法
const exportQuestionSet = (setId) => {
  if (!setId) {
    ElMessage.error('题库ID不存在');
    return;
  }
  try {
    loading.value = true;
    // 调用后端导出接口，打开新窗口下载
    window.open(`/api/question-set/export/${setId}`, '_blank');
    ElMessage.success('导出请求已发送，正在准备文件...');
  } catch (error) {
    ElMessage.error('导出失败：' + error.message);
  } finally {
    loading.value = false;
  }
};


// 获取题库列表（优化版）
const fetchQuestionSets = async () => {
  try {
    // 1. 获取用户信息并校验
    const userInfoStr = localStorage.getItem('userInfo');
    if (!userInfoStr) {
      alert('请先登录');
      router.push('/login');
      return;
    }
    const userInfo = JSON.parse(userInfoStr);
    if (!userInfo.id) {
      alert('用户信息异常');
      return;
    }

    // 2. 显示加载状态
    loading.value = true;

    // 3. 构建请求URL和参数
    const params = new URLSearchParams();
    params.append('userId', userInfo.id);  // 必传用户ID
    if (searchName.value.trim()) {
      params.append('name', searchName.value.trim());  // 去除首尾空格
    }
    if (selectedCategory.value) {
      params.append('category', selectedCategory.value);
    }

    const url = `/api/question-set/user/${userInfo.id}/filter?${params.toString()}`;

    // 4. 发送请求
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`HTTP错误，状态码: ${response.status}`);
    }
    const result = await response.json();

    // 5. 处理响应
    if (result.code === 0) {
      questionSets.value = result.data || [];  // 兼容空数据
    } else {
      alert(result.message || '获取题库列表失败');
      questionSets.value = [];
    }
  } catch (err) {
    console.error('获取题库失败:', err);
    alert('网络错误，请稍后重试');
    questionSets.value = [];
  } finally {
    // 6. 无论成功失败都关闭加载状态
    loading.value = false;
  }
};

// 新增：监听搜索框输入防抖（避免频繁请求）
const debounce = (func, delay = 500) => {
  let timer;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => func.apply(this, args), delay);
  };
};

// 为搜索框添加防抖处理
const handleSearchInput = debounce(() => {
  fetchQuestionSets();
});

// 新增题库
const handleAdd = () => {
  router.push('/home/question-set/add');
};

// 编辑题库
const handleEdit = async (set) => {
  try {
    // 获取题库详情
    const response = await fetch(`/api/question-set/${set.id}`);
    const result = await response.json();

    if (result.code === 0) {
      // 填充编辑表单
      editForm.value = {
        id: result.data.id,
        name: result.data.name,
        category: result.data.category,
        description: result.data.description || ''
      };
      showEditModal.value = true;
    } else {
      alert(result.message || '获取题库详情失败');
    }
  } catch (err) {
    console.error('获取题库详情失败:', err);
    alert('网络错误，请稍后重试');
  }
};

// 保存编辑
const handleSaveEdit = async () => {
  if (!editForm.value.name.trim()) {
    alert('请输入题库名称');
    return;
  }
  if (!editForm.value.category) {
    alert('请选择分类');
    return;
  }

  try {
    const response = await fetch(`/api/question-set/${editForm.value.id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        id: editForm.value.id,
        name: editForm.value.name,
        category: editForm.value.category,
        description: editForm.value.description
      })
    });

    const result = await response.json();
    if (result.code === 0) {
      showEditModal.value = false;
      fetchQuestionSets(); // 重新加载列表
      alert('修改成功');
    } else {
      alert(result.message || '修改失败');
    }
  } catch (err) {
    console.error('修改题库失败:', err);
    alert('网络错误，修改失败');
  }
};

// 删除题库
const handleDelete = async (id) => {
  if (confirm('确定要删除该题库吗？删除后不可恢复！')) {
    try {
      const response = await fetch(`/api/question-set/${id}`, {
        method: 'DELETE'
      });
      const result = await response.json();

      if (result.code === 0) {
        alert('删除成功');
        fetchQuestionSets(); // 重新加载列表
      } else {
        alert(result.message || '删除失败');
      }
    } catch (err) {
      console.error('删除题库失败:', err);
      alert('网络错误，请稍后重试');
    }
  }
};

// 修改：在onMounted中初始化
onMounted(() => {
  // 初始化时触发一次搜索
  fetchQuestionSets();
});
</script>

<style scoped>
.loading-indicator {
  text-align: center;
  padding: 20px;
  color: #666;
}

.reset-btn {
  background-color: #9e9e9e;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.add-btn {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.filter-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: center;
}

.search-input {
  flex: 1;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.category-select {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.search-btn {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.set-table {
  width: 100%;
  border-collapse: collapse;
  border: 1px solid #ddd;
}

.set-table th,
.set-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.set-table th {
  background-color: #f5f5f5;
  font-weight: bold;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap; /* 防止按钮溢出 */
}

.edit-btn {
  background-color: #4285f4;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
}

.delete-btn {
  background-color: #ea4335;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
}

.detail-btn {
  background-color: #fbbc05;
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
}

.empty-text {
  text-align: center;
  color: #666;
  padding: 20px;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

.required {
  color: #ff4444;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.cancel-btn {
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}

.save-btn {
  background-color: #42b983;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
}

/* 导出按钮样式，与其他按钮风格统一 */
.export-btn {
  background-color: #2196f3; /* 蓝色标识导出 */
  color: white;
  border: none;
  padding: 4px 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.export-btn:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}
</style>