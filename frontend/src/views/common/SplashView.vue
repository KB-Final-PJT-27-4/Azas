<script setup lang="ts">
import { useRouter } from 'vue-router'

import AppSplashScreen from '@/components/feedback/AppSplashScreen.vue'

const router = useRouter()
const goToLogin = () => router.replace('/login')
</script>

<template>
  <AppSplashScreen @finished="goToLogin" />
</template>
