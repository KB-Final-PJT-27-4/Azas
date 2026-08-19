<script setup lang="ts">
import { computed } from 'vue'
import { CheckSquare } from 'lucide-vue-next'

import ChildBottomNavigation from '@/components/child/ChildBottomNavigation.vue'
import { childMissions } from '@/mocks/childFinanceFlow'

const missions = computed(() =>
  [...childMissions].sort((current, next) => {
    const order = { review: 0, progress: 1, completed: 2 }
    return (
      (order[current.status as keyof typeof order] ?? 1) -
      (order[next.status as keyof typeof order] ?? 1)
    )
  }),
)

const formatCurrency = (amount: number) => `${amount.toLocaleString('ko-KR')}원`
const getStatusLabel = (status: string) => {
  if (status === 'completed') return '완료됨'
  if (status === 'review') return '확인 필요'
  return '진행 중'
}
const getStatusBadgeClass = (status: string) => {
  if (status === 'completed') return 'bg-[#eaf8ef] text-[#2f9b62]'
  if (status === 'review') return 'bg-[#fff7dd] text-[#c8951d]'
  return 'bg-[#eaf8ff] text-[var(--color-selected-text)]'
}
const progressMissionCount = computed(
  () => missions.value.filter((mission) => mission.status === 'progress').length,
)
const completedMissionCount = computed(
  () => missions.value.filter((mission) => mission.status === 'completed').length,
)
const activeRewardTotal = computed(() =>
  missions.value
    .filter((mission) => mission.status !== 'completed')
    .reduce((total, mission) => total + mission.reward, 0),
)
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-5 pb-[112px] text-[var(--color-text-primary)]"
  >
    <section
      class="relative overflow-hidden rounded-[26px] border border-[#dceef6] bg-[#eaf8ff] px-5 pt-5 pb-5 shadow-[0_12px_30px_rgba(54,112,139,0.08)]"
      aria-label="미션 요약"
    >
      <div class="relative z-[1]">
        <div class="mb-7 flex items-center justify-between gap-3">
          <div class="flex min-w-0 items-center gap-3">
            <span
              class="grid size-10 shrink-0 place-items-center rounded-[13px] bg-white text-[var(--color-brand-primary)]"
            >
              <CheckSquare :size="22" :stroke-width="2.4" aria-hidden="true" />
            </span>
            <strong class="truncate text-[16px] font-extrabold">깨비</strong>
          </div>
          <span
            class="rounded-full bg-white px-3 py-1.5 text-[12px] font-bold text-[var(--color-selected-text)]"
          >
            총 {{ missions.length }}개
          </span>
        </div>

        <p class="m-0 text-[13px] font-medium leading-[1.5] text-[#628096]">
          작은 실천이 좋은 금융 습관이 되도록
        </p>
        <h1
          class="mt-2 mb-0 text-[24px] leading-[1.35] font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]"
        >
          깨비의 용돈 미션을<br />
          확인해요
        </h1>
      </div>

      <div
        class="relative z-[2] mt-5 grid grid-cols-3 overflow-hidden rounded-[18px] bg-white px-3 py-3.5 shadow-[0_8px_20px_rgba(54,112,139,0.06)]"
      >
        <div class="grid place-items-center gap-1 border-r border-[#e5edf1]">
          <strong class="text-[22px] leading-none font-extrabold text-[var(--color-brand-primary)]">
            {{ progressMissionCount }}
          </strong>
          <span class="text-[11px] font-medium text-[var(--color-text-secondary)]">진행 중</span>
        </div>
        <div class="grid place-items-center gap-1 border-r border-[#e5edf1]">
          <strong class="text-[22px] leading-none font-extrabold text-[var(--color-brand-primary)]">
            {{ completedMissionCount }}
          </strong>
          <span class="text-[11px] font-medium text-[var(--color-text-secondary)]">완료</span>
        </div>
        <div class="grid place-items-center gap-1">
          <strong class="text-[17px] leading-none font-extrabold text-[var(--color-brand-primary)]">
            {{ formatCurrency(activeRewardTotal) }}
          </strong>
          <span class="text-[11px] font-medium text-[var(--color-text-secondary)]">예상 보상</span>
        </div>
      </div>
    </section>

    <section class="mt-8" aria-labelledby="child-mission-list-title">
      <h2
        id="child-mission-list-title"
        class="mb-4 text-[22px] leading-none font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]"
      >
        진행할 미션
      </h2>

      <div class="grid gap-3">
        <article
          v-for="mission in missions"
          :key="mission.id"
          class="mission-ticket"
          :class="
            mission.status === 'completed' ? 'mission-ticket--completed' : 'mission-ticket--active'
          "
        >
          <div class="mission-ticket__content">
            <div class="min-w-0">
              <strong
                class="block truncate text-[16px] leading-snug font-bold"
                :class="
                  mission.status === 'completed'
                    ? 'text-[#7d8790]'
                    : 'text-[var(--color-text-primary)]'
                "
              >
                {{ mission.title }}
              </strong>
              <span
                class="mt-1 block truncate text-[13px] leading-snug"
                :class="
                  mission.status === 'completed'
                    ? 'text-[#9aa4ad]'
                    : 'text-[var(--color-text-secondary)]'
                "
              >
                {{ mission.description }}
              </span>
            </div>
          </div>

          <div class="mission-ticket__reward">
            <span
              class="rounded-full px-2.5 py-1 text-[11px] leading-none font-bold"
              :class="getStatusBadgeClass(mission.status)"
            >
              {{ getStatusLabel(mission.status) }}
            </span>
            <strong
              class="text-[18px] leading-tight font-extrabold"
              :class="
                mission.status === 'completed'
                  ? 'text-[#9aa4ad]'
                  : 'text-[var(--color-brand-primary)]'
              "
            >
              {{ formatCurrency(mission.reward) }}
            </strong>
          </div>
        </article>
      </div>
    </section>

    <ChildBottomNavigation />
  </main>
</template>

<style scoped>
.mission-ticket {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 94px;
  min-height: 92px;
  overflow: hidden;
  border: 1px solid #dce8ee;
  border-radius: 20px;
  background: white;
  box-shadow: 0 10px 26px rgb(54 112 139 / 6%);
  transition:
    background-color 160ms ease,
    border-color 160ms ease,
    opacity 160ms ease;
}

.mission-ticket__content {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  align-items: center;
  min-width: 0;
  padding: 18px 18px;
}

.mission-ticket__reward {
  position: relative;
  display: grid;
  align-content: center;
  justify-items: center;
  gap: 9px;
  min-width: 0;
  padding: 16px 12px;
  background: transparent;
}

.mission-ticket__reward::before {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  width: 1px;
  content: '';
  border-left: 1px dashed #cfd9df;
}

.mission-ticket--active {
  border-color: #dce8ee;
}

.mission-ticket--completed {
  border-color: #e5e9ed;
  background: #f6f8fa;
  opacity: 0.78;
}
</style>
