<script setup lang="ts">
import { Landmark } from 'lucide-vue-next'
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()
const availableProductNames = [
  'KB Young Youth 적금',
  'KB아이사랑적금',
  '내 아이를 위한 280일 적금',
] as const
const selectedProductName = computed(() => {
  const productName = String(route.query.product ?? '')
  return availableProductNames.find((name) => name === productName) ?? 'KB아이사랑적금'
})
</script>

<template>
  <main
    class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col bg-white px-6 pt-8 pb-[calc(28px+env(safe-area-inset-bottom))] text-[var(--color-text-primary)]"
  >
    <section class="flex flex-1 flex-col text-center" aria-labelledby="savings-complete-title">
      <div class="complete-mark mx-auto mt-1" aria-hidden="true">
        <span class="complete-mark__dot complete-mark__dot--yellow"></span>
        <span class="complete-mark__dot complete-mark__dot--red"></span>
        <span class="complete-mark__dot complete-mark__dot--blue"></span>
        <span class="complete-mark__dot complete-mark__dot--green"></span>
        <span class="complete-mark__circle">
          <svg
            class="complete-mark__check"
            width="54"
            height="54"
            viewBox="0 0 24 24"
            fill="none"
            aria-hidden="true"
          >
            <path pathLength="1" d="M4 12.5L9.2 17.5L20 6.5" />
          </svg>
        </span>
      </div>

      <h1
        id="savings-complete-title"
        class="mt-8 text-[28px] leading-tight font-extrabold tracking-[-0.035em]"
      >
        자녀 적금이 개설되었어요!
      </h1>
      <p class="mt-4 text-sm leading-6 text-[var(--color-text-secondary)]">
        이제 깨비의 첫 적금을<br />서비스에 연결해볼까요?
      </p>

      <article
        class="mt-8 flex items-center rounded-2xl border border-[var(--color-border)] bg-white px-5 py-5 text-left shadow-[0_5px_18px_rgba(43,83,105,0.04)]"
        aria-label="개설된 적금 정보"
      >
        <span class="grid size-14 shrink-0 place-items-center rounded-full bg-[#e1e3e5] text-[#8a9298]">
          <Landmark :size="25" :stroke-width="2" aria-hidden="true" />
        </span>
        <div class="ml-4 min-w-0">
          <strong class="block truncate text-lg font-extrabold">{{ selectedProductName }}</strong>
          <span class="mt-1 block text-sm text-[var(--color-text-secondary)]">
            123-456-789012
          </span>
          <span class="mt-1 block text-sm font-semibold text-[var(--color-text-secondary)]">
            잔액 0원
          </span>
        </div>
      </article>

      <button
        class="mt-7 min-h-14 w-full rounded-xl bg-[var(--color-brand-primary)] text-base font-bold text-white shadow-[0_7px_18px_rgba(39,169,235,0.22)] transition-colors active:bg-[var(--color-brand-primary-pressed)]"
        type="button"
        @click="router.push({ name: 'Goals' })"
      >
        목표 설정하기
      </button>
    </section>
  </main>
</template>

<style scoped>
.complete-mark {
  position: relative;
  width: 142px;
  height: 142px;
}

.complete-mark__circle {
  position: absolute;
  inset: 11px;
  display: grid;
  color: white;
  background: linear-gradient(155deg, #5bc6f5 2%, #2387e9 82%);
  border-radius: 50%;
  box-shadow: 0 13px 24px rgb(35 135 233 / 24%);
  place-items: center;
}

.complete-mark__dot {
  position: absolute;
  z-index: 1;
  width: 10px;
  height: 10px;
  border-radius: 3px;
  transform: rotate(18deg);
}

.complete-mark__dot--yellow { top: 35px; left: 5px; background: #ffb634; --burst-x: 66px; --burst-y: 36px; --dot-rotate: 18deg; }
.complete-mark__dot--red { top: 24px; right: 4px; background: #ff7d86; --burst-x: -57px; --burst-y: 47px; --dot-rotate: 40deg; }
.complete-mark__dot--blue { bottom: 13px; left: 19px; background: #62c9f4; --burst-x: 52px; --burst-y: -48px; --dot-rotate: 18deg; }
.complete-mark__dot--green { right: 14px; bottom: 14px; background: #65d09c; --burst-x: -47px; --burst-y: -47px; --dot-rotate: 42deg; }

@media (prefers-reduced-motion: no-preference) {
  .complete-mark__circle {
    animation: complete-mark-arrive 760ms cubic-bezier(0.16, 1, 0.3, 1) both,
      complete-mark-glow 720ms ease-out 620ms both;
  }

  .complete-mark__check path {
    fill: none;
    stroke: currentColor;
    stroke-width: 3.4;
    stroke-linecap: round;
    stroke-linejoin: round;
    stroke-dasharray: 1;
    stroke-dashoffset: 1;
    animation: complete-check-draw 300ms cubic-bezier(0.25, 0.8, 0.3, 1) 300ms forwards;
  }

  .complete-mark__dot {
    animation: complete-confetti-burst 760ms cubic-bezier(0.15, 0.85, 0.25, 1.18) 440ms both;
  }

  .complete-mark__dot--red { animation-delay: 490ms; }
  .complete-mark__dot--blue { animation-delay: 540ms; }
  .complete-mark__dot--green { animation-delay: 590ms; }
}

@keyframes complete-mark-arrive {
  0% { opacity: 0; transform: scale(0.48) rotate(-7deg); }
  58% { opacity: 1; transform: scale(1.09) rotate(2deg); }
  78% { transform: scale(0.96) rotate(-1deg); }
  100% { opacity: 1; transform: scale(1) rotate(0); }
}

@keyframes complete-mark-glow {
  0% { box-shadow: 0 13px 24px rgb(35 135 233 / 24%), 0 0 0 0 rgb(65 170 240 / 26%); }
  55% { box-shadow: 0 13px 24px rgb(35 135 233 / 24%), 0 0 0 16px rgb(65 170 240 / 0%); }
  100% { box-shadow: 0 13px 24px rgb(35 135 233 / 24%), 0 0 0 16px rgb(65 170 240 / 0%); }
}

@keyframes complete-check-draw {
  to { stroke-dashoffset: 0; }
}

@keyframes complete-confetti-burst {
  0% {
    opacity: 0;
    transform: translate(var(--burst-x), var(--burst-y)) scale(0.15) rotate(-80deg);
  }
  45% { opacity: 1; }
  72% { transform: translate(0, 0) scale(1.28) rotate(calc(var(--dot-rotate) + 16deg)); }
  100% { opacity: 1; transform: translate(0, 0) scale(1) rotate(var(--dot-rotate)); }
}
</style>
