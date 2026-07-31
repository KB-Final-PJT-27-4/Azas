<script setup lang="ts">
import { Bell } from 'lucide-vue-next'

withDefaults(
  defineProps<{
    title?: string
    profileName?: string
    profileEmoji?: string
  }>(),
  {
    title: '깨비',
    profileName: '깨비',
    profileEmoji: '👶',
  },
)
</script>

<template>
  <header class="app-header">
    <div class="app-header__inner">
      <div class="app-header__profile">
        <div class="app-header__avatar" aria-hidden="true">{{ profileEmoji }}</div>
        <strong class="app-header__title">{{ profileName || title }}</strong>
      </div>
      <button class="app-header__notification" type="button" aria-label="알림 보기">
        <Bell :size="28" :stroke-width="2.5" />
      </button>
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
  justify-content: space-between;
  height: var(--app-header-height);
  padding: env(safe-area-inset-top) var(--space-5) 0;
}

.app-header__profile {
  display: flex;
  gap: var(--space-3);
  align-items: center;
  min-width: 0;
}

.app-header__avatar {
  display: grid;
  flex: 0 0 42px;
  place-items: center;
  width: 42px;
  height: 42px;
  font-size: 24px;
  background: var(--color-selected-background);
  border-radius: 999px;
}

.app-header__title {
  font-size: var(--font-size-lg);
  line-height: 1;
}

.app-header__notification {
  display: grid;
  flex: 0 0 44px;
  place-items: center;
  width: 44px;
  height: 44px;
  padding: 0;
  color: var(--color-unselected-text);
  cursor: pointer;
  background: transparent;
  border: 0;
  border-radius: 999px;
}

.app-header__notification:active {
  background: var(--color-unselected-background);
}
</style>
