<script setup lang="ts">
import { computed, ref } from 'vue'
import { Baby, Plus } from 'lucide-vue-next'

import { BaseBottomSheet } from '@/components/feedback'
import { useToast } from '@/composables/useToast'
import missionPigUrl from '@/assets/images/home/mission-pig.png'
import { childMissions } from '@/mocks/childFinanceFlow'

type MissionFilter = 'all' | 'progress' | 'review' | 'completed' | 'canceled'

const filters: { label: string; value: MissionFilter }[] = [
  { label: '전체', value: 'all' },
  { label: '진행 중', value: 'progress' },
  { label: '확인 필요', value: 'review' },
  { label: '완료', value: 'completed' },
  { label: '취소', value: 'canceled' },
]

const selectedFilter = ref<MissionFilter>('all')
const showCreateSheet = ref(false)
const missionTitle = ref('')
const missionDescription = ref('')
const missionReward = ref<number | null>(null)
const pendingMissionAction = ref<{
  missionId: string
  reason: 'reject' | 'cancel'
} | null>(null)
const { showToast } = useToast()

const statusOrder = { review: 0, progress: 1, completed: 2, canceled: 3 }
const missions = computed(() =>
  [...childMissions]
    .filter((mission) => selectedFilter.value === 'all' || mission.status === selectedFilter.value)
    .sort(
      (current, next) =>
        (statusOrder[current.status as keyof typeof statusOrder] ?? 1) -
        (statusOrder[next.status as keyof typeof statusOrder] ?? 1),
    ),
)

const progressCount = computed(
  () => childMissions.filter((mission) => mission.status === 'progress').length,
)
const reviewCount = computed(
  () => childMissions.filter((mission) => mission.status === 'review').length,
)
const completedCount = computed(
  () => childMissions.filter((mission) => mission.status === 'completed').length,
)
const pendingMission = computed(() =>
  pendingMissionAction.value
    ? childMissions.find((mission) => mission.id === pendingMissionAction.value?.missionId)
    : undefined,
)
const missionActionTitle = computed(() =>
  pendingMissionAction.value?.reason === 'reject' ? '미션을 반려할까요?' : '미션을 취소할까요?',
)

const formatCurrency = (amount: number) => `${amount.toLocaleString('ko-KR')}원`

const updateMissionReward = (event: Event) => {
  const input = event.target as HTMLInputElement
  const digits = input.value.replace(/\D/g, '')
  missionReward.value = digits ? Number(digits) : null
  input.value = missionReward.value?.toLocaleString('ko-KR') ?? ''
}

const statusMeta = {
  progress: {
    label: '진행 중',
    className: 'border-[var(--color-border)] bg-white',
    badgeClassName: 'bg-[#eaf8ff] text-[var(--color-selected-text)]',
  },
  review: {
    label: '확인 필요',
    className: 'border-[var(--color-border)] bg-white',
    badgeClassName: 'bg-[#fff4ce] text-[#9a7112]',
  },
  completed: {
    label: '완료',
    className: 'border-[var(--color-border)] bg-[#f7f9fa] opacity-70',
    badgeClassName: 'bg-[#e8f7ed] text-[#378454]',
  },
  canceled: {
    label: '취소',
    className: 'border-[var(--color-border)] bg-[#f7f9fa] opacity-70',
    badgeClassName: 'bg-[#eceff2] text-[var(--color-text-secondary)]',
  },
}

const getStatusMeta = (status: string) =>
  statusMeta[status as keyof typeof statusMeta] ?? statusMeta.progress

const approveMission = (missionId: string) => {
  const mission = childMissions.find((item) => item.id === missionId)
  if (!mission) return
  mission.status = 'completed'
  showToast(`${mission.title} 미션을 확인하고 보상했어요.`, 'success')
}

const requestMissionCancellation = (missionId: string, reason: 'reject' | 'cancel') => {
  const mission = childMissions.find((item) => item.id === missionId)
  if (!mission) return

  pendingMissionAction.value = { missionId, reason }
}

const closeMissionActionSheet = () => {
  pendingMissionAction.value = null
}

const confirmMissionCancellation = () => {
  const action = pendingMissionAction.value
  if (!action) return

  const mission = childMissions.find((item) => item.id === action.missionId)
  if (!mission) {
    closeMissionActionSheet()
    return
  }

  mission.status = 'canceled'
  closeMissionActionSheet()
  showToast(
    action.reason === 'reject'
      ? `${mission.title} 미션을 반려했어요.`
      : `${mission.title} 미션을 취소했어요.`,
    'success',
  )
}

const resetCreateForm = () => {
  missionTitle.value = ''
  missionDescription.value = ''
  missionReward.value = null
}

const closeCreateSheet = () => {
  showCreateSheet.value = false
  resetCreateForm()
}

const createMission = () => {
  const title = missionTitle.value.trim()
  const description = missionDescription.value.trim()
  const reward = Number(missionReward.value)

  if (!title || !description || !Number.isFinite(reward) || reward <= 0) {
    showToast('미션 내용과 보상 금액을 모두 입력해 주세요.', 'error')
    return
  }

  childMissions.unshift({
    id: `mission-${Date.now()}`,
    title,
    description,
    reward,
    status: 'progress',
    icon: '☑️',
  })
  closeCreateSheet()
  selectedFilter.value = 'all'
  showToast('깨비에게 새로운 용돈 미션을 보냈어요.', 'success')
}
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-[var(--color-surface)] px-[18px] pt-[18px] pb-[154px] text-[var(--color-text-primary)] max-[350px]:px-3.5"
  >
    <section
      class="relative overflow-hidden rounded-3xl border border-[#cfeaf7] bg-[#eef9fe] p-5 shadow-[0_12px_26px_rgb(61_157_203_/_5%)]"
    >
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2.5">
          <span
            class="grid size-11 place-items-center rounded-[15px] bg-white text-[var(--color-brand-primary-pressed)]"
            ><Baby :size="25" :stroke-width="2.2"
          /></span>
          <div class="grid gap-0.5">
            <strong class="text-base">깨비</strong>
          </div>
        </div>
        <span
          class="rounded-full bg-white px-2.5 py-[7px] text-[11px] font-bold text-[var(--color-brand-primary-pressed)]"
          >총 {{ childMissions.length }}개</span
        >
      </div>

      <div class="relative z-[1] mt-[25px] max-w-[62%]">
        <p class="mt-0 mb-[7px] text-xs text-[var(--color-text-secondary)]">
          작은 실천이 좋은 금융 습관이 되도록
        </p>
        <h1 class="m-0 text-2xl leading-[1.38] font-extrabold tracking-[-0.04em]">
          깨비에게 용돈 미션을<br />만들어 주세요
        </h1>
      </div>

      <img
        class="pointer-events-none absolute top-[78px] right-3 h-[132px] w-[120px] object-contain"
        :src="missionPigUrl"
        alt="미션을 확인하는 깨비"
      />

      <div
        class="mt-[22px] grid grid-cols-3 rounded-[17px] bg-white px-2 py-3.5 [&>div]:grid [&>div]:justify-items-center [&>div]:gap-[3px] [&>div]:border-r [&>div]:border-[var(--color-border)] [&>div:last-child]:border-0 [&_span]:text-[10px] [&_span]:text-[var(--color-text-secondary)] [&_strong]:text-[19px] [&_strong]:text-[var(--color-brand-primary-pressed)]"
        aria-label="미션 현황"
      >
        <div>
          <strong>{{ progressCount }}</strong
          ><span>진행 중</span>
        </div>
        <div>
          <strong>{{ reviewCount }}</strong
          ><span>확인 필요</span>
        </div>
        <div>
          <strong>{{ completedCount }}</strong
          ><span>완료</span>
        </div>
      </div>
    </section>

    <section class="mt-4" aria-labelledby="mission-list-title">
      <div
        class="mt-[17px] grid grid-cols-5 gap-1 rounded-[14px] bg-[#f2f5f7] p-1"
        role="tablist"
        aria-label="미션 상태 필터"
      >
        <button
          v-for="filter in filters"
          :key="filter.value"
          type="button"
          role="tab"
          :aria-selected="selectedFilter === filter.value"
          class="relative min-w-0 rounded-[11px] border-0 px-0.5 py-2.5 text-[11px] font-semibold transition-colors max-[350px]:text-[10px]"
          :class="
            selectedFilter === filter.value
              ? 'bg-white text-[var(--color-text-primary)] shadow-[0_3px_10px_rgb(80_102_117_/_6%)]'
              : 'bg-transparent text-[var(--color-text-secondary)]'
          "
          @click="selectedFilter = filter.value"
        >
          {{ filter.label }}
          <span
            v-if="filter.value === 'review' && reviewCount"
            class="absolute top-0.5 right-[3px] grid size-4 place-items-center rounded-full bg-[var(--color-brand-primary)] text-[9px] text-white"
            >{{ reviewCount }}</span
          >
        </button>
      </div>

      <div v-if="missions.length" class="mt-4 grid gap-[13px]">
        <article
          v-for="mission in missions"
          :key="mission.id"
          class="rounded-[19px] border p-[17px] shadow-[0_8px_22px_rgb(89_113_128_/_6%)]"
          :class="getStatusMeta(mission.status).className"
        >
          <div class="flex min-w-0 items-center justify-between gap-3">
            <h3 class="m-0 min-w-0 text-[17px] leading-tight font-bold">
              {{ mission.title }}
            </h3>
            <span
              class="shrink-0 rounded-full px-2 py-[5px] text-[10px] leading-none font-bold"
              :class="getStatusMeta(mission.status).badgeClassName"
              >{{ getStatusMeta(mission.status).label }}</span
            >
          </div>

          <p class="mt-2 mb-0 text-[13px] text-[var(--color-text-secondary)]">
            {{ mission.description }}
          </p>

          <div
            class="mt-4 flex items-center justify-between gap-3 border-t border-dashed border-[#cbd7de] pt-3.5"
          >
            <div class="grid gap-0.5">
              <strong
                class="text-base"
                :class="
                  mission.status === 'completed'
                    ? 'text-[var(--color-text-primary)]'
                    : 'text-[var(--color-brand-primary)]'
                "
                >{{ formatCurrency(mission.reward) }}</strong
              >
            </div>
            <div v-if="mission.status === 'review'" class="flex shrink-0 items-center gap-1.5">
              <button
                type="button"
                class="rounded-xl border border-[#e1e7eb] bg-white px-3 py-2.5 text-xs font-bold text-[var(--color-text-secondary)] active:bg-[#f5f7f8]"
                @click="requestMissionCancellation(mission.id, 'reject')"
              >
                반려하기
              </button>
              <button
                type="button"
                class="flex items-center gap-[5px] rounded-xl border-0 bg-[var(--color-brand-primary)] px-3 py-2.5 text-xs font-bold text-white"
                @click="approveMission(mission.id)"
              >
                보상하기
              </button>
            </div>
            <button
              v-else-if="mission.status === 'progress'"
              type="button"
              class="shrink-0 rounded-xl border border-[#e5e9ec] bg-white px-3 py-2.5 text-xs font-bold text-[var(--color-text-secondary)] active:bg-[#f5f7f8]"
              @click="requestMissionCancellation(mission.id, 'cancel')"
            >
              취소하기
            </button>
          </div>
        </article>
      </div>

      <div
        v-else
        class="mt-4 grid justify-items-center gap-2 rounded-[20px] border border-dashed border-[var(--color-border)] bg-white px-5 py-12 text-center text-[var(--color-text-secondary)]"
      >
        <strong class="text-sm text-[var(--color-text-primary)]">해당하는 미션이 없어요</strong>
        <span class="text-[11px] leading-normal"
          >새로운 미션을 만들어 아이의 도전을 응원해 주세요.</span
        >
      </div>
    </section>

    <button
      class="fixed right-1/2 bottom-[calc(var(--app-bottom-nav-height)+16px+env(safe-area-inset-bottom))] z-10 flex min-h-[54px] w-[calc(100%-36px)] max-w-[394px] translate-x-1/2 items-center justify-center gap-[7px] rounded-[17px] border-0 bg-[var(--color-brand-primary)] text-[15px] font-extrabold text-white shadow-[0_12px_24px_rgb(43_171_232_/_25%)]"
      type="button"
      @click="showCreateSheet = true"
    >
      <Plus :size="21" :stroke-width="2.7" />
      새 미션 만들기
    </button>

    <BaseBottomSheet :open="showCreateSheet" title="새 용돈 미션" @close="closeCreateSheet">
      <form class="grid gap-[17px] pt-[5px]" @submit.prevent="createMission">
        <label class="grid gap-2 text-[13px] font-bold text-[var(--color-text-primary)]">
          <span>미션 이름 <b class="text-[var(--color-danger)]">*</b></span>
          <input
            v-model="missionTitle"
            class="h-[49px] font-normal w-full rounded-[13px] border border-[var(--color-border)] bg-white px-3.5 text-sm text-[var(--color-text-primary)] outline-none focus:border-[var(--color-brand-primary)] focus:shadow-[0_0_0_3px_var(--color-selected-background)]"
            type="text"
            placeholder="예: 일주일 동안 방 정리하기"
          />
        </label>
        <label class="grid gap-2 text-[13px] font-bold text-[var(--color-text-primary)]">
          <span>미션 내용 <b class="text-[var(--color-danger)]">*</b></span>
          <textarea
            v-model="missionDescription"
            class="w-full font-normal resize-none rounded-[13px] border border-[var(--color-border)] bg-white px-3.5 py-[13px] text-sm leading-normal text-[var(--color-text-primary)] outline-none focus:border-[var(--color-brand-primary)] focus:shadow-[0_0_0_3px_var(--color-selected-background)]"
            rows="3"
            placeholder="아이가 해야 할 일을 알려주세요"
          ></textarea>
        </label>
        <label class="grid gap-2 text-[13px] font-bold text-[var(--color-text-primary)]">
          <span>완료 보상 <b class="text-[var(--color-danger)]">*</b></span>
          <span class="relative block">
            <input
              :value="missionReward?.toLocaleString('ko-KR') ?? ''"
              class="h-[49px] font-normal w-full rounded-[13px] border border-[var(--color-border)] bg-white pr-[42px] pl-3.5 text-sm text-[var(--color-text-primary)] outline-none focus:border-[var(--color-brand-primary)] focus:shadow-[0_0_0_3px_var(--color-selected-background)]"
              type="text"
              inputmode="numeric"
              pattern="[0-9]*"
              placeholder="0"
              @input="updateMissionReward"
            />
            <em
              class="absolute top-1/2 right-[15px] -translate-y-1/2 text-sm font-normal not-italic text-[var(--color-text-secondary)]"
              >원</em
            >
          </span>
        </label>
        <button
          class="mt-[3px] min-h-[52px] rounded-[15px] border-0 bg-[var(--color-brand-primary)] text-[15px] font-extrabold text-white"
          type="submit"
        >
          미션 보내기
        </button>
      </form>
    </BaseBottomSheet>

    <BaseBottomSheet
      :open="Boolean(pendingMissionAction)"
      :title="missionActionTitle"
      @close="closeMissionActionSheet"
    >
      <div v-if="pendingMission && pendingMissionAction" class="grid gap-5 pt-1">
        <div class="rounded-2xl bg-[#f7f9fa] px-4 py-3.5 flex items-center justify-between">
          <strong class="block truncate text-sm text-[var(--color-text-primary)]">
            {{ pendingMission.title }}
          </strong>
          <span class="block text-xs text-[var(--color-text-secondary)]">
            {{ formatCurrency(pendingMission.reward) }}
          </span>
        </div>

        <div class="grid grid-cols-2 gap-3">
          <button
            type="button"
            class="h-[52px] rounded-xl border border-[var(--color-border)] bg-white text-sm font-bold text-[var(--color-text-secondary)] active:bg-[#f5f7f8]"
            @click="closeMissionActionSheet"
          >
            계속 진행
          </button>
          <button
            type="button"
            class="h-[52px] rounded-xl border-0 bg-[var(--color-danger)] text-sm font-bold text-white active:opacity-85"
            @click="confirmMissionCancellation"
          >
            {{ pendingMissionAction.reason === 'reject' ? '반려하기' : '취소하기' }}
          </button>
        </div>
      </div>
    </BaseBottomSheet>
  </main>
</template>
