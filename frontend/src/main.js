import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

import router from './router'
// 关键：完整引入 Element Plus 及样式
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 全局 fetch 拦截：自动附加 JWT，401 时跳转登录
const originalFetch = window.fetch.bind(window)
window.fetch = async (url, options = {}) => {
  const requestUrl = String(url)
  const headers = new Headers(options.headers || {})
  const token = localStorage.getItem('accessToken')
  if (token && requestUrl.includes('/api/') && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`)
  }
  const response = await originalFetch(requestUrl, { ...options, headers })
  if (response.status === 401 && requestUrl.includes('/api/')) {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('userInfo')
    if (!window.location.hash.includes('/login')) {
      window.location.hash = '#/login'
    }
  }
  return response
}

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.mount('#app')