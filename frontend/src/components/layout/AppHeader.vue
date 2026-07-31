<script setup lang="ts">
defineProps<{
  title: string
}>()
</script>

<template>
  <header class="app-header">
    <div class="app-header__inner">
      <strong class="app-header__title">{{ title }}</strong>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 50%;
  z-index: var(--z-index-header);
  width: 100%;
  max-width: var(--app-max-width);
  height: calc(var(--app-header-height) + env(safe-area-inset-top));
  transform: translateX(-50%);
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}

.app-header__inner {
  display: flex;
  align-items: center;
  height: var(--app-header-height);
  padding: env(safe-area-inset-top) var(--space-5) 0;
}

.app-header__title {
  font-size: var(--font-size-lg);
  line-height: 1;
}
</style>
