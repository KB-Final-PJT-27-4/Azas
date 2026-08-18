<script setup lang="ts">
import { ChevronDown, ChevronRight, X } from 'lucide-vue-next'
import { ref } from 'vue'
import questionPigImage from '@/assets/images/mypage/question_pig.png'

interface FrequentlyAskedQuestion {
  id: string
  question: string
  answer: string
}

interface QuickGuide {
  title: string
  description: string
  steps: string[]
}

const openQuestionId = ref<string | null>(null)
const selectedGuide = ref<QuickGuide | null>(null)
const sheetDragOffset = ref(0)
const isSheetDragging = ref(false)
let sheetDragStartY = 0

const guides: QuickGuide[] = [
  {
    title: '계좌 연결하기',
    description: '아이의 자산을 한눈에 확인해요',
    steps: [
      '마이페이지에서 계좌정보 관리를 선택해주세요.',
      '계좌 추가하기를 누르고 연결할 은행을 선택해주세요.',
      '본인 인증을 완료하면 계좌와 잔액을 확인할 수 있어요.',
    ],
  },
  {
    title: '저축 목표 만들기',
    description: '목표 금액과 기간을 설정해요',
    steps: [
      '목표 메뉴에서 새로 만들 목표를 선택해주세요.',
      '필요한 목표 금액과 달성 시기를 입력해주세요.',
      '계산된 월 저축액을 확인하고 목표를 저장해주세요.',
    ],
  },
  {
    title: '타임캡슐 남기기',
    description: '소중한 순간과 마음을 기록해요',
    steps: [
      '타임캡슐 메뉴에서 새 타임캡슐 만들기를 눌러주세요.',
      '아이에게 남길 메시지와 사진을 기록해주세요.',
      '공개할 날짜를 정하면 소중하게 보관해드려요.',
    ],
  },
]

const questions: FrequentlyAskedQuestion[] = [
  {
    id: 'account',
    question: '계좌는 어떻게 추가하나요?',
    answer:
      '마이페이지의 계좌정보 관리에서 ‘계좌 추가하기’를 눌러주세요. 연결이 완료되면 대표 계좌도 설정할 수 있어요.',
  },
  {
    id: 'goal',
    question: '저축 목표는 언제든 수정할 수 있나요?',
    answer: '네. 마이페이지의 목표 관리에서 목표 금액과 달성 시기를 언제든 수정할 수 있어요.',
  },
  {
    id: 'guardian',
    question: '공동 보호자는 무엇을 할 수 있나요?',
    answer:
      '초대받은 공동 보호자는 아이의 자산과 목표를 함께 확인하고 가족의 금융 기록을 관리할 수 있어요.',
  },
  {
    id: 'security',
    question: '아이의 금융 정보는 안전하게 보관되나요?',
    answer:
      '서비스는 꼭 필요한 정보만 사용하며, 민감한 계좌 정보는 안전한 방식으로 보호하고 있어요.',
  },
]

const toggleQuestion = (questionId: string) => {
  openQuestionId.value = openQuestionId.value === questionId ? null : questionId
}

const startSheetDrag = (event: PointerEvent) => {
  if (event.pointerType === 'mouse' && event.button !== 0) return

  sheetDragStartY = event.clientY
  isSheetDragging.value = true
  ;(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId)
}

const moveSheetDrag = (event: PointerEvent) => {
  if (!isSheetDragging.value) return
  sheetDragOffset.value = Math.max(0, event.clientY - sheetDragStartY)
}

const endSheetDrag = () => {
  if (!isSheetDragging.value) return

  isSheetDragging.value = false
  if (sheetDragOffset.value >= 80) {
    selectedGuide.value = null
    window.setTimeout(() => {
      sheetDragOffset.value = 0
    }, 340)
    return
  }

  sheetDragOffset.value = 0
}

</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-5 pb-[calc(16px+env(safe-area-inset-bottom))] text-[var(--color-text-primary)]"
  >
    <section
      class="relative overflow-hidden rounded-[24px] border border-[#cfeaf6] bg-[#eefaff] px-5 py-6"
      aria-labelledby="guide-title"
    >
      <span class="text-xs font-bold text-[var(--color-text-primary)]">
        우리 <span class="text-[#f28faa]">아</span>이
        <span class="text-[#f28faa]">자</span>산관리 서비<span class="text-[#f28faa]">스</span>
      </span>
      <h1 id="guide-title" class="mt-2 text-[25px] font-extrabold tracking-[-0.04em]">
        무엇을 도와드릴까요?
      </h1>
      <p class="relative z-10 mt-2 max-w-[230px] text-[13px] leading-6 text-[var(--color-text-secondary)]">
        우리 아이의 자산 관리, 어렵지 않아요.<br />자주 찾는 기능부터 차근차근 알려드릴게요.
      </p>
      <img
        :src="questionPigImage"
        class="pointer-events-none absolute -right-3 top-1/2 w-[132px] -translate-y-1/2 object-contain"
        alt=""
        aria-hidden="true"
      />
    </section>

    <section class="mt-7" aria-labelledby="quick-guide-title">
      <h2 id="quick-guide-title" class="text-[19px] font-extrabold tracking-[-0.02em]">
        빠른 이용 가이드
      </h2>
      <p class="mt-1 text-xs text-[var(--color-text-secondary)]">필요한 기능을 순서대로 알려드려요.</p>

      <div class="mt-4 grid gap-3">
        <button
          v-for="(guide, index) in guides"
          :key="guide.title"
          class="group flex min-h-[76px] items-center gap-4 rounded-[20px] border border-[#d9e5eb] bg-white px-4 py-3 text-left transition-colors active:bg-[#f5fbfe]"
          type="button"
          @click="selectedGuide = guide"
        >
          <span
            class="grid size-10 shrink-0 place-items-center rounded-[14px] bg-[#eaf8fe] text-sm font-extrabold text-[var(--color-brand-primary)]"
          >
            {{ index + 1 }}
          </span>
          <span class="min-w-0 flex-1">
            <strong class="block text-[15px] font-bold">{{ guide.title }}</strong>
            <span class="mt-1 block text-xs text-[var(--color-text-secondary)]">
              {{ guide.description }}
            </span>
          </span>
          <ChevronRight
            :size="19"
            class="shrink-0 text-[#9aabb5] transition-transform group-active:translate-x-0.5"
            aria-hidden="true"
          />
        </button>
      </div>
    </section>

    <section class="mt-6" aria-labelledby="faq-title">
      <h2 id="faq-title" class="text-[19px] font-extrabold tracking-[-0.02em]">자주 묻는 질문</h2>
      <p class="mt-1 text-xs text-[var(--color-text-secondary)]">궁금한 질문을 눌러 확인해보세요.</p>

      <div class="mt-4 overflow-hidden rounded-[20px] border border-[#d9e5eb] bg-white">
        <article
          v-for="(item, index) in questions"
          :key="item.id"
          :class="index > 0 ? 'border-t border-[#edf1f3]' : ''"
        >
          <button
            class="flex min-h-[62px] w-full items-center justify-between gap-4 bg-white px-4 py-3 text-left transition-colors active:bg-[#f7fbfd]"
            type="button"
            :aria-expanded="openQuestionId === item.id"
            :aria-controls="`answer-${item.id}`"
            @click="toggleQuestion(item.id)"
          >
            <span class="text-[14px] font-bold">{{ item.question }}</span>
            <ChevronDown
              :size="19"
              class="shrink-0 text-[#8497a3] transition-all duration-300 ease-out"
              :class="openQuestionId === item.id ? 'rotate-180 text-[var(--color-brand-primary)]' : ''"
            />
          </button>
          <div
            :id="`answer-${item.id}`"
            class="grid bg-[#f4fbfe] transition-[grid-template-rows,opacity] duration-300 ease-[cubic-bezier(0.22,1,0.36,1)]"
            :class="
              openQuestionId === item.id
                ? 'grid-rows-[1fr] opacity-100'
                : 'grid-rows-[0fr] opacity-0'
            "
          >
            <div class="overflow-hidden">
              <p class="border-t border-[#e1f1f7] px-4 py-4 text-[13px] leading-6 text-[var(--color-text-secondary)]">
                {{ item.answer }}
              </p>
            </div>
          </div>
        </article>
      </div>
    </section>
  </main>

  <Transition name="guide-sheet">
    <div
      v-if="selectedGuide"
      class="guide-sheet__backdrop fixed inset-y-0 left-1/2 z-[var(--z-index-overlay)] w-full max-w-[var(--app-max-width)] -translate-x-1/2 bg-black/35"
      role="presentation"
      @click.self="selectedGuide = null"
    >
      <section
        class="guide-sheet__panel absolute right-0 bottom-0 left-0 rounded-t-[28px] bg-white px-5 pt-3 pb-[calc(24px+env(safe-area-inset-bottom))]"
        :class="{ 'is-dragging': isSheetDragging }"
        :style="{ '--sheet-drag-y': `${sheetDragOffset}px` }"
        role="dialog"
        aria-modal="true"
        aria-labelledby="quick-guide-modal-title"
      >
        <div
          class="sheet-drag-handle -mx-5 -mt-3 flex h-11 touch-none cursor-grab items-center justify-center active:cursor-grabbing"
          role="button"
          tabindex="0"
          aria-label="아래로 밀어 닫기"
          @pointerdown="startSheetDrag"
          @pointermove="moveSheetDrag"
          @pointerup="endSheetDrag"
          @pointercancel="endSheetDrag"
        >
          <span class="h-1.5 w-12 rounded-full bg-[#d8e1e6]" aria-hidden="true"></span>
        </div>
        <header class="flex items-start justify-between gap-4">
          <div>
            <h2 id="quick-guide-modal-title" class="text-xl font-extrabold">
              {{ selectedGuide.title }}
            </h2>
            <p class="mt-1 text-sm text-[var(--color-text-secondary)]">
              {{ selectedGuide.description }}
            </p>
          </div>
          <button
            class="grid size-9 shrink-0 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[#f2f5f6]"
            type="button"
            aria-label="닫기"
            @click="selectedGuide = null"
          >
            <X :size="21" />
          </button>
        </header>

        <ol class="mt-6 grid gap-3">
          <li
            v-for="(step, index) in selectedGuide.steps"
            :key="step"
            class="flex items-center gap-3 rounded-2xl bg-[#f4f8fa] p-4"
          >
            <span
              class="grid size-7 shrink-0 place-items-center rounded-full bg-[var(--color-brand-primary)] text-xs font-bold text-[var(--color-text-inverse)]"
            >
              {{ index + 1 }}
            </span>
            <p class="text-sm leading-6">{{ step }}</p>
          </li>
        </ol>

        <button
          class="mt-6 h-14 w-full rounded-2xl bg-[var(--color-brand-primary)] text-sm font-bold text-[var(--color-text-inverse)] active:bg-[var(--color-brand-primary-pressed)]"
          type="button"
          @click="selectedGuide = null"
        >
          확인했어요
        </button>
      </section>
    </div>
  </Transition>
</template>

<style scoped>
.guide-sheet__panel {
  transform: translateY(var(--sheet-drag-y, 0));
  transition: transform 220ms cubic-bezier(0.22, 1, 0.36, 1);
}

.guide-sheet__panel.is-dragging {
  transition: none;
}

.guide-sheet-enter-active,
.guide-sheet-leave-active {
  transition: opacity 220ms ease;
}

.guide-sheet-enter-active .guide-sheet__panel,
.guide-sheet-leave-active .guide-sheet__panel {
  transition: transform 320ms cubic-bezier(0.22, 1, 0.36, 1);
}

.guide-sheet-enter-from,
.guide-sheet-leave-to {
  opacity: 0;
}

.guide-sheet-enter-from .guide-sheet__panel,
.guide-sheet-leave-to .guide-sheet__panel {
  transform: translateY(100%);
}

@media (prefers-reduced-motion: reduce) {
  .guide-sheet-enter-active,
  .guide-sheet-leave-active,
  .guide-sheet-enter-active .guide-sheet__panel,
  .guide-sheet-leave-active .guide-sheet__panel {
    transition-duration: 1ms;
  }
}
</style>
