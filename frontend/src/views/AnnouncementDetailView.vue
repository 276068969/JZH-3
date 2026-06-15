<template>
  <main class="page">
    <header class="topbar">
      <div class="shell topbar-inner">
        <div class="brand" style="cursor: pointer" @click="router.push('/')">
          <span class="brand-mark">环</span>
          <span>机动车尾气监管平台</span>
        </div>
        <nav>
          <el-button text @click="router.push({ name: 'announcement-list' })">公告列表</el-button>
          <el-button text @click="router.push('/')">返回首页</el-button>
        </nav>
      </div>
    </header>

    <section v-loading="loading" class="section">
      <div class="shell">
        <el-empty v-if="!loading && !announcement" description="未找到该公告" />

        <template v-if="announcement">
          <div class="card detail-header">
            <div class="detail-header-left">
              <el-button :icon="ArrowLeft" circle @click="goBack" style="margin-right: 14px" />
              <div>
                <div class="breadcrumb muted" @click="router.push({ name: 'announcement-list' })" style="cursor: pointer">
                  首页 / 政策公告 / 详情
                </div>
                <h1 class="detail-title">{{ announcement.title }}</h1>
                <div class="detail-meta">
                  <el-tag
                    v-if="announcement.type"
                    :type="getTypeTagType(announcement.type)"
                    size="small"
                    effect="plain"
                  >
                    {{ announcement.type }}
                  </el-tag>
                  <span v-if="announcement.publisher" class="meta-item">
                    <el-icon><User /></el-icon>
                    {{ announcement.publisher }}
                  </span>
                  <span v-if="announcement.publishTime" class="meta-item">
                    <el-icon><Clock /></el-icon>
                    {{ formatDate(announcement.publishTime) }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div class="card" style="margin-top: 16px">
            <div class="detail-content" v-html="renderContent(announcement.content)"></div>
          </div>

          <div v-if="prevAnnouncement || nextAnnouncement" class="nav-links" style="margin-top: 16px">
            <div class="card nav-link-item" v-if="prevAnnouncement" @click="goToAnnouncement(prevAnnouncement.id)">
              <div class="nav-link-label muted">上一篇</div>
              <div class="nav-link-title">{{ prevAnnouncement.title }}</div>
            </div>
            <div v-else class="card nav-link-item nav-link-placeholder"></div>

            <div class="card nav-link-item" v-if="nextAnnouncement" @click="goToAnnouncement(nextAnnouncement.id)">
              <div class="nav-link-label muted">下一篇</div>
              <div class="nav-link-title">{{ nextAnnouncement.title }}</div>
            </div>
            <div v-else class="card nav-link-item nav-link-placeholder"></div>
          </div>
        </template>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Clock, User } from '@element-plus/icons-vue'
import { fetchAnnouncementDetail, fetchAnnouncements, type Announcement } from '@/api/platform'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const announcement = ref<Announcement | null>(null)
const allAnnouncements = ref<Announcement[]>([])

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push({ name: 'announcement-list' })
  }
}

const formatDate = (dateStr?: string): string => {
  if (!dateStr) return ''
  if (dateStr.includes(' ')) {
    return dateStr.split(' ')[0]
  }
  return dateStr
}

const getTypeTagType = (type: string) => {
  if (type === '政策' || type === '法规') return 'primary'
  if (type === '通知') return 'warning'
  if (type === '公告') return 'success'
  return 'info'
}

const renderContent = (content?: string): string => {
  if (!content) return '<p style="color: #999">暂无正文内容</p>'
  return content
    .replace(/\n/g, '<br>')
    .replace(/ {2}/g, '&nbsp;&nbsp;')
}

const currentIndex = computed(() => {
  if (!announcement.value) return -1
  return allAnnouncements.value.findIndex(a => a.id === announcement.value!.id)
})

const prevAnnouncement = computed(() => {
  const idx = currentIndex.value
  if (idx <= 0) return null
  return allAnnouncements.value[idx - 1]
})

const nextAnnouncement = computed(() => {
  const idx = currentIndex.value
  if (idx < 0 || idx >= allAnnouncements.value.length - 1) return null
  return allAnnouncements.value[idx + 1]
})

const goToAnnouncement = (id: number) => {
  router.push({ name: 'announcement-detail', params: { id } })
}

const loadDetail = async () => {
  const id = Number(route.params.id)
  if (!id) {
    ElMessage.error('缺少公告编号')
    return
  }
  loading.value = true
  try {
    const { data } = await fetchAnnouncementDetail(id)
    announcement.value = data
  } catch (e: any) {
    if (e?.response?.status === 404) {
      ElMessage.error('未找到该公告')
    } else {
      ElMessage.error(e?.message || '加载公告失败')
    }
    announcement.value = null
  } finally {
    loading.value = false
  }
}

const loadAllAnnouncements = async () => {
  try {
    const { data } = await fetchAnnouncements()
    allAnnouncements.value = data
  } catch {
    // ignore
  }
}

onMounted(async () => {
  await Promise.all([loadDetail(), loadAllAnnouncements()])
})

watch(() => route.params.id, () => {
  if (route.name === 'announcement-detail' && route.params.id) {
    loadDetail()
  }
})
</script>

<style scoped>
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.detail-header-left {
  display: flex;
  align-items: flex-start;
}

.breadcrumb {
  font-size: 13px;
}

.detail-title {
  margin: 8px 0 10px;
  font-size: 22px;
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #66758a;
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.detail-content {
  font-size: 15px;
  line-height: 1.9;
  color: #2c3e50;
  word-break: break-word;
}

.nav-links {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.nav-link-item {
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.nav-link-item:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.12);
}

.nav-link-placeholder {
  visibility: hidden;
}

.nav-link-label {
  font-size: 12px;
  margin-bottom: 6px;
}

.nav-link-title {
  font-size: 14px;
  font-weight: 500;
  color: #172033;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
