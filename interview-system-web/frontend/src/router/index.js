import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/views/LayoutView.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue')
      },
      {
        path: '/questions',
        name: 'Questions',
        component: () => import('@/views/QuestionsView.vue')
      },
      {
        path: '/interviews',
        name: 'Interviews',
        component: () => import('@/views/InterviewsView.vue')
      },
      {
        path: '/users',
        name: 'Users',
        component: () => import('@/views/UsersView.vue'),
        meta: { adminOnly: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (!to.meta.public && !authStore.isAuthenticated) {
    next('/login')
    return
  }
  
  if (to.meta.adminOnly && authStore.userRole !== 'ADMIN') {
    next('/dashboard')
    return
  }
  
  next()
})

export default router
