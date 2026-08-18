<script setup lang="ts">
import { ArrowLeft, Home } from 'lucide-vue-next'
import { useRouter } from 'vue-router'

import notFoundPigUrl from '@/assets/images/common/404-pig.png'

const router = useRouter()

const goBack = () => {
  if (window.history.state?.back) {
    router.back()
    return
  }

  router.push('/home')
}
</script>

<template>
  <main class="not-found-page">
    <section class="not-found-content" aria-labelledby="not-found-title">
      <span class="not-found-code">404 ERROR</span>

      <div class="not-found-visual" aria-hidden="true">
        <span class="not-found-spark not-found-spark--left">✦</span>
        <span class="not-found-spark not-found-spark--right">✦</span>
        <img class="not-found-pig" :src="notFoundPigUrl" alt="" />
      </div>

      <div class="not-found-copy">
        <h1 id="not-found-title">페이지를 찾지 못했어요</h1>
        <p>
          주소가 잘못 입력되었거나<br />
          찾으시는 페이지가 이동했을 수 있어요.
        </p>
      </div>

      <div class="not-found-actions">
        <RouterLink class="not-found-button not-found-button--primary" to="/home">
          홈으로 돌아가기
        </RouterLink>
        <button class="not-found-button not-found-button--secondary" type="button" @click="goBack">
          이전 페이지
        </button>
      </div>
    </section>
  </main>
</template>

<style scoped>
.not-found-page {
  position: relative;
  display: grid;
  min-height: 100dvh;
  place-items: center;
  overflow: hidden;
  padding: 28px 22px;
  background: linear-gradient(180deg, #eaf8ff 0%, #f8fcff 52%, #edf8ff 100%);
  color: var(--color-text-primary);
}

.not-found-content {
  position: relative;
  z-index: 2;
  display: flex;
  width: 100%;
  max-width: 360px;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.not-found-code {
  padding: 7px 13px;
  border: 1px solid var(--color-accent-yellow-border);
  border-radius: 999px;
  background: var(--color-accent-yellow-surface);
  color: var(--color-accent-yellow-text);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.08em;
}

.not-found-visual {
  position: relative;
  display: grid;
  width: 100%;
  min-height: min(25dvh, 11rem);
  place-items: center;
  margin-top: 8px;
}

.not-found-number {
  position: absolute;
  inset: 50% 0 auto;
  color: rgb(85 192 244 / 13%);
  font-size: 126px;
  font-weight: 900;
  line-height: 1;
  letter-spacing: -0.08em;
  transform: translateY(-50%);
}

.not-found-pig-wrap {
  position: relative;
  display: grid;
  width: 100%;
  place-items: center;
}

.not-found-pig {
  position: relative;
  z-index: 1;
  display: block;
  width: min(38%, 8.75rem) !important;
  max-width: 8.75rem !important;
  height: auto !important;
  object-fit: contain;
  filter: drop-shadow(0 12px 16px rgb(238 132 160 / 14%));
  animation: not-found-float 3.4s ease-in-out infinite;
}

.not-found-copy h1 {
  margin: 0;
  font-size: 25px;
  font-weight: 800;
  letter-spacing: -0.04em;
}

.not-found-copy p {
  margin: 12px 0 0;
  color: var(--color-text-secondary);
  font-size: 14px;
  line-height: 1.65;
}

.not-found-actions {
  display: grid;
  width: 100%;
  gap: 10px;
  margin-top: 28px;
}

.not-found-button {
  display: inline-flex;
  min-height: 52px;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid transparent;
  border-radius: 16px;
  font: inherit;
  font-size: 15px;
  font-weight: 700;
  transition:
    transform 160ms ease,
    background-color 160ms ease;
}

.not-found-button:active {
  transform: scale(0.98);
}

.not-found-button--primary {
  background: var(--color-brand-primary);
  box-shadow: 0 10px 24px rgb(85 192 244 / 22%);
  color: var(--color-text-inverse) !important;
}

.not-found-button--primary:active {
  background: var(--color-brand-primary-pressed);
}

.not-found-button--secondary {
  cursor: pointer;
  border-color: var(--color-border);
  background: rgb(255 255 255 / 82%);
  color: var(--color-text-secondary);
}

.not-found-help {
  margin: 18px 0 0;
  color: #8b9aaa;
  font-size: 11px;
}

.not-found-decoration {
  position: absolute;
  border-radius: 50%;
  filter: blur(1px);
}

.not-found-decoration--yellow {
  top: -64px;
  right: -76px;
  width: 210px;
  height: 210px;
  background: rgb(123 211 255 / 16%);
}

.not-found-decoration--blue {
  bottom: -74px;
  left: -92px;
  width: 240px;
  height: 240px;
  background: rgb(85 192 244 / 12%);
}

.not-found-spark {
  position: absolute;
  z-index: 1;
  color: var(--color-accent-yellow);
  text-shadow: 0 4px 12px rgb(247 215 81 / 28%);
  animation: not-found-twinkle 2.7s ease-in-out infinite;
}

.not-found-spark--left {
  top: 54%;
  left: 25%;
  font-size: 27px;
}

.not-found-spark--right {
  top: 25%;
  right: 25%;
  font-size: 18px;
  animation-delay: -1.2s;
}

@keyframes not-found-float {
  0%,
  100% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-7px);
  }
}

@keyframes not-found-twinkle {
  0%,
  100% {
    opacity: 0.55;
    transform: scale(0.88) rotate(-5deg);
  }

  50% {
    opacity: 1;
    transform: scale(1.08) rotate(5deg);
  }
}

@media (max-height: 700px) {
  .not-found-page {
    padding-block: 18px;
  }

  .not-found-visual {
    min-height: min(22dvh, 9.5rem);
  }

  .not-found-pig-wrap {
    width: 100%;
  }

  .not-found-pig {
    width: min(34%, 8rem) !important;
    max-width: 8rem !important;
  }

  .not-found-number {
    font-size: 106px;
  }

  .not-found-actions {
    margin-top: 20px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .not-found-pig,
  .not-found-spark {
    animation: none;
  }
}
</style>
