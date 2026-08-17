<script setup lang="ts">
import AppSplashScreen from '@/components/feedback/AppSplashScreen.vue'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterView } from 'vue-router'

const showSplash = ref(true)
let splashTimer: ReturnType<typeof setTimeout> | undefined

onMounted(() => {
  splashTimer = setTimeout(() => {
    showSplash.value = false
  }, 2100)
})

onBeforeUnmount(() => {
  if (splashTimer) clearTimeout(splashTimer)
})
</script>

<template>
  <DefaultLayout>
    <RouterView />
  </DefaultLayout>

  <Transition name="app-splash">
    <AppSplashScreen v-if="showSplash" />
  </Transition>
</template>

<style scoped>
.app-splash-leave-active {
  transition:
    opacity 420ms ease,
    visibility 420ms ease;
}

.app-splash-leave-to {
  visibility: hidden;
  opacity: 0;
}
</style>
