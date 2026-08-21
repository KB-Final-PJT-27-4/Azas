import axios from 'axios'

import { DefaultApi } from '@/api/generated'
import type { ApiErrorResponse } from '@/api/generated'
import { apiConfig } from './config'
import {
  clearTokenPair,
  createAuthenticatedHttpClient,
  getRefreshToken,
  saveTokenPair,
} from './http'

const generatedHttp = createAuthenticatedHttpClient()

export const api = new DefaultApi(apiConfig, '', generatedHttp)

let refreshPromise: Promise<string> | null = null
generatedHttp.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config as (typeof error.config & { _azasRetried?: boolean }) | undefined
    const refreshToken = getRefreshToken()
    if (error.response?.status !== 401 || !originalRequest || originalRequest._azasRetried || !refreshToken || originalRequest.url?.includes('/auth/token/refresh')) {
      return Promise.reject(error)
    }

    originalRequest._azasRetried = true
    try {
      refreshPromise ??= api.refreshUsingPOST({ refresh_token: refreshToken }).then(({ data }) => {
        saveTokenPair(data.access_token, data.refresh_token)
        return data.access_token
      }).finally(() => { refreshPromise = null })
      const accessToken = await refreshPromise
      originalRequest.headers.Authorization = `Bearer ${accessToken}`
      return generatedHttp(originalRequest)
    } catch (refreshError) {
      clearTokenPair()
      return Promise.reject(refreshError)
    }
  },
)

export const getApiErrorMessage = (error: unknown, fallback = '요청을 처리하지 못했어요.') => {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    return error.response?.data?.error?.message ?? fallback
  }

  return error instanceof Error ? error.message : fallback
}
