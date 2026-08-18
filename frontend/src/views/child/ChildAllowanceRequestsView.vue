<script setup lang="ts">
import { computed, ref } from 'vue'

import ChildBottomNavigation from '@/components/child/ChildBottomNavigation.vue'
import { childAllowanceRequests, type AllowanceRequestStatus } from '@/mocks/childFinanceFlow'

const selectedStatus = ref<'all' | AllowanceRequestStatus>('all')

const statusTabs = [
  { label: '전체', value: 'all' },
  { label: '승인 대기', value: 'pending' },
  { label: '승인됨', value: 'approved' },
  { label: '거절됨', value: 'rejected' },
] as const

const statusMeta = {
  pending: { label: '승인 대기', className: 'bg-[#fff8df] text-[#d59b00]' },
  approved: { label: '승인됨', className: 'bg-[#e8f8ef] text-[#159c60]' },
  rejected: { label: '거절됨', className: 'bg-[#fff0f0] text-[#ee5959]' },
}

const filteredRequests = computed(() =>
  selectedStatus.value === 'all'
    ? childAllowanceRequests
    : childAllowanceRequests.filter((request) => request.status === selectedStatus.value),
)

const formatCurrency = (amount: number) => `${amount.toLocaleString('ko-KR')}원`
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-5 pb-24">
    <div class="grid grid-cols-4 gap-2 rounded-[12px] bg-[#f4f8fb] p-1">
      <button
        v-for="tab in statusTabs"
        :key="tab.value"
        class="h-10 rounded-[10px] border-0 text-[length:var(--font-size-xs)] font-bold"
        :class="
          selectedStatus === tab.value
            ? 'bg-white text-[var(--color-brand-primary)] shadow-[0_6px_14px_rgb(110_122_138_/_12%)]'
            : 'bg-transparent text-[var(--color-text-secondary)]'
        "
        type="button"
        @click="selectedStatus = tab.value"
      >
        {{ tab.label }}
      </button>
    </div>

    <section class="mt-5 grid gap-4">
      <article
        v-for="request in filteredRequests"
        :key="request.id"
        class="rounded-[16px] border border-[var(--color-border)] bg-white px-5 py-5 shadow-[0_10px_24px_rgb(110_122_138_/_7%)]"
      >
        <div class="mb-5 flex items-start justify-between gap-3">
          <strong class="text-[length:var(--font-size-lg)] font-bold">
            {{ formatCurrency(request.amount) }} 요청
          </strong>
          <span
            class="rounded-full px-3 py-1 text-[length:var(--font-size-xs)] font-bold"
            :class="statusMeta[request.status].className"
          >
            {{ statusMeta[request.status].label }}
          </span>
        </div>
        <p class="m-0 text-[length:var(--font-size-sm)] font-bold text-[var(--color-text-primary)]">
          {{ request.purpose }}
        </p>
        <p class="mt-2 mb-0 text-[length:var(--font-size-xs)] text-[var(--color-text-secondary)]">
          {{ request.requestedAt }}
        </p>
      </article>
    </section>

    <p class="mt-8 text-center text-[length:var(--font-size-sm)] text-[var(--color-text-secondary)]">
      요청은 제한 없이 할 수 있어요!
    </p>

    <ChildBottomNavigation />
  </main>
</template>
