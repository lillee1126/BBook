<template>
  <div class="ai-chatbot-container">
    <button class="ai-trigger-btn" @click="toggleChat" title="召唤豆包AI">
      <span v-if="!isOpen">🤖</span>
      <span v-else>✕</span>
    </button>

    <div v-if="isOpen" class="ai-chat-window">
      <div class="chat-header">
        <div class="header-title">
          <span class="dot-online"></span>
          <h3>你的AI助手小蓝</h3>
        </div>
        <button class="clear-btn" @click="resetChat">清空记录</button>
      </div>

      <div class="chat-messages" ref="messageBox">
        <div v-if="messages.length === 0" class="empty-chat">
          你好！我是小篮。你可以问我任何问题。
        </div>
        
        <div 
          v-for="(msg, index) in messages" 
          :key="index" 
          :class="['message-row', msg.role === 'user' ? 'msg-user' : 'msg-ai']"
        >
          <div class="msg-bubble">
            {{ msg.content }}
          </div>
        </div>
        
        <div v-if="isTyping" class="message-row msg-ai">
          <div class="msg-bubble typing-indicator">
            <span>.</span><span>.</span><span>.</span>
          </div>
        </div>
      </div>

      <div class="chat-input-bar">
        <input 
          v-model.trim="userInput" 
          placeholder="聊点什么吧... (Enter发送)" 
          @keyup.enter="sendMessage"
          :disabled="isTyping"
        />
        <button @click="sendMessage" :disabled="!userInput || isTyping">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'

const isOpen = ref(false)
const userInput = ref('')
const messages = ref([])
const isTyping = ref(false)
const messageBox = ref(null)

// 切换打开/关闭状态
function toggleChat() {
  isOpen.value = !isOpen.value
  // 🔥 核心逻辑：一旦关闭页面/窗口，相当于输出了 'quit'，直接在前端内存中彻底销毁，不留任何痕迹
  if (!isOpen.value) {
    resetChat()
  }
}

// 清空当前聊天
function resetChat() {
  messages.value = []
  userInput.value = ''
  isTyping.value = false
}

// 核心：处理 SSE 流式传输接收
async function sendMessage() {
  if (!userInput.value || isTyping.value) return

  const userText = userInput.value
  userInput.value = ''
  
  // 1. 推入用户发言
  messages.value.push({ role: 'user', content: userText })
  scrollToBottom()

  // 2. 开启 AI 占位符
  isTyping.value = true
  const aiIndex = messages.value.length
  messages.value.push({ role: 'assistant', content: '' })
  scrollToBottom()

  try {
    // 获取当前登录系统的 JWT Token 附带鉴权
    const token = localStorage.getItem('bbook_token')
    const headers = { 'Content-Type': 'application/json' }
    if (token) headers['Authorization'] = `Bearer ${token}`

    const apiBase = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
    
    // 使用 fetch 原生消费后端 Server-Sent Events 流
    const response = await fetch(`${apiBase}/api/v1/ai/chat`, {
      method: 'POST',
      headers: headers,
      body: JSON.stringify({ messages: messages.value.slice(0, -1) }) // 传递上下文，排除AI占位符本身
    })

    if (!response.ok) throw new Error('AI服务连接失败')

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let done = false
    isTyping.value = false // 收到首字，关闭等待动画

    while (!done) {
      const { value, done: readerDone } = await reader.read()
      done = readerDone
      if (value) {
        const chunk = decoder.decode(value, { stream: !done })
        const lines = chunk.split('\n')
        
        for (const line of lines) {
          const trimmed = line.trim()
          if (!trimmed || trimmed === 'data: [DONE]') continue
          
          if (trimmed.startsWith('data:')) {
            try {
              const jsonStr = trimmed.replace('data:', '').trim()
              const parsed = JSON.parse(jsonStr)
              
              // 解析标准 OpenAI 流式格式 choices[0].delta.content
              const textChunk = parsed.choices?.[0]?.delta?.content || ''
              if (textChunk) {
                messages.value[aiIndex].content += textChunk
                scrollToBottom()
              }
            } catch (e) {
              // 容错解析后端回传的单体错误信息
              if (trimmed.includes('error')) {
                const parsed = JSON.parse(trimmed.replace('data:', ''))
                messages.value[aiIndex].content = `⚠️ 错误: ${parsed.error}`
              }
            }
          }
        }
      }
    }
  } catch (error) {
    console.error(error)
    messages.value[aiIndex].content = '⚠️ 无法连接到AI助手，请确保您已登录系统。'
    isTyping.value = false
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messageBox.value) {
      messageBox.value.scrollTop = messageBox.value.scrollHeight
    }
  })
}
</script>

<style scoped>
.ai-chatbot-container {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999; /* 保持在最顶层 */
  font-family: system-ui, -apple-system, sans-serif;
}

/* 现代感圆形悬浮按钮 */
.ai-trigger-btn {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: #1e80ff; /* 继承项目小蓝书标志性蓝色 */
  color: white;
  border: none;
  font-size: 22px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(30, 128, 255, 0.3);
  display: grid;
  place-items: center;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
.ai-trigger-btn:hover {
  transform: scale(1.08) rotate(90deg);
  background: #0066ff;
}

/* 精致小长条对话视窗 */
.ai-chat-window {
  position: absolute;
  bottom: 68px;
  right: 0;
  width: 340px;
  height: 460px;
  background: #ffffff;
  border-radius: 14px;
  border: 1px solid #eef2f6;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transform-origin: bottom right;
  animation: popIn 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.chat-header {
  padding: 12px 14px;
  background: #f8fafc;
  border-bottom: 1px solid #eef2f6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-title {
  display: flex;
  align-items: center;
  gap: 6px;
}
.dot-online {
  width: 7px;
  height: 7px;
  background: #00b96b;
  border-radius: 50%;
}
.chat-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #222;
}
.clear-btn {
  background: transparent;
  border: none;
  color: #8e96a3;
  font-size: 11px;
  cursor: pointer;
}
.clear-btn:hover {
  color: #ff4d4f;
}

.chat-messages {
  flex: 1;
  padding: 14px;
  overflow-y: auto;
  background: #fafafa;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.empty-chat {
  text-align: center;
  color: #8e96a3;
  font-size: 12px;
  margin-top: 50px;
  line-height: 1.6;
}

.message-row {
  display: flex;
  width: 100%;
}
.msg-user { justify-content: flex-end; }
.msg-ai { justify-content: flex-start; }

.msg-bubble {
  max-width: 85%;
  padding: 8px 12px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}
.msg-user .msg-bubble {
  background: #1e80ff;
  color: #ffffff;
  border-top-right-radius: 2px;
}
.msg-ai .msg-bubble {
  background: #ffffff;
  color: #222222;
  border-top-left-radius: 2px;
  border: 1px solid #eef2f6;
}

/* 输入框 */
.chat-input-bar {
  padding: 10px 12px;
  border-top: 1px solid #eef2f6;
  display: flex;
  gap: 6px;
  background: #ffffff;
}
.chat-input-bar input {
  flex: 1;
  border: 1px solid #eef2f6;
  border-radius: 6px;
  padding: 6px 10px;
  font-size: 13px;
  outline: none;
  background: #f8fafc;
}
.chat-input-bar input:focus {
  border-color: #1e80ff;
  background: #ffffff;
}
.chat-input-bar button {
  background: #1e80ff;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 0 12px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
.chat-input-bar button:disabled {
  background: #cbd5e1;
  cursor: not-allowed;
}

/* 动效 */
@keyframes popIn {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}
.typing-indicator span {
  animation: blink 1.4s infinite both;
  font-weight: bold;
  display: inline-block;
  font-size: 16px;
}
.typing-indicator span:nth-child(2) { animation-delay: .2s; }
.typing-indicator span:nth-child(3) { animation-delay: .4s; }
@keyframes blink {
  0% { opacity: .2; }
  20% { opacity: 1; }
  100% { opacity: .2; }
}
</style>