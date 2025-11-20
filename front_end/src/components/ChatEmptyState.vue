<template>
  <div class="empty-container">
    <div class="empty-state">
      <!-- Logo图标 -->
      <div class="empty-icon">
        <img src="@/assets/crypto-investment-logo.svg" alt="Crypto Investment Assistant" class="logo-image" />
      </div>
      <h3 class="empty-title">Cryptocurrency Investment Assistant</h3>
      <p class="empty-desc">Intelligent analysis, Smarter investments</p>
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
  background: #FFFFFF;
  padding: 40px 20px;
  position: relative;
}

.empty-state {
  text-align: center;
  max-width: 700px;
  animation: fadeIn 0.6s ease-out;
  transform: translateY(-80px);
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
  margin-bottom: 28px;
  opacity: 0.95;
  /* filter: drop-shadow(0 4px 16px rgba(218, 165, 32, 0.2)); */
  animation: float 3s ease-in-out infinite;
  display: flex;
  justify-content: center;
  align-items: center;
}

.logo-image {
  width: 180px;
  height: 180px;
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
  color: #DAA520;
  margin: 0 0 16px 0;
  letter-spacing: -0.5px;
}

.empty-desc {
  font-size: 18px;
  color: #8B7355;
  margin: 0;
  font-weight: 400;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .empty-state {
    transform: translateY(-40px);
  }

  .logo-image {
    width: 200px;
    height: 200px;
  }
  
  .empty-title {
    font-size: 28px;
  }
  
  .empty-desc {
    font-size: 16px;
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