import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import AdminView from '@/views/AdminView.vue'
import InspectionEntryView from '@/views/InspectionEntryView.vue'
import InspectionReportDetail from '@/views/InspectionReportDetail.vue'
import WarningHandleView from '@/views/WarningHandleView.vue'
import AnnouncementListView from '@/views/AnnouncementListView.vue'
import AnnouncementDetailView from '@/views/AnnouncementDetailView.vue'
import VehicleCenterView from '@/views/VehicleCenterView.vue'
import { useAuthStore, UserRole } from '@/stores/auth'

declare module 'vue-router' {
  interface RouteMeta {
    requiresAuth?: boolean
    roles?: string[]
  }
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/login', name: 'login', component: LoginView },
    { 
      path: '/admin', 
      name: 'admin', 
      component: AdminView,
      meta: { 
        requiresAuth: true,
        roles: [UserRole.ADMIN, UserRole.REGULATOR, UserRole.STATION]
      }
    },
    { 
      path: '/inspection-entry', 
      name: 'inspection-entry', 
      component: InspectionEntryView,
      meta: { 
        requiresAuth: true,
        roles: [UserRole.ADMIN, UserRole.REGULATOR, UserRole.STATION]
      }
    },
    { path: '/report/:inspectionNo?', name: 'report-detail', component: InspectionReportDetail, meta: { requiresAuth: true } },
    { path: '/announcements', name: 'announcement-list', component: AnnouncementListView },
    { path: '/announcements/:id', name: 'announcement-detail', component: AnnouncementDetailView },
    { 
      path: '/warning/:id', 
      name: 'warning-handle', 
      component: WarningHandleView,
      meta: { 
        requiresAuth: true,
        roles: [UserRole.ADMIN, UserRole.REGULATOR]
      }
    },
    { 
      path: '/vehicle-center', 
      name: 'vehicle-center', 
      component: VehicleCenterView,
      meta: { 
        requiresAuth: true
      }
    }
  ]
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const userStr = localStorage.getItem('user')
  let userRole = ''
  
  if (userStr) {
    try {
      const user = JSON.parse(userStr)
      userRole = user.role || ''
    } catch {
      userRole = ''
    }
  }

  if (to.meta.requiresAuth && !token) {
    return '/login'
  }

  if (to.meta.roles && to.meta.roles.length > 0 && token) {
    if (!to.meta.roles.includes(userRole)) {
      if (userRole === UserRole.USER) {
        return '/'
      }
      const auth = useAuthStore()
      return auth.homeRoute || '/'
    }
  }

  if (to.path === '/login' && token) {
    const auth = useAuthStore()
    return auth.homeRoute || '/'
  }

  return true
})

export default router
