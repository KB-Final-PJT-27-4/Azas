<script setup lang="ts">
import { computed, ref } from 'vue'
import { Check, ChevronDown, Landmark } from 'lucide-vue-next'

export type AssetAccountSelectOption = {
  id: string
  name: string
  number: string
  balance?: number
  tag?: string
}

const props = withDefaults(
  defineProps<{
    modelValue: string
    options: AssetAccountSelectOption[]
    label: string
    showBalance?: boolean
  }>(),
  { showBalance: false },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const root = ref<HTMLElement | null>(null)
const isOpen = ref(false)
const selectedOption = computed(
  () => props.options.find(({ id }) => id === props.modelValue) ?? props.options[0],
)

const selectOption = (id: string) => {
  emit('update:modelValue', id)
  isOpen.value = false
}

const handleFocusOut = (event: FocusEvent) => {
  const nextTarget = event.relatedTarget as Node | null
  if (!nextTarget || !root.value?.contains(nextTarget)) isOpen.value = false
}
</script>

<template>
  <div ref="root" class="relative" @focusout="handleFocusOut">
    <button
      class="flex w-full items-center gap-3 rounded-[12px] border bg-white px-3 text-left transition-colors"
      :class="[
        showBalance ? 'h-14' : 'h-12',
        isOpen ? 'border-[var(--color-brand-primary)] ring-2 ring-[#e5f7ff]' : 'border-[#dce8ee]',
      ]"
      type="button"
      :aria-label="label"
      :aria-expanded="isOpen"
      aria-haspopup="listbox"
      @click="isOpen = !isOpen"
    >
      <span
        class="grid size-7 shrink-0 place-items-center rounded-full border border-[#ffad20] bg-[#fffaf0] text-[#ff9f00]"
        aria-hidden="true"
      >
        <Landmark :size="15" />
      </span>
      <span v-if="selectedOption" class="min-w-0 flex-1">
        <span class="flex min-w-0 items-center gap-1.5 text-[14px] font-semibold">
          <span
            v-if="selectedOption.tag"
            class="shrink-0 rounded-full px-2 py-0.5 text-[9px] font-bold"
            :class="
              selectedOption.tag === '부모'
                ? 'bg-[#e5f6ff] text-[var(--color-selected-text)]'
                : 'bg-[#fff4cd] text-[#a9780b]'
            "
          >
            {{ selectedOption.tag }}
          </span>
          <span class="truncate">{{ selectedOption.name }}</span>
          <span class="ml-1 font-normal text-[var(--color-text-secondary)]">
            {{ selectedOption.number }}
          </span>
        </span>
        <span
          v-if="showBalance && selectedOption.balance !== undefined"
          class="mt-0.5 block text-[10px] text-[var(--color-text-secondary)]"
        >
          현재 잔액
          <span class="text-[#20a8eb]">{{ selectedOption.balance.toLocaleString('ko-KR') }}원</span>
        </span>
      </span>
      <ChevronDown
        :size="20"
        class="shrink-0 text-[var(--color-text-secondary)] transition-transform"
        :class="isOpen ? 'rotate-180' : ''"
      />
    </button>

    <Transition
      enter-active-class="transition duration-150 ease-out"
      enter-from-class="-translate-y-1 opacity-0"
      leave-active-class="transition duration-100 ease-in"
      leave-to-class="-translate-y-1 opacity-0"
    >
      <ul
        v-if="isOpen"
        class="absolute top-[calc(100%+6px)] right-0 left-0 z-30 m-0 max-h-48 list-none overflow-y-auto rounded-[14px] border border-[#dce8ee] bg-white p-1.5 shadow-[0_10px_28px_rgba(45,77,94,0.16)]"
        role="listbox"
        :aria-label="label"
      >
        <li
          v-for="option in options"
          :key="option.id"
          role="option"
          :aria-selected="option.id === modelValue"
        >
          <button
            class="flex w-full items-center gap-2.5 rounded-[10px] px-2.5 py-2 text-left"
            :class="
              option.id === modelValue ? 'bg-[#effaff]' : 'hover:bg-[#f6f9fb] active:bg-[#edf3f6]'
            "
            type="button"
            @click="selectOption(option.id)"
          >
            <span
              class="grid size-7 shrink-0 place-items-center rounded-full border border-[#ffad20] bg-[#fffaf0] text-[#ff9f00]"
              aria-hidden="true"
            >
              <Landmark :size="14" />
            </span>
            <span class="min-w-0 flex-1">
              <span class="flex min-w-0 items-center gap-1.5">
                <span
                  v-if="option.tag"
                  class="shrink-0 rounded-full px-2 py-0.5 text-[9px] font-bold"
                  :class="
                    option.tag === '부모'
                      ? 'bg-[#e5f6ff] text-[var(--color-selected-text)]'
                      : 'bg-[#fff4cd] text-[#a9780b]'
                  "
                >
                  {{ option.tag }}
                </span>
                <strong class="truncate text-[13px]">{{ option.name }}</strong>
              </span>
              <span class="mt-0.5 block truncate text-[11px] text-[var(--color-text-secondary)]">
                {{ option.number }}
                <template v-if="showBalance && option.balance !== undefined">
                  · {{ option.balance.toLocaleString('ko-KR') }}원
                </template>
              </span>
            </span>
            <span
              class="grid size-5 shrink-0 place-items-center rounded-full"
              :class="
                option.id === modelValue
                  ? 'bg-[var(--color-brand-primary)] text-white'
                  : 'text-transparent'
              "
              aria-hidden="true"
            >
              <Check :size="13" :stroke-width="3" />
            </span>
          </button>
        </li>
      </ul>
    </Transition>
  </div>
</template>
