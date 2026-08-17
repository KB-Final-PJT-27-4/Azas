<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import confetti from 'canvas-confetti'
import { Check, X } from 'lucide-vue-next'

import ChildBottomNavigation from '@/components/child/ChildBottomNavigation.vue'
import childQuizCorrectPigUrl from '@/assets/images/child/child-quiz-correct-pig.png'
import childQuizThinkingPigUrl from '@/assets/images/child/child-quiz-thinking-pig.png'
import childQuizWrongPigUrl from '@/assets/images/child/child-quiz-wrong-pig.png'
import checklistTrophyUrl from '@/assets/images/checklists/trophy.png'
import { childQuizQuestions } from '@/mocks/childFinanceFlow'

const quizIndex = ref(0)
const selectedAnswerIndex = ref<number | null>(null)
const isQuizComplete = ref(false)
const answerResults = ref<Array<'correct' | 'wrong' | null>>(
  Array.from({ length: childQuizQuestions.length }, () => null),
)

const quiz = computed(() => childQuizQuestions[quizIndex.value] ?? childQuizQuestions[0]!)
const hasAnsweredQuiz = computed(() => selectedAnswerIndex.value !== null)
const currentAnswerResult = computed(() => answerResults.value[quizIndex.value])
const quizReactionImage = computed(() => {
  if (currentAnswerResult.value === 'correct') return childQuizCorrectPigUrl
  if (currentAnswerResult.value === 'wrong') return childQuizWrongPigUrl
  return childQuizThinkingPigUrl
})

const selectQuizAnswer = (optionIndex: number) => {
  if (hasAnsweredQuiz.value) return

  selectedAnswerIndex.value = optionIndex
  answerResults.value[quizIndex.value] =
    optionIndex === quiz.value.answerIndex ? 'correct' : 'wrong'
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

const getStepState = (stepIndex: number) => {
  const result = answerResults.value[stepIndex]
  if (result) return result
  if (stepIndex === quizIndex.value) return 'current'
  if (stepIndex < quizIndex.value) return 'passed'
  return 'pending'
}

const getStepClass = (stepIndex: number) => {
  const state = getStepState(stepIndex)

  if (state === 'correct') return 'quiz-step--correct'
  if (state === 'wrong') return 'quiz-step--wrong'
  if (state === 'current') return 'quiz-step--current'
  if (state === 'passed') return 'quiz-step--passed'
  return 'quiz-step--pending'
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
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-white px-5 pt-0 pb-[168px]">
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

    <section
      v-else
      class="min-h-[calc(100dvh-var(--app-header-height)-168px)]"
      aria-label="오늘의 금융 퀴즈"
    >
      <div class="pt-7 pb-7 px-1" aria-label="퀴즈 진행 상태">
        <ol
          class="quiz-steps m-0 grid list-none p-0"
          :style="{ gridTemplateColumns: `repeat(${childQuizQuestions.length}, minmax(0, 1fr))` }"
        >
          <li
            v-for="(_, stepIndex) in childQuizQuestions"
            :key="stepIndex"
            class="quiz-step"
          >
            <span class="quiz-step__line" aria-hidden="true"></span>
            <span class="quiz-step__circle" :class="getStepClass(stepIndex)">
              <Check
                v-if="answerResults[stepIndex] === 'correct'"
                class="quiz-step__icon"
                :size="17"
                :stroke-width="3"
                aria-hidden="true"
              />
              <X
                v-else-if="answerResults[stepIndex] === 'wrong'"
                class="quiz-step__icon"
                :size="17"
                :stroke-width="3"
                aria-hidden="true"
              />
              <span v-else>{{ stepIndex + 1 }}</span>
            </span>
          </li>
        </ol>
      </div>

      <Transition name="quiz-slide" mode="out-in">
        <div :key="quizIndex" class="quiz-question-panel">
          <div class="mb-3 grid justify-items-center">
            <img
              class="quiz-reaction-image select-none object-contain"
              :class="hasAnsweredQuiz ? 'quiz-reaction-image--answered' : 'quiz-reaction-image--idle'"
              :src="quizReactionImage"
              alt=""
              aria-hidden="true"
            />
          </div>

          <h2
            class="mb-6 text-center text-[24px] leading-[1.45] font-bold text-[var(--color-text-primary)]"
          >
            {{ quiz.question }}
          </h2>

          <div class="grid gap-3">
            <button
              v-for="(option, optionIndex) in quiz.options"
              :key="option"
              class="flex min-h-12 items-center justify-between rounded-[10px] border px-4 py-3 text-left text-[16px] font-bold transition duration-200 active:scale-[0.99]"
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

          <div class="mt-6 min-h-[132px]">
            <Transition name="quiz-feedback">
              <div
                v-if="hasAnsweredQuiz"
                class="min-h-[132px] rounded-[12px] px-4 py-4 text-[16px] leading-[1.65] text-[var(--color-text-primary)]"
                :class="currentAnswerResult === 'correct' ? 'bg-[#e8f8ef]' : 'bg-[#fff1f2]'"
              >
                <strong class="mb-1 block">
                  {{ currentAnswerResult === 'correct' ? '정답이에요' : '다시 기억해봐요' }}
                </strong>
                {{ quiz.explanation }}
              </div>
            </Transition>
          </div>
        </div>
      </Transition>

      <button
        class="fixed bottom-[96px] left-1/2 h-14 w-[calc(100%-40px)] max-w-[calc(var(--app-max-width)-40px)] -translate-x-1/2 rounded-[14px] border-0 bg-[var(--color-brand-primary)] text-[18px] font-bold !text-white disabled:bg-[#cbd8df]"
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

<style scoped>
.quiz-step {
  position: relative;
  display: grid;
  min-width: 0;
  place-items: center;
}

.quiz-step__line {
  position: absolute;
  top: 50%;
  right: 50%;
  left: -50%;
  height: 2px;
  background: #dbe3e8;
  transform: translateY(-50%);
}

.quiz-step:first-child .quiz-step__line {
  display: none;
}

.quiz-step__circle {
  position: relative;
  z-index: 1;
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  border: 2px solid #dbe3e8;
  border-radius: 999px;
  background: white;
  color: #9aa6b2;
  font-size: 15px;
  font-weight: 800;
  transition:
    background-color 180ms ease,
    border-color 180ms ease,
    color 180ms ease,
    transform 180ms ease;
}

.quiz-step--current {
  border-color: var(--color-brand-primary);
  background: var(--color-brand-primary);
  color: white;
  box-shadow: 0 6px 16px rgb(39 169 235 / 18%);
}

.quiz-step--passed {
  border-color: #cfeaf7;
  background: #eef8ff;
  color: var(--color-brand-primary);
}

.quiz-step--pending {
  border-color: #dbe3e8;
}

.quiz-step--correct {
  border-color: #22c55e;
  background: #22c55e;
  color: white;
  animation: quiz-step-pop 260ms cubic-bezier(0.2, 1.4, 0.4, 1);
}

.quiz-step--wrong {
  border-color: #ef4444;
  background: #ef4444;
  color: white;
  animation: quiz-step-shake 300ms ease;
}

.quiz-step__icon {
  animation: quiz-step-icon 220ms ease both;
}

.quiz-question-panel {
  min-height: 572px;
}

.quiz-reaction-image {
  width: 148px;
  height: 112px;
  transition:
    transform 180ms ease,
    opacity 180ms ease;
}

.quiz-reaction-image--idle {
  opacity: 0.95;
}

.quiz-reaction-image--answered {
  animation: quiz-reaction-pop 240ms cubic-bezier(0.2, 1.2, 0.4, 1);
}

.quiz-slide-enter-active,
.quiz-slide-leave-active {
  transition:
    opacity 180ms ease,
    transform 220ms cubic-bezier(0.22, 1, 0.36, 1);
}

.quiz-slide-enter-from {
  opacity: 0;
  transform: translateX(18px);
}

.quiz-slide-leave-to {
  opacity: 0;
  transform: translateX(-18px);
}

.quiz-feedback-enter-active,
.quiz-feedback-leave-active {
  transition:
    opacity 180ms ease,
    transform 180ms ease;
}

.quiz-feedback-enter-from,
.quiz-feedback-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@keyframes quiz-step-pop {
  0% {
    transform: scale(0.72);
  }
  70% {
    transform: scale(1.12);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes quiz-step-shake {
  0%,
  100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-3px);
  }
  50% {
    transform: translateX(3px);
  }
  75% {
    transform: translateX(-2px);
  }
}

@keyframes quiz-step-icon {
  from {
    opacity: 0;
    transform: scale(0.65);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

@keyframes quiz-reaction-pop {
  0% {
    transform: translateY(4px) scale(0.92);
  }
  100% {
    transform: translateY(0) scale(1);
  }
}
</style>
