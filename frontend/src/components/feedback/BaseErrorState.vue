<script setup lang="ts">
withDefaults(
  defineProps<{
    title?: string
    description?: string
  }>(),
  {
    title: '문제가 발생했습니다',
    description: '잠시 후 다시 시도해 주세요.',
  },
)
</script>

<template>
  <section class="base-error-state" role="alert">
    <div class="base-error-state__symbol" aria-hidden="true">!</div>
    <h2>{{ title }}</h2>
    <p>{{ description }}</p>
    <slot />
  </section>
</template>

<style scoped>
.base-error-state {
  display: grid;
  place-items: center;
  gap: var(--space-3);
  min-height: 180px;
  padding: var(--space-6) var(--space-5);
  text-align: center;
  background: #fff1f0;
  border: 1px solid #ffd6d2;
  border-radius: var(--radius-lg);
}

.base-error-state__symbol {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  color: #b42318;
  font-weight: 800;
  background: #ffe5e2;
  border-radius: 999px;
}

.base-error-state h2,
.base-error-state p {
  margin: 0;
}

.base-error-state h2 {
  font-size: var(--font-size-lg);
}

.base-error-state p {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.6;
}
</style>
