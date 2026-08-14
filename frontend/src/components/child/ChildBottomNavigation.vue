<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ClipboardCheck, House, Trophy, WalletCards } from 'lucide-vue-next'

const route = useRoute()

const navItems = [
  { label: '홈', to: '/child/home', icon: House, match: ['/child/home'] },
  { label: '내 자산', to: '/child/assets', icon: WalletCards, match: ['/child/assets'] },
  { label: '미션', to: '/child/missions', icon: ClipboardCheck, match: ['/child/missions'] },
  { label: '퀴즈', to: '/child/quiz', icon: Trophy, match: ['/child/quiz'] },
]

const currentPath = computed(() => route.path)
</script>

<template>
  <nav
    class="fixed bottom-0 left-1/2 z-[var(--z-index-bottom-nav)] grid h-[calc(var(--app-bottom-nav-height)+env(safe-area-inset-bottom))] w-full max-w-[var(--app-max-width)] -translate-x-1/2 grid-cols-4 border-t border-[var(--color-border)] bg-[var(--color-surface)] px-[var(--space-2)] pt-[6px] pb-[calc(5px+env(safe-area-inset-bottom))]"
    aria-label="아이 하단 내비게이션"
  >
    <RouterLink
      v-for="item in navItems"
      :key="item.label"
      class="grid min-w-0 place-items-center gap-[3px] text-[11px] leading-none font-medium whitespace-nowrap !text-[var(--color-unselected-text)] no-underline"
      :class="item.match.includes(currentPath) ? 'font-semibold !text-[var(--color-selected-text)]' : ''"
      :to="item.to"
    >
      <component :is="item.icon" class="block" :size="22" :stroke-width="2.5" />
      <span>{{ item.label }}</span>
    </RouterLink>
  </nav>
</template>
