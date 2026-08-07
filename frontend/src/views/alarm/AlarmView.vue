<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'

import { allowanceRequests } from '@/mocks/allowanceRequests'

type AlarmItem = {
  id: number
  group: '오늘' | '지난 알림'
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
    title: '오늘은 아이사랑적금 저축일이에요',
    message: '목표를 향한 작은 한 걸음을 이어가 볼까요?',
    receivedAt: '오전 11:01',
    isRead: false,
  },
  {
    id: 2,
    group: '오늘',
    title: '저축 목표의 80%를 달성했어요',
    message: '조금만 더 모으면 기다리던 목표를 이룰 수 있어요.',
    receivedAt: '오전 9:20',
    isRead: false,
  },
  {
    id: 3,
    group: '지난 알림',
    title: '타임캡슐 공개일이 다가오고 있어요',
    message: '소중한 추억을 만나는 날까지 이제 3일 남았어요.',
    receivedAt: '8월 5일',
    isRead: true,
  },
  {
    id: 4,
    group: '지난 알림',
    title: '깨비의 특별한 날을 확인해 보세요',
    message: '다가오는 기념일에 따뜻한 추억을 남겨보세요.',
    receivedAt: '8월 3일',
    isRead: true,
  },
])

const groups = computed(() =>
  (['오늘', '지난 알림'] as const)
    .map((label) => ({ label, items: alarms.value.filter(({ group }) => group === label) }))
    .filter(({ items }) => items.length),
)
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
  <main class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col bg-white px-5 pt-4 pb-10">
    <h1 class="sr-only">알림</h1>

    <template v-if="alarms.length">
      <section v-for="(group, index) in groups" :key="group.label" :class="index ? 'mt-4' : ''">
        <h2 class="mb-3 px-1 text-xs font-bold text-[var(--color-text-secondary)]">
          {{ group.label }}
        </h2>
        <ul class="m-0 list-none space-y-3 p-0">
          <li
            v-for="alarm in group.items"
            :key="alarm.id"
          >
            <button
              class="block min-h-[96px] w-full rounded-xl border px-4 py-4 text-left transition-colors duration-150"
              :class="
                alarm.isRead
                  ? 'border-[var(--color-border)] bg-white active:bg-[var(--color-unselected-background)]'
                  : 'border-[#d5edf8] bg-[var(--color-selected-background)] active:bg-[#dff5ff]'
              "
              type="button"
              @click="readAlarm(alarm)"
            >
              <span class="block min-w-0">
                <span class="flex items-start gap-2">
                  <strong
                    class="min-w-0 flex-1 text-[14px] leading-snug font-bold tracking-[-0.015em] text-[var(--color-text-primary)]"
                  >
                    {{ alarm.title }}
                  </strong>
                  <span
                    v-if="!alarm.isRead"
                    class="mt-1.5 size-1.5 shrink-0 rounded-full bg-[var(--color-brand-primary)]"
                    aria-label="읽지 않음"
                  ></span>
                </span>
                <span class="mt-1.5 block text-[11px] leading-[1.45] text-[var(--color-text-secondary)]">
                  {{ alarm.message }}
                </span>
                <time class="mt-2 block text-[10px] font-medium text-[#a0aab3]">
                  {{ alarm.receivedAt }}
                </time>
              </span>
            </button>
          </li>
        </ul>
      </section>

      <div
        v-if="unreadCount"
        class="pointer-events-none fixed bottom-[calc(20px+env(safe-area-inset-bottom))] left-1/2 z-10 flex w-full max-w-[var(--app-max-width)] -translate-x-1/2 justify-end px-5"
      >
        <button
          class="pointer-events-auto rounded-lg border border-[#d5edf8] bg-[var(--color-selected-background)] px-4 py-2.5 text-xs font-bold text-[var(--color-selected-text)] shadow-[0_4px_14px_rgba(43,171,232,0.16)] transition-colors active:bg-[#dff5ff]"
          type="button"
          @click="readAllAlarms"
        >
          모두 읽기
        </button>
      </div>
    </template>

    <div v-else class="grid min-h-[55vh] place-items-center px-5 text-center">
      <div>
        <p class="text-base font-bold text-[var(--color-text-primary)]">새로운 알림이 없어요</p>
        <p class="mt-2 text-xs text-[var(--color-text-secondary)]">새 소식이 생기면 바로 알려드릴게요.</p>
      </div>
    </div>
  </main>
</template>
