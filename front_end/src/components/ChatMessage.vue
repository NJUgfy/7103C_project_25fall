<template>
  <div :class="['message', message.role]">
    <div class="message-avatar">
      <span class="avatar-content">{{ message.role === 'user' ? 'Me' : 'AI' }}</span>
    </div>
    <div class="message-content">
      <div class="message-text">{{ message.content }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  message: {
    type: Object,
    required: true
  }
});

function getCurrentTime() {
  return new Date().toLocaleTimeString('en-US', { 
    hour: '2-digit', 
    minute: '2-digit',
    hour12: true 
  });
}
</script>

<style scoped>
.message {
  display: flex;
  margin-bottom: 24px;
  padding: 0 16px;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  font-size: 14px;
  margin: 0 12px;
}

.message.user .message-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.message.assistant .message-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(240, 147, 251, 0.3);
}

.avatar-content {
  font-size: 13px;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 16px;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.2s ease;
}

.message-content:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
}

.message.assistant .message-content {
  background: white;
  color: #2c3e50;
  border-top-left-radius: 4px;
  border: 1px solid #e8e8e8;
}

.message.user .message-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-top-right-radius: 4px;
}

.message-text {
  word-wrap: break-word;
  line-height: 1.6;
  font-size: 14px;
  white-space: pre-wrap;
}

.message-time {
  font-size: 11px;
  margin-top: 6px;
  opacity: 0.7;
}

.message.user .message-time {
  text-align: right;
  color: rgba(255, 255, 255, 0.85);
}

.message.assistant .message-time {
  color: #999;
}
</style>