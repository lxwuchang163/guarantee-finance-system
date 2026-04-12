import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'
import type { LoginData, UserInfo } from '@/api/auth'
import request from '@/utils/request'
import router from '@/router'

export interface MenuItem {
  id: number
  menuName: string
  parentId: number
  path: string
  component: string
  icon: string
  permission: string
  menuType: number
  sortOrder: number
  children?: MenuItem[]
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const menuList = ref<MenuItem[]>([])
  const permissions = ref<string[]>([])

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

  async function loadMenuAndPermissions() {
    try {
      const [menuRes, permRes] = await Promise.all([
        request.get('/system/menu/user-menu'),
        request.get('/system/menu/user-permissions')
      ])
      menuList.value = (menuRes as any).data || []
      permissions.value = ((permRes as any).data || []) as string[]
    } catch (e) {
      console.error('加载菜单权限失败:', e)
    }
  }

  async function userLogout() {
    try {
      await logout()
    } finally {
      token.value = ''
      userInfo.value = null
      menuList.value = []
      permissions.value = []
      localStorage.removeItem('token')
      router.push('/login')
    }
  }

  function resetState() {
    token.value = ''
    userInfo.value = null
    menuList.value = []
    permissions.value = []
    localStorage.removeItem('token')
  }

  function hasPermission(perm: string) {
    return permissions.value.includes(perm)
  }

  return {
    token,
    userInfo,
    menuList,
    permissions,
    isLoggedIn,
    userLogin,
    fetchUserInfo,
    loadMenuAndPermissions,
    userLogout,
    resetState,
    hasPermission
  }
})
