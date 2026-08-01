<script setup lang="ts">
import backgroundDefaultImage from '@/assets/images/login/bg-default.png'
import backgroundHeroImage from '@/assets/images/login/bg-hero.png'
import googleLoginImage from '@/assets/images/login/google-login.png'
import kakaoLoginImage from '@/assets/images/login/kakao_login.png'
</script>

<template>
  <main class="min-h-dvh overflow-hidden bg-[#eaf7ff] text-[#102b50]">
    <section
      class="relative min-h-[420px] px-8 pt-[62px] pb-12 max-[360px]:min-h-[380px] max-[360px]:px-6 max-[360px]:py-11"
      aria-labelledby="login-title"
    >
      <img
        class="absolute inset-0 size-full object-cover object-bottom"
        :src="backgroundHeroImage"
        alt=""
      />

      <div class="relative z-1">
        <p class="mb-[30px] grid gap-1 text-lg leading-[1.35] font-[650] max-[360px]:mb-[22px]">
          <strong class="text-[25px] font-extrabold">우리 아이</strong>
          <span>자산 관리 서비스</span>
        </p>

        <h1
          id="login-title"
          class="grid gap-0.5 text-[clamp(29px,8.2vw,38px)] leading-[1.35] font-extrabold tracking-[-0.045em]"
        >
          아이의 오늘을 기록하고
          <strong class="text-[#438be7]">내일의 자산을 키워요</strong>
        </h1>
      </div>
    </section>

    <section
      class="relative z-2 -mt-8 min-h-[500px] bg-white px-6 pt-[52px] pb-[190px] text-center max-[360px]:px-[18px]"
      aria-labelledby="login-panel-title"
    >
      <img
        class="pointer-events-none absolute bottom-0 left-1/2 h-[195px] max-w-none -translate-x-1/2"
        :src="backgroundDefaultImage"
        alt=""
      />

      <div class="relative z-1 grid gap-3">
        <p class="flex items-center justify-center gap-2.5 text-xl font-[750] text-[#438be7]">
          <span class="text-xs text-[#77b9f4]" aria-hidden="true">♥</span>
          안녕하세요!
          <span class="text-xs text-[#77b9f4]" aria-hidden="true">♥</span>
        </p>
        <h2
          id="login-panel-title"
          class="text-[23px] leading-[1.4] font-bold tracking-[-0.035em] text-[#10213c] max-[360px]:text-xl"
        >
          우리 아이의 금융성장을 시작해요
        </h2>
        <p class="text-[15px] leading-[1.6] tracking-[-0.025em] text-[#687587]">
          간편하게 로그인하고 필요한 정보를 차근차근 채워볼게요.
        </p>
      </div>

      <div class="relative z-1 mt-12 grid gap-4">
        <button
          class="grid h-16 w-full cursor-pointer grid-cols-[42px_1fr_42px] items-center overflow-hidden rounded-full border border-[#d9dde3] bg-white px-5 text-lg font-bold text-[#132239] shadow-[0_4px_10px_rgb(31_48_69_/_12%)] transition-transform duration-120 outline-none active:scale-[0.985] focus-visible:outline-3 focus-visible:outline-offset-3 focus-visible:outline-[rgb(67_139_231_/_35%)] max-[360px]:text-base"
          type="button"
        >
          <img class="size-9" :src="googleLoginImage" alt="" />
          <span>Google로 시작하기</span>
        </button>

        <button
          class="grid h-16 w-full cursor-pointer grid-cols-[42px_1fr_42px] items-center overflow-hidden rounded-full border-0 bg-[#fee500] px-5 text-lg font-bold text-[#171600] shadow-[0_4px_10px_rgb(31_48_69_/_10%)] transition-transform duration-120 outline-none active:scale-[0.985] focus-visible:outline-3 focus-visible:outline-offset-3 focus-visible:outline-[rgb(67_139_231_/_35%)] max-[360px]:text-base"
          type="button"
        >
          <img class="size-9" :src="kakaoLoginImage" alt="" />
          <span>카카오로 시작하기</span>
        </button>
      </div>
    </section>
  </main>
</template>
