<script setup lang="ts">
import { useRouter } from 'vue-router'

import backgroundDefaultImage from '@/assets/images/login/bg-default.png'
import backgroundHeroImage from '@/assets/images/login/bg-hero.png'
import googleLoginImage from '@/assets/images/login/google-login.png'
import kakaoLoginImage from '@/assets/images/login/kakao_login.png'

const router = useRouter()

const moveToRegister = (invited = false) => {
  router.push({
    name: 'Register',
    query: invited ? { invited: 'true' } : undefined,
  })
}
</script>

<template>
  <main
    class="min-h-dvh overflow-hidden bg-[var(--color-selected-background)] text-[var(--color-text-primary)]"
  >
    <section
      class="relative min-h-[420px] px-8 pt-[62px] pb-12 max-[360px]:min-h-[380px] max-[360px]:px-6 max-[360px]:py-11"
      aria-labelledby="login-title"
    >
      <img
        class="absolute inset-0 size-full object-cover object-right-bottom"
        :src="backgroundHeroImage"
        alt=""
      />

      <div class="relative z-1">
        <p class="mb-[30px] grid gap-1 text-lg leading-[1.35] font-[650] max-[360px]:mb-[22px]">
          <strong class="text-[25px] font-bold">우리 아이</strong>
          <span>자산 관리 서비스</span>
        </p>

        <h1
          id="login-title"
          class="grid gap-0.5 text-[clamp(29px,8.2vw,38px)] leading-[1.35] font-bold tracking-[-0.045em]"
        >
          아이의 오늘을 기록하고
          <div class="text-[var(--color-selected-text)]">내일의 자산을 키워요</div>
        </h1>
      </div>
    </section>

    <section
      class="relative z-2 -mt-8 min-h-[500px] bg-[var(--color-surface)] px-6 pt-[52px] pb-[190px] text-center max-[360px]:px-[18px]"
      aria-labelledby="login-panel-title"
    >
      <img
        class="pointer-events-none absolute bottom-0 left-1/2 h-[220px] max-w-none -translate-x-1/2"
        :src="backgroundDefaultImage"
        alt=""
      />

      <div class="relative z-1 grid gap-3">
        <p
          class="flex items-center justify-center gap-2.5 text-xl font-[750] text-[var(--color-selected-text)]"
        >
          <span class="text-xs text-[var(--color-brand-primary)]" aria-hidden="true">♥</span>
          안녕하세요!
          <span class="text-xs text-[var(--color-brand-primary)]" aria-hidden="true">♥</span>
        </p>
        <h2
          id="login-panel-title"
          class="text-[23px] leading-[1.4] font-bold tracking-[-0.035em] text-[var(--color-text-primary)] max-[360px]:text-xl"
        >
          우리 아이의 금융성장을 시작해요
        </h2>
        <p class="text-[15px] leading-[1.6] tracking-[-0.025em] text-[var(--color-text-secondary)]">
          간편하게 로그인하고 필요한 정보를 차근차근 채워볼게요.
        </p>
      </div>

      <div class="relative z-1 mt-12 grid gap-4">
        <button
          class="grid h-16 w-full cursor-pointer grid-cols-[42px_1fr_42px] items-center overflow-hidden rounded-full border border-[var(--color-border)] bg-[var(--color-surface)] px-5 text-lg font-bold text-[var(--color-text-primary)] shadow-md transition-transform duration-120 outline-none active:scale-[0.985] focus-visible:outline-3 focus-visible:outline-offset-3 focus-visible:outline-[var(--color-brand-primary)] max-[360px]:text-base"
          type="button"
          @click="moveToRegister()"
        >
          <img class="size-9" :src="googleLoginImage" alt="" />
          <span>Google로 시작하기</span>
        </button>

        <button
          class="grid h-16 w-full cursor-pointer grid-cols-[42px_1fr_42px] items-center overflow-hidden rounded-full border-0 bg-[#fee500] px-5 text-lg font-bold text-[#171600] shadow-md transition-transform duration-120 outline-none active:scale-[0.985] focus-visible:outline-3 focus-visible:outline-offset-3 focus-visible:outline-[var(--color-brand-primary)] max-[360px]:text-base"
          type="button"
          @click="moveToRegister(true)"
        >
          <img class="size-9" :src="kakaoLoginImage" alt="" />
          <span>카카오로 시작하기</span>
        </button>
      </div>
    </section>
  </main>
</template>
