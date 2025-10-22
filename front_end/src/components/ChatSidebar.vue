<template>
  <div v-show="visible" class="sidebar">
    <div class="sidebar-header">
      <!-- 切换按钮 -->
      <button @click="$emit('toggle')" class="sidebar-toggle">
        <svg class="toggle-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>
      <h2 class="sidebar-title">Chat History</h2>
    </div>
    
    <el-button 
      type="primary" 
      @click="$emit('create-chat')"
      class="new-chat-btn"
    >
      <svg class="icon-plus" viewBox="0 0 24 24" fill="none" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
      </svg>
      New Chat
    </el-button>
    
    <!-- 历史聊天列表 -->
    <div class="chat-list">
      <div 
        v-for="id in chatIds" 
        :key="id"
        :class="['chat-item', { active: currentChatId === id }]"
        @click="$emit('select-chat', id)"
      >
        <svg class="chat-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
        </svg>
        <span class="chat-item-text">{{ getChatTitle(id) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  visible: {
    type: Boolean,
    default: true
  },
  chatIds: {
    type: Array,
    default: () => []
  },
  currentChatId: {
    type: String,
    default: ''
  }
});

defineEmits(['toggle', 'create-chat', 'select-chat']);

// 获取聊天标题
function getChatTitle(chatId) {
  // 查找当前chatId在列表中的索引
  const index = props.chatIds.indexOf(chatId);
  
  // 如果找到，返回Conversation N（从1开始计数）
  if (index !== -1) {
    return `Conversation ${props.chatIds.length - index}`;
  }
  
  // 兜底
  return 'New Chat';
}
</script>

<style scoped>
.sidebar {
  width: 280px;
  background: white;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.04);
  transition: transform 0.3s ease;
}

.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e8e8e8;
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 72px;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0;
  flex: 1;
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

.new-chat-btn {
  margin: 16px;
  width: calc(100% - 32px);
  height: 44px;
  border-radius: 10px;
  font-weight: 500;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.new-chat-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.3);
}

.icon-plus {
  width: 18px;
  height: 18px;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 16px;
}

.chat-item {
  padding: 12px 14px;
  margin-bottom: 6px;
  background: #f8f9fa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid transparent;
}

.chat-item:hover {
  background: #f0f2f5;
  border-color: #e0e0e0;
}

.chat-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border-color: #667eea;
}

.chat-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  stroke: #666;
}

.chat-item.active .chat-icon {
  stroke: #667eea;
}

.chat-item-text {
  font-size: 14px;
  color: #2c3e50;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-item.active .chat-item-text {
  color: #667eea;
  font-weight: 500;
}

@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 1000;
  }
}
</style>