<template>
  <div v-if="visible" class="modal-mask" @click.self="$emit('close')">
    <div class="modal-card">
      <div class="tabs-row auth-tabs">
        <button class="tab-button" :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
        <button class="tab-button" :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
      </div>

      <form class="form-stack" @submit.prevent="submit">
        <input v-model.trim="form.username" class="input" placeholder="用户名（3-20 位）" />
        <input v-if="mode === 'register'" v-model.trim="form.nickname" class="input" placeholder="昵称" />
        <input v-model="form.password" class="input" type="password" placeholder="密码（至少 6 位）" />
        <button class="button" :disabled="store.loading">{{ store.loading ? '提交中...' : mode === 'login' ? '立即登录' : '立即注册' }}</button>
      </form>
      <p class="muted-tip">演示账号：lanmei / 123456</p>
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
      alert('登录成功')
    } else {
      await store.doRegister({ username: form.username, nickname: form.nickname, password: form.password })
      alert('注册成功')
    }
    emit('success')
    emit('close')
  } catch (error) {
    alert(error.message)
  }
}
</script>
