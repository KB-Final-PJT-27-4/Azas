import axios from 'axios'

import { DefaultApi } from '@/api/generated'
import type { ApiErrorResponse } from '@/api/generated'
import { apiConfig } from './config'
import { createAuthenticatedHttpClient } from './http'

const generatedHttp = createAuthenticatedHttpClient()

export const api = new DefaultApi(apiConfig, '', generatedHttp)

export const getApiErrorMessage = (error: unknown, fallback = '요청을 처리하지 못했어요.') => {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    return error.response?.data?.error?.message ?? fallback
  }

  return error instanceof Error ? error.message : fallback
}
