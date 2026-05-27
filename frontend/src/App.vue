<template>
  <div class="layout-shell">
    <AppHeader />
    <router-view />
    <AppFooter />
    
    <AiChatBot />
  </div>
</template>

<script setup>
import AppHeader from './components/AppHeader.vue'
import AppFooter from './components/AppFooter.vue'
import AiChatBot from './components/AiChatBot.vue' // 引入组件
</script>