import { beforeEach, describe, expect, it, vi } from 'vitest'

const firebaseMessagingMocks = vi.hoisted(() => ({
  deleteToken: vi.fn(),
  getMessaging: vi.fn(() => ({ name: 'messaging' })),
  getToken: vi.fn(),
  isSupported: vi.fn(),
}))

const pushDeviceApiMocks = vi.hoisted(() => ({
  registerPushDevice: vi.fn(),
  unregisterPushDevice: vi.fn(),
}))

vi.mock('firebase/messaging', () => firebaseMessagingMocks)
vi.mock('@/api/pushDevices', () => pushDeviceApiMocks)
vi.mock('@/config/firebase', () => ({
  firebaseVapidKey: 'test-vapid-key',
  getFirebaseApp: () => ({ name: 'firebase-app' }),
  isFirebaseMessagingConfigured: true,
}))

import { ACCESS_TOKEN_STORAGE_KEY } from '@/api/http'
import {
  enablePushNotifications,
  subscribeToForegroundPushMessages,
} from '@/services/pushNotifications'
import { PUSH_DEVICE_ID_STORAGE_KEY } from '@/utils/pushDevice'

class MemoryStorage {
  private readonly values = new Map<string, string>()

  getItem(key: string): string | null {
    return this.values.get(key) ?? null
  }

  setItem(key: string, value: string): void {
    this.values.set(key, value)
  }

  removeItem(key: string): void {
    this.values.delete(key)
  }
}

describe('pushNotifications', () => {
  beforeEach(() => {
    vi.clearAllMocks()

    const sessionStorage = new MemoryStorage()
    const localStorage = new MemoryStorage()
    sessionStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, 'access-token')

    const notification = {
      permission: 'default',
      requestPermission: vi.fn().mockResolvedValue('granted'),
    }
    const serviceWorkerRegistration = { scope: '/' }
    const navigator = {
      serviceWorker: {
        register: vi.fn().mockResolvedValue(serviceWorkerRegistration),
        addEventListener: vi.fn(),
        removeEventListener: vi.fn(),
      },
      userAgent: 'Chrome on macOS',
    }

    vi.stubGlobal('sessionStorage', sessionStorage)
    vi.stubGlobal('localStorage', localStorage)
    vi.stubGlobal('Notification', notification)
    vi.stubGlobal('navigator', navigator)
    vi.stubGlobal('window', {
      isSecureContext: true,
      Notification: notification,
    })
    vi.stubGlobal('crypto', {
      randomUUID: () => '550e8400-e29b-41d4-a716-446655440000',
    })

    firebaseMessagingMocks.isSupported.mockResolvedValue(true)
    firebaseMessagingMocks.getToken.mockResolvedValue('real-fcm-token')
    pushDeviceApiMocks.registerPushDevice.mockResolvedValue({
      push_device_id: 31,
    })
  })

  it('requests permission, gets an FCM token and registers the device', async () => {
    await enablePushNotifications()

    expect(Notification.requestPermission).toHaveBeenCalledOnce()
    expect(navigator.serviceWorker.register).toHaveBeenCalledWith('/firebase-messaging-sw.js')
    expect(firebaseMessagingMocks.getToken).toHaveBeenCalledWith(
      { name: 'messaging' },
      expect.objectContaining({ vapidKey: 'test-vapid-key' }),
    )
    expect(pushDeviceApiMocks.registerPushDevice).toHaveBeenCalledWith({
      device_key: '550e8400-e29b-41d4-a716-446655440000',
      platform: 'WEB',
      provider: 'FCM',
      push_token: 'real-fcm-token',
      device_name: 'Chrome on macOS',
    })
    expect(localStorage.getItem(PUSH_DEVICE_ID_STORAGE_KEY)).toBe('31')
  })

  it('subscribes to foreground messages and rejects external action URLs', () => {
    const listener = vi.fn()
    const unsubscribe = subscribeToForegroundPushMessages(listener)
    const messageHandler = vi.mocked(navigator.serviceWorker.addEventListener).mock.calls[0]?.[1]

    expect(messageHandler).toBeTypeOf('function')
    if (typeof messageHandler !== 'function') return

    messageHandler({
      data: {
        type: 'AZAS_FCM_MESSAGE',
        title: '미션 알림',
        body: '새 미션이 도착했어요.',
        action_url: 'https://malicious.example/path',
        data: { notification_type: 'MISSION_ASSIGNED' },
      },
    } as MessageEvent)

    expect(listener).toHaveBeenCalledWith({
      title: '미션 알림',
      body: '새 미션이 도착했어요.',
      actionUrl: '/alarm',
      data: { notification_type: 'MISSION_ASSIGNED' },
    })

    unsubscribe()
    expect(navigator.serviceWorker.removeEventListener).toHaveBeenCalledWith(
      'message',
      messageHandler,
    )
  })
})
