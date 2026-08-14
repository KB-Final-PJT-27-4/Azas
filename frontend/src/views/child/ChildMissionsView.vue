<script setup lang="ts">
import { computed, ref, type Component } from 'vue'
import { CheckSquare, Coins, Droplet } from 'lucide-vue-next'

import ChildBottomNavigation from '@/components/child/ChildBottomNavigation.vue'
import { childMissions } from '@/mocks/childFinanceFlow'

const selectedTab = ref<'progress' | 'done'>('progress')
const requestedMissionIds = ref<string[]>([])
const missions = computed(() =>
  childMissions.filter((mission) =>
    selectedTab.value === 'progress'
      ? !requestedMissionIds.value.includes(mission.id)
      : requestedMissionIds.value.includes(mission.id),
  ),
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
const getMissionVisual = (missionId: string) =>
  missionVisuals[missionId] ?? defaultMissionVisual
const requestMissionComplete = (missionId: string) => {
  if (!requestedMissionIds.value.includes(missionId)) {
    requestedMissionIds.value.push(missionId)
  }
}
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-5 pb-[104px] text-[var(--color-text-primary)]">
    <div class="grid grid-cols-2 gap-2 rounded-[16px] bg-[#f4f8fb] p-1">
      <button
        class="h-11 rounded-[13px] border-0 text-[15px] font-bold transition-colors"
        :class="selectedTab === 'progress' ? 'bg-white text-[var(--color-brand-primary)] shadow-[0_6px_14px_rgb(110_122_138_/_12%)]' : 'bg-transparent text-[var(--color-text-secondary)]'"
        type="button"
        @click="selectedTab = 'progress'"
      >
        진행 중
      </button>
      <button
        class="h-11 rounded-[13px] border-0 text-[15px] font-bold transition-colors"
        :class="selectedTab === 'done' ? 'bg-white text-[var(--color-brand-primary)] shadow-[0_6px_14px_rgb(110_122_138_/_12%)]' : 'bg-transparent text-[var(--color-text-secondary)]'"
        type="button"
        @click="selectedTab = 'done'"
      >
        완료
      </button>
    </div>

    <section class="mt-6 grid gap-4">
      <article
        v-for="mission in missions"
        :key="mission.id"
        class="grid grid-cols-[44px_minmax(0,1fr)_auto] items-center gap-3 rounded-[16px] border border-[var(--color-border)] bg-white px-4 py-4 shadow-[0_8px_20px_rgb(110_122_138_/_6%)]"
      >
        <div class="grid size-10 place-items-center rounded-[12px]" :class="getMissionVisual(mission.id).iconClass">
          <component
            :is="getMissionVisual(mission.id).icon"
            :size="20"
            :stroke-width="2.4"
            aria-hidden="true"
          />
        </div>
        <div class="min-w-0">
          <strong class="block truncate text-[17px] leading-snug font-bold text-[var(--color-text-primary)]">
            {{ mission.title }}
          </strong>
          <span class="mt-1 block truncate text-[14px] leading-snug text-[var(--color-text-secondary)]">
            {{ mission.description }}
          </span>
        </div>
        <div class="grid justify-items-end gap-2">
          <strong class="text-[14px] leading-none font-bold text-[var(--color-brand-primary)]">
            {{ formatCurrency(mission.reward) }}
          </strong>
          <button
            v-if="selectedTab === 'progress' && mission.status === 'completed'"
            class="h-[34px] rounded-[10px] border border-[#bfeaff] bg-[#f7fdff] px-3 text-[14px] font-bold text-[var(--color-brand-primary)]"
            type="button"
            @click="requestMissionComplete(mission.id)"
          >
            완료 요청하기
          </button>
          <span
            v-else-if="selectedTab === 'progress'"
            class="text-[14px] font-bold text-[var(--color-text-secondary)]"
          >
            진행 중
          </span>
          <span v-else class="text-[14px] font-bold text-[var(--color-text-secondary)]">
            확인 대기
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
