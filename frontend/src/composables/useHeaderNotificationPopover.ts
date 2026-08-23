import { readonly, ref } from 'vue'

export type HeaderNotificationNotice = {
  title: string
  message: string
}

const notice = ref<HeaderNotificationNotice | null>(null)
let dismissTimer: ReturnType<typeof globalThis.setTimeout> | null = null

const clearDismissTimer = () => {
  if (dismissTimer === null) return
  globalThis.clearTimeout(dismissTimer)
  dismissTimer = null
}

const dismissHeaderNotification = () => {
  clearDismissTimer()
  notice.value = null
}

const showHeaderNotification = (
  nextNotice: HeaderNotificationNotice,
  duration = 5_000,
) => {
  clearDismissTimer()
  notice.value = nextNotice
  dismissTimer = globalThis.setTimeout(dismissHeaderNotification, duration)
}

export const useHeaderNotificationPopover = () => ({
  headerNotification: readonly(notice),
  showHeaderNotification,
  dismissHeaderNotification,
})
