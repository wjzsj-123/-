<template>
  <div class="online-bank-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-item">
        <label>题库分类：</label>
        <select v-model="searchParams.category">
          <option value="">全部</option>
          <option value="Java">Java</option>
          <option value="Python">Python</option>
          <option value="前端">前端</option>
          <option value="数据库">数据库</option>
        </select>
      </div>
      <div class="search-item">
        <label>题库名称：</label>
        <input
            type="text"
            v-model="searchParams.name"
            placeholder="请输入题库名称模糊搜索"
            @keyup.enter="getPublicQuestionSets"
        >
      </div>
      <button class="search-btn" @click="getPublicQuestionSets">搜索</button>
      <button class="reset-btn" @click="resetSearch">重置</button>
    </div>

    <!-- 题库列表 -->
    <div class="bank-list">
      <div class="bank-card" v-for="bank in bankList" :key="bank.id">
        <div class="bank-header">
          <h3>{{ bank.name }}</h3>
          <span class="publisher">发布者ID：{{ bank.publisherId }}</span>
        </div>
        <div class="bank-body">
          <p><span class="label">分类：</span>{{ bank.category }}</p>
          <p><span class="label">发布时间：</span>{{ formatTime(bank.publishTime) }}</p>
        </div>
        <div class="bank-footer">
          <button class="import-btn" @click="importBank(bank.id)">导入为我的题库</button>
        </div>
      </div>
    </div>

    <!-- 空数据提示 -->
    <div class="empty-tip" v-if="bankList.length === 0 && !loading">
      暂无公开题库数据
    </div>

    <!-- 加载中提示 -->
    <div class="loading-tip" v-if="loading">
      加载中...
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus' // 如需使用element-ui提示，需先安装：npm i element-plus

// 搜索参数
const searchParams = ref({
  category: '',
  name: ''
})

// 题库列表
const bankList = ref([])
// 加载状态
const loading = ref(false)

// 获取当前登录用户ID（从localStorage中解析）
const getCurrentUserId = () => {
  const userInfo = localStorage.getItem('userInfo')
  return userInfo ? JSON.parse(userInfo).id : ''
}

// 格式化时间（简单处理，可根据需求优化）
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return new Date(timeStr).toLocaleString()
}

// 查询公共题库列表
const getPublicQuestionSets = async () => {
  loading.value = true
  try {
    // 拼接查询参数
    const params = new URLSearchParams()
    if (searchParams.value.category) params.append('category', searchParams.value.category)
    if (searchParams.value.name) params.append('name', searchParams.value.name)

    const response = await fetch(`/api/question-set/public?${params.toString()}`, {
      method: 'GET',
      headers: { 'Content-Type': 'application/json' }
    })
    const result = await response.json()

    if (result.code === 0) {
      bankList.value = result.data || []
    } else {
      ElMessage.error(result.message || '查询失败')
    }
  } catch (err) {
    console.error('查询公共题库失败：', err)
    ElMessage.error('网络错误，查询失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索条件
const resetSearch = () => {
  searchParams.value.category = ''
  searchParams.value.name = ''
  getPublicQuestionSets()
}

// 导入公共题库
const importBank = async (publicSetId) => {
  const userId = getCurrentUserId()
  if (!userId) {
    ElMessage.error('未获取到用户信息，请重新登录')
    return
  }

  try {
    const response = await fetch(`/api/question-set/public/import/${publicSetId}?userId=${userId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' }
    })
    const result = await response.json()

    if (result.code === 0) {
      ElMessage.success('导入成功，已添加到我的题库')
    } else {
      ElMessage.error(result.message || '导入失败')
    }
  } catch (err) {
    console.error('导入题库失败：', err)
    ElMessage.error('网络错误，导入失败')
  }
}

// 页面挂载时初始化查询
onMounted(() => {
  getPublicQuestionSets()
})
</script>

<style scoped>
.online-bank-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 搜索栏样式 */
.search-bar {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 15px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.search-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-item label {
  color: #333;
  font-weight: 500;
}

.search-item select, .search-item input {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  min-width: 150px;
}

.search-btn, .reset-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.search-btn {
  background: #42b983;
  color: #fff;
}

.reset-btn {
  background: #f5f5f5;
  color: #666;
}

/* 题库列表样式 */
.bank-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.bank-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.bank-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 10px;
}

.bank-header h3 {
  font-size: 16px;
  color: #333;
  margin: 0;
}

.bank-header .publisher {
  font-size: 12px;
  color: #999;
}

.bank-body {
  flex: 1;
}

.bank-body .label {
  color: #666;
  font-weight: 500;
}

.bank-footer {
  text-align: right;
}

.import-btn {
  padding: 6px 15px;
  background: #409eff;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.import-btn:hover {
  background: #66b1ff;
}

/* 空数据/加载中提示 */
.empty-tip, .loading-tip {
  text-align: center;
  padding: 50px;
  color: #999;
  font-size: 16px;
}

.loading-tip {
  color: #666;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
    align-items: flex-start;
  }

  .bank-list {
    grid-template-columns: 1fr;
  }
}
</style>