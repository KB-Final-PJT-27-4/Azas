import { beforeEach, describe, expect, it, vi } from 'vitest'

import {
  ACCESS_TOKEN_STORAGE_KEY,
  AUTH_MEMBER_STORAGE_KEY,
  CURRENT_CHILD_STORAGE_KEY,
  REFRESH_TOKEN_STORAGE_KEY,
} from '@/api/http'
import { saveAuthSession } from '@/api/auth'

class MemoryStorage {
  private readonly values = new Map<string, string>()

  getItem(key: string) {
    return this.values.get(key) ?? null
  }

  setItem(key: string, value: string) {
    this.values.set(key, value)
  }

  removeItem(key: string) {
    this.values.delete(key)
  }
}

describe('saveAuthSession', () => {
  beforeEach(() => {
    vi.stubGlobal('sessionStorage', new MemoryStorage())
  })

  it('clears the previously selected child when the authenticated account changes', () => {
    sessionStorage.setItem(CURRENT_CHILD_STORAGE_KEY, '23')

    saveAuthSession({
      access_token: 'next-access-token',
      refresh_token: 'next-refresh-token',
      token_type: 'Bearer',
      expires_in: 3600,
      is_new_member: false,
      member: {
        member_id: 99,
        member_type: 'CHILD',
        name: '새 자녀',
        email: 'child@example.com',
        created_at: '2026-08-23T00:00:00Z',
        phone_verified: false,
      },
    })

    expect(sessionStorage.getItem(CURRENT_CHILD_STORAGE_KEY)).toBeNull()
    expect(sessionStorage.getItem(ACCESS_TOKEN_STORAGE_KEY)).toBe('next-access-token')
    expect(sessionStorage.getItem(REFRESH_TOKEN_STORAGE_KEY)).toBe('next-refresh-token')
    expect(sessionStorage.getItem(AUTH_MEMBER_STORAGE_KEY)).toContain('새 자녀')
  })
})
