<script setup lang="ts">
import { useRouter } from 'vue-router'

import onboardingImage from '@/assets/images/login/bg-hero.png'

const router = useRouter()

const startService = () => {
  router.push({ name: 'Home' })
}
</script>

<template>
  <main
    class="relative isolate min-h-dvh overflow-hidden bg-[#EAF7FE] text-[var(--color-text-primary)]"
  >
    <div class="absolute inset-x-0 top-0 -z-2 h-30 bg-[#EAF7FE]" aria-hidden="true"></div>

    <img
      class="absolute inset-x-0 top-40 bottom-20 -z-1 h-[calc(100%-15rem)] w-full object-cover object-[94%_100%]"
      :src="onboardingImage"
      alt="아이와 저금통이 함께 있는 자산관리 서비스 일러스트"
    />

    <div class="absolute inset-x-0 bottom-0 -z-1 h-20 bg-[#C7E7FE]" aria-hidden="true"></div>

    <section
      class="absolute top-[39%] right-6 left-6 -translate-y-1/2 text-center"
      aria-labelledby="onboarding-title"
    >
      <h1 id="onboarding-title" class="text-[32px] leading-[1.3] font-extrabold tracking-[-0.04em]">
        준비가 모두 끝났어요!
      </h1>
      <p class="mt-2 text-base text-[var(--color-text-secondary)]">
        우리 아이의 성장과 자산을 함께 기록해 보세요.
      </p>
    </section>

    <button
      class="absolute right-6 bottom-[max(32px,env(safe-area-inset-bottom))] left-6 h-14 rounded-xl bg-[var(--color-brand-primary)] text-lg font-bold text-[var(--color-text-inverse)] transition-colors active:bg-[var(--color-brand-primary-pressed)]"
      type="button"
      @click="startService"
    >
      시작하기
    </button>
  </main>
</template>
