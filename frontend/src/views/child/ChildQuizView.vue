<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Check, Lightbulb, Trophy, X } from 'lucide-vue-next'

import completeStarUrl from '@/assets/images/accounts/complete-star.png'
import childQuizCorrectPigUrl from '@/assets/images/child/child-quiz-correct-pig.png'
import childQuizThinkingPigUrl from '@/assets/images/child/child-quiz-thinking-pig.png'
import childQuizWrongPigUrl from '@/assets/images/child/child-quiz-wrong-pig.png'
import childQuizCompletePigUrl from '@/assets/images/child/child-quiz-complete-pig.png'
import { childQuizQuestions } from '@/mocks/childFinanceFlow'
import { markChildQuizCompletedToday } from '@/utils/childQuizProgress'

const QUIZ_ROUND_SIZE = 5

const shuffleItems = <T,>(items: T[]) => {
  const shuffledItems = [...items]

  for (let index = shuffledItems.length - 1; index > 0; index -= 1) {
    const swapIndex = Math.floor(Math.random() * (index + 1))
    const currentItem = shuffledItems[index]!
    shuffledItems[index] = shuffledItems[swapIndex]!
    shuffledItems[swapIndex] = currentItem
  }

  return shuffledItems
}

const createQuizRound = () =>
  shuffleItems(childQuizQuestions)
    .slice(0, QUIZ_ROUND_SIZE)
    .map((question) => {
      const shuffledOptions = shuffleItems(
        question.options.map((option, optionIndex) => ({ option, optionIndex })),
      )

      return {
        ...question,
        options: shuffledOptions.map(({ option }) => option),
        answerIndex: shuffledOptions.findIndex(
          ({ optionIndex }) => optionIndex === question.answerIndex,
        ),
      }
    })

const quizQuestions = ref(createQuizRound())
const quizIndex = ref(0)
const selectedAnswerIndex = ref<number | null>(null)
const isQuizComplete = ref(false)
const nextButtonRef = ref<HTMLButtonElement | null>(null)
const answerResults = ref<Array<'correct' | 'wrong' | null>>(
  Array.from({ length: quizQuestions.value.length }, () => null),
)

const quiz = computed(() => quizQuestions.value[quizIndex.value] ?? quizQuestions.value[0]!)
const hasAnsweredQuiz = computed(() => selectedAnswerIndex.value !== null)
const currentAnswerResult = computed(() => answerResults.value[quizIndex.value])
const correctAnswerCount = computed(
  () => answerResults.value.filter((result) => result === 'correct').length,
)
const quizReactionImage = computed(() => {
  if (currentAnswerResult.value === 'correct') return childQuizCorrectPigUrl
  if (currentAnswerResult.value === 'wrong') return childQuizWrongPigUrl
  return childQuizThinkingPigUrl
})

const selectQuizAnswer = async (optionIndex: number) => {
  if (hasAnsweredQuiz.value) return

  selectedAnswerIndex.value = optionIndex
  answerResults.value[quizIndex.value] =
    optionIndex === quiz.value.answerIndex ? 'correct' : 'wrong'

  await nextTick()
  nextButtonRef.value?.scrollIntoView({ behavior: 'smooth', block: 'end' })
}

const goNextQuiz = async () => {
  if (!hasAnsweredQuiz.value) return

  if (quizIndex.value + 1 >= quizQuestions.value.length) {
    markChildQuizCompletedToday()
    isQuizComplete.value = true
    await nextTick()
    window.scrollTo({ top: 0, behavior: 'smooth' })
    return
  }

  quizIndex.value += 1
  selectedAnswerIndex.value = null

  await nextTick()
  window.scrollTo({ top: 0, behavior: 'smooth' })
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

</script>

<template>
  <main
    class="px-[18px]"
    :class="
      isQuizComplete
        ? 'h-[calc(100dvh-var(--app-header-height))] overflow-hidden bg-white pb-[var(--app-bottom-nav-height)]'
        : 'min-h-[calc(100dvh-var(--app-header-height))] bg-[#eef9ff] pt-4 pb-[calc(var(--app-bottom-nav-height)+24px)]'
    "
  >
    <section
      v-if="isQuizComplete"
      class="grid h-full content-center justify-items-center bg-white px-5 py-4 text-center"
      aria-label="오늘의 퀴즈 완료"
    >
      <div class="quiz-complete-scene mb-5" aria-hidden="true">
        <img
          class="quiz-complete-star quiz-complete-star--left"
          :src="completeStarUrl"
          alt=""
        />
        <img
          class="quiz-complete-star quiz-complete-star--right"
          :src="completeStarUrl"
          alt=""
        />
        <img
          class="quiz-complete-pig select-none object-contain"
          :src="childQuizCompletePigUrl"
          alt=""
        />
      </div>
      <span class="inline-flex items-center gap-1.5 rounded-full bg-[#fff7d8] px-3 py-1.5 text-[12px] font-bold text-[#b98916]">
        <Trophy :size="15" :stroke-width="2.4" /> 오늘의 금융 습관
      </span>
      <h1 class="mt-4 text-[26px] font-extrabold text-[var(--color-text-primary)]">
        오늘의 퀴즈 완료!
      </h1>
      <p class="mt-3 text-[14px] leading-[1.6] text-[var(--color-text-secondary)]">
        오늘도 금융 습관을 하나 배웠어요.<br />
        내일 또 새로운 퀴즈를 풀어봐요.
      </p>
      <div class="mt-5 flex w-full items-center justify-between rounded-[16px] bg-[#f3faff] px-4 py-3.5">
        <span class="text-[13px] font-semibold text-[var(--color-text-secondary)]">맞힌 문제</span>
        <strong class="text-[18px] text-[var(--color-selected-text)]">
          {{ correctAnswerCount }} / {{ quizQuestions.length }}
        </strong>
      </div>
      <RouterLink
        class="mt-5 grid h-14 w-full place-items-center rounded-[14px] bg-[var(--color-brand-primary)] text-[16px] font-bold !text-white no-underline"
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
      <div class="rounded-[20px] border border-[#dce8ee] bg-white px-4 py-4 shadow-[0_8px_22px_rgba(54,112,139,0.05)]" aria-label="퀴즈 진행 상태">
        <div class="mb-4 flex items-center justify-between">
          <strong class="text-[14px] font-extrabold">오늘의 금융 퀴즈</strong>
          <span class="rounded-full bg-[#eaf8ff] px-2.5 py-1 text-[11px] font-bold text-[var(--color-selected-text)]">
            {{ quizIndex + 1 }} / {{ quizQuestions.length }}
          </span>
        </div>
        <ol
          class="quiz-steps m-0 grid list-none p-0"
          :style="{ gridTemplateColumns: `repeat(${quizQuestions.length}, minmax(0, 1fr))` }"
        >
          <li
            v-for="(_, stepIndex) in quizQuestions"
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
        <div :key="quizIndex" class="quiz-question-panel mt-4 rounded-[22px] border border-[#dce8ee] bg-white p-5 shadow-[0_10px_28px_rgba(54,112,139,0.06)]">
          <div class="mb-2 grid justify-items-center rounded-[18px] bg-[#f5fbfe] py-2">
            <img
              class="quiz-reaction-image select-none object-contain"
              :class="hasAnsweredQuiz ? 'quiz-reaction-image--answered' : 'quiz-reaction-image--idle'"
              :src="quizReactionImage"
              alt=""
              aria-hidden="true"
            />
          </div>

          <span class="mt-4 block text-[12px] font-extrabold text-[var(--color-selected-text)]">
            Q{{ quizIndex + 1 }}
          </span>
          <h2 class="mt-2 mb-5 text-[21px] leading-[1.45] font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]">
            {{ quiz.question }}
          </h2>

          <div class="grid gap-3">
            <button
              v-for="(option, optionIndex) in quiz.options"
              :key="option"
              class="flex min-h-14 items-center justify-between gap-3 rounded-[14px] border px-3.5 py-3 text-left text-[14px] font-bold transition duration-200 active:scale-[0.99]"
              :class="getOptionClass(optionIndex)"
              type="button"
              @click="selectQuizAnswer(optionIndex)"
            >
              <span class="flex min-w-0 items-center gap-3">
                <span class="grid size-7 shrink-0 place-items-center rounded-full bg-[#f0f4f6] text-[12px] font-extrabold">
                  {{ optionIndex + 1 }}
                </span>
                <span>{{ option }}</span>
              </span>
              <Check
                v-if="hasAnsweredQuiz && optionIndex === quiz.answerIndex"
                :size="18"
                :stroke-width="3"
              />
            </button>
          </div>

          <div class="mt-4 min-h-[112px]">
            <Transition name="quiz-feedback">
              <div
                v-if="hasAnsweredQuiz"
                class="min-h-[112px] rounded-[14px] border px-4 py-4 text-[13px] leading-[1.65] text-[var(--color-text-primary)]"
                :class="currentAnswerResult === 'correct' ? 'border-[#ccebdd] bg-[#edf9f3]' : 'border-[#f8d4d4] bg-[#fff4f4]'"
              >
                <strong class="mb-1.5 flex items-center gap-1.5 text-[14px]">
                  <Lightbulb :size="16" :stroke-width="2.4" />
                  {{ currentAnswerResult === 'correct' ? '정답이에요' : '다시 기억해봐요' }}
                </strong>
                {{ quiz.explanation }}
              </div>
            </Transition>
          </div>
        </div>
      </Transition>

      <button
        ref="nextButtonRef"
        class="mt-4 h-14 w-full scroll-mb-[calc(var(--app-bottom-nav-height)+24px)] rounded-[15px] border-0 bg-[var(--color-brand-primary)] text-[16px] font-bold !text-white shadow-[0_6px_18px_rgba(39,169,235,0.2)] disabled:bg-[#cbd8df] disabled:shadow-none"
        type="button"
        :disabled="!hasAnsweredQuiz"
        @click="goNextQuiz"
      >
        {{ quizIndex + 1 === quizQuestions.length ? '완료하기' : '다음 문제' }}
      </button>
    </section>

  </main>
</template>

<style scoped>
.quiz-complete-scene {
  position: relative;
  width: 240px;
  max-width: 72vw;
  aspect-ratio: 1 / 1;
}

.quiz-complete-pig {
  position: absolute;
  z-index: 1;
  inset: 0;
  width: 100%;
  height: 100%;
}

.quiz-complete-star {
  position: absolute;
  z-index: 2;
  width: 38px;
  object-fit: contain;
  pointer-events: none;
}

.quiz-complete-star--left {
  top: auto;
  bottom: 18%;
  left: 1%;
  rotate: -10deg;
}

.quiz-complete-star--right {
  top: 14%;
  right: -3%;
  bottom: auto;
  width: 31px;
  rotate: 12deg;
}

@media (prefers-reduced-motion: no-preference) {
  .quiz-complete-pig {
    animation: quiz-complete-arrive 680ms cubic-bezier(0.16, 1, 0.3, 1) both;
  }

  .quiz-complete-star {
    animation: quiz-complete-star-twinkle 2.2s ease-in-out 520ms infinite;
  }

  .quiz-complete-star--right {
    animation-delay: 1.05s;
    animation-duration: 2.55s;
  }
}

@keyframes quiz-complete-arrive {
  0% { opacity: 0; transform: translateY(12px) scale(0.82); }
  68% { opacity: 1; transform: translateY(-2px) scale(1.04); }
  100% { opacity: 1; transform: none; }
}

@keyframes quiz-complete-star-twinkle {
  0%,
  100% { opacity: 0.58; transform: translateY(2px) scale(0.88); }
  50% { opacity: 1; transform: translateY(-4px) scale(1.08); }
}

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
  border-color: #bfe9d8;
  background: #e8f8ef;
  color: #189f63;
  animation: quiz-step-pop 260ms cubic-bezier(0.2, 1.4, 0.4, 1);
}

.quiz-step--wrong {
  border-color: #fecaca;
  background: #fff1f2;
  color: #ef4444;
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
