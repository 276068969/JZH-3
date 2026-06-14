<template>
  <main class="page">
    <header class="topbar">
      <div class="shell topbar-inner">
        <div class="brand" style="cursor: pointer" @click="router.push('/')">
          <span class="brand-mark">环</span>
          <span>机动车尾气监管平台</span>
        </div>
        <nav>
          <el-button text @click="router.push('/')">返回首页</el-button>
          <el-button type="primary" @click="router.push('/admin')">管理后台</el-button>
        </nav>
      </div>
    </header>

    <section v-loading="loading" class="section">
      <div class="shell">
        <el-empty v-if="!loading && !record" description="未找到检测报告" />

        <template v-if="record">
          <div class="card report-header">
            <div class="report-header-left">
              <el-button :icon="ArrowLeft" circle @click="goBack" style="margin-right: 14px" />
              <div>
                <div class="breadcrumb muted" @click="router.push('/')" style="cursor: pointer">
                  首页 / 车辆查询 / 检测报告详情
                </div>
                <h1 style="margin: 6px 0 4px; font-size: 24px">
                  检测报告详情
                </h1>
                <div class="muted">
                  报告编号：<span class="mono">{{ record.inspectionNo }}</span>
                </div>
              </div>
            </div>
            <div class="report-header-right">
              <el-tag
                :type="record.result === '合格' ? 'success' : 'danger'"
                effect="dark"
                size="large"
              >
                {{ record.result === '合格' ? '✓ 检测合格' : '⚠ 检测不合格' }}
              </el-tag>
              <el-tag
                :type="getStatusType(record.reportStatus)"
                effect="plain"
                size="large"
                style="margin-left: 8px"
              >
                {{ record.reportStatus }}
              </el-tag>
            </div>
          </div>

          <div class="grid grid-3" style="margin-top: 16px">
            <div class="card">
              <h2 class="card-title">
                <span class="title-icon vehicle-icon">🚗</span>
                车辆信息
              </h2>
              <el-descriptions :column="1" border size="default">
                <el-descriptions-item label="车牌号">
                  <span class="plate-number">{{ record.plateNumber }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="检测时间">
                  {{ record.inspectionTime }}
                </el-descriptions-item>
                <el-descriptions-item label="燃料/排放标准" v-if="vehicleInfo">
                  <el-tag size="small" effect="plain">{{ vehicleInfo.fuelType }} / {{ vehicleInfo.emissionStandard }}</el-tag>
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <div class="card">
              <h2 class="card-title">
                <span class="title-icon station-icon">🏢</span>
                检测站信息
              </h2>
              <el-descriptions :column="1" border size="default">
                <el-descriptions-item label="检测站">
                  {{ record.stationName }}
                </el-descriptions-item>
                <el-descriptions-item label="检测人员">
                  {{ record.inspector }}
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <div class="card">
              <h2 class="card-title">
                <span class="title-icon audit-icon">📋</span>
                审核信息
              </h2>
              <el-descriptions :column="1" border size="default">
                <el-descriptions-item label="审核人">
                  {{ record.auditor || '—' }}
                </el-descriptions-item>
                <el-descriptions-item label="审核时间">
                  {{ record.auditTime || '—' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </div>

          <div class="card" style="margin-top: 16px">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px">
              <h2 class="card-title" style="margin: 0">
                <span class="title-icon pollutant-icon">📊</span>
                污染物检测结果
              </h2>
              <el-tag v-if="currentRule" size="small" effect="plain" type="info">
                适用规则：{{ currentRule.fuelType }} / {{ currentRule.emissionStandard }}
              </el-tag>
              <el-tag v-else size="small" effect="plain" type="warning">
                默认限值
              </el-tag>
            </div>
            <el-table :data="pollutantRows" border stripe>
              <el-table-column prop="name" label="污染物项目" width="160" />
              <el-table-column prop="unit" label="单位" width="100" align="center" />
              <el-table-column label="限值" width="180" align="center">
                <template #default="{ row }">
                  ≤ {{ row.limit }}
                </template>
              </el-table-column>
              <el-table-column prop="value" label="检测值" width="180" align="center">
                <template #default="{ row }">
                  <span
                    :class="{
                      'value-exceed': !row.passed,
                      'value-pass': row.passed
                    }"
                  >
                    {{ row.value }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="120" align="center">
                <template #default="{ row }">
                  <el-tag
                    :type="row.passed ? 'success' : 'danger'"
                    effect="light"
                    size="small"
                  >
                    {{ row.passed ? '合格' : '超标' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="达标比例">
                <template #default="{ row }">
                  <el-progress
                    :percentage="row.percentage"
                    :status="row.passed ? (row.percentage > 80 ? 'warning' : undefined) : 'exception'"
                    :stroke-width="14"
                    :show-text="true"
                  />
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="card" style="margin-top: 16px">
            <h2 class="card-title">
              <span class="title-icon conclusion-icon">✅</span>
              检测结论
            </h2>
            <div class="conclusion-box" :class="record.result === '合格' ? 'pass' : 'fail'">
              <div class="conclusion-icon">
                {{ record.result === '合格' ? '✓' : '✗' }}
              </div>
              <div class="conclusion-content">
                <div class="conclusion-title">
                  检测结论：<strong>{{ record.result }}</strong>
                </div>
                <div class="conclusion-desc">
                  <template v-if="record.result === '合格'">
                    所有污染物检测项目均符合排放标准（{{ appliedStandardText }}），车辆环保状态正常。
                  </template>
                  <template v-else>
                    存在污染物检测项目超出限值（{{ appliedStandardText }}），
                    <strong>建议尽快进行车辆维修并复检</strong>。
                  </template>
                </div>
                <div v-if="judgmentResult" class="judgment-detail">
                  <div style="margin-top: 8px; color: #475569">
                    <strong>环保状态：</strong>
                    <el-tag :type="getEnvStatusType(judgmentResult.environmentalStatus)" effect="plain">
                      {{ judgmentResult.environmentalStatus }}
                    </el-tag>
                    <el-tag :type="getLevelType(judgmentResult.statusLevel)" size="small" style="margin-left: 8px">
                      {{ judgmentResult.statusLevel }}级
                    </el-tag>
                  </div>
                  <div v-if="judgmentResult.suggestion" style="margin-top: 8px; color: #475569">
                    <strong>处置建议：</strong>{{ judgmentResult.suggestion }}
                  </div>
                </div>
              </div>
            </div>

            <div v-if="record.auditOpinion" class="audit-opinion">
              <div class="audit-opinion-label">审核意见：</div>
              <div class="audit-opinion-text">{{ record.auditOpinion }}</div>
            </div>
          </div>
        </template>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import {
  fetchInspectionDetail,
  searchVehicle,
  queryPollutantLimitRule,
  judgeEnvironmentalByNo,
  type InspectionRecord,
  type Vehicle,
  type PollutantLimitRule,
  type EnvironmentalJudgmentResult
} from '@/api/platform'

const DEFAULT_LIMITS = { co: 0.3, hc: 30, nox: 70, opacity: 0.25 }

interface PollutantRow {
  name: string
  unit: string
  limit: number
  value: number
  passed: boolean
  percentage: number
}

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const record = ref<InspectionRecord | null>(null)
const vehicleInfo = ref<Vehicle | null>(null)
const currentRule = ref<PollutantLimitRule | null>(null)
const judgmentResult = ref<EnvironmentalJudgmentResult | null>(null)

const currentLimits = computed(() => {
  if (currentRule.value) {
    return {
      co: currentRule.value.coLimit,
      hc: currentRule.value.hcLimit,
      nox: currentRule.value.noxLimit,
      opacity: currentRule.value.opacityLimit
    }
  }
  return DEFAULT_LIMITS
})

const appliedStandardText = computed(() => {
  if (currentRule.value) {
    return `${currentRule.value.fuelType} / ${currentRule.value.emissionStandard}`
  }
  return '默认标准'
})

const pollutantRows = computed<PollutantRow[]>(() => {
  if (!record.value) return []
  const r = record.value
  const limits = currentLimits.value
  const calcRow = (name: string, unit: string, value: number, limit: number): PollutantRow => {
    const passed = value <= limit
    const rawPct = Math.round((value / limit) * 100)
    const percentage = rawPct > 999 ? 999 : rawPct
    return { name, unit, limit, value, passed, percentage }
  }
  return [
    calcRow('一氧化碳 (CO)', '%', Number(r.coValue.toFixed(2)), limits.co),
    calcRow('碳氢化合物 (HC)', 'ppm', Number(r.hcValue.toFixed(1)), limits.hc),
    calcRow('氮氧化物 (NOx)', 'ppm', Number(r.noxValue.toFixed(1)), limits.nox),
    calcRow('烟度 (Opacity)', 'm⁻¹', Number(r.opacityValue.toFixed(2)), limits.opacity)
  ]
})

const getStatusType = (status: string) => {
  if (status === '已审核') return 'primary'
  if (status === '待审核') return 'warning'
  if (status === '已退回') return 'danger'
  return 'info'
}

const getEnvStatusType = (status: string) => {
  if (status === '环保合格' || status === '合格') return 'success'
  if (status === '轻微超标') return 'warning'
  if (status === '超标') return 'warning'
  if (status === '严重超标') return 'danger'
  return 'info'
}

const getLevelType = (level: string) => {
  if (level === '合格' || level === '低') return 'success'
  if (level === '中') return 'warning'
  if (level === '高') return 'danger'
  return 'info'
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

const loadDetail = async () => {
  const inspectionNo = (route.params.inspectionNo as string) || (route.query.inspectionNo as string)
  if (!inspectionNo) {
    ElMessage.error('缺少检测报告编号')
    return
  }
  loading.value = true
  try {
    const { data } = await fetchInspectionDetail(inspectionNo)
    record.value = data

    if (data.plateNumber) {
      try {
        const vehicleResp = await searchVehicle(data.plateNumber)
        if (vehicleResp.data.success && vehicleResp.data.data) {
          vehicleInfo.value = vehicleResp.data.data
          try {
            const ruleResp = await queryPollutantLimitRule(
              vehicleInfo.value.fuelType,
              vehicleInfo.value.emissionStandard
            )
            currentRule.value = ruleResp.data
          } catch {
            currentRule.value = null
          }
        }
      } catch {
        // ignore vehicle info load error
      }

      try {
        const fuelType = vehicleInfo.value?.fuelType
        const emissionStandard = vehicleInfo.value?.emissionStandard
        const judgeResp = await judgeEnvironmentalByNo(inspectionNo, { fuelType, emissionStandard })
        judgmentResult.value = judgeResp.data
      } catch {
        // ignore judgment load error
      }
    }
  } catch (e: any) {
    if (e?.response?.status === 404) {
      ElMessage.error('未找到对应的检测报告')
    } else {
      ElMessage.error(e?.message || '加载检测报告失败')
    }
    record.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.mono {
  font-family: 'JetBrains Mono', Consolas, monospace;
  letter-spacing: 0.5px;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.report-header-left {
  display: flex;
  align-items: center;
}

.report-header-right {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.breadcrumb {
  font-size: 13px;
}

.card-title {
  margin: 0 0 14px;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 18px;
}

.plate-number {
  font-weight: 700;
  color: #1d4ed8;
  letter-spacing: 1px;
}

.value-pass {
  font-weight: 600;
  color: #217346;
}

.value-exceed {
  font-weight: 700;
  color: #c0392b;
}

.conclusion-box {
  display: flex;
  align-items: flex-start;
  gap: 18px;
  padding: 20px;
  border-radius: 10px;
  border-left: 5px solid;
}

.conclusion-box.pass {
  background: linear-gradient(135deg, #ecfdf5 0%, #f0fdf4 100%);
  border-color: #10b981;
}

.conclusion-box.fail {
  background: linear-gradient(135deg, #fef2f2 0%, #fff1f2 100%);
  border-color: #ef4444;
}

.conclusion-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-size: 26px;
  font-weight: 800;
  flex-shrink: 0;
}

.pass .conclusion-icon {
  background: #10b981;
  color: #fff;
}

.fail .conclusion-icon {
  background: #ef4444;
  color: #fff;
}

.conclusion-title {
  font-size: 18px;
  margin-bottom: 6px;
}

.conclusion-desc {
  color: #516173;
  line-height: 1.75;
}

.judgment-detail {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed rgba(0, 0, 0, 0.1);
}

.audit-opinion {
  margin-top: 18px;
  padding: 16px;
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 8px;
}

.audit-opinion-label {
  font-weight: 600;
  color: #334155;
  margin-bottom: 6px;
}

.audit-opinion-text {
  color: #475569;
  line-height: 1.7;
  white-space: pre-wrap;
}
</style>
