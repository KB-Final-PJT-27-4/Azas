<script setup lang="ts">
import { Bell, ChevronLeft } from 'lucide-vue-next'
import { useRouter } from 'vue-router'

import defaultProfileImageUrl from '@/assets/images/home/home-profile-baby.png'

const router = useRouter()

withDefaults(
  defineProps<{
    title?: string
    centerTitle?: string
    profileName?: string
    profileImage?: string
    profileEmoji?: string
    showBack?: boolean
    showNotification?: boolean
  }>(),
  {
    title: '깨비',
    centerTitle: undefined,
    profileName: '깨비',
    profileImage: defaultProfileImageUrl,
    profileEmoji: '👶',
    showBack: false,
    showNotification: true,
  },
)

const goBack = () => {
  router.back()
}
</script>

<template>
  <header
    class="fixed top-0 left-1/2 z-[var(--z-index-header)] h-[calc(var(--app-header-height)+env(safe-area-inset-top))] w-full max-w-[var(--app-max-width)] -translate-x-1/2 border-b border-[var(--color-border)] bg-[var(--color-surface)]"
  >
    <div
      class="relative flex h-[var(--app-header-height)] items-center justify-between px-[var(--space-5)] pt-[env(safe-area-inset-top)]"
    >
      <button
        v-if="showBack"
        class="grid size-11 flex-[0_0_44px] cursor-pointer place-items-center rounded-full border-0 bg-transparent p-0 text-[var(--color-unselected-text)] active:bg-[var(--color-unselected-background)]"
        type="button"
        aria-label="뒤로가기"
        @click="goBack"
      >
        <ChevronLeft :size="28" :stroke-width="2.5" />
      </button>

      <div v-else class="flex min-w-0 items-center gap-[var(--space-3)]">
        <div
          class="grid size-[42px] flex-[0_0_42px] place-items-center overflow-hidden rounded-full bg-[var(--color-selected-background)] text-2xl"
          aria-hidden="true"
        >
          <img
            v-if="profileImage"
            class="size-full object-cover"
            :src="profileImage"
            alt=""
          />
          <span v-else>{{ profileEmoji }}</span>
        </div>
        <strong class="text-[length:var(--font-size-lg)] leading-none">{{
          profileName || title
        }}</strong>
      </div>

      <strong
        v-if="centerTitle"
        class="pointer-events-none absolute left-1/2 -translate-x-1/2 text-[length:var(--font-size-md)] font-extrabold text-[var(--color-text-primary)]"
      >
        {{ centerTitle }}
      </strong>

      <button
        v-if="showNotification"
        class="grid size-11 flex-[0_0_44px] cursor-pointer place-items-center rounded-full border-0 bg-transparent p-0 text-[var(--color-unselected-text)] active:bg-[var(--color-unselected-background)]"
        type="button"
        aria-label="알림 보기"
      >
        <Bell :size="28" :stroke-width="2.5" />
      </button>
      <div v-else class="size-11 flex-[0_0_44px]" aria-hidden="true" />
    </div>
  </header>
</template>
