<template>
  <main class="page">
    <header class="topbar">
      <div class="shell topbar-inner">
        <div class="brand" style="cursor: pointer" @click="router.push('/admin')">
          <span class="brand-mark">环</span>
          <span>机动车尾气监管平台</span>
        </div>
        <nav>
          <el-button text @click="router.push('/admin')">返回后台</el-button>
        </nav>
      </div>
    </header>

    <section v-loading="loading" class="section">
      <div class="shell">
        <el-empty v-if="!loading && !warning" description="未找到预警记录" />

        <template v-if="warning">
          <div class="card warning-header" :class="`level-${levelClass}`">
            <div class="warning-header-left">
              <el-button :icon="ArrowLeft" circle @click="goBack" style="margin-right: 14px" />
              <div>
                <div class="breadcrumb muted" @click="router.push('/admin')" style="cursor: pointer">
                  管理后台 / 超标预警 / 预警处置
                </div>
                <h1 style="margin: 6px 0 4px; font-size: 22px">
                  超标车辆预警处置
                </h1>
                <div class="muted">
                  预警编号：<span class="mono">WN-{{ String(warning.id).padStart(6, '0') }}</span>
                  <span style="margin: 0 12px">|</span>
                  生成时间：<span>{{ warning.createdAt }}</span>
                </div>
              </div>
            </div>
            <div class="warning-header-right">
              <el-tag :type="levelTagType" effect="dark" size="large">
                {{ warning.level }}风险
              </el-tag>
              <el-tag
                :type="statusTagType"
                effect="plain"
                size="large"
                style="margin-left: 8px"
              >
                {{ warning.status }}
              </el-tag>
            </div>
          </div>

          <div class="grid grid-3" style="margin-top: 16px">
            <div class="card">
              <h2 class="card-title">
                <span class="title-icon">🚗</span>
                车辆与预警信息
              </h2>
              <el-descriptions :column="1" border size="default">
                <el-descriptions-item label="车牌号">
                  <span class="plate-number">{{ warning.plateNumber }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="超标污染物">
                  <el-tag type="danger" effect="light">{{ warning.pollutant }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="预警等级">
                  <el-tag :type="levelTagType" effect="light">{{ warning.level }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="预警描述">
                  {{ warning.description }}
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <div class="card">
              <h2 class="card-title">
                <span class="title-icon">📋</span>
                处置信息
              </h2>
              <el-descriptions :column="1" border size="default">
                <el-descriptions-item label="处置人">
                  {{ warning.handler || '—' }}
                </el-descriptions-item>
                <el-descriptions-item label="处置时间">
                  {{ warning.handleTime || '—' }}
                </el-descriptions-item>
                <el-descriptions-item label="处置意见">
                  {{ warning.handleOpinion || '—' }}
                </el-descriptions-item>
                <el-descriptions-item label="复检要求">
                  <el-tag v-if="warning.reinspectRequired" type="warning" effect="light">需要复检</el-tag>
                  <span v-else>无需复检</span>
                </el-descriptions-item>
                <el-descriptions-item v-if="warning.reinspectRequired" label="复检截止日期">
                  {{ warning.reinspectDeadline || '—' }}
                </el-descriptions-item>
              </el-descriptions>
            </div>

            <div class="card">
              <h2 class="card-title">
                <span class="title-icon">📊</span>
                处置流程
              </h2>
              <el-steps :active="flowStepActive" direction="vertical" :space="60">
                <el-step title="预警生成" :description="warning.createdAt" />
                <el-step title="开始处置" :description="warning.status !== '待处置' ? warning.handleTime : ''" />
                <el-step title="处置完成" :description="warning.status === '已处置' ? warning.handleTime : ''" />
                <el-step
                  v-if="warning.reinspectRequired"
                  title="复检确认"
                  :description="warning.status === '已复检' ? '已完成' : '待复检'"
                />
              </el-steps>
            </div>
          </div>

          <div class="card" style="margin-top: 16px">
            <h2 class="card-title">
              <span class="title-icon">🔍</span>
              关联检测记录
            </h2>
            <el-table v-if="relatedInspections.length > 0" :data="relatedInspections" border stripe>
              <el-table-column prop="inspectionNo" label="检测编号" min-width="150" />
              <el-table-column prop="inspectionTime" label="检测时间" min-width="160" />
              <el-table-column prop="stationName" label="检测站" min-width="160" />
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
              <el-table-column label="操作" width="100" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link @click="viewReportDetail(row)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="暂无关联检测记录" :image-size="60" />
          </div>

          <div v-if="warning.status !== '已处置' && warning.status !== '已复检'" class="card" style="margin-top: 16px">
            <h2 class="card-title">
              <span class="title-icon">✏️</span>
              填写处置意见
            </h2>
            <el-form ref="handleFormRef" :model="handleForm" :rules="handleRules" label-width="120px" class="handle-form">
              <el-form-item label="处置意见" prop="handleOpinion">
                <el-input
                  v-model="handleForm.handleOpinion"
                  type="textarea"
                  :rows="4"
                  placeholder="请填写处置意见，描述超标原因分析及处置措施"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
              <el-form-item label="处理状态" prop="status">
                <el-radio-group v-model="handleForm.status">
                  <el-radio label="处置中">处置中（继续跟踪）</el-radio>
                  <el-radio label="已处置">已处置（处置完成）</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="是否需要复检" prop="reinspectRequired">
                <el-switch
                  v-model="handleForm.reinspectRequired"
                  active-text="需要复检"
                  inactive-text="无需复检"
                />
              </el-form-item>
              <el-form-item v-if="handleForm.reinspectRequired" label="复检截止日期" prop="reinspectDeadline">
                <el-date-picker
                  v-model="handleForm.reinspectDeadline"
                  type="date"
                  placeholder="选择复检截止日期"
                  value-format="YYYY-MM-DD"
                  :disabled-date="disablePastDate"
                  style="width: 100%"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" :loading="submitLoading" @click="submitHandle">
                  提交处置意见
                </el-button>
                <el-button @click="resetForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <div v-if="warning.status === '已处置' && warning.reinspectRequired" class="card" style="margin-top: 16px">
            <div class="reinspect-action">
              <div>
                <h2 class="card-title">
                  <span class="title-icon">🔄</span>
                  复检确认
                </h2>
                <p class="muted">该车辆已标记需要复检，复检截止日期：{{ warning.reinspectDeadline || '—' }}</p>
              </div>
              <el-button type="success" :loading="reinspectLoading" @click="confirmReinspect">
                确认已复检
              </el-button>
            </div>
          </div>
        </template>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import {
  fetchWarningDetail,
  fetchWarningInspections,
  handleWarning,
  type InspectionRecord,
  type WarningRecord
} from '@/api/platform'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const submitLoading = ref(false)
const reinspectLoading = ref(false)
const warning = ref<WarningRecord | null>(null)
const relatedInspections = ref<InspectionRecord[]>([])
const handleFormRef = ref<FormInstance>()

const handleForm = reactive({
  handleOpinion: '',
  status: '处置中',
  reinspectRequired: false,
  reinspectDeadline: ''
})

const handleRules: FormRules = {
  handleOpinion: [
    { required: true, message: '请填写处置意见', trigger: 'blur' },
    { min: 5, message: '处置意见至少输入5个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择处理状态', trigger: 'change' }
  ],
  reinspectDeadline: [
    {
      validator: (_rule: any, value: any, callback: any) => {
        if (handleForm.reinspectRequired && !value) {
          callback(new Error('请选择复检截止日期'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

const levelClass = computed(() => {
  if (!warning.value) return 'low'
  const map: Record<string, string> = { '高': 'high', '中': 'medium', '低': 'low' }
  return map[warning.value.level] || 'low'
})

const levelTagType = computed(() => {
  if (!warning.value) return 'info'
  const map: Record<string, string> = { '高': 'danger', '中': 'warning', '低': 'info' }
  return map[warning.value.level] || 'info'
})

const statusTagType = computed(() => {
  if (!warning.value) return 'info'
  const map: Record<string, string> = {
    '待处置': 'danger',
    '处置中': 'warning',
    '已处置': 'success',
    '已复检': 'primary'
  }
  return map[warning.value.status] || 'info'
})

const flowStepActive = computed(() => {
  if (!warning.value) return 0
  if (warning.value.status === '已复检') return 4
  if (warning.value.status === '已处置') return warning.value.reinspectRequired ? 2 : 3
  if (warning.value.status === '处置中') return 1
  return 0
})

const getStatusType = (status: string) => {
  if (status === '已审核') return 'primary'
  if (status === '待审核') return 'warning'
  if (status === '已退回') return 'danger'
  return 'info'
}

const disablePastDate = (date: Date) => {
  return date.getTime() < Date.now() - 86400000
}

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/admin')
  }
}

const viewReportDetail = (row: InspectionRecord) => {
  router.push({ name: 'report-detail', params: { inspectionNo: row.inspectionNo } })
}

const loadWarningDetail = async () => {
  const id = Number(route.params.id || route.query.id)
  if (!id) {
    ElMessage.error('缺少预警编号')
    return
  }

  loading.value = true
  try {
    const { data } = await fetchWarningDetail(id)
    warning.value = data

    const inspResp = await fetchWarningInspections(data.plateNumber)
    relatedInspections.value = inspResp.data
  } catch (e: any) {
    if (e?.response?.status === 404) {
      ElMessage.error('未找到对应的预警记录')
    } else {
      ElMessage.error(e?.message || '加载预警详情失败')
    }
    warning.value = null
  } finally {
    loading.value = false
  }
}

const submitHandle = async () => {
  if (!handleFormRef.value || !warning.value) return

  await handleFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true
    try {
      const { data } = await handleWarning({
        warningId: warning.value!.id,
        handleOpinion: handleForm.handleOpinion,
        reinspectRequired: handleForm.reinspectRequired,
        reinspectDeadline: handleForm.reinspectRequired ? handleForm.reinspectDeadline : undefined,
        status: handleForm.status
      })

      if (data.success) {
        ElMessage.success(data.message)
        await loadWarningDetail()
      } else {
        ElMessage.error(data.message)
      }
    } catch (e: any) {
      ElMessage.error(e?.message || '处置提交失败，请重试')
    } finally {
      submitLoading.value = false
    }
  })
}

const resetForm = () => {
  handleFormRef.value?.resetFields()
}

const confirmReinspect = async () => {
  if (!warning.value) return

  reinspectLoading.value = true
  try {
    const { data } = await handleWarning({
      warningId: warning.value.id,
      handleOpinion: warning.value.handleOpinion
        ? `${warning.value.handleOpinion}\n[复检确认] 车辆已完成复检`
        : '[复检确认] 车辆已完成复检',
      reinspectRequired: warning.value.reinspectRequired,
      reinspectDeadline: warning.value.reinspectDeadline,
      status: '已复检'
    })

    if (data.success) {
      ElMessage.success('复检确认成功')
      await loadWarningDetail()
    } else {
      ElMessage.error(data.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '复检确认失败，请重试')
  } finally {
    reinspectLoading.value = false
  }
}

onMounted(() => {
  loadWarningDetail()
})
</script>

<style scoped>
.mono {
  font-family: 'JetBrains Mono', Consolas, monospace;
  letter-spacing: 0.5px;
}

.warning-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.warning-header.level-high {
  border-left: 5px solid #ef4444;
}

.warning-header.level-medium {
  border-left: 5px solid #e6a23c;
}

.warning-header.level-low {
  border-left: 5px solid #409eff;
}

.warning-header-left {
  display: flex;
  align-items: center;
}

.warning-header-right {
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

.handle-form {
  max-width: 700px;
}

.reinspect-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.reinspect-action h2 {
  margin: 0 0 6px;
}
</style>
