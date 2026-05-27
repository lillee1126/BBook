<template>
  <header class="app-header">
    <div class="container app-header__inner">
      
      <RouterLink to="/" class="brand-wrap">
        <span class="brand-badge">书</span>
        <div class="brand-title-group">
          <div class="brand">小蓝书</div>
          <div class="brand-slogan-box">
            <span class="slogan-text-main">让内容更好看，让分享更轻松。</span>
            <span class="slogan-text-tags">发布 · 浏览 · 互动</span>
          </div>
        </div>
      </RouterLink>

      <nav class="nav-links">
        <RouterLink to="/">发现</RouterLink>
        <RouterLink v-if="auth.user" :to="`/users/${auth.user.id}`">我的</RouterLink>
      </nav>

      <div class="header-user-actions">
        <template v-if="auth.isLoggedIn">
          <RouterLink to="/publish" class="button publish-btn-header">+ 发布</RouterLink>
          <RouterLink :to="`/users/${auth.user.id}`" class="header-avatar-link">
            <img :src="auth.user.avatar" class="avatar-header" :alt="auth.user.nickname" />
          </RouterLink>
          <button class="logout-text-btn" @click="logout">退出</button>
        </template>
        <template v-else>
          <button class="auth-text-btn" @click="open('login')">登录</button>
          <button class="button publish-btn-header" @click="open('register')">注册</button>
        </template>
      </div>

    </div>
  </header>

  <AuthModal :visible="showAuth" :initial-mode="authMode" @close="showAuth = false" @success="afterAuth" />
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AuthModal from './AuthModal.vue'

const router = useRouter()
const auth = useAuthStore()
const showAuth = ref(false)
const authMode = ref('login')

onMounted(() => {
  auth.refreshMe()
})

function open(mode) {
  authMode.value = mode
  showAuth.value = true
}

function afterAuth() {
  showAuth.value = false
  if (auth.user?.id) {
    router.push(`/users/${auth.user.id}`)
  }
}

function logout() {
  auth.clearAuth()
  router.push('/')
}
</script>