import axios from 'axios'
import { http } from '@/api/http'

export type TimeCapsuleStatus = 'COLLECTING' | 'RELEASED'
export type TimeCapsuleEntryStatus = 'DRAFT' | 'SEALED' | 'DELETED'
export type TimeCapsuleMediaType = 'IMAGE' | 'VIDEO'

export type TimeCapsuleGoal = {
  financial_goal_template_id: number
  goal_name: string
  goal_target_amount: number
  goal_target_date: string
}

export type TimeCapsule = {
  time_capsule_id: number
  child_id: number
  financial_account_id: number
  title: string
  status: TimeCapsuleStatus
  expected_release_at: string | null
  release_reason: string | null
  released_at: string | null
  entry_count: number
  latest_entry_at: string | null
  goal: TimeCapsuleGoal | null
  created_at: string
}

export type TimeCapsuleSummary = Pick<
  TimeCapsule,
  | 'time_capsule_id'
  | 'financial_account_id'
  | 'title'
  | 'status'
  | 'expected_release_at'
  | 'entry_count'
  | 'latest_entry_at'
  | 'goal'
  | 'created_at'
> & {
  d_day: number | null
}

export type TimeCapsuleListResponse = {
  items: TimeCapsuleSummary[]
  next_cursor: string | null
  has_next: boolean
}

export type TimeCapsuleEntrySummary = {
  time_capsule_entry_id: number
  title: string
  contribution_amount: number
  contributed_at: string
  media_mode: string
  thumbnail_url: string | null
  thumbnail_expires_at: string | null
  status: TimeCapsuleEntryStatus
  media_count: number
}

export type TimeCapsuleEntryListResponse = {
  entries: TimeCapsuleEntrySummary[]
}

export type TimeCapsuleMedia = {
  time_capsule_media_id: number
  media_type: TimeCapsuleMediaType
  mime_type: string
  file_size: number
  slot_no: number
  download_url: string
  expires_at: string
}

export type TimeCapsuleEntry = {
  time_capsule_entry_id: number
  time_capsule_id: number
  author_member_id: number
  account_transaction_id: number
  title: string
  message: string | null
  contribution_amount: number
  contributed_at: string
  media_mode: string
  status: TimeCapsuleEntryStatus
  edit_count: number
  sealed_at: string | null
  media: TimeCapsuleMedia[]
  created_at: string
}

export type MediaUploadRequest = {
  mime_type: string
  file_size: number
  slot_no: number
}

export type MediaUpload = {
  time_capsule_media_id: number
  slot_no: number
  upload_url: string
  expires_at: string
  required_headers: Record<string, string>
}

export type MediaUploadUrlsResponse = {
  time_capsule_entry_id: number
  uploads: MediaUpload[]
}

export const getTimeCapsules = async (
  childId: number,
  params: {
    view?: 'CARD' | 'CALENDAR'
    status?: TimeCapsuleStatus
    cursor?: string
    size?: number
    year?: number
    month?: number
  } = {},
) => {
  const { data } = await http.get<TimeCapsuleListResponse>(
    `/v1/children/${childId}/time-capsules`,
    { params },
  )

  return data
}

export const getTimeCapsule = async (timeCapsuleId: number) => {
  const { data } = await http.get<TimeCapsule>(`/v1/time-capsules/${timeCapsuleId}`)

  return data
}

export const getTimeCapsuleEntries = async (timeCapsuleId: number) => {
  const { data } = await http.get<TimeCapsuleEntryListResponse>(
    `/v1/time-capsules/${timeCapsuleId}/entries`,
  )

  return data
}

export const getTimeCapsuleEntry = async (entryId: number) => {
  const { data } = await http.get<TimeCapsuleEntry>(`/v1/time-capsule-entries/${entryId}`)

  return data
}

export const updateTimeCapsuleEntry = async (
  entryId: number,
  request: { title?: string; message?: string },
) => {
  const { data } = await http.patch(`/v1/time-capsule-entries/${entryId}`, request)

  return data
}

export const deleteTimeCapsuleEntry = async (entryId: number) => {
  await http.delete(`/v1/time-capsule-entries/${entryId}`)
}

export const createMediaUploadUrls = async (entryId: number, files: MediaUploadRequest[]) => {
  const { data } = await http.post<MediaUploadUrlsResponse>(
    `/v1/time-capsule-entries/${entryId}/media/upload-urls`,
    { files },
  )

  return data
}

export const uploadMediaFile = async (upload: MediaUpload, file: File) => {
  await axios.put(upload.upload_url, file, {
    headers: upload.required_headers,
  })
}

export const completeMediaUpload = async (entryId: number, mediaIds: number[]) => {
  const { data } = await http.post(`/v1/time-capsule-entries/${entryId}/media/complete`, {
    media_ids: mediaIds,
  })

  return data
}

export const sealTimeCapsuleEntry = async (entryId: number) => {
  const { data } = await http.patch(`/v1/time-capsule-entries/${entryId}/seal`)

  return data
}
