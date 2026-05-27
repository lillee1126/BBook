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
          <input v-model.trim="form.coverImage" class="input" placeholder="封面地址（可选，不填默认取第一张图片）" />
        </div>

        <div class="form-stack">
          <input type="file" multiple accept="image/*,video/*" @change="handleFileChange" />
          <div v-if="uploading" class="empty-state">文件上传中...</div>
          <div v-if="mediaList.length" class="gallery-grid">
            <template v-for="item in mediaList" :key="item.url">
              <img v-if="item.type === 'image'" :src="item.url" class="detail-image" alt="上传图片" />
              <video v-else :src="item.url" class="detail-image" controls preload="metadata"></video>
            </template>
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
  coverImage: '',
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

async function submit() {
  submitting.value = true
  try {
    const payload = {
      title: form.title,
      summary: form.summary,
      content: form.content,
      tagId: form.tagId,
      coverImage: form.coverImage,
      topics: form.topicsText.split(',').map((item) => item.trim()).filter(Boolean),
      images: mediaList.value.filter((item) => item.type === 'image').map((item) => item.url),
      mediaUrls: mediaList.value.map((item) => item.url)
    }
    const res = await createPost(payload)
    alert('发布成功')
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
