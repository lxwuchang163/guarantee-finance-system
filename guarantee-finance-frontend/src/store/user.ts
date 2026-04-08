import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'
import type { LoginData, UserInfo } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  async function userLogin(loginData: LoginData) {
    const res = await login(loginData) as any
    token.value = res.data.token
    localStorage.setItem('token', res.data.token)
    return res.data
  }

  async function fetchUserInfo() {
    const res = await getUserInfo() as any
    userInfo.value = res.data
    return res.data
  }

  async function userLogout() {
    try {
      await logout()
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      router.push('/login')
    }
  }

  function resetState() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    userLogin,
    fetchUserInfo,
    userLogout,
    resetState
  }
})
