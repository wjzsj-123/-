<template>
  <div class="paper-container">
    <div class="paper-header">
      <h3>试卷管理页面</h3>
      <button class="generate-btn" @click="goToGeneratePaper">生成试卷</button>
    </div>
    <div class="paper-content">
      <p>这里是试卷生成/编辑/发布功能区域</p>
      <!-- 后续可补充具体业务逻辑 -->
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router';

// 路由实例
const router = useRouter();

// 跳转到生成试卷页面
const goToGeneratePaper = () => {
  router.push('/home/paper/generate'); // 这里需要与路由配置的path一致
};

console.log('试卷管理组件已加载');
</script>

<style scoped>
.paper-container {
  width: 100%;
  height: 100%;
  padding: 2rem;
  background-color: #f8f9fa;
}

.paper-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.generate-btn {
  padding: 0.6rem 1.2rem;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.3s;
}

.generate-btn:hover {
  background-color: #359e69;
}

.paper-content {
  margin-top: 1rem;
  padding: 1rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

h3 {
  color: #333;
  margin-bottom: 0.5rem;
}

p {
  color: #666;
  line-height: 1.6;
}
</style>