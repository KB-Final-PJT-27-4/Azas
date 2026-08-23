<script setup lang="ts">
import { ArrowRight, X } from 'lucide-vue-next'

defineEmits<{
  create: []
  later: []
}>()
</script>

<template>
  <section
    class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col bg-white px-5 pt-8 pb-[max(24px,env(safe-area-inset-bottom))] text-center"
    aria-labelledby="account-import-empty-title"
  >
    <div class="flex flex-1 flex-col items-center justify-center pb-15">
      <div class="account-empty-icon" aria-hidden="true">
        <span class="account-empty-icon__halo"></span>
        <span class="account-empty-icon__circle">
          <X class="account-empty-icon__x" :size="42" :stroke-width="2.8" />
        </span>
      </div>

      <h1
        id="account-import-empty-title"
        class="mt-6 text-[26px] leading-[1.3] font-extrabold tracking-[-0.04em]"
      >
        불러올 수 있는 계좌가<br />아직 없어요
      </h1>
      <p class="mt-4 break-keep text-sm leading-6 text-[var(--color-text-secondary)]">
        연결 가능한 KB국민은행 계좌를 찾지 못했어요.<br />새 계좌를 만들고 우리 아이 자산관리를 시작해보세요.
      </p>

      <div class="mt-7 w-full rounded-[20px] bg-[#f8fafb] px-5 py-4 text-left">
        <strong class="text-sm font-bold">새 계좌를 만들면</strong>
        <ul class="mt-3 grid list-none gap-2 p-0 text-[13px] text-[var(--color-text-secondary)]">
          <li class="flex items-center gap-2">
            <span class="size-1.5 rounded-full bg-[var(--color-brand-primary)]"></span>
            아이를 위한 자금을 따로 관리할 수 있어요.
          </li>
          <li class="flex items-center gap-2">
            <span class="size-1.5 rounded-full bg-[var(--color-brand-primary)]"></span>
            목표와 적금을 연결해 변화를 확인할 수 있어요.
          </li>
        </ul>
      </div>
    </div>

    <button
      class="flex min-h-14 w-full items-center justify-center gap-1.5 rounded-[16px] bg-[var(--color-brand-primary)] text-base font-bold text-white shadow-[0_7px_18px_rgba(39,169,235,0.2)] active:bg-[var(--color-brand-primary-pressed)]"
      type="button"
      @click="$emit('create')"
    >
      계좌 개설하기
      <ArrowRight :size="18" :stroke-width="2.5" />
    </button>
    <button
      class="mt-2 h-11 border-0 bg-transparent text-sm font-semibold text-[var(--color-text-secondary)]"
      type="button"
      @click="$emit('later')"
    >
      다음에 할게요
    </button>
  </section>
</template>

<style scoped>
.account-empty-icon {
  position: relative;
  display: grid;
  width: 116px;
  height: 116px;
  place-items: center;
}

.account-empty-icon__halo {
  position: absolute;
  inset: 0;
  border-radius: 999px;
  background: #fff1f3;
}

.account-empty-icon__circle {
  position: relative;
  display: grid;
  width: 78px;
  height: 78px;
  place-items: center;
  border: 1px solid #ffd9df;
  border-radius: 999px;
  color: #ef6372;
  background: white;
  box-shadow: 0 10px 24px rgb(239 99 114 / 14%);
}

@media (prefers-reduced-motion: no-preference) {
  .account-empty-icon {
    animation: account-empty-arrive 560ms cubic-bezier(0.16, 1, 0.3, 1) both;
  }

  .account-empty-icon__halo {
    animation: account-empty-pulse 2.4s ease-in-out 650ms infinite;
  }

  .account-empty-icon__x {
    animation: account-empty-x-pop 420ms cubic-bezier(0.2, 1.45, 0.4, 1) 260ms both;
  }
}

@keyframes account-empty-arrive {
  from {
    opacity: 0;
    transform: translateY(14px) scale(0.78);
  }
  to {
    opacity: 1;
    transform: none;
  }
}

@keyframes account-empty-x-pop {
  0% {
    opacity: 0;
    transform: rotate(-18deg) scale(0.45);
  }
  72% {
    opacity: 1;
    transform: rotate(4deg) scale(1.12);
  }
  100% {
    opacity: 1;
    transform: none;
  }
}

@keyframes account-empty-pulse {
  0%,
  100% {
    opacity: 0.72;
    transform: scale(0.94);
  }
  50% {
    opacity: 1;
    transform: scale(1.04);
  }
}
</style>
