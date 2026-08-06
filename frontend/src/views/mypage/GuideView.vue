<script setup lang="ts">
import {
  BookOpen,
  ChevronDown,
  Clock3,
  MessageCircleQuestion,
  Target,
  UsersRound,
  WalletCards,
} from 'lucide-vue-next'
import { ref } from 'vue'

import { useToast } from '@/composables/useToast'

interface FrequentlyAskedQuestion {
  id: string
  question: string
  answer: string
}

const { showToast } = useToast()
const openQuestionId = ref<string | null>('account')

const guides = [
  {
    title: '계좌 연결하기',
    description: '아이의 자산을 한눈에 확인해요',
    icon: WalletCards,
  },
  {
    title: '저축 목표 만들기',
    description: '목표 금액과 기간을 설정해요',
    icon: Target,
  },
  {
    title: '타임캡슐 남기기',
    description: '소중한 순간과 마음을 기록해요',
    icon: Clock3,
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
      <div
        class="absolute -top-8 -right-8 size-32 rounded-full bg-[var(--color-brand-primary)] opacity-10"
        aria-hidden="true"
      ></div>
      <div
        class="mb-5 grid size-12 place-items-center rounded-2xl bg-[var(--color-surface)] text-[var(--color-selected-text)] shadow-sm"
        aria-hidden="true"
      >
        <BookOpen :size="25" />
      </div>
      <h1 id="guide-title" class="text-[26px] font-bold tracking-[-0.04em]">
        무엇을 도와드릴까요?
      </h1>
      <p class="mt-2 text-sm leading-6 text-[var(--color-text-secondary)]">
        우리 아이의 자산 관리, 어렵지 않아요.<br />자주 찾는 기능부터 차근차근 알려드릴게요.
      </p>
    </section>

    <section class="mt-8" aria-labelledby="quick-guide-title">
      <div class="flex items-center gap-2">
        <MessageCircleQuestion :size="20" class="text-[var(--color-selected-text)]" />
        <h2 id="quick-guide-title" class="text-lg font-bold">빠른 이용 가이드</h2>
      </div>

      <div class="mt-4 grid gap-3">
        <article
          v-for="guide in guides"
          :key="guide.title"
          class="flex items-center gap-4 rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-4 shadow-sm"
        >
          <div
            class="grid size-11 shrink-0 place-items-center rounded-xl bg-[var(--color-selected-background)] text-[var(--color-selected-text)]"
            aria-hidden="true"
          >
            <component :is="guide.icon" :size="22" />
          </div>
          <div>
            <h3 class="text-base font-bold">{{ guide.title }}</h3>
            <p class="mt-1 text-xs text-[var(--color-text-secondary)]">
              {{ guide.description }}
            </p>
          </div>
        </article>
      </div>
    </section>

    <section class="mt-8" aria-labelledby="faq-title">
      <div class="flex items-center gap-2">
        <UsersRound :size="20" class="text-[var(--color-selected-text)]" />
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
</template>
