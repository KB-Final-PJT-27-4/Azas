<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { api, getApiErrorMessage } from '@/api'
import type { NotificationListItemResponse } from '@/api/generated'
import {
  IN_APP_NOTIFICATIONS_RECEIVED_EVENT,
  markOneNotificationRead,
  setNotificationUnreadCount,
  type InAppNotificationsReceivedDetail,
  useInAppNotificationPolling,
} from '@/services/inAppNotificationPolling'

type AlarmItem = {
  id: number
  group: '오늘' | '이전 알림'
  category: string
  title: string
  message: string
  receivedAt: string
  isRead: boolean
  requestId?: number
  missionRouteName?: 'ChildMissions' | 'ParentMissions'
}

const router = useRouter()
const alarms = ref<AlarmItem[]>([])
const errorMessage = ref('')
const { unreadCount } = useInAppNotificationPolling()

const formatReceivedAt = (value?: string) => {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const today = new Date()
  return date.toDateString() === today.toDateString()
    ? date.toLocaleTimeString('ko-KR', { hour: 'numeric', minute: '2-digit' })
    : date.toLocaleDateString('ko-KR', { month: 'long', day: 'numeric' })
}

const resolveMissionRouteName = (item: NotificationListItemResponse) => {
  if (item.reference_type !== 'MISSION') return undefined

  if (item.notification_type === 'MISSION_ASSIGNED') {
    return 'ChildMissions' as const
  }

  if (item.notification_type === 'MISSION_SUBMITTED') {
    return 'ParentMissions' as const
  }

  return undefined
}

const mapAlarm = (item: NotificationListItemResponse): AlarmItem => ({
  id: item.notification_id ?? 0,
  group:
    item.created_at && new Date(item.created_at).toDateString() === new Date().toDateString()
      ? '오늘'
      : '이전 알림',
  category: item.notification_category ?? '',
  title: item.title ?? '새 알림',
  message: item.content ?? '',
  receivedAt: formatReceivedAt(item.created_at),
  isRead: item.is_read ?? false,
  requestId: item.reference_type === 'ALLOWANCE_REQUEST' ? item.reference_id : undefined,
  missionRouteName: resolveMissionRouteName(item),
})

const loadAlarms = async () => {
  try {
    const { data } = await api.getNotificationsUsingGET(
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      '50',
    )
    alarms.value = (data.items ?? []).map(mapAlarm)
    setNotificationUnreadCount(data.unread_count ?? 0)
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, '알림을 불러오지 못했어요.')
  }
}

const readAlarm = async (alarm: AlarmItem) => {
  if (!alarm.isRead) {
    try {
      await api.readNotificationUsingPATCH(alarm.id)
      alarm.isRead = true
      markOneNotificationRead()
    } catch (error) {
      errorMessage.value = getApiErrorMessage(error, '알림을 읽음 처리하지 못했어요.')
      return
    }
  }
  if (alarm.requestId) {
    await router.push({ name: 'AllowanceRequest', params: { requestId: alarm.requestId } })
    return
  }

  if (alarm.missionRouteName) {
    await router.push({ name: alarm.missionRouteName })
  }
}

const readAllAlarms = async () => {
  try {
    await api.readAllNotificationsUsingPATCH()
    alarms.value.forEach((alarm) => {
      alarm.isRead = true
    })
    setNotificationUnreadCount(0)
  } catch (error) {
    errorMessage.value = getApiErrorMessage(error, '알림을 모두 읽음 처리하지 못했어요.')
  }
}

const handleNewNotifications = (event: Event) => {
  const items = (event as CustomEvent<InAppNotificationsReceivedDetail>).detail?.items ?? []
  const existingIds = new Set(alarms.value.map(({ id }) => id))
  const newAlarms = items.map(mapAlarm).filter(({ id }) => !existingIds.has(id))
  alarms.value = [...newAlarms.reverse(), ...alarms.value]
}

onMounted(() => {
  window.addEventListener(IN_APP_NOTIFICATIONS_RECEIVED_EVENT, handleNewNotifications)
  void loadAlarms()
})

onBeforeUnmount(() => {
  window.removeEventListener(IN_APP_NOTIFICATIONS_RECEIVED_EVENT, handleNewNotifications)
})
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height))] bg-white pt-2 pb-[calc(24px+env(safe-area-inset-bottom))]"
  >
    <h1 class="sr-only">알림</h1>

    <div class="flex min-h-14 items-center justify-between px-5">
      <p class="text-[13px] font-semibold text-[var(--color-text-secondary)]">
        <template v-if="unreadCount">
          읽지 않은 알림
          <strong class="text-[var(--color-brand-primary)]">{{ unreadCount }}</strong>
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
              :class="
                alarm.isRead ? 'bg-white active:bg-[#f7f9fa]' : 'bg-[#f1faff] active:bg-[#e5f6fd]'
              "
              type="button"
              @click="readAlarm(alarm)"
            >
              <span class="min-w-0 flex-1">
                <span class="flex items-start justify-between gap-3">
                  <strong
                    class="min-w-0 flex-1 text-[15px] leading-snug font-extrabold tracking-[-0.015em] text-[var(--color-text-primary)]"
                  >
                    {{ alarm.title }}
                  </strong>
                  <time class="shrink-0 pt-0.5 text-[11px] font-medium text-[#9aa5ac]">
                    {{ alarm.receivedAt }}
                  </time>
                </span>
                <span
                  class="mt-2 block text-[13px] leading-[1.6] text-[var(--color-text-secondary)]"
                >
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
