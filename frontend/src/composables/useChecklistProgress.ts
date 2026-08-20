import { ref } from 'vue'

import { checklistItems, type ChecklistItem } from '@/mocks/lifecycleChecklist'

const CHECKLIST_STORAGE_KEY = 'azas_checked_checklist_item_ids'

const loadCheckedItemIds = () => {
  const defaultIds = checklistItems.filter((item) => item.completed).map((item) => item.id)
  const storedIds = localStorage.getItem(CHECKLIST_STORAGE_KEY)

  if (!storedIds) return new Set(defaultIds)

  try {
    return new Set(JSON.parse(storedIds) as ChecklistItem['id'][])
  } catch {
    localStorage.removeItem(CHECKLIST_STORAGE_KEY)
    return new Set(defaultIds)
  }
}

const checkedItemIds = ref(loadCheckedItemIds())

export const useChecklistProgress = () => {
  const isChecklistItemCompleted = (item: ChecklistItem) => checkedItemIds.value.has(item.id)

  const toggleChecklistItem = (item: ChecklistItem) => {
    const nextCheckedItemIds = new Set(checkedItemIds.value)

    if (nextCheckedItemIds.has(item.id)) {
      nextCheckedItemIds.delete(item.id)
    } else {
      nextCheckedItemIds.add(item.id)
    }

    checkedItemIds.value = nextCheckedItemIds
    localStorage.setItem(CHECKLIST_STORAGE_KEY, JSON.stringify([...nextCheckedItemIds]))
  }

  return {
    checkedItemIds,
    isChecklistItemCompleted,
    toggleChecklistItem,
  }
}
