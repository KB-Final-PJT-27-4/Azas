<script setup lang="ts">
defineProps<{
  title?: string
}>()
</script>

<template>
  <section class="base-card">
    <header v-if="title || $slots.header" class="base-card__header">
      <slot name="header">
        <h2>{{ title }}</h2>
      </slot>
    </header>
    <div class="base-card__body">
      <slot />
    </div>
  </section>
</template>

<style scoped>
.base-card {
  padding: var(--space-5);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
}

.base-card__header {
  margin-bottom: var(--space-3);
}

.base-card__header h2 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
}

.base-card__body {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.6;
}
</style>
