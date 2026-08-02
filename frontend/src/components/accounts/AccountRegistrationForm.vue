<script setup lang="ts">
import { ChevronDown } from 'lucide-vue-next'

defineProps<{
  selectedBank: string
}>()

const accountNumber = defineModel<string>('accountNumber', { required: true })
const accountAlias = defineModel<string>('accountAlias', { required: true })

const emit = defineEmits<{
  'open-bank-selector': []
  skip: []
  next: []
}>()

const updateAccountNumber = (event: Event) => {
  accountNumber.value = (event.target as HTMLInputElement).value.replace(/\D/g, '')
}
</script>

<template>
  <section class="flex min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] flex-col bg-white">

    <div class="flex flex-1 flex-col px-6 pt-5 pb-6">
      <div class="mb-6 grid grid-cols-2 gap-2" aria-label="계좌 등록 진행 단계">
        <div class="h-1 rounded-full bg-[var(--color-brand-primary)]"></div>
        <div class="h-1 rounded-full bg-[var(--color-border)]"></div>
      </div>

      <h1 class="text-[28px] leading-tight font-bold tracking-[-0.03em] text-[var(--color-text-primary)]">
        계좌를 등록하고 저축을 시작해요
      </h1>
      <p class="mt-2 text-sm text-[var(--color-text-secondary)]">
        아이의 목표 저축을 함께 관리할 계좌를 등록해 주세요.
      </p>

      <form class="mt-7 flex flex-1 flex-col" @submit.prevent="emit('next')">
        <div class="space-y-5">
          <label class="block">
            <span class="mb-2 block text-sm font-bold text-[var(--color-text-primary)]">은행 선택</span>
            <button
              class="flex min-h-12 w-full items-center justify-between rounded-xl border border-[var(--color-border)] bg-white px-4 text-left text-base text-[var(--color-text-primary)] focus:border-[var(--color-brand-primary)] focus:outline-none"
              type="button"
              @click="emit('open-bank-selector')"
            >
              <span :class="selectedBank ? '' : 'text-[#9aa2ad]'">
                {{ selectedBank || '은행을 선택해주세요' }}
              </span>
              <ChevronDown :size="20" class="text-[var(--color-text-secondary)]" />
            </button>
          </label>

          <label class="block">
            <span class="mb-2 block text-sm font-bold text-[var(--color-text-primary)]">계좌번호 입력</span>
            <div class="flex min-h-12 items-center rounded-xl border border-[var(--color-border)] bg-white pl-4 pr-2 focus-within:border-[var(--color-brand-primary)]">
              <input
                class="min-w-0 flex-1 border-0 bg-transparent text-base text-[var(--color-text-primary)] outline-none placeholder:text-[#9aa2ad]"
                :value="accountNumber"
                inputmode="numeric"
                autocomplete="off"
                placeholder="-없이 숫자만 입력해주세요"
                aria-label="계좌번호"
                @input="updateAccountNumber"
              />
              <button
                class="ml-3 shrink-0 rounded-md bg-[var(--color-selected-background)] px-3 py-2 text-xs text-[var(--color-selected-text)] disabled:cursor-not-allowed disabled:bg-[var(--color-unselected-background)] disabled:text-[var(--color-unselected-text)]"
                type="button"
                :disabled="!accountNumber"
              >
                계좌 확인
              </button>
            </div>
          </label>

          <label class="block">
            <span class="mb-2 block text-sm font-bold text-[var(--color-text-primary)]">계좌별칭(선택)</span>
            <div class="flex min-h-12 items-center rounded-xl border border-[var(--color-border)] bg-white px-4 focus-within:border-[var(--color-brand-primary)]">
              <input
                v-model="accountAlias"
                class="min-w-0 flex-1 border-0 bg-transparent text-base text-[var(--color-text-primary)] outline-none placeholder:text-[#9aa2ad]"
                maxlength="20"
                placeholder="예) 아이 저축계좌"
                aria-label="계좌 별칭"
              />
              <span class="ml-3 shrink-0 text-xs text-[#9aa2ad]">{{ accountAlias.length }} / 20</span>
            </div>
          </label>
        </div>

        <div class="mt-auto grid grid-cols-2 gap-4 pt-10">
          <button
            class="min-h-13 rounded-xl border border-[var(--color-border)] bg-white text-base font-bold text-[var(--color-selected-text)]"
            type="button"
            @click="emit('skip')"
          >
            건너뛰기
          </button>
          <button
            class="min-h-13 rounded-xl bg-[var(--color-brand-primary)] text-base font-bold text-white active:bg-[var(--color-brand-primary-pressed)]"
            type="submit"
          >
            다음
          </button>
        </div>
      </form>
    </div>
  </section>
</template>
