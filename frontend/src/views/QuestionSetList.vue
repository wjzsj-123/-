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
      >
      <select v-model="selectedCategory" class="category-select">
        <option value="">全部分类</option>
        <option v-for="category in categories" :key="category" :value="category">
          {{ category }}
        </option>
      </select>
      <button class="search-btn" @click="fetchQuestionSets">搜索</button>
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
        </td>
      </tr>
      <tr v-if="questionSets.length === 0">
        <td colspan="5" class="empty-text">暂无题库数据</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const questionSets = ref([]);
const searchName = ref('');
const selectedCategory = ref('');
const categories = ref(['编程语言', '数据库', '操作系统', '计算机网络']); // 示例分类

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return '';
  return new Date(timeStr).toLocaleString();
};

// 获取题库列表
const fetchQuestionSets = async () => {
  try {
    // 从localStorage获取当前用户ID
    const userInfo = JSON.parse(localStorage.getItem('userInfo'));
    let url = `/api/question-set?userId=${userInfo.id}`;

    // 添加搜索参数
    if (searchName.value) url += `&name=${searchName.value}`;
    if (selectedCategory.value) url += `&category=${selectedCategory.value}`;

    const response = await fetch(url);
    const result = await response.json();

    if (result.code === 0) {
      questionSets.value = result.data;
    } else {
      alert(result.message || '获取题库列表失败');
    }
  } catch (err) {
    console.error('获取题库失败:', err);
    alert('网络错误，请稍后重试');
  }
};

// 新增题库
const handleAdd = () => {
  router.push('/home/question-set/add');
};

// 编辑题库
const handleEdit = (set) => {
  router.push(`/home/question-set/edit/${set.id}`);
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

// 页面加载时获取数据
onMounted(() => {
  fetchQuestionSets();
});
</script>

<style scoped>
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
</style>