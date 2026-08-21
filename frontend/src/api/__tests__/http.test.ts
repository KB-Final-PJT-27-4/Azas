import type { AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { beforeEach, describe, expect, it, vi } from 'vitest'

import {
  ACCESS_TOKEN_STORAGE_KEY,
  createAuthenticatedHttpClient,
} from '@/api/http'

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

const createResponse = (config: InternalAxiosRequestConfig): AxiosResponse => ({
  config,
  data: null,
  headers: {},
  status: 200,
  statusText: 'OK',
})

describe('authenticated HTTP client', () => {
  beforeEach(() => {
    vi.stubGlobal('sessionStorage', new MemoryStorage())
  })

  it('adds the latest access token to every request', async () => {
    const adapter = vi.fn(async (config: InternalAxiosRequestConfig) => createResponse(config))
    const client = createAuthenticatedHttpClient({ adapter })

    sessionStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, 'first-access-token')
    await client.get('/api/v1/profile')

    sessionStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, 'renewed-access-token')
    await client.get('/api/v1/accounts')

    expect(adapter).toHaveBeenCalledTimes(2)
    expect(adapter.mock.calls[0]?.[0].headers.Authorization).toBe('Bearer first-access-token')
    expect(adapter.mock.calls[1]?.[0].headers.Authorization).toBe('Bearer renewed-access-token')
  })

  it('does not send an empty Authorization header before login', async () => {
    const adapter = vi.fn(async (config: InternalAxiosRequestConfig) => createResponse(config))
    const client = createAuthenticatedHttpClient({ adapter })

    await client.post('/api/v1/auth/login')

    expect(adapter.mock.calls[0]?.[0].headers.Authorization).toBeUndefined()
  })
})
