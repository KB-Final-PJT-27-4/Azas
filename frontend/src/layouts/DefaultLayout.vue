<script setup lang="ts">
import AppBottomNavigation from '@/components/layout/AppBottomNavigation.vue'
import AppHeader from '@/components/layout/AppHeader.vue'
</script>

<template>
  <div class="default-layout">
    <div class="default-layout__shell">
      <AppHeader title="Azas" />
      <div class="default-layout__content">
        <slot />
      </div>
      <AppBottomNavigation />
    </div>
  </div>
</template>

<style scoped>
.default-layout {
  min-height: 100dvh;
  background: var(--color-app-background);
}

.default-layout__shell {
  width: 100%;
  max-width: var(--app-max-width);
  min-height: 100dvh;
  margin: 0 auto;
  background: var(--color-surface);
  box-shadow: 0 0 0 1px var(--color-border);
}

.default-layout__content {
  min-height: 100dvh;
  padding-top: calc(var(--app-header-height) + env(safe-area-inset-top));
  padding-bottom: calc(var(--app-bottom-nav-height) + env(safe-area-inset-bottom));
}
</style>
