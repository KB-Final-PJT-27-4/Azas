<script setup lang="ts">
import { ChevronLeft } from 'lucide-vue-next'

withDefaults(
  defineProps<{
    title: string
    fixed?: boolean
    backLabel?: string
  }>(),
  {
    fixed: true,
    backLabel: '뒤로가기',
  },
)

defineEmits<{
  back: []
}>()
</script>

<template>
  <header
    class="h-[calc(var(--app-header-height)+env(safe-area-inset-top))] border-b border-[var(--color-border)] bg-[var(--color-surface)]"
    :class="
      fixed
        ? 'fixed top-0 left-1/2 z-[var(--z-index-header)] w-full max-w-[var(--app-max-width)] -translate-x-1/2'
        : 'relative w-full shrink-0'
    "
  >
    <div
      class="grid h-[var(--app-header-height)] grid-cols-[44px_minmax(0,1fr)_44px] items-center px-[var(--space-5)] pt-[env(safe-area-inset-top)]"
    >
      <button
        class="-ml-3 grid size-11 cursor-pointer place-items-center rounded-full border-0 bg-transparent p-0 text-[var(--color-unselected-text)] active:bg-[var(--color-unselected-background)]"
        type="button"
        :aria-label="backLabel"
        @click="$emit('back')"
      >
        <ChevronLeft :size="28" :stroke-width="2.5" />
      </button>

      <h1
        class="m-0 truncate text-center text-[length:var(--font-size-md)] font-extrabold text-[var(--color-text-primary)]"
      >
        {{ title }}
      </h1>

      <div class="size-11" aria-hidden="true"></div>
    </div>
  </header>
</template>
