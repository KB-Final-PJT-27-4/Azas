<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

import allowancePageBgUrl from '@/assets/images/child/child-allowance-page-bg.png'
import allowanceRequestPigUrl from '@/assets/images/child/child-allowance-request-pig.png'
import { allowanceOptions } from '@/mocks/childHome'

const router = useRouter()
const selectedAmount = ref(10_000)
const customAmount = ref('')
const reason = ref('')

const customAmountValue = computed(() => Number(customAmount.value.replace(/\D/g, '')) || 0)
const requestAmount = computed(() => customAmountValue.value || selectedAmount.value)
const formattedCustomAmount = computed(() =>
  customAmountValue.value > 0 ? customAmountValue.value.toLocaleString('ko-KR') : '',
)
const canSubmit = computed(() => requestAmount.value > 0 && reason.value.trim().length > 0)

const formatCurrency = (amount: number) => `${amount.toLocaleString('ko-KR')}원`

const selectAmount = (amount: number) => {
  selectedAmount.value = amount
  customAmount.value = ''
}

const updateCustomAmount = (event: Event) => {
  const input = event.target as HTMLInputElement
  customAmount.value = input.value.replace(/\D/g, '')
}

const submitRequest = () => {
  if (!canSubmit.value) return
  router.push('/child/allowance-done')
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
      <div class="mb-5 flex items-end justify-between">
        <div>
          <p class="m-0 text-[length:var(--font-size-md)] font-bold">얼마가 필요한가요?</p>
          <strong class="mt-3 block text-[32px] font-bold">{{ formatCurrency(requestAmount) }}</strong>
        </div>
        <button
          class="h-9 rounded-[10px] border border-[#d8ebff] bg-white px-3 text-[length:var(--font-size-xs)] font-bold text-[var(--color-brand-primary)]"
          type="button"
          @click="customAmount = ''"
        >
          금액 변경
        </button>
      </div>

      <div class="grid grid-cols-3 gap-2">
        <button
          v-for="amount in allowanceOptions"
          :key="amount"
          class="h-11 rounded-full border text-[length:var(--font-size-sm)] font-bold"
          :class="
            selectedAmount === amount && !customAmount
              ? 'border-[var(--color-brand-primary)] bg-[var(--color-brand-primary)] text-white'
              : 'border-[var(--color-border)] bg-white text-[var(--color-text-secondary)]'
          "
          type="button"
          @click="selectAmount(amount)"
        >
          {{ formatCurrency(amount) }}
        </button>
      </div>

      <div class="relative mt-3">
        <input
          :value="formattedCustomAmount"
          class="h-11 w-full rounded-[12px] border border-[var(--color-border)] px-4 pr-10 text-center text-[length:var(--font-size-sm)] outline-none focus:border-[var(--color-brand-primary)]"
          inputmode="numeric"
          placeholder="직접 입력하기"
          type="text"
          @input="updateCustomAmount"
        />
        <span
          v-if="customAmountValue > 0"
          class="pointer-events-none absolute top-1/2 right-4 -translate-y-1/2 text-[length:var(--font-size-sm)] font-bold text-[var(--color-text-secondary)]"
        >
          원
        </span>
      </div>

      <div class="my-5 h-px bg-[var(--color-border)]" />

      <label class="block text-[length:var(--font-size-md)] font-bold">
        부모님께 하고 싶은 말
        <textarea
          v-model="reason"
          class="mt-3 min-h-[118px] w-full resize-none rounded-[12px] border border-[var(--color-border)] px-4 py-4 text-[length:var(--font-size-sm)] font-normal outline-none focus:border-[var(--color-brand-primary)]"
          maxlength="200"
          placeholder="용돈이 왜 필요한지 적어보세요 :)"
        />
      </label>

      <div class="mt-3 rounded-[12px] bg-[#f0fbff] px-4 py-4 text-[length:var(--font-size-xs)] leading-[1.65] text-[var(--color-text-secondary)]">
        <strong class="mb-2 block text-[var(--color-text-primary)]">
          이렇게 쓰면 용돈 받을 확률이 올라가요 ✨
        </strong>
        누가? 언제? 어디서? 무엇을? 왜? 얼마만큼?<br />
        예) 친구 생일 선물을 사려고 해요.<br />
        이번 주 토요일에 친구 집 근처 문구점에서 10,000원 정도 선물을 살 예정이에요.
      </div>
      <p class="mt-2 mb-0 text-right text-[11px] text-[var(--color-text-secondary)]">
        {{ reason.length }} / 200
      </p>
    </section>

    <button
      class="mt-5 h-14 w-full rounded-[14px] border-0 text-[length:var(--font-size-md)] font-bold text-white"
      :class="canSubmit ? 'bg-[var(--color-brand-primary)]' : 'bg-[#c8d2da]'"
      type="button"
      :disabled="!canSubmit"
      @click="submitRequest"
    >
      부모님께 요청하기
    </button>
  </main>
</template>
