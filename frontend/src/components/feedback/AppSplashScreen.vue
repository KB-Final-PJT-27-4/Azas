<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'

import splashCoinUrl from '@/assets/images/splash/splash-coin.png'
import splashHeartUrl from '@/assets/images/splash/splash-heart.png'
import splashPigUrl from '@/assets/images/splash/splash-pig.png'
import splashPopUrl from '@/assets/images/splash/splash-pop.png'

const emit = defineEmits<{ finished: [] }>()
const entered = ref(false)
let finishTimer: ReturnType<typeof setTimeout> | undefined
let enterTimer: ReturnType<typeof setTimeout> | undefined
let previousHtmlOverflow = ''
let previousBodyOverflow = ''

onMounted(() => {
  previousHtmlOverflow = document.documentElement.style.overflow
  previousBodyOverflow = document.body.style.overflow
  document.documentElement.style.overflow = 'hidden'
  document.body.style.overflow = 'hidden'

  enterTimer = setTimeout(() => (entered.value = true), 30)
  finishTimer = setTimeout(() => emit('finished'), 2100)
})

onBeforeUnmount(() => {
  if (enterTimer) clearTimeout(enterTimer)
  if (finishTimer) clearTimeout(finishTimer)
  document.documentElement.style.overflow = previousHtmlOverflow
  document.body.style.overflow = previousBodyOverflow
})
</script>

<template>
  <main
    class="fixed inset-0 z-[1000] grid h-dvh min-w-80 place-items-center overflow-hidden bg-[linear-gradient(180deg,#eaf8ff_0%,#dff4ff_50%,#eefaff_100%)] text-[var(--color-text-primary)]"
    role="status"
    aria-label="우리 아이 자산관리 서비스를 불러오는 중입니다"
  >
    <div
      class="absolute top-[8%] left-1/2 aspect-square w-[min(78vw,340px)] -translate-x-1/2 rounded-full bg-white/60 blur-[18px]"
      aria-hidden="true"
    ></div>

    <div
      class="relative grid w-[min(100%,var(--app-max-width))] justify-items-center px-[30px] pt-6 pb-[max(48px,env(safe-area-inset-bottom))] text-center"
    >
      <div
        class="relative aspect-[1/0.93] w-[min(88vw,378px)] [@media(max-height:700px)]:w-[min(62vh,310px)]"
        aria-hidden="true"
      >
        <img
          class="absolute top-[8%] right-[1%] z-[3] w-[16%] object-contain drop-shadow-[0_8px_12px_rgb(216_169_24_/_18%)] transition-all delay-200 duration-500 ease-out motion-safe:animate-bounce motion-safe:[animation-duration:2.8s]"
          :class="
            entered ? 'translate-y-0 scale-100 opacity-100' : 'translate-y-4 scale-50 opacity-0'
          "
          :src="splashCoinUrl"
          alt=""
        />
        <img
          class="absolute right-[2%] bottom-[5%] z-[3] w-[13%] object-contain drop-shadow-[0_8px_12px_rgb(216_169_24_/_18%)] transition-all delay-300 duration-500 ease-out motion-safe:animate-bounce motion-safe:[animation-duration:3.1s]"
          :class="
            entered ? 'translate-y-0 scale-100 opacity-100' : 'translate-y-4 scale-50 opacity-0'
          "
          :src="splashHeartUrl"
          alt=""
        />
        <img
          class="absolute top-[40%] left-[1%] z-[3] w-[16%] object-contain drop-shadow-[0_8px_12px_rgb(216_169_24_/_18%)] transition-all delay-[400ms] duration-500 ease-out motion-safe:animate-bounce motion-safe:[animation-duration:2.6s]"
          :class="
            entered ? 'translate-y-0 scale-100 opacity-100' : 'translate-y-4 scale-50 opacity-0'
          "
          :src="splashPopUrl"
          alt=""
        />
        <img
          class="absolute bottom-0 left-1/2 z-[2] w-[68%] -translate-x-1/2 object-contain drop-shadow-[0_18px_24px_rgb(235_123_155_/_18%)] transition-all duration-700 ease-out"
          :class="
            entered ? 'translate-y-0 scale-100 opacity-100' : 'translate-y-7 scale-[.82] opacity-0'
          "
          :src="splashPigUrl"
          alt=""
        />
      </div>

      <div
        class="mt-[30px] w-[min(100%,340px)] transition-all delay-500 duration-500 [@media(max-height:700px)]:mt-4"
        :class="entered ? 'translate-y-0 opacity-100' : 'translate-y-3 opacity-0'"
      >
        <h1 class="m-0 text-[28px] leading-[1.4] font-extrabold tracking-[-0.015em]">
          우리
          <strong
            class="inline-block text-[35px] leading-none font-black text-[#f2769d] drop-shadow-[0_5px_16px_rgb(242_118_157_/_24%)] motion-safe:animate-bounce motion-safe:[animation-duration:1.1s] motion-safe:[animation-iteration-count:1]"
            >아</strong
          >이
          <strong
            class="inline-block text-[35px] leading-none font-black text-[#f2769d] drop-shadow-[0_5px_16px_rgb(242_118_157_/_24%)] motion-safe:animate-bounce motion-safe:[animation-delay:130ms] motion-safe:[animation-duration:1.1s] motion-safe:[animation-iteration-count:1]"
            >자</strong
          >산관리 서비<strong
            class="inline-block text-[35px] leading-none font-black text-[#f2769d] drop-shadow-[0_5px_16px_rgb(242_118_157_/_24%)] motion-safe:animate-bounce motion-safe:[animation-delay:260ms] motion-safe:[animation-duration:1.1s] motion-safe:[animation-iteration-count:1]"
            >스</strong
          >
        </h1>
        <div class="mx-auto mt-2.5 w-fit max-w-full">
          <p class="m-0 text-sm font-medium text-[var(--color-text-secondary)]">
            아이의 오늘을 기록하고 내일의 자산을 키워요
          </p>
          <div
            class="mt-6 h-[5px] w-full overflow-hidden rounded-full bg-white/85 shadow-[inset_0_0_0_1px_rgb(85_192_244_/_9%)]"
            aria-hidden="true"
          >
            <span
              class="block h-full origin-left rounded-[inherit] bg-[var(--color-brand-primary)] transition-transform delay-300 duration-[1650ms] ease-out motion-reduce:scale-x-100"
              :class="entered ? 'scale-x-100' : 'scale-x-0'"
            ></span>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>
