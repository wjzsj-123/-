<template>
  <div class="online-bank-container">
    <!-- 搜索栏 -->
    <div class="search-bar">
      <div class="search-item">
        <label>题库分类：</label>
        <select v-model="searchParams.category">
          <option value="">全部</option>
          <!-- 动态渲染分类选项 -->
          <option
              v-for="category in categories"
              :key="category"
              :value="category"
          >
            {{ category }}
          </option>
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
      <div class="search-item">
        <label>排序方式：</label>
        <select v-model="searchParams.sortBy" @change="onSortChange">
          <option value="publishTime">按发布时间</option>
          <option value="hot">按热度</option>
        </select>
      </div>
      <button class="search-btn" @click="getPublicQuestionSets">搜索</button>
      <button class="reset-btn" @click="resetSearch">重置</button>
    </div>

    <!-- 题库列表 -->
    <div class="bank-list">
      <div class="bank-card" v-for="bank in bankList" :key="bank.id">
        <div class="bank-header">
          <p><span class="label">分类：</span>{{ bank.category }}</p>
          <div class="publisher-line" v-if="bank.publisherId">
            <router-link class="publisher-name" :to="'/home/user/center/' + bank.publisherId">
              {{ publisherDisplayName(bank) }}
            </router-link>
            <button
              v-if="canFollowPublisher(bank)"
              type="button"
              class="follow-pub-btn"
              @click.stop="toggleFollowPublisher(bank)"
            >
              {{ bank.viewerFollowsPublisher ? '取消关注' : '关注' }}
            </button>
          </div>
          <span v-else class="publisher">发布者：—</span>
        </div>
        <div class="bank-body">
          <h3>{{ bank.name }}</h3>
          <p><span class="label">发布时间：</span>{{ formatTime(bank.publishTime) }}</p>
          <p><span class="label">热度：</span>{{ bank.hotScore ?? 0 }}（点赞 {{ bank.likeCount || 0 }} / 导入 {{ bank.importCount || 0 }}）</p>
          <p><span class="label">点踩：</span>{{ bank.dislikeCount || 0 }} <span class="label">好评度：</span>{{ bank.favorability || 0 }}</p>
          <div class="tag-list">
            <span class="label">题库标签：</span>
            <template v-if="bank.questionTags && bank.questionTags.length">
              <span
                  v-for="tag in bank.questionTags"
                  :key="`${bank.id}-${tag}`"
                  class="tag-item"
              >
                {{ tag }}
              </span>
            </template>
            <span v-else class="tag-empty">暂无标签</span>
          </div>
        </div>
        <div class="bank-footer">
          <button
              class="vote-btn"
              :class="{ active: bank.userVote === 1 }"
              @click="voteBank(bank, 1)"
          >
            👍 {{ bank.userVote === 1 ? '已赞' : '点赞' }}
          </button>
          <button
              class="vote-btn dislike"
              :class="{ active: bank.userVote === -1 }"
              @click="voteBank(bank, -1)"
          >
            👎 {{ bank.userVote === -1 ? '已踩' : '点踩' }}
          </button>
          <button class="comment-btn" @click="goDiscussion(bank.id)">查看讨论区</button>
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

    <div v-if="total > 0" class="pagination">
      <button :disabled="page <= 1 || loading" @click="changePage(page - 1)">上一页</button>
      <span>第 {{ page }} / {{ totalPages }} 页（共 {{ total }} 条）</span>
      <button :disabled="page >= totalPages || loading" @click="changePage(page + 1)">下一页</button>
      <span>每页</span>
      <select v-model="size" :disabled="loading" @change="onPageSizeChange">
        <option value="5">5</option>
        <option value="10">10</option>
        <option value="20">20</option>
        <option value="50">50</option>
      </select>
      <span>条</span>
      <input
          v-model="jumpPageInput"
          type="number"
          min="1"
          :max="totalPages"
          class="jump-input"
          :disabled="loading"
          @keyup.enter="jumpToPage"
      />
      <button :disabled="loading" @click="jumpToPage">跳转</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus' // 如需使用element-ui提示，需先安装：npm i element-plus
import { useRouter } from 'vue-router'
import { displayUserName } from '@/utils/userDisplay'
import request from '@/utils/request'

const router = useRouter()

const categories = ref([
  '编程语言',
  '数据库',
  '操作系统',
  '计算机网络',
  '语文',
  '数学',
  '英语',
  '线性代数',
  '高等数学',
  '概率论与数理统计',
  '数据结构',
  '算法分析',
  '软件工程'
]);

// 搜索参数
const searchParams = ref({
  category: '',
  name: '',
  sortBy: 'publishTime'
})

// 题库列表
const bankList = ref([])
// 加载状态
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const totalPages = ref(1)
const jumpPageInput = ref('1')
const currentUserId = ref('')

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
    params.append('sortBy', searchParams.value.sortBy)
    params.append('page', String(page.value))
    params.append('size', String(size.value))
    if (currentUserId.value) params.append('currentUserId', currentUserId.value)

    const result = await request.get(`/api/question-set/public?${params.toString()}`)

    if (result.code === 0) {
      bankList.value = result.data?.list || []
      total.value = result.data?.total || 0
      totalPages.value = Math.max(1, Math.ceil(total.value / size.value))
      if (page.value > totalPages.value) {
        page.value = totalPages.value
      }
      jumpPageInput.value = String(page.value)
    } else {
      ElMessage.error(result.message || '查询失败')
      bankList.value = []
      total.value = 0
      totalPages.value = 1
      jumpPageInput.value = '1'
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
  searchParams.value.sortBy = 'publishTime'
  page.value = 1
  size.value = 10
  jumpPageInput.value = '1'
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
    const result = await request.post(`/api/question-set/public/import/${publicSetId}`)

    if (result.code === 0) {
      ElMessage.success('导入成功，已添加到我的题库')
      await getPublicQuestionSets()
    } else {
      ElMessage.error(result.message || '导入失败')
    }
  } catch (err) {
    console.error('导入题库失败：', err)
    ElMessage.error('网络错误，导入失败')
  }
}

const goDiscussion = (setId) => {
  router.push(`/home/online-bank/${setId}/discussion`)
}

const publisherDisplayName = (bank) => displayUserName(bank)

const canFollowPublisher = (bank) => {
  if (!bank.publisherId || !currentUserId.value) return false
  return String(bank.publisherId) !== String(currentUserId.value)
}

const toggleFollowPublisher = async (bank) => {
  if (!canFollowPublisher(bank)) return
  const pid = bank.publisherId
  const following = !!bank.viewerFollowsPublisher
  try {
    const url = `/api/user-center/follow?followerId=${currentUserId.value}&followeeId=${pid}`
    const result = following
      ? await request.delete(url)
      : await request.post(url)
    if (result.code === 0) {
      ElMessage.success(following ? '已取消关注' : '关注成功')
      await getPublicQuestionSets()
    } else {
      ElMessage.error(result.message || '操作失败')
    }
  } catch {
    ElMessage.error('网络错误')
  }
}

const voteBank = async (bank, voteType) => {
  if (!currentUserId.value) {
    ElMessage.error('请先登录')
    return
  }
  try {
    const result = await request.post(`/api/question-set/public/${bank.id}/vote?voteType=${voteType}`)
    if (result.code === 0) {
      await getPublicQuestionSets()
    } else {
      ElMessage.error(result.message || '投票失败')
    }
  } catch (err) {
    ElMessage.error('网络错误，投票失败')
  }
}

const changePage = async (targetPage) => {
  if (targetPage < 1 || targetPage > totalPages.value) return
  page.value = targetPage
  await getPublicQuestionSets()
}

const onPageSizeChange = async () => {
  const parsed = Number(size.value)
  if (![5, 10, 20, 50].includes(parsed)) {
    size.value = 10
  } else {
    size.value = parsed
  }
  page.value = 1
  await getPublicQuestionSets()
}

const jumpToPage = async () => {
  const target = Number(jumpPageInput.value)
  if (!Number.isFinite(target)) {
    ElMessage.warning('请输入有效页码')
    jumpPageInput.value = String(page.value)
    return
  }
  const normalized = Math.min(Math.max(Math.floor(target), 1), totalPages.value)
  if (normalized === page.value) {
    jumpPageInput.value = String(page.value)
    return
  }
  await changePage(normalized)
}

const onSortChange = async () => {
  page.value = 1
  await getPublicQuestionSets()
}

// 页面挂载时初始化查询
onMounted(() => {
  currentUserId.value = getCurrentUserId()
  getPublicQuestionSets()
})
</script>

<style scoped>
.online-bank-container {
  width: 100%;
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

.publisher-line {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.publisher-name {
  color: #409eff;
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
}

.publisher-name:hover {
  text-decoration: underline;
}

.follow-pub-btn {
  padding: 4px 12px;
  font-size: 13px;
  border: 1px solid #42b983;
  background: #fff;
  color: #42b983;
  border-radius: 4px;
  cursor: pointer;
}

.follow-pub-btn:hover {
  background: #f0fff6;
}

.bank-body {
  flex: 1;
}

.bank-body .label {
  color: #666;
  font-weight: 500;
}

.tag-list {
  margin-top: 8px;
  line-height: 1.8;
}

.tag-item {
  display: inline-block;
  margin-right: 6px;
  margin-bottom: 6px;
  padding: 2px 8px;
  border-radius: 12px;
  background: #eef6ff;
  color: #409eff;
  font-size: 12px;
}

.tag-empty {
  color: #999;
  font-size: 12px;
}

.bank-footer {
  text-align: right;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.vote-btn {
  padding: 6px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  font-size: 13px;
}

.vote-btn.active {
  border-color: #67c23a;
  color: #67c23a;
}

.vote-btn.dislike.active {
  border-color: #f56c6c;
  color: #f56c6c;
}

.comment-btn {
  padding: 6px 15px;
  background: #67c23a;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.comment-btn:hover {
  background: #85ce61;
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.pagination button {
  padding: 4px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}

.pagination select {
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
}

.jump-input {
  width: 70px;
  padding: 4px 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
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