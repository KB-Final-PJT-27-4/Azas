<script setup lang="ts">
withDefaults(
  defineProps<{
    title: string
    description?: string
  }>(),
  {
    description: undefined,
  },
)
</script>

<template>
  <section class="base-empty-state">
    <div class="base-empty-state__symbol" aria-hidden="true"></div>
    <h2>{{ title }}</h2>
    <p v-if="description">{{ description }}</p>
    <slot />
  </section>
</template>

<style scoped>
.base-empty-state {
  display: grid;
  place-items: center;
  gap: var(--space-3);
  min-height: 180px;
  padding: var(--space-6) var(--space-5);
  text-align: center;
  background: var(--color-unselected-background);
  border-radius: var(--radius-lg);
}

.base-empty-state__symbol {
  width: 44px;
  height: 44px;
  background: var(--color-brand-secondary);
  border-radius: 999px;
}

.base-empty-state h2,
.base-empty-state p {
  margin: 0;
}

.base-empty-state h2 {
  font-size: var(--font-size-lg);
}

.base-empty-state p {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.6;
}
</style>
