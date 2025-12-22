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
  height: 100vh;
  overflow: hidden;
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: "Microsoft YaHei", sans-serif;
}
</style>