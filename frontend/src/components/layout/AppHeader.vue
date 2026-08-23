<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import {
  Bell,
  Check,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  CircleHelp,
  LogOut,
  Plus,
  ShieldCheck,
  X,
} from 'lucide-vue-next'
import { useRoute, useRouter } from 'vue-router'

import defaultProfileImageUrl from '@/assets/images/home/home-profile-baby.png'
import { useToast } from '@/composables/useToast'
import { api } from '@/api'
import { clearAuthSession, getRefreshToken } from '@/api/auth'
import {
  getStoredCurrentChildId,
  requireAuthorizationHeader,
  setCurrentChildId,
} from '@/api/context'

const router = useRouter()
const route = useRoute()
const { showToast } = useToast()

const props = withDefaults(
  defineProps<{
    title?: string
    profileName?: string
    profileImage?: string
    profileEmoji?: string
    showBack?: boolean
    showNotification?: boolean
    showGuide?: boolean
    notificationCount?: number
    backgroundColor?: string
    profileBackgroundColor?: string
    hideDivider?: boolean
    changeOnScroll?: boolean
    scrollThreshold?: number
  }>(),
  {
    title: '깨비',
    profileName: '깨비',
    profileImage: defaultProfileImageUrl,
    profileEmoji: '👶',
    showBack: false,
    showNotification: true,
    showGuide: false,
    notificationCount: 0,
    backgroundColor: '',
    profileBackgroundColor: '',
    hideDivider: false,
    changeOnScroll: false,
    scrollThreshold: 12,
  },
)

type HeaderChildProfile = {
  id: number
  name: string
  detail: string
  image: string
}

const profiles = ref<HeaderChildProfile[]>([])
const selectedProfileId = ref<number | null>(null)
const isProfileLoading = ref(true)
const isProfileSheetOpen = ref(false)
const isChildAccountSheetOpen = ref(false)
const isScrolled = ref(false)
const isChildRoute = computed(() => route.path === '/child' || route.path.startsWith('/child/'))
const useTopAppearance = computed(() => !props.changeOnScroll || !isScrolled.value)
const appliedHeaderBackgroundColor = computed(() =>
  useTopAppearance.value ? props.backgroundColor : '',
)
const appliedProfileBackgroundColor = computed(() =>
  useTopAppearance.value ? props.profileBackgroundColor : '',
)
const hideAppliedDivider = computed(() => props.hideDivider && useTopAppearance.value)
const fallbackProfileName = computed(() =>
  isChildRoute.value ? '아이' : props.profileName || props.title || '아이',
)
const activeProfile = computed(
  () => profiles.value.find(({ id }) => id === selectedProfileId.value) ?? profiles.value[0] ?? {
    id: 0,
    name: fallbackProfileName.value,
    detail: '',
    image: props.profileImage || defaultProfileImageUrl,
  },
)
let profilePressTimer: ReturnType<typeof window.setTimeout> | null = null

const clearProfilePress = () => {
  if (profilePressTimer === null) return
  window.clearTimeout(profilePressTimer)
  profilePressTimer = null
}

const startProfilePress = (event: PointerEvent) => {
  if (isChildRoute.value) return
  if (event.button !== 0) return
  clearProfilePress()
  profilePressTimer = window.setTimeout(() => {
    isProfileSheetOpen.value = true
    profilePressTimer = null
  }, 450)
}

const openProfileSheet = () => {
  clearProfilePress()
  if (isChildRoute.value) {
    isChildAccountSheetOpen.value = true
    return
  }
  isProfileSheetOpen.value = true
}

const closeProfileSheet = () => {
  isProfileSheetOpen.value = false
}

const selectProfile = (profileId: number) => {
  if (profileId === selectedProfileId.value) {
    closeProfileSheet()
    return
  }

  selectedProfileId.value = profileId
  setCurrentChildId(profileId)
  closeProfileSheet()
  showToast(`${activeProfile.value.name} 프로필로 전환했습니다.`, 'success')
  window.location.reload()
}

const goToAlarm = () => router.push('/alarm')

const openGuide = () => window.dispatchEvent(new CustomEvent('azas:open-home-guide'))

const goToAddChild = () => {
  closeProfileSheet()
  router.push({ name: 'ChildAdd' })
}

const goBack = () => {
  router.back()
}

const closeChildAccountSheet = () => {
  isChildAccountSheetOpen.value = false
}

const logoutChildAccount = async () => {
  closeChildAccountSheet()
  try {
    const refreshToken = getRefreshToken()
    if (refreshToken) await api.logoutUsingPOST({ refresh_token: refreshToken })
  } finally {
    clearAuthSession()
    showToast('로그아웃되었습니다.', 'info')
    await router.push({ name: 'Login' })
  }
}

const updateScrollState = () => {
  isScrolled.value = window.scrollY > props.scrollThreshold
}

const setChildFallbackProfile = () => {
  profiles.value = [{
    id: 0,
    name: '아이',
    detail: '아이 계정',
    image: defaultProfileImageUrl,
  }]
  selectedProfileId.value = 0
}

const loadProfiles = async () => {
  isProfileLoading.value = true
  try {
    if (isChildRoute.value) {
      const { data } = await api.getDashboardUsingGET()
      profiles.value = [{
        id: data.child?.child_id ?? 0,
        name: data.child?.name?.trim() || '아이',
        detail: '아이 계정',
        image: data.child?.profile_image_url || defaultProfileImageUrl,
      }]
      selectedProfileId.value = profiles.value[0]?.id ?? null
      return
    }

    const authorization = requireAuthorizationHeader()
    const { data } = await api.getChildrenUsingGET(authorization)
    const childrenByAge = [...(data.items ?? [])].sort((firstChild, secondChild) => {
      const firstAge =
        firstChild.birth_status === 'EXPECTED' || typeof firstChild.age !== 'number'
          ? -1
          : firstChild.age
      const secondAge =
        secondChild.birth_status === 'EXPECTED' || typeof secondChild.age !== 'number'
          ? -1
          : secondChild.age

      return secondAge - firstAge
    })

    profiles.value = childrenByAge.flatMap((child) => {
      if (!child.child_id) return []

      return [{
        id: child.child_id,
        name: child.name?.trim() || '아이',
        detail:
          child.birth_status === 'EXPECTED'
            ? '출산 예정'
            : typeof child.age === 'number'
              ? `${child.age}세`
              : '나이 정보 없음',
        image: child.profile_image_url || defaultProfileImageUrl,
      }]
    })
    const storedChildId = getStoredCurrentChildId()
    selectedProfileId.value = profiles.value.some(({ id }) => id === storedChildId)
      ? storedChildId
      : profiles.value[0]?.id ?? null
  } catch (error) {
    if (import.meta.env.DEV) console.warn('AppHeader profile load failed.', error)
    if (isChildRoute.value) {
      setChildFallbackProfile()
    } else {
      profiles.value = []
      selectedProfileId.value = null
    }
  } finally {
    isProfileLoading.value = false
  }
}

onMounted(() => {
  updateScrollState()
  window.addEventListener('scroll', updateScrollState, { passive: true })
  void loadProfiles()
})

watch(() => route.path, () => {
  closeProfileSheet()
  closeChildAccountSheet()
  void loadProfiles()
})

onBeforeUnmount(() => {
  clearProfilePress()
  window.removeEventListener('scroll', updateScrollState)
})
</script>

<template>
  <header
    class="fixed top-0 left-1/2 z-[var(--z-index-header)] h-[calc(var(--app-header-height)+env(safe-area-inset-top))] w-full max-w-[var(--app-max-width)] -translate-x-1/2 border-b bg-[var(--color-surface)] transition-[background-color,border-color] duration-300 ease-out"
    :class="hideAppliedDivider ? 'border-transparent' : 'border-[var(--color-border)]'"
    :style="
      appliedHeaderBackgroundColor ? { backgroundColor: appliedHeaderBackgroundColor } : undefined
    "
  >
    <div
      class="relative flex h-[var(--app-header-height)] items-center justify-between px-[var(--space-5)] pt-[env(safe-area-inset-top)]"
    >
      <button
        v-if="showBack"
        class="-ml-3 grid size-11 flex-[0_0_44px] cursor-pointer place-items-center rounded-full border-0 bg-transparent p-0 text-[var(--color-unselected-text)] active:bg-[var(--color-unselected-background)]"
        type="button"
        aria-label="뒤로가기"
        @click="goBack"
      >
        <ChevronLeft :size="28" :stroke-width="2.5" />
      </button>

      <div
        v-if="!showBack && isProfileLoading"
        class="flex min-w-0 items-center gap-[var(--space-3)]"
        aria-label="아이 프로필 불러오는 중"
        aria-busy="true"
      >
        <span
          class="header-skeleton size-[38px] flex-[0_0_38px] rounded-full"
          aria-hidden="true"
        ></span>
        <span class="header-skeleton h-5 w-[72px] rounded-md" aria-hidden="true"></span>
        <span
          v-if="!isChildRoute"
          class="header-skeleton size-4 shrink-0 rounded"
          aria-hidden="true"
        ></span>
      </div>

      <button
        v-else-if="!showBack"
        class="flex min-w-0 select-none items-center gap-[var(--space-3)] rounded-xl border-0 bg-transparent p-0 pr-2 text-left active:bg-[var(--color-unselected-background)]"
        type="button"
        :aria-label="isChildRoute ? '아이 계정 메뉴 열기' : '프로필 전환 메뉴 열기'"
        @pointerdown="startProfilePress"
        @pointerup="clearProfilePress"
        @pointercancel="clearProfilePress"
        @pointerleave="clearProfilePress"
        @click="openProfileSheet"
        @contextmenu.prevent
        @keydown.enter.prevent="openProfileSheet"
        @keydown.space.prevent="openProfileSheet"
      >
        <span
          class="grid size-[38px] flex-[0_0_38px] place-items-center overflow-hidden rounded-full bg-[var(--color-selected-background)] text-xl transition-colors duration-300 ease-out"
          :style="
            appliedProfileBackgroundColor
              ? { backgroundColor: appliedProfileBackgroundColor }
              : undefined
          "
          aria-hidden="true"
        >
          <img
            v-if="activeProfile.image"
            class="size-full object-cover"
            :src="activeProfile.image"
            alt=""
          />
          <span v-else>{{ props.profileEmoji }}</span>
        </span>
        <strong class="text-[length:var(--font-size-lg)] leading-none">
          {{ activeProfile.name }}
        </strong>
        <ChevronDown
          :size="18"
          :stroke-width="2.5"
          class="-ml-1 shrink-0 text-[var(--color-unselected-text)]"
          aria-hidden="true"
        />
      </button>

      <div v-if="showNotification || showGuide" class="flex shrink-0 items-center">
        <button
          v-if="showGuide"
          class="grid size-10 cursor-pointer place-items-center rounded-full border-0 bg-transparent p-0 text-[var(--color-unselected-text)] active:bg-[var(--color-unselected-background)]"
          type="button"
          aria-label="홈 사용 안내 보기"
          @click="openGuide"
        >
          <CircleHelp :size="23" :stroke-width="2.3" />
        </button>
        <button
          v-if="showNotification"
          class="relative grid size-11 flex-[0_0_44px] cursor-pointer place-items-center rounded-full border-0 bg-transparent p-0 text-[var(--color-unselected-text)] active:bg-[var(--color-unselected-background)]"
          type="button"
          aria-label="알림 보기"
          @click="goToAlarm"
        >
          <Bell :size="25" :stroke-width="2.4" />
          <span
            v-if="notificationCount > 0"
            class="absolute top-1 right-0 grid size-[18px] place-items-center rounded-full bg-[#f04c5d] text-[10px] font-bold text-white"
          >
            {{ notificationCount > 9 ? '9+' : notificationCount }}
          </span>
        </button>
      </div>
      <div v-else id="app-header-action" class="size-11 flex-[0_0_44px]" />
    </div>
  </header>

  <Teleport to="body">
    <Transition name="profile-sheet">
      <div
        v-if="isProfileSheetOpen && !isChildRoute"
        class="fixed inset-y-0 left-1/2 z-[var(--z-index-overlay)] w-full max-w-[var(--app-max-width)] -translate-x-1/2"
      >
        <button
          class="absolute inset-0 size-full border-0 bg-black/40"
          type="button"
          aria-label="프로필 메뉴 닫기"
          @click="closeProfileSheet"
        ></button>

        <section
          class="profile-sheet__panel absolute right-0 bottom-0 left-0 rounded-t-[28px] bg-white px-5 pt-3 pb-[calc(24px+env(safe-area-inset-bottom))] shadow-[0_-12px_36px_rgba(31,52,62,0.16)]"
          role="dialog"
          aria-modal="true"
          aria-labelledby="profile-sheet-title"
        >
          <div class="mx-auto h-1.5 w-10 rounded-full bg-[#d8e0e5]" aria-hidden="true"></div>

          <ul class="mt-5 m-0 list-none space-y-2 p-0">
            <li v-for="profile in profiles" :key="profile.id">
              <button
                class="flex min-h-[68px] w-full items-center gap-3 rounded-xl border px-3.5 text-left transition-colors"
                :class="
                  profile.id === selectedProfileId
                    ? 'border-[var(--color-brand-primary)] bg-[var(--color-selected-background)]'
                    : 'border-[var(--color-border)] bg-white active:bg-[var(--color-unselected-background)]'
                "
                type="button"
                @click="selectProfile(profile.id)"
              >
                <span
                  class="grid size-10 shrink-0 place-items-center overflow-hidden rounded-full bg-white"
                  aria-hidden="true"
                >
                  <img
                    v-if="profile.image"
                    class="size-full object-cover"
                    :src="profile.image"
                    alt=""
                  />
                  <span v-else>{{ props.profileEmoji }}</span>
                </span>
                <span class="min-w-0 flex-1">
                  <strong class="block truncate text-sm font-bold">{{ profile.name }}</strong>
                  <span class="mt-0.5 block text-[11px] text-[var(--color-text-secondary)]">
                    {{ profile.detail }}
                  </span>
                </span>
                <span
                  v-if="profile.id === selectedProfileId"
                  class="grid size-6 shrink-0 place-items-center rounded-full bg-[var(--color-brand-primary)] text-white"
                  aria-label="현재 프로필"
                >
                  <Check :size="14" :stroke-width="3" />
                </span>
              </button>
            </li>
          </ul>

          <button
            class="mt-4 flex min-h-13 w-full items-center gap-3 rounded-xl border border-[var(--color-border)] bg-white px-4 text-left active:bg-[var(--color-unselected-background)]"
            type="button"
            @click="goToAddChild"
          >
            <span
              class="grid size-9 shrink-0 place-items-center rounded-full bg-[var(--color-unselected-background)] text-[var(--color-text-secondary)]"
              aria-hidden="true"
            >
              <Plus :size="20" :stroke-width="2.5" />
            </span>
            <strong class="min-w-0 flex-1 text-sm font-bold text-[var(--color-text-primary)]">
              아이 추가
            </strong>
            <ChevronRight :size="18" class="text-[var(--color-text-secondary)]" />
          </button>

        </section>
      </div>
    </Transition>
  </Teleport>

  <Teleport to="body">
    <Transition name="profile-sheet">
      <div
        v-if="isChildAccountSheetOpen && isChildRoute"
        class="fixed inset-y-0 left-1/2 z-[var(--z-index-overlay)] w-full max-w-[var(--app-max-width)] -translate-x-1/2"
      >
        <button
          class="absolute inset-0 size-full border-0 bg-black/40"
          type="button"
          aria-label="아이 계정 메뉴 닫기"
          @click="closeChildAccountSheet"
        ></button>

        <section
          class="profile-sheet__panel absolute right-0 bottom-0 left-0 rounded-t-[28px] bg-white px-5 pt-3 pb-[calc(24px+env(safe-area-inset-bottom))] shadow-[0_-12px_36px_rgba(31,52,62,0.16)]"
          role="dialog"
          aria-modal="true"
          aria-labelledby="child-account-sheet-title"
        >
          <div class="mx-auto h-1.5 w-10 rounded-full bg-[#d8e0e5]" aria-hidden="true"></div>

          <header class="mt-5 flex items-start justify-between gap-4">
            <div class="flex min-w-0 items-center gap-3">
              <span
                class="grid size-12 shrink-0 place-items-center overflow-hidden rounded-full bg-[var(--color-selected-background)]"
                aria-hidden="true"
              >
                <img
                  v-if="activeProfile.image"
                  class="size-full object-cover"
                  :src="activeProfile.image"
                  alt=""
                />
                <span v-else>{{ props.profileEmoji }}</span>
              </span>
              <div class="min-w-0">
                <h2
                  id="child-account-sheet-title"
                  class="m-0 truncate text-[18px] font-extrabold text-[var(--color-text-primary)]"
                >
                  {{ activeProfile.name }}
                </h2>
                <p class="mt-1 mb-0 text-[12px] text-[var(--color-text-secondary)]">
                  아이 계정으로 이용 중이에요.
                </p>
              </div>
            </div>
            <button
              class="grid size-9 shrink-0 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[#f0f3f5]"
              type="button"
              aria-label="아이 계정 메뉴 닫기"
              @click="closeChildAccountSheet"
            >
              <X :size="20" />
            </button>
          </header>

          <div class="mt-5 flex items-center gap-3 rounded-[16px] bg-[#f1f9fd] p-4">
            <ShieldCheck class="shrink-0 text-[var(--color-brand-primary)]" :size="25" />
            <p class="m-0 text-[12px] leading-[1.55] text-[var(--color-text-secondary)]">
              로그아웃해도 용돈 내역과 미션 기록은 안전하게 보관돼요.
            </p>
          </div>

          <div class="mt-5 grid grid-cols-2 gap-3">
            <button
              class="h-12 rounded-[13px] border border-[var(--color-border)] bg-white text-[14px] font-bold text-[var(--color-unselected-text)]"
              type="button"
              @click="closeChildAccountSheet"
            >
              취소
            </button>
            <button
              class="flex h-12 items-center justify-center gap-1.5 rounded-[13px] bg-[var(--color-brand-primary)] text-[14px] font-bold text-white active:bg-[var(--color-brand-primary-pressed)]"
              type="button"
              @click="logoutChildAccount"
            >
              <LogOut :size="17" />
              로그아웃
            </button>
          </div>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.header-skeleton {
  background: linear-gradient(90deg, #e9eef1 25%, #f7f9fa 50%, #e9eef1 75%);
  background-size: 200% 100%;
  animation: header-skeleton-shimmer 1.35s ease-in-out infinite;
}

@keyframes header-skeleton-shimmer {
  from {
    background-position: 200% 0;
  }

  to {
    background-position: -200% 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .header-skeleton {
    animation: none;
  }
}

.profile-sheet-enter-active,
.profile-sheet-leave-active {
  transition: opacity 200ms ease;
}

.profile-sheet-enter-active .profile-sheet__panel,
.profile-sheet-leave-active .profile-sheet__panel {
  transition: transform 260ms cubic-bezier(0.22, 1, 0.36, 1);
}

.profile-sheet-enter-from,
.profile-sheet-leave-to {
  opacity: 0;
}

.profile-sheet-enter-from .profile-sheet__panel,
.profile-sheet-leave-to .profile-sheet__panel {
  transform: translateY(100%);
}
</style>
