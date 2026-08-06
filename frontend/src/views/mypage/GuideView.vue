<script setup lang="ts">
import { ChevronDown, X } from 'lucide-vue-next'
import { ref } from 'vue'

import { useToast } from '@/composables/useToast'

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

const { showToast } = useToast()
const openQuestionId = ref<string | null>('account')
const selectedGuide = ref<QuickGuide | null>(null)

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

const requestHelp = () => {
  showToast('문의가 접수되었습니다. 빠르게 도와드릴게요.', 'success')
}
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-[var(--color-surface)] px-5 pt-7 pb-10 text-[var(--color-text-primary)]"
  >
    <section
      class="relative overflow-hidden rounded-[24px] bg-[var(--color-selected-background)] p-6"
      aria-labelledby="guide-title"
    >
      <h1 id="guide-title" class="text-[26px] font-bold tracking-[-0.04em]">
        무엇을 도와드릴까요?
      </h1>
      <p class="mt-2 text-sm leading-6 text-[var(--color-text-secondary)]">
        우리 아이의 자산 관리, 어렵지 않아요.<br />자주 찾는 기능부터 차근차근 알려드릴게요.
      </p>
    </section>

    <section class="mt-8" aria-labelledby="quick-guide-title">
      <div class="flex items-center gap-2">
        <h2 id="quick-guide-title" class="text-lg font-bold">빠른 이용 가이드</h2>
      </div>

      <div class="mt-4 grid gap-3">
        <button
          v-for="guide in guides"
          :key="guide.title"
          class="flex items-center gap-4 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4 text-left shadow-sm transition-colors active:bg-[var(--color-surface-muted)]"
          type="button"
          @click="selectedGuide = guide"
        >
          <div class="min-w-0 flex-1">
            <h3 class="text-base font-bold">{{ guide.title }}</h3>
            <p class="mt-1 text-xs text-[var(--color-text-secondary)]">
              {{ guide.description }}
            </p>
          </div>
        </button>
      </div>
    </section>

    <section class="mt-8" aria-labelledby="faq-title">
      <div class="flex items-center gap-2">
        <h2 id="faq-title" class="text-lg font-bold">자주 묻는 질문</h2>
      </div>

      <div class="mt-4 overflow-hidden rounded-2xl border border-[var(--color-border)]">
        <article
          v-for="(item, index) in questions"
          :key="item.id"
          :class="index > 0 ? 'border-t border-[var(--color-border)]' : ''"
        >
          <button
            class="flex min-h-14 w-full items-center justify-between gap-4 bg-[var(--color-surface)] px-4 py-3 text-left"
            type="button"
            :aria-expanded="openQuestionId === item.id"
            :aria-controls="`answer-${item.id}`"
            @click="toggleQuestion(item.id)"
          >
            <span class="text-sm font-semibold">{{ item.question }}</span>
            <ChevronDown
              :size="19"
              class="shrink-0 text-[var(--color-text-secondary)] transition-transform"
              :class="openQuestionId === item.id ? 'rotate-180' : ''"
            />
          </button>
          <div
            v-if="openQuestionId === item.id"
            :id="`answer-${item.id}`"
            class="bg-[var(--color-surface-muted)] px-4 py-4 text-sm leading-6 text-[var(--color-text-secondary)]"
          >
            {{ item.answer }}
          </div>
        </article>
      </div>
    </section>

    <section class="mt-8 rounded-2xl bg-[var(--color-brand-secondary)] p-5 text-center">
      <h2 class="text-base font-bold">원하는 답변을 찾지 못했나요?</h2>
      <p class="mt-1 text-xs text-[var(--color-text-secondary)]">
        궁금한 내용을 남겨주시면 친절하게 안내해드릴게요.
      </p>
      <button
        class="mt-4 h-11 w-full rounded-xl bg-[var(--color-brand-primary)] text-sm font-bold text-[var(--color-text-inverse)] active:bg-[var(--color-brand-primary-pressed)]"
        type="button"
        @click="requestHelp"
      >
        1:1 문의하기
      </button>
    </section>
  </main>

  <div
    v-if="selectedGuide"
    class="fixed inset-0 z-[var(--z-index-overlay)] grid place-items-center bg-black/40 px-6"
    role="presentation"
    @click.self="selectedGuide = null"
  >
    <section
      class="w-full max-w-[380px] rounded-2xl bg-[var(--color-surface)] p-6 shadow-lg"
      role="dialog"
      aria-modal="true"
      aria-labelledby="quick-guide-modal-title"
    >
      <header class="flex items-start justify-between gap-4">
        <div>
          <h2 id="quick-guide-modal-title" class="text-xl font-bold">
            {{ selectedGuide.title }}
          </h2>
          <p class="mt-1 text-sm text-[var(--color-text-secondary)]">
            {{ selectedGuide.description }}
          </p>
        </div>
        <button
          class="grid size-9 shrink-0 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[var(--color-surface-muted)]"
          type="button"
          aria-label="닫기"
          @click="selectedGuide = null"
        >
          <X :size="21" />
        </button>
      </header>

      <ol class="mt-6 grid gap-4">
        <li
          v-for="(step, index) in selectedGuide.steps"
          :key="step"
          class="flex items-center gap-3 rounded-xl bg-[var(--color-surface-muted)] p-4"
        >
          <span
            class="grid size-7 shrink-0 place-items-center rounded-full bg-[var(--color-brand-primary)] text-xs font-bold text-[var(--color-text-inverse)]"
          >
            {{ index + 1 }}
          </span>
          <p class="pt-0.5 text-sm leading-6">{{ step }}</p>
        </li>
      </ol>

      <button
        class="mt-6 h-12 w-full rounded-xl bg-[var(--color-brand-primary)] text-sm font-bold text-[var(--color-text-inverse)] active:bg-[var(--color-brand-primary-pressed)]"
        type="button"
        @click="selectedGuide = null"
      >
        확인했어요
      </button>
    </section>
  </div>
</template>
