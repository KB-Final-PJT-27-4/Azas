import { Configuration } from '@/api/generated'

const configuredApiUrl = import.meta.env.VITE_API_BASE_URL?.replace(/\/+$/, '')

// Generated endpoint paths already include `/api`, while hand-written clients
// use paths beginning with `/v1`. Accept either an origin or an `/api` base URL
// from the environment and normalize both client styles to the same backend.
export const apiOrigin = configuredApiUrl && configuredApiUrl !== '/api'
  ? configuredApiUrl.replace(/\/api$/, '')
  : ''
export const apiBaseUrl = apiOrigin ? `${apiOrigin}/api` : '/api'

export const apiConfig = new Configuration({
  basePath: apiOrigin,
})
