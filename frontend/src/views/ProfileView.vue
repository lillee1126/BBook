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

    <section class="profile-tabs-nav">
      <button 
        class="tab-nav-btn" 
        :class="{ active: currentTab === 'published' }" 
        @click="currentTab = 'published'"
      >
        {{ isMine ? '我发布的内容' : 'TA 发布的内容' }}
        <span class="tab-count">({{ profile.posts.length }})</span>
      </button>
      
      <button 
        v-if="isMine"
        class="tab-nav-btn" 
        :class="{ active: currentTab === 'liked' }" 
        @click="switchTabToLiked"
      >
        我的喜欢
        <span class="tab-count" v-if="likedPosts.length">({{ likedPosts.length }})</span>
      </button>
    </section>

    <section class="profile-post-grid" v-if="currentTab === 'published'">
      <PostCard v-for="item in profile.posts" :key="item.id" :post="item" />
      <div v-if="!profile.posts.length" class="empty-state">还没有发布过任何内容，快去记录生活吧~</div>
    </section>

    <section class="profile-post-grid" v-else-if="currentTab === 'liked'">
      <PostCard v-for="item in likedPosts" :key="item.id" :post="item" />
      <div v-if="loadingLiked" class="empty-state">正在拼命加载喜欢内容...</div>
      <div v-else-if="!likedPosts.length" class="empty-state">这里空空如也，去主页发现喜欢的动态吧 ♥</div>
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
import { fetchUserHome, updateMyProfile, uploadLocalFile, fetchPosts } from '../api/bbook'
import { useAuthStore } from '../stores/auth'
import PostCard from '../components/PostCard.vue'

const props = defineProps({
  id: { type: String, required: true }
})

const authStore = useAuthStore()
const profile = ref(null)
const openEditor = ref(false) 
const saving = ref(false)

const currentTab = ref('published')
const likedPosts = ref([])
const loadingLiked = ref(false)

const form = reactive({
  nickname: '',
  avatar: '',
  bio: '',
  location: ''
})

const isMine = computed(() => authStore.user?.id === props.id)

onMounted(() => {
  loadProfile()
  if (isMine.value) {
    loadLikedPosts() 
  }
})

watch(() => props.id, () => {
  currentTab.value = 'published'
  likedPosts.value = []
  loadProfile()
  if (isMine.value) {
    loadLikedPosts()
  }
})

async function loadProfile() {
  try {
    const res = await fetchUserHome(props.id)
    profile.value = res.data
    syncForm()
  } catch (error) {
    alert(error.message)
  }
}

async function switchTabToLiked() {
  currentTab.value = 'liked'
  await loadLikedPosts()
}

async function loadLikedPosts() {
  loadingLiked.value = true
  try {
    const res = await fetchPosts({ page: 0, size: 100 })
    if (res.data && res.data.records) {
      likedPosts.value = res.data.records.filter(post => post.liked)
    }
  } catch (error) {
    console.error('拉取喜欢列表异常:', error)
  } finally {
    loadingLiked.value = false
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

/* 选项卡样式区 */
.profile-tabs-nav {
  display: flex;
  gap: 32px;
  border-bottom: 1px solid var(--line);
  margin: 28px 0 20px;
  padding-bottom: 2px;
}

.tab-nav-btn {
  background: transparent;
  border: none;
  font-size: 16px;
  font-weight: 600;
  color: var(--muted);
  padding: 8px 4px;
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
}

.tab-nav-btn:hover {
  color: var(--text);
}

.tab-nav-btn.active {
  color: var(--text);
  font-weight: 700;
}

.tab-nav-btn.active::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: 0;
  width: 100%;
  height: 3px;
  background: var(--primary);
  border-radius: 999px;
}

.tab-count {
  font-size: 14px;
  font-weight: 500;
  margin-left: 2px;
}

/* 🎯 核心技术实现点：绝对固定长宽、拒绝任何自适应形变的刚性布局网格 */
.profile-post-grid {
  display: flex;
  flex-wrap: wrap;       /* 允许超出的卡片整齐向下换行 */
  gap: 20px;            /* 严格卡片间距 20 像素 */
  width: 100%;
  margin-top: 16px;
}

/* 💡 深度穿透强制改写内部 PostCard 大小，使其永远保持 230px * 320px 的绝对经典尺寸 */
.profile-post-grid :deep(.post-card) {
  width: 230px !important;
  height: 320px !important;
  flex: 0 0 230px !important; /* 绝对禁止伸展 flex-grow:0, 禁止压缩 flex-shrink:0 */
  margin-bottom: 0 !important;
  display: flex;
  flex-direction: column;
}

/* 💡 将卡片的媒体区高宽严格锁定，溢出裁剪成标准比例 */
.profile-post-grid :deep(.post-card__media) {
  width: 100% !important;
  height: 230px !important;    /* 媒体展示区调整为标准的正方形区域 */
  overflow: hidden !important;
}

.profile-post-grid :deep(.post-card__image),
.profile-post-grid :deep(.text-note-cover) {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important; /* 维持高质量填满，不发生比例变形 */
}

/* 💡 底部内容区域大小同样硬性卡死 */
.profile-post-grid :deep(.post-card__body) {
  flex: 1 !important;
  padding: 10px !important;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

/* 弹窗遮罩与常规样式区保持稳定 */
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
}

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
}

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

.modal-bottom-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
.btn-save {
  background: #1e80ff !important;
  color: white !important;
  border-radius: 8px;
}
</style>