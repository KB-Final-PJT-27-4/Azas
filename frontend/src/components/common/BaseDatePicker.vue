<script setup lang="ts">
import { CalendarDays, ChevronDown, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { computed, onBeforeUnmount, onMounted, ref, useId } from 'vue'

type CalendarView = 'days' | 'months' | 'years'
type SelectionMode = 'date' | 'month'

const props = withDefaults(
  defineProps<{
    modelValue: string
    label?: string
    placeholder?: string
    disabled?: boolean
    minYear?: number
    maxYear?: number
    selectionMode?: SelectionMode
  }>(),
  {
    label: undefined,
    placeholder: '날짜를 선택해주세요',
    disabled: false,
    minYear: 1900,
    maxYear: 2100,
    selectionMode: 'date',
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const pickerId = `date-picker-${useId()}`
const pickerRoot = ref<HTMLElement | null>(null)
const isOpen = ref(false)
const calendarView = ref<CalendarView>('days')
const visibleMonth = ref(new Date())
const yearPageStart = ref(new Date().getFullYear() - 5)
const weekDays = ['일', '월', '화', '수', '목', '금', '토']
const monthOptions = Array.from({ length: 12 }, (_, index) => index)

const formatDateValue = (date: Date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatMonthValue = (date: Date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
}

const selectedDate = computed(() => {
  const match = /^(\d{4})-(\d{2})(?:-(\d{2}))?$/.exec(props.modelValue)
  if (!match) return null
  return new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3] ?? 1))
})

const formattedDate = computed(() => {
  if (!selectedDate.value) return ''
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'long',
    ...(props.selectionMode === 'date' ? { day: 'numeric' } : {}),
  }).format(selectedDate.value)
})

const calendarDays = computed(() => {
  const year = visibleMonth.value.getFullYear()
  const month = visibleMonth.value.getMonth()
  const firstDay = new Date(year, month, 1).getDay()
  const lastDate = new Date(year, month + 1, 0).getDate()

  return [
    ...Array.from({ length: firstDay }, () => null),
    ...Array.from({ length: lastDate }, (_, index) => new Date(year, month, index + 1)),
  ]
})

const visibleYears = computed(() =>
  Array.from({ length: 12 }, (_, index) => yearPageStart.value + index),
)
const yearRangeLabel = computed(() => `${yearPageStart.value}년 – ${yearPageStart.value + 11}년`)

const openPicker = () => {
  if (props.disabled) return
  visibleMonth.value = selectedDate.value ?? new Date()
  yearPageStart.value = visibleMonth.value.getFullYear() - 5
  calendarView.value = props.selectionMode === 'month' ? 'months' : 'days'
  isOpen.value = !isOpen.value
}

const moveMonth = (offset: number) => {
  visibleMonth.value = new Date(
    visibleMonth.value.getFullYear(),
    visibleMonth.value.getMonth() + offset,
    1,
  )
}

const showMonthPicker = () => {
  calendarView.value = 'months'
}

const showYearPicker = () => {
  yearPageStart.value = visibleMonth.value.getFullYear() - 5
  calendarView.value = 'years'
}

const moveYearPage = (offset: number) => {
  yearPageStart.value += offset * 12
}

const selectYear = (year: number) => {
  visibleMonth.value = new Date(year, visibleMonth.value.getMonth(), 1)
  calendarView.value = props.selectionMode === 'month' ? 'months' : 'days'
}

const selectMonth = (month: number) => {
  visibleMonth.value = new Date(visibleMonth.value.getFullYear(), month, 1)
  if (props.selectionMode === 'month') {
    emit('update:modelValue', formatMonthValue(visibleMonth.value))
    isOpen.value = false
    return
  }
  calendarView.value = 'days'
}

const selectDate = (date: Date) => {
  emit('update:modelValue', formatDateValue(date))
  isOpen.value = false
}

const isSelectedDate = (date: Date) => formatDateValue(date) === props.modelValue
const isToday = (date: Date) => formatDateValue(date) === formatDateValue(new Date())

const closeOnOutsideClick = (event: PointerEvent) => {
  if (!pickerRoot.value?.contains(event.target as Node)) isOpen.value = false
}

onMounted(() => document.addEventListener('pointerdown', closeOnOutsideClick))
onBeforeUnmount(() => document.removeEventListener('pointerdown', closeOnOutsideClick))
</script>

<template>
  <div ref="pickerRoot" class="relative grid gap-3">
    <span v-if="label" :id="`${pickerId}-label`" class="text-base font-bold">{{ label }}</span>
    <button
      class="flex h-14 items-center justify-between rounded-xl border border-[var(--color-border)] bg-[var(--color-surface)] px-4 text-left text-lg outline-none transition-colors focus:border-[var(--color-brand-primary-pressed)] focus:ring-2 focus:ring-[var(--color-selected-background)] disabled:cursor-not-allowed disabled:border-[var(--color-disabled-border)] disabled:bg-[var(--color-disabled-background)] disabled:text-[var(--color-unselected-text)]"
      type="button"
      :disabled="disabled"
      :aria-labelledby="label ? `${pickerId}-label` : undefined"
      :aria-label="label ? undefined : placeholder"
      :aria-expanded="isOpen"
      @click="openPicker"
    >
      <span :class="formattedDate ? '' : 'text-[var(--color-text-secondary)]'">
        {{ formattedDate || placeholder }}
      </span>
      <CalendarDays :size="22" class="shrink-0 text-[var(--color-text-secondary)]" />
    </button>

    <div
      v-if="isOpen"
      class="absolute top-full right-0 left-0 z-20 mt-2 min-h-[360px] rounded-2xl border border-[var(--color-border)] bg-[var(--color-surface)] p-5 shadow-[0_16px_40px_rgb(29_68_89_/_16%)]"
      role="dialog"
      aria-label="날짜 선택"
    >
      <template v-if="calendarView === 'days'">
        <div class="mb-4 flex items-center justify-between gap-2">
          <button
            class="grid size-10 shrink-0 place-items-center rounded-full text-[var(--color-text-secondary)] hover:bg-[var(--color-selected-background)]"
            type="button"
            aria-label="이전 달"
            @click="moveMonth(-1)"
          >
            <ChevronLeft :size="21" />
          </button>
          <div class="flex items-center gap-0">
            <button
              class="flex h-10 items-center gap-1 rounded-xl px-2 text-lg font-bold hover:bg-[var(--color-selected-background)] hover:text-[var(--color-selected-text)]"
              type="button"
              aria-label="연도 선택 화면 열기"
              @click="showYearPicker"
            >
              {{ visibleMonth.getFullYear() }}년
            </button>
            <button
              class="flex h-10 items-center gap-1 rounded-xl px-2 text-lg font-bold transition-colors hover:bg-[var(--color-surface-muted)]"
              type="button"
              aria-label="월 선택 화면 열기"
              @click="showMonthPicker"
            >
              {{ visibleMonth.getMonth() + 1 }}월
            </button>
          </div>
          <button
            class="grid size-10 shrink-0 place-items-center rounded-full text-[var(--color-text-secondary)] hover:bg-[var(--color-selected-background)]"
            type="button"
            aria-label="다음 달"
            @click="moveMonth(1)"
          >
            <ChevronRight :size="21" />
          </button>
        </div>

        <div class="grid grid-cols-7 text-center">
          <span
            v-for="(weekDay, index) in weekDays"
            :key="weekDay"
            class="pb-2 text-sm font-bold"
            :class="
              index === 0
                ? 'text-[#ef6b6b]'
                : index === 6
                  ? 'text-[var(--color-selected-text)]'
                  : 'text-[var(--color-text-secondary)]'
            "
          >
            {{ weekDay }}
          </span>
          <div
            v-for="(date, index) in calendarDays"
            :key="date?.toISOString() ?? `blank-${index}`"
            class="grid aspect-square place-items-center"
          >
            <button
              v-if="date"
              class="grid size-10 place-items-center rounded-full text-sm font-semibold hover:bg-[var(--color-selected-background)]"
              :class="[
                isSelectedDate(date)
                  ? 'bg-[var(--color-brand-primary)] text-white hover:bg-[var(--color-brand-primary-pressed)]'
                  : date.getDay() === 0
                    ? 'text-[#ef6b6b]'
                    : date.getDay() === 6
                      ? 'text-[var(--color-selected-text)]'
                      : '',
                isToday(date) && !isSelectedDate(date)
                  ? 'ring-1 ring-inset ring-[var(--color-brand-primary)]'
                  : '',
              ]"
              type="button"
              :aria-label="`${date.getFullYear()}년 ${date.getMonth() + 1}월 ${date.getDate()}일`"
              :aria-pressed="isSelectedDate(date)"
              @click="selectDate(date)"
            >
              {{ date.getDate() }}
            </button>
          </div>
        </div>
      </template>

      <template v-else-if="calendarView === 'years'">
        <div class="mb-6 flex items-center justify-between">
          <button
            class="grid size-10 place-items-center rounded-full text-[var(--color-text-secondary)] hover:bg-[var(--color-selected-background)]"
            type="button"
            aria-label="이전 12년"
            @click="moveYearPage(-1)"
          >
            <ChevronLeft :size="21" />
          </button>
          <strong class="text-lg">{{ yearRangeLabel }}</strong>
          <button
            class="grid size-10 place-items-center rounded-full text-[var(--color-text-secondary)] hover:bg-[var(--color-selected-background)]"
            type="button"
            aria-label="다음 12년"
            @click="moveYearPage(1)"
          >
            <ChevronRight :size="21" />
          </button>
        </div>
        <div class="grid grid-cols-3 gap-3">
          <button
            v-for="year in visibleYears"
            :key="year"
            class="h-14 rounded-xl font-bold transition-colors hover:bg-[var(--color-selected-background)] hover:text-[var(--color-selected-text)] disabled:cursor-not-allowed disabled:text-[var(--color-unselected-text)] disabled:hover:bg-transparent"
            :class="
              year === visibleMonth.getFullYear()
                ? 'bg-[var(--color-brand-primary)] text-white hover:bg-[var(--color-brand-primary-pressed)] hover:text-white'
                : 'bg-[var(--color-surface-muted)]'
            "
            type="button"
            :disabled="year < minYear || year > maxYear"
            @click="selectYear(year)"
          >
            {{ year }}년
          </button>
        </div>
      </template>

      <template v-else>
        <div class="mb-6 flex h-10 items-center justify-center">
          <button
            class="flex h-10 items-center gap-1 rounded-xl px-4 text-lg font-bold transition-colors hover:bg-[var(--color-selected-background)] hover:text-[var(--color-selected-text)]"
            type="button"
            aria-label="연도 선택 화면 열기"
            @click="showYearPicker"
          >
            {{ visibleMonth.getFullYear() }}년 월 선택
            <ChevronDown :size="17" />
          </button>
        </div>
        <div class="grid grid-cols-3 gap-3">
          <button
            v-for="month in monthOptions"
            :key="month"
            class="h-14 rounded-xl font-bold transition-colors hover:bg-[var(--color-selected-background)] hover:text-[var(--color-selected-text)]"
            :class="
              month === visibleMonth.getMonth()
                ? 'bg-[var(--color-brand-primary)] text-white hover:bg-[var(--color-brand-primary-pressed)] hover:text-white'
                : 'bg-[var(--color-surface-muted)]'
            "
            type="button"
            @click="selectMonth(month)"
          >
            {{ month + 1 }}월
          </button>
        </div>
      </template>
    </div>
  </div>
</template>
