<script setup lang="ts">
import { onMounted } from 'vue'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { RouterView } from 'vue-router'

onMounted(() => {
  void import('@/services/pushNotifications').then(({ syncPushNotificationsIfPermitted }) =>
    syncPushNotificationsIfPermitted(),
  )
})
</script>

<template>
  <DefaultLayout>
    <RouterView />
  </DefaultLayout>
</template>
