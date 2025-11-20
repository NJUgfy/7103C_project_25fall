<template>
  <div class="chat-input-wrapper">
    <div class="chat-input-container">
      <div class="input-box">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="1"
          :autosize="{ minRows: 1, maxRows: 4 }"
          placeholder="Ask me anything "
          @keydown.enter="handleKeyDown"
          class="custom-textarea"
        />
        <el-button 
          type="primary" 
          @click="handleSend"
          :loading="loading"
          :disabled="!inputText.trim() || loading"
          class="send-button"
          circle
        >
          <svg v-if="!loading" class="send-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8" />
          </svg>
        </el-button>
      </div>
      <div class="input-hint"></div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const inputText = ref('');

const props = defineProps({
  loading: Boolean
});

const emit = defineEmits(['send']);

function handleKeyDown(event) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault();
    // 如果正在加载，不允许发送
    if (props.loading) {
      return;
    }
    handleSend();
  }
}

function handleSend() {
  if (!inputText.value.trim() || props.loading) {
    return;
  }
  const messageSend = inputText.value;
  inputText.value = '';
  emit('send', messageSend);
}
</script>

<style scoped>
.chat-input-wrapper {
  background: transparent;
  border: none;
  padding: 20px;
}

.chat-input-container {
  max-width: 900px;
  margin: 0 auto;
  background: white;
  border-radius: 24px;
  padding: 6px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08), 0 0 0 1px rgba(218, 165, 32, 0.1);
  transition: all 0.2s ease;
}

.chat-input-container:focus-within {
  box-shadow: 0 4px 20px rgba(218, 165, 32, 0.2), 0 0 0 2px rgba(218, 165, 32, 0.25);
}

.input-box {
  display: flex;
  gap: 8px;
  align-items: flex-end;
  padding: 8px;
}

.custom-textarea {
  flex: 1;
}

.custom-textarea :deep(.el-textarea__inner) {
  border: none;
  border-radius: 16px;
  padding: 12px 16px;
  font-size: 15px;
  line-height: 1.5;
  background: transparent;
  transition: none;
  resize: none;
  box-shadow: none;
  color: #2c3e50;
  pointer-events: auto;
  user-select: text;
}

.custom-textarea :deep(.el-textarea__inner):disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

.custom-textarea :deep(.el-textarea__inner):focus {
  border: none;
  box-shadow: none;
  outline: none;
}

.custom-textarea :deep(.el-textarea__inner)::placeholder {
  color: #a0a0a0;
  font-size: 15px;
}

.send-button {
  width: 40px;
  height: 40px;
  min-width: 40px;
  background: #DAA520;
  border: none;
  transition: all 0.2s ease;
  box-shadow: none;
  padding: 0;
  flex-shrink: 0;
}

.send-button:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(218, 165, 32, 0.4);
  background: #EBC97F;
}

.send-button:active:not(:disabled) {
  transform: scale(0.95);
}

.send-button:disabled {
  background: #f0f0f0;
  box-shadow: none;
  cursor: not-allowed;
  opacity: 0.6;
}

/* 加载中的按钮样式 */
.send-button.is-loading {
  background: #DAA520;
  opacity: 0.8;
  cursor: wait;
}

.send-icon {
  width: 18px;
  height: 18px;
}

.input-hint {
  text-align: center;
  font-size: 12px;
  color: #999;
  margin-top: 10px;
  display: none; /* 隐藏提示文字，让界面更简洁 */
}
</style>