import { defineStore } from 'pinia'
import { fetchMe, login, register } from '../api/bbook'

const TOKEN_KEY = 'bbook_token'
const USER_KEY = 'bbook_user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    user: JSON.parse(localStorage.getItem(USER_KEY) || 'null'),
    loading: false
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token && state.user)
  },
  actions: {
    saveAuth(data) {
      this.token = data.token
      this.user = data.user
      localStorage.setItem(TOKEN_KEY, data.token)
      localStorage.setItem(USER_KEY, JSON.stringify(data.user))
    },
    clearAuth() {
      this.token = ''
      this.user = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
    },
    async doRegister(payload) {
      this.loading = true
      try {
        const res = await register(payload)
        this.saveAuth(res.data)
        return res
      } finally {
        this.loading = false
      }
    },
    async doLogin(payload) {
      this.loading = true
      try {
        const res = await login(payload)
        this.saveAuth(res.data)
        return res
      } finally {
        this.loading = false
      }
    },
    async refreshMe() {
      if (!this.token) return
      try {
        const res = await fetchMe()
        this.saveAuth(res.data)
      } catch (error) {
        this.clearAuth()
      }
    }
  }
})
