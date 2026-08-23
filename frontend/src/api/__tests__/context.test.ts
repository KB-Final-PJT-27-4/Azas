import { beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  getAuthMember: vi.fn(),
  getChildUsingGET: vi.fn(),
  getDashboardUsingGET: vi.fn(),
}))

vi.mock('@/api', () => ({
  api: {
    getChildUsingGET: mocks.getChildUsingGET,
    getDashboardUsingGET: mocks.getDashboardUsingGET,
  },
}))

vi.mock('@/api/auth', () => ({
  getAuthMember: mocks.getAuthMember,
}))

vi.mock('@/api/http', () => ({
  CURRENT_CHILD_STORAGE_KEY: 'azas_current_child_id',
  getAuthorizationHeader: vi.fn(),
}))

import { getCurrentChild } from '@/api/context'

describe('getCurrentChild', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('uses the child dashboard profile for a child session', async () => {
    mocks.getAuthMember.mockReturnValue({ member_type: 'CHILD' })
    mocks.getDashboardUsingGET.mockResolvedValue({
      data: { child: { child_id: 30, name: '하늘' } },
    })

    await expect(getCurrentChild()).resolves.toMatchObject({ name: '하늘' })
    expect(mocks.getDashboardUsingGET).toHaveBeenCalledOnce()
    expect(mocks.getChildUsingGET).not.toHaveBeenCalled()
  })
})
