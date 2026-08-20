<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Check, ChevronRight, FileText } from 'lucide-vue-next'

import completeStarUrl from '@/assets/images/accounts/complete-star.png'
import childQuizCompletePigUrl from '@/assets/images/child/child-quiz-complete-pig.png'
import { useChecklistProgress } from '@/composables/useChecklistProgress'
import {
  checklistItems,
  currentChildLifecycle,
  lifecycleStages,
  type ChecklistItem,
  type ChecklistInfoItem,
} from '@/mocks/lifecycleChecklist'

const router = useRouter()
const { checkedItemIds, isChecklistItemCompleted, toggleChecklistItem } = useChecklistProgress()

const selectedInfoItem = ref<ChecklistItem | null>(null)
const selectedDetailInfo = ref<ChecklistInfoItem | null>(null)
const pendingRouteItem = ref<ChecklistItem | null>(null)
const isCompleteSheetOpen = ref(false)
const selectedStageId = ref(currentChildLifecycle.childStatus)
const draggingSheet = ref<'info' | 'complete' | null>(null)
const sheetDragStartY = ref<number | null>(null)
const sheetDragOffsetY = ref(0)

const currentStage = computed(
  () => lifecycleStages.find((stage) => stage.id === selectedStageId.value) ?? lifecycleStages[0]!,
)

const currentStageItems = computed(() =>
  checklistItems
    .filter((item) => item.stageId === currentStage.value.id)
    .map((item, index) => ({ item, index }))
    .sort((current, next) => {
      const currentCompleted = checkedItemIds.value.has(current.item.id) ? 1 : 0
      const nextCompleted = checkedItemIds.value.has(next.item.id) ? 1 : 0

      return currentCompleted - nextCompleted || current.index - next.index
    })
    .map(({ item }) => item),
)

const completedCount = computed(
  () => currentStageItems.value.filter((item) => checkedItemIds.value.has(item.id)).length,
)
const totalCount = computed(() => currentStageItems.value.length)
const progressPercent = computed(() =>
  totalCount.value === 0 ? 0 : Math.round((completedCount.value / totalCount.value) * 100),
)
const progressStyle = computed(() => ({ width: `${progressPercent.value}%` }))

const hasChecklistAction = (item: ChecklistItem) =>
  item.actionType === 'info' || Boolean(item.route) || Boolean(item.externalUrl)

const openExternalUrl = (url: string) => {
  window.open(url, '_blank', 'noopener,noreferrer')
}

const openChecklistAction = (item: ChecklistItem) => {
  if (item.externalUrl) {
    openExternalUrl(item.externalUrl)
    return
  }

  if (item.actionType === 'info') {
    selectedInfoItem.value = item
    return
  }

  if (item.route) {
    pendingRouteItem.value = item
  }
}

const closeRouteConfirm = () => {
  pendingRouteItem.value = null
}

const confirmRouteNavigation = () => {
  if (!pendingRouteItem.value?.route) return

  const route = pendingRouteItem.value.route
  pendingRouteItem.value = null
  router.push(route)
}

const openInfoItem = (info: ChecklistInfoItem) => {
  if (info.externalUrl) {
    openExternalUrl(info.externalUrl)
    return
  }

  selectedDetailInfo.value = info
}

const closeDetailInfo = () => {
  selectedDetailInfo.value = null
}

const closeInfoSheet = () => {
  selectedInfoItem.value = null
  selectedDetailInfo.value = null
  resetSheetDrag()
}

const openCompleteSheet = () => {
  isCompleteSheetOpen.value = true
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const closeCompleteSheet = () => {
  isCompleteSheetOpen.value = false
  resetSheetDrag()
}

const sheetDragStyle = computed(() =>
  sheetDragOffsetY.value > 0 ? { transform: `translateY(${sheetDragOffsetY.value}px)` } : undefined,
)

const resetSheetDrag = () => {
  draggingSheet.value = null
  sheetDragStartY.value = null
  sheetDragOffsetY.value = 0
}

const handleSheetDrag = (event: PointerEvent) => {
  if (sheetDragStartY.value === null) return

  sheetDragOffsetY.value = Math.max(0, event.clientY - sheetDragStartY.value)
}

const endSheetDrag = () => {
  const shouldClose = sheetDragOffsetY.value > 80
  const targetSheet = draggingSheet.value

  window.removeEventListener('pointermove', handleSheetDrag)
  window.removeEventListener('pointerup', endSheetDrag)
  resetSheetDrag()

  if (!shouldClose) return

  if (targetSheet === 'info') {
    closeInfoSheet()
    return
  }

  closeCompleteSheet()
}

const startSheetDrag = (event: PointerEvent, sheet: 'info' | 'complete') => {
  draggingSheet.value = sheet
  sheetDragStartY.value = event.clientY
  sheetDragOffsetY.value = 0
  window.addEventListener('pointermove', handleSheetDrag)
  window.addEventListener('pointerup', endSheetDrag)
}
</script>

<template>
  <main class="min-h-[100dvh] bg-white">
    <Transition name="checklist-page" mode="out-in">
      <section
        v-if="isCompleteSheetOpen"
        key="complete"
        class="grid min-h-[calc(100dvh-var(--app-header-height))] content-center justify-items-center px-5 py-8 text-center"
        aria-labelledby="stage-complete-title"
      >
        <div class="checklist-complete-scene" aria-hidden="true">
          <img
            class="checklist-complete-star checklist-complete-star--left"
            :src="completeStarUrl"
            alt=""
          />
          <img
            class="checklist-complete-star checklist-complete-star--right"
            :src="completeStarUrl"
            alt=""
          />
          <img
            class="checklist-complete-pig"
            :src="childQuizCompletePigUrl"
            alt=""
          />
        </div>

        <h1
          id="stage-complete-title"
          class="mt-7 mb-0 text-[28px] leading-tight font-extrabold tracking-[-0.04em] text-[var(--color-text-primary)]"
        >
          체크리스트를 모두 완료했어요!
        </h1>
        <p class="mt-4 mb-0 text-[16px] leading-[1.65] text-[var(--color-text-secondary)]">
          {{ currentStage.ageRange }} 단계에서 필요한 준비를<br />
          하나씩 든든하게 마쳤어요.
        </p>

        <button
          class="mt-10 h-14 w-full rounded-[14px] border-0 bg-[#55C0F4] text-[17px] font-bold text-white"
          type="button"
          @click="router.push('/home')"
        >
          홈으로 이동
        </button>
        <button
          class="mt-3 h-12 border-0 bg-transparent px-5 text-[14px] font-bold text-[var(--color-text-secondary)]"
          type="button"
          @click="closeCompleteSheet"
        >
          체크리스트 다시 보기
        </button>
      </section>

      <div v-else key="checklist">
    <section class="px-5 pt-6 pb-5" aria-label="생애주기 로드맵">
      <div
        class="flex gap-2 overflow-x-auto rounded-[22px] border border-[#dce8f0] bg-[#f4f9fc] p-2 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
      >
        <button
          v-for="stage in lifecycleStages"
          :key="stage.id"
          class="shrink-0 rounded-[16px] border-0 px-3 py-2 text-[12px] font-bold whitespace-nowrap"
          :class="
            stage.id === currentStage.id
              ? 'bg-white text-[var(--color-selected-text)] shadow-[0_8px_18px_rgb(85_192_244_/_16%)]'
              : 'bg-transparent text-[var(--color-text-secondary)]'
          "
          type="button"
          @click="selectedStageId = stage.id"
        >
          {{ stage.ageRange }}
        </button>
      </div>
    </section>

    <section class="px-5 pb-5">
      <article
        class="rounded-[22px] border border-[#d5e8f8] bg-[#EBFAFF] px-5 py-5 shadow-[0_14px_34px_rgb(31_72_97_/_7%),inset_0_0_0_1px_rgb(255_255_255_/_70%)]"
      >
        <div class="min-w-0">
          <div class="flex items-start justify-between gap-3">
            <strong class="min-w-0 text-[22px] leading-[1.35] font-extrabold text-[#55C0F4]">
              {{ currentStage.title }}
            </strong>
            <span
              class="shrink-0 rounded-full bg-white px-3 py-1.5 text-[12px] leading-none font-bold text-[var(--color-selected-text)] shadow-[0_8px_18px_rgb(85_192_244_/_14%)]"
            >
              {{ currentStage.ageRange }}
            </span>
          </div>
          <span class="mt-2 block text-[13px] leading-[1.45] text-[var(--color-text-secondary)]">
            {{ currentStage.description }}
          </span>
        </div>

        <div class="mt-5">
          <div class="mb-2 flex items-center justify-between gap-3">
            <strong class="text-[17px] font-bold text-[var(--color-text-primary)]">
              {{ completedCount }} / {{ totalCount }} 완료
            </strong>
            <strong class="text-[17px] font-bold text-[#2BABE8]">
              {{ progressPercent }}%
            </strong>
          </div>
          <div class="h-2.5 overflow-hidden rounded-full bg-white shadow-[inset_0_0_0_1px_rgb(215_232_248_/_42%)]">
            <div
              class="h-full rounded-full bg-[#55C0F4] transition-[width] duration-300"
              :style="progressStyle"
            ></div>
          </div>
        </div>
      </article>
    </section>

    <section class="px-5 pb-7">
      <div class="flex items-center justify-between gap-4">
        <div>
          <h2 class="m-0 text-[24px] font-bold text-[var(--color-text-primary)]">
            체크리스트
          </h2>
        </div>
        <span class="shrink-0 text-[13px] text-[var(--color-text-secondary)]">
          전체 {{ totalCount }}개
        </span>
      </div>

      <ul
        class="mt-3 m-0 overflow-hidden rounded-[18px] border border-[var(--color-border)] bg-white p-0 shadow-[0_10px_24px_rgb(31_72_97_/_6%)]"
      >
        <li
          v-for="item in currentStageItems"
          :key="item.id"
          class="border-b border-[var(--color-border)] last:border-b-0"
        >
          <div
            class="grid min-h-[92px] w-full cursor-pointer grid-cols-[26px_minmax(0,1fr)_68px] items-center gap-3 py-0 pr-0 pl-4 text-left transition-colors active:bg-[#f7fbfe]"
            :class="isChecklistItemCompleted(item) ? 'bg-[#f6f8fa]' : 'bg-white'"
            @click="toggleChecklistItem(item)"
          >
            <button
              class="grid size-[22px] place-items-center rounded-full border"
              :class="
                isChecklistItemCompleted(item)
                  ? 'border-[#55C0F4] bg-[#55C0F4] text-white'
                  : 'border-[#cddbe6] bg-white text-transparent'
              "
              type="button"
              :aria-label="
                isChecklistItemCompleted(item) ? `${item.title} 완료 취소` : `${item.title} 완료`
              "
              @click.stop="toggleChecklistItem(item)"
            >
              <Check :size="14" :stroke-width="3" />
            </button>
            <span class="min-w-0 py-4">
              <strong
                class="block text-[14px] leading-[1.35] font-bold"
                :class="
                  isChecklistItemCompleted(item)
                    ? 'text-[#8f9aa3]'
                    : 'text-[var(--color-text-primary)]'
                "
              >
                {{ item.title }}
              </strong>
              <span
                class="mt-1 block text-[12px] leading-[1.45]"
                :class="
                  isChecklistItemCompleted(item)
                    ? 'text-[#a8b1b9]'
                    : 'text-[var(--color-text-secondary)]'
                "
              >
                {{ item.description }}
              </span>
            </span>
            <button
              v-if="hasChecklistAction(item)"
              class="grid h-full min-h-[92px] w-full shrink-0 place-items-center border-0 bg-transparent p-0 text-[#8A95A3] transition-colors active:bg-[#f7fbfe]"
              type="button"
              :aria-label="`${item.title} 바로가기`"
              @click.stop="openChecklistAction(item)"
            >
              <ChevronRight :size="22" :stroke-width="3" />
            </button>
            <span
              v-else
              class="h-full min-h-[92px]"
              aria-hidden="true"
            ></span>
          </div>
        </li>
      </ul>

      <button
        class="mt-5 h-12 w-full rounded-[16px] border border-[#55C0F4] bg-[#55C0F4] text-[15px] font-bold text-white"
        type="button"
        @click="openCompleteSheet"
      >
        완료 화면 보기
      </button>
    </section>

    <Teleport to="body">
      <Transition name="checklist-sheet">
        <div
          v-if="selectedInfoItem"
          class="fixed inset-0 z-[var(--z-index-overlay)] flex items-end justify-center bg-black/40"
          role="presentation"
          @click.self="closeInfoSheet"
        >
          <section
            class="checklist-sheet-panel w-full max-w-[var(--app-max-width)] rounded-t-[28px] bg-white px-5 pt-4 pb-[calc(16px+env(safe-area-inset-bottom))]"
            role="dialog"
            aria-modal="true"
            :aria-labelledby="`${selectedInfoItem.id}-title`"
            :style="sheetDragStyle"
          >
            <button
              class="mx-auto block h-7 w-20 touch-none border-0 bg-transparent p-0"
              type="button"
              aria-label="지원정보 닫기"
              @pointerdown="startSheetDrag($event, 'info')"
            >
              <span class="mx-auto block h-1.5 w-12 rounded-full bg-[#ccd6df]"></span>
            </button>
            <div class="mt-6">
              <h2
                :id="`${selectedInfoItem.id}-title`"
                class="m-0 text-[25px] leading-[1.22] font-bold text-[var(--color-text-primary)]"
              >
                {{ selectedInfoItem.infoTitle }}
              </h2>
            </div>

            <div class="mt-6">
              <strong class="text-[16px] font-bold text-[var(--color-text-primary)]">
                지금 확인해볼 지원
              </strong>
              <ul class="mt-3 m-0 list-none space-y-3 p-0">
                <li v-for="info in selectedInfoItem.infoItems" :key="info.title">
                  <button
                    class="grid min-h-[76px] w-full grid-cols-[44px_1fr_auto] items-center gap-3 rounded-[16px] border border-[var(--color-border)] bg-white px-4 text-left"
                    type="button"
                    @click="openInfoItem(info)"
                  >
                    <span
                      class="grid size-10 place-items-center rounded-full bg-[var(--color-selected-background)] text-[var(--color-selected-text)]"
                      aria-hidden="true"
                    >
                      <FileText :size="20" />
                    </span>
                    <span class="min-w-0">
                      <strong class="block text-[15px] font-bold text-[var(--color-text-primary)]">
                        {{ info.title }}
                      </strong>
                      <span
                        class="mt-1 block text-[12px] leading-[1.35] text-[var(--color-text-secondary)]"
                      >
                        {{ info.description }}
                      </span>
                    </span>
                    <ChevronRight :size="18" class="text-[#9cadba]" />
                  </button>
                </li>
              </ul>
            </div>

            <div class="mt-5">
              <button
                class="h-[52px] w-full rounded-[14px] border-0 bg-[#f1f5f8] text-[15px] font-bold text-[var(--color-text-secondary)]"
                type="button"
                @click="closeInfoSheet"
              >
                닫기
              </button>
            </div>
          </section>
        </div>
      </Transition>

      <Transition name="checklist-modal">
        <div
          v-if="selectedDetailInfo"
          class="fixed inset-0 z-[calc(var(--z-index-overlay)+1)] grid place-items-center bg-black/40 px-6"
          role="presentation"
          @click.self="closeDetailInfo"
        >
          <section
            class="w-full max-w-[320px] rounded-[22px] bg-white px-5 py-5 shadow-[0_18px_40px_rgb(15_23_42_/_18%)]"
            role="dialog"
            aria-modal="true"
            aria-labelledby="checklist-detail-title"
          >
            <h2
              id="checklist-detail-title"
              class="m-0 text-[19px] font-bold text-[var(--color-text-primary)]"
            >
              {{ selectedDetailInfo.title }}
            </h2>
            <p class="mt-3 mb-0 text-[14px] leading-[1.6] text-[var(--color-text-secondary)]">
              {{ selectedDetailInfo.detail ?? selectedDetailInfo.description }}
            </p>
            <button
              class="mt-5 h-12 w-full rounded-[14px] border-0 bg-[#55C0F4] text-[15px] font-bold text-white"
              type="button"
              @click="closeDetailInfo"
            >
              확인
            </button>
          </section>
        </div>
      </Transition>

      <Transition name="checklist-modal">
        <div
          v-if="pendingRouteItem"
          class="fixed inset-0 z-[var(--z-index-overlay)] grid place-items-center bg-black/40 px-6"
          role="presentation"
          @click.self="closeRouteConfirm"
        >
          <section
            class="w-full max-w-[320px] rounded-[22px] bg-white px-5 py-5 text-center shadow-[0_18px_40px_rgb(15_23_42_/_18%)]"
            role="dialog"
            aria-modal="true"
            aria-labelledby="route-confirm-title"
          >
            <h2
              id="route-confirm-title"
              class="m-0 text-[19px] font-bold text-[var(--color-text-primary)]"
            >
              페이지로 이동할까요?
            </h2>
            <p class="mt-3 mb-0 text-[14px] leading-[1.5] text-[var(--color-text-secondary)]">
              {{ pendingRouteItem.title }} 관련 화면으로 이동합니다.
            </p>
            <div class="mt-5 grid grid-cols-2 gap-3">
              <button
                class="h-12 rounded-[14px] border-0 bg-[#f1f5f8] text-[15px] font-bold text-[var(--color-text-secondary)]"
                type="button"
                @click="closeRouteConfirm"
              >
                닫기
              </button>
              <button
                class="h-12 rounded-[14px] border-0 bg-[#55C0F4] text-[15px] font-bold text-white"
                type="button"
                @click="confirmRouteNavigation"
              >
                확인
              </button>
            </div>
          </section>
        </div>
      </Transition>

    </Teleport>
      </div>
    </Transition>
  </main>
</template>

<style scoped>
.checklist-complete-scene {
  position: relative;
  width: 240px;
  max-width: 72vw;
  aspect-ratio: 1 / 1;
}

.checklist-complete-pig {
  position: absolute;
  z-index: 1;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  user-select: none;
}

.checklist-complete-star {
  position: absolute;
  z-index: 2;
  width: 38px;
  object-fit: contain;
  pointer-events: none;
}

.checklist-complete-star--left {
  bottom: 18%;
  left: 1%;
  rotate: -10deg;
}

.checklist-complete-star--right {
  top: 14%;
  right: -3%;
  width: 31px;
  rotate: 12deg;
}

.checklist-page-enter-active,
.checklist-page-leave-active {
  transition:
    opacity 200ms ease,
    transform 240ms cubic-bezier(0.22, 1, 0.36, 1);
}

.checklist-page-enter-from {
  opacity: 0;
  transform: translateX(18px);
}

.checklist-page-leave-to {
  opacity: 0;
  transform: translateX(-12px);
}

@media (prefers-reduced-motion: no-preference) {
  .checklist-complete-pig {
    animation: checklist-complete-arrive 680ms cubic-bezier(0.16, 1, 0.3, 1) both;
  }

  .checklist-complete-star {
    animation: checklist-complete-star-twinkle 2.2s ease-in-out 520ms infinite;
  }

  .checklist-complete-star--right {
    animation-delay: 1.05s;
    animation-duration: 2.55s;
  }
}

@keyframes checklist-complete-arrive {
  0% { opacity: 0; transform: translateY(12px) scale(0.82); }
  68% { opacity: 1; transform: translateY(-2px) scale(1.04); }
  100% { opacity: 1; transform: none; }
}

@keyframes checklist-complete-star-twinkle {
  0%,
  100% { opacity: 0.58; transform: translateY(2px) scale(0.88); }
  50% { opacity: 1; transform: translateY(-4px) scale(1.08); }
}

.checklist-sheet-enter-active,
.checklist-sheet-leave-active {
  transition: opacity 220ms ease;
}

.checklist-sheet-enter-active .checklist-sheet-panel,
.checklist-sheet-leave-active .checklist-sheet-panel {
  transition: transform 280ms cubic-bezier(0.22, 1, 0.36, 1);
}

.checklist-sheet-enter-from,
.checklist-sheet-leave-to {
  opacity: 0;
}

.checklist-sheet-enter-from .checklist-sheet-panel,
.checklist-sheet-leave-to .checklist-sheet-panel {
  transform: translateY(100%);
}

.checklist-modal-enter-active,
.checklist-modal-leave-active {
  transition: opacity 180ms ease;
}

.checklist-modal-enter-active > section,
.checklist-modal-leave-active > section {
  transition: transform 180ms ease;
}

.checklist-modal-enter-from,
.checklist-modal-leave-to {
  opacity: 0;
}

.checklist-modal-enter-from > section,
.checklist-modal-leave-to > section {
  transform: translateY(8px) scale(0.98);
}
</style>
