import { readFileSync } from 'node:fs'
import vm from 'node:vm'

import { describe, expect, it, vi } from 'vitest'

type WorkerHandler = (event: Record<string, unknown>) => void

const serviceWorkerSource = readFileSync(
  new URL('../../../public/firebase-messaging-sw.js', import.meta.url),
  'utf8',
)

const createWorker = (windowClients: Array<Record<string, unknown>> = []) => {
  const handlers = new Map<string, WorkerHandler>()
  const showNotification = vi.fn().mockResolvedValue(undefined)
  const openWindow = vi.fn().mockResolvedValue(undefined)
  const workerSelf = {
    addEventListener: vi.fn((type: string, handler: WorkerHandler) => handlers.set(type, handler)),
    skipWaiting: vi.fn(),
    clients: {
      claim: vi.fn().mockResolvedValue(undefined),
      matchAll: vi.fn().mockResolvedValue(windowClients),
      openWindow,
    },
    registration: { showNotification },
    location: { origin: 'https://azas.example' },
  }

  vm.runInNewContext(serviceWorkerSource, {
    self: workerSelf,
    URL,
    Promise,
  })

  return { handlers, openWindow, showNotification, workerSelf }
}

describe('firebase messaging service worker', () => {
  it('shows a background notification with a safe same-origin action path', async () => {
    const { handlers, showNotification } = createWorker()
    let pending: Promise<unknown> | undefined

    handlers.get('push')?.({
      data: {
        json: () => ({
          notification: { title: '미션 알림', body: '미션을 확인해 주세요.' },
          data: {
            action_url: '/child/missions',
            notification_type: 'MISSION_ASSIGNED',
            reference_id: '71',
          },
        }),
      },
      waitUntil: (promise: Promise<unknown>) => {
        pending = promise
      },
    })
    await pending

    expect(showNotification).toHaveBeenCalledWith(
      '미션 알림',
      expect.objectContaining({
        body: '미션을 확인해 주세요.',
        data: expect.objectContaining({ action_url: '/child/missions' }),
      }),
    )
  })

  it('focuses an existing app window and blocks external action URLs', async () => {
    const navigate = vi.fn().mockResolvedValue(undefined)
    const focus = vi.fn().mockResolvedValue(undefined)
    const existingClient = {
      url: 'https://azas.example/current',
      navigate,
      focus,
    }
    const { handlers, openWindow } = createWorker([existingClient])
    let pending: Promise<unknown> | undefined

    handlers.get('notificationclick')?.({
      notification: {
        close: vi.fn(),
        data: { action_url: 'https://malicious.example/path' },
      },
      waitUntil: (promise: Promise<unknown>) => {
        pending = promise
      },
    })
    await pending

    expect(navigate).toHaveBeenCalledWith('https://azas.example/alarm')
    expect(focus).toHaveBeenCalledOnce()
    expect(openWindow).not.toHaveBeenCalled()
  })
})
