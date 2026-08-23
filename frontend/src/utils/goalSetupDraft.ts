const GOAL_SETUP_DRAFT_KEY = 'azas_goal_setup_draft'

type StoredGoalSetupDraft = {
  selectedGoals?: string[]
  linkedSavings?: Record<string, string[]>
}

export const addOpenedSavingsToGoalSetupDraft = (accountId?: number) => {
  if (accountId == null) return

  try {
    const storedDraft = sessionStorage.getItem(GOAL_SETUP_DRAFT_KEY)
    if (!storedDraft) return

    const draft = JSON.parse(storedDraft) as StoredGoalSetupDraft
    const goalId = draft.selectedGoals?.[0]
    if (!goalId) return

    const linkedSavings = draft.linkedSavings ?? {}
    const selectedAccountIds = new Set(linkedSavings[goalId] ?? [])
    selectedAccountIds.add(String(accountId))

    sessionStorage.setItem(
      GOAL_SETUP_DRAFT_KEY,
      JSON.stringify({
        ...draft,
        linkedSavings: {
          ...linkedSavings,
          [goalId]: [...selectedAccountIds],
        },
      }),
    )
  } catch {
    // 임시 목표 데이터가 손상된 경우에도 적금 개설 완료 흐름은 유지합니다.
  }
}
