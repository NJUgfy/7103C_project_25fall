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
  
  if (index !== -1) {
    return `Conversation ${props.chatIds.length - index}`;
  }
  
  return 'New Chat';
}
</script>

<style scoped>
/* === Sidebar 主体 === */
.sidebar {
  width: 280px;
  background: #ffffff;
  border-right: 1px solid #e5e2dc;
  display: flex;
  flex-direction: column;
  box-shadow: 2px 0 6px rgba(0, 0, 0, 0.03);
  transition: transform 0.3s ease;
}


/* === Header === */
.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid #e5e2dc;
  display: flex;
  align-items: center;
  gap: 12px;
  min-height: 72px;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 600;
  color: #2f2f2f;
  margin: 0;
  flex: 1;
}


/* === Toggle Button === */
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
  transition: all 0.15s ease;
  padding: 0;
  flex-shrink: 0;
}

.sidebar-toggle:hover {
  background: #f4f2ee;
}

.toggle-icon {
  width: 20px;
  height: 20px;
  stroke: #3c3c3c;
}


/* === New Chat 按钮 === */
.new-chat-btn {
  margin: 16px;
  width: calc(100% - 32px);
  height: 44px;
  border-radius: 10px;
  font-weight: 500;
  background: #d4a017; /* 更沉稳的金色 */
  border: 1px solid #c39b54;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.2s ease;
}

.new-chat-btn:hover {
  background: #c79615;
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
}


/* === Chat List === */
.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 16px;
}


/* === Chat Item === */
.chat-item {
  padding: 12px 14px;
  margin-bottom: 6px;
  background: #faf9f7; 
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s ease;
  display: flex;
  align-items: center;
  gap: 10px;
  border: 1px solid #f0ede8;
}

.chat-item:hover {
  background: #f4f2ee;
  border-color: #e3dfd7;
}

.chat-item.active {
  background: #fffdf8;
  border-color: #d4a017; 
  box-shadow: 0 0 0 1px #d4a017 inset;
}


/* === Icons === */
.chat-icon {
  width: 18px;
  height: 18px;
  flex-shrink: 0;
  stroke: #6c6c6c;
}

.chat-item.active .chat-icon {
  stroke: #b58b40; 
}


.chat-item-text {
  font-size: 14px;
  color: #2f2f2f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chat-item.active .chat-item-text {
  color: #b58b40;
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