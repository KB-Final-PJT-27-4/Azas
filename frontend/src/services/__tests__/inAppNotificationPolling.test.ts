import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const apiMocks = vi.hoisted(() => ({
  getNotificationsUsingGET: vi.fn(),
}))

const authMocks = vi.hoisted(() => ({
  getAccessToken: vi.fn(() => 'access-token'),
}))

vi.mock('@/api', () => ({ api: apiMocks }))
vi.mock('@/api/http', () => ({
  AUTH_SESSION_CHANGED_EVENT: 'azas:auth-session-changed',
  getAccessToken: authMocks.getAccessToken,
}))

import {
  IN_APP_NOTIFICATIONS_RECEIVED_EVENT,
  pollForNewNotifications,
  startInAppNotificationPolling,
  stopInAppNotificationPolling,
  useInAppNotificationPolling,
} from '@/services/inAppNotificationPolling'

class TestDocument extends EventTarget {
  visibilityState: DocumentVisibilityState = 'visible'
}

class TestWindow extends EventTarget {
  setTimeout = globalThis.setTimeout
  clearTimeout = globalThis.clearTimeout
}

const flushPromises = async () => {
  await Promise.resolve()
  await Promise.resolve()
  await Promise.resolve()
}

describe('inAppNotificationPolling', () => {
  let testDocument: TestDocument
  let testWindow: TestWindow

  beforeEach(() => {
    vi.useFakeTimers()
    vi.spyOn(Math, 'random').mockReturnValue(0.5)
    vi.clearAllMocks()
    authMocks.getAccessToken.mockReturnValue('access-token')
    testDocument = new TestDocument()
    testWindow = new TestWindow()
    vi.stubGlobal('document', testDocument)
    vi.stubGlobal('window', testWindow)
    apiMocks.getNotificationsUsingGET.mockResolvedValue({
      data: {
        items: [],
        poll_cursor: 42,
        unread_count: 2,
        has_more_new: false,
        recommended_poll_interval_seconds: 15,
      },
    })
  })

  afterEach(() => {
    stopInAppNotificationPolling()
    vi.useRealTimers()
    vi.restoreAllMocks()
    vi.unstubAllGlobals()
  })

  it('initializes silently and polls with the last cursor at the server interval', async () => {
    const received = vi.fn()
    testWindow.addEventListener(IN_APP_NOTIFICATIONS_RECEIVED_EVENT, received)

    startInAppNotificationPolling()
    await flushPromises()

    expect(apiMocks.getNotificationsUsingGET).toHaveBeenNthCalledWith(
      1,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      '50',
    )
    expect(useInAppNotificationPolling().unreadCount.value).toBe(2)
    expect(received).not.toHaveBeenCalled()

    apiMocks.getNotificationsUsingGET.mockResolvedValueOnce({
      data: {
        items: [{ notification_id: 43, title: '용돈 요청', content: '확인해 주세요.' }],
        poll_cursor: 43,
        unread_count: 3,
        has_more_new: false,
      },
    })
    await vi.advanceTimersByTimeAsync(15_000)
    await flushPromises()

    expect(apiMocks.getNotificationsUsingGET).toHaveBeenLastCalledWith(
      '42',
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      '20',
    )
    expect(received).toHaveBeenCalledOnce()
    expect(useInAppNotificationPolling().unreadCount.value).toBe(3)
  })

  it('drains accumulated pages immediately while has_more_new is true', async () => {
    startInAppNotificationPolling()
    await flushPromises()

    apiMocks.getNotificationsUsingGET
      .mockResolvedValueOnce({
        data: {
          items: [{ notification_id: 43 }],
          poll_cursor: 43,
          unread_count: 4,
          has_more_new: true,
        },
      })
      .mockResolvedValueOnce({
        data: {
          items: [{ notification_id: 44 }],
          poll_cursor: 44,
          unread_count: 4,
          has_more_new: false,
        },
      })

    await pollForNewNotifications()

    expect(apiMocks.getNotificationsUsingGET).toHaveBeenNthCalledWith(
      2,
      '42',
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      '20',
    )
    expect(apiMocks.getNotificationsUsingGET).toHaveBeenNthCalledWith(
      3,
      '43',
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      '20',
    )
  })

  it('stops in the background and polls immediately when the PWA becomes visible', async () => {
    startInAppNotificationPolling()
    await flushPromises()

    testDocument.visibilityState = 'hidden'
    testDocument.dispatchEvent(new Event('visibilitychange'))
    await vi.advanceTimersByTimeAsync(60_000)
    expect(apiMocks.getNotificationsUsingGET).toHaveBeenCalledTimes(1)

    testDocument.visibilityState = 'visible'
    testDocument.dispatchEvent(new Event('visibilitychange'))
    await flushPromises()

    expect(apiMocks.getNotificationsUsingGET).toHaveBeenCalledTimes(2)
    expect(apiMocks.getNotificationsUsingGET).toHaveBeenLastCalledWith(
      '42',
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      undefined,
      '20',
    )
  })
})
