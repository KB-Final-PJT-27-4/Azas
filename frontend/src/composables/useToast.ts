import { readonly, ref } from 'vue'

export type ToastVariant = 'success' | 'error' | 'info'

const message = ref('')
const variant = ref<ToastVariant>('info')
let timer: ReturnType<typeof window.setTimeout> | null = null

export const useToast = () => {
  const showToast = (nextMessage: string, nextVariant: ToastVariant = 'info', duration = 2200) => {
    if (timer !== null) window.clearTimeout(timer)

    message.value = nextMessage
    variant.value = nextVariant
    timer = window.setTimeout(() => {
      message.value = ''
      timer = null
    }, duration)
  }

  return {
    toastMessage: readonly(message),
    toastVariant: readonly(variant),
    showToast,
  }
}
