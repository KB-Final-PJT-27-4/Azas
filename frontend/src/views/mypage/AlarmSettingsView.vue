<script setup lang="ts">
import { ref } from 'vue'
import { useToast } from '@/composables/useToast'

type AlarmSetting = {
  id: string
  title: string
  description: string
  enabled: boolean
}

const { showToast } = useToast()
const alarmSettings = ref<AlarmSetting[]>([
  {
    id: 'saving-day',
    title: '저축일 알림',
    description: '매월 설정한 저축일에 알려드려요',
    enabled: true,
  },
  {
    id: 'goal',
    title: '목표 달성 알림',
    description: '달성률이 변할 때 알려드려요',
    enabled: true,
  },
  {
    id: 'time-capsule',
    title: '타임캡슐 D-Day',
    description: '공개 예정일 전에 알려드려요',
    enabled: true,
  },
  {
    id: 'anniversary',
    title: '생일·기념일',
    description: '아이의 주요 기념일을 알려드려요',
    enabled: false,
  },
  {
    id: 'financial-product',
    title: '금융상품 소식',
    description: '연령에 맞는 새 상품을 알려드려요',
    enabled: false,
  },
])

const saveSettings = () => {
  showToast('알림 설정이 저장되었습니다.', 'success')
}
</script>

<template>
  <main class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col bg-white px-5 pt-8 pb-5">
    <h1 class="text-[22px] font-extrabold tracking-[-0.025em] text-[var(--color-text-primary)]">
      알림 설정
    </h1>

    <form class="mt-7 flex flex-1 flex-col" @submit.prevent="saveSettings">
      <ul class="m-0 list-none space-y-4 p-0">
        <li
          v-for="setting in alarmSettings"
          :key="setting.id"
          class="flex min-h-[84px] items-center gap-4 rounded-2xl border border-[#dce8ee] bg-white px-5 py-4"
        >
          <label class="min-w-0 flex-1 cursor-pointer" :for="`alarm-${setting.id}`">
            <strong class="block text-[16px] font-bold tracking-[-0.015em] text-[var(--color-text-primary)]">
              {{ setting.title }}
            </strong>
            <span class="mt-1 block text-xs text-[var(--color-text-secondary)]">
              {{ setting.description }}
            </span>
          </label>

          <label class="relative h-8 w-[59px] shrink-0 cursor-pointer">
            <input
              :id="`alarm-${setting.id}`"
              v-model="setting.enabled"
              class="peer sr-only"
              type="checkbox"
              role="switch"
              :aria-label="`${setting.title} ${setting.enabled ? '끄기' : '켜기'}`"
            />
            <span
              class="absolute inset-0 rounded-full bg-[#e3edf2] transition-colors duration-200 peer-checked:bg-[#28a9e2] peer-focus-visible:ring-2 peer-focus-visible:ring-[#bcecff] peer-focus-visible:ring-offset-2"
            ></span>
            <span
              class="absolute top-1 left-1 size-6 rounded-full bg-white shadow-sm transition-transform duration-200 peer-checked:translate-x-[27px]"
            ></span>
          </label>
        </li>
      </ul>

      <button
        class="mt-auto min-h-[60px] w-full rounded-2xl bg-[var(--color-brand-primary)] text-base font-bold text-white transition-colors active:bg-[var(--color-brand-primary-pressed)]"
        type="submit"
      >
        알림 설정 저장
      </button>
    </form>
  </main>
</template>
