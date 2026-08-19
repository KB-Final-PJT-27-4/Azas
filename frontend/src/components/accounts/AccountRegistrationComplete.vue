<script setup lang="ts">
import { Landmark } from 'lucide-vue-next'
import completePigUrl from '@/assets/images/accounts/complete-pig.png'
import completeStarUrl from '@/assets/images/accounts/complete-star.png'
import completeDiamondUrl from '@/assets/images/accounts/complete-diamond.png'
import completeCircleUrl from '@/assets/images/accounts/complete-circle.png'

defineProps<{
  accounts: {
    bank: string
    accountNumber: string
    accountName: string
    balance: number
  }[]
}>()

const emit = defineEmits<{
  home: []
}>()
</script>

<template>
  <section
    class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col bg-white"
  >
    <div class="flex flex-1 flex-col px-6 pb-6 text-center">
      <div class="my-auto w-full pb-6">
        <div class="complete-scene mx-auto" aria-label="계좌 등록 완료">
          <img class="complete-scene__pig" :src="completePigUrl" alt="" />


          <span class="complete-scene__check" aria-hidden="true">
            <svg width="38" height="38" viewBox="0 0 24 24" fill="none">
              <path pathLength="1" d="M4 12.5L9.2 17.5L20 6.5" />
            </svg>
          </span>
        </div>

        <h1
          class="mt-5 text-[28px] leading-tight font-extrabold tracking-[-0.035em] text-[var(--color-text-primary)]"
        >
          계좌 등록이 완료되었어요!
        </h1>
        <p class="mt-3 text-sm leading-6 text-[var(--color-text-secondary)]">
          연결한 계좌로 아이의 자산을 관리하고<br />목표를 함께 만들어보세요.
        </p>

        <div
          class="mt-6 overflow-hidden rounded-[20px] border border-[var(--color-border)] bg-white text-left"
          aria-label="연결된 계좌 목록"
        >
          <article
            v-for="account in accounts"
            :key="`${account.bank}-${account.accountNumber}`"
            class="flex items-center border-b border-[#edf1f3] px-4 py-3.5 last:border-b-0"
          >
            <span class="grid size-12 shrink-0 place-items-center rounded-full bg-[#f1f3f4] text-[#8a9298]">
              <Landmark :size="23" :stroke-width="2" aria-hidden="true" />
            </span>
            <div class="ml-3 min-w-0 flex-1">
              <strong class="block truncate text-[15px] font-extrabold text-[var(--color-text-primary)]">
                {{ account.accountName }}
              </strong>
              <span class="mt-1 block text-xs text-[var(--color-text-secondary)]">
                {{ account.accountNumber }}
              </span>
            </div>
            <strong class="ml-3 shrink-0 text-sm font-bold text-[var(--color-text-primary)]">
              {{ account.balance.toLocaleString('ko-KR') }}원
            </strong>
          </article>
        </div>
      </div>

      <div>
        <button
          class="min-h-14 w-full rounded-xl bg-[var(--color-brand-primary)] text-base font-bold text-white transition-colors active:bg-[var(--color-brand-primary-pressed)]"
          type="button"
          @click="emit('home')"
        >
          홈으로 가기
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.complete-scene {
  position: relative;
  width: 248px;
  height: 168px;
}

.complete-scene__pig {
  position: absolute;
  z-index: 2;
  top: 0;
  left: 9px;
  width: 230px;
  height: 168px;
  object-fit: contain;
}

.complete-scene__decoration {
  position: absolute;
  z-index: 1;
  object-fit: contain;
}

.complete-scene__star { width: 45px; height: 45px; }
.complete-scene__star--left { top: 19px; left: -7px; }
.complete-scene__star--right { right: -6px; bottom: 19px; width: 40px; height: 40px; }
.complete-scene__diamond { width: 25px; height: 25px; }
.complete-scene__diamond--left { left: 8px; bottom: 33px; }
.complete-scene__diamond--right { top: 65px; right: 13px; width: 22px; height: 22px; }
.complete-scene__circle { width: 18px; height: 18px; }
.complete-scene__circle--left { top: 75px; left: 21px; }
.complete-scene__circle--right { top: 94px; right: -1px; width: 14px; height: 14px; }

.complete-scene__check {
  position: absolute;
  z-index: 3;
  top: 2px;
  right: 10px;
  display: grid;
  width: 58px;
  height: 58px;
  color: white;
  background: linear-gradient(155deg, #61c8f5 2%, #2d8dec 82%);
  border-radius: 50%;
  box-shadow: 0 8px 18px rgb(45 141 236 / 24%);
  place-items: center;
}

.complete-scene__check path {
  fill: none;
  stroke: currentColor;
  stroke-width: 3.4;
  stroke-linecap: round;
  stroke-linejoin: round;
}

@media (prefers-reduced-motion: no-preference) {
  .complete-scene__pig {
    animation: complete-pig-arrive 680ms cubic-bezier(0.16, 1, 0.3, 1) both;
  }

  .complete-scene__decoration {
    animation: complete-spark-arrive 620ms cubic-bezier(0.16, 1, 0.3, 1) both,
      complete-spark-float 2.8s ease-in-out 1.15s infinite alternate;
  }

  .complete-scene__star--left { animation-delay: 180ms, 1.15s; }
  .complete-scene__star--right { animation-delay: 290ms, 1.35s; }
  .complete-scene__diamond--left { animation-delay: 350ms, 1.25s; }
  .complete-scene__diamond--right { animation-delay: 430ms, 1.5s; }
  .complete-scene__circle--left { animation-delay: 500ms, 1.4s; }
  .complete-scene__circle--right { animation-delay: 570ms, 1.6s; }

  .complete-scene__check {
    animation: complete-check-arrive 620ms cubic-bezier(0.16, 1, 0.3, 1) 260ms both;
  }

  .complete-scene__check path {
    stroke-dasharray: 1;
    stroke-dashoffset: 1;
    animation: complete-check-draw 300ms cubic-bezier(0.25, 0.8, 0.3, 1) 560ms forwards;
  }
}

@keyframes complete-pig-arrive {
  0% { opacity: 0; transform: translateY(14px) scale(0.78); }
  65% { opacity: 1; transform: translateY(-3px) scale(1.035); }
  100% { opacity: 1; transform: translateY(0) scale(1); }
}

@keyframes complete-spark-arrive {
  0% { opacity: 0; transform: translate(0, 10px) scale(0.2) rotate(-25deg); }
  70% { opacity: 1; transform: translate(0, -2px) scale(1.18) rotate(8deg); }
  100% { opacity: 1; transform: translate(0, 0) scale(1) rotate(0); }
}

@keyframes complete-spark-float {
  from { transform: translateY(0) rotate(-3deg); }
  to { transform: translateY(-5px) rotate(5deg); }
}

@keyframes complete-check-arrive {
  0% { opacity: 0; transform: scale(0.35) rotate(-12deg); }
  68% { opacity: 1; transform: scale(1.12) rotate(3deg); }
  100% { opacity: 1; transform: scale(1) rotate(0); }
}

@keyframes complete-check-draw { to { stroke-dashoffset: 0; } }
</style>
