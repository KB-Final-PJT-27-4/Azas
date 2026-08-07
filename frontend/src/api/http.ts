import axios from 'axios'

export const ACCESS_TOKEN_STORAGE_KEY = 'azas_access_token'

export const http = axios.create({
  // Vite proxies this default to the local Tomcat server during development.
  // An environment variable can override it when a deployed API is ready.
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 15_000,
})

http.interceptors.request.use((config) => {
  const accessToken = sessionStorage.getItem(ACCESS_TOKEN_STORAGE_KEY)

  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }

  return config
})
