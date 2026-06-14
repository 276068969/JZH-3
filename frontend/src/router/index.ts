import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import AdminView from '@/views/AdminView.vue'
import InspectionEntryView from '@/views/InspectionEntryView.vue'
import InspectionReportDetail from '@/views/InspectionReportDetail.vue'
import WarningHandleView from '@/views/WarningHandleView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/admin', name: 'admin', component: AdminView },
    { path: '/inspection-entry', name: 'inspection-entry', component: InspectionEntryView },
    { path: '/report/:inspectionNo?', name: 'report-detail', component: InspectionReportDetail },
    { path: '/warning/:id', name: 'warning-handle', component: WarningHandleView }
  ]
})

router.beforeEach((to) => {
  if ((to.path.startsWith('/admin') || to.path.startsWith('/warning')) && !localStorage.getItem('token')) {
    return '/login'
  }
  return true
})

export default router
