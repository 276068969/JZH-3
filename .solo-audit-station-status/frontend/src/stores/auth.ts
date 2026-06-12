import { defineStore } from 'pinia'
import { login } from '@/api/platform'

interface UserInfo {
  username: string
  role: string
  displayName: string
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null') as UserInfo | null
  }),
  actions: {
    async signIn(username: string, password: string) {
      const { data } = await login(username, password)
      this.token = data.token
      this.user = data.user
      localStorage.setItem('token', this.token)
      localStorage.setItem('user', JSON.stringify(this.user))
    },
    signOut() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})
