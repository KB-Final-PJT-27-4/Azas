<script setup lang="ts">
import { X } from 'lucide-vue-next'
import { computed, onBeforeUnmount, ref, watch } from 'vue'

import AppBottomNavigation from '@/components/layout/AppBottomNavigation.vue'

const props = withDefaults(
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

const dragOffset = ref(0)
const isDragging = ref(false)
let dragStartY = 0

const panelStyle = computed(() =>
  isDragging.value || dragOffset.value > 0
    ? { transform: `translateY(${dragOffset.value}px)` }
    : undefined,
)

const removeDragListeners = () => {
  window.removeEventListener('pointermove', moveDragging)
  window.removeEventListener('pointerup', stopDragging)
  window.removeEventListener('pointercancel', stopDragging)
}

const stopDragging = () => {
  if (!isDragging.value) return
  isDragging.value = false

  if (dragOffset.value >= 96) emit('close')
  dragOffset.value = 0
  removeDragListeners()
}

const moveDragging = (event: PointerEvent) => {
  if (!isDragging.value) return
  dragOffset.value = Math.max(0, event.clientY - dragStartY)
}

const startDragging = (event: PointerEvent) => {
  isDragging.value = true
  dragStartY = event.clientY - dragOffset.value
  window.addEventListener('pointermove', moveDragging)
  window.addEventListener('pointerup', stopDragging, { once: true })
  window.addEventListener('pointercancel', stopDragging, { once: true })
}

watch(
  () => props.open,
  () => {
    dragOffset.value = 0
    isDragging.value = false
    removeDragListeners()
  },
)

onBeforeUnmount(removeDragListeners)
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
            :class="isDragging ? '!transition-none' : ''"
            :style="panelStyle"
            role="dialog"
            aria-modal="true"
            :aria-labelledby="titleId"
          >
            <button
              type="button"
              class="mx-auto mt-2 flex h-5 w-20 touch-none cursor-grab items-center justify-center border-0 bg-transparent p-0 active:cursor-grabbing"
              aria-label="바텀시트 끌어서 닫기"
              @pointerdown.prevent="startDragging"
            >
              <span class="h-1 w-10 rounded-full bg-[var(--color-border)]"></span>
            </button>

            <div class="px-6 pt-1 pb-[calc(var(--app-bottom-nav-height)+18px)]">
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
