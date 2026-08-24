<script setup lang="ts">
import { BellRing, ChevronRight, X } from 'lucide-vue-next'
import { useRouter } from 'vue-router'

import { api } from '@/api'
import { useHeaderNotificationPopover } from '@/composables/useHeaderNotificationPopover'
import { markOneNotificationRead } from '@/services/inAppNotificationPolling'

const router = useRouter()
const { headerNotification, dismissHeaderNotification } = useHeaderNotificationPopover()

const openNotificationTarget = async () => {
  const notification = headerNotification.value
  dismissHeaderNotification()

  if (notification && !notification.isRead && typeof notification.notificationId === 'number') {
    try {
      await api.readNotificationUsingPATCH(notification.notificationId)
      markOneNotificationRead()
    } catch {
      // 읽음 처리 실패는 화면 이동을 막지 않습니다.
    }
  }

  await router.push(notification?.targetPath ?? { name: 'Alarm' })
}
</script>

<template>
  <Transition name="header-notification">
    <aside
      v-if="headerNotification"
      class="header-notification-popover"
      role="status"
      aria-live="polite"
    >
      <span class="header-notification-popover__pointer" aria-hidden="true"></span>

      <button
        class="header-notification-popover__content"
        type="button"
        aria-label="알림 목록 열기"
        @click="openNotificationTarget"
      >
        <span class="header-notification-popover__icon" aria-hidden="true">
          <BellRing :size="18" :stroke-width="2.6" />
          <span class="header-notification-popover__dot"></span>
        </span>
        <span class="header-notification-popover__copy">
          <strong>{{ headerNotification.title }}</strong>
          <span>{{ headerNotification.message }}</span>
        </span>
        <ChevronRight
          :size="18"
          :stroke-width="2.5"
          class="header-notification-popover__arrow"
          aria-hidden="true"
        />
      </button>

      <button
        class="header-notification-popover__close"
        type="button"
        aria-label="알림 닫기"
        @click="dismissHeaderNotification"
      >
        <X :size="14" :stroke-width="3" />
      </button>
    </aside>
  </Transition>
</template>

<style scoped>
.header-notification-popover {
  position: fixed;
  top: calc(var(--app-header-height) + env(safe-area-inset-top) + 6px);
  right: max(12px, calc((100vw - var(--app-max-width)) / 2 + 12px));
  z-index: var(--z-index-toast);
  display: flex;
  width: min(calc(100vw - 24px), 332px);
  min-height: 76px;
  padding: 8px 8px 8px 10px;
  color: var(--color-text-primary);
  background: linear-gradient(135deg, #f4fcff 0%, #ffffff 72%);
  border: 1px solid #bfe7f8;
  border-radius: 20px;
  box-shadow: 0 12px 28px rgb(51 101 126 / 18%);
}

.header-notification-popover__pointer {
  position: absolute;
  top: -7px;
  right: 31px;
  width: 13px;
  height: 13px;
  background: #f4fcff;
  border-top: 1px solid #bfe7f8;
  border-left: 1px solid #bfe7f8;
  transform: rotate(45deg);
}

.header-notification-popover__content {
  position: relative;
  z-index: 1;
  display: flex;
  min-width: 0;
  flex: 1;
  align-items: center;
  gap: 10px;
  padding: 3px 2px;
  text-align: left;
  background: transparent;
  border: 0;
  border-radius: 14px;
}

.header-notification-popover__content:active {
  background: rgb(85 192 244 / 10%);
}

.header-notification-popover__icon {
  position: relative;
  display: grid;
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  place-items: center;
  color: #258fc2;
  background: #dff5ff;
  border: 1px solid #b9e5f8;
  border-radius: 15px;
}

.header-notification-popover__dot {
  position: absolute;
  top: 7px;
  right: 7px;
  width: 7px;
  height: 7px;
  background: #ff7e80;
  border: 1.5px solid #fff;
  border-radius: 999px;
}

.header-notification-popover__copy {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 3px;
}

.header-notification-popover__copy strong,
.header-notification-popover__copy span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-notification-popover__copy strong {
  font-size: 13px;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.header-notification-popover__copy span {
  color: var(--color-text-secondary);
  font-size: 12px;
  font-weight: 600;
}

.header-notification-popover__arrow {
  flex: 0 0 auto;
  color: #77b9d6;
}

.header-notification-popover__close {
  position: relative;
  z-index: 1;
  display: grid;
  width: 26px;
  height: 26px;
  flex: 0 0 26px;
  place-items: center;
  margin: -2px -2px 0 0;
  color: #8ca3af;
  background: transparent;
  border: 0;
  border-radius: 999px;
}

.header-notification-popover__close:active {
  background: rgb(85 192 244 / 12%);
}

.header-notification-enter-active,
.header-notification-leave-active {
  transition: opacity 180ms ease, transform 200ms ease;
}

.header-notification-enter-from,
.header-notification-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.96);
}

@media (prefers-reduced-motion: reduce) {
  .header-notification-enter-active,
  .header-notification-leave-active {
    transition-duration: 1ms;
  }
}
</style>
