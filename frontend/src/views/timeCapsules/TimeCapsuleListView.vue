<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChevronDown, ChevronRight } from 'lucide-vue-next'
import capsulePigImage from '@/assets/images/timeCapsules/archive/capsule-pig.png'
import memoryCafe1 from '@/assets/images/timeCapsules/thumbnails/light.PNG'
import memoryCafe2 from '@/assets/images/timeCapsules/thumbnails/v.PNG'

type CapsuleRecord = {
  id: number
  title: string
  date: string
  amount: number
  thumbnail: string
}

type CapsuleAccount = {
  name: string
  description: string
  records: CapsuleRecord[]
}

const route = useRoute()
const router = useRouter()
const activeTab = ref<'list' | 'calendar'>('list')
const today = new Date()
const currentYear = today.getFullYear()
const currentMonth = today.getMonth() + 1
const selectedYear = ref(currentYear)

const capsuleAccounts: Record<string, CapsuleAccount> = {
  '1': {
    name: '아이사랑적금',
    description: '깨비의 성장 순간과 금융 기록을 모아보세요.',
    records: [
      { id: 101, title: '첫 생일 축하', date: '2026-05-04', amount: 150000, thumbnail: memoryCafe1 },
      { id: 102, title: '가족 여행', date: '2026-05-09', amount: 80000, thumbnail: memoryCafe2 },
      { id: 103, title: '첫 어린이집', date: '2026-05-23', amount: 100000, thumbnail: memoryCafe1 },
      { id: 104, title: '첫 놀이터', date: '2026-05-30', amount: 100000, thumbnail: memoryCafe2 },
      { id: 105, title: '첫 걸음마', date: '2026-05-31', amount: 150000, thumbnail: memoryCafe1 },
      { id: 106, title: '바닷가 나들이', date: '2026-06-07', amount: 80000, thumbnail: memoryCafe2 },
      { id: 107, title: '동물원에 간 날', date: '2026-06-11', amount: 70000, thumbnail: memoryCafe1 },
      { id: 108, title: '즐거운 물놀이', date: '2026-06-14', amount: 50000, thumbnail: memoryCafe2 },
      { id: 109, title: '할머니와의 하루', date: '2026-06-28', amount: 90000, thumbnail: memoryCafe1 },
      { id: 110, title: '크리스마스 선물', date: '2025-12-25', amount: 100000, thumbnail: memoryCafe2 },
      { id: 111, title: '새해 첫 저축', date: '2027-01-02', amount: 120000, thumbnail: memoryCafe1 },
    ],
  },
  '2': {
    name: '우리사랑적금',
    description: '우리 가족의 행복한 추억과 저축 기록이에요.',
    records: [
      { id: 201, title: '가족 캠핑', date: '2026-05-03', amount: 120000, thumbnail: memoryCafe2 },
      { id: 202, title: '엄마와 쿠키 만들기', date: '2026-05-17', amount: 50000, thumbnail: memoryCafe1 },
      { id: 203, title: '아빠와 자전거', date: '2026-05-24', amount: 70000, thumbnail: memoryCafe2 },
      { id: 204, title: '우리 가족 사진', date: '2026-06-06', amount: 100000, thumbnail: memoryCafe1 },
      { id: 205, title: '첫 수영 수업', date: '2026-06-20', amount: 60000, thumbnail: memoryCafe2 },
      { id: 206, title: '겨울 가족 여행', date: '2025-12-14', amount: 110000, thumbnail: memoryCafe1 },
      { id: 207, title: '새해 가족 저축', date: '2027-01-10', amount: 150000, thumbnail: memoryCafe2 },
    ],
  },
}

const accountId = computed(() => String(route.params.capsuleListId ?? '1'))
const account = computed(() => capsuleAccounts[accountId.value] ?? capsuleAccounts['1']!)
const totalAmount = computed(() => account.value.records.reduce((sum, record) => sum + record.amount, 0))
const listRecords = computed(() => [...account.value.records].sort((a, b) => b.date.localeCompare(a.date)))
const availableYears = computed(() =>
  [...new Set(account.value.records.map(({ date }) => Number(date.slice(0, 4))))].sort((a, b) => b - a),
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

const getRecord = (year: number, month: number, day: number | null) => {
  if (!day) return undefined
  const date = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`
  return account.value.records.find((record) => record.date === date)
}

const isToday = (year: number, month: number, day: number | null) =>
  year === currentYear && month === currentMonth && day === today.getDate()

const formatDate = (date: string) => date.replaceAll('-', '.')
const openRecord = (recordId: number) => router.push(`/time-capsules/${accountId.value}/${recordId}`)

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
  <main class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-white">
    <section class="px-5">
      <div class="flex items-center">
        <div class="min-w-0 flex-1">
          <h1 class="truncate text-[24px] font-bold tracking-[-0.025em]">{{ account.name }}</h1>
          <p class="mt-1 text-xs text-[var(--color-text-secondary)]">{{ account.description }}</p>
        </div>
        <img
          class="h-28 w-32 translate-y-4 shrink-0 origin-right scale-[1.3] object-contain"
          :src="capsulePigImage"
          alt="타임캡슐 저금통"
        />
      </div>

      <div class="flex gap-4 border-b border-[var(--color-border)]" role="tablist">
        <button
          class="relative py-3 text-sm font-bold"
          :class="activeTab === 'list' ? 'text-[var(--color-selected-text)] after:absolute after:right-0 after:bottom-0 after:left-0 after:h-0.5 after:bg-[var(--color-brand-primary)]' : 'text-[var(--color-text-secondary)]'"
          type="button"
          role="tab"
          :aria-selected="activeTab === 'list'"
          @click="activeTab = 'list'"
        >
          리스트
        </button>
        <button
          class="relative py-3 text-sm font-bold"
          :class="activeTab === 'calendar' ? 'text-[var(--color-selected-text)] after:absolute after:right-0 after:bottom-0 after:left-0 after:h-0.5 after:bg-[var(--color-brand-primary)]' : 'text-[var(--color-text-secondary)]'"
          type="button"
          role="tab"
          :aria-selected="activeTab === 'calendar'"
          @click="showCalendar"
        >
          캘린더
        </button>
      </div>
    </section>

    <section v-if="activeTab === 'list'" class="space-y-3 px-5 py-4">
      <button
        v-for="record in listRecords"
        :key="record.id"
        class="flex min-h-16 w-full items-center rounded-xl border border-[var(--color-border)] bg-white px-4 text-left active:bg-[var(--color-unselected-background)]"
        type="button"
        @click="openRecord(record.id)"
      >
        <span class="min-w-0 flex-1">
          <strong class="block truncate text-sm">{{ record.title }}</strong>
          <time class="mt-1 block text-xs text-[var(--color-text-secondary)]">{{ formatDate(record.date) }}</time>
        </span>
        <strong class="ml-3 shrink-0 text-sm text-[var(--color-selected-text)]">
          {{ record.amount.toLocaleString('ko-KR') }}원
        </strong>
      </button>

      <button
        class="flex min-h-16 w-full items-center rounded-xl border border-[#d5edf8] bg-[#eaf8ff] px-4 text-left"
        type="button"
      >
        <span class="min-w-0 flex-1">
          <span class="block text-xs text-[var(--color-text-secondary)]">총 저축 금액</span>
          <strong class="mt-1 block text-base text-[var(--color-selected-text)]">{{ totalAmount.toLocaleString('ko-KR') }}원</strong>
        </span>
        <ChevronRight :size="22" class="text-[var(--color-selected-text)]" />
      </button>
    </section>

    <section v-else class="px-5 py-6">
      <div class="sticky top-[calc(var(--app-header-height)+env(safe-area-inset-top))] z-[5] mb-7 flex justify-center bg-white/95 py-2 backdrop-blur-sm">
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

      <div class="space-y-12">
      <article
        v-for="month in months"
        :id="`calendar-month-${month.year}-${month.month}`"
        :key="`${month.year}-${month.month}`"
        class="scroll-mt-32"
      >
        <h2 class="text-center text-base font-bold">{{ month.month }}월</h2>
        <div class="mt-5 grid grid-cols-7 text-center text-xs font-bold text-[var(--color-text-primary)]">
          <span v-for="dayName in weekDays" :key="dayName" class="py-2">{{ dayName }}</span>
        </div>
        <div class="grid grid-cols-7 gap-y-1 text-center">
          <div
            v-for="(day, index) in getMonthCells(month.year, month.month)"
            :key="`${month.month}-${index}`"
            class="grid aspect-square place-items-center"
          >
            <button
              v-if="day && getRecord(month.year, month.month, day)"
              class="relative grid size-10 place-items-center overflow-hidden rounded-full text-sm font-black text-white shadow-sm"
              type="button"
              :aria-label="`${day}일 ${getRecord(month.year, month.month, day)!.title}`"
              @click="openRecord(getRecord(month.year, month.month, day)!.id)"
            >
              <img
                class="absolute inset-0 size-full object-cover"
                :src="getRecord(month.year, month.month, day)!.thumbnail"
                alt=""
                aria-hidden="true"
              />
              <span class="absolute inset-0 grid place-items-center bg-black/15 text-xs">{{ day }}</span>
            </button>
            <span
              v-else-if="day"
              class="grid size-9 place-items-center rounded-full text-sm font-semibold"
              :class="isToday(month.year, month.month, day) ? 'bg-[var(--color-brand-primary)] text-white' : 'text-[#87919e]'"
            >
              {{ day }}
            </span>
          </div>
        </div>
      </article>
      </div>
    </section>
  </main>
</template>
