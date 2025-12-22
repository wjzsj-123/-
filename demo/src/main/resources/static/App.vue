<!-- src/App.vue（根组件，必须存在） -->
<template>
  <!-- 路由出口：所有路由组件（登录、首页等）都会渲染到这里 -->
  <router-view></router-view>
</template>

<script setup>
// 无需额外逻辑，空脚本即可，仅需提供路由渲染容器
</script>

<style scoped>
/* 全局基础样式（可选，用于统一页面样式） */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: "Helvetica Neue", "Microsoft YaHei", sans-serif;
  background-color: #f8f9fa;
}
</style>