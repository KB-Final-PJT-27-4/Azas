<script setup lang="ts">
defineProps<{
  goalName: string
  amount: number
  targetDate: string
}>()

const emit = defineEmits<{
  'update:amount': [value: number]
  'update:targetDate': [value: string]
  openRecommendation: []
}>()

const minimumMonth = new Date().toISOString().slice(0, 7)

const updateAmount = (event: Event) => {
  const value = Number((event.target as HTMLInputElement).value.replace(/[^0-9]/g, ''))
  emit('update:amount', value)
}
</script>

<template>
  <section>
    <h1 class="text-[25px] leading-[1.35] font-bold tracking-[-0.04em]">
      <strong class="text-[var(--color-selected-text)]">{{ goalName }}</strong> 목표 금액을
      정해주세요
    </h1>
    <p class="mt-2 text-sm text-[var(--color-text-secondary)]">
      금액과 시기를 입력하면 월 저축액을 계산해드려요.
    </p>

    <div class="mt-9 grid gap-6">
      <label class="grid gap-2">
        <span class="flex items-center justify-between text-sm font-bold">
          목표 금액
          <button
            class="rounded-full border border-[var(--color-border)] px-3 py-1 text-xs"
            type="button"
            @click="emit('openRecommendation')"
          >
            AI 추천 ⓘ
          </button>
        </span>
        <div
          class="flex h-16 items-center rounded-2xl bg-[var(--color-brand-secondary)] px-5 text-[27px] font-bold"
        >
          <input
            class="min-w-0 flex-1 bg-transparent outline-none"
            inputmode="numeric"
            :value="amount.toLocaleString('ko-KR')"
            aria-label="목표 금액"
            @input="updateAmount"
          />
          <span>원</span>
        </div>
      </label>

      <label class="grid gap-2">
        <span class="text-sm font-bold">목표 달성 시기</span>
        <input
          class="h-14 rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 outline-none focus:border-[var(--color-brand-primary-pressed)]"
          type="month"
          :min="minimumMonth"
          :value="targetDate"
          @input="emit('update:targetDate', ($event.target as HTMLInputElement).value)"
        />
      </label>

      <div class="rounded-2xl bg-[var(--color-selected-background)] p-5">
        <strong class="text-xl text-[var(--color-selected-text)]">
          매월 약 {{ Math.ceil(amount / 240).toLocaleString('ko-KR') }}원
        </strong>
        <p class="mt-2 text-sm text-[var(--color-text-secondary)]">
          현재부터 240개월 동안 균등 저축 기준
        </p>
      </div>
    </div>
  </section>
</template>
