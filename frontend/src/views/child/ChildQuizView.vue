<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import confetti from 'canvas-confetti'
import { Check } from 'lucide-vue-next'

import ChildBottomNavigation from '@/components/child/ChildBottomNavigation.vue'
import checklistTrophyUrl from '@/assets/images/checklists/trophy.png'
import { childQuizQuestions } from '@/mocks/childFinanceFlow'

const quizIndex = ref(0)
const selectedAnswerIndex = ref<number | null>(null)
const isQuizComplete = ref(false)

const quiz = computed(() => childQuizQuestions[quizIndex.value] ?? childQuizQuestions[0]!)
const hasAnsweredQuiz = computed(() => selectedAnswerIndex.value !== null)

const selectQuizAnswer = (optionIndex: number) => {
  if (hasAnsweredQuiz.value) return

  selectedAnswerIndex.value = optionIndex
}

const goNextQuiz = () => {
  if (!hasAnsweredQuiz.value) return

  if (quizIndex.value + 1 >= childQuizQuestions.length) {
    isQuizComplete.value = true
    return
  }

  quizIndex.value += 1
  selectedAnswerIndex.value = null
}

const getOptionClass = (optionIndex: number) => {
  if (!hasAnsweredQuiz.value) {
    return selectedAnswerIndex.value === optionIndex
      ? 'border-[var(--color-brand-primary)] bg-[#eef8ff] text-[var(--color-brand-primary)]'
      : 'border-[var(--color-border)] bg-white text-[var(--color-text-primary)]'
  }

  if (optionIndex === quiz.value.answerIndex) {
    return 'border-[#bfe9d8] bg-[#e8f8ef] text-[#189f63]'
  }

  if (optionIndex === selectedAnswerIndex.value) {
    return 'border-[#fecaca] bg-[#fff1f2] text-[#ef4444]'
  }

  return 'border-[var(--color-border)] bg-white text-[var(--color-text-primary)]'
}

const runCompleteConfetti = () => {
  confetti({ particleCount: 90, spread: 72, origin: { y: 0.54 } })
  confetti({ particleCount: 45, angle: 60, spread: 58, origin: { x: 0.16, y: 0.68 } })
  confetti({ particleCount: 45, angle: 120, spread: 58, origin: { x: 0.84, y: 0.68 } })
}

watch(isQuizComplete, (complete) => {
  if (complete) {
    runCompleteConfetti()
  }
})
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-6 pb-[104px]">
    <section
      v-if="isQuizComplete"
      class="grid min-h-[calc(100dvh-var(--app-header-height)-104px)] content-center justify-items-center text-center"
      aria-label="오늘의 퀴즈 완료"
    >
      <img
        class="mb-8 w-[240px] max-w-[72vw] select-none object-contain"
        :src="checklistTrophyUrl"
        alt=""
        aria-hidden="true"
      />
      <h1 class="m-0 text-[26px] font-bold text-[var(--color-text-primary)]">
        오늘의 퀴즈 완료!
      </h1>
      <p class="mt-4 mb-10 text-[16px] leading-[1.6] text-[var(--color-text-secondary)]">
        정답을 모두 맞혔어요!<br />
        정말 대단해요.
      </p>
      <RouterLink
        class="grid h-14 w-full place-items-center rounded-[14px] bg-[var(--color-brand-primary)] text-[18px] font-bold !text-white no-underline"
        to="/child/home"
      >
        확인
      </RouterLink>
    </section>

    <section v-else aria-label="오늘의 금융 퀴즈">
      <div class="mb-7 flex items-center justify-between">
        <h1 class="m-0 text-[20px] font-bold text-[var(--color-text-primary)]">
          오늘의 금융 퀴즈
        </h1>
        <span class="text-[18px] font-bold text-[var(--color-text-secondary)]">
          {{ quizIndex + 1 }} / {{ childQuizQuestions.length }}
        </span>
      </div>

      <h2 class="mb-6 text-[24px] leading-[1.45] font-bold text-[var(--color-text-primary)]">
        {{ quiz.question }}
      </h2>

      <div class="grid gap-3">
        <button
          v-for="(option, optionIndex) in quiz.options"
          :key="option"
          class="flex min-h-12 items-center justify-between rounded-[10px] border px-4 py-3 text-left text-[16px] font-bold"
          :class="getOptionClass(optionIndex)"
          type="button"
          @click="selectQuizAnswer(optionIndex)"
        >
          <span>{{ optionIndex + 1 }}. {{ option }}</span>
          <Check
            v-if="hasAnsweredQuiz && optionIndex === quiz.answerIndex"
            :size="18"
            :stroke-width="3"
          />
        </button>
      </div>

      <div
        v-if="hasAnsweredQuiz"
        class="mt-6 rounded-[12px] bg-[#e8f8ef] px-4 py-4 text-[16px] leading-[1.65] text-[var(--color-text-primary)]"
      >
        <strong class="mb-1 block">해설</strong>
        {{ quiz.explanation }}
      </div>

      <button
        class="fixed bottom-[96px] left-1/2 h-14 w-[calc(100%-40px)] max-w-[calc(var(--app-max-width)-40px)] -translate-x-1/2 rounded-[14px] border-0 bg-[var(--color-brand-primary)] text-[18px] font-bold !text-white disabled:bg-[#c8d2da]"
        type="button"
        :disabled="!hasAnsweredQuiz"
        @click="goNextQuiz"
      >
        {{ quizIndex + 1 === childQuizQuestions.length ? '완료하기' : '다음 문제' }}
      </button>
    </section>

    <ChildBottomNavigation />
  </main>
</template>
