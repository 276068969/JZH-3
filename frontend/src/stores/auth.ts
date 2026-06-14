import { defineStore } from 'pinia'
import { login } from '@/api/platform'

export enum UserRole {
  ADMIN = '平台管理员',
  REGULATOR = '监管人员',
  STATION = '检测站工作人员',
  USER = '普通用户'
}

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
  getters: {
    isAdmin: (state) => state.user?.role === UserRole.ADMIN,
    isRegulator: (state) => state.user?.role === UserRole.REGULATOR,
    isStation: (state) => state.user?.role === UserRole.STATION,
    isUser: (state) => state.user?.role === UserRole.USER,
    isLoggedIn: (state) => !!state.token,
    role: (state) => state.user?.role || '',
    homeRoute: (state): string => {
      switch (state.user?.role) {
        case UserRole.ADMIN:
        case UserRole.REGULATOR:
        case UserRole.STATION:
          return '/admin'
        case UserRole.USER:
        default:
          return '/'
      }
    }
  },
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
