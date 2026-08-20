<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

import { api, getApiErrorMessage } from '@/api'
import { useToast } from '@/composables/useToast'

import allowancePageBgUrl from '@/assets/images/child/child-allowance-page-bg.png'
import allowanceRequestPigUrl from '@/assets/images/child/child-allowance-request-pig.png'

const router = useRouter()
const { showToast } = useToast()
const allowanceAmount = ref('')
const reason = ref('')
const maxMoneyDigits = 8
const maxReasonLength = 200

const allowanceAmountValue = computed(() => Number(allowanceAmount.value.replace(/\D/g, '')) || 0)
const canSubmit = computed(() => allowanceAmountValue.value > 0 && reason.value.trim().length > 0)

const updateAllowanceAmount = (event: Event) => {
  const input = event.target as HTMLInputElement
  const digits = input.value.replace(/\D/g, '').slice(0, maxMoneyDigits)
  allowanceAmount.value = digits ? Number(digits).toLocaleString('ko-KR') : ''
  if (input.value !== allowanceAmount.value) input.value = allowanceAmount.value
}

const startAllowanceAmountEdit = (event: FocusEvent) => {
  allowanceAmount.value = allowanceAmountValue.value > 0 ? String(allowanceAmountValue.value) : ''
  const input = event.target as HTMLInputElement
  requestAnimationFrame(() => input.select())
}

const finishAllowanceAmountEdit = () => {
  allowanceAmount.value =
    allowanceAmountValue.value > 0 ? allowanceAmountValue.value.toLocaleString('ko-KR') : ''
}

const updateReason = (event: Event) => {
  const textarea = event.target as HTMLTextAreaElement
  reason.value = textarea.value.slice(0, maxReasonLength)
  if (textarea.value !== reason.value) textarea.value = reason.value
}

const isSubmitting = ref(false)
const submitRequest = async () => {
  if (!canSubmit.value) return
  isSubmitting.value = true
  try {
    await api.createAllowanceRequestUsingPOST({
      requested_amount: allowanceAmountValue.value,
      message: reason.value.trim(),
    })
    await router.push('/child/allowance-done')
  } catch (error) {
    showToast(getApiErrorMessage(error, '용돈 요청을 보내지 못했습니다.'), 'error')
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-[#eef8ff] bg-cover bg-top bg-no-repeat px-5 pt-7 pb-8"
    :style="{ backgroundImage: `url(${allowancePageBgUrl})` }"
  >
    <section class="text-center">
      <img
        class="mx-auto w-[168px] select-none object-contain"
        :src="allowanceRequestPigUrl"
        alt=""
        aria-hidden="true"
      />
      <h1 class="mt-4 mb-3 text-[24px] leading-[1.35] font-bold text-[var(--color-text-primary)]">
        부모님께<br />
        용돈을 요청해볼까요?
      </h1>
      <p class="m-0 text-[length:var(--font-size-sm)] leading-[1.5] text-[var(--color-text-secondary)]">
        하고 싶은 게 있다면<br />
        부모님께 용돈을 요청해보세요!
      </p>
    </section>

    <section
      class="mt-6 rounded-[22px] bg-white px-5 py-5 shadow-[0_14px_32px_rgb(110_122_138_/_10%)]"
    >
      <div class="mb-5">
        <div>
          <p class="m-0 text-[length:var(--font-size-md)] font-bold">얼마가 필요한가요?</p>
          <div
            class="mt-3 rounded-[14px] border border-[#dce8ee] bg-white px-4 py-3 transition focus-within:border-[var(--color-brand-primary)]"
          >
            <div class="flex min-w-0 items-baseline gap-1">
              <input
                :value="allowanceAmount"
                class="allowance-amount-input min-w-0 flex-1 border-0 bg-transparent p-0 text-[clamp(30px,7vw,40px)] leading-tight font-extrabold text-[var(--color-text-primary)] outline-none placeholder:text-[#9da5ad]"
                inputmode="numeric"
                placeholder="0"
                type="text"
                @focus="startAllowanceAmountEdit"
                @input="updateAllowanceAmount"
                @blur="finishAllowanceAmountEdit"
              />
              <span class="shrink-0 text-[20px] leading-none font-extrabold">원</span>
            </div>
          </div>
        </div>
      </div>

      <div class="my-5 h-px bg-[var(--color-border)]" />

      <label class="block text-[length:var(--font-size-md)] font-bold">
        부모님께 하고 싶은 말
        <div class="relative mt-3">
          <textarea
            :value="reason"
            class="block min-h-[132px] w-full resize-none rounded-[12px] border border-[var(--color-border)] px-4 py-4 pb-8 text-[length:var(--font-size-sm)] font-normal outline-none focus:border-[var(--color-brand-primary)]"
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

      <div class="mt-3 rounded-[12px] bg-[#f0fbff] px-4 py-4 text-[length:var(--font-size-xs)] leading-[1.65] text-[var(--color-text-secondary)]">
        <strong class="mb-2 block text-[var(--color-text-primary)]">
          이렇게 쓰면 용돈 받을 확률이 올라가요 ✨
        </strong>
        누가? 언제? 어디서? 무엇을? 왜? 얼마만큼?<br />
        예) 친구 생일 선물을 사려고 해요.<br />
        이번 주 토요일에 친구 집 근처 문구점에서 10,000원 정도 선물을 살 예정이에요.
      </div>
    </section>

    <button
      class="mt-5 h-14 w-full rounded-[14px] border-0 text-[length:var(--font-size-md)] font-bold text-white"
      :class="canSubmit ? 'bg-[var(--color-brand-primary)]' : 'bg-[#cbd8df]'"
      type="button"
      :disabled="!canSubmit"
      @click="submitRequest"
    >
      부모님께 요청하기
    </button>
  </main>
</template>

<style scoped>
.allowance-amount-input {
  display: block;
  width: 100%;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  font-variant-numeric: tabular-nums;
}
</style>
