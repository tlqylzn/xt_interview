import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userInfo = ref<any>(null)

  const isLoggedIn = computed(() => !!token.value)

  function setToken(newToken: string) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUserInfo(info: any) {
    userInfo.value = info
  }

  function logout() {
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('currentUser')
  }

  async function validateToken() {
    if (!token.value) return { valid: false }
    try {
      const res: any = await authApi.validate()
      return res
    } catch (e) {
      console.error('Token验证失败', e)
      return { valid: false }
    }
  }

  function loadFromStorage() {
    const stored = localStorage.getItem('currentUser')
    if (stored) {
      userInfo.value = JSON.parse(stored)
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    setToken,
    setUserInfo,
    logout,
    validateToken,
    loadFromStorage
  }
})
