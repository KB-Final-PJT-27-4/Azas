<script setup lang="ts">
import { ref } from 'vue'

type AlarmItem = {
  id: number
  title: string
  message: string
  receivedAt: string
  isRead: boolean
}

const alarms = ref<AlarmItem[]>([
  {
    id: 1,
    title: '채린아 잘 지내니',
    message:
      '알림이 울렸다.. 누구에게 온 것일까...? 혹시 그녀...? 채린아.. 잘 지내니..? 혹시 자니? 자나보네 잘자.... 너는 진짜 별로다 ㅋㅋ 내 마음의 별로 ㅋㅋ 너 진짜 싫어라.... 어메이징 ㅎㅎ',
    receivedAt: '2026.07.21 11:01',
    isRead: false,
  },
  {
    id: 2,
    title: '보겔카 가자 ㅋㅋ',
    message:
      '아 배고프다 꼬르륵 꼬르륵 홍합 여기는 피그마 공장이라 날 제발 보겔카로 데려가줘 제발 자유 제기야 내일만된다고요!!!',
    receivedAt: '2026.07.21 11:01',
    isRead: true,
  },
  {
    id: 3,
    title: '대주오빠 잘지내',
    message: '대주오빠 3분째 내 디엠 안읽네 잘지내 ㅋㅋ',
    receivedAt: '2026.07.21 11:01',
    isRead: true,
  },
])

const readAlarm = (alarm: AlarmItem) => {
  alarm.isRead = true
}
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height))] bg-white">
    <h1 class="sr-only">알림</h1>

    <ul v-if="alarms.length" class="m-0 list-none p-0">
      <li v-for="alarm in alarms" :key="alarm.id" class="border-b border-[#dce8ee]">
        <button
          class="block min-h-[100px] w-full px-6 py-5 text-left transition-colors duration-150"
          :class="alarm.isRead ? 'bg-white active:bg-[#f7fafb]' : 'bg-[#eaf9ff] active:bg-[#ddf4fd]'"
          type="button"
          @click="readAlarm(alarm)"
        >
          <span class="flex items-start gap-4">
            <strong
              class="min-w-0 flex-1 text-[16px] font-extrabold tracking-[-0.02em] text-[var(--color-text-primary)]"
            >
              {{ alarm.title }}
            </strong>
            <time class="mt-0.5 shrink-0 text-[10px] text-[var(--color-text-secondary)]">
              {{ alarm.receivedAt }}
            </time>
          </span>
          <span
            class="mt-2 block text-[11px] leading-[1.45] text-[var(--color-text-secondary)]"
          >
            {{ alarm.message }}
          </span>
        </button>
      </li>
    </ul>

    <div v-else class="grid min-h-[55vh] place-items-center px-5 text-center">
      <div>
        <p class="text-base font-bold text-[var(--color-text-primary)]">새로운 알림이 없어요</p>
        <p class="mt-2 text-xs text-[var(--color-text-secondary)]">새 소식이 생기면 바로 알려드릴게요.</p>
      </div>
    </div>
  </main>
</template>
