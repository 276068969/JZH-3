<template>
  <main class="page">
    <header class="topbar">
      <div class="shell topbar-inner">
        <div class="brand">
          <span class="brand-mark">环</span>
          <span>机动车尾气监管平台</span>
        </div>
        <nav>
          <el-button text @click="router.push('/')">车辆查询</el-button>
          <el-button text @click="scrollToStations">检测站</el-button>
          <el-button type="primary" @click="router.push('/admin')">管理后台</el-button>
        </nav>
      </div>
    </header>

    <section class="hero">
      <div class="shell">
        <h1>车辆环保状态与尾气检测查询</h1>
        <p>面向车主、企业和检测站提供车辆环保信息查询、尾气检测结果查询、政策公告和检测站信息检索。</p>
      </div>
    </section>

    <section class="section">
      <div class="shell grid grid-3">
        <div class="card query-card">
          <h2>车辆查询</h2>
          <el-input v-model="keyword" size="large" placeholder="输入车牌号、VIN 或检测报告编号" clearable maxlength="30" show-word-limit @keyup.enter="queryVehicle" />
          <el-button type="primary" size="large" :loading="loading" @click="queryVehicle">查询</el-button>
          <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />
          <el-alert v-if="successMessage && !errorMessage" :title="successMessage" type="success" show-icon :closable="false" />
        </div>

        <div v-if="vehicle" class="card result-card">
          <h2>{{ vehicle.plateNumber }}</h2>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="VIN">{{ vehicle.vin }}</el-descriptions-item>
            <el-descriptions-item label="车辆类型">{{ vehicle.vehicleType }}</el-descriptions-item>
            <el-descriptions-item label="燃料类型">{{ vehicle.fuelType }}</el-descriptions-item>
            <el-descriptions-item label="排放标准">{{ vehicle.emissionStandard }}</el-descriptions-item>
            <el-descriptions-item label="环保状态">
              <el-tag :type="vehicle.environmentalStatus === '合格' ? 'success' : 'danger'">
                {{ vehicle.environmentalStatus }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="card">
          <h2>最新公告</h2>
          <el-timeline>
            <el-timeline-item v-for="item in announcements" :key="item.id" :timestamp="formatDate(item.publishTime)">
              {{ item.title }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </section>

    <section v-if="vehicle && inspections.length > 0" class="section">
      <div class="shell">
        <div class="card">
          <div class="section-header">
            <h2>尾气检测履历</h2>
            <el-tag
              v-if="inspections[0].result === '不合格'"
              type="danger"
              effect="dark"
              size="large"
            >
              ⚠ 最近一次检测不合格，建议尽快复检
            </el-tag>
            <el-tag
              v-else-if="vehicle.environmentalStatus === '待复检'"
              type="warning"
              effect="dark"
              size="large"
            >
              待复检
            </el-tag>
            <el-tag v-else type="success" effect="dark" size="large">
              ✓ 检测状态正常
            </el-tag>
          </div>
          <el-table :data="inspections" border stripe>
            <el-table-column prop="inspectionNo" label="检测编号" min-width="160" />
            <el-table-column prop="inspectionTime" label="检测时间" min-width="160" />
            <el-table-column prop="stationName" label="检测站" min-width="200" />
            <el-table-column prop="result" label="检测结果" width="120">
              <template #default="{ row }">
                <el-tag :type="row.result === '合格' ? 'success' : 'danger'" effect="light">
                  {{ row.result }}
                </el-tag>
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
            <el-table-column prop="auditTime" label="审核时间" min-width="160" />
            <el-table-column prop="auditOpinion" label="审核意见" min-width="180" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  link
                  @click="viewReportDetail(row)"
                >
                  查看详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </section>

    <section class="section" ref="stationSection">
      <div class="shell">
        <h2>检测站信息</h2>
        <div class="station-filter">
          <el-select v-model="stationDistrict" placeholder="选择辖区" clearable style="width: 160px">
            <el-option label="全部辖区" value="" />
            <el-option
              v-for="d in districtOptions"
              :key="d"
              :label="d"
              :value="d"
            />
          </el-select>
          <el-select v-model="stationStatus" placeholder="选择状态" clearable style="width: 140px">
            <el-option label="全部状态" value="" />
            <el-option label="正常" value="正常" />
            <el-option label="停运" value="停运" />
          </el-select>
          <el-button type="primary" @click="queryStations">查询</el-button>
        </div>
        <el-table :data="stations" border @row-click="handleStationClick" style="cursor: pointer">
          <el-table-column prop="name" label="检测站" min-width="160" />
          <el-table-column prop="district" label="辖区" width="120" />
          <el-table-column prop="address" label="地址" min-width="220" />
          <el-table-column prop="phone" label="电话" width="150" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === '正常' ? 'success' : 'danger'" effect="light">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link @click.stop="handleStationClick(row)">
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </section>

    <StationDetailDrawer v-model="drawerVisible" :station="currentStationStatus" />
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import StationDetailDrawer from '@/components/StationDetailDrawer.vue'
import {
  fetchAnnouncements,
  fetchInspections,
  fetchStations,
  fetchStationStatuses,
  searchVehicle,
  type Announcement,
  type ApiResponse,
  type InspectionRecord,
  type Station,
  type StationStatus,
  type Vehicle
} from '@/api/platform'

const router = useRouter()
const keyword = ref('京A12345')
const vehicle = ref<Vehicle | null>(null)
const inspections = ref<InspectionRecord[]>([])
const loading = ref(false)
const errorMessage = ref('')
const successMessage = ref('')
const stations = ref<Station[]>([])
const stationStatuses = ref<StationStatus[]>([])
const announcements = ref<Announcement[]>([])
const stationSection = ref<HTMLElement | null>(null)
const stationDistrict = ref('')
const stationStatus = ref('')
const districtOptions = ref<string[]>([])
const drawerVisible = ref(false)
const currentStationStatus = ref<StationStatus | null>(null)

const normalizeKeyword = (raw: string | null | undefined): string => {
  if (!raw) return ''
  return raw
    .trim()
    .replace(/\s+/g, '')
    .replace(/　/g, '')
    .replace(/\u00A0/g, '')
    .replace(/\u200B/g, '')
    .replace(/\uFEFF/g, '')
    .toUpperCase()
}

const getErrorMessageByCode = (code: string, fallback: string): string => {
  const errorMap: Record<string, string> = {
    EMPTY_KEYWORD: '请输入车牌号、VIN 或检测报告编号',
    KEYWORD_TOO_SHORT: '输入内容过短，请至少输入 2 个字符',
    KEYWORD_TOO_LONG: '输入内容过长，请控制在 30 个字符以内',
    VEHICLE_NOT_FOUND: '未找到匹配的车辆，请检查输入是否正确。支持车牌号、17位VIN码或检测报告编号查询',
    PLATE_NOT_FOUND: '未找到该车牌号对应的车辆，请核对车牌号是否正确',
    VIN_NOT_FOUND: '未找到该 VIN 对应的车辆，请核对车辆识别代号是否正确',
    INSPECTION_NOT_FOUND: '未找到该检测报告编号对应的记录，请核对报告编号是否正确',
    VEHICLE_NOT_FOUND_BY_INSPECTION: '检测报告编号存在，但未找到关联的车辆信息',
    INTERNAL_ERROR: '系统内部错误，请稍后重试'
  }
  return errorMap[code] || fallback
}

const queryVehicle = async () => {
  loading.value = true
  errorMessage.value = ''
  successMessage.value = ''
  inspections.value = []
  try {
    const normalizedKeyword = normalizeKeyword(keyword.value)
    if (!normalizedKeyword) {
      errorMessage.value = '请输入车牌号、VIN 或检测报告编号'
      vehicle.value = null
      return
    }

    if (normalizedKeyword.length < 2) {
      errorMessage.value = '输入内容过短，请至少输入 2 个字符'
      vehicle.value = null
      return
    }

    if (keyword.value && keyword.value.trim() !== normalizedKeyword) {
      keyword.value = normalizedKeyword
    }

    const { data } = await searchVehicle(normalizedKeyword)
    const resp = data as ApiResponse<Vehicle>

    if (resp.success && resp.data) {
      vehicle.value = resp.data
      if (resp.message) {
        successMessage.value = resp.message
      }
      try {
        const inspResp = await fetchInspections(resp.data.plateNumber)
        inspections.value = inspResp.data
      } catch {
        ElMessage.warning('检测履历加载失败')
      }
    } else {
      vehicle.value = null
      errorMessage.value = getErrorMessageByCode(resp.code, resp.message || '查询失败，请稍后重试')
    }
  } catch (err: any) {
    vehicle.value = null
    if (err?.response?.data) {
      const errData = err.response.data
      errorMessage.value = getErrorMessageByCode(errData.code, errData.message || '查询失败，请稍后重试')
    } else if (err?.message?.includes('timeout')) {
      errorMessage.value = '请求超时，请检查网络后重试'
    } else {
      errorMessage.value = '网络异常，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

const getStatusType = (status: string) => {
  if (status === '已审核') return 'primary'
  if (status === '待审核') return 'warning'
  if (status === '已退回') return 'danger'
  return 'info'
}

const scrollToStations = () => stationSection.value?.scrollIntoView({ behavior: 'smooth' })

const queryStations = async () => {
  try {
    const { data } = await fetchStations(
      stationDistrict.value || undefined,
      stationStatus.value || undefined
    )
    stations.value = data
  } catch {
    ElMessage.warning('检测站查询失败')
  }
}

const loadStations = async () => {
  try {
    const { data } = await fetchStations()
    stations.value = data
    const districts = [...new Set(data.map(s => s.district))]
    districtOptions.value = districts
  } catch {
    ElMessage.warning('检测站加载失败')
  }
}

const loadStationStatuses = async () => {
  try {
    const { data } = await fetchStationStatuses()
    stationStatuses.value = data
  } catch {
    ElMessage.warning('检测站状态加载失败')
  }
}

const handleStationClick = (row: Station) => {
  const status = stationStatuses.value.find(s => s.stationId === row.id)
  if (status) {
    currentStationStatus.value = status
  } else {
    currentStationStatus.value = {
      stationId: row.id,
      stationName: row.name,
      district: row.district,
      address: row.address,
      phone: row.phone,
      todayInspectionCount: 0,
      passedCount: 0,
      failedCount: 0,
      passRate: 0,
      lastInspectionTime: '',
      runningStatus: row.status === '正常' ? '空闲' : '停运'
    }
  }
  drawerVisible.value = true
}

const formatDate = (dateStr?: string): string => {
  if (!dateStr) return ''
  if (dateStr.includes(' ')) {
    return dateStr.split(' ')[0]
  }
  return dateStr
}

const viewReportDetail = (row: InspectionRecord) => {
  router.push({ name: 'report-detail', params: { inspectionNo: row.inspectionNo } })
}

onMounted(async () => {
  const [announcementResp] = await Promise.all([fetchAnnouncements()])
  announcements.value = announcementResp.data
  await Promise.all([loadStations(), loadStationStatuses()])
  await queryVehicle()
})
</script>

<style scoped>
h2 {
  margin: 0 0 16px;
}

.query-card {
  display: grid;
  gap: 14px;
  align-content: start;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-header h2 {
  margin: 0;
}

.station-filter {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}
</style>
