<script setup lang="ts">
import { ChevronRight, Landmark, Pencil } from 'lucide-vue-next'

defineEmits<{
  import: []
  manual: []
}>()
</script>

<template>
  <section class="px-5 pt-7" aria-labelledby="account-connection-title">
    <header>
      <h1
        id="account-connection-title"
        class="text-[22px] leading-tight font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]"
      >
        계좌를 연결해 볼까요?
      </h1>
      <p class="mt-2 text-sm leading-6 text-[var(--color-text-secondary)]">
        연결하면 자산 현황 확인과 목표 관리가 더욱 쉬워져요.
      </p>
    </header>

    <div class="mt-6 grid gap-3">
      <button
        class="connection-option group flex min-h-[92px] w-full items-center rounded-2xl border border-[var(--color-border)] bg-white px-4 text-left"
        type="button"
        @click="$emit('import')"
      >
        <span class="grid size-12 shrink-0 place-items-center rounded-full bg-[#eaf8ff] text-[#2da9e8]">
          <Landmark :size="24" :stroke-width="2.2" aria-hidden="true" />
        </span>
        <span class="ml-4 min-w-0 flex-1">
          <strong class="block text-base font-bold text-[var(--color-text-primary)]">
            계좌 정보 불러오기
          </strong>
          <span class="mt-1 block text-xs leading-5 text-[var(--color-text-secondary)]">
            KB국민은행 계좌를<br />간편하게 불러와요
          </span>
        </span>
        <ChevronRight
          class="ml-3 shrink-0 text-[var(--color-unselected-text)] transition-transform group-active:translate-x-0.5"
          :size="20"
          :stroke-width="2"
          aria-hidden="true"
        />
      </button>

      <button
        class="connection-option group flex min-h-[92px] w-full items-center rounded-2xl border border-[var(--color-border)] bg-white px-4 text-left"
        type="button"
        @click="$emit('manual')"
      >
        <span class="grid size-12 shrink-0 place-items-center rounded-full bg-[#fff7df] text-[#f1ad36]">
          <Pencil :size="24" :stroke-width="2.2" aria-hidden="true" />
        </span>
        <span class="ml-4 min-w-0 flex-1">
          <strong class="block text-base font-bold text-[var(--color-text-primary)]">
            직접 계좌 정보 입력
          </strong>
          <span class="mt-1 block text-xs leading-5 text-[var(--color-text-secondary)]">
            다른 은행 계좌도<br />직접 입력하여 추가할 수 있어요
          </span>
        </span>
        <ChevronRight
          class="ml-3 shrink-0 text-[var(--color-unselected-text)] transition-transform group-active:translate-x-0.5"
          :size="20"
          :stroke-width="2"
          aria-hidden="true"
        />
      </button>
    </div>
  </section>
</template>

<style scoped>
.connection-option {
  box-shadow: 0 5px 18px rgb(43 83 105 / 4%);
  transition: border-color 150ms ease, box-shadow 150ms ease, transform 150ms ease;
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
