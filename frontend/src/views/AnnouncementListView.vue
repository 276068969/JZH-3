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
        </nav>
      </div>
    </header>

    <section class="section">
      <div class="shell">
        <div class="card list-header">
          <div class="list-header-left">
            <el-button :icon="ArrowLeft" circle @click="goBack" style="margin-right: 14px" />
            <div>
              <div class="breadcrumb muted" @click="router.push('/')" style="cursor: pointer">
                首页 / 政策公告
              </div>
              <h1 style="margin: 6px 0 0; font-size: 24px">政策公告</h1>
            </div>
          </div>
        </div>

        <div v-loading="loading" style="margin-top: 16px">
          <el-empty v-if="!loading && announcements.length === 0" description="暂无公告" />

          <div v-else class="announcement-list">
            <div
              v-for="item in announcements"
              :key="item.id"
              class="card announcement-item"
              @click="router.push({ name: 'announcement-detail', params: { id: item.id } })"
            >
              <div class="announcement-item-main">
                <div class="announcement-item-title">
                  <el-tag
                    v-if="item.type"
                    :type="getTypeTagType(item.type)"
                    size="small"
                    effect="plain"
                    style="margin-right: 10px; flex-shrink: 0"
                  >
                    {{ item.type }}
                  </el-tag>
                  <span class="title-text">{{ item.title }}</span>
                </div>
                <div class="announcement-item-meta">
                  <span v-if="item.publisher" class="meta-item">
                    <el-icon><User /></el-icon>
                    {{ item.publisher }}
                  </span>
                  <span v-if="item.publishTime" class="meta-item">
                    <el-icon><Clock /></el-icon>
                    {{ formatDate(item.publishTime) }}
                  </span>
                </div>
              </div>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, Clock, User } from '@element-plus/icons-vue'
import { fetchAnnouncements, type Announcement } from '@/api/platform'

const router = useRouter()
const loading = ref(false)
const announcements = ref<Announcement[]>([])

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
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

onMounted(async () => {
  loading.value = true
  try {
    const { data } = await fetchAnnouncements()
    announcements.value = data
  } catch {
    ElMessage.warning('公告加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
}

.list-header-left {
  display: flex;
  align-items: center;
}

.breadcrumb {
  font-size: 13px;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  cursor: pointer;
  transition: box-shadow 0.2s, border-color 0.2s;
}

.announcement-item:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.12);
}

.announcement-item-main {
  flex: 1;
  min-width: 0;
}

.announcement-item-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #172033;
  margin-bottom: 8px;
}

.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.announcement-item-meta {
  display: flex;
  gap: 18px;
  color: #66758a;
  font-size: 13px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.arrow-icon {
  color: #c0c4cc;
  font-size: 16px;
  margin-left: 12px;
  flex-shrink: 0;
}

.announcement-item:hover .arrow-icon {
  color: #409EFF;
}
</style>
