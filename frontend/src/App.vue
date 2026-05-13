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
  overflow-x: hidden;
  overflow-y: auto;
  padding: 0;
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

/* 固定根字号，避免断点切换导致各页 rem 布局整体缩放、产生跳变感 */
html {
  font-size: 16px;
}

@media (max-width: 768px) {
  html {
    font-size: 15px;
  }
}
</style>