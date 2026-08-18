export type GuardianRole = 'father' | 'mother' | 'guardian'
export type ChildGender = 'male' | 'female' | 'unknown'

export interface RegistrationDraft {
  guardianRole: GuardianRole
  childName: string
  birthDate: string
  gender: ChildGender
  invited: boolean
}

const STORAGE_KEY = 'azas-registration-draft'

export const saveRegistrationDraft = (draft: RegistrationDraft) => {
  sessionStorage.setItem(STORAGE_KEY, JSON.stringify(draft))
}

export const loadRegistrationDraft = (): RegistrationDraft | null => {
  try {
    const storedDraft = sessionStorage.getItem(STORAGE_KEY)
    if (!storedDraft) return null

    const draft = JSON.parse(storedDraft) as Partial<RegistrationDraft>
    if (!draft.childName || !draft.birthDate || !draft.guardianRole || !draft.gender) return null

    return draft as RegistrationDraft
  } catch {
    return null
  }
}
