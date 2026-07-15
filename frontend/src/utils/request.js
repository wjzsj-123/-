import axios from 'axios'
import router from '@/router'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  const url = config.url || ''
  if (token && url.includes('/api/') && !config.headers.Authorization) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const status = error.response?.status
    const url = error.config?.url || ''
    if (status === 401 && url.includes('/api/')) {
      useUserStore().clearLogin()
      if (!window.location.hash.includes('/login')) {
        router.push('/login')
      }
    }
    return Promise.reject(error)
  }
)

export default request
