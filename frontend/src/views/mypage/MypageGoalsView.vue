<script setup lang="ts">
import { EllipsisVertical } from 'lucide-vue-next'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

interface ManagedGoal {
  id: number
  name: string
  amount: number
  targetDate: string
}

const router = useRouter()
const openMenuId = ref<number | null>(null)
const goals = ref<ManagedGoal[]>([
  {
    id: 1,
    name: '대학자금',
    amount: 30_000_000,
    targetDate: '2045-03',
  },
  {
    id: 2,
    name: '독립자금',
    amount: 10_000_000,
    targetDate: '2045-03',
  },
])

const formatAmount = (amount: number) => `${amount.toLocaleString('ko-KR')}원`

const formatTargetDate = (targetDate: string) => {
  const [year, month] = targetDate.split('-')
  return `${year}년 ${Number(month)}월`
}

const editGoal = (goalId: number) => {
  openMenuId.value = null
  router.push({ name: 'MypageGoalEdit', params: { goalsId: goalId } })
}

const toggleGoalMenu = (goalId: number) => {
  openMenuId.value = openMenuId.value === goalId ? null : goalId
}

const deleteGoal = (goalId: number) => {
  goals.value = goals.value.filter((goal) => goal.id !== goalId)
  openMenuId.value = null
}

const addGoal = () => {
  router.push({ name: 'Goals' })
}
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-[var(--color-surface)] px-6 pt-8 pb-10 text-[var(--color-text-primary)]"
    @click="openMenuId = null"
  >
    <section aria-labelledby="goals-title">
      <h1 id="goals-title" class="text-[28px] leading-tight font-bold tracking-[-0.04em]">
        등록된 목표
      </h1>
      <p class="mt-3 text-sm text-[var(--color-text-secondary)]">
        목표 금액과 달성 시기를 수정할 수 있어요.
      </p>
    </section>

    <section class="mt-8" aria-label="등록된 목표 목록">
      <div class="grid gap-4">
        <article
          v-for="goal in goals"
          :key="goal.id"
          class="relative rounded-[20px] border border-[var(--color-border)] bg-[var(--color-surface)] px-5 py-5 shadow-sm"
        >
          <div class="flex items-start justify-between gap-4">
            <h2 class="min-w-0 text-xl font-bold tracking-[-0.03em]">{{ goal.name }}</h2>
            <button
              class="grid size-8 shrink-0 place-items-center rounded-md bg-[var(--color-surface-muted)] text-[var(--color-text-secondary)] active:bg-[var(--color-selected-background)]"
              type="button"
              :aria-label="`${goal.name} 더보기`"
              :aria-expanded="openMenuId === goal.id"
              @click.stop="toggleGoalMenu(goal.id)"
            >
              <EllipsisVertical :size="18" />
            </button>

            <div
              v-if="openMenuId === goal.id"
              class="absolute top-[64px] right-5 z-10 w-32 overflow-hidden rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] py-1 shadow-sm"
              role="menu"
              @click.stop
            >
              <button
                class="h-11 w-full px-4 text-left text-sm font-semibold transition-colors hover:bg-[var(--color-surface-muted)]"
                type="button"
                role="menuitem"
                @click="editGoal(goal.id)"
              >
                수정
              </button>
              <button
                class="h-11 w-full px-4 text-left text-sm font-semibold text-[var(--color-danger)] transition-colors hover:bg-[var(--color-danger-background)]"
                type="button"
                role="menuitem"
                @click="deleteGoal(goal.id)"
              >
                삭제
              </button>
            </div>
          </div>
          <div class="mt-3 flex items-center justify-between gap-4">
            <strong class="text-lg font-semibold text-[var(--color-selected-text)]">
              {{ formatAmount(goal.amount) }}
            </strong>
            <p class="shrink-0 text-sm text-[var(--color-text-secondary)]">
              {{ formatTargetDate(goal.targetDate) }}
            </p>
          </div>
        </article>
      </div>
    </section>

    <button
      class="mt-8 flex h-14 w-full items-center justify-center gap-2 rounded-2xl bg-[var(--color-brand-primary)] text-base font-bold text-[var(--color-text-inverse)] transition-colors active:bg-[var(--color-brand-primary-pressed)]"
      type="button"
      @click="addGoal"
    >
      목표 추가하기
    </button>
  </main>
</template>
