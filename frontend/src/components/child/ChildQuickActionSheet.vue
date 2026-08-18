<script setup lang="ts">
import { computed, ref, watch, type CSSProperties } from 'vue'
import { Send, Wallet, X } from 'lucide-vue-next'

import AssetAccountSelect from '@/components/assets/AssetAccountSelect.vue'
import allowanceCompletePigUrl from '@/assets/images/child/child-allowance-complete-pig-v2.png'
import transferCompletePigUrl from '@/assets/images/child/child-transfer-complete-pig-v2.png'
import { childAccountSummary, recordChildTransfer, transferDefaults } from '@/mocks/childHome'

type SheetMode = 'menu' | 'transfer' | 'allowance' | 'transferDone' | 'allowanceDone'

const props = defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  close: []
}>()

const mode = ref<SheetMode>('menu')
const firstContact = transferDefaults.contacts[0]!
const sourceAccountId = ref('child-main')
const selectedContactId = ref(firstContact.id)
const transferAmount = ref('0')
const transferMessage = ref('')
const allowanceAmount = ref('0')
const reason = ref('')
const dragStartY = ref(0)
const dragOffset = ref(0)
const isDragging = ref(false)
const maxMoneyDigits = 8
const maxReasonLength = 200
const maxTransferMemoLength = 50

const sourceAccounts = computed(() => [
  {
    id: 'child-main',
    name: childAccountSummary.accountName,
    number: '952-17362605-47',
    balance: transferDefaults.balance,
  },
])
const targetAccounts = computed(() =>
  transferDefaults.contacts.map((contact) => ({
    id: contact.id,
    name: `${contact.name} ${contact.bankName}`,
    number: contact.accountNumber,
  })),
)
const selectedContact = computed(
  () => transferDefaults.contacts.find((contact) => contact.id === selectedContactId.value) ?? firstContact,
)
const transferAmountValue = computed(() => Number(transferAmount.value.replace(/\D/g, '')) || 0)
const canSubmitTransfer = computed(
  () =>
    Boolean(selectedContactId.value) &&
    transferAmountValue.value > 0 &&
    transferAmountValue.value <= transferDefaults.balance,
)

const allowanceAmountValue = computed(() => Number(allowanceAmount.value.replace(/\D/g, '')) || 0)
const canSubmitAllowance = computed(
  () => allowanceAmountValue.value > 0 && reason.value.trim().length > 0,
)
const sheetTitle = computed(() => {
  if (mode.value === 'transfer') return '돈 보내기'
  if (mode.value === 'allowance') return '용돈 요청하기'
  if (mode.value === 'transferDone') return '이체 완료'
  if (mode.value === 'allowanceDone') return '요청 완료'
  return '무엇을 할까요?'
})
const canGoBack = computed(() => mode.value === 'transfer' || mode.value === 'allowance')
const isDoneMode = computed(() => mode.value === 'transferDone' || mode.value === 'allowanceDone')
const panelDragStyle = computed<CSSProperties | undefined>(() => {
  if (dragOffset.value <= 0) return undefined

  return {
    transform: `translateY(${dragOffset.value}px)`,
    transition: isDragging.value ? 'none' : undefined,
  }
})

const formatQuickAmount = (amount: number) => `+${amount / 10_000}만`

const closeSheet = () => {
  dragOffset.value = 0
  isDragging.value = false
  emit('close')
}

const showMenu = () => {
  mode.value = 'menu'
}

const showTransfer = () => {
  mode.value = 'transfer'
}

const showAllowance = () => {
  mode.value = 'allowance'
}

const resetSheet = () => {
  mode.value = 'menu'
  sourceAccountId.value = 'child-main'
  selectedContactId.value = firstContact.id
  transferAmount.value = '0'
  transferMessage.value = ''
  allowanceAmount.value = '0'
  reason.value = ''
  dragOffset.value = 0
  isDragging.value = false
}

const updateTransferAmount = (event: Event) => {
  const input = event.target as HTMLInputElement
  const digits = input.value.replace(/\D/g, '').slice(0, maxMoneyDigits)
  transferAmount.value = digits ? Number(digits).toLocaleString('ko-KR') : '0'
  if (input.value !== transferAmount.value) input.value = transferAmount.value
}

const startTransferAmountEdit = (event: FocusEvent) => {
  transferAmount.value = transferAmountValue.value.toLocaleString('ko-KR')
  const input = event.target as HTMLInputElement
  requestAnimationFrame(() => {
    if (input.value === '0') input.select()
  })
}

const finishTransferAmountEdit = () => {
  transferAmount.value = transferAmountValue.value.toLocaleString('ko-KR')
}

const addTransferAmount = (amount: number) => {
  transferAmount.value = Math.min(
    Number('9'.repeat(maxMoneyDigits)),
    transferAmountValue.value + amount,
  ).toLocaleString('ko-KR')
}

const clearTransferAmount = () => {
  transferAmount.value = '0'
}

const submitTransfer = () => {
  if (!canSubmitTransfer.value) return

  recordChildTransfer({
    amount: transferAmountValue.value,
    receiverName: selectedContact.value.bankName,
  })
  mode.value = 'transferDone'
}

const updateAllowanceAmount = (event: Event) => {
  const input = event.target as HTMLInputElement
  const digits = input.value.replace(/\D/g, '').slice(0, maxMoneyDigits)
  allowanceAmount.value = digits ? Number(digits).toLocaleString('ko-KR') : '0'
  if (input.value !== allowanceAmount.value) input.value = allowanceAmount.value
}

const startAllowanceAmountEdit = (event: FocusEvent) => {
  allowanceAmount.value = allowanceAmountValue.value.toLocaleString('ko-KR')
  const input = event.target as HTMLInputElement
  requestAnimationFrame(() => {
    if (input.value === '0') input.select()
  })
}

const finishAllowanceAmountEdit = () => {
  allowanceAmount.value = allowanceAmountValue.value.toLocaleString('ko-KR')
}

const clearAllowanceAmount = () => {
  allowanceAmount.value = '0'
}

const updateReason = (event: Event) => {
  const textarea = event.target as HTMLTextAreaElement
  reason.value = textarea.value.slice(0, maxReasonLength)
  if (textarea.value !== reason.value) textarea.value = reason.value
}

const startHandleDrag = (event: PointerEvent) => {
  isDragging.value = true
  dragStartY.value = event.clientY - dragOffset.value
  ;(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId)
}

const moveHandleDrag = (event: PointerEvent) => {
  if (!isDragging.value) return
  dragOffset.value = Math.max(event.clientY - dragStartY.value, 0)
}

const endHandleDrag = (event: PointerEvent) => {
  if (!isDragging.value) return

  const handle = event.currentTarget as HTMLElement
  if (handle.hasPointerCapture(event.pointerId)) {
    handle.releasePointerCapture(event.pointerId)
  }
  isDragging.value = false

  if (dragOffset.value > 80) {
    closeSheet()
    return
  }

  dragOffset.value = 0
}

const submitAllowance = () => {
  if (!canSubmitAllowance.value) return

  mode.value = 'allowanceDone'
}

watch(
  () => props.open,
  (open) => {
    if (!open) {
      resetSheet()
    }
  },
)
</script>

<template>
  <Teleport to="body">
    <Transition name="quick-sheet">
      <div
        v-if="open"
        class="fixed inset-0 z-[var(--z-index-overlay)] overflow-hidden bg-black/40"
        @click.self="closeSheet"
      >
        <section
          class="quick-sheet-panel fixed right-0 bottom-0 left-0 mx-auto max-h-[min(680px,calc(100dvh-24px))] w-full max-w-[var(--app-max-width)] overflow-hidden rounded-t-[20px] bg-white text-[var(--color-text-primary)] shadow-[0_-14px_36px_rgb(51_51_51_/_18%)]"
          :style="panelDragStyle"
          role="dialog"
          aria-modal="true"
          :aria-label="sheetTitle"
        >
          <button
            class="mx-auto mt-3 mb-4 grid h-5 w-20 place-items-start border-0 bg-transparent p-0"
            type="button"
            aria-label="바텀시트 아래로 내려 닫기"
            @pointerdown="startHandleDrag"
            @pointermove="moveHandleDrag"
            @pointerup="endHandleDrag"
            @pointercancel="endHandleDrag"
          >
            <span class="mx-auto block h-1 w-11 rounded-full bg-[var(--color-border)]" />
          </button>

          <div
            class="max-h-[calc(100dvh-88px)] overflow-y-auto overflow-x-hidden px-5 pb-[calc(20px+env(safe-area-inset-bottom))]"
          >
            <header
              v-if="!isDoneMode"
              class="mb-5 grid grid-cols-[44px_1fr_44px] items-center"
            >
              <button
                v-if="canGoBack"
                class="justify-self-start border-0 bg-transparent p-0 text-[14px] font-bold text-[var(--color-text-secondary)]"
                type="button"
                @click="showMenu"
              >
                이전
              </button>
              <span v-else />
              <h2 class="m-0 text-center text-[20px] font-bold text-[var(--color-text-primary)]">
                {{ sheetTitle }}
              </h2>
              <span />
            </header>

            <Transition name="quick-action-slide" mode="out-in">
              <div v-if="mode === 'menu'" key="menu" class="grid gap-3">
                <button
                  class="grid min-h-[76px] grid-cols-[44px_1fr] items-center gap-3 rounded-[16px] border border-[var(--color-border)] bg-white px-4 text-left transition active:scale-[0.99] active:bg-[#f7fbfd]"
                  type="button"
                  @click="showTransfer"
                >
                  <span
                    class="grid size-11 place-items-center rounded-[13px] bg-[#eef8ff] text-[var(--color-brand-primary)]"
                  >
                    <Send :size="22" :stroke-width="2.6" aria-hidden="true" />
                  </span>
                  <span>
                    <strong class="block text-[17px] font-bold">돈 보내기</strong>
                    <small class="mt-1 block text-[14px] text-[var(--color-text-secondary)]">
                      친구나 가족에게 돈을 보낼 수 있어요
                    </small>
                  </span>
                </button>

                <button
                  class="grid min-h-[76px] grid-cols-[44px_1fr] items-center gap-3 rounded-[16px] border border-[var(--color-border)] bg-white px-4 text-left transition active:scale-[0.99] active:bg-[#f7fbfd]"
                  type="button"
                  @click="showAllowance"
                >
                  <span class="grid size-11 place-items-center rounded-[13px] bg-[#fff7d7] text-[#8a6b13]">
                    <Wallet :size="22" :stroke-width="2.6" aria-hidden="true" />
                  </span>
                  <span>
                    <strong class="block text-[17px] font-bold">용돈 요청하기</strong>
                    <small class="mt-1 block text-[14px] text-[var(--color-text-secondary)]">
                      필요한 용돈을 부모님께 요청해요
                    </small>
                  </span>
                </button>
              </div>

              <section v-else-if="mode === 'transfer'" key="transfer">
                <form class="mt-1" @submit.prevent="submitTransfer">
                  <label class="block text-[12px] font-semibold">
                    출금 계좌 <span class="text-[#f04444]">*</span>
                  </label>
                  <AssetAccountSelect
                    v-model="sourceAccountId"
                    class="mt-2"
                    :options="sourceAccounts"
                    label="출금 계좌 선택"
                    show-balance
                  />

                  <label class="mt-4 block text-[12px] font-semibold">
                    받는 계좌 <span class="text-[#f04444]">*</span>
                  </label>
                  <AssetAccountSelect
                    v-model="selectedContactId"
                    class="mt-2"
                    :options="targetAccounts"
                    label="받는 계좌 선택"
                  />

                  <label for="child-transfer-amount" class="mt-4 block text-[12px] font-semibold">
                    이체 금액 <span class="text-[#f04444]">*</span>
                  </label>
                  <div class="relative mt-2">
                    <input
                      id="child-transfer-amount"
                      :value="transferAmount"
                      class="h-10 w-full rounded-[12px] border border-[#dce8ee] pr-16 pl-3 text-[15px] outline-none transition focus:border-[var(--color-brand-primary)]"
                      inputmode="numeric"
                      type="text"
                      @focus="startTransferAmountEdit"
                      @input="updateTransferAmount"
                      @blur="finishTransferAmountEdit"
                    />
                    <span
                      class="pointer-events-none absolute top-1/2 -translate-y-1/2 text-[15px]"
                      :class="transferAmountValue > 0 ? 'right-10' : 'right-3'"
                    >
                      원
                    </span>
                    <button
                      v-if="transferAmountValue > 0"
                      class="absolute top-1/2 right-2 grid size-7 -translate-y-1/2 place-items-center rounded-full text-[#9aa6b2] active:bg-[#eef3f6]"
                      type="button"
                      aria-label="이체 금액 지우기"
                      @click="clearTransferAmount"
                    >
                      <X :size="16" :stroke-width="2.5" />
                    </button>
                  </div>

                  <div class="mt-2.5 grid grid-cols-4 gap-2">
                    <button
                      v-for="amount in [10000, 50000, 100000, 500000]"
                      :key="amount"
                      class="h-[22px] rounded-full bg-[#e6eef3] text-[10px] text-[var(--color-text-secondary)] active:bg-[#d7e4eb]"
                      type="button"
                      @click="addTransferAmount(amount)"
                    >
                      {{ formatQuickAmount(amount) }}
                    </button>
                  </div>

                  <label for="child-transfer-memo" class="mt-4 block text-[12px] font-semibold">
                    메모
                  </label>
                  <div class="relative mt-2">
                    <textarea
                      id="child-transfer-memo"
                      v-model="transferMessage"
                      class="block h-[114px] w-full resize-none rounded-[12px] border border-[#dce8ee] px-3 pt-2.5 pb-7 text-[13px] leading-[1.35] outline-none transition focus:border-[var(--color-brand-primary)]"
                      :maxlength="maxTransferMemoLength"
                      placeholder="메모를 입력해 주세요."
                    ></textarea>
                    <span
                      class="pointer-events-none absolute right-3 bottom-2 text-[10px] tabular-nums text-[var(--color-text-secondary)]"
                    >
                      {{ transferMessage.length }}/{{ maxTransferMemoLength }}
                    </span>
                  </div>

                  <button
                    class="mt-5 h-14 w-full rounded-[14px] text-[16px] font-bold text-white transition active:bg-[var(--color-brand-primary-pressed)] disabled:bg-[#cbd8df]"
                    :class="canSubmitTransfer ? 'bg-[var(--color-brand-primary)]' : 'bg-[#cbd8df]'"
                    type="submit"
                    :disabled="!canSubmitTransfer"
                  >
                    이체하기
                  </button>
                </form>
              </section>

              <section v-else-if="mode === 'allowance'" key="allowance" class="grid min-w-0 gap-4 overflow-x-hidden">
                <div>
                  <label for="child-allowance-amount" class="block text-[12px] font-semibold">
                    요청 금액 <span class="text-[#f04444]">*</span>
                  </label>
                  <div class="relative mt-2">
                    <input
                      id="child-allowance-amount"
                      :value="allowanceAmount"
                      class="h-10 w-full rounded-[12px] border border-[#dce8ee] pr-16 pl-3 text-[15px] outline-none transition focus:border-[var(--color-brand-primary)]"
                      inputmode="numeric"
                      type="text"
                      @focus="startAllowanceAmountEdit"
                      @input="updateAllowanceAmount"
                      @blur="finishAllowanceAmountEdit"
                    />
                    <span
                      class="pointer-events-none absolute top-1/2 -translate-y-1/2 text-[15px]"
                      :class="allowanceAmountValue > 0 ? 'right-10' : 'right-3'"
                    >
                      원
                    </span>
                    <button
                      v-if="allowanceAmountValue > 0"
                      class="absolute top-1/2 right-2 grid size-7 -translate-y-1/2 place-items-center rounded-full text-[#9aa6b2] active:bg-[#eef3f6]"
                      type="button"
                      aria-label="요청 금액 지우기"
                      @click="clearAllowanceAmount"
                    >
                      <X :size="16" :stroke-width="2.5" />
                    </button>
                  </div>
                </div>

                <label class="block text-[16px] font-bold">
                  부모님께 하고 싶은 말
                  <div class="relative mt-3">
                    <textarea
                      :value="reason"
                      class="block min-h-[132px] w-full resize-none rounded-[12px] border border-[var(--color-border)] px-4 py-4 pb-8 text-[14px] font-normal outline-none transition focus:border-[var(--color-brand-primary)]"
                      :maxlength="maxReasonLength"
                      placeholder="용돈이 왜 필요한지 적어보세요 :)"
                      @input="updateReason"
                      @compositionend="updateReason"
                    />
                    <span
                      class="pointer-events-none absolute right-4 bottom-3 text-[11px] font-normal tabular-nums text-[var(--color-text-secondary)]"
                    >
                      {{ reason.length }}/{{ maxReasonLength }}
                    </span>
                  </div>
                </label>

                <div
                  class="rounded-[12px] bg-[#f0fbff] px-4 py-4 text-[12px] leading-[1.65] text-[var(--color-text-secondary)]"
                >
                  <strong class="mb-2 block text-[var(--color-text-primary)]">
                    이렇게 쓰면 용돈 받을 확률이 올라가요
                  </strong>
                  누가? 언제? 어디서? 무엇을? 왜? 얼마만큼?<br />
                  예) 친구 생일 선물을 사려고 해요.
                </div>

                <button
                  class="h-14 w-full rounded-[14px] border-0 text-[16px] font-bold text-white transition active:scale-[0.99] disabled:bg-[#cbd8df]"
                  :class="canSubmitAllowance ? 'bg-[var(--color-brand-primary)]' : 'bg-[#cbd8df]'"
                  type="button"
                  :disabled="!canSubmitAllowance"
                  @click="submitAllowance"
                >
                  부모님께 요청하기
                </button>
              </section>

              <section
                v-else-if="mode === 'allowanceDone'"
                key="allowanceDone"
                class="quick-done-panel"
              >
                <div class="quick-done-panel__sky quick-done-panel__sky--allowance">
                  <h3 class="m-0 text-center text-[24px] leading-[1.35] font-extrabold text-[var(--color-text-primary)]">
                    용돈 요청이<br />
                    전송되었어요!
                  </h3>
                  <p class="mt-4 mb-0 text-center text-[15px] leading-[1.7] text-[var(--color-text-secondary)]">
                    부모님이 확인하시면<br />
                    알림으로 알려드릴게요
                  </p>
                  <img
                    class="quick-done-panel__image quick-done-panel__image--allowance"
                    :src="allowanceCompletePigUrl"
                    alt=""
                    aria-hidden="true"
                  />
                  <span
                    class="quick-done-panel__check quick-done-panel__check--allowance"
                    aria-hidden="true"
                  >
                    <svg width="29" height="29" viewBox="0 0 24 24" fill="none">
                      <path pathLength="1" d="M4 12.5L9.2 17.5L20 6.5" />
                    </svg>
                  </span>
                </div>
                <button
                  class="mt-5 h-14 w-full rounded-[14px] border-0 bg-[var(--color-brand-primary)] text-[16px] font-bold text-white transition active:bg-[var(--color-brand-primary-pressed)]"
                  type="button"
                  @click="closeSheet"
                >
                  확인
                </button>
              </section>

              <section v-else key="transferDone" class="quick-done-panel">
                <div class="quick-done-panel__sky quick-done-panel__sky--transfer">
                  <h3 class="m-0 text-center text-[24px] leading-[1.35] font-extrabold text-[var(--color-text-primary)]">
                    이체를<br />
                    완료했어요!
                  </h3>
                  <p class="mt-4 mb-0 text-center text-[15px] leading-[1.7] text-[var(--color-text-secondary)]">
                    보낸 내역은<br />
                    내 자산에서 확인할 수 있어요
                  </p>
                  <img
                    class="quick-done-panel__image quick-done-panel__image--transfer"
                    :src="transferCompletePigUrl"
                    alt=""
                    aria-hidden="true"
                  />
                  <span
                    class="quick-done-panel__check quick-done-panel__check--transfer"
                    aria-hidden="true"
                  >
                    <svg width="29" height="29" viewBox="0 0 24 24" fill="none">
                      <path pathLength="1" d="M4 12.5L9.2 17.5L20 6.5" />
                    </svg>
                  </span>
                </div>
                <button
                  class="mt-5 h-14 w-full rounded-[14px] border-0 bg-[var(--color-brand-primary)] text-[16px] font-bold text-white transition active:bg-[var(--color-brand-primary-pressed)]"
                  type="button"
                  @click="closeSheet"
                >
                  확인
                </button>
              </section>
            </Transition>
          </div>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.quick-sheet-enter-active,
.quick-sheet-leave-active {
  transition: background-color 180ms ease;
}

.quick-sheet-enter-active .quick-sheet-panel,
.quick-sheet-leave-active .quick-sheet-panel {
  transition:
    transform 240ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 180ms ease;
}

.quick-sheet-enter-from,
.quick-sheet-leave-to {
  background-color: transparent;
}

.quick-sheet-enter-from .quick-sheet-panel,
.quick-sheet-leave-to .quick-sheet-panel {
  opacity: 0;
  transform: translateY(100%);
}

.quick-action-slide-enter-active,
.quick-action-slide-leave-active {
  transition:
    opacity 180ms ease,
    transform 200ms cubic-bezier(0.22, 1, 0.36, 1);
}

.quick-action-slide-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.quick-action-slide-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

.quick-done-panel {
  display: grid;
  min-width: 0;
}

.quick-done-panel__sky {
  position: relative;
  min-height: 382px;
  overflow: hidden;
  padding: 26px 18px 20px;
  border-radius: 18px;
  background:
    radial-gradient(circle at 12% 20%, rgb(255 255 255 / 95%) 0 32px, transparent 33px),
    radial-gradient(circle at 21% 28%, rgb(255 255 255 / 82%) 0 24px, transparent 25px),
    radial-gradient(circle at 92% 29%, rgb(255 255 255 / 92%) 0 34px, transparent 35px),
    linear-gradient(180deg, #eaf7ff 0%, #f5fbff 100%);
}

.quick-done-panel__sky--allowance,
.quick-done-panel__sky--transfer {
  background: transparent;
}

.quick-done-panel__cloud {
  position: absolute;
  width: 92px;
  height: 34px;
  border-radius: 999px;
  background: rgb(255 255 255 / 76%);
}

.quick-done-panel__cloud::before,
.quick-done-panel__cloud::after {
  position: absolute;
  content: '';
  background: inherit;
  border-radius: inherit;
}

.quick-done-panel__cloud::before {
  top: -13px;
  left: 16px;
  width: 38px;
  height: 38px;
}

.quick-done-panel__cloud::after {
  top: -8px;
  right: 13px;
  width: 29px;
  height: 29px;
}

.quick-done-panel__cloud--left {
  top: 198px;
  left: 8px;
}

.quick-done-panel__cloud--right {
  right: -10px;
  bottom: 96px;
}

.quick-done-panel__image {
  position: absolute;
  left: 50%;
  object-fit: contain;
  transform: translateX(-50%);
  animation: quick-done-pop 260ms cubic-bezier(0.2, 1.2, 0.4, 1);
}

.quick-done-panel__image--allowance {
  bottom: 44px;
  width: 145px;
}

.quick-done-panel__check {
  position: absolute;
  z-index: 4;
  display: grid;
  width: 44px;
  height: 44px;
  color: white;
  background: linear-gradient(155deg, #61c8f5 2%, #2d8dec 82%);
  border-radius: 50%;
  box-shadow: 0 6px 14px rgb(45 141 236 / 22%);
  place-items: center;
}

.quick-done-panel__check--allowance {
  right: calc(50% - 100px);
  bottom: 146px;
}

.quick-done-panel__check--transfer {
  right: calc(50% - 100px);
  bottom: 154px;
}

.quick-done-panel__check path {
  fill: none;
  stroke: currentColor;
  stroke-width: 3.4;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.quick-done-panel__image--transfer {
  bottom: 44px;
  width: 132px;
}

@keyframes quick-done-pop {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(10px) scale(0.92);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0) scale(1);
  }
}

@media (prefers-reduced-motion: no-preference) {
  .quick-done-panel__check {
    animation: quick-check-arrive 620ms cubic-bezier(0.16, 1, 0.3, 1) 260ms both;
  }

  .quick-done-panel__check path {
    stroke-dasharray: 1;
    stroke-dashoffset: 1;
    animation: quick-check-draw 300ms ease 560ms forwards;
  }
}

@keyframes quick-check-arrive {
  0% {
    opacity: 0;
    transform: scale(0.35) rotate(-12deg);
  }
  68% {
    opacity: 1;
    transform: scale(1.1) rotate(3deg);
  }
  100% {
    opacity: 1;
    transform: none;
  }
}

@keyframes quick-check-draw {
  to {
    stroke-dashoffset: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .quick-done-panel__image,
  .quick-done-panel__check,
  .quick-done-panel__check path {
    animation: none;
  }
}
</style>
