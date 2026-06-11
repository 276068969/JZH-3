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
          <el-input v-model="keyword" size="large" placeholder="输入车牌号、VIN 或检测报告编号" clearable />
          <el-button type="primary" size="large" :loading="loading" @click="queryVehicle">查询</el-button>
          <el-alert v-if="notFound" title="未找到匹配车辆" type="warning" show-icon />
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
            <el-timeline-item v-for="item in announcements" :key="item.id" :timestamp="item.publishDate">
              {{ item.title }}
            </el-timeline-item>
          </el-timeline>
        </div>
      </div>
    </section>

    <section class="section" ref="stationSection">
      <div class="shell">
        <h2>检测站信息</h2>
        <el-table :data="stations" border>
          <el-table-column prop="name" label="检测站" min-width="160" />
          <el-table-column prop="district" label="辖区" width="120" />
          <el-table-column prop="address" label="地址" min-width="220" />
          <el-table-column prop="phone" label="电话" width="150" />
          <el-table-column prop="status" label="状态" width="100" />
        </el-table>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchAnnouncements, fetchStations, searchVehicle, type Vehicle } from '@/api/platform'

const router = useRouter()
const keyword = ref('京A12345')
const vehicle = ref<Vehicle | null>(null)
const loading = ref(false)
const notFound = ref(false)
const stations = ref([])
const announcements = ref([])
const stationSection = ref<HTMLElement | null>(null)

const queryVehicle = async () => {
  loading.value = true
  notFound.value = false
  try {
    const { data } = await searchVehicle(keyword.value)
    vehicle.value = data
  } catch {
    vehicle.value = null
    notFound.value = true
  } finally {
    loading.value = false
  }
}

const scrollToStations = () => stationSection.value?.scrollIntoView({ behavior: 'smooth' })

onMounted(async () => {
  const [stationResp, announcementResp] = await Promise.all([fetchStations(), fetchAnnouncements()])
  stations.value = stationResp.data
  announcements.value = announcementResp.data
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
</style>
