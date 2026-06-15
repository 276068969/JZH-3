<template>
  <main class="page">
    <header class="topbar">
      <div class="shell topbar-inner">
        <div class="brand">
          <span class="brand-mark">环</span>
          <span>机动车尾气监管平台</span>
        </div>
        <nav>
          <el-button text @click="router.push('/')">首页</el-button>
          <el-button type="primary" text @click="router.push('/vehicle-center')">
            个人车辆中心
          </el-button>
          <template v-if="auth.isLoggedIn && (auth.isAdmin || auth.isRegulator || auth.isStation)">
            <el-button type="primary" @click="goToAdmin">管理后台</el-button>
          </template>
          <el-dropdown @command="handleDropdownCommand">
            <span class="user-info">
              <el-avatar :size="28" style="background-color: #409EFF; margin-right: 8px">
                {{ auth.user?.displayName?.charAt(0) || '用' }}
              </el-avatar>
              {{ auth.user?.displayName }}
              <el-icon><arrow-down /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="home">返回首页</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </nav>
      </div>
    </header>

    <section class="hero vehicle-hero">
      <div class="shell">
        <h1>个人车辆中心</h1>
        <p>
          {{ auth.user?.displayName }}，您好！这里集中展示您名下所有车辆的环保信息与检测记录。
        </p>
      </div>
    </section>

    <section class="section">
      <div class="shell">
        <div class="grid grid-3 stats-grid">
          <div class="card stat-card">
            <div class="stat-icon icon-total">
              <el-icon :size="28"><van /></el-icon>
            </div>
            <div class="stat-content">
              <div class="muted">名下车辆</div>
              <div class="stat-value">{{ centerData?.totalVehicles || 0 }}</div>
            </div>
          </div>
          <div class="card stat-card">
            <div class="stat-icon icon-success">
              <el-icon :size="28"><circle-check /></el-icon>
            </div>
            <div class="stat-content">
              <div class="muted">环保合格</div>
              <div class="stat-value text-success">{{ centerData?.qualifiedCount || 0 }}</div>
            </div>
          </div>
          <div class="card stat-card">
            <div class="stat-icon icon-warning">
              <el-icon :size="28"><warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="muted">待复检</div>
              <div class="stat-value text-warning">{{ centerData?.pendingCount || 0 }}</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="section">
      <div class="shell">
        <div v-if="loading" class="card loading-card">
          <el-skeleton :rows="8" animated />
        </div>

        <template v-else-if="centerData && centerData.vehicles.length > 0">
          <div
            v-for="(item, idx) in centerData.vehicles"
            :key="item.vehicle.plateNumber"
            class="card vehicle-card"
          >
            <div class="vehicle-header">
              <div class="vehicle-title">
                <span class="plate-badge">{{ item.vehicle.plateNumber }}</span>
                <el-tag
                  :type="getEnvStatusType(item.vehicle.environmentalStatus)"
                  effect="dark"
                  size="large"
                >
                  {{ item.vehicle.environmentalStatus }}
                </el-tag>
                <el-tag v-if="item.latestInspection" type="info" effect="plain" size="large">
                  最近检测：{{ item.latestInspection.inspectionTime }}
                </el-tag>
              </div>
              <div class="vehicle-stats">
                <div class="mini-stat">
                  <span class="mini-label">检测次数</span>
                  <span class="mini-value">{{ item.inspectionCount }}</span>
                </div>
                <div class="mini-stat">
                  <span class="mini-label">合格</span>
                  <span class="mini-value text-success">{{ item.passedCount }}</span>
                </div>
                <div class="mini-stat">
                  <span class="mini-label">不合格</span>
                  <span class="mini-value text-danger">{{ item.failedCount }}</span>
                </div>
              </div>
            </div>

            <div class="vehicle-body">
              <div class="vehicle-info-section">
                <h3>车辆基本信息</h3>
                <el-descriptions :column="2" border size="default">
                  <el-descriptions-item label="VIN">{{ item.vehicle.vin }}</el-descriptions-item>
                  <el-descriptions-item label="车辆类型">{{ item.vehicle.vehicleType }}</el-descriptions-item>
                  <el-descriptions-item label="燃料类型">{{ item.vehicle.fuelType }}</el-descriptions-item>
                  <el-descriptions-item label="排放标准">{{ item.vehicle.emissionStandard }}</el-descriptions-item>
                  <el-descriptions-item label="注册日期">{{ item.vehicle.registerDate }}</el-descriptions-item>
                  <el-descriptions-item label="车主">{{ item.vehicle.owner }}</el-descriptions-item>
                </el-descriptions>
              </div>

              <div v-if="item.latestInspection" class="latest-section">
                <h3>最近一次检测</h3>
                <div class="latest-card">
                  <div class="latest-grid">
                    <div class="latest-item">
                      <span class="muted">检测编号</span>
                      <span class="mono">{{ item.latestInspection.inspectionNo }}</span>
                    </div>
                    <div class="latest-item">
                      <span class="muted">检测站</span>
                      <span>{{ item.latestInspection.stationName }}</span>
                    </div>
                    <div class="latest-item">
                      <span class="muted">检测时间</span>
                      <span>{{ item.latestInspection.inspectionTime }}</span>
                    </div>
                    <div class="latest-item">
                      <span class="muted">检测结果</span>
                      <el-tag
                        :type="item.latestInspection.result === '合格' ? 'success' : 'danger'"
                        effect="dark"
                      >
                        {{ item.latestInspection.result }}
                      </el-tag>
                    </div>
                    <div class="latest-item">
                      <span class="muted">CO 浓度</span>
                      <span :class="getPollutantClass(item.latestInspection, 'CO')">
                        {{ item.latestInspection.coValue }}%
                      </span>
                    </div>
                    <div class="latest-item">
                      <span class="muted">HC 浓度</span>
                      <span :class="getPollutantClass(item.latestInspection, 'HC')">
                        {{ item.latestInspection.hcValue }} ppm
                      </span>
                    </div>
                    <div class="latest-item">
                      <span class="muted">NOx 浓度</span>
                      <span :class="getPollutantClass(item.latestInspection, 'NOx')">
                        {{ item.latestInspection.noxValue }} ppm
                      </span>
                    </div>
                    <div class="latest-item">
                      <span class="muted">烟度值</span>
                      <span :class="getPollutantClass(item.latestInspection, 'opacity')">
                        {{ item.latestInspection.opacityValue }} m⁻¹
                      </span>
                    </div>
                    <div class="latest-item">
                      <span class="muted">报告状态</span>
                      <el-tag :type="getStatusType(item.latestInspection.reportStatus)" effect="plain">
                        {{ item.latestInspection.reportStatus }}
                      </el-tag>
                    </div>
                    <div class="latest-item">
                      <span class="muted">检测人员</span>
                      <span>{{ item.latestInspection.inspector }}</span>
                    </div>
                  </div>
                  <div class="latest-actions">
                    <el-button
                      type="primary"
                      size="large"
                      @click="viewReportDetail(item.latestInspection)"
                    >
                      查看完整检测报告
                    </el-button>
                  </div>
                </div>
              </div>
              <el-empty v-else description="暂无检测记录" :image-size="80" />

              <div v-if="item.recentInspections && item.recentInspections.length > 0" class="history-section">
                <div class="section-header">
                  <h3>最近检测记录</h3>
                  <el-button type="primary" link @click="showAllInspections(item)">
                    查看全部（{{ item.inspectionCount }}条）
                  </el-button>
                </div>
                <el-table :data="item.recentInspections" border stripe>
                  <el-table-column prop="inspectionNo" label="检测编号" min-width="160">
                    <template #default="{ row }">
                      <span class="mono">{{ row.inspectionNo }}</span>
                    </template>
                  </el-table-column>
                  <el-table-column prop="inspectionTime" label="检测时间" min-width="160" />
                  <el-table-column prop="stationName" label="检测站" min-width="200" />
                  <el-table-column prop="result" label="结果" width="100">
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
                  <el-table-column label="排放数据" min-width="260">
                    <template #default="{ row }">
                      <div class="pollutant-row">
                        <span :title="'CO: ' + row.coValue + '%'">CO {{ row.coValue }}%</span>
                        <el-divider direction="vertical" />
                        <span :title="'HC: ' + row.hcValue + ' ppm'">HC {{ row.hcValue }}</span>
                        <el-divider direction="vertical" />
                        <span :title="'NOx: ' + row.noxValue + ' ppm'">NOx {{ row.noxValue }}</span>
                      </div>
                    </template>
                  </el-table-column>
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
          </div>
        </template>

        <div v-else class="card empty-card">
          <el-empty
            description="您的名下暂无登记车辆"
            :image-size="120"
          >
            <template #description>
              <div class="empty-desc">
                <p>您的账户名下暂未查询到登记车辆信息</p>
                <p class="muted" style="font-size: 13px">
                  如有疑问，请联系当地车管所或拨打环保服务热线咨询
                </p>
              </div>
            </template>
            <el-button type="primary" @click="router.push('/')">返回首页查询</el-button>
          </el-empty>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  Van,
  CircleCheck,
  Warning
} from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import {
  fetchUserVehicleCenter,
  type InspectionRecord,
  type UserVehicleCenter
} from '@/api/platform'

const router = useRouter()
const auth = useAuthStore()

const centerData = ref<UserVehicleCenter | null>(null)
const loading = ref(true)

const goToAdmin = () => {
  router.push(auth.homeRoute)
}

const handleDropdownCommand = async (command: string) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      auth.signOut()
      ElMessage.success('已退出登录')
      router.push('/')
    } catch {
      // 用户取消
    }
  } else if (command === 'home') {
    router.push('/')
  }
}

const getEnvStatusType = (status: string) => {
  if (status === '合格') return 'success'
  if (status === '待复检') return 'warning'
  return 'danger'
}

const getStatusType = (status: string) => {
  if (status === '已审核') return 'primary'
  if (status === '待审核') return 'warning'
  if (status === '已退回') return 'danger'
  return 'info'
}

const getPollutantClass = (_record: InspectionRecord, _type: string) => {
  return ''
}

const showAllInspections = (_item: any) => {
  ElMessage.info('全部检测记录功能开发中')
}

const viewReportDetail = (row: InspectionRecord) => {
  router.push({ name: 'report-detail', params: { inspectionNo: row.inspectionNo } })
}

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await fetchUserVehicleCenter()
    centerData.value = data
  } catch (err: any) {
    if (err?.response?.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      auth.signOut()
      router.push('/login')
    } else {
      ElMessage.error('加载个人车辆信息失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (!auth.isLoggedIn) {
    ElMessage.warning('请先登录后访问个人车辆中心')
    router.push('/login')
    return
  }
  loadData()
})
</script>

<style scoped>
.vehicle-hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.vehicle-hero h1,
.vehicle-hero p {
  color: #fff;
}

.stats-grid {
  margin-top: -30px;
  position: relative;
  z-index: 1;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px 24px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.icon-total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.icon-success {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
}

.icon-warning {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
  margin-top: 4px;
}

.text-success {
  color: #67c23a;
}

.text-warning {
  color: #e6a23c;
}

.text-danger {
  color: #f56c6c;
}

.vehicle-card {
  margin-bottom: 24px;
  padding: 0;
  overflow: hidden;
}

.vehicle-header {
  padding: 20px 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 16px;
}

.vehicle-title {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.plate-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 18px;
  background: linear-gradient(180deg, #1e3a8a 0%, #1e40af 100%);
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  border-radius: 6px;
  letter-spacing: 2px;
  font-family: 'Microsoft YaHei', sans-serif;
  box-shadow: 0 2px 8px rgba(30, 58, 138, 0.25);
  border: 2px solid #fbbf24;
}

.vehicle-stats {
  display: flex;
  gap: 24px;
}

.mini-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 4px 16px;
  background: #fff;
  border-radius: 8px;
  min-width: 70px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.mini-label {
  font-size: 12px;
  color: #909399;
}

.mini-value {
  font-size: 20px;
  font-weight: 700;
  color: #303133;
}

.vehicle-body {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.vehicle-info-section h3,
.latest-section h3,
.history-section .section-header h3 {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  padding-left: 10px;
  border-left: 4px solid #409eff;
  line-height: 1.2;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-header h3 {
  margin: 0;
}

.latest-card {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 20px;
}

.latest-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px 24px;
  margin-bottom: 16px;
}

.latest-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.latest-item .muted {
  font-size: 12px;
}

.latest-item span:not(.muted) {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.mono {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

.latest-actions {
  padding-top: 16px;
  border-top: 1px dashed #e2e8f0;
  display: flex;
  justify-content: flex-end;
}

.pollutant-row {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
}

.loading-card {
  padding: 24px;
}

.empty-card {
  padding: 48px 24px;
  text-align: center;
}

.empty-desc p {
  margin: 6px 0;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #303133;
  margin-left: 12px;
}

.user-info:hover {
  color: #409eff;
}

.muted {
  color: #909399;
  font-size: 13px;
}
</style>
