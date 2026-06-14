<template>
  <main class="page login-page">
    <section class="login-panel">
      <div class="brand login-brand">
        <span class="brand-mark">环</span>
        <span>机动车尾气监管平台</span>
      </div>
      <el-form class="login-form" label-position="top" @submit.prevent>
        <el-form-item label="账号">
          <el-input v-model="username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="password" type="password" show-password autocomplete="current-password" />
        </el-form-item>
        <el-alert v-if="error" :title="error" type="error" show-icon />
        <el-button type="primary" size="large" :loading="loading" @click="submit">登录</el-button>
      </el-form>
      <p class="muted">测试账号：admin / regulator / station / user，密码均为 123456</p>
    </section>
  </main>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const username = ref('admin')
const password = ref('123456')
const loading = ref(false)
const error = ref('')

const submit = async () => {
  loading.value = true
  error.value = ''
  try {
    await auth.signIn(username.value, password.value)
    await router.push(auth.homeRoute)
  } catch {
    error.value = '账号或密码不正确'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: grid;
  place-items: center;
  padding: 32px;
}

.login-panel {
  width: min(420px, 100%);
  background: #fff;
  border: 1px solid #dfe7f2;
  border-radius: 8px;
  padding: 28px;
}

.login-brand {
  margin-bottom: 24px;
}

.login-form {
  display: grid;
  gap: 4px;
}
</style>
