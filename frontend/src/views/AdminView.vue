<template>
  <main class="page admin-page">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">环</span>
        <span>监管后台</span>
      </div>
      <el-menu default-active="dashboard">
        <el-menu-item index="dashboard">数据看板</el-menu-item>
        <el-menu-item index="vehicles">车辆信息</el-menu-item>
        <el-menu-item index="records">检测记录</el-menu-item>
        <el-menu-item index="warnings">超标预警</el-menu-item>
        <el-menu-item index="stations">检测站管理</el-menu-item>
      </el-menu>
    </aside>

    <section class="admin-main">
      <header class="admin-header">
        <div>
          <h1>数据看板</h1>
          <p class="muted">{{ auth.user?.displayName }} · {{ auth.user?.role }}</p>
        </div>
        <el-button @click="logout">退出</el-button>
      </header>

      <div class="grid grid-4">
        <div class="card" v-for="metric in metrics" :key="metric.label">
          <div class="muted">{{ metric.label }}</div>
          <div class="metric-value">{{ metric.value }}</div>
        </div>
      </div>

      <section class="section admin-section">
        <div class="grid grid-3">
          <div class="card chart-panel">
            <h2>近 7 日检测趋势</h2>
            <div ref="trendChart" class="chart"></div>
          </div>
          <div class="card chart-panel">
            <h2>排放标准占比</h2>
            <div ref="standardChart" class="chart"></div>
          </div>
          <div class="card">
            <h2>超标车辆预警</h2>
            <el-table :data="warnings" height="280">
              <el-table-column prop="plateNumber" label="车牌" width="105" />
              <el-table-column prop="pollutant" label="污染物" width="90" />
              <el-table-column prop="level" label="等级" />
            </el-table>
          </div>
        </div>
      </section>

      <section class="section admin-section">
        <div class="card">
          <h2>检测记录审核</h2>
          <el-table :data="records" border>
            <el-table-column prop="inspectionNo" label="检测编号" min-width="150" />
            <el-table-column prop="plateNumber" label="车牌号" width="120" />
            <el-table-column prop="stationName" label="检测站" min-width="160" />
            <el-table-column prop="inspectionTime" label="检测时间" min-width="160" />
            <el-table-column prop="result" label="结果" width="100">
              <template #default="{ row }">
                <el-tag :type="row.result === '合格' ? 'success' : 'danger'">{{ row.result }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reportStatus" label="报告状态" width="120" />
          </el-table>
        </div>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchDashboard, fetchInspections, fetchWarnings } from '@/api/platform'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const dashboard = ref<any>({})
const records = ref([])
const warnings = ref([])
const trendChart = ref<HTMLElement | null>(null)
const standardChart = ref<HTMLElement | null>(null)

const metrics = computed(() => [
  { label: '今日检测车辆数', value: dashboard.value.todayInspections ?? 0 },
  { label: '合格车辆数', value: dashboard.value.passedVehicles ?? 0 },
  { label: '不合格车辆数', value: dashboard.value.failedVehicles ?? 0 },
  { label: '超标率', value: `${dashboard.value.exceedRate ?? 0}%` }
])

const logout = async () => {
  auth.signOut()
  await router.push('/login')
}

const renderCharts = async () => {
  await nextTick()
  if (trendChart.value) {
    echarts.init(trendChart.value).setOption({
      tooltip: {},
      xAxis: { type: 'category', data: dashboard.value.trend?.map((item: any) => item.date) || [] },
      yAxis: { type: 'value' },
      series: [{ type: 'line', smooth: true, data: dashboard.value.trend?.map((item: any) => item.count) || [] }]
    })
  }
  if (standardChart.value) {
    echarts.init(standardChart.value).setOption({
      tooltip: { trigger: 'item' },
      series: [{ type: 'pie', radius: ['45%', '72%'], data: dashboard.value.emissionStandards || [] }]
    })
  }
}

onMounted(async () => {
  const [dashboardResp, recordResp, warningResp] = await Promise.all([
    fetchDashboard(),
    fetchInspections(),
    fetchWarnings()
  ])
  dashboard.value = dashboardResp.data
  records.value = recordResp.data
  warnings.value = warningResp.data
  await renderCharts()
})
</script>

<style scoped>
.admin-page {
  display: grid;
  grid-template-columns: 240px 1fr;
}

.sidebar {
  min-height: 100vh;
  background: #ffffff;
  border-right: 1px solid #dfe7f2;
  padding: 18px 12px;
}

.sidebar .brand {
  margin: 0 8px 18px;
}

.admin-main {
  padding: 24px;
  overflow: hidden;
}

.admin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.admin-header h1,
.admin-header p,
.chart-panel h2 {
  margin: 0;
}

.admin-section {
  padding-bottom: 0;
}

.chart {
  width: 100%;
  height: 280px;
}

@media (max-width: 960px) {
  .admin-page {
    grid-template-columns: 1fr;
  }

  .sidebar {
    min-height: auto;
  }
}
</style>
