<script setup lang="ts">
import { X } from 'lucide-vue-next'
import { useRouter } from 'vue-router'

import failPigImage from '@/assets/images/assets/fail-pig.png'
import successPigImage from '@/assets/images/assets/success-pig.png'
import AppBottomNavigation from '@/components/layout/AppBottomNavigation.vue'

defineProps<{
  open: boolean
  status: 'success' | 'failure'
}>()

const emit = defineEmits<{
  close: []
  retry: []
}>()

const router = useRouter()

const createTimeCapsule = () => router.push({ name: 'TimeCapsuleCreate' })
</script>

<template>
  <Teleport to="body">
    <Transition name="asset-result-sheet">
      <div
        v-if="open"
        class="asset-result-sheet-overlay fixed inset-0 z-[var(--z-index-overlay)] flex items-end justify-center bg-black/35"
        role="presentation"
        @click.self="emit('close')"
      >
      <section
        class="relative flex h-[min(655px,calc(100dvh-120px))] w-full max-w-[var(--app-max-width)] flex-col overflow-hidden rounded-t-[20px] bg-white px-6 pt-5 pb-[calc(var(--app-bottom-nav-height)+18px)] text-[var(--color-text-primary)]"
        role="dialog"
        aria-modal="true"
        :aria-labelledby="
          status === 'success' ? 'transfer-success-title' : 'transfer-failure-title'
        "
      >
        <button
          class="ml-auto grid size-8 shrink-0 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[var(--color-unselected-background)]"
          type="button"
          aria-label="결과 창 닫기"
          @click="emit('close')"
        >
          <X :size="19" :stroke-width="2.5" />
        </button>

        <template v-if="status === 'success'">
          <div class="flex min-h-0 flex-1 flex-col items-center text-center">
            <h2
              id="transfer-success-title"
              class="mt-5 mb-0 text-[19px] leading-[1.35] font-semibold"
            >
              이체에 성공했어요!<br />오늘의 추억을 타임캡슐에 저장해보세요!
            </h2>
            <img
              class="mt-5 h-[500px] w-[300px] object-contain"
              :src="successPigImage"
              alt="타임캡슐을 만드는 돼지 캐릭터"
            />
            <div class="mt-auto grid w-full gap-2.5 pt-4">
              <button
                class="h-11 rounded-[13px] bg-[var(--color-brand-primary)] text-[15px] font-semibold text-white active:bg-[var(--color-brand-primary-pressed)]"
                type="button"
                @click="createTimeCapsule"
              >
                타임캡슐 만들기
              </button>
              <button
                class="h-11 rounded-[13px] bg-[#f0f0f0] text-[14px] font-semibold text-[#737373] active:bg-[#e5e5e5]"
                type="button"
                @click="emit('close')"
              >
                나중에 하기
              </button>
            </div>
          </div>
        </template>

        <template v-else>
          <div class="flex min-h-0 flex-1 flex-col items-center text-center">
            <h2
              id="transfer-failure-title"
              class="mt-5 mb-0 text-[19px] leading-[1.35] font-semibold"
            >
              이체에 실패했어요
            </h2>
            <p class="mt-2 mb-0 text-[13px] text-[var(--color-text-secondary)]">
              출금 계좌의 잔액과 이체 금액을 다시 확인해 주세요.
            </p>
            <img
              class="mt-4 h-[300px] w-full object-contain"
              :src="failPigImage"
              alt="이체 실패를 알리는 돼지 캐릭터"
            />
            <div class="mt-auto grid w-full gap-2.5 pt-4">
              <button
                class="h-11 rounded-[13px] bg-[var(--color-brand-primary)] text-[15px] font-semibold text-white active:bg-[var(--color-brand-primary-pressed)]"
                type="button"
                @click="emit('retry')"
              >
                다시 시도하기
              </button>
              <button
                class="h-11 rounded-[13px] bg-[#f0f0f0] text-[14px] font-semibold text-[#737373] active:bg-[#e5e5e5]"
                type="button"
                @click="emit('close')"
              >
                닫기
              </button>
            </div>
          </div>
        </template>

        <AppBottomNavigation />
      </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.asset-result-sheet-overlay {
  transition: background-color 260ms ease;
}

.asset-result-sheet-overlay > section {
  transition: transform 300ms cubic-bezier(0.22, 1, 0.36, 1);
}

.asset-result-sheet-enter-from,
.asset-result-sheet-leave-to {
  background-color: transparent;
}

.asset-result-sheet-enter-from > section,
.asset-result-sheet-leave-to > section {
  transform: translateY(100%);
}

.asset-result-sheet-leave-active > section {
  transition-timing-function: cubic-bezier(0.4, 0, 1, 1);
  transition-duration: 240ms;
}
</style>
