<script setup lang="ts">
import { Check, Info, X } from 'lucide-vue-next'

withDefaults(
  defineProps<{
    message: string
    variant?: 'success' | 'error' | 'info'
    withBottomNavigation?: boolean
  }>(),
  {
    variant: 'info',
    withBottomNavigation: true,
  },
)
</script>

<template>
  <div
    class="base-toast"
    :class="[
      `base-toast--${variant}`,
      { 'base-toast--without-bottom-navigation': !withBottomNavigation },
    ]"
    role="status"
  >
    <span class="base-toast__icon" aria-hidden="true">
      <Check v-if="variant === 'success'" :size="15" :stroke-width="3" />
      <X v-else-if="variant === 'error'" :size="15" :stroke-width="3" />
      <Info v-else :size="15" :stroke-width="2.5" />
    </span>
    <span>{{ message }}</span>
  </div>
</template>

<style scoped>
.base-toast {
  position: fixed;
  bottom: calc(var(--app-bottom-nav-height) + 96px);
  left: 50%;
  z-index: var(--z-index-toast);
  display: flex;
  width: max-content;
  max-width: calc(var(--app-max-width) - var(--space-8));
  min-height: 48px;
  align-items: center;
  gap: 10px;
  padding: 10px 16px 10px 12px;
  transform: translateX(-50%);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: 700;
  white-space: nowrap;
  background: #fff;
  border: 1px solid rgb(225 230 235 / 85%);
  border-radius: 16px;
  box-shadow: 0 8px 28px rgb(29 43 54 / 16%);
}

.base-toast--without-bottom-navigation {
  bottom: calc(24px + env(safe-area-inset-bottom));
}

.base-toast__icon {
  display: grid;
  width: 24px;
  height: 24px;
  flex: 0 0 24px;
  place-items: center;
  color: #fff;
  background: var(--color-brand-primary);
  border-radius: 999px;
}

.base-toast--error .base-toast__icon {
  background: #f05b61;
}

.base-toast--info .base-toast__icon {
  background: #8a96a3;
}
</style>
