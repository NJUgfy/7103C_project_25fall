<template>
  <div class="messages-container" ref="containerRef">
    <div class="messages-wrapper">
      <ChatMessage 
        v-for="(msg, index) in messages" 
        :key="index"
        :message="msg"
      />
      <ChatMessage 
        v-if="streamingMessage"
        :message="streamingMessage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue';
import ChatMessage from './ChatMessage.vue';

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  streamingMessage: {
    type: Object,
    default: null
  }
});

const containerRef = ref(null);

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    if (containerRef.value) {
      containerRef.value.scrollTop = containerRef.value.scrollHeight;
    }
  });
}

// 监听消息变化自动滚动
watch(() => props.messages.length, () => {
  scrollToBottom();
});

watch(() => props.streamingMessage?.content, () => {
  scrollToBottom();
});

// 暴露方法给父组件
defineExpose({
  scrollToBottom
});
</script>

<style scoped>
.messages-container {
  flex: 1;
  overflow-y: auto;
  background: linear-gradient(to bottom, #f8f9fa 0%, #ffffff 100%);
  scroll-behavior: smooth;
}

.messages-container::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track {
  background: transparent;
}

.messages-container::-webkit-scrollbar-thumb {
  background: #d0d0d0;
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover {
  background: #b0b0b0;
}

.messages-wrapper {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px 120px;
  min-height: 100%;
}
</style>