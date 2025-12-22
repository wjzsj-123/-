// main.js
// 错误原因：Vue 已通过 CDN 暴露全局变量，无需再解构声明 createApp
// 移除：const { createApp } = Vue;

// 直接使用全局 Vue.createApp，挂载全局路由 appRouter
Vue.createApp({})
    .use(window.appRouter) // 使用 router/index.js 中挂载的全局路由
    .mount('#app');

// 移除所有 import 语句（因为是 CDN 全局引入，无需模块化导入）