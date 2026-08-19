import { readonly, ref } from 'vue'

export type ToastVariant = 'success' | 'error' | 'info'
export type ToastPlacement = 'default' | 'slightly-above' | 'above-actions'

const message = ref('')
const variant = ref<ToastVariant>('info')
const placement = ref<ToastPlacement>('default')
let timer: ReturnType<typeof window.setTimeout> | null = null

export const useToast = () => {
  const showToast = (
    nextMessage: string,
    nextVariant: ToastVariant = 'info',
    duration = 2200,
    nextPlacement: ToastPlacement = 'default',
  ) => {
    if (timer !== null) window.clearTimeout(timer)

    message.value = nextMessage
    variant.value = nextVariant
    placement.value = nextPlacement
    timer = window.setTimeout(() => {
      message.value = ''
      placement.value = 'default'
      timer = null
    }, duration)
  }

  return {
    toastMessage: readonly(message),
    toastVariant: readonly(variant),
    toastPlacement: readonly(placement),
    showToast,
  }
}
