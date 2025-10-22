<template>
  <div class="chat-container">
    <!-- 侧边栏组件 -->
    <ChatSidebar 
      :visible="sidebarVisible"
      :chat-ids="chatIds"
      :current-chat-id="currentChatId"
      @toggle="toggleSidebar"
      @create-chat="createNewChat"
      @select-chat="loadChat"
    />

    <!-- 主聊天区域 -->
    <div class="main-chat">
      <!-- 头部组件 -->
      <ChatHeader 
        :sidebar-visible="sidebarVisible"
        :is-streaming="isStreaming"
        @toggle-sidebar="toggleSidebar"
      />

      <!-- 消息列表组件 -->
      <ChatMessageList 
        v-if="hasMessages"
        ref="messageListRef"
        :messages="messages"
        :streaming-message="streamingMessage"
      />

      <!-- 空状态组件 -->
      <ChatEmptyState v-else />

      <!-- 输入框遮罩层 -->
      <div v-if="!hasMessages" class="input-overlay"></div>
      
      <!-- 输入框组件 -->
      <ChatInput 
        :class="{'input-centered': !hasMessages}"
        :loading="isStreaming"
        @send="handleSendMessage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { getChatIds, getChatHistory, sendChatMessage } from '../api/chat';
import { ElMessage } from 'element-plus';

// 导入子组件
import ChatSidebar from '../components/ChatSidebar.vue';
import ChatHeader from '../components/ChatHeader.vue';
import ChatMessageList from '../components/ChatMessageList.vue';
import ChatEmptyState from '../components/ChatEmptyState.vue';
import ChatInput from '../components/ChatInput.vue';

// ========== 状态管理 ==========
const chatIds = ref([]);
const currentChatId = ref('');
const messages = ref([]);
const streamingMessage = ref(null);
const isStreaming = ref(false);
const sidebarVisible = ref(true);
const messageListRef = ref(null);

let currentSSEConnection = null;

// ========== 计算属性 ==========
const hasMessages = computed(() => {
  return messages.value.length > 0 || streamingMessage.value;
});

// ========== 侧边栏操作 ==========
function toggleSidebar() {
  sidebarVisible.value = !sidebarVisible.value;
}

// ========== 聊天列表操作 ==========
// 加载聊天列表
async function loadChatIds() {
  try {
    const res = await getChatIds();
    chatIds.value = res.data.chatIds || [];
    
    if (currentChatId.value && chatIds.value.includes(currentChatId.value)) {
      loadChat(currentChatId.value);
    }
  } catch (error) {
    console.error('Failed to load chat list:', error);
    ElMessage.error('Failed to load chat list');
  }
}

// 加载指定聊天记录
async function loadChat(chatId) {
  try {
    // 取消当前SSE连接
    if (currentSSEConnection) {
      currentSSEConnection.abort();
      currentSSEConnection = null;
      isStreaming.value = false;
      streamingMessage.value = null;
    }

    currentChatId.value = chatId;
    const res = await getChatHistory(chatId);
    messages.value = res.data.messages || [];
  } catch (error) {
    console.error('Failed to load chat history:', error);
    ElMessage.error('Failed to load chat history');
  }
}

// 创建新聊天
function createNewChat() {
  // 取消当前SSE连接
  if (currentSSEConnection) {
    currentSSEConnection.abort();
    currentSSEConnection = null;
    isStreaming.value = false;
    streamingMessage.value = null;
  }

  currentChatId.value = `chat_${Date.now()}`;
  messages.value = [];
}

// ========== 消息发送 ==========
function handleSendMessage(content) {
  // 如果没有当前聊天ID，创建新的
  if (!currentChatId.value) {
    currentChatId.value = `chat_${Date.now()}`;
    console.log('Created new chat:', currentChatId.value);
  }

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content
  });

  // 初始化流式消息
  streamingMessage.value = {
    role: 'assistant',
    content: ''
  };
  
  isStreaming.value = true;

  // 发送消息并处理SSE流
  currentSSEConnection = sendChatMessage({
    chatId: currentChatId.value,
    message: content,
    
    onMessage: (data) => {
      console.log('Received SSE data:', data);
      
      let contentChunk = '';
      
      // 解析不同格式的数据
      if (data.content !== undefined) {
        contentChunk = data.content;
      } else if (data.delta && data.delta.content !== undefined) {
        contentChunk = data.delta.content;
      } else if (data.choices && data.choices[0]?.delta?.content !== undefined) {
        contentChunk = data.choices[0].delta.content;
      } else {
        console.warn('Unrecognized data format:', data);
        contentChunk = JSON.stringify(data);
      }
      
      // 追加内容到流式消息
      if (streamingMessage.value) {
        streamingMessage.value.content += contentChunk;
      }
    },
    
    onError: (error) => {
      console.error('SSE error:', error);
      ElMessage.error('Failed to send message: ' + error.message);
      isStreaming.value = false;
      streamingMessage.value = null;
      currentSSEConnection = null;
    },
    
    onComplete: () => {
      console.log('SSE completed');
      
      // 将流式消息添加到消息列表
      if (streamingMessage.value && streamingMessage.value.content) {
        messages.value.push({
          role: 'assistant',
          content: streamingMessage.value.content
        });
      }
      
      streamingMessage.value = null;
      isStreaming.value = false;
      currentSSEConnection = null;
      
      // 更新聊天列表
      if (!chatIds.value.includes(currentChatId.value)) {
        loadChatIds();
      }
    }
  });
}

// ========== 生命周期 ==========
onMounted(() => {
  loadChatIds();
  
  // 页面卸载时取消SSE连接
  window.addEventListener('beforeunload', () => {
    if (currentSSEConnection) {
      currentSSEConnection.abort();
    }
  });
});
</script>

<style scoped>
.chat-container {
  display: flex;
  height: 100vh;
  background: #f8f9fa;
}

.main-chat {
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

/* 输入框遮罩层 */
.input-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(
    ellipse at center,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.3) 40%,
    rgba(255, 255, 255, 0.7) 70%,
    rgba(255, 255, 255, 0.95) 100%
  );
  pointer-events: none;
  z-index: 1;
}

/* 输入框居中样式 */
.input-centered {
  position: absolute;
  bottom: 20%;
  left: 50%;
  transform: translateX(-50%);
  width: calc(100% - 80px);
  max-width: 800px;
  z-index: 10;
}

.input-centered :deep(.chat-input-container) {
  background: white;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 2px 8px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(102, 126, 234, 0.1);
  border-radius: 24px;
  backdrop-filter: blur(10px);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.input-centered :deep(.chat-input-container:hover) {
  box-shadow: 
    0 12px 48px rgba(102, 126, 234, 0.2),
    0 4px 16px rgba(0, 0, 0, 0.1),
    0 0 0 2px rgba(102, 126, 234, 0.2);
  transform: translateY(-2px);
}

/* 响应式 */
@media (max-width: 768px) {
  .input-centered {
    width: calc(100% - 40px);
    bottom: 15%;
  }
}
</style>