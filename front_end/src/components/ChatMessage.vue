<template>
  <div :class="['message', message.role]">
    <div class="message-avatar">
      <!-- AI头像 -->
      <img v-if="message.role === 'assistant'" 
           src="@/assets/crypto-investment-logo.svg" 
           alt="AI" 
           class="avatar-logo" />
      <!-- 用户头像 -->
      <span v-else class="avatar-content">Me</span>
    </div>
    <div class="message-content">
      <div 
        class="message-text markdown-body" 
        v-html="renderedContent"
      ></div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { marked } from 'marked';
import hljs from 'highlight.js';
import 'highlight.js/styles/github-dark.css'; 


marked.setOptions({
  highlight: function(code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value;
      } catch (err) {
        console.error('Highlight error:', err);
      }
    }
    return hljs.highlightAuto(code).value;
  },
  breaks: true, 
  gfm: true, // 启用 GitHub Flavored Markdown
});

const renderedContent = computed(() => {
  try {
    let content = props.message.content || '';
    content = content.replace(/(\*\*[^*]+\*\*)：\s*\n+/g, '$1：\n');
    content = content.replace(/\n{2,}(-\s)/g, '\n$1');
    content = content.replace(/\n{3,}/g, '\n\n');
    
    return marked.parse(content);
  } catch (error) {
    console.error('Markdown parse error:', error);
    return props.message.content;
  }
});


const props = defineProps({
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
  overflow: hidden;
}

.message.user .message-avatar {
  background: #EBC97F;
  color: white;
  box-shadow: 0 2px 8px rgba(235, 201, 127, 0.3);
}

.message.assistant .message-avatar {
  background: white;
  box-shadow: 0 2px 8px rgba(218, 165, 32, 0.2);
  padding: 2px;
}

.avatar-content {
  font-size: 13px;
}

.avatar-logo {
  width: 100%;
  height: 100%;
  object-fit: contain;
  transform: scale(2);
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

/* AI助手消息气泡 */
.message.assistant .message-content {
  background: white;
  color: #2c3e50;
  border-top-left-radius: 4px;
  border: 1px solid #e8e8e8;
}

/* 用户消息气泡 */
.message.user .message-content {
  background: #EBC97F;
  color: #2c3e50;
  border-top-right-radius: 4px;
  border: 1px solid rgba(235, 201, 127, 0.3);
}

.message-text {
  word-wrap: break-word;
  line-height: 1.5;
  font-size: 14px;
  /* white-space: pre-wrap; */
}

.message-time {
  font-size: 11px;
  margin-top: 6px;
  opacity: 0.7;
}

.message.user .message-time {
  text-align: right;
  color: rgba(44, 62, 80, 0.7);
}

.message.assistant .message-time {
  color: #999;
}


/* 重置 markdown-body 的一些默认样式 */
.markdown-body {
  font-size: 14px;
  line-height: 1.5;
  color: inherit;
  background: transparent;
}

/* 标题样式 */
.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin-bottom: 4px !important;
  font-weight: 600;
  line-height: 1.3;
}

.markdown-body :deep(h1) { font-size: 1.8em; }
.markdown-body :deep(h2) { font-size: 1.5em; }
.markdown-body :deep(h3) { font-size: 1.25em; }
.markdown-body :deep(h4) { font-size: 1.1em; }

/* 段落样式 */
.markdown-body :deep(p) {
  margin: 0 0 4px 0;
}

.markdown-body :deep(p:last-child) {
  margin-bottom: 0;
}

/* 粗体 */
.markdown-body :deep(strong) {
  font-weight: 700;
}

/* 斜体 */
.markdown-body :deep(em) {
  font-style: italic;
}

/* 行内代码 */
.markdown-body :deep(code) {
  padding: 2px 6px;
  margin: 0 2px;
  font-size: 90%;
  background-color: rgba(0, 0, 0, 0.08);
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

/* 用户消息中的行内代码 */
.message.user .markdown-body :deep(code) {
  background-color: rgba(255, 255, 255, 0.3);
  color: #2c3e50;
}

/* 代码块 */
.markdown-body :deep(pre) {
  padding: 10px;
  overflow: auto;
  font-size: 13px;
  line-height: 1.5;
  background-color: #1e1e1e;
  border-radius: 8px;
  margin: 8px 0;
  max-width: 100%;
}

.markdown-body :deep(pre code) {
  padding: 0;
  margin: 0;
  background: transparent;
  border: 0;
  color: #e4e4e4;
  font-size: 100%;
}

/* 列表样式 */
.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 1.8em;
  margin: 4px 0;
}

.markdown-body :deep(li) {
  margin-bottom: 1px;
  line-height: 1.5;
}

.markdown-body :deep(li > p) {
  margin-bottom: 0;
}

/* 引用样式 */
.markdown-body :deep(blockquote) {
  padding: 6px 10px;
  margin: 8px 0;
  border-left: 3px solid #ddd;
  background-color: rgba(0, 0, 0, 0.03);
  border-radius: 4px;
}

/* 用户消息中的引用 */
.message.user .markdown-body :deep(blockquote) {
  border-left-color: #DAA520;
  background-color: rgba(218, 165, 32, 0.1);
}

.markdown-body :deep(blockquote p) {
  margin: 0;
}

/* 链接样式 - 金色 */
.markdown-body :deep(a) {
  color: #DAA520;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.2s;
}

.markdown-body :deep(a:hover) {
  border-bottom-color: #DAA520;
}

/* 用户消息中的链接 */
.message.user .markdown-body :deep(a) {
  color: #2c3e50;
  font-weight: 500;
  border-bottom-color: rgba(44, 62, 80, 0.3);
}

.message.user .markdown-body :deep(a:hover) {
  border-bottom-color: #2c3e50;
}

/* 分隔线 */
.markdown-body :deep(hr) {
  height: 1px;
  padding: 0;
  margin: 16px 0;
  background-color: #e1e4e8;
  border: 0;
}

/* 表格样式 */
.markdown-body :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 12px 0;
  font-size: 13px;
}

.markdown-body :deep(table th),
.markdown-body :deep(table td) {
  padding: 8px 12px;
  border: 1px solid #ddd;
  text-align: left;
}

.markdown-body :deep(table th) {
  font-weight: 600;
  background-color: #f6f8fa;
}

.markdown-body :deep(table tr:nth-child(even)) {
  background-color: #f9f9f9;
}

/* 图片样式 */
.markdown-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 6px;
  margin: 8px 0;
}

/* 隐藏空的段落标签 */
.markdown-body :deep(p:empty) {
  display: none;
}

/* 隐藏只包含空白字符的段落 */
.markdown-body :deep(p) {
  margin: 0 0 4px 0;
}

/* 如果段落后面紧跟列表,段落不要下边距 */
.markdown-body :deep(p:has(+ ul)),
.markdown-body :deep(p:has(+ ol)) {
  margin-bottom: 0;
}

/* 标题后面紧跟列表，列表不要上边距 */
.markdown-body :deep(h1 + ul),
.markdown-body :deep(h2 + ul),
.markdown-body :deep(h3 + ul),
.markdown-body :deep(h4 + ul),
.markdown-body :deep(h1 + ol),
.markdown-body :deep(h2 + ol),
.markdown-body :deep(h3 + ol),
.markdown-body :deep(h4 + ol) {
  margin-top: 0 !important;
}
/* 标题后紧跟段落，不要产生额外空白 */
.markdown-body :deep(h1 + p),
.markdown-body :deep(h2 + p),
.markdown-body :deep(h3 + p),
.markdown-body :deep(h4 + p) {
  margin-top: 0 !important;
}
/* 段落后面紧跟列表，列表不要上边距 */
.markdown-body :deep(p + ul),
.markdown-body :deep(p + ol) {
  margin-top: 0;
}
</style>