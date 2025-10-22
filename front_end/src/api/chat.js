import request from "../utils/request";
import { fetchEventSource } from '@microsoft/fetch-event-source';

/**
 * 获取所有聊天会话ID列表
 * @returns {Promise} 
 */
export function getChatIds() {
  return request({
    url: '/ai/history/getChatIds',
    method: 'get'
  });
}

/**
 * 根据chatId获取聊天历史记录
 * @param {string} chatId - 聊天会话ID
 * @returns {Promise} 
 */
export function getChatHistory(chatId) {
  return request({
    url: `/ai/history/get/${chatId}`,
    method: 'get',
  });
}

/**
 * 发送聊天消息（SSE流式响应）
 * 使用 @microsoft/fetch-event-source 处理 POST 请求的 SSE
 * 
 * @param {Object} params
 * @param {string} params.chatId - 聊天会话ID
 * @param {string} params.message - 用户消息内容
 * @param {function} params.onMessage - 接收消息回调 (data) => void
 * @param {function} params.onError - 错误回调 (error) => void
 * @param {function} params.onComplete - 完成回调 () => void
 * @returns {Object} 返回 { abort: function } 用于取消请求
 */
export function sendChatMessage({ chatId, message, onMessage, onError, onComplete }) {
  // const fullUrl = 'http://localhost:3000/api/ai/chat'; // 本地调试服务器地址
   const baseURL = request.defaults.baseURL || '';
   const fullUrl = `${baseURL}/ai/chat`;
  
  console.log('调试信息:');
  console.log('完整URL:', fullUrl);
  console.log('请求数据:', { chatId, message });
  
  const controller = new AbortController();
  let isComplete = false;
  
  fetchEventSource(fullUrl, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ chatId, message }),
    signal: controller.signal,
    
    async onopen(response) {
      console.log('SSE连接已打开', response.status);
      
      if (response.ok) {
        return;
      } else if (response.status >= 400 && response.status < 500 && response.status !== 429) {
        const errorText = await response.text();
        console.error('客户端错误:', response.status, errorText);
        throw new Error(`HTTP ${response.status}: ${errorText}`);
      } else {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
    },
    
    onmessage(event) {
      console.log('收到SSE消息:', event.data);
      
      if (event.data === '[DONE]' || event.data === 'DONE') {
        console.log('收到完成标记');
        isComplete = true;
        controller.abort();
        onComplete();
        return;
      }
      
      try {
        const data = JSON.parse(event.data);
        onMessage(data);
      } catch (error) {
        console.warn('解析JSON失败，作为纯文本处理:', event.data);
        onMessage({ content: event.data });
      }
    },
    
    onerror(error) {
      console.error('SSE错误:', error);
      if (isComplete) return;
      onError(error);
      throw error;
    },
    
    onclose() {
      console.log('SSE连接已关闭');
      if (!isComplete) {
        onComplete();
      }
    },
    
    openWhenHidden: false,
  });
  
  return {
    abort: () => {
      console.log('手动中止SSE连接');
      controller.abort();
    }
  };
}