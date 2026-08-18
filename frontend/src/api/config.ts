import { Configuration } from '@/api/generated'

export const apiConfig = new Configuration({
  basePath: import.meta.env.VITE_API_BASE_URL,
})
