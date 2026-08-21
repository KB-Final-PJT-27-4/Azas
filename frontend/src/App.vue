<script setup lang="ts">
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { RouterView } from 'vue-router'
</script>

<template>
  <DefaultLayout>
    <div class="route-page-viewport">
      <RouterView v-slot="{ Component, route }">
        <Transition name="route-page" mode="out-in">
          <div :key="route.path" class="route-page-frame">
            <component :is="Component" />
          </div>
        </Transition>
      </RouterView>
    </div>
  </DefaultLayout>
</template>

<style scoped>
.route-page-viewport {
  overflow-x: clip;
}

.route-page-enter-active,
.route-page-leave-active {
  transition:
    transform 180ms cubic-bezier(0.25, 0.8, 0.25, 1),
    opacity 140ms ease-out;
}

.route-page-enter-from {
  opacity: 0;
  transform: translateX(16px);
}

.route-page-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

@media (prefers-reduced-motion: reduce) {
  .route-page-enter-active,
  .route-page-leave-active {
    transition-duration: 1ms;
  }
}
</style>
