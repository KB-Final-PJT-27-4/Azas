<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Check, Plus, X } from 'lucide-vue-next'
import { useRouter, type RouteLocationRaw } from 'vue-router'
import calendarImage from '@/assets/images/timeCapsules/archive/calendar.png'
import lockImage from '@/assets/images/timeCapsules/archive/lock.png'
import archiveBackgroundImage from '@/assets/images/timeCapsules/archive/new-bg.png'
import openImage from '@/assets/images/timeCapsules/archive/open.png'
import bornBabyPigImage from '@/assets/images/timeCapsules/archive/born-baby-pig.png'
import avocadoImage from '@/assets/images/pregnancy/avocado.png'
import bananaImage from '@/assets/images/pregnancy/banana.png'
import blueberryImage from '@/assets/images/pregnancy/blueberry.png'
import coconutImage from '@/assets/images/pregnancy/coconut.png'
import eggplantImage from '@/assets/images/pregnancy/eggplant.png'
import lemonImage from '@/assets/images/pregnancy/lemon.png'
import mangoImage from '@/assets/images/pregnancy/mango.png'
import melonImage from '@/assets/images/pregnancy/melon.png'
import peachImage from '@/assets/images/pregnancy/peach.png'
import pineappleImage from '@/assets/images/pregnancy/pineapple.png'
import strawberryImage from '@/assets/images/pregnancy/strawberry.png'
import watermelonImage from '@/assets/images/pregnancy/watermelon.png'
import BaseDatePicker from '@/components/common/BaseDatePicker.vue'
import { useToast } from '@/composables/useToast'
import { api, getApiErrorMessage } from '@/api'
import { resolveCurrentChildId } from '@/api/context'

const router = useRouter()
const { showToast } = useToast()
const isPageLeaving = ref(false)
const isLoading = ref(true)
const isFreeCapsuleSheetOpen = ref(false)
const freeCapsuleSheetOffset = ref(0)
const isFreeCapsuleSheetDragging = ref(false)
let freeCapsuleDragStartY = 0
let freeCapsuleDragStartTime = 0
const freeCapsuleOpenDate = ref('')
const isFreeCapsuleCreated = ref(false)
const registration = ref<{ birthDate: string; childName: string } | null>(null)
const childId = ref<number | null>(null)
const demandAccountId = ref<number | null>(null)
const capsuleAccounts = ref<Array<{ id: number; name: string; createdAt: string; savedAmount: number; dDay?: number; isFree?: boolean }>>([])
const archiveChildName = computed(() => registration.value?.childName.trim() || '아이')

const pregnancyStages = [
  { week: 7, fruit: '블루베리', image: blueberryImage, color: '#777bdc' },
  { week: 9, fruit: '딸기', image: strawberryImage, color: '#ef6d73' },
  { week: 14, fruit: '레몬', image: lemonImage, color: '#e4b52b' },
  { week: 16, fruit: '아보카도', image: avocadoImage, color: '#74a34b' },
  { week: 20, fruit: '바나나', image: bananaImage, color: '#e9b52e' },
  { week: 20, fruit: '복숭아', image: peachImage, color: '#ed8c96' },
  { week: 24, fruit: '메론', image: melonImage, color: '#8db958' },
  { week: 28, fruit: '가지', image: eggplantImage, color: '#7753c6' },
  { week: 29, fruit: '망고', image: mangoImage, color: '#e39a33' },
  { week: 31, fruit: '파인애플', image: pineappleImage, color: '#dca82a' },
  { week: 35, fruit: '코코넛', image: coconutImage, color: '#a26e46' },
  { week: 39, fruit: '수박', image: watermelonImage, color: '#599952' },
] as const

const today = new Date()
today.setHours(0, 0, 0, 0)

const pregnancyGrowth = computed(() => {
  if (!registration.value?.birthDate) return null

  const [year, month, day] = registration.value.birthDate.split('-').map(Number)
  if (!year || !month || !day) return null

  const dueDate = new Date(year, month - 1, day)
  dueDate.setHours(0, 0, 0, 0)
  if (dueDate <= today) return null

  const remainingDays = Math.ceil((dueDate.getTime() - today.getTime()) / 86_400_000)
  const currentWeek = Math.max(1, Math.min(40, 40 - Math.ceil(remainingDays / 7)))
  const nearestDistance = Math.min(
    ...pregnancyStages.map(({ week }) => Math.abs(week - currentWeek)),
  )
  const nearestStages = pregnancyStages.filter(
    ({ week }) => Math.abs(week - currentWeek) === nearestDistance,
  )
  const stage =
    nearestStages.length === 1
      ? nearestStages[0]!
      : nearestStages[(registration.value.childName.codePointAt(0) ?? 0) % nearestStages.length]!

  return { ...stage, currentWeek }
})

const bornBabyGrowth = computed(() => {
  if (!registration.value?.birthDate) return null

  const [year, month, day] = registration.value.birthDate.split('-').map(Number)
  if (!year || !month || !day) return null

  const birthDate = new Date(year, month - 1, day)
  birthDate.setHours(0, 0, 0, 0)
  if (birthDate > today) return null

  const daysSinceBirth = Math.floor((today.getTime() - birthDate.getTime()) / 86_400_000) + 1
  return {
    daysSinceBirth,
    childName: registration.value.childName || '아이',
  }
})
const todayKey = [
  today.getFullYear(),
  String(today.getMonth() + 1).padStart(2, '0'),
  String(today.getDate()).padStart(2, '0'),
].join('.')

const isReleased = (releaseDate: string) => releaseDate <= todayKey

const calculateDday = (releaseDate: string) => {
  const [year, month, day] = releaseDate.replaceAll('.', '-').split('-').map(Number)
  if (!year || !month || !day) return null

  const releaseDay = new Date(year, month - 1, day)
  releaseDay.setHours(0, 0, 0, 0)
  return Math.ceil((releaseDay.getTime() - today.getTime()) / 86_400_000)
}

const nearestCapsule = computed(() => {
  const upcomingCapsules = capsuleAccounts.value
    .map((capsule) => ({
      ...capsule,
      resolvedDday: capsule.dDay ?? calculateDday(capsule.createdAt),
    }))
    .filter(
      (capsule): capsule is typeof capsule & { resolvedDday: number } =>
        capsule.resolvedDday !== null && capsule.resolvedDday >= 0,
    )
    .sort((a, b) => a.resolvedDday - b.resolvedDday)

  return upcomingCapsules[0] ?? null
})

const nearestCapsuleDdayLabel = computed(() => {
  if (!nearestCapsule.value) return ''
  return nearestCapsule.value.resolvedDday === 0
    ? 'D-Day'
    : `D-${nearestCapsule.value.resolvedDday}`
})

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
const closeFreeCapsuleSheet = () => {
  freeCapsuleSheetOffset.value = 0
  isFreeCapsuleSheetDragging.value = false
  isFreeCapsuleSheetOpen.value = false
}

const startFreeCapsuleSheetDrag = (event: PointerEvent) => {
  if (event.button !== 0) return
  freeCapsuleDragStartY = event.clientY
  freeCapsuleDragStartTime = performance.now()
  isFreeCapsuleSheetDragging.value = true
  ;(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId)
}

const moveFreeCapsuleSheetDrag = (event: PointerEvent) => {
  if (!isFreeCapsuleSheetDragging.value) return
  freeCapsuleSheetOffset.value = Math.max(0, event.clientY - freeCapsuleDragStartY)
}

const endFreeCapsuleSheetDrag = (event: PointerEvent) => {
  if (!isFreeCapsuleSheetDragging.value) return
  const elapsed = Math.max(performance.now() - freeCapsuleDragStartTime, 1)
  const velocity = freeCapsuleSheetOffset.value / elapsed
  isFreeCapsuleSheetDragging.value = false

  if (freeCapsuleSheetOffset.value >= 72 || velocity >= 0.45) {
    closeFreeCapsuleSheet()
    return
  }

  freeCapsuleSheetOffset.value = 0
  const target = event.currentTarget as HTMLElement
  if (target.hasPointerCapture(event.pointerId)) target.releasePointerCapture(event.pointerId)
}

const confirmFreeCapsule = async () => {
  if (!freeCapsuleOpenDate.value) {
    showToast('오픈 날짜를 선택해주세요.', 'error')
    return
  }
  if (!childId.value || !demandAccountId.value) {
    showToast('연결된 입출금계좌가 필요합니다.', 'error')
    return
  }
  try {
    const { data } = await api.createTimeCapsuleUsingPOST(childId.value, {
      financial_account_id: demandAccountId.value,
      release_date: freeCapsuleOpenDate.value,
    })
    capsuleAccounts.value.push({
      id: data.time_capsule_id ?? 0,
      name: data.title ?? '입출금계좌 타임캡슐',
      createdAt: data.release_date?.replaceAll('-', '.') ?? formattedFreeOpenDate.value,
      savedAmount: data.total_saved_amount ?? 0,
      dDay: calculateDday(data.release_date ?? freeCapsuleOpenDate.value) ?? undefined,
      isFree: true,
    })
    isFreeCapsuleCreated.value = true
    closeFreeCapsuleSheet()
    showToast('입출금계좌의 오픈 날짜를 설정했습니다.', 'success')
  } catch (error) {
    showToast(getApiErrorMessage(error, '타임캡슐을 만들지 못했습니다.'), 'error')
  }
}

const navigateForward = async (to: RouteLocationRaw) => {
  if (isPageLeaving.value) return
  isPageLeaving.value = true

  try {
    await new Promise((resolve) => window.setTimeout(resolve, 150))
    await router.push(to)
  } catch {
    isPageLeaving.value = false
  }
}

const openCapsule = (capsule: { id: number; createdAt: string; isFree?: boolean }) => {
  if (!capsule.isFree) {
    navigateForward(
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
  navigateForward(
    isCapsuleReleased(capsule)
      ? `/time-capsules/${capsule.id}/open`
      : {
          name: 'TimeCapsuleList',
          params: { capsuleListId: String(capsule.id) },
          query: { openDate: capsule.createdAt.replaceAll('.', '-') },
        },
  )
}

const isCapsuleReleased = (capsule: { createdAt: string; isFree?: boolean }) =>
  capsule.isFree
    ? isFreeCapsuleCreated.value && isReleased(capsule.createdAt)
    : isReleased(capsule.createdAt)

onMounted(async () => {
  try {
    childId.value = await resolveCurrentChildId()
    const [{ data: child }, { data: capsules }, { data: accounts }] = await Promise.all([
      api.getChildUsingGET(childId.value),
      api.getTimeCapsulesUsingGET(childId.value),
      api.getChildAccountsUsingGET(childId.value),
    ])
    registration.value = {
      birthDate: child.birth_date ?? child.expected_birth_date ?? '',
      childName: child.name ?? '아이',
    }
    demandAccountId.value = accounts.accounts.find(({ account_product_type }) => account_product_type === 'DEMAND_DEPOSIT')?.account_id ?? null
    capsuleAccounts.value = (capsules.time_capsules ?? []).map((capsule) => ({
      id: capsule.time_capsule_id ?? 0,
      name: capsule.title ?? '타임캡슐',
      createdAt: capsule.release_date?.replaceAll('-', '.') ?? '오픈 날짜 설정',
      savedAmount: capsule.total_saved_amount ?? 0,
      dDay: capsule.d_day,
      isFree: capsule.account_id === demandAccountId.value,
    }))
    isFreeCapsuleCreated.value = capsuleAccounts.value.some(({ isFree }) => isFree)
  } catch (error) {
    showToast(getApiErrorMessage(error, '타임캡슐을 불러오지 못했습니다.'), 'error')
  } finally {
    isLoading.value = false
  }
})
</script>

<template>
  <main
    class="flex min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] flex-col bg-white"
    :class="isPageLeaving ? 'time-capsule-archive--leaving pointer-events-none' : ''"
  >
    <section
      class="bg-cover bg-center bg-no-repeat px-5 pt-7 pb-5"
      :style="{ backgroundImage: `url(${archiveBackgroundImage})` }"
    >
      <div v-if="isLoading" class="animate-pulse" aria-label="타임캡슐 정보 불러오는 중" aria-busy="true">
        <div class="relative min-h-[128px] overflow-hidden" aria-hidden="true">
          <span class="block h-7 w-40 rounded-lg bg-[#dde8ed]"></span>
          <span class="mt-3 block h-3.5 w-36 rounded-full bg-[#e4edf1]"></span>
          <span class="mt-2 block h-3.5 w-28 rounded-full bg-[#e4edf1]"></span>
          <span class="absolute top-0 right-0 block h-[118px] w-[108px] rounded-[34px] bg-white/55 p-3">
            <span class="block size-full rounded-[28px] bg-[#dfeaed]"></span>
          </span>
        </div>

        <article
          class="relative z-10 mt-2 flex min-h-20 translate-y-7 items-center rounded-2xl border border-[var(--color-border)] bg-white px-4 shadow-[0_8px_24px_rgba(67,139,179,0.08)]"
          aria-hidden="true"
        >
          <span class="block size-14 shrink-0 rounded-2xl bg-[#e5edf1]"></span>
          <span class="ml-3 min-w-0 flex-1">
            <span class="block h-4 w-36 max-w-full rounded-md bg-[#dfe8ec]"></span>
            <span class="mt-2 block h-3 w-24 rounded-full bg-[#e8eef1]"></span>
          </span>
          <span class="ml-3 block h-7 w-12 shrink-0 rounded-lg bg-[#dfeaf0]"></span>
        </article>
      </div>

      <div v-else>
        <div class="relative min-h-[128px] overflow-visible">
          <h1
            class="relative z-10 pt-1 text-[24px] leading-tight font-bold tracking-[-0.025em] text-[var(--color-text-primary)]"
          >
            타임캡슐 보관함
          </h1>
          <p class="relative z-10 mt-2 text-sm leading-5 text-[var(--color-text-secondary)]">
            {{ archiveChildName }}의 성장 순간과<br />금융 기록을 모아보세요.
          </p>

          <aside
            v-if="pregnancyGrowth"
            class="pregnancy-growth-card absolute top-1 right-0 h-[156px] w-[184px] translate-y-3"
            :aria-label="`임신 ${pregnancyGrowth.currentWeek}주, ${pregnancyGrowth.fruit}만큼 자랐어요`"
          >
            <div class="absolute right-[116px] bottom-14 z-10 whitespace-nowrap text-right">
              <strong
                class="block text-[20px] leading-none font-extrabold tracking-[-0.03em]"
                :style="{ color: pregnancyGrowth.color }"
              >
                {{ pregnancyGrowth.currentWeek }}주
              </strong>
              <span
                class="mt-1.5 block text-[10px] font-semibold text-[var(--color-text-secondary)]"
              >
                {{ pregnancyGrowth.fruit }}만큼 자랐어요
              </span>
            </div>
            <img
              class="pregnancy-growth-card__image absolute right-0 bottom-7 h-[142px] w-[116px] object-contain object-bottom"
              :src="pregnancyGrowth.image"
              :alt="`${pregnancyGrowth.fruit} 깨비`"
            />
          </aside>

          <aside
            v-else-if="bornBabyGrowth"
            class="absolute top-1 right-0 h-[156px] w-[184px] translate-y-3"
            :aria-label="`${bornBabyGrowth.childName}가 태어난 지 ${bornBabyGrowth.daysSinceBirth}일째`"
          >
            <div class="absolute right-[116px] bottom-14 z-10 whitespace-nowrap text-right">
              <span
                class="block text-[10px] font-semibold text-[var(--color-text-secondary)]"
              >
                태어난 지
              </span>
              <strong
                class="mt-1.5 block text-[20px] leading-none font-extrabold tracking-[-0.03em] text-[#f07f9d]"
              >
                {{ bornBabyGrowth.daysSinceBirth }}일째
              </strong>
              <span
                class="mt-1.5 block text-[10px] font-semibold text-[var(--color-text-secondary)]"
              >
                함께 자라고 있어요
              </span>
            </div>
            <img
              class="absolute right-0 bottom-7 h-[142px] w-[116px] object-contain object-bottom"
              :src="bornBabyPigImage"
              :alt="`${bornBabyGrowth.childName}의 성장 이미지`"
            />
          </aside>
        </div>

        <article
          class="relative z-10 mt-2 flex min-h-20 translate-y-7 items-center rounded-2xl border border-[var(--color-border)] bg-white px-4 shadow-[0_8px_24px_rgba(67,139,179,0.08)]"
        >
          <img class="size-14 shrink-0 object-contain" :src="calendarImage" alt="공개 예정일" />
          <div v-if="nearestCapsule" class="ml-3 min-w-0 flex-1">
            <p class="truncate text-sm font-bold text-[var(--color-text-primary)]">
              {{ nearestCapsule.name }}
            </p>
            <time
              class="mt-1 block text-xs font-medium text-[var(--color-text-secondary)]"
              :datetime="nearestCapsule.createdAt.replaceAll('.', '-')"
            >
              {{ nearestCapsule.createdAt }}
            </time>
          </div>
          <div v-else class="ml-3 min-w-0 flex-1">
            <p class="truncate text-sm font-bold text-[var(--color-text-primary)]">
              새 타임캡슐을 기다리고 있어요
            </p>
            <p class="mt-1 text-xs font-medium text-[var(--color-text-secondary)]">
              캡슐을 만들면 디데이를 알려드려요
            </p>
          </div>
          <strong
            v-if="nearestCapsule"
            class="ml-3 shrink-0 text-[30px] font-bold tracking-[-0.04em] text-[#27a9eb]"
          >
            {{ nearestCapsuleDdayLabel }}
          </strong>
          <strong v-else class="ml-3 shrink-0 text-sm font-bold text-[#27a9eb]">
            등록 전
          </strong>
        </article>
      </div>
    </section>

    <section class="grid grid-cols-2 gap-3 px-5 pt-7 pb-5" aria-label="타임캡슐 계좌 목록">
      <template v-if="isLoading">
        <article
          v-for="index in 2"
          :key="`capsule-skeleton-${index}`"
          class="rounded-2xl border border-[var(--color-border)] bg-white p-3"
          aria-hidden="true"
        >
          <span class="block aspect-[4/3] animate-pulse rounded-xl bg-[#edf2f4]"></span>
          <span class="mt-4 block h-4 w-24 max-w-full animate-pulse rounded-md bg-[#e5ecef]"></span>
          <span class="mt-2 block h-3 w-20 animate-pulse rounded-full bg-[#edf2f4]"></span>
          <span class="mt-3 block h-3 w-28 max-w-full animate-pulse rounded-full bg-[#e5eef2]"></span>
        </article>
      </template>

      <article
        v-else-if="!capsuleAccounts.length"
        class="col-span-2 rounded-[22px] border border-dashed border-[#cfe3ed] bg-[#f7fcff] px-5 py-8 text-center"
      >
        <span class="mx-auto grid size-16 place-items-center rounded-full bg-[#e8f7fe]">
          <img class="h-10 w-12 object-contain" :src="lockImage" alt="" aria-hidden="true" />
        </span>
        <h2 class="mt-4 text-[17px] font-extrabold">아직 만들어진 타임캡슐이 없어요</h2>
        <p class="mt-2 text-[12px] leading-5 text-[var(--color-text-secondary)]">
          적금이나 아이 명의 입출금계좌를 만들면<br />해당 계좌의 타임캡슐이 자동으로 생성돼요.
        </p>
        <div class="mt-5 grid grid-cols-2 gap-2.5">
          <RouterLink
            :to="{ name: 'SavingsRecommendation' }"
            class="flex min-h-11 items-center justify-center rounded-[13px] border border-[#cdebf9] bg-white text-[12px] font-bold !text-[var(--color-selected-text)] active:bg-[#edf9ff]"
          >
            적금 알아보기
          </RouterLink>
          <RouterLink
            :to="{ name: 'ChildAccountCreate' }"
            class="flex min-h-11 items-center justify-center rounded-[13px] bg-[var(--color-brand-primary)] text-[12px] font-bold !text-white active:bg-[var(--color-brand-primary-pressed)]"
          >
            아이 계좌 만들기
          </RouterLink>
        </div>
      </article>

      <template v-else>
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
            :class="
              capsule.isFree && !isFreeCapsuleCreated
                ? 'font-bold text-[#ef5b5b]'
                : 'text-[var(--color-text-secondary)]'
            "
          >
            {{ capsule.createdAt }}
          </time>
          <p class="mt-3 text-xs font-bold text-[var(--color-selected-text)]">
            저축 금액 {{ capsule.savedAmount.toLocaleString('ko-KR') }}원
          </p>
        </button>
      </article>
      </template>
    </section>

    <Teleport to="body">
      <button
        v-if="!isLoading && capsuleAccounts.length"
        class="time-capsule-create-button fixed z-[40]"
        type="button"
        aria-label="타임캡슐 생성하기"
        @click="navigateForward('/time-capsules/new')"
      >
        <span class="time-capsule-create-button__surface">
          <Plus class="-translate-y-0.5" :size="23" :stroke-width="3" aria-hidden="true" />
        </span>
      </button>
    </Teleport>

    <Teleport to="body">
      <Transition name="free-capsule-sheet">
        <div
          v-if="isFreeCapsuleSheetOpen"
          class="fixed inset-0 z-[var(--z-index-overlay)] flex items-end justify-center bg-black/40"
          @click.self="closeFreeCapsuleSheet"
        >
          <section
            class="free-capsule-panel w-full max-w-[var(--app-max-width)] rounded-t-[26px] bg-white px-5 pt-3 pb-[calc(24px+env(safe-area-inset-bottom))]"
            :class="isFreeCapsuleSheetDragging ? 'free-capsule-panel--dragging' : ''"
            :style="freeCapsuleSheetOffset ? { transform: `translateY(${freeCapsuleSheetOffset}px)` } : undefined"
            role="dialog"
            aria-modal="true"
            aria-labelledby="free-capsule-title"
          >
            <div
              class="-mx-5 -mt-3 flex h-10 touch-none cursor-grab items-center justify-center active:cursor-grabbing"
              role="button"
              tabindex="0"
              aria-label="아래로 밀어 오픈 날짜 설정 닫기"
              @pointerdown="startFreeCapsuleSheetDrag"
              @pointermove="moveFreeCapsuleSheetDrag"
              @pointerup="endFreeCapsuleSheetDrag"
              @pointercancel="endFreeCapsuleSheetDrag"
            >
              <span class="block h-1 w-10 rounded-full bg-[#d7dfe4]"></span>
            </div>
            <header class="mt-1 flex items-start justify-between gap-4">
              <div>
                <h2 id="free-capsule-title" class="m-0 text-[20px] font-bold">언제 열어볼까요?</h2>
              </div>
              <button
                class="grid size-9 shrink-0 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[#f2f5f7]"
                type="button"
                aria-label="오픈 날짜 설정 닫기"
                @click="closeFreeCapsuleSheet"
              >
                <X :size="20" />
              </button>
            </header>

            <div class="mt-5 rounded-2xl bg-[#f6f8fa] p-4">
              <div class="mb-3 flex items-center gap-2 text-xs font-bold text-[#5d7180]">
                타임캡슐 오픈 날짜
              </div>
              <BaseDatePicker
                v-model="freeCapsuleOpenDate"
                class="free-capsule-date-picker"
                placeholder="날짜를 선택해주세요"
                :min-year="tomorrow.getFullYear()"
                :max-year="maxOpenYear"
                :min-date="minOpenDate"
              />
            </div>

            <button
              class="mt-5 flex h-[54px] w-full items-center justify-center gap-2 rounded-2xl bg-[var(--color-brand-primary)] text-sm font-bold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[#cbd8df]"
              type="button"
              :disabled="!freeCapsuleOpenDate"
              @click="confirmFreeCapsule"
            >
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
.time-capsule-archive--leaving > section {
  transform: translateX(-18px);
  opacity: 0;
  transition:
    transform 150ms cubic-bezier(0.25, 0.8, 0.25, 1),
    opacity 120ms ease-out;
}

.time-capsule-archive--leaving > .time-capsule-create-button {
  opacity: 0;
  transition: opacity 120ms ease-out;
}

@media (prefers-reduced-motion: no-preference) {
  .pregnancy-growth-card__image {
    animation: pregnancy-character-float 3.2s ease-in-out infinite;
    transform-origin: 50% 100%;
  }
}

@keyframes pregnancy-character-float {
  0%,
  100% {
    transform: translateY(0) rotate(0);
  }
  50% {
    transform: translateY(-3px) rotate(1.5deg);
  }
}

.time-capsule-create-button {
  bottom: calc(var(--app-bottom-nav-height) + env(safe-area-inset-bottom) - 7px);
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
  transition:
    background-color 140ms ease,
    transform 140ms ease;
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
  0%,
  10%,
  24%,
  100% {
    transform: translateY(0) scale(1);
  }
  14% {
    transform: translateY(-2px) scale(1.12);
  }
  19% {
    transform: translateY(0) scale(0.96);
  }
}

@keyframes open-date-alert-ripple {
  0%,
  12% {
    opacity: 0;
    transform: scale(0.85);
  }
  16% {
    opacity: 0.62;
  }
  34%,
  100% {
    opacity: 0;
    transform: scale(2.15);
  }
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
  background: linear-gradient(
    110deg,
    #9fddf5 5%,
    #d8c4f3 24%,
    #f5bfd9 42%,
    #fff0ae 60%,
    #b9efd9 78%,
    #9fddf5 96%
  );
  background-size: 260% 100%;
  border-radius: inherit;
  content: '';
  animation: silver-border-shimmer 5s ease-in-out infinite;
  -webkit-mask:
    linear-gradient(#000 0 0) content-box,
    linear-gradient(#000 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  filter: drop-shadow(0 0 4px rgb(174 218 241 / 32%));
}

@keyframes silver-border-shimmer {
  0%,
  18% {
    background-position: 100% 0;
  }
  72%,
  100% {
    background-position: -100% 0;
  }
}

.free-capsule-date-picker :deep(> button) {
  height: 52px;
  font-size: 14px;
}
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
.free-capsule-sheet-leave-active {
  transition: background-color 180ms ease;
}
.free-capsule-sheet-enter-active .free-capsule-panel,
.free-capsule-sheet-leave-active .free-capsule-panel {
  transition: transform 220ms cubic-bezier(0.22, 1, 0.36, 1);
}
.free-capsule-sheet-enter-from,
.free-capsule-sheet-leave-to {
  background-color: transparent;
}
.free-capsule-sheet-enter-from .free-capsule-panel,
.free-capsule-sheet-leave-to .free-capsule-panel {
  transform: translateY(100%);
}
.free-capsule-panel {
  transition: transform 180ms cubic-bezier(0.22, 1, 0.36, 1);
}
.free-capsule-panel--dragging {
  transition: none !important;
}

@media (prefers-reduced-motion: reduce) {
  .time-capsule-archive--leaving > section,
  .time-capsule-archive--leaving > .time-capsule-create-button {
    transition-duration: 1ms;
  }
  .capsule-card--released::before {
    animation: none;
  }
  .open-date-alert,
  .open-date-alert::before,
  .open-date-alert::after {
    animation: none;
  }
  .free-capsule-sheet-enter-active,
  .free-capsule-sheet-leave-active,
  .free-capsule-sheet-enter-active .free-capsule-panel,
  .free-capsule-sheet-leave-active .free-capsule-panel {
    transition-duration: 1ms;
  }
}
</style>
