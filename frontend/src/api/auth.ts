import axios from 'axios'

import type { ApiErrorResponse, OAuthLoginMemberResponse, OAuthLoginResponse } from '@/api/generated'
import { api } from '@/api'
import { clearTokenPair, getRefreshToken, saveTokenPair } from '@/api/http'

export type OAuthProvider = 'google' | 'kakao'

const AUTH_MEMBER_STORAGE_KEY = 'azas_auth_member'
export { getRefreshToken }

export const loginWithOAuthCode = async (
  provider: OAuthProvider,
  authorizationCode: string,
  redirectUri: string,
  invitation?: { inviteToken: string; inviteeType: 'PARENT' | 'CHILD' },
) => {
  const request = { authorization_code: authorizationCode, redirect_uri: redirectUri }
  const { data } = invitation?.inviteeType === 'CHILD'
    ? await api.loginWithChildInviteUsingPOST(provider, { ...request, invite_token: invitation.inviteToken })
    : invitation?.inviteeType === 'PARENT'
      ? await api.loginWithParentInviteUsingPOST(provider, { ...request, invite_token: invitation.inviteToken })
      : await api.loginUsingPOST(provider, request)

  saveAuthSession(data)
  return data
}

export const saveAuthSession = (response: OAuthLoginResponse) => {
  saveTokenPair(response.access_token, response.refresh_token)
  sessionStorage.setItem(AUTH_MEMBER_STORAGE_KEY, JSON.stringify(response.member))
}

export const clearAuthSession = () => {
  clearTokenPair()
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
