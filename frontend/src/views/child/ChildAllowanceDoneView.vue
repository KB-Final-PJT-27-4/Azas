<script setup lang="ts">
import allowanceCompletePigUrl from '@/assets/images/child/child-allowance-complete-pig.png'
import allowancePageBgUrl from '@/assets/images/child/child-allowance-page-bg.png'
</script>

<template>
  <main
    class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col bg-[#eef8ff] bg-cover bg-top bg-no-repeat px-5 pt-16 pb-8"
    :style="{ backgroundImage: `url(${allowancePageBgUrl})` }"
  >
    <section class="grid flex-1 content-start justify-items-center text-center">
      <h1 class="m-0 text-[24px] leading-[1.35] font-black text-[var(--color-text-primary)]">
        용돈 요청이<br />
        전송되었어요!
      </h1>
      <p
        class="mt-4 mb-10 text-[length:var(--font-size-sm)] leading-[1.5] text-[var(--color-text-secondary)]"
      >
        부모님이 확인하시면<br />
        알림으로 알려드릴게요
      </p>
      <img
        class="w-[400px] select-none object-contain"
        :src="allowanceCompletePigUrl"
        alt=""
        aria-hidden="true"
      />
    </section>

    <RouterLink
      class="flex h-14 w-full items-center justify-center rounded-[14px] bg-[var(--color-brand-primary)] text-[length:var(--font-size-md)] font-extrabold !text-[#FFFFFF] shadow-[0_12px_28px_rgb(85_192_244_/_22%)]"
      to="/child/home"
    >
      확인
    </RouterLink>
  </main>
</template>
