export type StoredTimeCapsuleEntry = {
  id: number
  timeCapsuleId: number
  title: string
  message: string
  contributedAt: string
  contributionAmount: number
  hasPhoto?: boolean
}

const STORAGE_KEY = 'azas_time_capsule_text_only_entries_v1'

const readEntries = () => {
  if (typeof window === 'undefined') return []

  try {
    const parsed = JSON.parse(window.localStorage.getItem(STORAGE_KEY) ?? '[]')
    if (!Array.isArray(parsed)) return []

    return parsed.filter(
      (entry): entry is StoredTimeCapsuleEntry =>
        typeof entry?.id === 'number' &&
        typeof entry?.timeCapsuleId === 'number' &&
        typeof entry?.title === 'string' &&
        typeof entry?.message === 'string' &&
        typeof entry?.contributedAt === 'string' &&
        typeof entry?.contributionAmount === 'number',
    )
  } catch {
    return []
  }
}

const writeEntries = (entries: StoredTimeCapsuleEntry[]) => {
  if (typeof window === 'undefined') return
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(entries))
}

export const getStoredTimeCapsuleEntries = (timeCapsuleId?: number) => {
  const entries = readEntries()
  if (timeCapsuleId === undefined) return entries
  return entries.filter((entry) => entry.timeCapsuleId === timeCapsuleId)
}

export const getStoredTimeCapsuleEntry = (entryId: number) =>
  readEntries().find((entry) => entry.id === entryId) ?? null

export const saveStoredTimeCapsuleEntry = (entry: StoredTimeCapsuleEntry) => {
  const entries = readEntries().filter((storedEntry) => storedEntry.id !== entry.id)
  entries.push(entry)
  writeEntries(entries)
}

export const removeStoredTimeCapsuleEntry = (entryId: number) => {
  writeEntries(readEntries().filter((entry) => entry.id !== entryId))
}
