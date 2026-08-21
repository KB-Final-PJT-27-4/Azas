<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import {
  Bell,
  Check,
  ChevronDown,
  ChevronLeft,
  ChevronRight,
  CircleHelp,
  Plus,
  UserRound,
} from 'lucide-vue-next'
import { useRouter } from 'vue-router'

import defaultProfileImageUrl from '@/assets/images/home/home-profile-baby.png'
import { useToast } from '@/composables/useToast'

const router = useRouter()
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

const profiles = computed(() => [
  { id: 1, name: props.profileName || props.title, detail: '12세 · 초등학생' },
  { id: 2, name: '아이 2', detail: '10세 · 초등학생' },
  { id: 3, name: '아이 3', detail: '7세 · 초등학생' },
])
const selectedProfileId = ref(1)
const isProfileSheetOpen = ref(false)
const isScrolled = ref(false)
const useTopAppearance = computed(() => !props.changeOnScroll || !isScrolled.value)
const appliedHeaderBackgroundColor = computed(() =>
  useTopAppearance.value ? props.backgroundColor : '',
)
const appliedProfileBackgroundColor = computed(() =>
  useTopAppearance.value ? props.profileBackgroundColor : '',
)
const hideAppliedDivider = computed(() => props.hideDivider && useTopAppearance.value)
const activeProfile = computed(
  () => profiles.value.find(({ id }) => id === selectedProfileId.value) ?? profiles.value[0]!,
)
let profilePressTimer: ReturnType<typeof window.setTimeout> | null = null

const clearProfilePress = () => {
  if (profilePressTimer === null) return
  window.clearTimeout(profilePressTimer)
  profilePressTimer = null
}

const startProfilePress = (event: PointerEvent) => {
  if (event.button !== 0) return
  clearProfilePress()
  profilePressTimer = window.setTimeout(() => {
    isProfileSheetOpen.value = true
    profilePressTimer = null
  }, 450)
}

const openProfileSheet = () => {
  clearProfilePress()
  isProfileSheetOpen.value = true
}

const closeProfileSheet = () => {
  isProfileSheetOpen.value = false
}

const selectProfile = (profileId: number) => {
  selectedProfileId.value = profileId
  closeProfileSheet()
  showToast(`${activeProfile.value.name} 프로필로 전환했습니다.`, 'success')
}

const goToAlarm = () => router.push('/alarm')

const openGuide = () => window.dispatchEvent(new CustomEvent('azas:open-home-guide'))

const goToMypage = () => {
  closeProfileSheet()
  router.push('/mypage')
}

const goToAddChild = () => {
  closeProfileSheet()
  router.push('/mypage/family')
}

const goBack = () => {
  router.back()
}

const updateScrollState = () => {
  isScrolled.value = window.scrollY > props.scrollThreshold
}

onMounted(() => {
  updateScrollState()
  window.addEventListener('scroll', updateScrollState, { passive: true })
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

      <button
        v-else
        class="flex min-w-0 select-none items-center gap-[var(--space-3)] rounded-xl border-0 bg-transparent p-0 pr-2 text-left active:bg-[var(--color-unselected-background)]"
        type="button"
        aria-label="프로필 전환 메뉴 열기"
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
            v-if="props.profileImage"
            class="size-full object-cover"
            :src="props.profileImage"
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
        v-if="isProfileSheetOpen"
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
                    v-if="props.profileImage"
                    class="size-full object-cover"
                    :src="props.profileImage"
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

          <button
            class="mt-2 flex min-h-13 w-full items-center gap-3 rounded-xl border border-[var(--color-border)] bg-white px-4 text-left active:bg-[var(--color-unselected-background)]"
            type="button"
            @click="goToMypage"
          >
            <span
              class="grid size-9 shrink-0 place-items-center rounded-full bg-[var(--color-selected-background)] text-[var(--color-selected-text)]"
              aria-hidden="true"
            >
              <UserRound :size="19" />
            </span>
            <strong class="min-w-0 flex-1 text-sm font-bold">마이페이지로 이동</strong>
            <ChevronRight :size="18" class="text-[var(--color-text-secondary)]" />
          </button>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
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
