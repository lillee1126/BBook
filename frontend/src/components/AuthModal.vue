<template>
  <div v-if="visible" class="modal-mask" @click.self="$emit('close')">
    <div class="modal-card">
      <button type="button" class="modal-close-x" @click="$emit('close')">×</button>

      <div class="tabs-row auth-tabs">
        <button class="tab-button" :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
        <button class="tab-button" :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
      </div>

      <form class="form-stack" @submit.prevent="submit">
        <div class="input-wrapper">
          <input v-model.trim="form.username" class="input" placeholder="用户名（3-20 位）" required />
        </div>
        
        <div v-if="mode === 'register'" class="input-wrapper animate-slide">
          <input v-model.trim="form.nickname" class="input" placeholder="昵称" required />
        </div>
        
        <div class="input-wrapper">
          <input v-model="form.password" class="input" type="password" placeholder="密码（至少 6 位）" required />
        </div>

        <button class="button auth-submit-btn" :disabled="store.loading">
          {{ store.loading ? '提交中...' : mode === 'login' ? '立即登录' : '立即注册' }}
        </button>
      </form>

      <p class="muted-tip">💡 演示账号：<span class="code-highlight">lanmei</span> / <span class="code-highlight">123456</span></p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { useAuthStore } from '../stores/auth'

const props = defineProps({
  visible: { type: Boolean, default: false },
  initialMode: { type: String, default: 'login' }
})

const emit = defineEmits(['close', 'success'])
const store = useAuthStore()
const mode = ref(props.initialMode)
const form = reactive({ username: '', nickname: '', password: '' })

watch(() => props.initialMode, (value) => {
  mode.value = value
})

watch(() => props.visible, (value) => {
  if (value) {
    form.username = ''
    form.nickname = ''
    form.password = ''
  }
})

async function submit() {
  try {
    if (mode.value === 'login') {
      await store.doLogin({ username: form.username, password: form.password })
      alert('登录成功 ✨')
    } else {
      await store.doRegister({ username: form.username, nickname: form.nickname, password: form.password })
      alert('注册成功 🎉')
    }
    emit('success')
    emit('close')
  } catch (error) {
    alert(error.message)
  }
}
</script>

<style scoped>
/* 🌟 核心：全屏半透明磨砂背景遮罩 */
.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.35);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px); /* 兼容 Safari */
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999; /* 碾压所有底层元素 */
  animation: fadeIn 0.2s ease-out forwards;
}

/* 🌟 悬浮精致独立小窗口卡片 */
.modal-card {
  position: relative;
  width: 90%;
  max-width: 400px;
  background: #ffffff;
  border-radius: 20px;
  padding: 36px 32px;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15);
  border: 1px solid #eef2f6;
  display: flex;
  flex-direction: column;
  gap: 24px;
  animation: popIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) forwards;
}

/* 右上角原生优雅的物理关闭按钮 */
.modal-close-x {
  position: absolute;
  top: 16px;
  right: 20px;
  background: transparent;
  border: none;
  font-size: 24px;
  color: #999999;
  cursor: pointer;
  line-height: 1;
  transition: color 0.2s;
}
.modal-close-x:hover {
  color: #333333;
}

/* 选项卡行样式 */
.tabs-row {
  display: flex;
  justify-content: center;
  gap: 36px;
  border-bottom: 1px solid #f1f5f9;
  padding-bottom: 8px;
}

.tab-button {
  background: transparent;
  border: none;
  font-size: 18px;
  font-weight: 600;
  color: var(--muted);
  cursor: pointer;
  padding: 4px 12px;
  position: relative;
  transition: all 0.2s ease;
}
.tab-button:hover {
  color: var(--primary);
}

/* 激活项：高亮并带有小蓝书专属下划胶囊线 */
.tab-button.active {
  color: #111111;
  font-weight: 700;
}
.tab-button.active::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 0;
  width: 100%;
  height: 3px;
  background: var(--primary);
  border-radius: 999px;
}

/* 表单纵向垂直间距 */
.form-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 增强输入框的呼吸感与边缘微调 */
.form-stack .input {
  font-size: 14px;
  border: 1px solid #e2e8f0;
  background: #f7f9fc;
  transition: all 0.2s ease;
}
.form-stack .input:focus {
  border-color: var(--primary);
  background: #ffffff;
  box-shadow: 0 0 0 4px rgba(30, 128, 255, 0.1);
}

/* 提交按钮拉满宽度并追加现代阴影 */
.auth-submit-btn {
  width: 100%;
  padding: 12px !important;
  font-size: 15px !important;
  font-weight: 700 !important;
  letter-spacing: 1px;
  margin-top: 8px;
  background: var(--primary) !important;
  box-shadow: 0 6px 16px rgba(30, 128, 255, 0.25) !important;
}
.auth-submit-btn:hover {
  background: var(--primary-strong) !important;
}
.auth-submit-btn:disabled {
  background: #cbd5e1 !important;
  box-shadow: none !important;
  cursor: not-allowed;
}

/* 演示提示框精细化 */
.muted-tip {
  margin: 0;
  text-align: center;
  font-size: 12px;
  color: #7e8693;
  background: #f8fafc;
  padding: 10px;
  border-radius: 10px;
  border: 1px dashed #e2e8f0;
  line-height: 1.4;
}
.code-highlight {
  color: var(--primary);
  font-weight: 700;
  background: #e6f0ff;
  padding: 1px 5px;
  border-radius: 4px;
}

/* 切换注册时的微滑移动效 */
.animate-slide {
  animation: slideDown 0.2s ease-out forwards;
}

/* ✨ CSS3 动画序列 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes popIn {
  from { transform: scale(0.9) translateY(15px); opacity: 0; }
  to { transform: scale(1) translateY(0); opacity: 1; }
}

@keyframes slideDown {
  from { transform: translateY(-10px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
</style>