<script setup lang="ts">
import { computed, type Component } from 'vue'
import { CheckSquare, Coins, Droplet, Sparkles } from 'lucide-vue-next'

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
const defaultMissionVisual: { icon: Component; iconClass: string } = {
  icon: CheckSquare,
  iconClass: 'bg-[#eef8ff] text-[var(--color-brand-primary)]',
}
const missionVisuals: Record<string, { icon: Component; iconClass: string }> = {
  'mission-1': {
    icon: CheckSquare,
    iconClass: 'bg-[#eef8ff] text-[var(--color-brand-primary)]',
  },
  'mission-2': {
    icon: Coins,
    iconClass: 'bg-[#fff7d7] text-[#c99a13]',
  },
  'mission-3': {
    icon: Droplet,
    iconClass: 'bg-[#eef8ff] text-[var(--color-brand-primary)]',
  },
}
const getMissionVisual = (missionId: string) => missionVisuals[missionId] ?? defaultMissionVisual
const getStatusLabel = (status: string) => (status === 'completed' ? '완료됨' : '진행 중')
const activeMissionCount = computed(
  () => missions.value.filter((mission) => mission.status !== 'completed').length,
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
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-5 pb-[112px] text-[var(--color-text-primary)]">
    <section
      class="relative overflow-hidden rounded-[26px] border border-[#dceef6] bg-[#eaf8ff] px-5 pt-5 pb-5 shadow-[0_12px_30px_rgba(54,112,139,0.08)]"
      aria-label="미션 요약"
    >
      <div class="relative z-[1] max-w-[58%]">
        <span class="inline-flex items-center gap-1 rounded-full bg-white/85 px-3 py-1.5 text-[12px] font-bold text-[var(--color-selected-text)]">
          <Sparkles :size="14" :stroke-width="2.5" />
          오늘 할 일
        </span>
        <h1 class="mt-4 mb-0 text-[23px] leading-[1.25] font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]">
          미션을 완료하고<br />
          용돈을 모아봐요
        </h1>
        <p class="mt-2 mb-0 text-[13px] leading-[1.45] text-[#628096]">
          진행 중 {{ activeMissionCount }}개 · 완료 {{ completedMissionCount }}개
        </p>
      </div>

      <div class="relative z-[2] mt-5 rounded-[18px] bg-white/92 px-4 py-3.5 backdrop-blur-sm">
        <div class="flex items-center justify-between gap-3">
          <span class="text-[12px] font-bold text-[var(--color-text-secondary)]">
            받을 수 있는 보상
          </span>
          <strong class="text-[17px] font-extrabold text-[var(--color-brand-primary)]">
            {{ formatCurrency(activeRewardTotal) }}
          </strong>
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
          <div
            class="grid size-11 place-items-center rounded-[14px]"
            :class="
              mission.status === 'completed'
                ? 'bg-[#e4e8ec] text-[#8b98a4]'
                : getMissionVisual(mission.id).iconClass
            "
          >
            <component
              :is="getMissionVisual(mission.id).icon"
              :size="21"
              :stroke-width="2.4"
              aria-hidden="true"
            />
          </div>
          <div class="min-w-0">
            <strong
              class="block truncate text-[16px] leading-snug font-bold"
              :class="mission.status === 'completed' ? 'text-[#7d8790]' : 'text-[var(--color-text-primary)]'"
            >
              {{ mission.title }}
            </strong>
            <span
              class="mt-1 block truncate text-[13px] leading-snug"
              :class="mission.status === 'completed' ? 'text-[#9aa4ad]' : 'text-[var(--color-text-secondary)]'"
            >
              {{ mission.description }}
            </span>
          </div>
        </div>

        <div class="mission-ticket__reward">
          <strong
            class="text-[17px] leading-tight font-extrabold"
            :class="mission.status === 'completed' ? 'text-[#9aa4ad]' : 'text-[var(--color-brand-primary)]'"
          >
            {{ formatCurrency(mission.reward) }}
          </strong>
          <span
            class="text-[12px] leading-none font-bold"
            :class="
              mission.status === 'completed'
                ? 'text-[#77828c]'
                : 'text-[var(--color-text-secondary)]'
            "
          >
            {{ getStatusLabel(mission.status) }}
          </span>
        </div>
      </article>
      </div>
    </section>

    <section class="mt-6 rounded-[18px] bg-[#fff9df] px-5 py-5 text-center shadow-[0_8px_22px_rgba(242,213,117,0.12)]">
      <p class="m-0 text-[15px] leading-[1.7] text-[var(--color-text-primary)]">
        미션을 완료하면 부모님이 확인하고<br />
        보상 용돈을 보내주세요!
      </p>
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
  grid-template-columns: 44px minmax(0, 1fr);
  align-items: center;
  gap: 14px;
  min-width: 0;
  padding: 18px 16px;
}

.mission-ticket__reward {
  position: relative;
  display: grid;
  align-content: center;
  justify-items: center;
  gap: 9px;
  min-width: 0;
  padding: 16px 12px;
  background: linear-gradient(180deg, rgb(255 255 255 / 0%) 0%, rgb(234 248 255 / 44%) 100%);
}

.mission-ticket__reward::before {
  position: absolute;
  top: 12px;
  bottom: 12px;
  left: 0;
  width: 1px;
  content: '';
  border-left: 1px dashed #cfd9df;
}

.mission-ticket__reward::after {
  position: absolute;
  top: 8px;
  left: -5px;
  color: #bdc8cf;
  font-size: 9px;
  line-height: 1;
  transform: rotate(90deg);
  content: '✂';
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
