<template>
  <main class="container page-main">
    <TagFilterBar v-model="filters" :tags="tags" @search="search" />

    <div v-if="posts.length" class="post-grid">
      <PostCard v-for="item in posts" :key="item.id" :post="item" />
    </div>

    <div v-if="loading" class="empty-state">内容加载中...</div>
    <div v-else-if="!posts.length" class="empty-state">还没有内容，去发布第一篇吧</div>

    <div v-if="pageData.totalPages > 1" class="pagination-bar">
      <button class="button button--ghost" :disabled="pageData.page <= 0" @click="changePage(pageData.page - 1)">上一页</button>
      <span style="font-size: 14px; color: #666;">第 {{ pageData.page + 1 }} / {{ pageData.totalPages }} 页</span>
      <button class="button button--ghost" :disabled="pageData.page >= pageData.totalPages - 1" @click="changePage(pageData.page + 1)">下一页</button>
    </div>
  </main>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { fetchPosts, fetchTags } from '../api/bbook'
import PostCard from '../components/PostCard.vue'
import TagFilterBar from '../components/TagFilterBar.vue'

const tags = ref([])
const posts = ref([])
const loading = ref(false)
const filters = ref({ keyword: '', tagCode: '' })
const pageData = ref({ records: [], page: 0, totalPages: 0, total: 0 })

onMounted(async () => {
  const [tagRes] = await Promise.all([fetchTags(), loadPosts()])
  tags.value = tagRes.data || []
})

async function loadPosts(page = 0) {
  loading.value = true
  try {
    const res = await fetchPosts({ ...filters.value, page, size: 12 })
    pageData.value = res.data
    posts.value = res.data.records || []
  } catch (error) {
    alert(error.message)
  } finally {
    loading.value = false
  }
}

function search(payload) {
  filters.value = payload
  loadPosts(0)
}

function changePage(page) {
  loadPosts(page)
}
</script>