<template>
  <RouterLink :to="`/posts/${post.id}`" class="post-card">
    <div class="post-card__media">
      <img v-if="hasMedia && heroMediaIsImage" :src="heroMedia" :alt="post.title" class="post-card__image" />
      <video v-else-if="hasMedia && !heroMediaIsImage" :src="heroMedia" class="post-card__image" preload="metadata" muted playsinline></video>
      
      <div v-else class="text-note-cover">
        <span class="text-note-tag">Text Note</span>
        <h3 class="text-note-title">{{ post.title }}</h3>
      </div>
      
      <span v-if="hasMedia && !heroMediaIsImage" class="media-badge">▶ 视频</span>
    </div>

    <div class="post-card__body">
      <h3 class="post-card__title">{{ post.title }}</h3>
      <div class="post-card__footer">
        <div class="user-inline">
          <img :src="post.authorAvatar" :alt="post.authorNickname" class="avatar avatar--xs" />
          <span>{{ post.authorNickname }}</span>
        </div>
        <span class="like-stat" :class="{ active: post.liked }">
          ♥ {{ post.likeCount || 0 }}
        </span>
      </div>
    </div>
  </RouterLink>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  post: { type: Object, required: true }
})

const heroMedia = computed(() => {
  return props.post.coverImage || props.post.images?.[0] || props.post.mediaUrls?.[0] || ''
})
const hasMedia = computed(() => Boolean(heroMedia.value))

const heroMediaIsImage = computed(() => {
  const lower = (heroMedia.value || '').toLowerCase()
  if (['.mp4', '.mov', '.avi', '.mkv', '.webm', '.m4v'].some((ext) => lower.includes(ext))) {
    return false
  }
  return true
})
</script>