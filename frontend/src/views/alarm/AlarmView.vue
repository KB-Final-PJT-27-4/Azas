<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

import { allowanceRequests } from '@/mocks/allowanceRequests'

type AlarmItem = {
  id: number
  group: '오늘' | '이전 알림'
  category: string
  title: string
  message: string
  receivedAt: string
  isRead: boolean
  requestId?: string
}

const router = useRouter()
const pendingAllowanceRequest = allowanceRequests.find(({ status }) => status === 'pending')

const alarms = ref<AlarmItem[]>([
  ...(pendingAllowanceRequest
    ? [
        {
          id: 5,
          group: '오늘' as const,
          category: '아이 활동',
          title: `${pendingAllowanceRequest.childName}가 용돈을 요청했어요`,
          message: `${pendingAllowanceRequest.amount.toLocaleString('ko-KR')}원이 필요한 이유를 확인해 주세요.`,
          receivedAt: '방금 전',
          isRead: false,
          requestId: pendingAllowanceRequest.id,
        },
      ]
    : []),
  {
    id: 1,
    group: '오늘',
    category: '저축 예정',
    title: '오늘은 아이사랑적금 저축일이에요',
    message: '목표를 향한 작은 한 걸음을 이어가 볼까요?',
    receivedAt: '오전 11:01',
    isRead: false,
  },
  {
    id: 2,
    group: '오늘',
    category: '목표 달성',
    title: '저축 목표의 80%를 달성했어요',
    message: '조금만 더 모으면 기다리던 목표를 이룰 수 있어요.',
    receivedAt: '오전 9:20',
    isRead: false,
  },
  {
    id: 3,
    group: '이전 알림',
    category: '타임캡슐',
    title: '타임캡슐 공개일이 다가오고 있어요',
    message: '소중한 추억을 만나는 날까지 이제 3일 남았어요.',
    receivedAt: '8월 5일',
    isRead: true,
  },
  {
    id: 4,
    group: '이전 알림',
    category: '아이 성장',
    title: '깨비의 특별한 날을 확인해 보세요',
    message: '다가오는 기념일에 따뜻한 추억을 남겨보세요.',
    receivedAt: '8월 3일',
    isRead: true,
  },
])

const unreadCount = computed(() => alarms.value.filter(({ isRead }) => !isRead).length)

const readAlarm = (alarm: AlarmItem) => {
  alarm.isRead = true
  if (alarm.requestId) {
    router.push({ name: 'AllowanceRequest', params: { requestId: alarm.requestId } })
  }
}

const readAllAlarms = () => {
  alarms.value.forEach((alarm) => {
    alarm.isRead = true
  })
}
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-white pt-2 pb-[calc(24px+env(safe-area-inset-bottom))]"
  >
    <h1 class="sr-only">알림</h1>

    <div class="flex min-h-14 items-center justify-between px-5">
      <p class="text-[13px] font-semibold text-[var(--color-text-secondary)]">
        <template v-if="unreadCount">
          읽지 않은 알림 <strong class="text-[var(--color-brand-primary)]">{{ unreadCount }}</strong>
        </template>
        <template v-else>모든 알림을 확인했어요</template>
      </p>
      <button
        v-if="unreadCount"
        class="shrink-0 rounded-lg px-2 py-2 text-[12px] font-bold text-[var(--color-brand-primary)] transition-colors active:bg-[#eefaff]"
        type="button"
        @click="readAllAlarms"
      >
        모두 읽기
      </button>
    </div>

    <template v-if="alarms.length">
      <section class="border-t border-[#edf1f3]" aria-label="알림 목록">
        <ul class="m-0 list-none bg-white p-0">
          <li v-for="alarm in alarms" :key="alarm.id">
            <button
              class="relative flex min-h-[100px] w-full items-start px-5 py-4 text-left transition-colors duration-200"
              :class="alarm.isRead ? 'bg-white active:bg-[#f7f9fa]' : 'bg-[#f1faff] active:bg-[#e5f6fd]'"
              type="button"
              @click="readAlarm(alarm)"
            >
              <span class="min-w-0 flex-1">
                <span class="flex items-start justify-between gap-3">
                  <strong class="min-w-0 flex-1 text-[15px] leading-snug font-extrabold tracking-[-0.015em] text-[var(--color-text-primary)]">
                    {{ alarm.title }}
                  </strong>
                  <time class="shrink-0 pt-0.5 text-[11px] font-medium text-[#9aa5ac]">
                    {{ alarm.receivedAt }}
                  </time>
                </span>
                <span class="mt-2 block text-[13px] leading-[1.6] text-[var(--color-text-secondary)]">
                  {{ alarm.message }}
                </span>
              </span>
            </button>
          </li>
        </ul>
      </section>
    </template>

    <div v-else class="grid min-h-[42vh] place-items-center px-5 text-center">
      <div>
        <span
          class="mx-auto grid size-14 place-items-center rounded-full bg-[#eefaff] text-[22px]"
          aria-hidden="true"
        >
          ✓
        </span>
        <p class="mt-4 text-[16px] font-extrabold text-[var(--color-text-primary)]">
          새로운 알림이 없어요
        </p>
        <p class="mt-2 text-xs text-[var(--color-text-secondary)]">
          새 소식이 생기면 바로 알려드릴게요.
        </p>
      </div>
    </div>
  </main>
</template>
