<script setup lang="ts">
import { X } from 'lucide-vue-next'

import AppBottomNavigation from '@/components/layout/AppBottomNavigation.vue'

withDefaults(
  defineProps<{
    open: boolean
    title: string
    titleId: string
    closeLabel?: string
  }>(),
  {
    closeLabel: '창 닫기',
  },
)

const emit = defineEmits<{
  close: []
}>()
</script>

<template>
  <Teleport to="body">
    <Transition name="asset-sheet">
      <div
        v-if="open"
        class="asset-sheet-overlay fixed inset-0 z-[var(--z-index-overlay)] overflow-y-auto bg-black/35"
        role="presentation"
        @click.self="emit('close')"
      >
        <div class="flex min-h-full w-full items-end justify-center" @click.self="emit('close')">
          <section
            class="asset-sheet-panel flex w-full max-w-[var(--app-max-width)] flex-col overflow-hidden rounded-t-[20px] bg-white text-[var(--color-text-primary)]"
            role="dialog"
            aria-modal="true"
            :aria-labelledby="titleId"
          >
            <div class="px-6 pt-5 pb-[calc(var(--app-bottom-nav-height)+18px)]">
              <header class="flex items-center justify-between">
                <h2 :id="titleId" class="m-0 text-[20px] font-semibold">{{ title }}</h2>
                <button
                  class="grid size-8 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[var(--color-unselected-background)]"
                  type="button"
                  :aria-label="closeLabel"
                  @click="emit('close')"
                >
                  <X :size="19" :stroke-width="2.5" />
                </button>
              </header>

              <slot />
            </div>

            <AppBottomNavigation />
          </section>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.asset-sheet-overlay {
  transition: background-color 260ms ease;
}

.asset-sheet-panel {
  transition: transform 300ms cubic-bezier(0.22, 1, 0.36, 1);
}

.asset-sheet-enter-from,
.asset-sheet-leave-to {
  background-color: transparent;
}

.asset-sheet-enter-from .asset-sheet-panel,
.asset-sheet-leave-to .asset-sheet-panel {
  transform: translateY(100%);
}

.asset-sheet-leave-active .asset-sheet-panel {
  transition-timing-function: cubic-bezier(0.4, 0, 1, 1);
  transition-duration: 240ms;
}
</style>
