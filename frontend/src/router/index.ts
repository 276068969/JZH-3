import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import LoginView from '@/views/LoginView.vue'
import AdminView from '@/views/AdminView.vue'
import InspectionReportDetail from '@/views/InspectionReportDetail.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/admin', name: 'admin', component: AdminView },
    { path: '/report/:inspectionNo?', name: 'report-detail', component: InspectionReportDetail }
  ]
})

router.beforeEach((to) => {
  if (to.path.startsWith('/admin') && !localStorage.getItem('token')) {
    return '/login'
  }
  return true
})

export default router
