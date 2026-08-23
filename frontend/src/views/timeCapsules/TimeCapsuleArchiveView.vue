<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Check, Plus, X } from 'lucide-vue-next'
import { useRouter, type RouteLocationRaw } from 'vue-router'
import calendarImage from '@/assets/images/timeCapsules/archive/calendar.png'
import lockImage from '@/assets/images/timeCapsules/archive/lock.png'
import archiveBackgroundImage from '@/assets/images/timeCapsules/archive/new-bg.png'
import openImage from '@/assets/images/timeCapsules/archive/open.png'
import bornBabyPigImage from '@/assets/images/timeCapsules/archive/born-baby-pig.png'
import unlockClosedShackleImage from '@/assets/images/timeCapsules/unlock/closed-shackle.png'
import unlockLockBodyImage from '@/assets/images/timeCapsules/unlock/lock-body.png'
import unlockOpenShackleImage from '@/assets/images/timeCapsules/unlock/open-shackle.png'
import unlockPigQuestionImage from '@/assets/images/timeCapsules/unlock/pig-question.png'
import unlockPigSurprisedImage from '@/assets/images/timeCapsules/unlock/pig-surprised.png'
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
const capsuleAccounts = ref<
  Array<{
    id: number
    accountId?: number
    name: string
    createdAt: string
    savedAmount: number
    dDay?: number
    isFree?: boolean
  }>
>([])
const selectedFreeCapsuleId = ref<number | null>(null)
const archiveChildName = computed(() => registration.value?.childName.trim() || '아이')
type CapsuleAccount = (typeof capsuleAccounts.value)[number]
type UnlockPhase =
  | 'idle'
  | 'centering'
  | 'emerging'
  | 'flashing'
  | 'revealing'
  | 'ready'
  | 'unlocked'
  | 'glow'
  | 'leaving'
type UnlockCardRect = {
  left: number
  top: number
  width: number
  height: number
  dx: number
  dy: number
  scale: number
}

const unlockPhase = ref<UnlockPhase>('idle')
const unlockSelectedCapsule = ref<CapsuleAccount | null>(null)
const unlockCardRect = ref<UnlockCardRect | null>(null)
const isUnlockCardCentered = ref(false)
const unlockTimers: number[] = []
const UNLOCK_CARD_CENTER_MS = 900
const UNLOCK_LOCK_EMERGE_MS = 1000
const UNLOCK_WHITE_FLASH_MS = 700
const UNLOCK_REVEAL_FADE_MS = 800
const UNLOCK_SEEN_STORAGE_PREFIX = 'azas_time_capsule_unlock_seen'
const OPEN_FLASH_STORAGE_KEY = 'azas_time_capsule_open_flash'
const UNLOCK_LONG_PRESS_MS = 2000
const unlockLongPressState = {
  timer: null as number | null,
  didTrigger: false,
}

const isUnlockOverlayOpen = computed(() => unlockPhase.value !== 'idle')
const shouldShowUnlockReplayButton = computed(() => import.meta.env.DEV)
const unlockStatusText = computed(() => '자물쇠를 눌러보세요')
const unlockCardStyle = computed(() => {
  if (!unlockCardRect.value) return undefined

  return {
    left: `${unlockCardRect.value.left}px`,
    top: `${unlockCardRect.value.top}px`,
    width: `${unlockCardRect.value.width}px`,
    height: `${unlockCardRect.value.height}px`,
    '--unlock-card-x': `${unlockCardRect.value.dx}px`,
    '--unlock-card-y': `${unlockCardRect.value.dy}px`,
    '--unlock-card-scale': String(unlockCardRect.value.scale),
  }
})

const applyLocalTimeCapsuleFallback = () => {
  registration.value = {
    birthDate: '2024-03-15',
    childName: '깨비',
  }
  childId.value = 1
  demandAccountId.value = 1
  isFreeCapsuleCreated.value = true
  capsuleAccounts.value = [
    {
      id: 1,
      name: '깨비 첫 타임캡슐',
      createdAt: '2024.03.15',
      savedAmount: 120000,
      dDay: 0,
      isFree: false,
    },
  ]
}

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

const clearUnlockTimers = () => {
  unlockTimers.splice(0).forEach((timer) => window.clearTimeout(timer))
}

const scheduleUnlockTimer = (callback: () => void, delay: number) => {
  const timer = window.setTimeout(() => {
    const index = unlockTimers.indexOf(timer)
    if (index >= 0) unlockTimers.splice(index, 1)
    callback()
  }, delay)
  unlockTimers.push(timer)
  return timer
}

const waitUnlock = (delay: number) =>
  new Promise<void>((resolve) => {
    scheduleUnlockTimer(resolve, delay)
  })

const resetUnlockFlow = () => {
  clearUnlockTimers()
  unlockPhase.value = 'idle'
  unlockSelectedCapsule.value = null
  unlockCardRect.value = null
  isUnlockCardCentered.value = false
}

const clearUnlockLongPressTimer = () => {
  if (unlockLongPressState.timer === null) return
  window.clearTimeout(unlockLongPressState.timer)
  unlockLongPressState.timer = null
}

const startUnlockLongPress = (capsule: CapsuleAccount, event: PointerEvent) => {
  clearUnlockLongPressTimer()
  unlockLongPressState.didTrigger = false

  const currentTarget = event.currentTarget as HTMLElement | null
  if (!currentTarget) return
  if (capsule.isFree && capsule.createdAt === '오픈 날짜 설정') return

  unlockLongPressState.timer = window.setTimeout(() => {
    unlockLongPressState.timer = null
    unlockLongPressState.didTrigger = true
    window.localStorage.removeItem(getUnlockSeenStorageKey(capsule.id))
    startUnlockFlowFromElement(capsule, currentTarget)
  }, UNLOCK_LONG_PRESS_MS)
}

const cancelUnlockLongPress = () => {
  clearUnlockLongPressTimer()
}

const getUnlockSeenStorageKey = (capsuleId: number) =>
  `${UNLOCK_SEEN_STORAGE_PREFIX}_${capsuleId}`

const hasSeenUnlockAnimation = (capsule: CapsuleAccount) => {
  if (typeof window === 'undefined') return false
  return window.localStorage.getItem(getUnlockSeenStorageKey(capsule.id)) === 'true'
}

const markUnlockAnimationSeen = (capsule: CapsuleAccount) => {
  if (typeof window === 'undefined') return
  window.localStorage.setItem(getUnlockSeenStorageKey(capsule.id), 'true')
}

const getOpenRoute = (capsule: CapsuleAccount) =>
  import.meta.env.DEV && capsule.id === 1 && capsule.name === '깨비 첫 타임캡슐'
    ? '/time-capsules/local/open'
    : `/time-capsules/${capsule.id}/open`

const getListRoute = (capsule: CapsuleAccount) =>
  import.meta.env.DEV && capsule.id === 1 && capsule.name === '깨비 첫 타임캡슐'
    ? '/time-capsules/local'
    : `/time-capsules/${capsule.id}`

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
  if (!childId.value || !demandAccountId.value || !selectedFreeCapsuleId.value) {
    showToast('연결된 입출금계좌가 필요합니다.', 'error')
    return
  }
  try {
    const { data } = await api.updateTimeCapsuleReleaseDateUsingPATCH(
      selectedFreeCapsuleId.value,
      {
        release_date: freeCapsuleOpenDate.value,
      },
    )
    const capsule = capsuleAccounts.value.find(({ id }) => id === selectedFreeCapsuleId.value)
    if (capsule) {
      capsule.createdAt = data.release_date?.replaceAll('-', '.') ?? formattedFreeOpenDate.value
      capsule.dDay = calculateDday(data.release_date ?? freeCapsuleOpenDate.value) ?? undefined
    }
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

const openFreeCapsuleList = (freeCapsule: { id: number; createdAt: string }) => {
  navigateForward({
    name: 'TimeCapsuleList',
    params: { capsuleListId: String(freeCapsule.id) },
    query: { openDate: freeCapsule.createdAt.replaceAll('.', '-') },
  })
}

const startUnlockFlowFromElement = (capsule: CapsuleAccount, currentTarget: HTMLElement) => {
  if (isUnlockOverlayOpen.value) return

  const sourceElement = currentTarget.closest('article') ?? currentTarget
  const rect = sourceElement.getBoundingClientRect()
  const targetCardWidth = Math.min(window.innerWidth * 0.88, window.innerHeight * 0.48, 420)
  const targetCardScale = Math.max(1.22, targetCardWidth / rect.width)

  clearUnlockTimers()
  unlockSelectedCapsule.value = capsule
  unlockPhase.value = 'centering'
  unlockCardRect.value = {
    left: rect.left,
    top: rect.top,
    width: rect.width,
    height: rect.height,
    dx: window.innerWidth / 2 - rect.left - rect.width / 2,
    dy: window.innerHeight / 2 - rect.top - rect.height / 2,
    scale: targetCardScale,
  }

  window.requestAnimationFrame(() => {
    window.requestAnimationFrame(() => {
      isUnlockCardCentered.value = true
    })
  })

  scheduleUnlockTimer(() => {
    if (unlockPhase.value === 'centering') unlockPhase.value = 'emerging'
  }, UNLOCK_CARD_CENTER_MS)

  scheduleUnlockTimer(() => {
    if (unlockPhase.value === 'emerging') unlockPhase.value = 'flashing'
  }, UNLOCK_CARD_CENTER_MS + UNLOCK_LOCK_EMERGE_MS)

  scheduleUnlockTimer(
    () => {
      if (unlockPhase.value === 'flashing') unlockPhase.value = 'revealing'
    },
    UNLOCK_CARD_CENTER_MS + UNLOCK_LOCK_EMERGE_MS + UNLOCK_WHITE_FLASH_MS,
  )

  scheduleUnlockTimer(
    () => {
      if (unlockPhase.value === 'revealing') unlockPhase.value = 'ready'
    },
    UNLOCK_CARD_CENTER_MS + UNLOCK_LOCK_EMERGE_MS + UNLOCK_WHITE_FLASH_MS + UNLOCK_REVEAL_FADE_MS,
  )
}

const startUnlockFlow = (capsule: CapsuleAccount, event: MouseEvent) => {
  const currentTarget = event.currentTarget as HTMLElement | null
  if (!currentTarget) return
  startUnlockFlowFromElement(capsule, currentTarget)
}

const replayUnlockFlow = (capsule: CapsuleAccount, event: MouseEvent) => {
  event.stopPropagation()
  if (typeof window !== 'undefined') {
    window.localStorage.removeItem(getUnlockSeenStorageKey(capsule.id))
  }
  startUnlockFlow(capsule, event)
}

const completeUnlockFlow = async () => {
  if (unlockPhase.value !== 'ready' || !unlockSelectedCapsule.value) return

  const capsule = unlockSelectedCapsule.value
  unlockPhase.value = 'unlocked'
  await waitUnlock(980)
  unlockPhase.value = 'glow'
  await waitUnlock(620)
  unlockPhase.value = 'leaving'
  await waitUnlock(760)

  markUnlockAnimationSeen(capsule)
  window.sessionStorage.setItem(OPEN_FLASH_STORAGE_KEY, 'true')

  try {
    await router.push(getOpenRoute(capsule))
  } catch {
    resetUnlockFlow()
  }
}

const openCapsule = (capsule: CapsuleAccount, event: MouseEvent) => {
  if (unlockLongPressState.didTrigger) {
    event.preventDefault()
    unlockLongPressState.didTrigger = false
    return
  }

  if (!capsule.isFree) {
    if (isCapsuleReleased(capsule)) {
      if (hasSeenUnlockAnimation(capsule)) {
        navigateForward(getListRoute(capsule))
        return
      }

      startUnlockFlow(capsule, event)
      return
    }

    navigateForward(`/time-capsules/${capsule.id}`)
    return
  }
  if (capsule.createdAt === '오픈 날짜 설정') {
    demandAccountId.value = capsule.accountId ?? null
    selectedFreeCapsuleId.value = capsule.id
    freeCapsuleOpenDate.value = ''
    isFreeCapsuleSheetOpen.value = true
    return
  }
  openFreeCapsuleList(capsule)
}

const isCapsuleReleased = (capsule: { createdAt: string; isFree?: boolean }) =>
  capsule.isFree
    ? capsule.createdAt !== '오픈 날짜 설정' && isReleased(capsule.createdAt)
    : isReleased(capsule.createdAt)

onMounted(async () => {
  try {
    childId.value = await resolveCurrentChildId()
    const [
      { data: child },
      { data: initialCapsules },
      { data: childAccounts },
      { data: parentAccounts },
    ] = await Promise.all([
      api.getChildUsingGET(childId.value),
      api.getTimeCapsulesUsingGET(childId.value),
      api.getChildAccountsUsingGET(childId.value),
      api.getMyAccountsUsingGET(),
    ])
    registration.value = {
      birthDate: child.birth_date ?? child.expected_birth_date ?? '',
      childName: child.name ?? '아이',
    }
    const allAccounts = [...childAccounts.accounts, ...parentAccounts.accounts]
    const demandAccountIds = new Set(
      allAccounts
        .filter(({ account_product_type }) => account_product_type === 'DEMAND_DEPOSIT')
        .map(({ account_id }) => account_id),
    )
    const activeAccountIds = new Set(allAccounts.map(({ account_id }) => account_id))
    const eligibleAccountIds = [
      ...new Set([
        ...allAccounts
          .filter(({ account_product_type }) => account_product_type === 'SAVINGS')
          .map(({ account_id }) => account_id),
        ...demandAccountIds,
      ]),
    ]
    const eligibleAccountIdSet = new Set(eligibleAccountIds)
    let capsuleItems = initialCapsules.time_capsules ?? []
    const orphanedCapsules = capsuleItems.filter(
      ({ account_id, time_capsule_id }) =>
        account_id != null &&
        time_capsule_id != null &&
        (!activeAccountIds.has(account_id) || !eligibleAccountIdSet.has(account_id)),
    )

    if (orphanedCapsules.length > 0) {
      const deleteResults = await Promise.allSettled(
        orphanedCapsules.map(({ time_capsule_id }) =>
          api.deleteTimeCapsuleUsingDELETE(time_capsule_id!),
        ),
      )
      if (deleteResults.some(({ status }) => status === 'rejected')) {
        showToast('삭제된 계좌의 일부 타임캡슐을 정리하지 못했습니다.', 'error')
      }
      const { data: capsulesAfterCleanup } = await api.getTimeCapsulesUsingGET(childId.value)
      capsuleItems = capsulesAfterCleanup.time_capsules ?? []
    }

    const capsuleAccountIds = new Set(
      capsuleItems
        .map(({ account_id }) => account_id)
        .filter((accountId): accountId is number => accountId != null),
    )
    const missingAccountIds = eligibleAccountIds.filter(
      (accountId) => !capsuleAccountIds.has(accountId),
    )

    if (missingAccountIds.length > 0) {
      await Promise.allSettled(
        missingAccountIds.map((financialAccountId) =>
          api.createTimeCapsuleUsingPOST(childId.value!, {
            financial_account_id: financialAccountId,
          }),
        ),
      )
      const { data: refreshedCapsules } = await api.getTimeCapsulesUsingGET(childId.value)
      capsuleItems = refreshedCapsules.time_capsules ?? []

      const refreshedAccountIds = new Set(
        capsuleItems
          .map(({ account_id }) => account_id)
          .filter((accountId): accountId is number => accountId != null),
      )
      if (missingAccountIds.some((accountId) => !refreshedAccountIds.has(accountId))) {
        showToast('일부 계좌의 타임캡슐을 만들지 못했습니다.', 'error')
      }
    }

    capsuleAccounts.value = capsuleItems.map((capsule) => ({
      id: capsule.time_capsule_id ?? 0,
      accountId: capsule.account_id,
      name: capsule.title ?? '타임캡슐',
      createdAt: capsule.release_date?.replaceAll('-', '.') ?? '오픈 날짜 설정',
      savedAmount: capsule.total_saved_amount ?? 0,
      dDay: capsule.d_day,
      isFree: capsule.account_id != null && demandAccountIds.has(capsule.account_id),
    }))
    isFreeCapsuleCreated.value = capsuleAccounts.value.some(({ isFree }) => isFree)
    if (import.meta.env.DEV && capsuleAccounts.value.length === 0) {
      applyLocalTimeCapsuleFallback()
    }
  } catch (error) {
    showToast(getApiErrorMessage(error, '타임캡슐을 불러오지 못했습니다.'), 'error')
    applyLocalTimeCapsuleFallback()
  } finally {
    isLoading.value = false
  }
})

onBeforeUnmount(() => {
  resetUnlockFlow()
  clearUnlockLongPressTimer()
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
              :alt="`${pregnancyGrowth.fruit} ${archiveChildName}`"
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
          class="block w-full touch-manipulation select-none text-left transition-transform active:scale-[0.98]"
          type="button"
          @pointerdown="startUnlockLongPress(capsule, $event)"
          @pointerup="cancelUnlockLongPress"
          @pointercancel="cancelUnlockLongPress"
          @pointerleave="cancelUnlockLongPress"
          @contextmenu.prevent
          @click="openCapsule(capsule, $event)"
        >
          <span
            class="relative grid aspect-[4/3] place-items-center rounded-xl"
            :class="isCapsuleReleased(capsule) ? 'bg-[#ecfaff]' : 'bg-[#f0f3f5]'"
          >
            <span
              v-if="capsule.isFree && capsule.createdAt === '오픈 날짜 설정'"
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
              capsule.isFree && capsule.createdAt === '오픈 날짜 설정'
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
        <button
          v-if="shouldShowUnlockReplayButton && isCapsuleReleased(capsule)"
          class="unlock-replay-button mt-3"
          type="button"
          @click="replayUnlockFlow(capsule, $event)"
        >
          자물쇠 애니메이션 다시보기
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
      <div
        v-if="isUnlockOverlayOpen"
        class="time-capsule-unlock fixed inset-0 z-[var(--z-index-overlay)] overflow-hidden"
        role="dialog"
        aria-modal="true"
        aria-labelledby="unlock-title"
      >
        <div
          class="time-capsule-unlock__backdrop absolute inset-0"
          :class="isUnlockCardCentered ? 'time-capsule-unlock__backdrop--active' : ''"
        ></div>
        <article
          v-if="unlockSelectedCapsule && unlockCardStyle"
          class="unlock-card-copy"
          :class="[
            isUnlockCardCentered ? 'unlock-card-copy--centered' : '',
            unlockPhase === 'emerging' ||
            unlockPhase === 'flashing' ||
            unlockPhase === 'revealing'
              ? 'unlock-card-copy--expanded'
              : '',
            unlockPhase !== 'centering' && unlockPhase !== 'emerging'
              ? 'unlock-card-copy--soft-hidden'
              : '',
          ]"
          :style="unlockCardStyle"
          aria-hidden="true"
        >
            <span
              class="relative grid aspect-[4/3] place-items-center rounded-xl"
              :class="isCapsuleReleased(unlockSelectedCapsule) ? 'bg-[#ecfaff]' : 'bg-[#f0f3f5]'"
            >
              <img
                class="unlock-card-copy__lock h-16 w-20 object-contain"
                :src="lockImage"
                alt=""
              />
            </span>
            <strong class="mt-4 block truncate text-base text-[var(--color-text-primary)]">
              {{ unlockSelectedCapsule.name }}
            </strong>
            <time class="mt-1 block text-xs text-[var(--color-text-secondary)]">
              {{ unlockSelectedCapsule.createdAt }}
            </time>
            <p class="mt-3 text-xs font-bold text-[var(--color-selected-text)]">
              저축 금액 {{ unlockSelectedCapsule.savedAmount.toLocaleString('ko-KR') }}원
            </p>
        </article>

        <Transition name="unlock-lock">
            <section
              v-if="
                unlockPhase !== 'centering' &&
                unlockPhase !== 'emerging' &&
                unlockPhase !== 'flashing'
              "
              class="unlock-panel absolute inset-0 px-5"
              :class="[
                unlockPhase === 'revealing' ? 'unlock-panel--revealing' : '',
                unlockPhase === 'ready' ? 'unlock-panel--tap-ready' : '',
                unlockPhase === 'glow' || unlockPhase === 'leaving' ? 'unlock-panel--glow' : '',
                unlockPhase === 'leaving' ? 'unlock-panel--leaving' : '',
              ]"
            >
              <div
                class="unlock-light"
                :class="
                  unlockPhase === 'unlocked' || unlockPhase === 'glow' || unlockPhase === 'leaving'
                    ? 'unlock-light--warm'
                    : ''
                "
                aria-hidden="true"
              ></div>
              <div
                v-if="unlockPhase === 'glow' || unlockPhase === 'leaving'"
                class="unlock-white-flash unlock-white-flash--after absolute inset-0"
                aria-hidden="true"
              ></div>

              <button
                class="unlock-lock-stack"
                :class="[
                  unlockPhase === 'revealing' ? 'unlock-lock-stack--revealing' : '',
                  unlockPhase === 'ready' ? 'unlock-lock-stack--ready' : '',
                  unlockPhase === 'unlocked' ? 'unlock-lock-stack--opening' : '',
                ]"
                type="button"
                aria-label="타임캡슐 자물쇠 열기"
                @pointerdown="completeUnlockFlow"
                @click="completeUnlockFlow"
                @keydown.enter.prevent="completeUnlockFlow"
                @keydown.space.prevent="completeUnlockFlow"
              >
                <img
                  class="unlock-layer unlock-shackle unlock-shackle--closed"
                  :class="
                    unlockPhase === 'unlocked' ||
                    unlockPhase === 'glow' ||
                    unlockPhase === 'leaving'
                      ? 'unlock-shackle--hidden'
                      : ''
                  "
                  :src="unlockClosedShackleImage"
                  alt=""
                />
                <img
                  class="unlock-layer unlock-shackle unlock-shackle--open"
                  :class="
                    unlockPhase === 'unlocked' ||
                    unlockPhase === 'glow' ||
                    unlockPhase === 'leaving'
                      ? 'unlock-shackle--visible'
                      : ''
                  "
                  :src="unlockOpenShackleImage"
                  alt=""
                />
                <img
                  class="unlock-layer unlock-lock-body"
                  :src="unlockLockBodyImage"
                  alt="닫힌 자물쇠"
                />
                <img
                  class="unlock-layer unlock-pig unlock-pig--question"
                  :class="
                    unlockPhase === 'unlocked' ||
                    unlockPhase === 'glow' ||
                    unlockPhase === 'leaving'
                      ? 'unlock-pig--hidden'
                      : ''
                  "
                  :src="unlockPigQuestionImage"
                  alt=""
                />
                <img
                  class="unlock-layer unlock-pig unlock-pig--surprised"
                  :class="
                    unlockPhase === 'unlocked' ||
                    unlockPhase === 'glow' ||
                    unlockPhase === 'leaving'
                      ? 'unlock-pig--visible'
                      : ''
                  "
                  :src="unlockPigSurprisedImage"
                  alt=""
                />
              </button>

              <div class="unlock-copy text-center">
                <h2
                  id="unlock-title"
                  class="text-[22px] leading-7 font-bold tracking-[-0.03em] text-[var(--color-text-primary)]"
                >
                  타임캡슐을 열어볼까요?
                </h2>
                <p
                  v-if="unlockPhase === 'ready'"
                  class="mt-2 min-h-[40px] text-sm leading-5 text-[var(--color-text-secondary)]"
                >
                  {{ unlockStatusText }}
                </p>
              </div>
            </section>
        </Transition>
      </div>
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

.unlock-replay-button {
  position: relative;
  z-index: 2;
  display: flex;
  min-height: 34px;
  width: 100%;
  align-items: center;
  justify-content: center;
  border: 1px solid #cfe8f7;
  border-radius: 11px;
  background: #f6fcff;
  color: #4d9ed7;
  font-size: 11px;
  font-weight: 800;
  transition:
    background-color 120ms ease,
    transform 120ms ease;
}

.unlock-replay-button:active {
  background: #e9f8ff;
  transform: scale(0.985);
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

.time-capsule-unlock {
  touch-action: none;
}

.time-capsule-unlock__backdrop {
  background:
    linear-gradient(180deg, rgb(230 247 255 / 96%) 0%, rgb(249 253 255 / 98%) 52%, #ffffff 100%),
    #f3fbff;
  transition: opacity 360ms ease;
}

.time-capsule-unlock__backdrop--active {
  opacity: 1;
}

.unlock-white-flash {
  z-index: 4;
  pointer-events: none;
  background: #fff;
  opacity: 0;
  animation: unlock-white-flash-in 800ms ease-out both;
}

.unlock-white-flash--leaving {
  animation: unlock-white-flash-out 800ms ease-in both;
}

.unlock-white-flash--after {
  animation: unlock-white-after-glow 1.7s ease-out both;
}

.unlock-card-copy {
  position: fixed;
  z-index: 1;
  padding: 12px;
  overflow: hidden;
  pointer-events: none;
  background: white;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  transform: translate3d(0, 0, 0) scale(1);
  transform-origin: center;
  transition:
    transform 860ms cubic-bezier(0.2, 0.86, 0.2, 1),
    opacity 1200ms ease;
}

.unlock-card-copy--centered {
  transform: translate3d(var(--unlock-card-x), var(--unlock-card-y), 0) scale(1.04);
}

.unlock-card-copy--expanded {
  transform: translate3d(var(--unlock-card-x), var(--unlock-card-y), 0)
    scale(var(--unlock-card-scale));
  transition:
    transform 2500ms cubic-bezier(0.22, 1, 0.36, 1),
    opacity 1200ms ease;
}

.unlock-card-copy--soft-hidden {
  opacity: 0;
}

.unlock-panel {
  --unlock-size: min(94vw, 58dvh, 520px);
  --unlock-x: -58%;

  z-index: 2;
  cursor: default;
  transition:
    opacity 520ms ease,
    transform 520ms cubic-bezier(0.2, 0.86, 0.2, 1);
}

.unlock-panel--tap-ready {
  cursor: pointer;
}

.unlock-panel--revealing .unlock-copy {
  opacity: 0;
}

.unlock-panel--glow .unlock-lock-stack {
  animation: unlock-lock-warm-pop 620ms ease-out both;
}

.unlock-panel--leaving {
  opacity: 0;
  transform: scale(0.98);
}

.unlock-light {
  position: absolute;
  top: 50%;
  left: 50%;
  z-index: 0;
  width: max(142vw, 142dvh);
  aspect-ratio: 1;
  pointer-events: none;
  background: radial-gradient(
    circle,
    rgb(255 246 216 / 92%) 0%,
    rgb(255 236 184 / 72%) 34%,
    rgb(255 248 226 / 44%) 58%,
    rgb(255 255 255 / 0%) 78%
  );
  border-radius: 50%;
  opacity: 0;
  transform: translate(-50%, -50%) scale(0.48);
}

.unlock-light--warm {
  animation: unlock-warm-light 2.15s ease-out both;
}

.unlock-lock-stack {
  position: absolute;
  top: 50%;
  left: 50%;
  z-index: 1;
  display: block;
  width: var(--unlock-size);
  padding: 0;
  aspect-ratio: 1;
  background: transparent;
  border: 0;
  cursor: default;
  transform: translate(var(--unlock-x), -50%);
  -webkit-tap-highlight-color: transparent;
  transition: transform 260ms ease;
}

.unlock-lock-stack--ready {
  cursor: pointer;
  animation: unlock-lock-settle 420ms cubic-bezier(0.2, 0.86, 0.2, 1) both;
}

.unlock-lock-stack--ready:active {
  transform: translate(var(--unlock-x), -50%) scale(0.992);
  animation: unlock-press-shake 520ms ease-in-out;
}

.unlock-lock-stack--revealing {
  animation: unlock-lock-reveal 800ms ease-out both;
}

.unlock-lock-stack--revealing .unlock-shackle--closed,
.unlock-lock-stack--revealing .unlock-lock-body,
.unlock-lock-stack--revealing .unlock-pig--question {
  animation: unlock-layer-fade-in 800ms ease-out both;
}

.unlock-lock-stack--opening {
  animation: unlock-press-shake 620ms ease-in-out;
}

.unlock-lock-stack:focus-visible {
  border-radius: 28px;
  outline: 3px solid rgb(147 201 245 / 82%);
  outline-offset: 6px;
}

.unlock-layer {
  position: absolute;
  display: block;
  height: auto;
  object-fit: contain;
  pointer-events: none;
  user-select: none;
  transition:
    opacity 260ms ease,
    transform 560ms cubic-bezier(0.2, 0.86, 0.2, 1);
}

.unlock-lock-body {
  z-index: 2;
  top: 35.65%;
  left: 36.76%;
  width: 45.3%;
}

.unlock-shackle {
  z-index: 1;
}

.unlock-shackle--closed {
  top: 12.52%;
  left: 42.19%;
  width: 34.61%;
  opacity: 1;
}

.unlock-shackle--open {
  top: 10.61%;
  left: 45.61%;
  width: 38.52%;
  opacity: 0;
  transform: translate(-10%, 7%) rotate(-12deg) scale(0.96);
  transform-origin: 18% 78%;
}

.unlock-shackle--hidden {
  opacity: 0;
  transform: translate(-5%, 4%) rotate(-4deg) scale(0.98);
}

.unlock-shackle--visible {
  opacity: 1;
  transform: translate(0, 0) rotate(0) scale(1);
}

.unlock-pig {
  z-index: 3;
}

.unlock-pig--question {
  top: 27.35%;
  left: 25.52%;
  width: 26.08%;
  opacity: 1;
}

.unlock-pig--surprised {
  top: 27.11%;
  left: 26.95%;
  width: 26.08%;
  opacity: 0;
  transform: scale(0.96);
}

.unlock-pig--hidden {
  opacity: 0;
}

.unlock-pig--visible {
  opacity: 1;
  transform: translateY(-2%) scale(1.04);
}

.unlock-copy {
  position: absolute;
  top: calc(50% + (var(--unlock-size) * 0.36) + 34px);
  right: 20px;
  left: 20px;
  z-index: 2;
}

.unlock-overlay-enter-active,
.unlock-overlay-leave-active {
  transition: opacity 260ms ease;
}

.unlock-overlay-enter-from,
.unlock-overlay-leave-to {
  opacity: 0;
}

.unlock-lock-enter-active {
  transition:
    opacity 480ms ease,
    transform 560ms cubic-bezier(0.2, 0.86, 0.2, 1);
}

.unlock-lock-enter-from {
  opacity: 0;
  transform: scale(0.98);
}

@keyframes unlock-lock-warm-pop {
  0% {
    transform: translate(var(--unlock-x), -50%) scale(1);
  }
  44% {
    transform: translate(var(--unlock-x), -50%) scale(1.018);
  }
  100% {
    transform: translate(var(--unlock-x), -50%) scale(1);
  }
}

@keyframes unlock-lock-reveal {
  0%,
  100% {
    transform: translate(var(--unlock-x), -50%) scale(1);
  }
}

@keyframes unlock-white-flash-in {
  0% {
    opacity: 0;
  }
  72%,
  100% {
    opacity: 0.98;
  }
}

@keyframes unlock-white-flash-out {
  0% {
    opacity: 0.98;
  }
  100% {
    opacity: 0;
  }
}

@keyframes unlock-white-after-glow {
  0% {
    opacity: 0;
  }
  36% {
    opacity: 0.98;
  }
  100% {
    opacity: 0.98;
  }
}

@keyframes unlock-layer-fade-in {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

@keyframes unlock-lock-settle {
  0% {
    transform: translate(var(--unlock-x), -50%) scale(0.98);
  }
  100% {
    transform: translate(var(--unlock-x), -50%) scale(1);
  }
}

@keyframes unlock-press-shake {
  0%,
  100% {
    transform: translate(var(--unlock-x), -50%) rotate(0) scale(1);
  }
  14% {
    transform: translate(calc(var(--unlock-x) - 3px), -50%) rotate(-0.8deg) scale(0.998);
  }
  28% {
    transform: translate(calc(var(--unlock-x) + 3px), -50%) rotate(0.8deg) scale(1.004);
  }
  42% {
    transform: translate(calc(var(--unlock-x) - 2px), -50%) rotate(-0.45deg) scale(1.006);
  }
  60% {
    transform: translate(calc(var(--unlock-x) + 1px), -50%) rotate(0.25deg) scale(1.01);
  }
}

@keyframes unlock-warm-light {
  0% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.38);
  }
  22% {
    opacity: 0.98;
    transform: translate(-50%, -50%) scale(0.84);
  }
  54% {
    opacity: 0.78;
    transform: translate(-50%, -50%) scale(1.06);
  }
  100% {
    opacity: 0;
    transform: translate(-50%, -50%) scale(1.24);
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
  .time-capsule-unlock__backdrop,
  .unlock-panel,
  .unlock-layer,
  .unlock-overlay-enter-active,
  .unlock-overlay-leave-active {
    transition-duration: 1ms;
  }
  .unlock-lock-stack--ready,
  .unlock-lock-stack--revealing,
  .unlock-lock-stack--opening,
  .unlock-card-copy,
  .unlock-light,
  .unlock-light--warm,
  .unlock-white-flash {
    animation-duration: 1ms;
  }
}
</style>
