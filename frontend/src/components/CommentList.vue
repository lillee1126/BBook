<template>
  <div class="comment-area">
    <div style="color: #555; font-size: 14px; margin-bottom: 16px;">共 {{ comments.length }} 条评论</div>

    <div v-if="!comments.length" style="color: #999; font-size: 13px; text-align: center; padding: 40px 0;">
      还没有评论，快来抢沙发～
    </div>

    <div v-for="item in comments" :key="item.id" class="comment-item" style="display: flex; gap: 12px; margin-bottom: 20px;">
      <img :src="item.avatar" :alt="item.nickname" class="avatar avatar--sm" />
      <div style="flex: 1; border-bottom: 1px solid #f0f0f0; padding-bottom: 16px;">
        <div style="display: flex; justify-content: space-between;">
          <strong style="color: #555; font-size: 13px;">{{ item.nickname }}</strong>
          <div style="display: flex; gap: 16px; color: #999; font-size: 12px;">
            <span style="cursor: pointer;" :style="{ color: item.liked ? '#ff4d4f' : '#999' }" @click="handleLike(item)">
              ♥ {{ item.likeCount || 0 }}
            </span>
            <span v-if="canDelete(item)" style="cursor: pointer;" @click="handleDelete(item)">删除</span>
          </div>
        </div>
        <p style="margin: 6px 0 8px; font-size: 14px; color: #333;">{{ item.content }}</p>
        <div style="font-size: 12px; color: #999;">{{ formatDate(item.createdAt) }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { deleteComment, likeComment } from '../api/bbook'
import { useAuthStore } from '../stores/auth'

const props = defineProps({
  comments: { type: Array, default: () => [] },
  postId: { type: String, default: '' },
  postAuthorId: { type: String, default: '' }
})
const emit = defineEmits(['changed'])
const authStore = useAuthStore()

async function handleLike(item) {
  try {
    const res = await likeComment(props.postId, item.id)
    item.likeCount = res.data.likeCount
    item.liked = res.data.liked
  } catch (error) { alert(error.message) }
}

async function handleDelete(item) {
  if (!confirm('确认删除？')) return
  try {
    await deleteComment(props.postId, item.id)
    emit('changed')
  } catch (error) { alert(error.message) }
}

function canDelete(item) {
  const uid = authStore.user?.id
  return Boolean(uid && (uid === item.userId || uid === props.postAuthorId))
}

// 时间格式化为 年-月-日 时:分
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
</script>