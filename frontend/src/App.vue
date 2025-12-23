<template>
  <div id="app-container">
    <router-view />
  </div>
</template>

<script setup>
console.log('App 根组件已加载');
</script>

<style scoped>
#app-container {
  width: 100%;
  min-height: 100vh;
  overflow: auto; /* 改为auto允许滚动 */
  padding: 0 20px; /* 增加左右内边距 */
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: "Microsoft YaHei", sans-serif;
  font-size: 16px; /* 基础字体大小调整 */
  background-color: #f9f9f9; /* 增加背景色区分内容区 */
}

/* 响应式基础设置 */
@media (max-width: 768px) {
  html {
    font-size: 14px;
  }
}

@media (min-width: 1200px) {
  html {
    font-size: 18px;
  }
}
</style>