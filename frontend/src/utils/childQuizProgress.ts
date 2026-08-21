const CHILD_QUIZ_COMPLETED_DATE_KEY = 'azas.childQuiz.completedDate'

const getTodayKey = () => {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const date = String(today.getDate()).padStart(2, '0')

  return `${year}-${month}-${date}`
}

export const isChildQuizCompletedToday = () => {
  if (typeof window === 'undefined') return false

  return window.localStorage.getItem(CHILD_QUIZ_COMPLETED_DATE_KEY) === getTodayKey()
}

export const markChildQuizCompletedToday = () => {
  if (typeof window === 'undefined') return

  window.localStorage.setItem(CHILD_QUIZ_COMPLETED_DATE_KEY, getTodayKey())
}
