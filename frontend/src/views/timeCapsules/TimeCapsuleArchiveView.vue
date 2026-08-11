<script setup lang="ts">
import { computed, ref } from 'vue'
import { CalendarClock, Check, Plus, X } from 'lucide-vue-next'
import { useRouter } from 'vue-router'
import calendarImage from '@/assets/images/timeCapsules/archive/calendar.png'
import lockImage from '@/assets/images/timeCapsules/archive/lock.png'
import openImage from '@/assets/images/timeCapsules/archive/open.png'
import BaseDatePicker from '@/components/common/BaseDatePicker.vue'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { showToast } = useToast()
const isFreeCapsuleSheetOpen = ref(false)
const freeCapsuleOpenDate = ref('')
const isFreeCapsuleCreated = ref(false)

const today = new Date()
const todayKey = [
  today.getFullYear(),
  String(today.getMonth() + 1).padStart(2, '0'),
  String(today.getDate()).padStart(2, '0'),
].join('.')

const isReleased = (releaseDate: string) => releaseDate <= todayKey

const toDateValue = (date: Date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
const tomorrow = new Date()
tomorrow.setDate(tomorrow.getDate() + 1)
const minOpenDate = toDateValue(tomorrow)
const maxOpenYear = today.getFullYear() + 30
const formattedFreeOpenDate = computed(() => {
  if (!freeCapsuleOpenDate.value) return ''
  const [year, month, day] = freeCapsuleOpenDate.value.split('-')
  return `${year}.${month}.${day}`
})
const confirmFreeCapsule = () => {
  if (!freeCapsuleOpenDate.value) {
    showToast('오픈 날짜를 선택해주세요.', 'error')
    return
  }
  isFreeCapsuleCreated.value = true
  isFreeCapsuleSheetOpen.value = false
  showToast('입출금계좌의 오픈 날짜를 설정했습니다.', 'success')
}

const openFreeCapsuleList = () => {
  router.push({
    name: 'TimeCapsuleList',
    params: { capsuleListId: '3' },
    query: { openDate: freeCapsuleOpenDate.value },
  })
}

const openCapsule = (capsule: { id: number; createdAt: string; isFree?: boolean }) => {
  if (!capsule.isFree) {
    router.push(
      isCapsuleReleased(capsule)
        ? `/time-capsules/${capsule.id}/open`
        : `/time-capsules/${capsule.id}`,
    )
    return
  }
  if (!isFreeCapsuleCreated.value) {
    isFreeCapsuleSheetOpen.value = true
    return
  }
  openFreeCapsuleList()
}

const capsuleAccounts = computed(() => [
  {
    id: 1,
    name: '아이사랑적금',
    createdAt: '2026.08.06',
    savedAmount: 200000,
  },
  {
    id: 2,
    name: '우리사랑적금',
    createdAt: '2027.07.18',
    savedAmount: 200000,
  },
  {
    id: 3,
    name: '입출금계좌',
    createdAt: isFreeCapsuleCreated.value ? formattedFreeOpenDate.value : '오픈 날짜 설정',
    savedAmount: 0,
    isFree: true,
  },
])

const isCapsuleReleased = (capsule: { createdAt: string; isFree?: boolean }) =>
  capsule.isFree ? isFreeCapsuleCreated.value && isReleased(capsule.createdAt) : isReleased(capsule.createdAt)
</script>

<template>
  <main
    class="flex min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] flex-col bg-white"
  >
    <section class="px-5 pt-7 pb-6">
      <div>
        <h1
          class="text-[24px] leading-tight font-bold tracking-[-0.025em] text-[var(--color-text-primary)]"
        >
          타임캡슐 보관함
        </h1>
        <p class="mt-2 text-sm text-[var(--color-text-secondary)]">
          깨비의 성장 순간과 금융 기록을 모아보세요.
        </p>

        <article
          class="mt-6 flex min-h-20 items-center rounded-2xl border border-[var(--color-border)] bg-white px-4 shadow-[0_8px_24px_rgba(67,139,179,0.08)]"
        >
          <img class="size-14 shrink-0 object-contain" :src="calendarImage" alt="공개 예정일" />
          <div class="ml-3 min-w-0 flex-1">
            <p class="truncate text-sm font-bold text-[var(--color-text-primary)]">아이사랑적금</p>
            <time class="mt-1 block text-xs font-medium text-[var(--color-text-secondary)]">2026.08.08</time>
          </div>
          <strong class="ml-3 shrink-0 text-[30px] font-bold tracking-[-0.04em] text-[#27a9eb]">
            D-23
          </strong>
        </article>
      </div>
    </section>

    <section class="grid grid-cols-2 gap-3 px-5 pt-1" aria-label="타임캡슐 계좌 목록">
      <article
        v-for="capsule in capsuleAccounts"
        :key="capsule.id"
        class="overflow-hidden rounded-2xl border p-3 transition-colors"
        :class="
          isCapsuleReleased(capsule)
            ? 'capsule-card--released border-transparent bg-white'
            : 'border-[var(--color-border)] bg-white'
        "
      >
        <button
          class="block w-full text-left transition-transform active:scale-[0.98]"
          type="button"
          @click="openCapsule(capsule)"
        >
          <span
            class="relative grid aspect-[4/3] place-items-center rounded-xl"
            :class="isCapsuleReleased(capsule) ? 'bg-[#ecfaff]' : 'bg-[#f0f3f5]'"
          >
            <span
              v-if="capsule.isFree && !isFreeCapsuleCreated"
              class="open-date-alert absolute top-2 right-2 grid size-5 place-items-center rounded-full bg-[#ef5b5b] text-[15px] font-black leading-none text-white"
              aria-label="오픈 날짜 설정 필요"
            >
              !
            </span>
            <img
              class="h-16 w-20 object-contain"
              :src="isCapsuleReleased(capsule) ? openImage : lockImage"
              :alt="isCapsuleReleased(capsule) ? '열린 타임캡슐' : '잠긴 타임캡슐'"
            />
          </span>
          <strong class="mt-4 block truncate text-base text-[var(--color-text-primary)]">
            {{ capsule.name }}
          </strong>
          <time
            class="mt-1 block text-xs"
            :class="capsule.isFree && !isFreeCapsuleCreated ? 'font-bold text-[#ef5b5b]' : 'text-[var(--color-text-secondary)]'"
          >
            {{ capsule.createdAt }}
          </time>
          <p class="mt-3 text-xs font-bold text-[var(--color-selected-text)]">
            저축 금액 {{ capsule.savedAmount.toLocaleString('ko-KR') }}원
          </p>
        </button>

      </article>

    </section>

    <button
      class="time-capsule-create-button fixed z-[60]"
      type="button"
      aria-label="타임캡슐 생성하기"
      @click="router.push('/time-capsules/new')"
    >
      <span class="time-capsule-create-button__surface">
        <Plus :size="23" :stroke-width="3" aria-hidden="true" />
      </span>
    </button>

    <Teleport to="body">
      <Transition name="free-capsule-sheet">
        <div v-if="isFreeCapsuleSheetOpen" class="fixed inset-0 z-[var(--z-index-overlay)] flex items-end justify-center bg-black/40" @click.self="isFreeCapsuleSheetOpen = false">
          <section class="free-capsule-panel w-full max-w-[var(--app-max-width)] rounded-t-[26px] bg-white px-5 pt-3 pb-[calc(24px+env(safe-area-inset-bottom))]" role="dialog" aria-modal="true" aria-labelledby="free-capsule-title">
            <span class="mx-auto block h-1 w-10 rounded-full bg-[#d7dfe4]"></span>
            <header class="mt-4 flex items-start justify-between gap-4">
              <div>
                <h2 id="free-capsule-title" class="m-0 text-[20px] font-bold">언제 열어볼까요?</h2>
              </div>
              <button class="grid size-9 shrink-0 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[#f2f5f7]" type="button" aria-label="오픈 날짜 설정 닫기" @click="isFreeCapsuleSheetOpen = false"><X :size="20" /></button>
            </header>

            <div class="mt-5 rounded-2xl bg-[#f6f8fa] p-4">
              <div class="mb-3 flex items-center gap-2 text-xs font-bold text-[#5d7180]">타임캡슐 오픈 날짜</div>
              <BaseDatePicker
                v-model="freeCapsuleOpenDate"
                class="free-capsule-date-picker"
                placeholder="날짜를 선택해주세요"
                :min-year="tomorrow.getFullYear()"
                :max-year="maxOpenYear"
                :min-date="minOpenDate"
              />
            </div>

            <button class="mt-5 flex h-[54px] w-full items-center justify-center gap-2 rounded-2xl bg-[var(--color-brand-primary)] text-sm font-bold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[#cbd8df]" type="button" :disabled="!freeCapsuleOpenDate" @click="confirmFreeCapsule">
              <Check :size="17" :stroke-width="2.8" />
              이 날짜로 캡슐 만들기
            </button>
          </section>
        </div>
      </Transition>
    </Teleport>
  </main>
</template>

<style scoped>
.time-capsule-create-button {
  bottom: calc(var(--app-bottom-nav-height) + env(safe-area-inset-bottom) - 1px);
  left: 50%;
  width: 88px;
  height: 47px;
  padding: 7px 8px 0;
  color: var(--color-text-inverse);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-bottom: 0;
  border-radius: 48px 48px 0 0;
  box-shadow: 0 -5px 16px rgb(45 91 116 / 8%);
  transform: translateX(-50%);
}

.time-capsule-create-button::after {
  position: absolute;
  right: -7px;
  bottom: 0;
  left: -7px;
  height: 2px;
  background: var(--color-surface);
  content: '';
}

.time-capsule-create-button__surface {
  position: relative;
  z-index: 1;
  display: grid;
  width: 100%;
  height: 40px;
  padding-top: 3px;
  place-items: center;
  background: var(--color-brand-primary);
  border-radius: 40px 40px 0 0;
  box-shadow: 0 -3px 10px rgb(39 169 235 / 18%);
  transition: background-color 140ms ease, transform 140ms ease;
}

.time-capsule-create-button:active .time-capsule-create-button__surface {
  background: var(--color-brand-primary-pressed);
  transform: translateY(2px);
}

.time-capsule-create-button:focus-visible {
  outline: 3px solid rgb(39 169 235 / 24%);
  outline-offset: 3px;
}

.open-date-alert {
  isolation: isolate;
  box-shadow: 0 3px 9px rgb(239 91 91 / 32%);
  animation: open-date-alert-bounce 2.2s ease-in-out infinite;
}

.open-date-alert::before,
.open-date-alert::after {
  position: absolute;
  z-index: -1;
  inset: 0;
  border: 2px solid rgb(239 91 91 / 58%);
  border-radius: 50%;
  content: '';
  opacity: 0;
  animation: open-date-alert-ripple 2.2s cubic-bezier(0.2, 0.7, 0.35, 1) infinite;
}

.open-date-alert::after {
  animation-delay: 240ms;
}

@keyframes open-date-alert-bounce {
  0%, 10%, 24%, 100% { transform: translateY(0) scale(1); }
  14% { transform: translateY(-2px) scale(1.12); }
  19% { transform: translateY(0) scale(0.96); }
}

@keyframes open-date-alert-ripple {
  0%, 12% { opacity: 0; transform: scale(0.85); }
  16% { opacity: 0.62; }
  34%, 100% { opacity: 0; transform: scale(2.15); }
}

.capsule-card--released {
  position: relative;
}

.capsule-card--released::before {
  position: absolute;
  inset: 0;
  z-index: 1;
  padding: 2px;
  pointer-events: none;
  background: linear-gradient(110deg, #9fddf5 5%, #d8c4f3 24%, #f5bfd9 42%, #fff0ae 60%, #b9efd9 78%, #9fddf5 96%);
  background-size: 260% 100%;
  border-radius: inherit;
  content: '';
  animation: silver-border-shimmer 5s ease-in-out infinite;
  -webkit-mask: linear-gradient(#000 0 0) content-box, linear-gradient(#000 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  filter: drop-shadow(0 0 4px rgb(174 218 241 / 32%));
}

@keyframes silver-border-shimmer {
  0%, 18% { background-position: 100% 0; }
  72%, 100% { background-position: -100% 0; }
}

.free-capsule-date-picker :deep(> button) { height: 52px; font-size: 14px; }
.free-capsule-date-picker :deep(> div[role='dialog']) {
  top: auto;
  bottom: 100%;
  z-index: 30;
  max-height: min(430px, calc(100dvh - 84px));
  margin-top: 0;
  margin-bottom: 8px;
  overflow-y: auto;
}
.free-capsule-sheet-enter-active,
.free-capsule-sheet-leave-active { transition: background-color 180ms ease; }
.free-capsule-sheet-enter-active .free-capsule-panel,
.free-capsule-sheet-leave-active .free-capsule-panel { transition: transform 220ms cubic-bezier(0.22, 1, 0.36, 1); }
.free-capsule-sheet-enter-from,
.free-capsule-sheet-leave-to { background-color: transparent; }
.free-capsule-sheet-enter-from .free-capsule-panel,
.free-capsule-sheet-leave-to .free-capsule-panel { transform: translateY(100%); }

@media (prefers-reduced-motion: reduce) {
  .capsule-card--released::before { animation: none; }
  .open-date-alert,
  .open-date-alert::before,
  .open-date-alert::after { animation: none; }
  .free-capsule-sheet-enter-active,
  .free-capsule-sheet-leave-active,
  .free-capsule-sheet-enter-active .free-capsule-panel,
  .free-capsule-sheet-leave-active .free-capsule-panel { transition-duration: 1ms; }
}
</style>
