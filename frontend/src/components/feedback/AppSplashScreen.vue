<script setup lang="ts">
import splashCoinUrl from '@/assets/images/splash/splash-coin.png'
import splashHeartUrl from '@/assets/images/splash/splash-heart.png'
import splashPigUrl from '@/assets/images/splash/splash-pig.png'
import splashPopUrl from '@/assets/images/splash/splash-pop.png'
import { onBeforeUnmount, onMounted } from 'vue'

let previousHtmlOverflow = ''
let previousBodyOverflow = ''

onMounted(() => {
  previousHtmlOverflow = document.documentElement.style.overflow
  previousBodyOverflow = document.body.style.overflow
  document.documentElement.style.overflow = 'hidden'
  document.body.style.overflow = 'hidden'
})

onBeforeUnmount(() => {
  document.documentElement.style.overflow = previousHtmlOverflow
  document.body.style.overflow = previousBodyOverflow
})
</script>

<template>
  <div class="splash-screen" role="status" aria-label="깨비 서비스를 불러오는 중입니다">
    <div class="splash-screen__glow" aria-hidden="true"></div>

    <div class="splash-screen__content">
      <div class="splash-screen__visual" aria-hidden="true">
        <img
          class="splash-screen__ornament splash-screen__ornament--coin"
          :src="splashCoinUrl"
          alt=""
        />
        <img
          class="splash-screen__ornament splash-screen__ornament--heart"
          :src="splashHeartUrl"
          alt=""
        />
        <img
          class="splash-screen__ornament splash-screen__ornament--pop"
          :src="splashPopUrl"
          alt=""
        />
        <img class="splash-screen__pig" :src="splashPigUrl" alt="" />
      </div>

      <div class="splash-screen__copy">
        <h1 aria-label="우리 아이 자산관리 서비스">
          우리 <strong>아</strong>이 <strong>자</strong>산관리 서비<strong>스</strong>
        </h1>
        <span>아이의 오늘을 기록하고 내일의 자산을 키워요</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.splash-screen {
  position: fixed;
  z-index: 1000;
  inset: 0;
  display: grid;
  min-width: 320px;
  width: 100%;
  height: 100dvh;
  place-items: center;
  overflow: hidden;
  background: linear-gradient(180deg, #eaf8ff 0%, #dff4ff 50%, #eefaff 100%);
  color: var(--color-text-primary);
}

.splash-screen__glow {
  position: absolute;
  top: 8%;
  left: 50%;
  width: min(78vw, 340px);
  aspect-ratio: 1;
  border-radius: 50%;
  background: rgb(255 255 255 / 62%);
  filter: blur(18px);
  transform: translateX(-50%);
}

.splash-screen__content {
  position: relative;
  display: grid;
  width: min(100%, var(--app-max-width));
  justify-items: center;
  padding: 24px 30px max(48px, env(safe-area-inset-bottom));
  text-align: center;
}

.splash-screen__visual {
  position: relative;
  width: min(88vw, 378px);
  aspect-ratio: 1 / 0.93;
}

.splash-screen__pig {
  position: absolute;
  z-index: 2;
  left: 50%;
  bottom: 0;
  width: 68%;
  height: auto;
  object-fit: contain;
  filter: drop-shadow(0 18px 24px rgb(235 123 155 / 18%));
  animation: splash-pig-in 720ms cubic-bezier(0.2, 0.85, 0.32, 1.15) both;
}

.splash-screen__ornament {
  position: absolute;
  z-index: 3;
  display: block;
  height: auto;
  object-fit: contain;
  filter: drop-shadow(0 8px 12px rgb(216 169 24 / 18%));
}

.splash-screen__ornament--coin {
  top: 8%;
  right: 1%;
  width: 16%;
  animation:
    splash-float-in 620ms 260ms ease-out both,
    splash-float 2.8s 900ms ease-in-out infinite;
}

.splash-screen__ornament--heart {
  right: 2%;
  bottom: 5%;
  width: 13%;
  animation:
    splash-float-in 620ms 400ms ease-out both,
    splash-float 3.1s 1s ease-in-out infinite reverse;
}

.splash-screen__ornament--pop {
  top: 40%;
  left: 1%;
  width: 16%;
  animation:
    splash-float-in 620ms 520ms ease-out both,
    splash-float 2.6s 1.15s ease-in-out infinite;
}

.splash-screen__copy {
  position: relative;
  z-index: 4;
  width: min(100%, 340px);
  margin-top: 30px;
  animation: splash-copy-in 520ms 520ms ease-out both;
}

.splash-screen__copy h1 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 28px;
  font-weight: 800;
  line-height: 1.4;
  letter-spacing: -0.04em;
}

.splash-screen__copy h1 strong {
  display: inline-block;
  color: var(--color-brand-primary-pressed);
  font-size: 35px;
  font-weight: 900;
  line-height: 1;
  text-shadow: 0 5px 16px rgb(85 192 244 / 24%);
  animation: splash-letter-pop 480ms cubic-bezier(0.2, 0.9, 0.35, 1.3) both;
}

.splash-screen__copy h1 strong:nth-of-type(1) {
  animation-delay: 650ms;
}

.splash-screen__copy h1 strong:nth-of-type(2) {
  animation-delay: 780ms;
}

.splash-screen__copy h1 strong:nth-of-type(3) {
  animation-delay: 910ms;
}

.splash-screen__copy span {
  display: block;
  margin-top: 10px;
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 500;
}

.splash-screen__loading {
  width: min(54%, 172px);
  height: 5px;
  margin: 24px auto 0;
  overflow: hidden;
  border-radius: 999px;
  background: rgb(255 255 255 / 86%);
  box-shadow: inset 0 0 0 1px rgb(85 192 244 / 9%);
}

.splash-screen__loading span {
  display: block;
  width: 100%;
  height: 100%;
  margin: 0;
  border-radius: inherit;
  background: var(--color-brand-primary);
  transform: scaleX(0);
  transform-origin: left center;
  animation: splash-loading 1.65s 320ms cubic-bezier(0.22, 0.7, 0.28, 1) both;
}

@keyframes splash-pig-in {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(28px) scale(0.82) rotate(-3deg);
  }

  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0) scale(1) rotate(0);
  }
}

@keyframes splash-float-in {
  from {
    opacity: 0;
    transform: translateY(14px) scale(0.55) rotate(-12deg);
  }

  to {
    opacity: 1;
    transform: translateY(0) scale(1) rotate(0);
  }
}

@keyframes splash-float {
  0%,
  100% {
    transform: translateY(0) rotate(-2deg);
  }

  50% {
    transform: translateY(-8px) rotate(3deg);
  }
}

@keyframes splash-copy-in {
  from {
    opacity: 0;
    transform: translateY(12px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes splash-letter-pop {
  0% {
    color: var(--color-text-primary);
    opacity: 0;
    transform: translateY(8px) scale(0.55) rotate(-8deg);
  }

  70% {
    transform: translateY(-3px) scale(1.14) rotate(3deg);
  }

  100% {
    color: var(--color-brand-primary-pressed);
    opacity: 1;
    transform: translateY(0) scale(1) rotate(0);
  }
}

@keyframes splash-loading {
  0% {
    transform: scaleX(0);
  }

  72% {
    transform: scaleX(0.78);
  }

  100% {
    transform: scaleX(1);
  }
}

@media (max-height: 700px) {
  .splash-screen__visual {
    width: min(62vh, 310px);
  }

  .splash-screen__copy {
    margin-top: 16px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .splash-screen__pig,
  .splash-screen__ornament,
  .splash-screen__copy,
  .splash-screen__copy h1 strong,
  .splash-screen__loading span {
    animation: none;
  }

  .splash-screen__loading span {
    transform: scaleX(1);
  }
}
</style>
