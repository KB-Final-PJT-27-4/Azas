<script setup lang="ts">
defineProps<{
  selectedBank: string
  accountNumber: string
  accountAlias: string
}>()

const emit = defineEmits<{
  edit: []
  register: []
}>()
</script>

<template>
  <section
    class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col bg-white"
  >
    <div class="flex flex-1 flex-col px-6 pt-5 pb-6">
      <div class="mb-6 grid grid-cols-2 gap-2" aria-label="계좌 등록 진행 단계">
        <div class="h-1 rounded-full bg-[var(--color-border)]"></div>
        <div class="h-1 rounded-full bg-[var(--color-brand-primary)]"></div>
      </div>

      <h1
        class="text-[28px] leading-tight font-bold tracking-[-0.03em] text-[var(--color-text-primary)]"
      >
        입력한 정보를 확인해주세요
      </h1>
      <p class="mt-2 text-sm text-[var(--color-text-secondary)]">
        등록한 계좌 정보가 맞는지 확인해주세요.
      </p>

      <dl
        class="mt-8 space-y-5 rounded-xl border border-[var(--color-border)] px-5 py-6 text-sm"
      >
        <div class="grid grid-cols-[auto_1fr] items-center gap-6">
          <dt class="font-bold text-[var(--color-text-secondary)]">은행</dt>
          <dd class="min-w-0 text-right font-semibold text-[var(--color-text-secondary)]">
            {{ selectedBank }}
          </dd>
        </div>
        <div class="grid grid-cols-[auto_1fr] items-center gap-6">
          <dt class="font-bold text-[var(--color-text-secondary)]">계좌번호</dt>
          <dd class="min-w-0 break-all text-right font-semibold text-[var(--color-text-secondary)]">
            {{ accountNumber }}
          </dd>
        </div>
        <div class="grid grid-cols-[auto_1fr] items-center gap-6">
          <dt class="font-bold text-[var(--color-text-secondary)]">계좌별칭</dt>
          <dd class="min-w-0 break-all text-right font-semibold text-[var(--color-text-secondary)]">
            {{ accountAlias || '미입력' }}
          </dd>
        </div>
      </dl>

      <div class="mt-auto grid grid-cols-2 gap-4 pt-10">
        <button
          class="min-h-13 rounded-xl border border-[var(--color-border)] bg-white text-base font-bold text-[var(--color-selected-text)]"
          type="button"
          @click="emit('edit')"
        >
          수정하기
        </button>
        <button
          class="min-h-13 rounded-xl bg-[var(--color-brand-primary)] text-base font-bold text-white active:bg-[var(--color-brand-primary-pressed)]"
          type="button"
          @click="emit('register')"
        >
          등록하기
        </button>
      </div>
    </div>
  </section>
</template>
