<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

import completePigImage from '@/assets/images/accounts/complete-pig.png'
import googleLoginImage from '@/assets/images/login/google-login.png'
import kakaoLoginImage from '@/assets/images/login/kakao_login.png'
import logoPigImage from '@/assets/images/login/logo-pig.png'
import reportPigImage from '@/assets/images/reports/report-pig-graph.png'
import splashPigImage from '@/assets/images/splash/splash-pig.png'
import { getOAuthErrorMessage, type OAuthProvider } from '@/api/auth'
import { startOAuthLogin } from '@/utils/oauth'

const slides = [
  { kicker: '함께 시작하는 금융 습관', title: '아이의 오늘을 모아\n든든한 내일로', description: '작은 저축부터 소중한 성장 기록까지\n한곳에서 차근차근 쌓아가요.', image: splashPigImage, tone: '#fff8e5', accent: '#f2b72f' },
  { kicker: '눈에 보이는 자산 성장', title: '목표를 세우고\n함께 키워가요', description: '목표와 적금을 연결하고\n아이의 자산 변화를 한눈에 확인해요.', image: reportPigImage, tone: '#edf8ff', accent: '#52bbed' },
  { kicker: '우리 가족만의 기록', title: '성장의 모든 순간을\n오래도록 간직해요', description: '가족과 함께 관리하고 기록하며\n아이의 미래를 다정하게 준비해요.', image: completePigImage, tone: '#fff1f5', accent: '#f28daa' },
] as const

const currentSlide = ref(0)
const route = useRoute()
const isLoginOpen = ref(false)
const errorMessage = ref('')
const touchStartX = ref(0)
const sheetTouchStartY = ref(0)
const sheetDragY = ref(0)
const isSheetDragging = ref(false)
const activeSlide = computed(() => slides[currentSlide.value] ?? slides[0]!)
const canStart = computed(() => currentSlide.value === slides.length - 1)

const setSlide = (index: number) => { currentSlide.value = Math.max(0, Math.min(index, slides.length - 1)) }
const openLogin = () => { errorMessage.value = ''; isLoginOpen.value = true }
const closeLogin = () => { isLoginOpen.value = false }
const handleSheetTouchStart = (event: TouchEvent) => {
  sheetTouchStartY.value = event.touches[0]?.clientY ?? 0
  sheetDragY.value = 0
  isSheetDragging.value = true
}
const handleSheetTouchMove = (event: TouchEvent) => {
  const currentY = event.touches[0]?.clientY ?? sheetTouchStartY.value
  sheetDragY.value = Math.max(0, currentY - sheetTouchStartY.value)
}
const handleSheetTouchEnd = () => {
  isSheetDragging.value = false
  if (sheetDragY.value > 90) closeLogin()
  sheetDragY.value = 0
}
const handleTouchStart = (event: TouchEvent) => { touchStartX.value = event.touches[0]?.clientX ?? 0 }
const handleTouchEnd = (event: TouchEvent) => {
  const distance = (event.changedTouches[0]?.clientX ?? touchStartX.value) - touchStartX.value
  if (Math.abs(distance) >= 45) setSlide(currentSlide.value + (distance < 0 ? 1 : -1))
}
const handleKeydown = (event: KeyboardEvent) => {
  if (event.key === 'Escape' && isLoginOpen.value) closeLogin()
  if (isLoginOpen.value) return
  if (event.key === 'ArrowRight') setSlide(currentSlide.value + 1)
  if (event.key === 'ArrowLeft') setSlide(currentSlide.value - 1)
}
onMounted(() => window.addEventListener('keydown', handleKeydown))
onBeforeUnmount(() => window.removeEventListener('keydown', handleKeydown))

const login = (provider: OAuthProvider) => {
  errorMessage.value = ''
  try {
    const inviteToken = typeof route.query.inviteToken === 'string' ? route.query.inviteToken : ''
    const inviteeType = route.query.inviteeType
    startOAuthLogin(provider, inviteToken && (inviteeType === 'PARENT' || inviteeType === 'CHILD')
      ? { inviteToken, inviteeType }
      : undefined)
  }
  catch (error) { errorMessage.value = getOAuthErrorMessage(error) }
}
</script>

<template>
  <main class="relative min-h-dvh overflow-hidden bg-white text-[var(--color-text-primary)]">
    <section class="onboarding-shell relative z-1 mx-auto w-full max-w-[620px] px-6 max-[360px]:px-5" aria-labelledby="onboarding-title" @touchstart.passive="handleTouchStart" @touchend.passive="handleTouchEnd">
      <header class="flex shrink-0 items-center gap-2.5" aria-label="우리 아이 자산관리 서비스">
        <img class="-ml-1 h-9 w-10 shrink-0 object-contain" :src="logoPigImage" alt="" />
        <p class="m-0 whitespace-nowrap text-[15px] leading-none font-extrabold tracking-[-0.035em]">우리 <span class="text-[#f28daa]">아</span>이 <span class="text-[#f28daa]">자</span>산관리 서비<span class="text-[#f28daa]">스</span></p>
      </header>

      <div class="onboarding-content">
        <div class="onboarding-stage relative shrink-0 overflow-hidden">
          <Transition name="slide-copy" mode="out-in">
            <article
              :key="currentSlide"
              class="absolute inset-0 flex flex-col items-center justify-center overflow-hidden rounded-[32px] px-6 py-6 text-center max-[380px]:py-4"
              :style="{ background: `linear-gradient(180deg, #ffffff 0%, #ffffff 24%, ${activeSlide.tone} 100%)` }"
            >
            <div class="flex w-full flex-col items-center">
              <p class="mb-2 text-[13px] font-extrabold" :style="{ color: activeSlide.accent }">{{ activeSlide.kicker }}</p>
              <h1 id="onboarding-title" class="m-0 whitespace-pre-line text-[clamp(27px,7.3vw,37px)] leading-[1.22] font-extrabold tracking-[-0.055em]">{{ activeSlide.title }}</h1>
              <p class="mt-3 mb-0 whitespace-pre-line text-[14px] leading-[1.65] font-medium text-[var(--color-text-secondary)]">{{ activeSlide.description }}</p>
            </div>

            <div
              class="relative mt-5 flex w-full items-center justify-center max-[380px]:mt-3"
              :class="currentSlide === 0 ? 'h-[225px]' : 'h-[min(30vh,255px)] max-[380px]:h-[min(27vh,215px)]'"
            >
              <img
                class="relative z-1 object-contain drop-shadow-[0_14px_18px_rgba(84,101,128,0.06)] transition-all duration-200"
                :class="currentSlide === 0 ? 'onboarding-first-image' : 'h-[88%] w-[min(68vw,320px)]'"
                :style="currentSlide === 0 ? { width: '200px', height: '180px', maxWidth: '215px', maxHeight: '205px' } : undefined"
                :src="activeSlide.image"
                :alt="`${activeSlide.kicker} 일러스트`"
              />
            </div>
            </article>
          </Transition>
        </div>

        <div class="onboarding-dots flex items-center justify-center gap-0.5 pt-2" aria-label="서비스 소개 화면 선택">
          <button v-for="(_, index) in slides" :key="index" type="button" class="onboarding-dot-hit" :aria-label="`${index + 1}번째 소개 보기`" :aria-current="index === currentSlide ? 'step' : undefined" @click="setSlide(index)">
            <span class="onboarding-dot" :class="{ 'is-active': index === currentSlide }" />
          </button>
        </div>
      </div>

      <div class="onboarding-footer relative z-3 min-h-0">
        <button
          type="button"
          class="h-14 w-full shrink-0 rounded-[18px] border-0 bg-[var(--color-brand-primary)] text-[17px] font-extrabold text-white transition-[background-color,color,transform] active:scale-[0.985] disabled:cursor-not-allowed disabled:bg-[var(--color-disabled-background)] disabled:text-[var(--color-unselected-text)] disabled:active:scale-100"
          :disabled="!canStart"
          @click="openLogin"
        >
          시작하기
        </button>
      </div>
    </section>

    <Transition name="login-sheet">
      <div v-if="isLoginOpen" class="fixed inset-0 z-30 flex items-end justify-center bg-black/55" role="presentation" @click.self="closeLogin">
        <section
          class="login-sheet-panel relative w-full max-w-[720px] overflow-hidden rounded-t-[32px] bg-white px-6 pt-3 pb-[max(28px,env(safe-area-inset-bottom))] shadow-none max-[360px]:px-5"
          :class="{ 'is-dragging': isSheetDragging }"
          :style="sheetDragY > 0 ? { transform: `translateY(${sheetDragY}px)` } : undefined"
          role="dialog"
          aria-modal="true"
          aria-labelledby="social-login-title"
        >
          <div
            class="-mx-6 -mt-3 flex h-11 w-[calc(100%+3rem)] touch-none cursor-grab items-center justify-center active:cursor-grabbing max-[360px]:-mx-5 max-[360px]:w-[calc(100%+2.5rem)]"
            aria-label="아래로 밀어 로그인 창 닫기"
            @touchstart.stop="handleSheetTouchStart"
            @touchmove.stop.prevent="handleSheetTouchMove"
            @touchend.stop="handleSheetTouchEnd"
          >
            <span class="h-1.5 w-12 rounded-full bg-[#d8e1e6]" aria-hidden="true" />
          </div>
          <div class="mt-1 flex items-start justify-between gap-4">
            <div>
              <p class="mb-1.5 text-sm font-bold text-[var(--color-brand-primary)]">반가워요!</p>
              <h2 id="social-login-title" class="m-0 text-[27px] leading-[1.25] font-extrabold tracking-[-0.045em]">우리 가족의 첫걸음을<br />함께 시작해요</h2>
            </div>
          </div>
          <p class="mt-3 mb-7 text-[15px] leading-6 text-[var(--color-text-secondary)]">간편 로그인으로 안전하고 빠르게 시작할 수 있어요.</p>
          <div class="grid gap-3">
            <button class="grid h-15 w-full grid-cols-[40px_1fr_40px] items-center rounded-[18px] border-0 bg-[#f3f6f8] px-4 text-[17px] font-bold text-[var(--color-text-primary)] transition-[filter,transform] active:scale-[0.99] active:brightness-95" type="button" @click="login('google')"><img class="size-8 object-contain" :src="googleLoginImage" alt="" /><span>Google로 계속하기</span></button>
            <button class="grid h-15 w-full grid-cols-[40px_1fr_40px] items-center rounded-[18px] border-0 bg-[#fee500] px-4 text-[17px] font-extrabold text-[#171600] transition-[filter,transform] active:scale-[0.99] active:brightness-95" type="button" @click="login('kakao')"><img class="size-8 object-contain" :src="kakaoLoginImage" alt="" /><span>카카오로 계속하기</span></button>
            <p v-if="errorMessage" class="m-0 px-2 text-center text-sm leading-6 font-semibold text-[var(--color-danger,#ef5267)]" role="alert">{{ errorMessage }}</p>
          </div>
        </section>
      </div>
    </Transition>
  </main>
</template>

<style scoped>
.onboarding-shell {
  box-sizing: border-box;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  height: 100dvh;
  min-height: 0;
  padding-top: max(16px, env(safe-area-inset-top));
  padding-bottom: max(44px, env(safe-area-inset-bottom));
}
.onboarding-content {
  display: flex;
  min-height: 0;
  flex-direction: column;
  align-self: center;
  width: 100%;
  gap: 8px;
}
.onboarding-stage {
  width: 100%;
  height: min(60vh, 580px);
  min-height: 0;
}
.onboarding-footer {
  display: block;
}
.onboarding-dots {
  min-height: 18px;
}
.onboarding-dot-hit {
  display: grid;
  width: 20px;
  height: 18px;
  padding: 0;
  place-items: center;
  border: 0;
  background: transparent;
}
.onboarding-dot {
  display: block;
  width: 8px;
  height: 8px;
  border-radius: 999px;
  background: #d9dee4;
  transition: background-color 260ms ease, transform 260ms ease;
}
.onboarding-dot.is-active {
  background: var(--color-brand-primary);
  transform: scale(1.12);
}
.slide-copy-enter-active, .slide-copy-leave-active, .slide-visual-enter-active, .slide-visual-leave-active { transition: opacity 170ms ease, transform 240ms cubic-bezier(0.22, 1, 0.36, 1); }
.slide-copy-enter-from, .slide-visual-enter-from { opacity: 0; transform: translateX(22px); }
.slide-copy-leave-to, .slide-visual-leave-to { opacity: 0; transform: translateX(-16px); }
.login-sheet-enter-active, .login-sheet-leave-active { transition: background-color 260ms ease; }
.login-sheet-enter-active > section, .login-sheet-leave-active > section { transition: transform 380ms cubic-bezier(0.22, 1, 0.36, 1); }
.login-sheet-panel { transition: transform 260ms cubic-bezier(0.22, 1, 0.36, 1); }
.login-sheet-panel.is-dragging { transition: none; }
.login-sheet-enter-from, .login-sheet-leave-to { background-color: transparent; }
.login-sheet-enter-from > section, .login-sheet-leave-to > section { transform: translateY(100%); }
@media (prefers-reduced-motion: reduce) {
  .slide-copy-enter-active, .slide-copy-leave-active, .slide-visual-enter-active, .slide-visual-leave-active, .login-sheet-enter-active, .login-sheet-leave-active, .login-sheet-enter-active > section, .login-sheet-leave-active > section { transition-duration: 1ms; }
}
@media (max-width: 380px) {
  .onboarding-shell {
    padding-top: max(14px, env(safe-area-inset-top));
    padding-bottom: max(30px, env(safe-area-inset-bottom));
  }
  .onboarding-stage {
    height: min(55vh, 470px);
  }
}
</style>
