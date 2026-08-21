<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRoute, useRouter, type RouteLocationRaw } from 'vue-router'
import { ChevronDown } from 'lucide-vue-next'
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

const accountId = computed(() => String(route.params.capsuleListId ?? '1'))
const account = ref({ name: '타임캡슐', description: '아이의 성장 순간과 금융 기록을 모아보세요.', totalSavedAmount: 0, records: [] as Array<{ id: number; title: string; date: string; amount: number; thumbnail: string; photos: Array<{ src: string; orientation: 'portrait'; type: 'image' }> }> })
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
  } catch (error) {
    showToast(getApiErrorMessage(error, '타임캡슐 기록을 불러오지 못했습니다.'), 'error')
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

const openRecord = (recordId: number) =>
  navigateForward(`/time-capsules/${accountId.value}/${recordId}`)

const createFirstRecord = () =>
  navigateForward({
    name: 'TimeCapsuleCreate',
    query: {
      account: accountId.value,
      ...(typeof route.query.openDate === 'string' ? { openDate: route.query.openDate } : {}),
    },
  })

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
      <button
        v-for="record in listRecords"
        :key="record.id"
        class="flex min-h-16 w-full items-center rounded-xl border border-[var(--color-border)] bg-white px-4 text-left active:bg-[var(--color-unselected-background)]"
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

@media (prefers-reduced-motion: reduce) {
  .time-capsule-list--leaving {
    transition-duration: 1ms;
  }
}
</style>
