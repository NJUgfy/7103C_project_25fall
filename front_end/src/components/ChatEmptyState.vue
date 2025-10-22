<template>
  <div class="empty-container">
    <div class="empty-state">
      <div class="empty-icon">💬</div>
      <h3 class="empty-title">Start New Conversation</h3>
      <p class="empty-desc">Ask anything to get intelligent responses</p>
    </div>
  </div>
</template>

<script setup>
// 
</script>

<style scoped>
.empty-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  padding: 40px 20px;
  position: relative;
}

.empty-state {
  text-align: center;
  max-width: 600px;
  animation: fadeIn 0.6s ease-out;
  transform: translateY(-60px);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-70px);
  }
  to {
    opacity: 1;
    transform: translateY(-60px);
  }
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 28px;
  opacity: 0.8;
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.1));
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-12px);
  }
}

.empty-title {
  font-size: 36px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 16px 0;
  letter-spacing: -0.5px;
}

.empty-desc {
  font-size: 18px;
  color: #7f8c8d;
  margin: 0;
  font-weight: 400;
}

@media (max-width: 768px) {
  .empty-state {
    transform: translateY(-40px);
  }
  
  @keyframes fadeIn {
    from {
      opacity: 0;
      transform: translateY(-50px);
    }
    to {
      opacity: 1;
      transform: translateY(-40px);
    }
  }
}
</style>