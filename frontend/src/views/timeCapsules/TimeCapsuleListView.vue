<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter, type RouteLocationRaw } from 'vue-router'
import { ChevronDown, EllipsisVertical, Trash2, X } from 'lucide-vue-next'
import capsulePigImage from '@/assets/images/timeCapsules/archive/list-capsule-pig.png'
import { api, getApiErrorMessage } from '@/api'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const { showToast } = useToast()
const activeTab = ref<'list' | 'calendar'>('list')
const isOpeningRecord = ref(false)
const today = new Date()
const currentYear = today.getFullYear()
const currentMonth = today.getMonth() + 1
const selectedYear = ref(currentYear)
const isCapsuleReleased = ref(false)

type TimeCapsuleRecord = {
  id: number
  title: string
  date: string
  amount: number
  thumbnail: string
  photos: Array<{ src: string; orientation: 'portrait'; type: 'image' }>
}

const accountId = computed(() => String(route.params.capsuleListId ?? '1'))
const account = ref({ name: '타임캡슐', description: '아이의 성장 순간과 금융 기록을 모아보세요.', totalSavedAmount: 0, records: [] as TimeCapsuleRecord[] })
const recordToDelete = ref<TimeCapsuleRecord | null>(null)
const isDeletingRecord = ref(false)
const openRecordMenuId = ref<number | null>(null)
const listRecords = computed(() =>
  [...account.value.records].sort((a, b) => b.date.localeCompare(a.date)),
)
const availableYears = computed(() =>
  [...new Set(account.value.records.map(({ date }) => Number(date.slice(0, 4))))].sort(
    (a, b) => b - a,
  ),
)
const months = computed(() =>
  Array.from({ length: 12 }, (_, index) => ({ year: selectedYear.value, month: index + 1 })),
)
const weekDays = ['일', '월', '화', '수', '목', '금', '토']

const localTimeCapsuleRecords: TimeCapsuleRecord[] = [
  {
    id: 1,
    title: '처음 마주 본 봄',
    date: '2023-01-15',
    amount: 30000,
    thumbnail: capsulePigImage,
    photos: [{ src: capsulePigImage, orientation: 'portrait', type: 'image' }],
  },
  {
    id: 2,
    title: '작은 손의 온기',
    date: '2023-02-20',
    amount: 50000,
    thumbnail: capsulePigImage,
    photos: [{ src: capsulePigImage, orientation: 'portrait', type: 'image' }],
  },
  {
    id: 3,
    title: '함께 웃던 오후',
    date: '2023-03-12',
    amount: 40000,
    thumbnail: capsulePigImage,
    photos: [{ src: capsulePigImage, orientation: 'portrait', type: 'image' }],
  },
]

const applyLocalTimeCapsuleFallback = () => {
  account.value = {
    name: '깨비 첫 타임캡슐',
    description: '로컬 더미데이터로 열어보는 타임캡슐입니다.',
    totalSavedAmount: localTimeCapsuleRecords.reduce((sum, record) => sum + record.amount, 0),
    records: localTimeCapsuleRecords,
  }
  isCapsuleReleased.value = true
}

const getMonthCells = (year: number, month: number) => {
  const firstDay = new Date(year, month - 1, 1).getDay()
  const lastDate = new Date(year, month, 0).getDate()
  return [
    ...Array.from({ length: firstDay }, () => null),
    ...Array.from({ length: lastDate }, (_, index) => index + 1),
  ]
}

onMounted(async () => {
  try {
    const { data } = await api.getTimeCapsuleEntriesUsingGET(Number(accountId.value))
    account.value = {
      name: data.time_capsule?.title ?? '타임캡슐',
      description: '아이의 성장 순간과 금융 기록을 모아보세요.',
      totalSavedAmount: data.time_capsule?.total_saved_amount ?? 0,
      records: (data.entries ?? []).map((entry) => ({
        id: entry.time_capsule_entry_id ?? 0,
        title: entry.title ?? '소중한 기록',
        date: entry.contributed_at?.slice(0, 10) ?? '',
        amount: entry.contribution_amount ?? 0,
        thumbnail: entry.thumbnail_url ?? capsulePigImage,
        photos: entry.thumbnail_url ? [{ src: entry.thumbnail_url, orientation: 'portrait', type: 'image' }] : [],
      })),
    }
    const releaseDate = data.time_capsule?.release_date
    isCapsuleReleased.value = data.time_capsule?.d_day !== undefined
      ? data.time_capsule.d_day <= 0
      : Boolean(releaseDate && releaseDate <= today.toISOString().slice(0, 10))
    if (import.meta.env.DEV && account.value.records.length === 0) {
      applyLocalTimeCapsuleFallback()
    }
  } catch {
    applyLocalTimeCapsuleFallback()
  }
})

const getRecord = (year: number, month: number, day: number | null) => {
  if (!day) return undefined
  const date = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  return account.value.records.find((record) => record.date === date)
}

const isToday = (year: number, month: number, day: number | null) =>
  year === currentYear && month === currentMonth && day === today.getDate()

const formatDate = (date: string) => date.replaceAll('-', '.')
const navigateForward = async (to: RouteLocationRaw) => {
  if (isOpeningRecord.value) return
  isOpeningRecord.value = true

  try {
    await new Promise((resolve) => window.setTimeout(resolve, 150))
    await router.push(to)
  } catch {
    isOpeningRecord.value = false
  }
}

const openRecord = (recordId: number) => {
  if (!isCapsuleReleased.value) {
    showToast('타임캡슐 기록은 공개일 이후에 확인할 수 있어요.', 'error')
    return
  }
  return navigateForward(`/time-capsules/${accountId.value}/${recordId}`)
}

const createFirstRecord = () =>
  navigateForward({
    name: 'TimeCapsuleCreate',
    query: {
      account: accountId.value,
      ...(typeof route.query.openDate === 'string' ? { openDate: route.query.openDate } : {}),
    },
  })

const requestRecordDeletion = (record: TimeCapsuleRecord) => {
  openRecordMenuId.value = null
  recordToDelete.value = record
}

const closeRecordMenuOnFocusOut = (event: FocusEvent) => {
  const menu = event.currentTarget as HTMLElement
  if (event.relatedTarget instanceof Node && menu.contains(event.relatedTarget)) return
  openRecordMenuId.value = null
}

const closeDeleteSheet = () => {
  if (isDeletingRecord.value) return
  recordToDelete.value = null
}

const deleteRecord = async () => {
  const record = recordToDelete.value
  if (!record || isDeletingRecord.value) return

  isDeletingRecord.value = true
  try {
    await api.deleteTimeCapsuleEntryUsingDELETE(record.id)
    account.value.records = account.value.records.filter(({ id }) => id !== record.id)
    account.value.totalSavedAmount = Math.max(0, account.value.totalSavedAmount - record.amount)
    recordToDelete.value = null
    showToast('타임캡슐 기록을 삭제했습니다.', 'success')
  } catch (error) {
    showToast(getApiErrorMessage(error, '타임캡슐 기록을 삭제하지 못했습니다.'), 'error')
  } finally {
    isDeletingRecord.value = false
  }
}

const scrollToMonth = async (month: number) => {
  await nextTick()
  document
    .getElementById(`calendar-month-${selectedYear.value}-${month}`)
    ?.scrollIntoView({ behavior: 'auto', block: 'start' })
}

const showCalendar = async () => {
  selectedYear.value = availableYears.value.includes(currentYear)
    ? currentYear
    : (availableYears.value[0] ?? currentYear)
  activeTab.value = 'calendar'
  await scrollToMonth(selectedYear.value === currentYear ? currentMonth : 1)
}

const changeYear = () => {
  scrollToMonth(selectedYear.value === currentYear ? currentMonth : 1)
}

const goToTimeCapsule = () => {
  const routePath =
    import.meta.env.DEV && accountId.value === 'local'
      ? '/time-capsules/local/open'
      : `/time-capsules/${accountId.value}/open`

  navigateForward(routePath)
}
</script>

<template>
  <main
    class="flex h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] flex-col overflow-hidden bg-white"
    :class="isOpeningRecord ? 'time-capsule-list--leaving pointer-events-none' : ''"
  >
    <section class="shrink-0 px-5">
      <div class="flex items-center">
        <div class="min-w-0 flex-1">
          <h1 class="truncate text-[24px] font-bold tracking-[-0.025em]">{{ account.name }}</h1>
          <p class="mt-1 text-xs text-[var(--color-text-secondary)]">{{ account.description }}</p>
        </div>
        <img
          class="h-24 w-28 translate-x-3 translate-y-3 shrink-0 origin-right scale-110 object-contain"
          :src="capsulePigImage"
          alt="타임캡슐 저금통"
        />
      </div>

      <div class="flex gap-4 border-b border-[var(--color-border)]" role="tablist">
        <button
          class="relative py-3 text-sm font-bold"
          :class="
            activeTab === 'list'
              ? 'text-[var(--color-selected-text)] after:absolute after:right-0 after:bottom-0 after:left-0 after:h-0.5 after:bg-[var(--color-brand-primary)]'
              : 'text-[var(--color-text-secondary)]'
          "
          type="button"
          role="tab"
          :aria-selected="activeTab === 'list'"
          @click="activeTab = 'list'"
        >
          리스트
        </button>
        <button
          class="relative py-3 text-sm font-bold"
          :class="
            activeTab === 'calendar'
              ? 'text-[var(--color-selected-text)] after:absolute after:right-0 after:bottom-0 after:left-0 after:h-0.5 after:bg-[var(--color-brand-primary)]'
              : 'text-[var(--color-text-secondary)]'
          "
          type="button"
          role="tab"
          :aria-selected="activeTab === 'calendar'"
          @click="showCalendar"
        >
          캘린더
        </button>
        <button
          class="relative py-3 text-sm font-bold text-[var(--color-text-secondary)]"
          type="button"
          role="tab"
          aria-selected="false"
          @click="goToTimeCapsule"
        >
          타임캡슐
        </button>
      </div>
    </section>

    <div
      v-if="activeTab === 'calendar'"
      class="shrink-0 border-[var(--color-border)] bg-white px-5 py-3"
    >
      <div class="flex justify-center">
        <label class="relative">
          <span class="sr-only">연도 선택</span>
          <select
            v-model="selectedYear"
            class="min-h-10 appearance-none rounded-full border border-[var(--color-border)] bg-white py-2 pr-10 pl-5 text-base font-bold outline-none focus:border-[var(--color-brand-primary)]"
            @change="changeYear"
          >
            <option v-for="year in availableYears" :key="year" :value="year">{{ year }}년</option>
          </select>
          <ChevronDown
            :size="17"
            class="pointer-events-none absolute top-1/2 right-4 -translate-y-1/2 text-[var(--color-text-secondary)]"
          />
        </label>
      </div>
    </div>

    <section v-if="activeTab === 'list'" class="min-h-0 flex-1 space-y-3 overflow-y-auto px-5 py-4">
      <button
        class="flex min-h-16 w-full items-center justify-between rounded-xl border border-[#d5edf8] bg-[#eaf8ff] px-4 text-left"
        type="button"
      >
        <span class="text-sm text-[var(--color-text-secondary)]">총 저축 금액</span>
        <strong class="ml-4 shrink-0 text-base text-[var(--color-selected-text)]">
          {{ account.totalSavedAmount.toLocaleString('ko-KR') }}원
        </strong>
      </button>
      <article
        v-for="record in listRecords"
        :key="record.id"
        class="relative flex min-h-16 w-full items-center rounded-xl border border-[var(--color-border)] bg-white px-3 shadow-[0_4px_14px_rgb(55_96_118_/_4%)]"
      >
        <button
          class="flex min-h-16 min-w-0 flex-1 items-center text-left active:bg-[var(--color-unselected-background)]"
          type="button"
          @click="openRecord(record.id)"
        >
          <span class="min-w-0 flex-1">
            <strong class="block truncate text-sm">{{ record.title }}</strong>
            <time class="mt-1 block text-xs text-[var(--color-text-secondary)]">{{
              formatDate(record.date)
            }}</time>
          </span>
          <strong class="ml-3 shrink-0 text-sm text-[var(--color-selected-text)]">
            {{ record.amount.toLocaleString('ko-KR') }}원
          </strong>
        </button>
        <div
          v-if="!isCapsuleReleased"
          class="relative shrink-0"
          @focusout="closeRecordMenuOnFocusOut"
        >
          <button
            class="grid size-7 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-black/5"
            type="button"
            :aria-label="`${record.title} 관리 메뉴`"
            :aria-expanded="openRecordMenuId === record.id"
            aria-haspopup="menu"
            @click="openRecordMenuId = openRecordMenuId === record.id ? null : record.id"
          >
            <EllipsisVertical :size="18" :stroke-width="2.3" aria-hidden="true" />
          </button>

          <Transition
            enter-active-class="transition duration-150 ease-out"
            enter-from-class="-translate-y-1 opacity-0"
            leave-active-class="transition duration-100 ease-in"
            leave-to-class="-translate-y-1 opacity-0"
          >
            <div
              v-if="openRecordMenuId === record.id"
              class="absolute top-[calc(100%+4px)] right-0 z-20 w-[128px] overflow-hidden rounded-[12px] border border-[#dce8ee] bg-white p-1.5 shadow-[0_10px_28px_rgba(45,77,94,0.16)]"
              role="menu"
            >
              <button
                class="flex h-10 w-full items-center gap-2 rounded-[8px] px-2.5 text-left text-[12px] font-bold text-[#ef4f5f] active:bg-[#fff1f3]"
                type="button"
                role="menuitem"
                @click="requestRecordDeletion(record)"
              >
                <Trash2 :size="15" :stroke-width="2.1" aria-hidden="true" />
                기록 삭제
              </button>
            </div>
          </Transition>
        </div>
      </article>

      <div
        v-if="listRecords.length === 0"
        class="flex min-h-[260px] flex-col items-center justify-center rounded-2xl border border-dashed border-[#cfe5ef] bg-[#f7fbfd] px-6 text-center"
      >
        <strong class="mt-4 text-base">아직 담긴 추억이 없어요</strong>
        <p class="mt-1.5 text-xs leading-relaxed text-[var(--color-text-secondary)]">
          타임 캡슐에 첫 번째 금융 기록과<br />소중한 순간을 남겨보세요.
        </p>
        <button
          class="mt-5 min-h-11 rounded-xl bg-[var(--color-brand-primary)] px-5 text-sm font-bold text-white active:bg-[var(--color-brand-primary-pressed)]"
          type="button"
          @click="createFirstRecord"
        >
          첫 기록 만들기
        </button>
      </div>
    </section>

    <section
      v-else
      class="min-h-0 flex-1 snap-y snap-mandatory overflow-y-auto overscroll-contain scroll-smooth scroll-pt-2 px-5 pt-2 pb-6"
    >
      <div class="space-y-12">
        <article
          v-for="month in months"
          :id="`calendar-month-${month.year}-${month.month}`"
          :key="`${month.year}-${month.month}`"
          class="snap-start snap-always scroll-mt-2"
        >
          <h2 class="text-center text-base font-bold">{{ month.month }}월</h2>
          <div
            class="mt-5 grid grid-cols-7 text-center text-xs font-bold text-[var(--color-text-primary)]"
          >
            <span v-for="dayName in weekDays" :key="dayName" class="py-2">{{ dayName }}</span>
          </div>
          <div class="grid grid-cols-7 gap-y-1 text-center">
            <div
              v-for="(day, index) in getMonthCells(month.year, month.month)"
              :key="`${month.month}-${index}`"
              class="relative flex aspect-square items-center justify-center"
            >
              <button
                v-if="day && getRecord(month.year, month.month, day)"
                class="relative grid size-10 place-items-center overflow-hidden rounded-full text-sm font-semibold text-white shadow-sm"
                :class="[
                  getRecord(month.year, month.month, day)!.photos.length ? '' : 'bg-[#79ccef]',
                ]"
                type="button"
                :aria-label="`${day}일 ${getRecord(month.year, month.month, day)!.title}`"
                @click="openRecord(getRecord(month.year, month.month, day)!.id)"
              >
                <img
                  v-if="getRecord(month.year, month.month, day)!.photos.length"
                  class="absolute inset-0 size-full object-cover"
                  :src="getRecord(month.year, month.month, day)!.thumbnail"
                  alt=""
                  aria-hidden="true"
                />
                <span
                  class="absolute inset-0 grid place-items-center text-xs"
                  :class="
                    getRecord(month.year, month.month, day)!.photos.length
                      ? 'bg-black/15'
                      : 'bg-transparent'
                  "
                  >{{ day }}</span
                >
              </button>
              <span
                v-else-if="day"
                class="grid size-9 place-items-center rounded-full text-sm font-semibold"
                :class="[
                  isToday(month.year, month.month, day)
                    ? 'bg-[#e85b61] text-white'
                    : 'text-[#87919e]',
                ]"
              >
                {{ day }}
              </span>
              <span
                v-if="day && isToday(month.year, month.month, day)"
                class="pointer-events-none absolute top-[calc(50%+23px)] text-[9px] leading-none font-bold text-[#e85b61]"
                aria-hidden="true"
              >
                오늘
              </span>
            </div>
          </div>
        </article>
      </div>
    </section>

    <Teleport to="body">
      <Transition name="delete-sheet">
        <div
          v-if="recordToDelete"
          class="fixed inset-0 z-[var(--z-index-overlay)] flex items-end justify-center bg-black/35"
          role="presentation"
          @click.self="closeDeleteSheet"
        >
          <section
            class="max-h-[calc(100dvh-16px)] w-full max-w-[var(--app-max-width)] overflow-y-auto overscroll-contain rounded-t-[28px] bg-white px-5 pt-3 pb-[calc(24px+env(safe-area-inset-bottom))] shadow-[0_-14px_38px_rgb(0_0_0_/_14%)]"
            role="dialog"
            aria-modal="true"
            aria-labelledby="delete-record-title"
          >
            <span class="mx-auto block h-1 w-10 rounded-full bg-[#d8e0e4]" aria-hidden="true"></span>
            <header class="mt-3 flex items-center justify-between">
              <div>
                <p class="text-xs font-bold text-[#e95762]">기록 삭제</p>
                <h2 id="delete-record-title" class="mt-1 text-xl font-extrabold">
                  이 기록을 삭제할까요?
                </h2>
              </div>
              <button
                class="grid size-9 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[#f2f5f6]"
                type="button"
                aria-label="삭제 창 닫기"
                @click="closeDeleteSheet"
              >
                <X :size="20" aria-hidden="true" />
              </button>
            </header>

            <div class="mt-5 rounded-2xl bg-[#f6f8f9] px-4 py-4">
              <strong class="block truncate text-sm">{{ recordToDelete.title }}</strong>
              <span class="mt-1 block text-xs text-[var(--color-text-secondary)]">
                {{ formatDate(recordToDelete.date) }} · {{ recordToDelete.amount.toLocaleString('ko-KR') }}원
              </span>
            </div>
            <p class="mt-3 text-xs leading-5 text-[var(--color-text-secondary)]">
              사진과 편지를 포함한 기록이 영구 삭제되며 다시 복구할 수 없어요.
            </p>

            <div class="mt-5 grid grid-cols-2 gap-3">
              <button
                class="h-[52px] rounded-xl border border-[var(--color-border)] bg-white text-sm font-bold text-[var(--color-text-secondary)] active:bg-[#f5f7f8]"
                type="button"
                :disabled="isDeletingRecord"
                @click="closeDeleteSheet"
              >
                취소
              </button>
              <button
                class="h-[52px] rounded-xl bg-[#e95762] text-sm font-bold text-white active:bg-[#d84854] disabled:opacity-50"
                type="button"
                :disabled="isDeletingRecord"
                @click="deleteRecord"
              >
                {{ isDeletingRecord ? '삭제 중...' : '삭제하기' }}
              </button>
            </div>
          </section>
        </div>
      </Transition>
    </Teleport>
  </main>
</template>

<style scoped>
.time-capsule-list--leaving {
  transform: translateX(-18px);
  opacity: 0;
  transition:
    transform 150ms cubic-bezier(0.25, 0.8, 0.25, 1),
    opacity 120ms ease-out;
}

.delete-sheet-enter-active,
.delete-sheet-leave-active {
  transition: background-color 180ms ease;
}

.delete-sheet-enter-active > section,
.delete-sheet-leave-active > section {
  transition: transform 240ms cubic-bezier(0.22, 1, 0.36, 1);
}

.delete-sheet-enter-from,
.delete-sheet-leave-to {
  background-color: transparent;
}

.delete-sheet-enter-from > section,
.delete-sheet-leave-to > section {
  transform: translateY(100%);
}

@media (prefers-reduced-motion: reduce) {
  .time-capsule-list--leaving {
    transition-duration: 1ms;
  }
}
</style>
