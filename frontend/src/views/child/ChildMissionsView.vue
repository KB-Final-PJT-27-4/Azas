<script setup lang="ts">
import { computed, type Component } from 'vue'
import { CheckSquare, Coins, Droplet } from 'lucide-vue-next'

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
const getStatusLabel = (status: string) => {
  if (status === 'completed') return '완료됨'
  if (status === 'review') return '확인 대기'
  return '진행 중'
}
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-5 pb-[104px] text-[var(--color-text-primary)]"
  >
    <section class="grid gap-4">
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
              class="block truncate text-[17px] leading-snug font-bold"
              :class="
                mission.status === 'completed'
                  ? 'text-[#7d8790]'
                  : 'text-[var(--color-text-primary)]'
              "
            >
              {{ mission.title }}
            </strong>
            <span
              class="mt-1 block truncate text-[14px] leading-snug"
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
          <span
            class="text-[13px] leading-none font-bold"
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
    </section>

    <section class="mt-6 rounded-[16px] bg-[#fffbe7] px-5 py-5 text-center">
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
  border: 1px solid var(--color-border);
  border-radius: 18px;
  background: white;
  box-shadow: 0 10px 24px rgb(110 122 138 / 7%);
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
}

.mission-ticket__reward::before {
  position: absolute;
  top: 0;
  bottom: 0;
  left: 0;
  width: 2px;
  content: '';
  background-image: repeating-linear-gradient(
    to bottom,
    #8f9dab 0,
    #8f9dab 10px,
    transparent 10px,
    transparent 18px
  );
}

.mission-ticket--active {
  border-color: #dce8ee;
}

.mission-ticket--completed {
  border-color: #e5e9ed;
  background: #f5f7f8;
  opacity: 0.78;
}
</style>
