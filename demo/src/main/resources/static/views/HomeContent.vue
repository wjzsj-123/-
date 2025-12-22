<template>
  <div class="home-content">
    <h2>欢迎使用题库系统</h2>
    <div class="stats-container">
      <div class="stat-card">
        <h3>题库数量</h3>
        <p>{{ questionSetCount }}</p>
      </div>
      <div class="stat-card">
        <h3>试卷数量</h3>
        <p>{{ paperCount }}</p>
      </div>
      <div class="stat-card">
        <h3>题目总数</h3>
        <p>{{ questionCount }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';

// 模拟统计数据
const questionSetCount = ref(0);
const paperCount = ref(0);
const questionCount = ref(0);

onMounted(() => {
  // 实际项目中这里会调用API获取统计数据
  // 这里使用模拟数据
  questionSetCount.value = 12;
  paperCount.value = 8;
  questionCount.value = 356;
});
</script>

<style scoped>
.home-content {
  max-width: 1200px;
  margin: 0 auto;
}

h2 {
  color: #333;
  margin-bottom: 30px;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  text-align: center;
}

.stat-card h3 {
  color: #666;
  margin-bottom: 15px;
  font-size: 1.1rem;
}

.stat-card p {
  font-size: 2rem;
  font-weight: bold;
  color: #42b983;
  margin: 0;
}
</style>