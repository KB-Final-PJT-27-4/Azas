import axios from 'axios'

import type { ApiErrorResponse, OAuthLoginMemberResponse, OAuthLoginResponse } from '@/api/generated'
import { ACCESS_TOKEN_STORAGE_KEY, http } from '@/api/http'

export type OAuthProvider = 'google' | 'kakao'

const REFRESH_TOKEN_STORAGE_KEY = 'azas_refresh_token'
const AUTH_MEMBER_STORAGE_KEY = 'azas_auth_member'

export const loginWithOAuthCode = async (
  provider: OAuthProvider,
  authorizationCode: string,
  redirectUri: string,
) => {
  const { data } = await http.post<OAuthLoginResponse>(`/v1/auth/oauth/${provider}`, {
    authorization_code: authorizationCode,
    redirect_uri: redirectUri,
  })

  saveAuthSession(data)
  return data
}

export const saveAuthSession = (response: OAuthLoginResponse) => {
  sessionStorage.setItem(ACCESS_TOKEN_STORAGE_KEY, response.access_token)
  sessionStorage.setItem(REFRESH_TOKEN_STORAGE_KEY, response.refresh_token)
  sessionStorage.setItem(AUTH_MEMBER_STORAGE_KEY, JSON.stringify(response.member))
}

export const clearAuthSession = () => {
  sessionStorage.removeItem(ACCESS_TOKEN_STORAGE_KEY)
  sessionStorage.removeItem(REFRESH_TOKEN_STORAGE_KEY)
  sessionStorage.removeItem(AUTH_MEMBER_STORAGE_KEY)
}

export const getAuthMember = (): OAuthLoginMemberResponse | null => {
  const storedMember = sessionStorage.getItem(AUTH_MEMBER_STORAGE_KEY)

  if (!storedMember) return null

  try {
    return JSON.parse(storedMember) as OAuthLoginMemberResponse
  } catch {
    sessionStorage.removeItem(AUTH_MEMBER_STORAGE_KEY)
    return null
  }
}

export const getOAuthErrorMessage = (error: unknown) => {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    return error.response?.data?.error?.message ?? '소셜 로그인에 실패했어요. 다시 시도해주세요.'
  }

  return error instanceof Error ? error.message : '소셜 로그인에 실패했어요. 다시 시도해주세요.'
}
