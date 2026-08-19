<script setup lang="ts">
import AppBottomNavigation from '@/components/layout/AppBottomNavigation.vue'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppSubHeader from '@/components/layout/AppSubHeader.vue'
import { BaseToast } from '@/components/feedback'
import { useToast } from '@/composables/useToast'
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const hideNavigation = computed(() => route.meta.hideNavigation === true)
const hideBottomNavigation = computed(
  () => hideNavigation.value || route.meta.hideBottomNavigation === true,
)
const headerTitle = computed(() => String(route.meta.headerTitle ?? ''))
const showHeaderBack = computed(() => route.meta.showHeaderBack === true)
const showHeaderNotification = computed(() => route.meta.showHeaderNotification !== false)
const notificationCount = computed(() => Number(route.meta.notificationCount ?? 0))
const isHome = computed(() => route.name === 'Home')
const pageBackgroundColor = computed(() => String(route.meta.pageBackgroundColor ?? ''))
const headerBackgroundColor = computed(() =>
  String(route.meta.headerBackgroundColor ?? pageBackgroundColor.value),
)
const hideHeaderDivider = computed(() => route.meta.hideHeaderDivider === true || isHome.value)
const changeHeaderOnScroll = computed(() => route.meta.changeHeaderOnScroll === true || isHome.value)
const pageBackgroundStyle = computed(() =>
  pageBackgroundColor.value ? { backgroundColor: pageBackgroundColor.value } : undefined,
)
const { toastMessage, toastVariant, toastPlacement } = useToast()
</script>

<template>
  <div class="default-layout">
    <div class="default-layout__shell" :style="pageBackgroundStyle">
      <AppSubHeader
        v-if="!hideNavigation && showHeaderBack"
        :title="headerTitle"
        :background-color="headerBackgroundColor || undefined"
        :hide-divider="hideHeaderDivider"
        :change-on-scroll="changeHeaderOnScroll"
        @back="router.back()"
      />
      <AppHeader
        v-else-if="!hideNavigation"
        title="깨비"
        profile-name="깨비"
        :show-notification="showHeaderNotification"
        :notification-count="notificationCount"
        :background-color="headerBackgroundColor || undefined"
        :profile-background-color="isHome || headerBackgroundColor ? '#ffffff' : undefined"
        :hide-divider="hideHeaderDivider"
        :change-on-scroll="changeHeaderOnScroll"
      />
      <div
        class="default-layout__content"
        :style="pageBackgroundStyle"
        :class="{
          'default-layout__content--without-navigation': hideNavigation,
          'default-layout__content--without-bottom-navigation': hideBottomNavigation,
        }"
      >
        <slot />
      </div>
      <AppBottomNavigation v-if="!hideBottomNavigation" />
      <Transition name="global-toast">
        <BaseToast
          v-if="toastMessage"
          :message="toastMessage"
          :variant="toastVariant"
          :with-bottom-navigation="!hideBottomNavigation"
          :above-actions="toastPlacement === 'above-actions'"
          :placement="toastPlacement"
        />
      </Transition>
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

.global-toast-enter-active,
.global-toast-leave-active {
  transition:
    transform 180ms ease,
    opacity 180ms ease;
}

.global-toast-enter-from,
.global-toast-leave-to {
  transform: translate(-50%, 10px);
  opacity: 0;
}
</style>
