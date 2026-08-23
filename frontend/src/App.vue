<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { RouterView } from 'vue-router'
import { useToast } from '@/composables/useToast'
import type { NotificationListItemResponse } from '@/api/generated'
import {
  IN_APP_NOTIFICATIONS_RECEIVED_EVENT,
  type InAppNotificationsReceivedDetail,
  startInAppNotificationPolling,
  stopInAppNotificationPolling,
} from '@/services/inAppNotificationPolling'

const { showToast } = useToast()
let unsubscribeFromPushMessages: (() => void) | null = null
const recentlyShownNotificationIds = new Map<number, number>()
const RECENT_NOTIFICATION_TTL_MS = 30_000

const shouldShowNotification = (notificationId?: number) => {
  const now = Date.now()
  for (const [id, shownAt] of recentlyShownNotificationIds) {
    if (now - shownAt > RECENT_NOTIFICATION_TTL_MS) recentlyShownNotificationIds.delete(id)
  }
  if (typeof notificationId !== 'number') return true
  if (recentlyShownNotificationIds.has(notificationId)) return false
  recentlyShownNotificationIds.set(notificationId, now)
  return true
}

const showPolledNotification = (item: NotificationListItemResponse) => {
  if (!shouldShowNotification(item.notification_id)) return
  const title = item.title?.trim() || '새 알림'
  const body = item.content?.trim() || '새로운 알림이 도착했어요.'
  showToast(`${title}: ${body}`, 'info', 5000)
}

const handlePolledNotifications = (event: Event) => {
  const items = (event as CustomEvent<InAppNotificationsReceivedDetail>).detail?.items ?? []
  const newestItem = items.at(-1)
  if (newestItem) showPolledNotification(newestItem)
}

onMounted(() => {
  window.addEventListener(IN_APP_NOTIFICATIONS_RECEIVED_EVENT, handlePolledNotifications)
  startInAppNotificationPolling()

  void import('@/services/pushNotifications').then(
    ({ subscribeToForegroundPushMessages, syncPushNotificationsIfPermitted }) => {
      unsubscribeFromPushMessages = subscribeToForegroundPushMessages((message) => {
        const notificationId = Number(message.data.notification_id)
        if (!shouldShowNotification(Number.isFinite(notificationId) ? notificationId : undefined)) {
          return
        }
        showToast(`${message.title}: ${message.body}`, 'info', 5000)
      })

      return syncPushNotificationsIfPermitted()
    },
  )
})

onUnmounted(() => {
  unsubscribeFromPushMessages?.()
  window.removeEventListener(IN_APP_NOTIFICATIONS_RECEIVED_EVENT, handlePolledNotifications)
  stopInAppNotificationPolling()
})
</script>

<template>
  <DefaultLayout>
    <div class="route-page-viewport">
      <RouterView v-slot="{ Component, route }">
        <Transition name="route-page" mode="out-in">
          <div :key="route.path" class="route-page-frame">
            <component :is="Component" />
          </div>
        </Transition>
      </RouterView>
    </div>
  </DefaultLayout>
</template>

<style scoped>
.route-page-viewport {
  overflow-x: clip;
}

.route-page-enter-active,
.route-page-leave-active {
  transition:
    transform 180ms cubic-bezier(0.25, 0.8, 0.25, 1),
    opacity 140ms ease-out;
}

.route-page-enter-from {
  opacity: 0;
  transform: translateX(16px);
}

.route-page-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}

@media (prefers-reduced-motion: reduce) {
  .route-page-enter-active,
  .route-page-leave-active {
    transition-duration: 1ms;
  }
}
</style>
