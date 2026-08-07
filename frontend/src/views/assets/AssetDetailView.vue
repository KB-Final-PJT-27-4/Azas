<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { Pencil } from 'lucide-vue-next'
import { useRoute, useRouter } from 'vue-router'

import { getAssetTransaction } from '@/data/assetDummyData'

const route = useRoute()
const router = useRouter()
const transaction = computed(() => getAssetTransaction(String(route.params.assetId ?? '1')))

const memo = ref(transaction.value.memo)
const memoDraft = ref(memo.value)
const isEditingMemo = ref(false)
const memoInput = ref<HTMLTextAreaElement | null>(null)

const startMemoEdit = async () => {
  memoDraft.value = memo.value
  isEditingMemo.value = true
  await nextTick()
  memoInput.value?.focus()
  memoInput.value?.select()
}

const saveMemo = () => {
  memo.value = memoDraft.value.trim()
  isEditingMemo.value = false
}

const cancelMemoEdit = () => {
  memoDraft.value = memo.value
  isEditingMemo.value = false
}

const goToAssets = () => router.push({ name: 'Assets' })
const formatWon = (amount: number) => `${amount.toLocaleString('ko-KR')} 원`
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-white">
    <article class="px-5 pt-7 pb-10 text-[var(--color-text-primary)]">
      <h1 class="m-0 text-[24px] leading-tight font-bold tracking-[-0.02em]">
        {{ transaction.accountLabel }}
      </h1>

      <form
        v-if="isEditingMemo"
        class="mt-3"
        aria-label="메모 수정"
        @submit.prevent="saveMemo"
      >
        <div class="relative">
          <textarea
            ref="memoInput"
            v-model="memoDraft"
            class="block h-24 w-full resize-none rounded-lg border border-[var(--color-brand-primary)] px-3 pt-2.5 pb-7 text-[15px] leading-6 text-[var(--color-text-secondary)] outline-none focus:ring-2 focus:ring-[var(--color-selected-background)]"
            aria-label="메모"
            maxlength="50"
            @keydown.esc="cancelMemoEdit"
          ></textarea>
          <span
            class="pointer-events-none absolute right-3 bottom-2 text-[12px] tabular-nums text-[var(--color-text-secondary)]"
          >
            {{ memoDraft.length }}/50
          </span>
        </div>
        <div class="mt-1.5 flex justify-end">
          <button
            class="h-9 shrink-0 rounded-lg bg-[var(--color-brand-primary)] px-4 text-[14px] font-bold text-white active:bg-[var(--color-brand-primary-pressed)]"
            type="submit"
            aria-label="메모 저장"
          >
            완료
          </button>
        </div>
      </form>

      <div v-else-if="memo" class="mt-3 flex min-w-0 items-start gap-1.5">
        <p class="m-0 min-w-0 text-[15px] leading-7 text-[var(--color-text-secondary)]">
          {{ memo }}
        </p>
        <button
          class="grid size-8 shrink-0 place-items-center rounded-full text-[#8b97a7] active:bg-[var(--color-unselected-background)]"
          type="button"
          aria-label="메모 수정"
          @click="startMemoEdit"
        >
          <Pencil :size="19" :stroke-width="2.8" />
        </button>
      </div>

      <button
        v-else
        class="mt-3 inline-flex h-9 items-center gap-1.5 rounded-lg border border-[var(--color-border)] bg-white px-3 text-[14px] font-semibold text-[var(--color-text-secondary)] active:bg-[var(--color-unselected-background)]"
        type="button"
        @click="startMemoEdit"
      >
        메모 추가
        <Pencil :size="16" :stroke-width="2.5" />
      </button>

      <div class="mt-5 border-t border-[var(--color-border)] pt-6">
        <dl class="m-0 grid grid-cols-[minmax(92px,1fr)_minmax(0,2fr)] gap-x-5 gap-y-6">
          <dt class="text-[16px] font-semibold text-[var(--color-text-secondary)]">거래 금액</dt>
          <dd class="m-0 text-right text-[20px] font-semibold text-[#22a8e8]">
            +{{ formatWon(transaction.amount) }}
          </dd>

          <dt class="text-[16px] font-semibold text-[var(--color-text-secondary)]">입금처</dt>
          <dd class="m-0 text-right text-[16px] leading-7">
            <strong class="block font-semibold">{{ transaction.depositName }}</strong>
            <span class="block text-[var(--color-text-secondary)]">
              {{ transaction.depositAccountNumber }}
            </span>
          </dd>

          <dt class="text-[16px] font-semibold text-[var(--color-text-secondary)]">출금처</dt>
          <dd class="m-0 text-right text-[16px] leading-7">
            <strong class="block font-semibold">{{ transaction.withdrawalName }}</strong>
            <span class="block text-[var(--color-text-secondary)]">
              {{ transaction.withdrawalAccountNumber }}
            </span>
          </dd>

          <dt class="text-[16px] font-semibold text-[var(--color-text-secondary)]">거래시각</dt>
          <dd class="m-0 text-right text-[16px] font-medium">{{ transaction.transactedAt }}</dd>

          <dt class="text-[16px] font-semibold text-[var(--color-text-secondary)]">거래 후 잔액</dt>
          <dd class="m-0 text-right text-[16px] font-medium">
            {{ formatWon(transaction.balanceAfterTransaction) }}
          </dd>
        </dl>
      </div>

      <button
        class="mt-16 h-14 w-full rounded-[14px] bg-[var(--color-brand-primary)] text-[17px] font-extrabold text-white transition-colors active:bg-[var(--color-brand-primary-pressed)]"
        type="button"
        @click="goToAssets"
      >
        확인
      </button>
    </article>
  </main>
</template>
