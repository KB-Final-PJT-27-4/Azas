<script setup lang="ts">
import { ClipboardCheck, House, Menu, UserRound, WalletCards } from 'lucide-vue-next'
import { useRoute } from 'vue-router'

const route = useRoute()

const navigationItems = [
  { label: '홈', path: '/home', icon: House },
  { label: '계좌', path: '/assets', icon: WalletCards },
  { label: '체크리스트', path: '/checklists', icon: ClipboardCheck },
  { label: '마이페이지', path: '/mypage', icon: UserRound },
  { label: '전체', path: '/menu', icon: Menu },
]

const isMypageRoute = (path: string) => path === '/mypage' && route.path.startsWith('/mypage')
</script>

<template>
  <nav
    class="fixed bottom-0 left-1/2 z-[var(--z-index-bottom-nav)] grid h-[calc(var(--app-bottom-nav-height)+env(safe-area-inset-bottom))] w-full max-w-[var(--app-max-width)] -translate-x-1/2 grid-cols-5 border-t border-[var(--color-border)] bg-[var(--color-surface)] px-[var(--space-2)] pt-[6px] pb-[calc(5px+env(safe-area-inset-bottom))]"
    aria-label="주요 메뉴"
  >
    <RouterLink
      v-for="item in navigationItems"
      :key="item.path"
      class="grid min-w-0 place-items-center gap-[3px] text-[11px] leading-none font-medium whitespace-nowrap !text-[var(--color-unselected-text)] [&.bottom-nav-item--active]:font-semibold [&.bottom-nav-item--active]:!text-[var(--color-selected-text)] [&.router-link-active]:font-semibold [&.router-link-active]:!text-[var(--color-selected-text)]"
      :class="{ 'bottom-nav-item--active': isMypageRoute(item.path) }"
      :to="item.path"
      :data-home-guide="item.path === '/mypage' ? 'mypage' : undefined"
    >
      <component :is="item.icon" class="block" :size="22" :stroke-width="2.5" />
      <span>{{ item.label }}</span>
    </RouterLink>
  </nav>
</template>
