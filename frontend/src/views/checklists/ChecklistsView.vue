<script setup lang="ts">
import confetti from 'canvas-confetti'
import { computed, nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Check, ChevronRight, FileText } from 'lucide-vue-next'

import checklistTrophyUrl from '@/assets/images/checklists/trophy.png'
import {
  checklistItems,
  currentChildLifecycle,
  lifecycleCategories,
  lifecycleStages,
  type ChecklistItem,
  type LifecycleCategory,
} from '@/mocks/lifecycleChecklist'

const router = useRouter()

const selectedInfoItem = ref<ChecklistItem | null>(null)
const isCompleteSheetOpen = ref(false)
const selectedStageId = ref(currentChildLifecycle.childStatus)
const checkedItemIds = ref(
  new Set(checklistItems.filter((item) => item.completed).map((item) => item.id)),
)
const draggingSheet = ref<'info' | 'complete' | null>(null)
const sheetDragStartY = ref<number | null>(null)
const sheetDragOffsetY = ref(0)

const currentStage = computed(
  () => lifecycleStages.find((stage) => stage.id === selectedStageId.value) ?? lifecycleStages[0]!,
)

const currentStageIndex = computed(() =>
  lifecycleStages.findIndex((stage) => stage.id === currentStage.value.id),
)
const nextStage = computed(() => lifecycleStages[currentStageIndex.value + 1])

const currentStageItems = computed(() =>
  checklistItems.filter((item) => item.stageId === currentStage.value.id),
)

const completedCount = computed(
  () => currentStageItems.value.filter((item) => checkedItemIds.value.has(item.id)).length,
)
const totalCount = computed(() => currentStageItems.value.length)
const progressPercent = computed(() =>
  totalCount.value === 0 ? 0 : Math.round((completedCount.value / totalCount.value) * 100),
)
const progressStyle = computed(() => ({ width: `${progressPercent.value}%` }))

const categoryLabelMap = computed(
  () =>
    new Map(
      lifecycleCategories
        .filter((category) => category.id !== 'all')
        .map((category) => [category.id, category.label]),
    ),
)

const categoryToneMap: Record<LifecycleCategory, string> = {
  service: 'bg-[#EBFAFF] text-[#2BABE8]',
  asset: 'bg-[#F6F8FA] text-[#6E7A8A]',
  education: 'bg-[#FFF7D7] text-[#8A6A00]',
  support: 'bg-[#FFF7D7] text-[#8A6A00]',
  allowance: 'bg-[#FDECA7] text-[#8A6A00]',
}

const getCategoryTone = (category: LifecycleCategory) => categoryToneMap[category]

const isChecklistItemCompleted = (item: ChecklistItem) => checkedItemIds.value.has(item.id)

const hasChecklistAction = (item: ChecklistItem) => item.actionType === 'info' || Boolean(item.route)

const infoFullViewLabel = computed(() => {
  if (!selectedInfoItem.value) return '전체 보기'

  const categoryLabel = categoryLabelMap.value.get(selectedInfoItem.value.category)

  return `${categoryLabel ?? '정보'} 전체 보기`
})

const toggleChecklistItem = (item: ChecklistItem) => {
  const nextCheckedItemIds = new Set(checkedItemIds.value)

  if (nextCheckedItemIds.has(item.id)) {
    nextCheckedItemIds.delete(item.id)
  } else {
    nextCheckedItemIds.add(item.id)
  }

  checkedItemIds.value = nextCheckedItemIds
}

const openChecklistAction = (item: ChecklistItem) => {
  if (item.actionType === 'info') {
    selectedInfoItem.value = item
    return
  }

  if (item.route) {
    router.push(item.route)
  }
}

const closeInfoSheet = () => {
  selectedInfoItem.value = null
  resetSheetDrag()
}

const openInfoFullView = () => {
  if (!selectedInfoItem.value) return

  if (selectedInfoItem.value.category === 'education') {
    router.push('/checklists?category=education')
    selectedInfoItem.value = null
    return
  }

  closeInfoSheet()
}

const openCompleteSheet = () => {
  isCompleteSheetOpen.value = true
  nextTick(() => {
    confetti({
      particleCount: 90,
      spread: 72,
      origin: { y: 0.72 },
      colors: ['#55C0F4', '#FFF5D2', '#FFD86B', '#9CD7F6'],
    })
  })
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
    <section class="px-5 pt-6 pb-6" aria-label="생애주기 로드맵">
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

    <section class="px-5 pb-7">
      <article
        class="rounded-[22px] border border-[#d5e8f8] bg-[#EBFAFF] px-5 py-5 shadow-[0_14px_34px_rgb(31_72_97_/_7%),inset_0_0_0_1px_rgb(255_255_255_/_70%)]"
      >
        <div class="grid grid-cols-[1fr_auto] items-center gap-4">
          <div class="min-w-0">
            <span class="block text-[12px] font-bold text-[var(--color-text-secondary)]">
              현재 단계
            </span>
            <strong class="mt-1 block text-[18px] leading-[1.35] font-bold text-[#55C0F4]">
              {{ currentStage.ageRange }} · {{ currentStage.title }}
            </strong>
            <span class="mt-2 block text-[13px] leading-[1.45] text-[var(--color-text-secondary)]">
              {{ currentStage.description }}
            </span>
          </div>
          <strong class="self-end text-[22px] font-bold text-[#2BABE8]">
            {{ progressPercent }}%
          </strong>
        </div>

        <div class="mt-4 grid grid-cols-[auto_1fr] items-center gap-4">
          <strong class="text-[17px] font-bold text-[var(--color-text-primary)]">
            {{ completedCount }} / {{ totalCount }} 완료
          </strong>
          <div class="h-2 overflow-hidden rounded-full bg-[#e9eef3]">
            <div
              class="h-full rounded-full bg-[#55C0F4] transition-[width] duration-300"
              :style="progressStyle"
            ></div>
          </div>
        </div>
      </article>
    </section>

    <section class="px-5 pb-7">
      <div class="flex items-end justify-between gap-4">
        <div>
          <h2 class="m-0 text-[24px] font-bold text-[var(--color-text-primary)]">
            체크리스트
          </h2>
          <p class="mt-1 mb-0 text-[13px] text-[var(--color-text-secondary)]">
            지금 시기에 필요한 준비를 확인해보세요.
          </p>
        </div>
        <span class="shrink-0 text-[13px] text-[var(--color-text-secondary)]">
          전체 {{ totalCount }}개
        </span>
      </div>

      <ul
        class="mt-6 m-0 overflow-hidden rounded-[18px] border border-[var(--color-border)] bg-white p-0 shadow-[0_10px_24px_rgb(31_72_97_/_6%)]"
      >
        <li
          v-for="item in currentStageItems"
          :key="item.id"
          class="border-b border-[var(--color-border)] last:border-b-0"
        >
          <div
            class="grid w-full cursor-pointer grid-cols-[26px_1fr_auto] items-center gap-3 bg-white px-4 py-4 text-left transition-colors active:bg-[#f7fbfe]"
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
            <span class="min-w-0">
              <span
                class="mb-1.5 inline-flex rounded-full px-2 py-0.5 text-[10px] font-bold"
                :class="getCategoryTone(item.category)"
              >
                {{ categoryLabelMap.get(item.category) }}
              </span>
              <strong
                class="block text-[14px] leading-[1.35] font-bold text-[var(--color-text-primary)]"
              >
                {{ item.title }}
              </strong>
              <span class="mt-0.5 block text-[12px] leading-[1.45] text-[var(--color-text-secondary)]">
                {{ item.description }}
              </span>
            </span>
            <button
              v-if="hasChecklistAction(item)"
              class="grid size-8 shrink-0 place-items-center rounded-full border-0 bg-transparent p-0 text-[#8A95A3] transition-colors active:bg-[#f7fbfe]"
              type="button"
              :aria-label="`${item.title} 바로가기`"
              @click.stop="openChecklistAction(item)"
            >
              <ChevronRight :size="22" :stroke-width="3" />
            </button>
            <span v-else class="size-[21px]" aria-hidden="true"></span>
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
              <span
                class="inline-flex rounded-full px-3 py-1 text-[12px] font-bold"
                :class="getCategoryTone(selectedInfoItem.category)"
              >
                {{ categoryLabelMap.get(selectedInfoItem.category) }}
              </span>
              <h2
                :id="`${selectedInfoItem.id}-title`"
                class="mt-3 mb-0 text-[25px] leading-[1.22] font-bold text-[var(--color-text-primary)]"
              >
                {{ selectedInfoItem.infoTitle }}
              </h2>
              <p class="mt-3 mb-0 text-[14px] leading-[1.55] text-[var(--color-text-secondary)]">
                {{ selectedInfoItem.infoDescription }}
              </p>
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
                      <span class="mt-1 block text-[12px] font-bold text-[var(--color-selected-text)]">
                        {{ info.actionLabel }}
                      </span>
                    </span>
                    <ChevronRight :size="18" class="text-[#9cadba]" />
                  </button>
                </li>
              </ul>
            </div>

            <p
              v-if="selectedInfoItem.infoNotice"
              class="mt-4 rounded-[14px] bg-[#f6f8fa] px-4 py-3 text-[12px] leading-[1.5] text-[var(--color-text-secondary)]"
            >
              {{ selectedInfoItem.infoNotice }}
            </p>

            <div class="mt-5 grid grid-cols-[0.9fr_1.4fr] gap-3">
              <button
                class="h-[52px] rounded-[14px] border-0 bg-[#f1f5f8] text-[15px] font-bold text-[var(--color-text-secondary)]"
                type="button"
                @click="closeInfoSheet"
              >
                닫기
              </button>
              <button
                class="relative h-[52px] rounded-[14px] border-0 bg-[#55C0F4] text-[15px] font-bold text-transparent"
                type="button"
                :aria-label="infoFullViewLabel"
                @click="openInfoFullView"
              >
                <span class="absolute inset-0 grid place-items-center text-white">
                  {{ infoFullViewLabel }}
                </span>
                지원정보 전체 보기
              </button>
            </div>
          </section>
        </div>
      </Transition>

      <Transition name="checklist-sheet">
        <div
          v-if="isCompleteSheetOpen"
          class="fixed inset-0 z-[var(--z-index-overlay)] flex items-end justify-center bg-black/40"
          role="presentation"
          @click.self="closeCompleteSheet"
        >
          <section
            class="checklist-sheet-panel w-full max-w-[var(--app-max-width)] rounded-t-[28px] bg-white px-5 pt-5 pb-[calc(16px+env(safe-area-inset-bottom))] text-center"
            role="dialog"
            aria-modal="true"
            aria-labelledby="stage-complete-title"
            :style="sheetDragStyle"
          >
            <button
              class="mx-auto block h-7 w-20 touch-none border-0 bg-transparent p-0"
              type="button"
              aria-label="완료 안내 닫기"
              @pointerdown="startSheetDrag($event, 'complete')"
            >
              <span class="mx-auto block h-1.5 w-12 rounded-full bg-[#ccd6df]"></span>
            </button>
            <div class="mt-10 grid place-items-center" aria-hidden="true">
              <img
                class="w-[min(230px,68vw)] select-none object-contain drop-shadow-[0_18px_28px_rgb(255_216_107_/_26%)]"
                :src="checklistTrophyUrl"
                alt=""
              />
            </div>
            <h2
              id="stage-complete-title"
              class="mt-8 mb-0 text-[28px] font-bold text-[var(--color-text-primary)]"
            >
              축하해요!
            </h2>
            <p class="mt-4 mb-0 text-[19px] leading-[1.45] font-bold text-[var(--color-text-primary)]">
              {{ currentStage.ageRange }} 단계 체크리스트를<br />
              모두 완료했어요
            </p>
            <p class="mt-5 mb-0 text-[14px] leading-[1.55] text-[var(--color-text-secondary)]">
              우리 아이의 든든한 미래를 위해<br />
              필요한 준비를 하나씩 잘 마쳤어요.
            </p>

            <article
              v-if="nextStage"
              class="mt-8 grid grid-cols-[1fr_auto] items-center gap-3 rounded-[18px] border border-[var(--color-border)] bg-white px-5 py-4 text-left"
            >
              <div>
                <span class="text-[13px] font-bold text-[var(--color-text-secondary)]">
                  다음 단계 미리보기
                </span>
                <strong
                  class="mt-1 block text-[19px] leading-[1.35] font-bold text-[#55C0F4]"
                >
                  {{ nextStage.ageRange }} · {{ nextStage.title }}
                </strong>
                <span class="mt-1 block text-[13px] leading-[1.4] text-[var(--color-text-secondary)]">
                  {{ nextStage.description }}
                </span>
              </div>
              <ChevronRight :size="22" class="text-[#9cadba]" />
            </article>

            <div class="mt-6 grid grid-cols-1 gap-3">
              <button
                class="h-[52px] rounded-[14px] border-0 bg-[#55C0F4] text-[15px] font-bold text-white"
                type="button"
                @click="closeCompleteSheet"
              >
                다음 단계 미리보기
              </button>
              <button
                class="h-12 rounded-[14px] border border-[var(--color-border)] bg-white text-[15px] font-bold text-[var(--color-text-secondary)]"
                type="button"
                @click="router.push('/home')"
              >
                홈으로 이동
              </button>
            </div>
          </section>
        </div>
      </Transition>
    </Teleport>
  </main>
</template>

<style scoped>
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
</style>

