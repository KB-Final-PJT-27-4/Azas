import axios from 'axios'

export const ACCESS_TOKEN_STORAGE_KEY = 'azas_access_token'

export const http = axios.create({
  // Keep requests same-origin; Vite and Vercel proxy /api to the deployment.
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
