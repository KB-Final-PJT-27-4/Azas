import { api } from '@/api'
import { getAuthMember } from '@/api/auth'
import { getAuthorizationHeader } from '@/api/http'
import type { ChildSummaryResponse } from '@/api/generated'

const CURRENT_CHILD_STORAGE_KEY = 'azas_current_child_id'

export const setCurrentChildId = (childId: number) => {
  sessionStorage.setItem(CURRENT_CHILD_STORAGE_KEY, String(childId))
}

export const getStoredCurrentChildId = () => {
  const value = Number(sessionStorage.getItem(CURRENT_CHILD_STORAGE_KEY))
  return Number.isInteger(value) && value > 0 ? value : null
}

export const getChildren = async (): Promise<ChildSummaryResponse[]> => {
  const { data } = await api.getChildrenUsingGET()
  return data.items ?? []
}

export const hasParentDemandDepositAccount = async () => {
  const { data } = await api.getMyAccountsUsingGET()
  return data.accounts.some(
    ({ account_product_type }) => account_product_type === 'DEMAND_DEPOSIT',
  )
}

export const resolveCurrentChildId = async () => {
  const storedChildId = getStoredCurrentChildId()
  if (storedChildId) return storedChildId

  if (getAuthMember()?.member_type === 'CHILD') {
    const { data } = await api.getDashboardUsingGET()
    const childId = data.child?.child_id
    if (!childId) throw new Error('연결된 자녀 정보를 찾을 수 없어요.')
    setCurrentChildId(childId)
    return childId
  }

  const children = await getChildren()
  const childId = children[0]?.child_id
  if (!childId) throw new Error('등록된 자녀가 없어요.')
  setCurrentChildId(childId)
  return childId
}

export const requireAuthorizationHeader = () => {
  const authorization = getAuthorizationHeader()
  if (!authorization) throw new Error('로그인이 필요해요.')
  return authorization
}
