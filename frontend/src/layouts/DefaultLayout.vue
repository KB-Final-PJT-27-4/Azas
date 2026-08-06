<script setup lang="ts">
import AppBottomNavigation from '@/components/layout/AppBottomNavigation.vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const hideNavigation = computed(() => route.meta.hideNavigation === true)
const hideBottomNavigation = computed(
  () => hideNavigation.value || route.meta.hideBottomNavigation === true,
)
const headerTitle = computed(() => String(route.meta.headerTitle ?? ''))
const showHeaderBack = computed(() => route.meta.showHeaderBack === true)
const showHeaderNotification = computed(() => route.meta.showHeaderNotification !== false)
const notificationCount = computed(() => Number(route.meta.notificationCount ?? 0))
</script>

<template>
  <div class="default-layout">
    <div class="default-layout__shell">
      <AppHeader
        v-if="!hideNavigation"
        title="깨비"
        profile-name="깨비"
        :center-title="headerTitle"
        :show-back="showHeaderBack"
        :show-notification="showHeaderNotification"
        :notification-count="notificationCount"
      />
      <div
        class="default-layout__content"
        :class="{
          'default-layout__content--without-navigation': hideNavigation,
          'default-layout__content--without-bottom-navigation': hideBottomNavigation,
        }"
      >
        <slot />
      </div>
      <AppBottomNavigation v-if="!hideBottomNavigation" />
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
  display: flow-root;
  min-height: 100dvh;
  padding-top: calc(var(--app-header-height) + env(safe-area-inset-top));
  padding-bottom: calc(var(--app-bottom-nav-height) + env(safe-area-inset-bottom));
}

.default-layout__content--without-navigation {
  padding-top: 0;
  padding-bottom: 0;
}

.default-layout__content--without-bottom-navigation {
  padding-bottom: env(safe-area-inset-bottom);
}

.default-layout__content--without-navigation :deep(main > :first-child) {
  margin-top: 0;
}
</style>
