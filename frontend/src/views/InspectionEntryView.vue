<template>
  <main class="page entry-page">
    <aside class="sidebar">
      <div class="brand">
        <span class="brand-mark">环</span>
        <span>检测站工作台</span>
      </div>
      <el-menu :default-active="activeMenu" @select="handleMenuSelect">
        <el-menu-item index="entry">检测记录录入</el-menu-item>
        <el-menu-item index="records">检测记录</el-menu-item>
      </el-menu>
    </aside>

    <section class="entry-main">
      <header class="entry-header">
        <div>
          <h1>检测记录录入</h1>
          <p class="muted">检测站工作人员 · 请准确填写车辆检测数据</p>
        </div>
        <div class="header-actions">
          <el-button @click="goBack">返回</el-button>
        </div>
      </header>

      <div class="grid grid-2">
        <div class="card form-card">
          <div class="section-header">
            <h2>基本信息</h2>
          </div>
          <el-form :model="form" label-width="100px" label-position="right">
            <el-form-item label="车牌号" prop="plateNumber" :rules="plateNumberRules">
              <el-input
                v-model="form.plateNumber"
                placeholder="请输入车牌号，如京A12345"
                maxlength="10"
                clearable
                style="text-transform: uppercase"
                @blur="onPlateNumberBlur"
              />
            </el-form-item>

            <el-form-item label="检测站" prop="stationName" :rules="stationRules">
              <el-select
                v-model="form.stationName"
                placeholder="请选择检测站"
                filterable
                style="width: 100%"
              >
                <el-option
                  v-for="station in stations"
                  :key="station.id"
                  :label="station.name"
                  :value="station.name"
                  :disabled="station.status !== '正常'"
                >
                  <span>{{ station.name }}</span>
                  <span v-if="station.status !== '正常'" style="color: #f56c6c; margin-left: 8px">
                    ({{ station.status }})
                  </span>
                </el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="检测人员" prop="inspector" :rules="inspectorRules">
              <el-input
                v-model="form.inspector"
                placeholder="请输入检测人员姓名"
                maxlength="20"
                clearable
              />
            </el-form-item>
          </el-form>

          <el-alert
            v-if="vehicleInfo"
            :title="`车辆信息：${vehicleInfo.fuelType} / ${vehicleInfo.emissionStandard}"
            type="info"
            :closable="false"
            style="margin: 0 0 16px"
          >
            <template #default>
              车辆类型：{{ vehicleInfo.vehicleType }} · 车主：{{ vehicleInfo.owner }}
            </template>
          </el-alert>

          <div class="section-header" style="margin-top: 24px">
            <h2>尾气检测数据</h2>
            <el-tag :type="computedResult === '合格' ? 'success' : 'danger'" effect="dark">
              预估结果：{{ computedResult }}
            </el-tag>
          </div>

          <el-form :model="form" label-width="100px" label-position="right">
            <el-form-item label="CO 检测值">
              <el-input-number
                v-model="form.coValue"
                :min="0"
                :max="10"
                :step="0.01"
                :precision="2"
                style="width: 100%"
                controls-position="right"
              />
              <div class="hint">
                限值：≤ {{ currentLimits.co }}%
                <el-tag
                  v-if="form.coValue > 0"
                  :type="form.coValue <= currentLimits.co ? 'success' : 'danger'"
                  size="small"
                  style="margin-left: 8px"
                >
                  {{ form.coValue <= currentLimits.co ? '合格' : '超标' }}
                </el-tag>
              </div>
            </el-form-item>

            <el-form-item label="HC 检测值">
              <el-input-number
                v-model="form.hcValue"
                :min="0"
                :max="200"
                :step="0.1"
                :precision="1"
                style="width: 100%"
                controls-position="right"
              />
              <div class="hint">
                限值：≤ {{ currentLimits.hc }} ppm
                <el-tag
                  v-if="form.hcValue > 0"
                  :type="form.hcValue <= currentLimits.hc ? 'success' : 'danger'"
                  size="small"
                  style="margin-left: 8px"
                >
                  {{ form.hcValue <= currentLimits.hc ? '合格' : '超标' }}
                </el-tag>
              </div>
            </el-form-item>

            <el-form-item label="NOx 检测值">
              <el-input-number
                v-model="form.noxValue"
                :min="0"
                :max="500"
                :step="0.1"
                :precision="1"
                style="width: 100%"
                controls-position="right"
              />
              <div class="hint">
                限值：≤ {{ currentLimits.nox }} ppm
                <el-tag
                  v-if="form.noxValue > 0"
                  :type="form.noxValue <= currentLimits.nox ? 'success' : 'danger'"
                  size="small"
                  style="margin-left: 8px"
                >
                  {{ form.noxValue <= currentLimits.nox ? '合格' : '超标' }}
                </el-tag>
              </div>
            </el-form-item>

            <el-form-item label="烟度值">
              <el-input-number
                v-model="form.opacityValue"
                :min="0"
                :max="5"
                :step="0.01"
                :precision="2"
                style="width: 100%"
                controls-position="right"
              />
              <div class="hint">
                限值：≤ {{ currentLimits.opacity }} m⁻¹
                <el-tag
                  v-if="form.opacityValue > 0"
                  :type="form.opacityValue <= currentLimits.opacity ? 'success' : 'danger'"
                  size="small"
                  style="margin-left: 8px"
                >
                  {{ form.opacityValue <= currentLimits.opacity ? '合格' : '超标' }}
                </el-tag>
              </div>
            </el-form-item>
          </el-form>

          <div class="form-actions">
            <el-button @click="resetForm">重置</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">
              提交检测记录
            </el-button>
          </div>
        </div>

        <div class="card preview-card">
          <div class="section-header">
            <h2>录入预览</h2>
          </div>
          <el-descriptions :column="1" border size="default">
            <el-descriptions-item label="车牌号">
              <span style="font-weight: 600; font-size: 16px">
                {{ form.plateNumber || '-' }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="燃料/排放标准">
              <el-tag size="small" effect="plain">{{ vehicleInfo ? `${vehicleInfo.fuelType} / ${vehicleInfo.emissionStandard}` : '-' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="检测站">
              {{ form.stationName || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="检测人员">
              {{ form.inspector || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="检测时间">
              {{ currentTime }}
            </el-descriptions-item>
          </el-descriptions>

          <div class="data-preview">
            <h3>检测数据</h3>
            <div class="data-grid">
              <div class="data-item">
                <div class="data-label">CO 值</div>
                <div class="data-value" :class="{ 'over-limit': form.coValue > currentLimits.co }">
                  {{ form.coValue.toFixed(2) }}
                  <span class="unit">%</span>
                </div>
                <div class="data-limit">限值：{{ currentLimits.co }}%</div>
              </div>
              <div class="data-item">
                <div class="data-label">HC 值</div>
                <div class="data-value" :class="{ 'over-limit': form.hcValue > currentLimits.hc }">
                  {{ form.hcValue.toFixed(1) }}
                  <span class="unit">ppm</span>
                </div>
                <div class="data-limit">限值：{{ currentLimits.hc }} ppm</div>
              </div>
              <div class="data-item">
                <div class="data-label">NOx 值</div>
                <div class="data-value" :class="{ 'over-limit': form.noxValue > currentLimits.nox }">
                  {{ form.noxValue.toFixed(1) }}
                  <span class="unit">ppm</span>
                </div>
                <div class="data-limit">限值：{{ currentLimits.nox }} ppm</div>
              </div>
              <div class="data-item">
                <div class="data-label">烟度值</div>
                <div class="data-value" :class="{ 'over-limit': form.opacityValue > currentLimits.opacity }">
                  {{ form.opacityValue.toFixed(2) }}
                  <span class="unit">m⁻¹</span>
                </div>
                <div class="data-limit">限值：{{ currentLimits.opacity }} m⁻¹</div>
              </div>
            </div>
          </div>

          <div class="result-preview">
            <div class="result-label">检测结果</div>
            <el-tag :type="computedResult === '合格' ? 'success' : 'danger'" size="large" effect="dark">
              {{ computedResult }}
            </el-tag>
          </div>

          <el-alert
            v-if="computedResult === '不合格'"
            type="warning"
            :closable="false"
            style="margin-top: 16px"
          >
            <template #title>
              检测结果不合格的记录将进入审核流程，由监管人员进行复核。
            </template>
          </el-alert>
        </div>
      </div>
    </section>

    <el-dialog
      v-model="successDialogVisible"
      title="录入成功"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="success-content">
        <el-result icon="success" title="检测记录录入成功" :sub-title="successMessage">
          <template #extra>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="检测编号">
                <span style="font-weight: 600">{{ newRecordNo }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="车牌号">{{ form.plateNumber }}</el-descriptions-item>
              <el-descriptions-item label="检测结果">
                <el-tag :type="computedResult === '合格' ? 'success' : 'danger'">
                  {{ computedResult }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="报告状态">
                <el-tag type="warning">待审核</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </template>
        </el-result>
      </div>
      <template #footer>
        <el-button @click="continueEntry">继续录入</el-button>
        <el-button type="primary" @click="viewRecord">查看记录</el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormRules } from 'element-plus'
import {
  createInspection,
  fetchStations,
  searchVehicle,
  queryPollutantLimitRule,
  type CreateInspectionRequest,
  type PollutantLimitRule,
  type Station,
  type Vehicle
} from '@/api/platform'

const router = useRouter()
const route = useRoute()
const activeMenu = ref('entry')
const stations = ref<Station[]>([])
const submitting = ref(false)
const successDialogVisible = ref(false)
const newRecordNo = ref('')
const successMessage = ref('')

const DEFAULT_LIMITS = { co: 0.3, hc: 30, nox: 70, opacity: 0.25 }

const form = ref<CreateInspectionRequest>({
  plateNumber: '',
  stationName: '',
  coValue: 0,
  hcValue: 0,
  noxValue: 0,
  opacityValue: 0,
  inspector: ''
})

const vehicleInfo = ref<Vehicle | null>(null)
const currentRule = ref<PollutantLimitRule | null>(null)
const currentLimits = reactive({ ...DEFAULT_LIMITS })

const plateNumberRules: FormRules = {
  plateNumber: [
    { required: true, message: '请输入车牌号', trigger: 'blur' },
    { min: 6, max: 10, message: '车牌号长度在 6 到 10 个字符', trigger: 'blur' }
  ]
}

const stationRules: FormRules = {
  stationName: [
    { required: true, message: '请选择检测站', trigger: 'change' }
  ]
}

const inspectorRules: FormRules = {
  inspector: [
    { required: true, message: '请输入检测人员姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

const computedResult = computed(() => {
  const coPass = form.value.coValue <= currentLimits.co
  const hcPass = form.value.hcValue <= currentLimits.hc
  const noxPass = form.value.noxValue <= currentLimits.nox
  const opacityPass = form.value.opacityValue <= currentLimits.opacity

  if (coPass && hcPass && noxPass && opacityPass) {
    return '合格'
  }
  return '不合格'
})

const currentTime = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
})

const loadStations = async () => {
  try {
    const { data } = await fetchStations()
    stations.value = data
  } catch {
    ElMessage.warning('检测站列表加载失败')
  }
}

const applyLimits = (rule: PollutantLimitRule | null) => {
  if (rule) {
    currentLimits.co = rule.coLimit
    currentLimits.hc = rule.hcLimit
    currentLimits.nox = rule.noxLimit
    currentLimits.opacity = rule.opacityLimit
  } else {
    Object.assign(currentLimits, DEFAULT_LIMITS)
  }
}

const loadVehicleAndRule = async (plateNumber: string) => {
  if (!plateNumber.trim()) {
    vehicleInfo.value = null
    currentRule.value = null
    applyLimits(null)
    return
  }
  try {
    const { data } = await searchVehicle(plateNumber.trim().toUpperCase())
    if (data.success && data.data) {
      vehicleInfo.value = data.data
      try {
        const ruleResp = await queryPollutantLimitRule(data.data.fuelType, data.data.emissionStandard)
        currentRule.value = ruleResp.data
        applyLimits(currentRule.value)
        ElMessage.info(`已加载 ${data.data.fuelType} / ${data.data.emissionStandard} 限值规则`)
      } catch {
        currentRule.value = null
        applyLimits(null)
      }
    } else {
      vehicleInfo.value = null
      currentRule.value = null
      applyLimits(null)
      ElMessage.warning('未找到车辆信息，将使用默认限值')
    }
  } catch {
    vehicleInfo.value = null
    currentRule.value = null
    applyLimits(null)
  }
}

const onPlateNumberBlur = () => {
  if (form.value.plateNumber.trim()) {
    loadVehicleAndRule(form.value.plateNumber)
  }
}

watch(() => form.value.plateNumber, (newVal) => {
  if (newVal && newVal.trim().length >= 6) {
    loadVehicleAndRule(newVal)
  }
})

const handleMenuSelect = (index: string) => {
  if (index === 'records') {
    router.push('/admin')
  }
}

const goBack = () => {
  router.push('/admin')
}

const resetForm = () => {
  form.value = {
    plateNumber: '',
    stationName: '',
    coValue: 0,
    hcValue: 0,
    noxValue: 0,
    opacityValue: 0,
    inspector: ''
  }
  vehicleInfo.value = null
  currentRule.value = null
  applyLimits(null)
}

const validateForm = (): boolean => {
  if (!form.value.plateNumber.trim()) {
    ElMessage.warning('请输入车牌号')
    return false
  }
  if (form.value.plateNumber.trim().length < 6) {
    ElMessage.warning('车牌号长度不能少于 6 个字符')
    return false
  }
  if (!form.value.stationName) {
    ElMessage.warning('请选择检测站')
    return false
  }
  if (!form.value.inspector.trim()) {
    ElMessage.warning('请输入检测人员姓名')
    return false
  }
  return true
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  submitting.value = true
  try {
    const { data } = await createInspection({
      ...form.value,
      plateNumber: form.value.plateNumber.trim().toUpperCase(),
      inspector: form.value.inspector.trim()
    })

    if (data.success) {
      newRecordNo.value = data.record.inspectionNo
      successMessage.value = data.message
      successDialogVisible.value = true
    } else {
      ElMessage.error(data.message || '录入失败，请重试')
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '网络异常，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const continueEntry = () => {
  successDialogVisible.value = false
  const prevPlate = form.value.plateNumber
  const prevStation = form.value.stationName
  const prevInspector = form.value.inspector
  resetForm()
  form.value.stationName = prevStation
  form.value.inspector = prevInspector
}

const viewRecord = () => {
  successDialogVisible.value = false
  router.push({ name: 'report-detail', params: { inspectionNo: newRecordNo.value } })
}

onMounted(async () => {
  await loadStations()
  if (route.query.plateNumber) {
    form.value.plateNumber = String(route.query.plateNumber)
    loadVehicleAndRule(form.value.plateNumber)
  }
})
</script>

<style scoped>
.entry-page {
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

.entry-main {
  padding: 24px;
  overflow-y: auto;
}

.entry-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.entry-header h1 {
  margin: 0 0 4px;
}

.entry-header p {
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.form-card {
  display: flex;
  flex-direction: column;
}

.preview-card {
  display: flex;
  flex-direction: column;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-header h2 {
  margin: 0;
  font-size: 18px;
}

.hint {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.data-preview {
  margin-top: 20px;
}

.data-preview h3 {
  margin: 0 0 12px;
  font-size: 15px;
}

.data-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.data-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.data-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.data-value {
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.data-value .unit {
  font-size: 12px;
  font-weight: normal;
  color: #909399;
  margin-left: 2px;
}

.data-limit {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
}

.data-value.over-limit {
  color: #f56c6c;
}

.result-preview {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  padding: 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 8px;
}

.result-label {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.success-content {
  text-align: center;
}

.muted {
  color: #909399;
}

.grid-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

@media (max-width: 960px) {
  .entry-page {
    grid-template-columns: 1fr;
  }

  .sidebar {
    min-height: auto;
  }

  .grid-2 {
    grid-template-columns: 1fr;
  }
}
</style>
