import { describe, expect, it } from 'vitest'

import {
  createWebPushDeviceRequest,
  DEVICE_KEY_STORAGE_KEY,
  getOrCreateDeviceKey,
} from '@/utils/pushDevice'

class MemoryStorage {
  private readonly values = new Map<string, string>()

  getItem(key: string): string | null {
    return this.values.get(key) ?? null
  }

  setItem(key: string, value: string): void {
    this.values.set(key, value)
  }
}

describe('pushDevice', () => {
  it('creates one stable device key and reuses it', () => {
    const storage = new MemoryStorage()
    let uuidCalls = 0
    const createUuid = () => {
      uuidCalls += 1
      return '550e8400-e29b-41d4-a716-446655440000'
    }

    expect(getOrCreateDeviceKey(storage, createUuid)).toBe('550e8400-e29b-41d4-a716-446655440000')
    expect(getOrCreateDeviceKey(storage, createUuid)).toBe('550e8400-e29b-41d4-a716-446655440000')
    expect(storage.getItem(DEVICE_KEY_STORAGE_KEY)).toBe('550e8400-e29b-41d4-a716-446655440000')
    expect(uuidCalls).toBe(1)
  })

  it('builds a web FCM registration request and limits device name length', () => {
    const request = createWebPushDeviceRequest(
      'fcm-token',
      'device-key',
      `Chrome ${'x'.repeat(120)}`,
    )

    expect(request).toMatchObject({
      device_key: 'device-key',
      platform: 'WEB',
      provider: 'FCM',
      push_token: 'fcm-token',
    })
    expect(request.device_name).toHaveLength(100)
  })
})
