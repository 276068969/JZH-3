<template>
  <el-drawer
    v-model="visible"
    :title="station?.stationName || '检测站详情'"
    direction="rtl"
    size="480px"
    :before-close="handleClose"
  >
    <div v-if="station" class="station-detail">
      <div class="status-header">
        <el-tag :type="getStatusType(station.runningStatus)" effect="dark" size="large">
          {{ station.runningStatus }}
        </el-tag>
      </div>

      <div class="info-section">
        <h3 class="section-title">基本信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="检测站名称">
            {{ station.stationName }}
          </el-descriptions-item>
          <el-descriptions-item label="辖区">
            {{ station.district }}
          </el-descriptions-item>
          <el-descriptions-item label="地址">
            {{ station.address }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            <a :href="'tel:' + station.phone" class="phone-link">
              {{ station.phone }}
            </a>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="info-section">
        <h3 class="section-title">今日检测概况</h3>
        <div class="stat-cards">
          <div class="stat-card total">
            <div class="stat-value">{{ station.todayInspectionCount }}</div>
            <div class="stat-label">今日检测量</div>
          </div>
          <div class="stat-card passed">
            <div class="stat-value">{{ station.passedCount }}</div>
            <div class="stat-label">合格数</div>
          </div>
          <div class="stat-card failed">
            <div class="stat-value">{{ station.failedCount }}</div>
            <div class="stat-label">不合格数</div>
          </div>
        </div>

        <div class="pass-rate-section">
          <div class="pass-rate-header">
            <span>合格率</span>
            <span class="pass-rate-value">{{ station.passRate }}%</span>
          </div>
          <el-progress
            :percentage="station.passRate"
            :color="getProgressColor(station.passRate)"
            :stroke-width="12"
          />
        </div>
      </div>

      <div class="info-section">
        <h3 class="section-title">其他信息</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="最近检测时间">
            {{ station.lastInspectionTime || '暂无检测记录' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>

    <div v-else class="loading-placeholder">
      <el-skeleton :rows="8" animated />
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { StationStatus } from '@/api/platform'

interface Props {
  modelValue: boolean
  station: StationStatus | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const handleClose = () => {
  emit('update:modelValue', false)
}

const getStatusType = (status: string) => {
  if (status === '运行中') return 'success'
  if (status === '空闲') return 'warning'
  if (status === '停运') return 'danger'
  return 'info'
}

const getProgressColor = (rate: number) => {
  if (rate >= 90) return '#67c23a'
  if (rate >= 70) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped>
.station-detail {
  padding: 0 8px;
}

.status-header {
  margin-bottom: 20px;
}

.info-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #303133;
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  padding: 16px 12px;
  border-radius: 8px;
  text-align: center;
  background: #f5f7fa;
}

.stat-card.total {
  background: linear-gradient(135deg, #409eff15, #409eff08);
}

.stat-card.passed {
  background: linear-gradient(135deg, #67c23a15, #67c23a08);
}

.stat-card.failed {
  background: linear-gradient(135deg, #f56c6c15, #f56c6c08);
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 4px;
}

.stat-card.passed .stat-value {
  color: #67c23a;
}

.stat-card.failed .stat-value {
  color: #f56c6c;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.pass-rate-section {
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.pass-rate-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;
  color: #606266;
}

.pass-rate-value {
  font-size: 20px;
  font-weight: 600;
  color: #67c23a;
}

.phone-link {
  color: #409eff;
  text-decoration: none;
}

.phone-link:hover {
  text-decoration: underline;
}

.loading-placeholder {
  padding: 0 8px;
}
</style>
