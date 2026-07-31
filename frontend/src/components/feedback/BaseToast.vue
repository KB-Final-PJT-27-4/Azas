<script setup lang="ts">
withDefaults(
  defineProps<{
    message: string
    variant?: 'success' | 'error' | 'info'
  }>(),
  {
    variant: 'info',
  },
)
</script>

<template>
  <div class="base-toast" :class="`base-toast--${variant}`" role="status">
    {{ message }}
  </div>
</template>

<style scoped>
.base-toast {
  position: fixed;
  right: var(--space-4);
  bottom: calc(var(--app-bottom-nav-height) + var(--space-4));
  left: var(--space-4);
  z-index: var(--z-index-toast);
  max-width: calc(var(--app-max-width) - var(--space-8));
  margin: 0 auto;
  padding: var(--space-3) var(--space-4);
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: 700;
  background: var(--color-selected-background);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.base-toast--success {
  color: #177245;
  background: #e9fbf1;
}

.base-toast--error {
  color: #b42318;
  background: #fff1f0;
}

.base-toast--info {
  color: var(--color-selected-text);
  background: var(--color-selected-background);
}
</style>
