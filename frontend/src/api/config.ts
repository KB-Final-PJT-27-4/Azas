import { Configuration } from '@/api/generated'

export const apiConfig = new Configuration({
  // Generated paths already start with /api. Keep them same-origin so both the
  // Vite and Vercel proxies can forward requests to the deployed backend.
  basePath: '',
})
