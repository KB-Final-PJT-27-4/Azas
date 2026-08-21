import { deleteToken, getMessaging, getToken, isSupported } from 'firebase/messaging'

import { ACCESS_TOKEN_STORAGE_KEY } from '@/api/http'
import { registerPushDevice, unregisterPushDevice } from '@/api/pushDevices'
import { firebaseVapidKey, getFirebaseApp, isFirebaseMessagingConfigured } from '@/config/firebase'
import {
  clearSavedPushDeviceId,
  createWebPushDeviceRequest,
  getOrCreateDeviceKey,
  getSavedPushDeviceId,
  savePushDeviceId,
} from '@/utils/pushDevice'

export type PushNotificationStatus =
  'enabled' | 'default' | 'denied' | 'unsupported' | 'not_configured'

export type ForegroundPushMessage = {
  title: string
  body: string
  actionUrl: string
  data: Record<string, string>
}

type PushServiceWorkerMessage = {
  type?: string
  title?: string
  body?: string
  action_url?: string
  data?: Record<string, string>
}

const isBrowserPushAvailable = (): boolean =>
  typeof window !== 'undefined' &&
  'Notification' in window &&
  'serviceWorker' in navigator &&
  window.isSecureContext

export const getPushNotificationStatus = async (): Promise<PushNotificationStatus> => {
  if (!isFirebaseMessagingConfigured) return 'not_configured'
  if (!isBrowserPushAvailable() || !(await isSupported())) return 'unsupported'

  return Notification.permission === 'granted' ? 'enabled' : Notification.permission
}

const getMessagingWithServiceWorker = async () => {
  const serviceWorkerRegistration = await navigator.serviceWorker.register(
    '/firebase-messaging-sw.js',
  )
  const messaging = getMessaging(getFirebaseApp())

  return { messaging, serviceWorkerRegistration }
}

export const enablePushNotifications = async (): Promise<void> => {
  if (!sessionStorage.getItem(ACCESS_TOKEN_STORAGE_KEY)) {
    throw new Error('로그인 후 푸시 알림을 설정할 수 있습니다.')
  }

  const status = await getPushNotificationStatus()
  if (status === 'not_configured') {
    throw new Error('Firebase Web Push 설정이 필요합니다.')
  }
  if (status === 'unsupported') {
    throw new Error('이 브라우저에서는 푸시 알림을 지원하지 않습니다.')
  }
  if (status === 'denied') {
    throw new Error('브라우저 설정에서 알림 권한을 허용해주세요.')
  }

  const permission =
    Notification.permission === 'granted' ? 'granted' : await Notification.requestPermission()

  if (permission !== 'granted') {
    throw new Error('알림 권한이 허용되지 않았습니다.')
  }

  const { messaging, serviceWorkerRegistration } = await getMessagingWithServiceWorker()
  const pushToken = await getToken(messaging, {
    vapidKey: firebaseVapidKey,
    serviceWorkerRegistration,
  })

  if (!pushToken) {
    throw new Error('FCM 푸시 토큰을 발급받지 못했습니다.')
  }

  const response = await registerPushDevice(
    createWebPushDeviceRequest(pushToken, getOrCreateDeviceKey(), navigator.userAgent),
  )
  savePushDeviceId(response.push_device_id)
}

export const syncPushNotificationsIfPermitted = async (): Promise<boolean> => {
  if (
    !sessionStorage.getItem(ACCESS_TOKEN_STORAGE_KEY) ||
    typeof Notification === 'undefined' ||
    Notification.permission !== 'granted'
  ) {
    return false
  }

  try {
    await enablePushNotifications()
    return true
  } catch (error) {
    console.warn('푸시 기기 정보를 동기화하지 못했습니다.', error)
    return false
  }
}

export const subscribeToForegroundPushMessages = (
  listener: (message: ForegroundPushMessage) => void,
): (() => void) => {
  if (typeof navigator === 'undefined' || !('serviceWorker' in navigator)) {
    return () => undefined
  }

  const handleMessage = (event: MessageEvent<PushServiceWorkerMessage>) => {
    const payload = event.data
    if (payload?.type !== 'AZAS_FCM_MESSAGE') return

    listener({
      title: payload.title?.trim() || '아자스 알림',
      body: payload.body?.trim() || '새로운 알림이 도착했어요.',
      actionUrl:
        typeof payload.action_url === 'string' &&
        payload.action_url.startsWith('/') &&
        !payload.action_url.startsWith('//')
          ? payload.action_url
          : '/alarm',
      data: payload.data ?? {},
    })
  }

  navigator.serviceWorker.addEventListener('message', handleMessage)
  return () => navigator.serviceWorker.removeEventListener('message', handleMessage)
}

export const disablePushNotifications = async (): Promise<void> => {
  const pushDeviceId = getSavedPushDeviceId()
  let unregisterError: unknown = null

  try {
    if (pushDeviceId !== null) {
      await unregisterPushDevice(pushDeviceId)
    }
  } catch (error) {
    unregisterError = error
  }

  try {
    if (isFirebaseMessagingConfigured && isBrowserPushAvailable() && (await isSupported())) {
      await deleteToken(getMessaging(getFirebaseApp()))
    }
  } finally {
    clearSavedPushDeviceId()
  }

  if (unregisterError) {
    throw unregisterError
  }
}
