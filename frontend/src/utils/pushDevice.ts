import type { RegisterPushDeviceRequest } from '@/api/pushDevices'

export const DEVICE_KEY_STORAGE_KEY = 'azas_push_device_key'
export const PUSH_DEVICE_ID_STORAGE_KEY = 'azas_push_device_id'

type DeviceKeyStorage = Pick<Storage, 'getItem' | 'setItem'>

export const getOrCreateDeviceKey = (
  storage: DeviceKeyStorage = localStorage,
  createUuid: () => string = () => crypto.randomUUID(),
): string => {
  const savedDeviceKey = storage.getItem(DEVICE_KEY_STORAGE_KEY)

  if (savedDeviceKey) {
    return savedDeviceKey
  }

  const deviceKey = createUuid()
  storage.setItem(DEVICE_KEY_STORAGE_KEY, deviceKey)
  return deviceKey
}

export const createWebPushDeviceRequest = (
  pushToken: string,
  deviceKey: string,
  userAgent: string,
): RegisterPushDeviceRequest => ({
  device_key: deviceKey,
  platform: 'WEB',
  provider: 'FCM',
  push_token: pushToken,
  device_name: userAgent.trim().slice(0, 100) || null,
})

export const savePushDeviceId = (pushDeviceId: number): void => {
  localStorage.setItem(PUSH_DEVICE_ID_STORAGE_KEY, String(pushDeviceId))
}

export const getSavedPushDeviceId = (): number | null => {
  const value = localStorage.getItem(PUSH_DEVICE_ID_STORAGE_KEY)
  if (!value) return null

  const pushDeviceId = Number(value)
  return Number.isSafeInteger(pushDeviceId) && pushDeviceId > 0 ? pushDeviceId : null
}

export const clearSavedPushDeviceId = (): void => {
  localStorage.removeItem(PUSH_DEVICE_ID_STORAGE_KEY)
}
