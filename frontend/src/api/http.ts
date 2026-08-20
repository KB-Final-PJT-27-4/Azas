import axios, { type InternalAxiosRequestConfig } from 'axios'

export const ACCESS_TOKEN_STORAGE_KEY = 'azas_access_token'
export const REFRESH_TOKEN_STORAGE_KEY = 'azas_refresh_token'

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
export const getAuthorizationHeader = () => {
  const accessToken = getAccessToken()
  return accessToken ? `Bearer ${accessToken}` : ''
}

export const applyAccessToken = (config: InternalAxiosRequestConfig) => {
  const authorization = getAuthorizationHeader()

  if (authorization) config.headers.Authorization = authorization
  return config
}

export const http = axios.create({
  // Keep requests same-origin; Vite and Vercel proxy /api to the deployment.
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 15_000,
})

http.interceptors.request.use(applyAccessToken)
