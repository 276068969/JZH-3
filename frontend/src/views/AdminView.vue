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
          <div class="metric-value" :class="metric.highlight ? 'highlight' : ''">{{ metric.value }}</div>
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
  type InspectionRecord
} from '@/api/platform'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const dashboard = ref<any>({})
const allRecords = ref<InspectionRecord[]>([])
const records = ref<InspectionRecord[]>([])
const warnings = ref([])
const trendChart = ref<HTMLElement | null>(null)
const standardChart = ref<HTMLElement | null>(null)
const statusFilter = ref('all')

const auditDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const currentRecord = ref<InspectionRecord | null>(null)
const auditAction = ref<'PASS' | 'REJECT'>('PASS')
const auditOpinion = ref('')
const auditLoading = ref(false)
const auditHistory = ref<AuditRecord[]>([])

const metrics = computed(() => [
  { label: '今日检测车辆数', value: dashboard.value.todayInspections ?? 0 },
  { label: '合格车辆数', value: dashboard.value.passedVehicles ?? 0 },
  { label: '不合格车辆数', value: dashboard.value.failedVehicles ?? 0 },
  { label: '待审核数量', value: dashboard.value.pendingAudit ?? 0, highlight: true },
  { label: '超标率', value: `${dashboard.value.exceedRate ?? 0}%` }
])

const filteredRecords = computed(() => {
  if (statusFilter.value === 'all') return records.value
  if (statusFilter.value === 'pending') return records.value.filter(r => r.reportStatus === '待审核')
  if (statusFilter.value === 'audited') return records.value.filter(r => r.reportStatus === '已审核')
  if (statusFilter.value === 'rejected') return records.value.filter(r => r.reportStatus === '已退回')
  return records.value
})

const getStatusType = (status: string) => {
  if (status === '已审核') return 'primary'
  if (status === '待审核') return 'warning'
  if (status === '已退回') return 'danger'
  return 'info'
}

const logout = async () => {
  auth.signOut()
  await router.push('/login')
}

const loadData = async () => {
  const [dashboardResp, recordResp, warningResp] = await Promise.all([
    fetchDashboard(),
    fetchInspections(),
    fetchWarnings()
  ])
  dashboard.value = dashboardResp.data
  allRecords.value = recordResp.data
  records.value = recordResp.data
  warnings.value = warningResp.data
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
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
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
