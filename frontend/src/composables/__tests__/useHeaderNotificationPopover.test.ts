import { afterEach, describe, expect, it, vi } from 'vitest'

import { useHeaderNotificationPopover } from '@/composables/useHeaderNotificationPopover'

describe('useHeaderNotificationPopover', () => {
  const popover = useHeaderNotificationPopover()

  afterEach(() => {
    popover.dismissHeaderNotification()
    vi.useRealTimers()
  })

  it('shows only the most recent notification and dismisses it after the duration', async () => {
    vi.useFakeTimers()

    popover.showHeaderNotification(
      { title: '용돈 요청', message: '30,000원을 요청했어요.' },
      5_000,
    )
    popover.showHeaderNotification(
      { title: '용돈 승인', message: '요청이 승인되었어요.' },
      3_000,
    )

    expect(popover.headerNotification.value).toEqual({
      title: '용돈 승인',
      message: '요청이 승인되었어요.',
    })

    await vi.advanceTimersByTimeAsync(2_999)
    expect(popover.headerNotification.value).not.toBeNull()

    await vi.advanceTimersByTimeAsync(1)
    expect(popover.headerNotification.value).toBeNull()
  })

  it('dismisses the current notification immediately', () => {
    popover.showHeaderNotification({ title: '새 알림', message: '확인해 주세요.' })

    popover.dismissHeaderNotification()

    expect(popover.headerNotification.value).toBeNull()
  })
})
