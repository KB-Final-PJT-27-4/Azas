import axios, {
  type AxiosError,
  type AxiosInstance,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from 'axios'

import { apiBaseUrl } from '@/api/config'

export const ACCESS_TOKEN_STORAGE_KEY = 'azas_access_token'
export const REFRESH_TOKEN_STORAGE_KEY = 'azas_refresh_token'
export const AUTH_MEMBER_STORAGE_KEY = 'azas_auth_member'
export const AUTH_EXPIRED_EVENT = 'azas:auth-expired'
export const AUTH_SESSION_CHANGED_EVENT = 'azas:auth-session-changed'

const dispatchAuthSessionChanged = () => {
  if (typeof window !== 'undefined') window.dispatchEvent(new Event(AUTH_SESSION_CHANGED_EVENT))
}

export const notifyAuthSessionChanged = dispatchAuthSessionChanged

export const getAccessToken = () => sessionStorage.getItem(ACCESS_TOKEN_STORAGE_KEY)
export const getRefreshToken = () => sessionStorage.getItem(REFRESH_TOKEN_STORAGE_KEY)
export const saveTokenPair = (accessToken: string, refreshToken: string) => {
  sessionStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, accessToken)
  sessionStorage.setItem(REFRESH_TOKEN_STORAGE_KEY, refreshToken)
}
export const clearTokenPair = () => {
  sessionStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY)
  sessionStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY)
}
export const clearAuthSessionStorage = () => {
  clearTokenPair()
  sessionStorage.removeItem(AUTH_MEMBER_STORAGE_KEY)
  dispatchAuthSessionChanged()
}
export const getAuthorizationHeader = () => {
  const accessToken = getAccessToken()
  return accessToken ? `Bearer ${accessToken}` : ''
}

export const applyAccessToken = (config: InternalAxiosRequestConfig) => {
  const authorization = getAuthorizationHeader()

  if (authorization) config.headers.Authorization = authorization
  return config
}

type RetriableRequestConfig = InternalAxiosRequestConfig & { _azasRetried?: boolean }
type RefreshResponse = { access_token: string; refresh_token: string }

let refreshPromise: Promise<string> | null = null

const refreshAccessToken = () => {
  const refreshToken = getRefreshToken()
  if (!refreshToken) return Promise.reject(new Error('Refresh token is missing.'))

  refreshPromise ??= axios
    .post<RefreshResponse>(
      `${apiBaseUrl}/v1/auth/token/refresh`,
      { refresh_token: refreshToken },
      { timeout: 15_000 },
    )
    .then(({ data }) => {
      saveTokenPair(data.access_token, data.refresh_token)
      return data.access_token
    })
    .finally(() => {
      refreshPromise = null
    })

  return refreshPromise
}

const installTokenRefreshInterceptor = (client: AxiosInstance) => {
  client.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
      const request = error.config as RetriableRequestConfig | undefined
      const isAuthRequest = request?.url?.includes('/auth/') ?? false

      if (
        error.response?.status !== 401 ||
        !request ||
        request._azasRetried ||
        isAuthRequest ||
        !getRefreshToken()
      ) {
        return Promise.reject(error)
      }

      request._azasRetried = true
      try {
        const accessToken = await refreshAccessToken()
        request.headers.Authorization = `Bearer ${accessToken}`
        return client(request)
      } catch (refreshError) {
        clearAuthSessionStorage()
        if (typeof window !== 'undefined') window.dispatchEvent(new Event(AUTH_EXPIRED_EVENT))
        return Promise.reject(refreshError)
      }
    },
  )
}

export const createAuthenticatedHttpClient = (config: AxiosRequestConfig = {}) => {
  const client = axios.create({ timeout: 15_000, ...config })
  client.interceptors.request.use(applyAccessToken)
  installTokenRefreshInterceptor(client)
  return client
}

export const http = createAuthenticatedHttpClient({
  baseURL: apiBaseUrl,
})
