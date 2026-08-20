import { Configuration } from '@/api/generated'

export const apiConfig = new Configuration({
  // Generated paths already start with /api. Keep them same-origin so the
  // development proxy can forward requests to the configured backend.
  basePath: '',
})
