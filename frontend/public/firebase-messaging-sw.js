const DEFAULT_ACTION_URL = '/alarm'
const MESSAGE_TYPE = 'AZAS_FCM_MESSAGE'

const normalizeActionUrl = (value) => {
  if (typeof value !== 'string' || !value.startsWith('/') || value.startsWith('//')) {
    return DEFAULT_ACTION_URL
  }

  return value
}

const parsePayload = (event) => {
  try {
    return event.data?.json() ?? {}
  } catch {
    return {}
  }
}

const notificationFromPayload = (payload) => {
  const notification = payload.notification ?? {}
  const data = payload.data ?? {}
  const actionUrl = normalizeActionUrl(data.action_url ?? notification.click_action)
  const tag =
    data.notification_id ||
    [data.notification_type, data.reference_id].filter(Boolean).join(':') ||
    undefined

  return {
    title: notification.title ?? data.title ?? '아자스 알림',
    options: {
      body: notification.body ?? data.body ?? '새로운 알림이 도착했어요.',
      data: {
        ...data,
        action_url: actionUrl,
      },
      tag,
      renotify: false,
    },
  }
}

self.addEventListener('install', () => {
  self.skipWaiting()
})

self.addEventListener('activate', (event) => {
  event.waitUntil(self.clients.claim())
})

self.addEventListener('push', (event) => {
  const payload = parsePayload(event)
  const notification = notificationFromPayload(payload)

  event.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((windowClients) => {
      const visibleClient = windowClients.find((client) => client.visibilityState === 'visible')

      if (visibleClient) {
        visibleClient.postMessage({
          type: MESSAGE_TYPE,
          title: notification.title,
          body: notification.options.body,
          action_url: notification.options.data.action_url,
          data: notification.options.data,
        })
      }

      return self.registration.showNotification(notification.title, notification.options)
    }),
  )
})

self.addEventListener('notificationclick', (event) => {
  event.notification.close()

  const actionUrl = normalizeActionUrl(event.notification.data?.action_url)
  const targetUrl = new URL(actionUrl, self.location.origin)

  event.waitUntil(
    self.clients
      .matchAll({ type: 'window', includeUncontrolled: true })
      .then(async (windowClients) => {
        const existingClient = windowClients.find(
          (client) => new URL(client.url).origin === targetUrl.origin,
        )

        if (existingClient) {
          if ('navigate' in existingClient) {
            await existingClient.navigate(targetUrl.href)
          }
          return existingClient.focus()
        }

        return self.clients.openWindow(targetUrl.href)
      }),
  )
})
