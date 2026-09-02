<script setup lang="ts">
import { computed, ref } from 'vue'
import { Search, X } from 'lucide-vue-next'

type Bank = {
  name: string
  mark: string
  color: string
  textColor?: string
}

defineProps<{
  open: boolean
}>()

const emit = defineEmits<{
  close: []
  select: [bank: string]
}>()

const searchQuery = ref('')

const banks: Bank[] = [
  { name: '카카오뱅크', mark: 'B', color: '#ffdc00', textColor: '#222222' },
  { name: '국민은행', mark: 'KB', color: '#7c6b5b' },
  { name: '기업은행', mark: 'IBK', color: '#0067b9' },
  { name: '농협은행', mark: 'NH', color: '#00a84d' },
  { name: '신한은행', mark: 'S', color: '#0868e8' },
  { name: 'iM뱅크', mark: 'iM', color: '#00bfae' },
  { name: '산업은행', mark: 'KDB', color: '#06458f' },
  { name: '우리은행', mark: 'W', color: '#0796ce' },
  { name: '한국씨티은행', mark: 'citi', color: '#0575aa' },
  { name: '하나은행', mark: '1Q', color: '#008b80' },
  { name: 'SC제일은행', mark: 'SC', color: '#0784dd' },
  { name: '경남은행', mark: 'BNK', color: '#e60012' },
  { name: '광주은행', mark: 'KJ', color: '#0874bb' },
  { name: '도이치은행', mark: 'D', color: '#1115a8' },
  { name: '뱅크오브아메리카', mark: 'BOA', color: '#e31837' },
  { name: '부산은행', mark: 'BNK', color: '#d71920' },
  { name: '산림조합중앙회', mark: 'SJ', color: '#06326c' },
  { name: '저축은행', mark: 'SB', color: '#39a94b' },
  { name: '새마을금고', mark: 'MG', color: '#12bfe6' },
  { name: '수협', mark: 'Sh', color: '#0878b9' },
  { name: '신협중앙회', mark: 'CU', color: '#0870ad' },
  { name: '우체국', mark: 'POST', color: '#e7462f' },
  { name: '전북은행', mark: 'JB', color: '#2269a5' },
  { name: '제주은행', mark: 'JJ', color: '#0759ee' },
  { name: '중국건설은행', mark: 'CCB', color: '#3156a3' },
  { name: '중국공상은행', mark: 'ICBC', color: '#d7000f' },
  { name: '중국은행', mark: 'BOC', color: '#c5002f' },
  { name: 'BNP파리바은행', mark: 'BNP', color: '#159d7f' },
  { name: 'HSBC은행', mark: 'HSBC', color: '#db0011' },
  { name: 'JP모간체이스은행', mark: 'JPM', color: '#1268ad' },
  { name: '케이뱅크', mark: 'K', color: '#1428a0' },
  { name: '토스뱅크', mark: 'T', color: '#3182f6' },
]

const filteredBanks = computed(() => {
  const query = searchQuery.value.trim().toLocaleLowerCase()
  if (!query) return banks
  return banks.filter((bank) => bank.name.toLocaleLowerCase().includes(query))
})
</script>

<template>
  <Teleport to="body">
    <Transition name="bank-sheet" appear>
      <div
        v-if="open"
        class="bank-sheet-backdrop fixed inset-0 z-[var(--z-index-overlay)] flex items-end justify-center bg-black/35"
        role="presentation"
        @click.self="emit('close')"
      >
        <section
          class="bank-sheet-panel flex h-[clamp(480px,68dvh,620px)] max-h-[calc(100dvh-32px)] w-full max-w-[var(--app-max-width)] flex-col overflow-hidden rounded-t-[32px] bg-white pt-7 text-[var(--color-text-primary)] shadow-[0_-12px_40px_rgba(0,0,0,0.12)]"
          role="dialog"
          aria-modal="true"
          aria-labelledby="bank-sheet-title"
        >
        <header class="flex shrink-0 items-center justify-between px-6">
          <h2 id="bank-sheet-title" class="text-xl font-bold">은행선택</h2>
          <button
            class="grid size-9 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[var(--color-unselected-background)]"
            type="button"
            aria-label="은행 선택 닫기"
            @click="emit('close')"
          >
            <X :size="22" />
          </button>
        </header>

        <label
          class="mx-6 mt-5 rounded-md flex h-13 shrink-0 items-center gap-3 bg-[var(--color-surface-muted)] px-4"
        >
          <Search :size="22" class="shrink-0 text-black" />
          <input
            v-model="searchQuery"
            class="min-w-0 flex-1 border-0 bg-transparent text-base outline-none placeholder:text-[#8b8f94]"
            type="search"
            placeholder="은행검색"
            aria-label="은행 검색"
          />
        </label>

        <div
          v-if="filteredBanks.length"
          class="mt-4 grid min-h-0 grid-cols-2 gap-x-6 gap-y-1 overflow-y-auto px-6 "
        >
          <button
            v-for="bank in filteredBanks"
            :key="bank.name"
            class="flex min-w-0 items-center gap-3 rounded-xl px-1 py-3 text-left active:bg-[var(--color-unselected-background)]"
            type="button"
            @click="emit('select', bank.name)"
          >
            <span
              class="grid size-7 shrink-0 place-items-center rounded-full text-[9px] font-black text-white"
              :style="{ backgroundColor: bank.color, color: bank.textColor || '#ffffff' }"
              aria-hidden="true"
            >
              {{ bank.mark }}
            </span>
            <span class="truncate text-base font-medium">{{ bank.name }}</span>
          </button>
        </div>
        <p
          v-else
          class="min-h-0 overflow-y-auto px-6 py-14 text-center text-sm text-[var(--color-text-secondary)]"
        >
          검색 결과가 없습니다.
        </p>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.bank-sheet-enter-active,
.bank-sheet-leave-active {
  transition: background-color 240ms ease;
}

.bank-sheet-enter-active .bank-sheet-panel {
  transition: transform 380ms cubic-bezier(0.22, 1, 0.36, 1);
}

.bank-sheet-leave-active .bank-sheet-panel {
  transition: transform 240ms cubic-bezier(0.4, 0, 1, 1);
}

.bank-sheet-enter-from,
.bank-sheet-leave-to {
  background-color: rgb(0 0 0 / 0%);
}

.bank-sheet-enter-from .bank-sheet-panel,
.bank-sheet-leave-to .bank-sheet-panel {
  transform: translateY(100%);
}

@media (prefers-reduced-motion: reduce) {
  .bank-sheet-enter-active,
  .bank-sheet-leave-active,
  .bank-sheet-enter-active .bank-sheet-panel,
  .bank-sheet-leave-active .bank-sheet-panel {
    transition-duration: 1ms;
  }
}
</style>
