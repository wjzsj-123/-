// static/main.js
// 适配 CDN 全局变量：从 Vue 全局对象中获取 createApp
const { createApp } = Vue;
// 导入路由实例（路径适配 static 目录结构）
import router from './router/index.js';

// 直接创建根组件，无需 App.vue，内置路由出口
const app = createApp({
    template: '<router-view></router-view>' // 路由出口，所有页面渲染到这里
});

// 挂载路由（此时 router 是合法插件，无报错）
app.use(router);
// 挂载到 #app 容器
app.mount('#app');