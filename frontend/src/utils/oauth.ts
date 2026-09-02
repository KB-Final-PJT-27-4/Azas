import type { OAuthProvider } from '@/api/auth'

const OAUTH_STATE_PREFIX = 'azas_oauth_state_'
const OAUTH_INVITE_PREFIX = 'azas_oauth_invite_'
export type OAuthInvitationContext = {
  inviteToken: string
  inviteeType: 'PARENT' | 'CHILD'
  relationType?: 'FATHER' | 'MOTHER' | 'GUARDIAN'
}

const providerConfig = {
  google: {
    authorizationUrl: 'https://accounts.google.com/o/oauth2/v2/auth',
    clientId: () => import.meta.env.VITE_GOOGLE_CLIENT_ID,
    scope: 'openid email profile',
  },
  kakao: {
    authorizationUrl: 'https://kauth.kakao.com/oauth/authorize',
    clientId: () => import.meta.env.VITE_KAKAO_CLIENT_ID,
    scope: 'profile_nickname profile_image account_email',
  },
} satisfies Record<OAuthProvider, { authorizationUrl: string; clientId: () => string | undefined; scope: string }>

export const isOAuthProvider = (value: unknown): value is OAuthProvider =>
  value === 'google' || value === 'kakao'

export const getOAuthRedirectUri = (provider: OAuthProvider) =>
  `${window.location.origin}/auth/${provider}/callback`

export const startOAuthLogin = (provider: OAuthProvider, invitation?: OAuthInvitationContext) => {
  const config = providerConfig[provider]
  const clientId = config.clientId()?.trim()

  if (!clientId) {
    throw new Error(
      `${provider === 'google' ? 'Google' : 'Kakao'} 로그인 설정이 필요해요. 환경변수의 클라이언트 ID를 확인해주세요.`,
    )
  }

  const state = createOAuthState()
  const stateKey = getOAuthStateStorageKey(provider)
  const invitationKey = getOAuthInvitationStorageKey(provider, state)

  // OAuth provider를 거쳤다가 돌아오는 과정에서도 초대 문맥을 복원할 수 있도록
  // 같은 출처의 두 저장소에 일시적으로 보관한다. 실제 초대 토큰은 URL state에 넣지 않는다.
  sessionStorage.setItem(stateKey, state)
  localStorage.setItem(stateKey, state)

  if (invitation) {
    const invitationValue = JSON.stringify(invitation)
    sessionStorage.setItem(invitationKey, invitationValue)
    localStorage.setItem(invitationKey, invitationValue)
  }

  const parameters = new URLSearchParams({
    client_id: clientId,
    redirect_uri: getOAuthRedirectUri(provider),
    response_type: 'code',
    scope: config.scope,
    state,
  })

  if (provider === 'google') parameters.set('prompt', 'select_account')

  window.location.assign(`${config.authorizationUrl}?${parameters.toString()}`)
}

export const consumeOAuthInvitation = (
  provider: OAuthProvider,
  state: string | null,
): OAuthInvitationContext | undefined => {
  if (!state) return undefined

  const key = getOAuthInvitationStorageKey(provider, state)
  const value = sessionStorage.getItem(key) ?? localStorage.getItem(key)
  sessionStorage.removeItem(key)
  localStorage.removeItem(key)
  if (!value) return undefined
  try {
    return JSON.parse(value) as OAuthInvitationContext
  } catch {
    return undefined
  }
}

export const consumeOAuthState = (provider: OAuthProvider, returnedState: string | null) => {
  const key = getOAuthStateStorageKey(provider)
  const expectedState = sessionStorage.getItem(key) ?? localStorage.getItem(key)
  sessionStorage.removeItem(key)
  localStorage.removeItem(key)

  return Boolean(expectedState && returnedState && expectedState === returnedState)
}

const getOAuthStateStorageKey = (provider: OAuthProvider) =>
  `${OAUTH_STATE_PREFIX}${provider}`

const getOAuthInvitationStorageKey = (provider: OAuthProvider, state: string) =>
  `${OAUTH_INVITE_PREFIX}${provider}_${state}`

const createOAuthState = () => {
  if (typeof crypto.randomUUID === 'function') return crypto.randomUUID()

  const randomValues = crypto.getRandomValues(new Uint32Array(4))
  return Array.from(randomValues, (value) => value.toString(16).padStart(8, '0')).join('')
}
