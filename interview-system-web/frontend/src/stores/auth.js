import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const userRole = ref(localStorage.getItem('userRole') || '')
  const loading = ref(false)
  const error = ref('')

  // Getters
  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => userRole.value === 'ADMIN')

  // Actions
  async function login(usernameInput, password) {
    loading.value = true
    error.value = ''
    
    try {
      const res = await authApi.login(usernameInput, password)
      token.value = res.token
      username.value = res.username
      userRole.value = res.role
      
      localStorage.setItem('token', res.token)
      localStorage.setItem('username', res.username)
      localStorage.setItem('userRole', res.role)
      
      return true
    } catch (err) {
      error.value = err.response?.data || '登录失败'
      return false
    } finally {
      loading.value = false
    }
  }

  function logout() {
    token.value = ''
    username.value = ''
    userRole.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('userRole')
  }

  return {
    token,
    username,
    userRole,
    loading,
    error,
    isAuthenticated,
    isAdmin,
    login,
    logout
  }
})
