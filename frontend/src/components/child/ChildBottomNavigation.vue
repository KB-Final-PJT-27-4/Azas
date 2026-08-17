<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ClipboardCheck, House, Plus, Trophy, WalletCards } from 'lucide-vue-next'

import ChildQuickActionSheet from '@/components/child/ChildQuickActionSheet.vue'

const route = useRoute()
const isQuickActionOpen = ref(false)

const navItems = [
  { label: '홈', to: '/child/home', icon: House, match: ['/child/home'] },
  { label: '내 자산', to: '/child/assets', icon: WalletCards, match: ['/child/assets'] },
  { label: '미션', to: '/child/missions', icon: ClipboardCheck, match: ['/child/missions'] },
  { label: '퀴즈', to: '/child/quiz', icon: Trophy, match: ['/child/quiz'] },
]

const currentPath = computed(() => route.path)
const showQuickAction = computed(() => currentPath.value !== '/child/quiz')

watch(showQuickAction, (show) => {
  if (!show) {
    isQuickActionOpen.value = false
  }
})
</script>

<template>
  <nav
    class="fixed bottom-0 left-1/2 z-[var(--z-index-bottom-nav)] grid h-[calc(var(--app-bottom-nav-height)+env(safe-area-inset-bottom))] w-full max-w-[var(--app-max-width)] -translate-x-1/2 grid-cols-4 overflow-visible border-t border-[var(--color-border)] bg-[var(--color-surface)] px-[var(--space-2)] pt-[6px] pb-[calc(5px+env(safe-area-inset-bottom))]"
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

    <button
      v-if="showQuickAction"
      class="child-quick-action-button"
      type="button"
      aria-label="돈 보내기 또는 용돈 요청하기"
      @click="isQuickActionOpen = true"
    >
      <span class="child-quick-action-button__surface">
        <Plus :size="23" :stroke-width="3" aria-hidden="true" />
      </span>
    </button>
  </nav>

  <ChildQuickActionSheet
    v-if="showQuickAction"
    :open="isQuickActionOpen"
    @close="isQuickActionOpen = false"
  />
</template>

<style scoped>
.child-quick-action-button {
  position: absolute;
  top: -47px;
  left: 50%;
  z-index: 1;
  width: 88px;
  height: 47px;
  padding: 7px 8px 0;
  color: var(--color-text-inverse);
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-bottom: 0;
  border-radius: 48px 48px 0 0;
  box-shadow: 0 -5px 16px rgb(45 91 116 / 8%);
  transform: translateX(-50%);
}

.child-quick-action-button__surface {
  position: relative;
  z-index: 1;
  display: grid;
  width: 100%;
  height: 40px;
  padding-top: 3px;
  place-items: center;
  background: var(--color-brand-primary);
  border-radius: 40px 40px 0 0;
  box-shadow: 0 -3px 10px rgb(39 169 235 / 18%);
  transition:
    background-color 140ms ease,
    transform 140ms ease;
}

.child-quick-action-button:active .child-quick-action-button__surface {
  background: var(--color-brand-primary-pressed);
  transform: translateY(2px);
}

.child-quick-action-button:focus-visible {
  outline: 3px solid rgb(39 169 235 / 24%);
  outline-offset: 3px;
}
</style>
