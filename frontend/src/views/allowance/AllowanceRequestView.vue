<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useToast } from '@/composables/useToast'
import { getAllowanceRequest } from '@/mocks/allowanceRequests'

const route = useRoute()
const router = useRouter()
const { showToast } = useToast()

const request = computed(() => getAllowanceRequest(String(route.params.requestId)))
const formatWon = (amount: number) => `${amount.toLocaleString('ko-KR')}원`

const rejectRequest = () => {
  if (!request.value) return
  request.value.status = 'rejected'
  showToast('용돈 요청을 거절했습니다.', 'info')
  window.setTimeout(() => router.push({ name: 'Alarm' }), 500)
}

const approveRequest = () => {
  if (!request.value) return
  request.value.status = 'approved'
  router.push({
    name: 'Assets',
    query: {
      allowanceRequest: request.value.id,
      amount: String(request.value.amount),
      memo: `${request.value.childName} 용돈`,
      targetName: request.value.targetAccountName,
      targetNumber: request.value.targetAccountNumber,
    },
  })
}
</script>

<template>
  <main
    class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col bg-[#f7fbfd] px-[18px] pt-[18px] pb-[calc(18px+env(safe-area-inset-bottom))] text-[var(--color-text-primary)]"
  >
    <template v-if="request">
      <section
        class="allowance-receipt relative mx-1 mt-2 bg-white px-5 pt-7 pb-8 shadow-[0_14px_38px_rgba(72,115,137,0.13)]"
      >
        <header class="text-center">
          <span
            class="mx-auto grid size-11 place-items-center rounded-full bg-[var(--color-selected-background)] text-[17px] font-black text-[var(--color-selected-text)]"
          >
            ₩
          </span>
          <p
            class="mt-3 mb-0 text-[10px] font-bold tracking-[0.18em] text-[var(--color-selected-text)]"
          >
            ALLOWANCE REQUEST
          </p>
          <h1 class="mt-1.5 mb-0 text-[18px] font-extrabold">
            {{ request.childName }}의 용돈 요청서
          </h1>
        </header>

        <div class="receipt-divider my-5" aria-hidden="true">
          <span></span>
        </div>

        <div class="text-center">
          <p class="m-0 text-[11px] font-semibold text-[var(--color-text-secondary)]">요청 금액</p>
          <strong class="mt-2 block text-[32px] leading-none font-black tracking-[-0.045em]">
            {{ formatWon(request.amount) }}
          </strong>
        </div>

        <dl class="mt-6 mb-0 grid gap-3 text-[12px]">
          <div class="flex items-center justify-between gap-4">
            <dt class="text-[var(--color-text-secondary)]">요청한 아이</dt>
            <dd class="m-0 font-bold">{{ request.childName }}</dd>
          </div>
          <div class="flex items-start justify-between gap-4">
            <dt class="shrink-0 text-[var(--color-text-secondary)]">받는 계좌</dt>
            <dd class="m-0 text-right font-semibold">
              {{ request.targetAccountName }}
              <span class="mt-0.5 block text-[10px] font-normal text-[var(--color-text-secondary)]">
                {{ request.targetAccountNumber }}
              </span>
            </dd>
          </div>
          <div class="flex items-start justify-between gap-4">
            <dt class="shrink-0 text-[var(--color-text-secondary)]">요청 시간</dt>
            <dd class="m-0 text-right font-semibold">{{ request.requestedAt }}</dd>
          </div>
        </dl>

        <div class="receipt-divider my-5" aria-hidden="true">
          <span></span>
        </div>

        <div>
          <p class="m-0 text-[11px] font-bold text-[var(--color-text-secondary)]">요청 사유</p>
          <p
            class="mt-2 mb-0 whitespace-pre-line rounded-[14px] bg-[#f4f9fc] px-4 py-[14px] text-[13px] leading-[1.65]"
          >
            {{ request.reason }}
          </p>
        </div>

        <div class="mt-8 text-center">
          <span
            class="inline-flex rounded-lg border-[3px] border-[#8fd5f4] px-5 py-2 text-[13px] font-extrabold tracking-[0.08em] text-[var(--color-selected-text)]"
          >
            확인 대기 중
          </span>
        </div>
      </section>

      <div class="mt-auto pt-8">
        <div class="grid grid-cols-2 gap-3">
          <button
            class="h-[52px] rounded-[14px] border border-[var(--color-border)] bg-white text-[15px] font-bold text-[var(--color-unselected-text)] transition-colors hover:bg-[#f7f9fa] active:bg-[#edf1f3]"
            type="button"
            @click="rejectRequest"
          >
            거절하기
          </button>
          <button
            class="h-[52px] rounded-[14px] bg-[var(--color-brand-primary)] text-[15px] font-bold text-white shadow-[0_7px_16px_rgba(43,171,232,0.2)] transition-colors hover:bg-[#1da4e3] active:bg-[var(--color-brand-primary-pressed)]"
            type="button"
            @click="approveRequest"
          >
            승인하기
          </button>
        </div>
      </div>
    </template>

    <section v-else class="grid min-h-[60vh] place-items-center text-center">
      <div>
        <h1 class="m-0 text-lg font-extrabold">요청을 찾을 수 없어요</h1>
        <button
          class="mt-4 text-sm font-bold text-[var(--color-selected-text)]"
          type="button"
          @click="router.push({ name: 'Alarm' })"
        >
          알림으로 돌아가기
        </button>
      </div>
    </section>
  </main>
</template>

<style scoped>
.allowance-receipt::before,
.allowance-receipt::after {
  position: absolute;
  left: 0;
  width: 100%;
  height: 10px;
  background: radial-gradient(circle at 7px 5px, #f7fbfd 0 5px, transparent 5.4px) 0 0 / 14px 10px
    repeat-x;
  content: '';
}

.allowance-receipt::before {
  top: -1px;
  transform: rotate(180deg);
}

.allowance-receipt::after {
  bottom: -1px;
}

.receipt-divider {
  display: flex;
  align-items: center;
  gap: 8px;
}

.receipt-divider::before,
.receipt-divider::after {
  color: #bdc8cf;
  font-size: 9px;
  content: '✂';
}

.receipt-divider::after {
  visibility: hidden;
}

.receipt-divider span {
  flex: 1;
  border-top: 1px dashed #cfd9df;
}
</style>
