<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { RouterView } from 'vue-router'
import { useToast } from '@/composables/useToast'

const { showToast } = useToast()
let unsubscribeFromPushMessages: (() => void) | null = null

onMounted(() => {
  void import('@/services/pushNotifications').then(
    ({ subscribeToForegroundPushMessages, syncPushNotificationsIfPermitted }) => {
      unsubscribeFromPushMessages = subscribeToForegroundPushMessages((message) => {
        showToast(`${message.title}: ${message.body}`, 'info', 5000)
      })

      return syncPushNotificationsIfPermitted()
    },
  )
})

onUnmounted(() => {
  unsubscribeFromPushMessages?.()
})
</script>

<template>
  <DefaultLayout>
    <RouterView />
  </DefaultLayout>
</template>
