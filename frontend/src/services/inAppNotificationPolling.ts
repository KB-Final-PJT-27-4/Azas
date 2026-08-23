import { readonly, ref } from 'vue'

import { api } from '@/api'
import type { NotificationListItemResponse, NotificationListResponse } from '@/api/generated'
import { AUTH_SESSION_CHANGED_EVENT, getAccessToken } from '@/api/http'

export const IN_APP_NOTIFICATIONS_RECEIVED_EVENT = 'azas:in-app-notifications-received'

type PollingResponse = NotificationListResponse & {
  recommended_poll_interval_seconds?: number
}

export type InAppNotificationsReceivedDetail = {
  items: NotificationListItemResponse[]
}

const DEFAULT_POLL_INTERVAL_SECONDS = 15
const MIN_POLL_INTERVAL_SECONDS = 5
const MAX_POLL_INTERVAL_SECONDS = 60
const POLL_PAGE_SIZE = '20'
const INITIAL_PAGE_SIZE = '50'
const MAX_CONSECUTIVE_PAGES = 100
const RETRY_DELAYS_MS = [20_000, 40_000, 60_000] as const

const getRetryDelay = (failureCount: number) => {
  const retryIndex = Math.min(Math.max(failureCount - 1, 0), RETRY_DELAYS_MS.length - 1)
  return RETRY_DELAYS_MS[retryIndex] ?? 60_000
}

const unreadCount = ref(0)
const isPolling = ref(false)
const isConnected = ref(false)

let pollCursor = 0
let pollIntervalMs = DEFAULT_POLL_INTERVAL_SECONDS * 1_000
let timer: ReturnType<typeof window.setTimeout> | null = null
let inFlight: Promise<void> | null = null
let initializationInFlight: Promise<void> | null = null
let started = false
let initialized = false
let consecutiveFailures = 0
let sessionGeneration = 0

const clearTimer = () => {
  if (timer === null) return
  window.clearTimeout(timer)
  timer = null
}

const isVisible = () => typeof document === 'undefined' || document.visibilityState === 'visible'

const normalizeInterval = (seconds?: number) => {
  if (!seconds || !Number.isFinite(seconds)) return
  const normalizedSeconds = Math.min(
    MAX_POLL_INTERVAL_SECONDS,
    Math.max(MIN_POLL_INTERVAL_SECONDS, seconds),
  )
  pollIntervalMs = normalizedSeconds * 1_000
}

const jitteredPollInterval = () => {
  const multiplier = 0.8 + Math.random() * 0.4
  return Math.round(pollIntervalMs * multiplier)
}

const scheduleNextPoll = (delayMs = jitteredPollInterval()) => {
  clearTimer()
  if (!started || !initialized || !getAccessToken() || !isVisible()) return
  timer = window.setTimeout(() => {
    timer = null
    void pollForNewNotifications()
  }, delayMs)
}

const updateFromResponse = (data: PollingResponse) => {
  unreadCount.value = Math.max(0, data.unread_count ?? unreadCount.value)
  pollCursor = data.poll_cursor ?? pollCursor
  normalizeInterval(data.recommended_poll_interval_seconds)
}

const dispatchReceivedItems = (items: NotificationListItemResponse[]) => {
  if (!items.length || typeof window === 'undefined') return
  window.dispatchEvent(
    new CustomEvent<InAppNotificationsReceivedDetail>(IN_APP_NOTIFICATIONS_RECEIVED_EVENT, {
      detail: { items },
    }),
  )
}

const initialize = async () => {
  if (!started || !getAccessToken() || !isVisible()) return
  const requestedGeneration = sessionGeneration

  const { data } = await api.getNotificationsUsingGET(
    undefined,
    undefined,
    undefined,
    undefined,
    undefined,
    undefined,
    undefined,
    INITIAL_PAGE_SIZE,
  )
  if (requestedGeneration !== sessionGeneration || !getAccessToken()) return
  updateFromResponse(data as PollingResponse)
  initialized = true
  consecutiveFailures = 0
  isConnected.value = true
  scheduleNextPoll()
}

const performPoll = async () => {
  const requestedGeneration = sessionGeneration
  const receivedItems: NotificationListItemResponse[] = []
  const receivedIds = new Set<number>()

  for (let page = 0; page < MAX_CONSECUTIVE_PAGES; page += 1) {
    const { data } = await api.getNotificationsUsingGET(
      String(pollCursor),
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      POLL_PAGE_SIZE,
    )
    if (requestedGeneration !== sessionGeneration || !getAccessToken()) return
    const response = data as PollingResponse
    updateFromResponse(response)

    for (const item of response.items ?? []) {
      const id = item.notification_id
      if (typeof id === 'number' && receivedIds.has(id)) continue
      if (typeof id === 'number') receivedIds.add(id)
      receivedItems.push(item)
    }

    if (!response.has_more_new) break
  }

  dispatchReceivedItems(receivedItems)
}

export const pollForNewNotifications = (): Promise<void> => {
  if (!started || !initialized || !getAccessToken() || !isVisible()) {
    return Promise.resolve()
  }
  if (inFlight) return inFlight

  clearTimer()
  isPolling.value = true
  inFlight = performPoll()
    .then(() => {
      consecutiveFailures = 0
      isConnected.value = true
      scheduleNextPoll()
    })
    .catch((error) => {
      consecutiveFailures += 1
      isConnected.value = false
      if (import.meta.env.DEV) console.warn('인앱 알림 폴링에 실패했습니다.', error)
      scheduleNextPoll(getRetryDelay(consecutiveFailures))
    })
    .finally(() => {
      isPolling.value = false
      inFlight = null
    })

  return inFlight
}

const resetPollingState = () => {
  clearTimer()
  initialized = false
  pollCursor = 0
  pollIntervalMs = DEFAULT_POLL_INTERVAL_SECONDS * 1_000
  consecutiveFailures = 0
  unreadCount.value = 0
  isConnected.value = false
}

const scheduleInitializationRetry = (delayMs: number) => {
  clearTimer()
  if (!started || !getAccessToken() || !isVisible()) return
  timer = window.setTimeout(() => {
    timer = null
    syncWithAuthSession()
  }, delayMs)
}

const runInitialization = () => {
  if (initializationInFlight) return
  const requestedGeneration = sessionGeneration
  initializationInFlight = initialize()
    .catch((error) => {
      if (requestedGeneration !== sessionGeneration) return
      consecutiveFailures += 1
      isConnected.value = false
      if (import.meta.env.DEV) console.warn('인앱 알림 초기화에 실패했습니다.', error)
      scheduleInitializationRetry(getRetryDelay(consecutiveFailures))
    })
    .finally(() => {
      initializationInFlight = null
      if (requestedGeneration !== sessionGeneration && started && getAccessToken()) {
        runInitialization()
      }
    })
}

const syncWithAuthSession = () => {
  if (!started) return
  if (!getAccessToken()) {
    resetPollingState()
    return
  }
  if (!initialized && isVisible()) {
    runInitialization()
  }
}

const handleVisibilityChange = () => {
  if (!isVisible()) {
    clearTimer()
    return
  }
  if (!initialized) {
    syncWithAuthSession()
    return
  }
  void pollForNewNotifications()
}

const handleAuthSessionChanged = () => {
  sessionGeneration += 1
  resetPollingState()
  syncWithAuthSession()
}

export const startInAppNotificationPolling = () => {
  if (started || typeof window === 'undefined' || typeof document === 'undefined') return
  started = true
  document.addEventListener('visibilitychange', handleVisibilityChange)
  window.addEventListener(AUTH_SESSION_CHANGED_EVENT, handleAuthSessionChanged)
  syncWithAuthSession()
}

export const stopInAppNotificationPolling = () => {
  if (!started || typeof window === 'undefined' || typeof document === 'undefined') return
  started = false
  sessionGeneration += 1
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  window.removeEventListener(AUTH_SESSION_CHANGED_EVENT, handleAuthSessionChanged)
  resetPollingState()
}

export const setNotificationUnreadCount = (count: number) => {
  unreadCount.value = Math.max(0, count)
}

export const markOneNotificationRead = () => {
  unreadCount.value = Math.max(0, unreadCount.value - 1)
}

export const useInAppNotificationPolling = () => ({
  unreadCount: readonly(unreadCount),
  isPolling: readonly(isPolling),
  isConnected: readonly(isConnected),
})
