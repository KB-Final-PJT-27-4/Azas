<script setup lang="ts">
import { ClipboardCheck, House, Menu, UserRound, WalletCards } from 'lucide-vue-next'

const navigationItems = [
  { label: '홈', path: '/home', icon: House },
  { label: '계좌', path: '/assets', icon: WalletCards },
  { label: '체크리스트', path: '/checklists', icon: ClipboardCheck },
  { label: '마이페이지', path: '/mypage', icon: UserRound },
  { label: '전체', path: '/menu', icon: Menu },
]
</script>

<template>
  <nav
    class="fixed bottom-0 left-1/2 z-[var(--z-index-bottom-nav)] grid h-[calc(var(--app-bottom-nav-height)+env(safe-area-inset-bottom))] w-full max-w-[var(--app-max-width)] -translate-x-1/2 grid-cols-5 border-t border-[var(--color-border)] bg-[var(--color-surface)] px-[var(--space-2)] pt-[6px] pb-[calc(5px+env(safe-area-inset-bottom))]"
    aria-label="주요 메뉴"
  >
    <RouterLink
      v-for="item in navigationItems"
      :key="item.path"
      class="grid min-w-0 place-items-center gap-[3px] text-[10px] leading-none font-bold whitespace-nowrap !text-[var(--color-unselected-text)] [&.router-link-active]:!text-[var(--color-selected-text)]"
      :to="item.path"
    >
      <component :is="item.icon" class="block" :size="20" :stroke-width="2.5" />
      <span>{{ item.label }}</span>
    </RouterLink>
  </nav>
</template>
