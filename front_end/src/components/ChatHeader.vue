<template>
  <div class="chat-header">
    <!-- 仅在侧边栏隐藏时显示toggle按钮 -->
    <button v-if="!sidebarVisible" @click="$emit('toggle-sidebar')" class="sidebar-toggle">
      <svg class="toggle-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
      </svg>
    </button>
    
    <h1 class="chat-title">AI Assistant</h1>
    
    <div class="chat-status">
      <span class="status-dot"></span>
      {{ isStreaming ? 'Typing...' : 'Online' }}
    </div>
  </div>
</template>

<script setup>
defineProps({
  sidebarVisible: {
    type: Boolean,
    default: true
  },
  isStreaming: {
    type: Boolean,
    default: false
  }
});

defineEmits(['toggle-sidebar']);
</script>

<style scoped>
.chat-header {
  background: white;
  border-bottom: 1px solid #e8e8e8;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  gap: 12px;
  min-height: 72px;
}

.sidebar-toggle {
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  padding: 0;
  flex-shrink: 0;
}

.sidebar-toggle:hover {
  background: #f0f2f5;
}

.toggle-icon {
  width: 20px;
  height: 20px;
  stroke: #2c3e50;
}

.chat-title {
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  flex: 1;
}

.chat-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #52c41a;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #52c41a;
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>