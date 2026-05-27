<template>
  <main class="container page-main page-main--full" v-if="detail">
    <div class="detail-modal-container detail-modal-container--full">
      
      <div class="detail-left-media">
        <template v-if="displayMedia.length">
          <div class="carousel-track-immersive" ref="carouselTrack" @scroll="onCarouselScroll">
            <div v-for="(item, index) in displayMedia" :key="index" class="carousel-slide-immersive">
              <img v-if="isImage(item)" :src="item" class="carousel-media-immersive" />
              <video v-else :src="item" class="carousel-media-immersive" controls preload="metadata"></video>
            </div>
          </div>
          <button v-if="displayMedia.length > 1" class="carousel-btn prev" @click="scrollPrev">‹</button>
          <button v-if="displayMedia.length > 1" class="carousel-btn next" @click="scrollNext">›</button>
        </template>
        
        <div v-else class="text-note-cover" style="height: 100%; border-radius: 0;">
          <span class="text-note-tag" style="top: 24px; left: 24px; font-size: 14px;">Text Note</span>
          <h3 class="text-note-title" style="font-size: 32px; padding: 40px;">{{ detail.title }}</h3>
        </div>
      </div>

      <div class="detail-right-content">
        
        <div class="detail-header sticky-header">
          <RouterLink :to="`/users/${detail.authorId}`" class="user-inline" style="font-size: 15px; color: #333; font-weight: 600;">
            <img :src="detail.authorAvatar" class="avatar avatar--sm" />
            <span>{{ detail.authorNickname }}</span>
          </RouterLink>
          <button v-if="isMine" class="button button--ghost" style="padding: 6px 16px;" @click="handleDeletePost">删除</button>
          <button v-else class="button" style="padding: 6px 16px;">关注</button>
        </div>

        <div class="detail-scroll-area">
          <h1 class="detail-title">{{ detail.title }}</h1>
          <div class="detail-text">{{ detail.content }}</div>
          
          <div v-if="detail.topics?.length" class="detail-tags">
            <span v-for="topic in detail.topics" :key="topic" class="topic-tag">#{{ topic }}</span>
          </div>
          
          <div class="detail-date">{{ formatDate(detail.createdAt) }} {{ authorLocation }}</div>
          
          <div class="detail-divider"></div>
          
          <CommentList :comments="detail.comments" :post-id="detail.id" :post-author-id="detail.authorId" @changed="loadDetail" />
        </div>

        <div class="detail-bottom-bar">
          <div class="comment-input-wrap">
            <input v-model.trim="commentText" placeholder="说点什么..." @keyup.enter="submitComment" />
          </div>
          <div class="action-icons">
            <div class="action-item" :class="{ active: detail.liked }" @click="handleLike">
              ♥ <span>{{ detail.likeCount || '赞' }}</span>
            </div>
            <div class="action-item">
              💬 <span>{{ detail.commentCount || '评论' }}</span>
            </div>
          </div>
        </div>

      </div>
    </div>
  </main>

  <main class="container page-main" v-else>
    <div class="empty-state">笔记加载中...</div>
  </main>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { deletePost, likePost, fetchPostDetail, postComment, fetchUserHome } from '../api/bbook'
import { useAuthStore } from '../stores/auth'
import CommentList from '../components/CommentList.vue'

const props = defineProps({ id: { type: String, required: true } })
const router = useRouter()
const authStore = useAuthStore()
const detail = ref(null)
const commentText = ref('')
const authorLocation = ref('') 

// 轮播控制变量
const carouselTrack = ref(null)

// 依次检查 mediaUrls -> images -> coverImage
const displayMedia = computed(() => {
  const media = detail.value?.mediaUrls?.filter(Boolean) || []
  if (media.length > 0) return media

  const images = detail.value?.images?.filter(Boolean) || []
  if (images.length > 0) return images

  if (detail.value?.coverImage) return [detail.value.coverImage]

  return []
})

const isMine = computed(() => authStore.user?.id === detail.value?.authorId)

onMounted(loadDetail)
watch(() => props.id, loadDetail)

async function loadDetail() {
  try {
    const res = await fetchPostDetail(props.id)
    detail.value = res.data
    
    // 异步查询作者归属地归属地归属地归属地
    try {
      const userRes = await fetchUserHome(detail.value.authorId)
      authorLocation.value = userRes.data.user.location || '未知地区'
    } catch(e) {
      authorLocation.value = '未知地区'
    }
  } catch (error) { alert(error.message) }
}

// 轮播切换函数
function scrollPrev() {
  if (carouselTrack.value) {
    carouselTrack.value.scrollBy({ left: -carouselTrack.value.clientWidth, behavior: 'smooth' })
  }
}
function scrollNext() {
  if (carouselTrack.value) {
    carouselTrack.value.scrollBy({ left: carouselTrack.value.clientWidth, behavior: 'smooth' })
  }
}

async function handleLike() {
  try {
    const res = await likePost(props.id)
    detail.value.likeCount = res.data.likeCount
    detail.value.liked = res.data.liked
  } catch (error) { alert(error.message) }
}

async function submitComment() {
  if (!commentText.value) return
  try {
    await postComment(props.id, { content: commentText.value })
    commentText.value = ''
    await loadDetail()
  } catch (error) { alert(error.message) }
}

async function handleDeletePost() {
  if (!confirm('确认删除这篇笔记吗？')) return
  try {
    await deletePost(props.id)
    router.push('/')
  } catch (error) { alert(error.message) }
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const h = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${h}:${min}`
}

function isImage(url) {
  const lower = (url || '').toLowerCase()
  if (['.mp4', '.mov', '.avi', '.mkv', '.webm', '.m4v'].some((ext) => lower.includes(ext))) {
    return false
  }
  return true
}
</script>