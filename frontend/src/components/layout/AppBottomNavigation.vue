<script setup lang="ts">
import { ChartNoAxesColumn, ChartSpline, ClipboardCheck, Clock3, Home } from 'lucide-vue-next'

const navigationItems = [
  { label: '홈', path: '/home', icon: Home },
  { label: '자산 관리', path: '/assets', icon: ChartSpline },
  { label: '타임 캡슐', path: '/time-capsules', icon: Clock3 },
  { label: '체크리스트', path: '/checklists', icon: ClipboardCheck },
  { label: '리포트', path: '/reports', icon: ChartNoAxesColumn },
]
</script>

<template>
  <nav class="app-bottom-navigation" aria-label="주요 메뉴">
    <RouterLink
      v-for="item in navigationItems"
      :key="item.path"
      class="app-bottom-navigation__item"
      :to="item.path"
    >
      <component :is="item.icon" class="app-bottom-navigation__icon" :size="28" :stroke-width="2.5" />
      <span>{{ item.label }}</span>
    </RouterLink>
  </nav>
</template>

<style scoped>
.app-bottom-navigation {
  position: fixed;
  bottom: 0;
  left: 50%;
  z-index: var(--z-index-bottom-nav);
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  width: 100%;
  max-width: var(--app-max-width);
  min-height: calc(var(--app-bottom-nav-height) + env(safe-area-inset-bottom));
  padding: var(--space-3) var(--space-2) calc(var(--space-3) + env(safe-area-inset-bottom));
  transform: translateX(-50%);
  background: var(--color-surface);
  border-top: 1px solid var(--color-border);
}

.app-bottom-navigation__item {
  display: grid;
  place-items: center;
  gap: var(--space-2);
  min-width: 0;
  color: var(--color-unselected-text);
  font-size: var(--font-size-xs);
  font-weight: 700;
  line-height: 1;
}

.app-bottom-navigation__item.router-link-active {
  color: var(--color-selected-text);
}

.app-bottom-navigation__icon {
  display: block;
}
</style>
