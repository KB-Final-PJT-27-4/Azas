<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ChevronLeft } from 'lucide-vue-next'

const props = withDefaults(
  defineProps<{
    title: string
    fixed?: boolean
    backLabel?: string
    backgroundColor?: string
    hideDivider?: boolean
    changeOnScroll?: boolean
    scrollThreshold?: number
  }>(),
  {
    fixed: true,
    backLabel: '뒤로가기',
    backgroundColor: '',
    hideDivider: false,
    changeOnScroll: false,
    scrollThreshold: 12,
  },
)

defineEmits<{
  back: []
}>()

const isScrolled = ref(false)
const useTopAppearance = computed(() => !props.changeOnScroll || !isScrolled.value)
const appliedHeaderBackgroundColor = computed(() =>
  useTopAppearance.value ? props.backgroundColor : '',
)
const hideAppliedDivider = computed(() => props.hideDivider && useTopAppearance.value)

const updateScrollState = () => {
  isScrolled.value = window.scrollY > props.scrollThreshold
}

onMounted(() => {
  updateScrollState()
  window.addEventListener('scroll', updateScrollState, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', updateScrollState)
})
</script>

<template>
  <header
    class="h-[calc(var(--app-header-height)+env(safe-area-inset-top))] border-b bg-[var(--color-surface)] transition-[background-color,border-color] duration-300 ease-out"
    :class="[
      fixed
        ? 'fixed top-0 left-1/2 z-[var(--z-index-header)] w-full max-w-[var(--app-max-width)] -translate-x-1/2'
        : 'relative w-full shrink-0',
      hideAppliedDivider ? 'border-transparent' : 'border-[var(--color-border)]',
    ]"
    :style="
      appliedHeaderBackgroundColor ? { backgroundColor: appliedHeaderBackgroundColor } : undefined
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
