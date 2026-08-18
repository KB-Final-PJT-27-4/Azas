<script setup lang="ts">
import { X } from 'lucide-vue-next'
import { computed, onBeforeUnmount, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    open: boolean
    title?: string
  }>(),
  {
    title: undefined,
  },
)

const emit = defineEmits<{
  close: []
}>()

const dragOffset = ref(0)
const isDragging = ref(false)
let dragStartY = 0
let previousBodyOverflow = ''
let scrollLocked = false

const panelStyle = computed(() =>
  isDragging.value || dragOffset.value > 0
    ? { transform: `translateY(${dragOffset.value}px)` }
    : undefined,
)

const stopDragging = () => {
  if (!isDragging.value) return
  isDragging.value = false

  if (dragOffset.value >= 96) {
    emit('close')
  }
  dragOffset.value = 0
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

const removeDragListeners = () => {
  window.removeEventListener('pointermove', moveDragging)
  window.removeEventListener('pointerup', stopDragging)
  window.removeEventListener('pointercancel', stopDragging)
}

watch(isDragging, (dragging) => {
  if (!dragging) removeDragListeners()
})

watch(
  () => props.open,
  (open) => {
    dragOffset.value = 0
    isDragging.value = false

    if (open && !scrollLocked) {
      previousBodyOverflow = document.body.style.overflow
      document.body.style.overflow = 'hidden'
      scrollLocked = true
    } else if (!open && scrollLocked) {
      document.body.style.overflow = previousBodyOverflow
      scrollLocked = false
    }
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  removeDragListeners()
  if (scrollLocked) document.body.style.overflow = previousBodyOverflow
})
</script>

<template>
  <Teleport to="body">
    <Transition
      enter-active-class="transition-opacity duration-300 ease-out"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-200 ease-in"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div
        v-if="open"
        class="fixed inset-0 z-[var(--z-index-overlay)] flex items-end justify-center bg-[rgb(51_51_51_/_40%)]"
        role="presentation"
        @click.self="emit('close')"
      >
        <Transition
          appear
          enter-active-class="transition-transform duration-300 ease-out"
          enter-from-class="translate-y-full"
          enter-to-class="translate-y-0"
          leave-active-class="transition-transform duration-200 ease-in"
          leave-from-class="translate-y-0"
          leave-to-class="translate-y-full"
        >
          <section
            v-if="open"
            class="w-full max-w-[var(--app-max-width)] rounded-t-[28px] bg-[var(--color-surface)] px-[var(--space-5)] pt-3 pb-[calc(var(--space-5)+env(safe-area-inset-bottom))] text-[var(--color-text-secondary)] shadow-[0_-12px_36px_rgb(30_52_66_/_12%)]"
            :class="isDragging ? 'transition-none' : 'transition-transform duration-200 ease-out'"
            :style="panelStyle"
            role="dialog"
            aria-modal="true"
            :aria-label="title"
          >
            <button
              type="button"
              class="mx-auto mb-3 flex h-5 w-20 touch-none cursor-grab items-center justify-center border-0 bg-transparent p-0 active:cursor-grabbing"
              aria-label="바텀시트 끌어서 닫기"
              @pointerdown.prevent="startDragging"
            >
              <span class="h-1 w-10 rounded-full bg-[var(--color-border)]"></span>
            </button>

            <header
              v-if="title || $slots.header"
              class="mb-4 flex items-center justify-between gap-4"
            >
              <slot name="header">
                <h2
                  class="m-0 text-xl leading-tight font-bold tracking-[-0.03em] text-[var(--color-text-primary)]"
                >
                  {{ title }}
                </h2>
              </slot>
              <button
                type="button"
                class="grid size-9 shrink-0 place-items-center rounded-full border-0 bg-transparent text-[var(--color-text-secondary)] transition-colors hover:bg-[#f2f5f7]"
                aria-label="닫기"
                @click="emit('close')"
              >
                <X :size="23" :stroke-width="2.3" />
              </button>
            </header>
            <slot />
          </section>
        </Transition>
      </div>
    </Transition>
  </Teleport>
</template>
