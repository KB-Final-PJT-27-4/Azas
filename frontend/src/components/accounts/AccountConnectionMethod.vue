<script setup lang="ts">
import { ChevronRight, Landmark, Plus } from 'lucide-vue-next'

defineEmits<{
  import: []
  create: []
}>()
</script>

<template>
  <section
    class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col px-5 pt-7 pb-5"
    aria-labelledby="account-connection-title"
  >
    <header>
      <h1
        id="account-connection-title"
        class="text-[22px] leading-tight font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]"
      >
        계좌를 연결해 볼까요?
      </h1>
      <p class="mt-2 text-sm leading-6 text-[var(--color-text-secondary)]">
        내게 맞는 방법으로 계좌를 연결해주세요.
      </p>
    </header>

    <div class="mt-5 grid min-h-0 flex-1 grid-rows-2 gap-3.5">
      <button
        class="connection-option connection-option--primary group flex h-full min-h-[180px] w-full flex-col items-stretch justify-between overflow-hidden rounded-[24px] border border-[#cfe8f4] bg-white p-5 text-left"
        type="button"
        @click="$emit('import')"
      >
        <span class="flex items-start justify-between gap-4">
          <span class="inline-flex h-7 items-center rounded-full bg-[#e9f8ff] px-3 text-[12px] font-bold text-[var(--color-selected-text)]">
            가장 간편해요
          </span>
          <span class="grid size-14 shrink-0 place-items-center rounded-[18px] bg-[#eef9fe] text-[var(--color-selected-text)]">
            <Landmark :size="28" :stroke-width="2.1" aria-hidden="true" />
          </span>
        </span>

        <span class="my-3 block min-w-0">
          <strong class="block text-[21px] font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]">계좌 정보 불러오기</strong>
          <span class="mt-2 block break-keep text-sm leading-6 text-[var(--color-text-secondary)]">
            이미 가지고 있는 KB국민은행 계좌를<br />안전하고 빠르게 연결해요.
          </span>
        </span>

        <span class="flex h-11 items-center justify-between rounded-[14px] bg-[#f0faff] px-4 text-sm font-bold text-[var(--color-selected-text)]">
          내 계좌 확인하기
          <ChevronRight class="transition-transform group-active:translate-x-1" :size="20" :stroke-width="2.4" aria-hidden="true" />
        </span>
      </button>

      <button
        class="connection-option group flex h-full min-h-[180px] w-full flex-col items-stretch justify-between overflow-hidden rounded-[24px] border border-[#f0e5c5] bg-white p-5 text-left"
        type="button"
        @click="$emit('create')"
      >
        <span class="flex items-start justify-between gap-4">
          <span class="inline-flex h-7 items-center rounded-full bg-[#fff7df] px-3 text-[12px] font-bold text-[#b68017]">
            계좌가 없다면
          </span>
          <span class="grid size-14 shrink-0 place-items-center rounded-[18px] bg-[#fff7df] text-[#e7a52c]">
            <Plus :size="29" :stroke-width="2.4" aria-hidden="true" />
          </span>
        </span>

        <span class="my-3 block min-w-0">
          <strong class="block text-[21px] font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]">계좌 개설하기</strong>
          <span class="mt-2 block break-keep text-sm leading-6 text-[var(--color-text-secondary)]">
            아이의 자산 관리를 시작할<br />KB국민은행 계좌를 새로 만들어요.
          </span>
        </span>

        <span class="flex h-11 items-center justify-between rounded-[14px] bg-[#fff9e9] px-4 text-sm font-bold text-[#9f7418]">
          새 계좌 만들기
          <ChevronRight class="text-[#b69244] transition-transform group-active:translate-x-1" :size="20" :stroke-width="2.4" aria-hidden="true" />
        </span>
      </button>
    </div>
  </section>
</template>

<style scoped>
.connection-option {
  box-shadow: 0 5px 18px rgb(43 83 105 / 3%);
  transition: border-color 150ms ease, box-shadow 150ms ease, transform 150ms ease;
}

.connection-option--primary {
  box-shadow: 0 5px 18px rgb(43 83 105 / 3%);
}

.connection-option:active {
  border-color: rgb(45 169 232 / 32%);
  box-shadow: 0 3px 12px rgb(43 83 105 / 6%);
  transform: scale(0.985);
}

.connection-option:focus-visible {
  outline: 3px solid rgb(45 169 232 / 20%);
  outline-offset: 2px;
}

@media (prefers-reduced-motion: reduce) {
  .connection-option,
  .connection-option :deep(svg) {
    transition-duration: 1ms;
  }
}
</style>
