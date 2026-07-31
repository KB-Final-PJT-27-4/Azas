<script setup lang="ts">
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { RouterView, useRoute } from 'vue-router'

const route = useRoute()
</script>

<template>
  <RouterView v-if="route.meta.hideDefaultLayout" />

  <DefaultLayout v-else>
    <RouterView />
  </DefaultLayout>
</template>

<style scoped></style>
