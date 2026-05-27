<template>
  <main class="container page-main" v-if="profile">
    <section class="profile-hero card">
      <img :src="profile.user.avatar" :alt="profile.user.nickname" class="avatar avatar--xl" />
      <div class="profile-info">
        <h1>{{ profile.user.nickname }}</h1>
        <p>@{{ profile.user.username }}</p>
        <p>{{ profile.user.bio || '还没有填写简介' }}</p>
        <div class="stats-inline stats-inline--large">
          <span>城市：{{ profile.user.location || '未设置' }}</span>
          <span>发布：{{ profile.user.postCount }}</span>
          <span>获赞：{{ profile.user.totalLikes }}</span>
        </div>
        <button v-if="isMine" class="button button--ghost edit-trigger-btn" @click="openEditModal">
          编辑个人信息
        </button>
      </div>
    </section>

    <section class="section-title profile-title">
      <h2>{{ isMine ? '我发布的内容' : 'TA 发布的内容' }}</h2>
      <span>{{ profile.posts.length }} 篇</span>
    </section>

    <section class="post-grid">
      <PostCard v-for="item in profile.posts" :key="item.id" :post="item" />
    </section>

    <div v-if="openEditor" class="custom-profile-modal-mask" @click.self="closeEditModal">
      <div class="custom-profile-modal-window">
        <div class="modal-top-bar">
          <h3>修改个人资料</h3>
          <button type="button" class="modal-close-btn" @click="closeEditModal">×</button>
        </div>

        <form class="modal-center-form" @submit.prevent="submitProfile">
          <div class="modal-input-group">
            <label>昵称</label>
            <input v-model.trim="form.nickname" class="input" placeholder="输入新的昵称" />
          </div>

          <div class="modal-input-group">
            <label>个人简介</label>
            <textarea v-model.trim="form.bio" class="textarea" rows="3" placeholder="介绍一下自己吧..." />
          </div>

          <div class="modal-input-group">
            <label>现居城市</label>
            <input v-model.trim="form.location" class="input" placeholder="例如：杭州、北京" />
          </div>

          <div class="modal-input-group">
            <label>更换头像</label>
            <div class="modal-avatar-row">
              <input v-model.trim="form.avatar" class="input shadow-input" placeholder="图片 URL 地址" />
              <div class="modal-upload-action">
                <span>本地上传</span>
                <input type="file" accept="image/*" @change="handleAvatarUpload" />
              </div>
            </div>
          </div>

          <div class="modal-bottom-actions">
            <button type="button" class="button button--ghost btn-cancel" @click="closeEditModal">取消</button>
            <button type="submit" class="button btn-save" :disabled="saving">
              {{ saving ? '正在保存...' : '保存资料' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </main>

  <main class="container page-main" v-else>
    <div class="card empty-state">主页加载中...</div>
  </main>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { fetchUserHome, updateMyProfile, uploadLocalFile } from '../api/bbook'
import { useAuthStore } from '../stores/auth'
import PostCard from '../components/PostCard.vue'

const props = defineProps({
  id: { type: String, required: true }
})

const authStore = useAuthStore()
const profile = ref(null)
const openEditor = ref(false) 
const saving = ref(false)

const form = reactive({
  nickname: '',
  avatar: '',
  bio: '',
  location: ''
})

const isMine = computed(() => authStore.user?.id === props.id)

onMounted(loadProfile)
watch(() => props.id, loadProfile)

async function loadProfile() {
  try {
    const res = await fetchUserHome(props.id)
    profile.value = res.data
    syncForm()
  } catch (error) {
    alert(error.message)
  }
}

function syncForm() {
  if (!profile.value) return
  form.nickname = profile.value.user.nickname || ''
  form.avatar = profile.value.user.avatar || ''
  form.bio = profile.value.user.bio || ''
  form.location = profile.value.user.location || ''
}

function openEditModal() {
  syncForm()
  openEditor.value = true
}

function closeEditModal() {
  openEditor.value = false
}

async function handleAvatarUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  try {
    const res = await uploadLocalFile(file)
    form.avatar = absoluteUrl(res.data.url)
  } catch (error) {
    alert(error.message)
  }
}

async function submitProfile() {
  if (!form.nickname) {
    alert('昵称不能为空哦~')
    return
  }
  saving.value = true
  try {
    const res = await updateMyProfile({ ...form })
    profile.value = res.data
    authStore.user = res.data.user
    localStorage.setItem('bbook_user', JSON.stringify(res.data.user))
    openEditor.value = false 
    alert('个人资料更新成功！')
    if (authStore.refreshMe) {
      await authStore.refreshMe()
    }
  } catch (error) {
    alert(error.message)
  } finally {
    saving.value = false
  }
}

function absoluteUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  const base = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  return `${base}${url}`
}
</script>

<style scoped>
.edit-trigger-btn {
  margin-top: 12px;
  border-radius: 20px;
  padding: 6px 18px;
  font-size: 13px;
  font-weight: 600;
  width: fit-content;
}

/* 遮罩背景：半透明加现代磨砂模糊 */
.custom-profile-modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  animation: fadeIn 0.2s ease-out;
}

/* 居中跳转的精致小编辑卡片页 */
.custom-profile-modal-window {
  width: 100%;
  max-width: 450px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #eef2f6;
  animation: popIn 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.modal-top-bar {
  padding: 16px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #f1f5f9;
}
.modal-top-bar h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #111111;
}
.modal-close-btn {
  background: transparent;
  border: none;
  font-size: 24px;
  color: #999999;
  cursor: pointer;
  line-height: 1;
}
.modal-close-btn:hover {
  color: #333333;
}

.modal-center-form {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.modal-input-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.modal-input-group label {
  font-size: 13px;
  font-weight: 600;
  color: #444444;
  padding-left: 2px;
}

/* 头像上传行排列调优 */
.modal-avatar-row {
  display: flex;
  gap: 10px;
  align-items: center;
}
.shadow-input {
  flex: 1;
}
.modal-upload-action {
  position: relative;
  overflow: hidden;
  background: #e6f0ff;
  color: #1e80ff;
  font-size: 13px;
  font-weight: 600;
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  white-space: nowrap;
}
.modal-upload-action input[type="file"] {
  position: absolute;
  top: 0;
  left: 0;
  min-width: 100%;
  min-height: 100%;
  opacity: 0;
  cursor: pointer;
}
.modal-upload-action:hover {
  background: #d0e4ff;
}

.modal-bottom-actions {
  padding-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.btn-cancel {
  border-radius: 8px;
  padding: 8px 16px;
}
.btn-save {
  background: #1e80ff !important;
  color: white !important;
  border-radius: 8px;
  padding: 8px 20px;
  box-shadow: 0 4px 12px rgba(30,128,255,0.15);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
@keyframes popIn {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
</style>