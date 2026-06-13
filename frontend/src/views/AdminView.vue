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
        <div class="header-actions">
          <el-radio-group v-model="timeRange" size="default" @change="onTimeRangeChange">
            <el-radio-button :label="7">近 7 日</el-radio-button>
            <el-radio-button :label="14">近 14 日</el-radio-button>
            <el-radio-button :label="30">近 30 日</el-radio-button>
          </el-radio-group>
          <el-button style="margin-left: 12px" @click="logout">退出</el-button>
        </div>
      </header>

      <div class="grid grid-4">
        <div class="card" v-for="metric in metrics" :key="metric.label">
          <div class="muted">{{ metric.label }}</div>
          <div class="metric-value" :class="metric.highlight ? 'highlight' : ''">{{ metric.value }}</div>
        </div>
      </div>

      <section class="section admin-section">
        <div class="grid grid-3">
          <div class="card chart-panel">
            <div class="section-header">
              <h2>近 {{ timeRange }} 日检测趋势</h2>
            </div>
            <div ref="trendChart" class="chart"></div>
          </div>
          <div class="card chart-panel">
            <h2>排放标准占比</h2>
            <div ref="standardChart" class="chart"></div>
          </div>
          <div class="card">
            <div class="section-header">
              <h2>超标车辆预警</h2>
              <el-radio-group v-model="warningFilter" size="small">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="pending">待处置</el-radio-button>
                <el-radio-button label="processing">处置中</el-radio-button>
                <el-radio-button label="done">已处置</el-radio-button>
              </el-radio-group>
            </div>
            <el-table :data="filteredWarnings" height="280">
              <el-table-column prop="plateNumber" label="车牌" width="105" />
              <el-table-column prop="pollutant" label="污染物" width="90" />
              <el-table-column prop="level" label="等级" width="80">
                <template #default="{ row }">
                  <el-tag :type="getLevelType(row.level)" size="small" effect="light">{{ row.level }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="getWarningStatusType(row.status)" size="small" effect="plain">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="goToWarningHandle(row)">处置</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </section>

      <section class="section admin-section">
        <div class="card">
          <div class="section-header">
            <h2>检测记录审核</h2>
            <div class="filter-bar">
              <el-radio-group v-model="statusFilter" @change="filterRecords">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="pending">待审核</el-radio-button>
                <el-radio-button label="audited">已审核</el-radio-button>
                <el-radio-button label="rejected">已退回</el-radio-button>
              </el-radio-group>
              <el-button type="primary" :icon="Refresh" @click="loadData">刷新</el-button>
            </div>
          </div>
          <el-table :data="filteredRecords" border stripe>
            <el-table-column prop="inspectionNo" label="检测编号" min-width="150" />
            <el-table-column prop="plateNumber" label="车牌号" width="120" />
            <el-table-column prop="stationName" label="检测站" min-width="160" />
            <el-table-column prop="inspectionTime" label="检测时间" min-width="160" />
            <el-table-column prop="result" label="结果" width="100">
              <template #default="{ row }">
                <el-tag :type="row.result === '合格' ? 'success' : 'danger'">{{ row.result }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reportStatus" label="报告状态" width="120">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.reportStatus)" effect="plain">
                  {{ row.reportStatus }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="auditor" label="审核人" width="100" />
            <el-table-column label="操作" width="240" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.reportStatus === '待审核'"
                  type="success"
                  size="small"
                  @click="openAuditDialog(row, 'PASS')"
                >
                  通过
                </el-button>
                <el-button
                  v-if="row.reportStatus === '待审核'"
                  type="danger"
                  size="small"
                  @click="openAuditDialog(row, 'REJECT')"
                >
                  退回
                </el-button>
                <el-button
                  v-if="row.reportStatus !== '待审核'"
                  size="small"
                  @click="viewAuditHistory(row)"
                >
                  审核记录
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </section>
    </section>

    <el-dialog
      v-model="auditDialogVisible"
      :title="auditAction === 'PASS' ? '审核通过' : '审核退回'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-descriptions :column="1" border size="small" class="mb16">
        <el-descriptions-item label="检测编号">{{ currentRecord?.inspectionNo }}</el-descriptions-item>
        <el-descriptions-item label="车牌号">{{ currentRecord?.plateNumber }}</el-descriptions-item>
        <el-descriptions-item label="检测站">{{ currentRecord?.stationName }}</el-descriptions-item>
        <el-descriptions-item label="检测结果">
          <el-tag :type="currentRecord?.result === '合格' ? 'success' : 'danger'">
            {{ currentRecord?.result }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <el-form label-width="80px">
        <el-form-item label="审核意见">
          <el-input
            v-model="auditOpinion"
            type="textarea"
            :rows="4"
            :placeholder="auditAction === 'PASS' ? '请输入审核通过意见（选填）' : '请输入退回原因（必填）'"
            :maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button
          :type="auditAction === 'PASS' ? 'success' : 'danger'"
          :loading="auditLoading"
          @click="submitAudit"
        >
          确认{{ auditAction === 'PASS' ? '通过' : '退回' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="historyDialogVisible" title="审核历史记录" width="600px">
      <el-timeline v-if="auditHistory.length > 0">
        <el-timeline-item
          v-for="item in auditHistory"
          :key="item.id"
          :timestamp="item.auditTime"
          :type="item.auditAction === '通过' ? 'success' : 'danger'"
        >
          <h4 style="margin: 0 0 8px">
            <el-tag :type="item.auditAction === '通过' ? 'success' : 'danger'" size="small">
              {{ item.auditAction }}
            </el-tag>
            <span style="margin-left: 8px">{{ item.auditor }}</span>
          </h4>
          <p v-if="item.auditOpinion" class="muted" style="margin: 0">
            {{ item.auditOpinion }}
          </p>
        </el-timeline-item>
      </el-timeline>
      <el-empty v-else description="暂无审核记录" />
    </el-dialog>
  </main>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import {
  auditInspection,
  fetchAuditRecords,
  fetchDashboard,
  fetchInspections,
  fetchWarnings,
  type AuditRecord,
  type InspectionRecord,
  type WarningRecord
} from '@/api/platform'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const dashboard = ref<any>({})
const allRecords = ref<InspectionRecord[]>([])
const records = ref<InspectionRecord[]>([])
const warnings = ref<WarningRecord[]>([])
const trendChart = ref<HTMLElement | null>(null)
const standardChart = ref<HTMLElement | null>(null)
const statusFilter = ref('all')
const warningFilter = ref('all')
const timeRange = ref<number>(7)
let trendChartInstance: echarts.ECharts | null = null
let standardChartInstance: echarts.ECharts | null = null

const auditDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const currentRecord = ref<InspectionRecord | null>(null)
const auditAction = ref<'PASS' | 'REJECT'>('PASS')
const auditOpinion = ref('')
const auditLoading = ref(false)
const auditHistory = ref<AuditRecord[]>([])

const metrics = computed(() => {
  const periodLabel = `近${timeRange.value}日`
  return [
    { label: `${periodLabel}检测车辆数`, value: dashboard.value.totalInspections ?? 0 },
    { label: `${periodLabel}合格车辆数`, value: dashboard.value.passedVehicles ?? 0 },
    { label: `${periodLabel}不合格车辆数`, value: dashboard.value.failedVehicles ?? 0 },
    { label: '待审核数量', value: dashboard.value.pendingAudit ?? 0, highlight: true },
    { label: `${periodLabel}超标率`, value: `${dashboard.value.exceedRate ?? 0}%` }
  ]
})

const filteredRecords = computed(() => {
  if (statusFilter.value === 'all') return records.value
  if (statusFilter.value === 'pending') return records.value.filter(r => r.reportStatus === '待审核')
  if (statusFilter.value === 'audited') return records.value.filter(r => r.reportStatus === '已审核')
  if (statusFilter.value === 'rejected') return records.value.filter(r => r.reportStatus === '已退回')
  return records.value
})

const filteredWarnings = computed(() => {
  if (warningFilter.value === 'all') return warnings.value
  if (warningFilter.value === 'pending') return warnings.value.filter(w => w.status === '待处置')
  if (warningFilter.value === 'processing') return warnings.value.filter(w => w.status === '处置中')
  if (warningFilter.value === 'done') return warnings.value.filter(w => w.status === '已处置' || w.status === '已复检')
  return warnings.value
})

const getStatusType = (status: string) => {
  if (status === '已审核') return 'primary'
  if (status === '待审核') return 'warning'
  if (status === '已退回') return 'danger'
  return 'info'
}

const getLevelType = (level: string) => {
  if (level === '高') return 'danger'
  if (level === '中') return 'warning'
  return 'info'
}

const getWarningStatusType = (status: string) => {
  if (status === '待处置') return 'danger'
  if (status === '处置中') return 'warning'
  if (status === '已处置') return 'success'
  if (status === '已复检') return 'primary'
  return 'info'
}

const goToWarningHandle = (row: WarningRecord) => {
  router.push({ name: 'warning-handle', params: { id: row.id } })
}

const logout = async () => {
  auth.signOut()
  await router.push('/login')
}

const loadData = async () => {
  const [dashboardResp, recordResp, warningResp] = await Promise.all([
    fetchDashboard(timeRange.value),
    fetchInspections(),
    fetchWarnings()
  ])
  dashboard.value = dashboardResp.data
  allRecords.value = recordResp.data
  records.value = recordResp.data
  warnings.value = warningResp.data
  await renderCharts()
}

const onTimeRangeChange = async () => {
  const dashboardResp = await fetchDashboard(timeRange.value)
  dashboard.value = dashboardResp.data
  await renderCharts()
}

const filterRecords = () => {
  if (statusFilter.value === 'all') {
    records.value = [...allRecords.value]
  } else if (statusFilter.value === 'pending') {
    records.value = allRecords.value.filter(r => r.reportStatus === '待审核')
  } else if (statusFilter.value === 'audited') {
    records.value = allRecords.value.filter(r => r.reportStatus === '已审核')
  } else if (statusFilter.value === 'rejected') {
    records.value = allRecords.value.filter(r => r.reportStatus === '已退回')
  }
}

const renderCharts = async () => {
  await nextTick()
  if (trendChart.value) {
    if (trendChartInstance) {
      trendChartInstance.dispose()
    }
    trendChartInstance = echarts.init(trendChart.value)
    trendChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 30, bottom: 40 },
      xAxis: {
        type: 'category',
        data: dashboard.value.trend?.map((item: any) => item.date) || [],
        axisLabel: {
          rotate: timeRange.value > 14 ? 45 : 0,
          fontSize: 11
        }
      },
      yAxis: { type: 'value' },
      series: [{
        type: 'line',
        smooth: true,
        data: dashboard.value.trend?.map((item: any) => item.count) || [],
        areaStyle: { opacity: 0.15 },
        lineStyle: { width: 2 },
        itemStyle: { color: '#409EFF' }
      }]
    })
  }
  if (standardChart.value) {
    if (standardChartInstance) {
      standardChartInstance.dispose()
    }
    standardChartInstance = echarts.init(standardChart.value)
    standardChartInstance.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie',
        radius: ['45%', '72%'],
        data: dashboard.value.emissionStandards || [],
        label: { formatter: '{b}: {d}%' }
      }]
    })
  }
}

const openAuditDialog = (row: InspectionRecord, action: 'PASS' | 'REJECT') => {
  currentRecord.value = row
  auditAction.value = action
  auditOpinion.value = ''
  auditDialogVisible.value = true
}

const submitAudit = async () => {
  if (auditAction.value === 'REJECT' && !auditOpinion.value.trim()) {
    ElMessage.warning('请输入退回原因')
    return
  }

  if (!currentRecord.value) return

  auditLoading.value = true
  try {
    const { data } = await auditInspection({
      inspectionNo: currentRecord.value.inspectionNo,
      action: auditAction.value,
      opinion: auditOpinion.value.trim()
    })

    if (data.success) {
      ElMessage.success(data.message)
      auditDialogVisible.value = false
      await loadData()
    } else {
      ElMessage.error(data.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '审核失败，请重试')
  } finally {
    auditLoading.value = false
  }
}

const viewAuditHistory = async (row: InspectionRecord) => {
  auditHistory.value = []
  historyDialogVisible.value = true
  try {
    const { data } = await fetchAuditRecords(row.inspectionNo)
    auditHistory.value = data
  } catch (e) {
    ElMessage.error('获取审核记录失败')
  }
}

onMounted(async () => {
  await loadData()
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

.header-actions {
  display: flex;
  align-items: center;
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

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h2 {
  margin: 0;
}

.filter-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}

.metric-value {
  font-size: 28px;
  font-weight: 600;
  margin-top: 8px;
}

.metric-value.highlight {
  color: #e6a23c;
}

.mb16 {
  margin-bottom: 16px;
}

.grid-4 {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}

@media (max-width: 1200px) {
  .grid-4 {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 960px) {
  .admin-page {
    grid-template-columns: 1fr;
  }

  .sidebar {
    min-height: auto;
  }

  .grid-4 {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
