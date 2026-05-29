<template>
  <main class="container page-main">
    <section class="card publish-card">
      <div class="section-title">
        <div>
          <h1>发布我的日常</h1>
          <p>选择内容标签后，可填写文字并上传本地图片或视频。</p>
        </div>
      </div>

      <form class="form-stack" @submit.prevent="submit">
        <input v-model.trim="form.title" class="input" placeholder="标题，例如：今天跑步后的一些感受" />
        <textarea v-model.trim="form.summary" class="textarea" rows="3" placeholder="一句话摘要（可选）"></textarea>
        <textarea v-model.trim="form.content" class="textarea" rows="10" placeholder="正文内容"></textarea>

        <div class="form-grid publish-grid">
          <select v-model="form.tagId">
            <option value="">请选择内容标签</option>
            <option v-for="tag in tags" :key="tag.id" :value="tag.id">{{ tag.name }}</option>
          </select>
          <input v-model.trim="form.topicsText" class="input" placeholder="话题，多个用英文逗号分隔" />
          </div>

        <div class="media-upload-section">
          <div class="upload-grid">
            <div v-for="(item, index) in mediaList" :key="item.url" class="upload-preview-item">
              <img v-if="item.type === 'image'" :src="item.url" alt="上传图片" />
              <video v-else :src="item.url" preload="metadata"></video>
              <button type="button" class="remove-btn" @click="removeMedia(index)" title="移除此文件">×</button>
            </div>

            <label v-if="!uploading" class="upload-plus-btn">
              <span class="plus-icon">+</span>
              <input type="file" multiple accept="image/*,video/*" @change="handleFileChange" style="display: none;" />
            </label>

            <div v-if="uploading" class="upload-loading-box">
              <div class="loading-spinner"></div>
              <span>文件上传中...</span>
            </div>
          </div>
        </div>

        <button class="button" :disabled="submitting || uploading">{{ submitting ? '发布中...' : '立即发布' }}</button>
      </form>
    </section>
  </main>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createPost, fetchTags, uploadLocalFile } from '../api/bbook'

const router = useRouter()
const tags = ref([])
const submitting = ref(false)
const uploading = ref(false)
const mediaList = ref([])
const form = reactive({
  title: '',
  summary: '',
  content: '',
  tagId: '',
  topicsText: ''
})

onMounted(async () => {
  const res = await fetchTags()
  tags.value = res.data || []
})

async function handleFileChange(event) {
  const files = Array.from(event.target.files || [])
  if (!files.length) return
  uploading.value = true
  try {
    for (const file of files) {
      const res = await uploadLocalFile(file)
      mediaList.value.push({
        url: absoluteUrl(res.data.url),
        type: res.data.type,
        originalName: res.data.originalName
      })
    }
  } catch (error) {
    alert(error.message)
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

// 移除不需要的文件卡片
function removeMedia(index) {
  mediaList.value.splice(index, 1)
}

async function submit() {
  if (!form.title) return alert('请先填写日常标题哦~')
  if (!form.content) return alert('正文内容不能为空~')
  if (!form.tagId) return alert('请选择发布分类标签~')

  submitting.value = true
  try {
    const payload = {
      title: form.title,
      summary: form.summary,
      content: form.content,
      tagId: form.tagId,
      coverImage: '', // 后端源码中如果不传，默认自动摘取 images 列表的第一张图作为封面
      topics: form.topicsText.split(',').map((item) => item.trim()).filter(Boolean),
      images: mediaList.value.filter((item) => item.type === 'image').map((item) => item.url),
      mediaUrls: mediaList.value.map((item) => item.url)
    }
    const res = await createPost(payload)
    alert('日常发布成功 🎉')
    router.push(`/posts/${res.data.id}`)
  } catch (error) {
    alert(error.message)
  } finally {
    submitting.value = false
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
/* 🎨 精致化的加号上传及网格卡片缩略图样式定义 */
.media-upload-section {
  margin: 10px 0 24px;
}

.upload-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.upload-preview-item, 
.upload-plus-btn, 
.upload-loading-box {
  width: 110px;
  height: 110px;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  background: #f7f9fc;
  box-sizing: border-box;
}

.upload-preview-item img, 
.upload-preview-item video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-preview-item .remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  border: none;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  z-index: 5;
  transition: background 0.2s;
}

.upload-preview-item .remove-btn:hover {
  background: rgba(255, 77, 4f, 0.9);
}

.upload-plus-btn {
  border: 2px dashed #cbd5e1;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.upload-plus-btn:hover {
  border-color: #1e80ff;
  background: #e6f0ff;
}

.upload-plus-btn .plus-icon {
  font-size: 38px;
  color: #8e96a3;
  line-height: 1;
}

.upload-plus-btn:hover .plus-icon {
  color: #1e80ff;
}

.upload-loading-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: #8e96a3;
  border: 1px solid #eef2f6;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #cbd5e1;
  border-top-color: #1e80ff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: 6px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>