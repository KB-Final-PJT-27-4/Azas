<script setup lang="ts">
import { Bell } from 'lucide-vue-next'

withDefaults(
  defineProps<{
    title?: string
    profileName?: string
    profileEmoji?: string
  }>(),
  {
    title: '깨비',
    profileName: '깨비',
    profileEmoji: '👶',
  },
)
</script>

<template>
  <header
    class="fixed top-0 left-1/2 z-[var(--z-index-header)] h-[calc(var(--app-header-height)+env(safe-area-inset-top))] w-full max-w-[var(--app-max-width)] -translate-x-1/2 border-b border-[var(--color-border)] bg-[var(--color-surface)]"
  >
    <div
      class="flex h-[var(--app-header-height)] items-center justify-between px-[var(--space-5)] pt-[env(safe-area-inset-top)]"
    >
      <div class="flex min-w-0 items-center gap-[var(--space-3)]">
        <div
          class="grid size-[42px] flex-[0_0_42px] place-items-center rounded-full bg-[var(--color-selected-background)] text-2xl"
          aria-hidden="true"
        >
          {{ profileEmoji }}
        </div>
        <strong class="text-[length:var(--font-size-lg)] leading-none">{{
          profileName || title
        }}</strong>
      </div>
      <button
        class="grid size-11 flex-[0_0_44px] cursor-pointer place-items-center rounded-full border-0 bg-transparent p-0 text-[var(--color-unselected-text)] active:bg-[var(--color-unselected-background)]"
        type="button"
        aria-label="알림 보기"
      >
        <Bell :size="28" :stroke-width="2.5" />
      </button>
    </div>
  </header>
</template>
