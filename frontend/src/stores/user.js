import { defineStore } from 'pinia'

function readUserInfo() {
  try {
    const raw = localStorage.getItem('userInfo')
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('accessToken') || '',
    userInfo: readUserInfo()
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    userId: (state) => state.userInfo?.id ?? null,
    username: (state) => state.userInfo?.username ?? ''
  },
  actions: {
    setLogin(payload) {
      const token = payload?.token || ''
      const user = payload?.user || payload || null
      this.token = token
      this.userInfo = user
      if (token) {
        localStorage.setItem('accessToken', token)
      } else {
        localStorage.removeItem('accessToken')
      }
      if (user) {
        localStorage.setItem('userInfo', JSON.stringify(user))
      } else {
        localStorage.removeItem('userInfo')
      }
    },
    clearLogin() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('userInfo')
    }
  }
})
