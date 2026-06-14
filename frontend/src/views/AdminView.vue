<template>
  <main class="page admin-page">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">环</span>
        <span>{{ sidebarTitle }}</span>
      </div>
      <el-menu :default-active="activeMenu" @select="handleMenuSelect">
        <template v-if="auth.isAdmin || auth.isRegulator">
          <el-menu-item index="dashboard">数据看板</el-menu-item>
          <el-menu-item index="records">检测记录</el-menu-item>
          <el-menu-item index="warnings">超标预警</el-menu-item>
        </template>
        <template v-if="auth.isAdmin">
          <el-menu-item index="vehicles">车辆信息</el-menu-item>
          <el-menu-item index="stations">检测站管理</el-menu-item>
        </template>
        <template v-if="auth.isStation">
          <el-menu-item index="entry">检测录入</el-menu-item>
          <el-menu-item index="records">检测记录</el-menu-item>
        </template>
        <template v-if="auth.isAdmin || auth.isRegulator || auth.isStation">
          <el-menu-item index="entry" v-if="auth.isAdmin || auth.isRegulator">检测录入</el-menu-item>
        </template>
      </el-menu>
    </aside>

    <section class="admin-main">
      <header class="admin-header">
        <div>
          <h1>{{ pageTitle }}</h1>
          <p class="muted">{{ auth.user?.displayName }} · {{ auth.user?.role }}</p>
        </div>
        <div class="header-actions">
          <el-radio-group v-if="showTimeRange" v-model="timeRange" size="default" @change="onTimeRangeChange">
            <el-radio-button :label="7">近 7 日</el-radio-button>
            <el-radio-button :label="14">近 14 日</el-radio-button>
            <el-radio-button :label="30">近 30 日</el-radio-button>
          </el-radio-group>
          <el-button style="margin-left: 12px" @click="goHome" v-if="!auth.isUser">返回首页</el-button>
          <el-button style="margin-left: 12px" @click="logout">退出</el-button>
        </div>
      </header>

      <template v-if="activeMenu === 'dashboard'">
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
      </template>

      <template v-if="activeMenu === 'records' || activeMenu === 'dashboard'">
        <section class="section admin-section" v-if="activeMenu === 'dashboard' || activeMenu === 'records'">
          <div class="card">
            <div class="section-header">
              <h2>{{ auth.isStation ? '本站检测记录' : '检测记录审核' }}</h2>
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
              <el-table-column prop="stationName" label="检测站" min-width="160" v-if="!auth.isStation" />
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
              <el-table-column prop="auditor" label="审核人" width="100" v-if="!auth.isStation" />
              <el-table-column label="操作" width="240" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-if="(auth.isAdmin || auth.isRegulator) && row.reportStatus === '待审核'"
                    type="success"
                    size="small"
                    @click="openAuditDialog(row, 'PASS')"
                  >
                    通过
                  </el-button>
                  <el-button
                    v-if="(auth.isAdmin || auth.isRegulator) && row.reportStatus === '待审核'"
                    type="danger"
                    size="small"
                    @click="openAuditDialog(row, 'REJECT')"
                  >
                    退回
                  </el-button>
                  <el-button
                    size="small"
                    @click="viewAuditHistory(row)"
                  >
                    {{ row.reportStatus === '待审核' ? '详情' : '审核记录' }}
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>
      </template>

      <template v-if="activeMenu === 'vehicles'">
        <section class="section admin-section">
          <div class="card">
            <div class="section-header">
              <h2>车辆信息管理</h2>
              <el-input
                v-model="vehicleSearchKeyword"
                placeholder="搜索车牌号或 VIN"
                clearable
                style="width: 280px"
                @keyup.enter="searchVehicles"
              >
                <template #append>
                  <el-button :icon="Search" @click="searchVehicles" />
                </template>
              </el-input>
            </div>
            <el-table :data="vehicleList" border stripe>
              <el-table-column prop="plateNumber" label="车牌号" width="120" />
              <el-table-column prop="vin" label="VIN" min-width="200" />
              <el-table-column prop="vehicleType" label="车辆类型" width="120" />
              <el-table-column prop="fuelType" label="燃料类型" width="100" />
              <el-table-column prop="emissionStandard" label="排放标准" width="100" />
              <el-table-column prop="owner" label="车主" width="120" />
              <el-table-column prop="environmentalStatus" label="环保状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.environmentalStatus === '合格' ? 'success' : 'warning'">
                    {{ row.environmentalStatus }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="viewVehicleDetail(row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>
      </template>

      <template v-if="activeMenu === 'stations'">
        <section class="section admin-section">
          <div class="card">
            <div class="section-header">
              <h2>检测站管理</h2>
              <el-button type="primary" :icon="Refresh" @click="loadStationData">刷新状态</el-button>
            </div>
            <el-table :data="stationStatusList" border stripe>
              <el-table-column prop="stationName" label="检测站名称" min-width="180" />
              <el-table-column prop="district" label="辖区" width="100" />
              <el-table-column prop="todayInspectionCount" label="今日检测量" width="120" />
              <el-table-column prop="passedCount" label="合格数" width="100">
                <template #default="{ row }">
                  <span style="color: #67c23a">{{ row.passedCount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="failedCount" label="不合格数" width="100">
                <template #default="{ row }">
                  <span style="color: #f56c6c">{{ row.failedCount }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="passRate" label="合格率" width="100">
                <template #default="{ row }">
                  {{ row.passRate }}%
                </template>
              </el-table-column>
              <el-table-column prop="runningStatus" label="运行状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStationStatusType(row.runningStatus)">
                    {{ row.runningStatus }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="lastInspectionTime" label="最后检测时间" min-width="160" />
            </el-table>
          </div>
        </section>
      </template>

      <template v-if="activeMenu === 'warnings'">
        <section class="section admin-section">
          <div class="card">
            <div class="section-header">
              <h2>超标预警管理</h2>
              <el-radio-group v-model="warningFilter" size="default">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="pending">待处置</el-radio-button>
                <el-radio-button label="processing">处置中</el-radio-button>
                <el-radio-button label="done">已处置</el-radio-button>
              </el-radio-group>
            </div>
            <el-table :data="filteredWarnings" border stripe>
              <el-table-column prop="plateNumber" label="车牌号" width="120" />
              <el-table-column prop="pollutant" label="污染物" width="100" />
              <el-table-column prop="level" label="等级" width="80">
                <template #default="{ row }">
                  <el-tag :type="getLevelType(row.level)" effect="light">{{ row.level }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="描述" min-width="200" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getWarningStatusType(row.status)" effect="plain">{{ row.status }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createdAt" label="预警时间" min-width="160" />
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="goToWarningHandle(row)">处置</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>
      </template>
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
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Refresh, Search } from '@element-plus/icons-vue'
import {
  auditInspection,
  fetchAuditRecords,
  fetchDashboard,
  fetchInspections,
  fetchWarnings,
  fetchStationStatuses,
  fetchStations,
  searchVehicle,
  type AuditRecord,
  type InspectionRecord,
  type WarningRecord,
  type StationStatus,
  type Vehicle
} from '@/api/platform'
import { useAuthStore, UserRole } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const dashboard = ref<any>({})
const allRecords = ref<InspectionRecord[]>([])
const records = ref<InspectionRecord[]>([])
const warnings = ref<WarningRecord[]>([])
const stationStatusList = ref<StationStatus[]>([])
const vehicleList = ref<Vehicle[]>([])
const vehicleSearchKeyword = ref('')
const trendChart = ref<HTMLElement | null>(null)
const standardChart = ref<HTMLElement | null>(null)
const statusFilter = ref('all')
const warningFilter = ref('all')
const timeRange = ref<number>(7)
let trendChartInstance: echarts.ECharts | null = null
let standardChartInstance: echarts.ECharts | null = null

type Metric = {
  label: string
  value: string | number
  highlight?: boolean
}

const auditDialogVisible = ref(false)
const historyDialogVisible = ref(false)
const currentRecord = ref<InspectionRecord | null>(null)
const auditAction = ref<'PASS' | 'REJECT'>('PASS')
const auditOpinion = ref('')
const auditLoading = ref(false)
const auditHistory = ref<AuditRecord[]>([])

const getDefaultMenu = (): string => {
  if (auth.isStation) return 'records'
  if (auth.isAdmin || auth.isRegulator) return 'dashboard'
  return 'dashboard'
}

const activeMenu = ref(getDefaultMenu())

watch(() => auth.user?.role, () => {
  activeMenu.value = getDefaultMenu()
})

const sidebarTitle = computed(() => {
  if (auth.isStation) return '检测站工作台'
  if (auth.isRegulator) return '监管平台'
  return '监管后台'
})

const pageTitle = computed(() => {
  switch (activeMenu.value) {
    case 'dashboard': return '数据看板'
    case 'records': return auth.isStation ? '检测记录' : '检测记录审核'
    case 'vehicles': return '车辆信息'
    case 'stations': return '检测站管理'
    case 'warnings': return '超标预警'
    case 'entry': return '检测录入'
    default: return '数据看板'
  }
})

const showTimeRange = computed(() => activeMenu.value === 'dashboard')

const metrics = computed<Metric[]>(() => {
  const periodLabel = `近${timeRange.value}日`
  const baseMetrics: Metric[] = [
    { label: `${periodLabel}检测车辆数`, value: dashboard.value.totalInspections ?? 0 },
    { label: `${periodLabel}合格车辆数`, value: dashboard.value.passedVehicles ?? 0 },
    { label: `${periodLabel}不合格车辆数`, value: dashboard.value.failedVehicles ?? 0 }
  ]
  
  if (auth.isAdmin || auth.isRegulator) {
    baseMetrics.push({ label: '待审核数量', value: dashboard.value.pendingAudit ?? 0, highlight: true })
  }
  
  baseMetrics.push({ label: `${periodLabel}超标率`, value: `${dashboard.value.exceedRate ?? 0}%` })
  
  return baseMetrics
})

const filteredRecords = computed(() => {
  let result = records.value
  if (auth.isStation) {
    result = result.filter(r => r.stationName.includes('朝阳'))
  }
  if (statusFilter.value === 'all') return result
  if (statusFilter.value === 'pending') return result.filter(r => r.reportStatus === '待审核')
  if (statusFilter.value === 'audited') return result.filter(r => r.reportStatus === '已审核')
  if (statusFilter.value === 'rejected') return result.filter(r => r.reportStatus === '已退回')
  return result
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

const getStationStatusType = (status: string) => {
  if (status === '运行中') return 'success'
  if (status === '空闲') return 'info'
  if (status === '停运') return 'danger'
  return 'info'
}

const goToWarningHandle = (row: WarningRecord) => {
  router.push({ name: 'warning-handle', params: { id: row.id } })
}

const handleMenuSelect = (index: string) => {
  activeMenu.value = index
  if (index === 'entry') {
    router.push('/inspection-entry')
    return
  }
  if (index === 'vehicles') {
    loadVehicleData()
  }
  if (index === 'stations') {
    loadStationData()
  }
}

const goHome = () => {
  router.push('/')
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

const loadStationData = async () => {
  try {
    const { data } = await fetchStationStatuses()
    stationStatusList.value = data
  } catch (e) {
    ElMessage.error('加载检测站数据失败')
  }
}

const loadVehicleData = async () => {
  try {
    const stations = await fetchStations()
    vehicleList.value = [
      {
        plateNumber: '京A12345',
        vin: 'LHGCM82633A004352',
        vehicleType: '小型轿车',
        fuelType: '汽油',
        emissionStandard: '国六',
        owner: '张先生',
        registerDate: '2021-06-18',
        environmentalStatus: '合格'
      },
      {
        plateNumber: '京B67890',
        vin: 'LSVNV2187N2039456',
        vehicleType: '轻型货车',
        fuelType: '柴油',
        emissionStandard: '国五',
        owner: '北京绿运物流',
        registerDate: '2019-03-12',
        environmentalStatus: '待复检'
      },
      {
        plateNumber: '京C24680',
        vin: 'LFPH4ACC9N1A20458',
        vehicleType: '小型客车',
        fuelType: '混合动力',
        emissionStandard: '国六',
        owner: '李女士',
        registerDate: '2022-10-09',
        environmentalStatus: '合格'
      }
    ]
  } catch (e) {
    ElMessage.error('加载车辆数据失败')
  }
}

const searchVehicles = async () => {
  if (!vehicleSearchKeyword.value.trim()) {
    loadVehicleData()
    return
  }
  try {
    const { data } = await searchVehicle(vehicleSearchKeyword.value)
    if (data.success && data.data) {
      vehicleList.value = [data.data]
    } else {
      vehicleList.value = []
      ElMessage.warning('未找到匹配的车辆')
    }
  } catch (e) {
    ElMessage.error('搜索失败')
  }
}

const viewVehicleDetail = (row: Vehicle) => {
  router.push({ name: 'home', query: { plate: row.plateNumber } })
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
  if (activeMenu.value === 'stations') {
    await loadStationData()
  }
  if (activeMenu.value === 'vehicles') {
    await loadVehicleData()
  }
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
