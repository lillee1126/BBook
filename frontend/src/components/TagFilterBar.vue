<template>
  <section class="filter-panel">
    <div class="filter-toolbar">
      
      <div class="search-box-row">
        <input 
          v-model.trim="keyword" 
          class="search-input" 
          placeholder="搜索标题、正文或话题" 
          @keyup.enter="emitSearch" 
        />
        <button class="filter-search-btn" @click="emitSearch">搜索</button>
      </div>

      <div class="chip-row">
        <button 
          class="chip-button" 
          :class="{ active: activeTag === '' }" 
          @click="selectTag('')"
        >
          推荐
        </button>
        <button
          v-for="tag in tags"
          :key="tag.id"
          class="chip-button"
          :class="{ active: activeTag === tag.code }"
          @click="selectTag(tag.code)"
        >
          {{ tag.name }}
        </button>
      </div>

    </div>
  </section>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  tags: { type: Array, default: () => [] },
  modelValue: { type: Object, default: () => ({ keyword: '', tagCode: '' }) }
})

const emit = defineEmits(['update:modelValue', 'search'])
const keyword = ref(props.modelValue.keyword || '')
const activeTag = ref(props.modelValue.tagCode || '')

watch(() => props.modelValue, (value) => {
  keyword.value = value.keyword || ''
  activeTag.value = value.tagCode || ''
}, { deep: true })

function selectTag(code) {
  activeTag.value = code
  emitSearch()
}

function emitSearch() {
  const payload = { keyword: keyword.value, tagCode: activeTag.value }
  emit('update:modelValue', payload)
  emit('search', payload)
}
</script>

<style scoped>
.filter-panel {
  background: #fff;
  padding: 0;
  margin-bottom: 32px;
  width: 100%;
}
.filter-toolbar {
  display: flex;
  flex-direction: column;
  gap: 18px;
  width: 100%;
  align-items: center;
}
.search-box-row {
  width: 100%;
  max-width: 600px;
  display: flex;
  gap: 12px;
  align-items: center;
  margin: 0 auto;
}
.search-input {
  flex: 1;
  border-radius: 999px;
  background: #f4f6f9;
  border: 1px solid transparent;
  padding: 12px 20px;
  font-size: 14px;
  outline: none;
  color: #222;
  transition: all 0.2s;
}
.search-input:focus {
  background: #fff;
  border-color: #1e80ff;
}

/* 🌟 核心保护：拒绝白框，强制渲染为饱满的小蓝书标志主题蓝色胶囊按钮 🌟 */
.filter-search-btn {
  background: #1e80ff !important;
  color: #ffffff !important;
  border: none !important;
  border-radius: 999px !important;
  padding: 10px 24px !important;
  font-size: 14px !important;
  font-weight: 600 !important;
  cursor: pointer !important;
  box-shadow: 0 4px 12px rgba(30,128,255,0.15) !important;
  white-space: nowrap !important;
  display: inline-block !important;
  transition: all 0.2s ease !important;
}
.filter-search-btn:hover {
  filter: brightness(0.92) !important;
}

.chip-row {
  display: flex;
  gap: 6px;
  justify-content: center;
  flex-wrap: wrap;
  width: 100%;
}
.chip-button {
  border: none;
  background: transparent;
  border-radius: 999px;
  padding: 8px 16px;
  color: #555;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.2s;
}
.chip-button:hover {
  color: #1e80ff;
}
.chip-button.active {
  background: #f5f5f5;
  color: #111;
  font-weight: 700;
}
</style>