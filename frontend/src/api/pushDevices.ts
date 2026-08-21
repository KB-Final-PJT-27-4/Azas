import { http } from '@/api/http'

export type PushPlatform = 'WEB' | 'ANDROID' | 'IOS'
export type PushProvider = 'FCM'

export type RegisterPushDeviceRequest = {
  device_key: string
  platform: PushPlatform
  provider: PushProvider
  push_token: string
  device_name: string | null
}

export type PushDeviceResponse = {
  push_device_id: number
  device_key: string
  platform: PushPlatform
  provider: PushProvider
  device_name: string | null
  active: boolean
  last_seen_at: string
  created_at: string
  updated_at: string
}

export const registerPushDevice = async (
  request: RegisterPushDeviceRequest,
): Promise<PushDeviceResponse> => {
  const response = await http.post<PushDeviceResponse>('/v1/push-devices', request)
  return response.data
}

export const unregisterPushDevice = async (pushDeviceId: number): Promise<void> => {
  await http.delete(`/v1/push-devices/${pushDeviceId}`)
}
