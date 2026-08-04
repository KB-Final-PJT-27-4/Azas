<script setup lang="ts">
import { useRouter } from 'vue-router'
import calendarImage from '@/assets/images/timeCapsules/archive/calendar.png'
import capsuleBackground from '@/assets/images/timeCapsules/archive/capsule-background.png'
import lockImage from '@/assets/images/timeCapsules/archive/lock.png'
import openImage from '@/assets/images/timeCapsules/archive/open.png'

const router = useRouter()

const capsuleAccounts = [
  {
    id: 1,
    name: '아이사랑적금',
    createdAt: '2027.07.16',
    savedAmount: 200000,
    image: lockImage,
    imageAlt: '잠긴 타임캡슐',
  },
  {
    id: 2,
    name: '우리사랑적금',
    createdAt: '2027.07.18',
    savedAmount: 200000,
    image: openImage,
    imageAlt: '열린 타임캡슐',
  },
]
</script>

<template>
  <main
    class="flex min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] flex-col bg-white"
  >
    <section class="relative overflow-hidden px-5 pt-7 pb-8">
      <div
        class="pointer-events-none absolute top-14 right-0 left-0 h-56 bg-cover bg-center bg-no-repeat"
        :style="{ backgroundImage: `url(${capsuleBackground})` }"
        aria-hidden="true"
      ></div>

      <div class="relative">
        <h1
          class="text-[24px] leading-tight font-bold tracking-[-0.025em] text-[var(--color-text-primary)]"
        >
          타임캡슐 보관함
        </h1>
        <p class="mt-2 text-sm text-[var(--color-text-secondary)]">
          깨비의 성장 순간과 금융 기록을 모아보세요.
        </p>

        <article
          class="mt-32 flex min-h-20 items-center rounded-2xl border border-[var(--color-border)] bg-white/95 px-4 shadow-[0_8px_24px_rgba(67,139,179,0.08)] backdrop-blur-sm"
        >
          <img class="size-14 shrink-0 object-contain" :src="calendarImage" alt="공개 예정일" />
          <div class="ml-3 min-w-0 flex-1">
            <p class="text-xs text-[var(--color-text-secondary)]">다음 공개까지</p>
            <p class="mt-1 truncate text-sm font-bold text-[var(--color-text-primary)]">
              아이사랑적금 <span class="font-medium">· 2026.08.08</span>
            </p>
          </div>
          <strong class="ml-3 shrink-0 text-[30px] font-bold tracking-[-0.04em] text-[#27a9eb]">
            D-23
          </strong>
        </article>
      </div>
    </section>

    <section class="grid grid-cols-2 gap-3 px-5 pt-1" aria-label="타임캡슐 계좌 목록">
      <button
        v-for="capsule in capsuleAccounts"
        :key="capsule.id"
        class="overflow-hidden rounded-2xl border border-[var(--color-border)] bg-white p-3 text-left shadow-[0_4px_16px_rgba(39,91,119,0.04)] transition-transform active:scale-[0.98]"
        type="button"
        @click="router.push(`/time-capsules/${capsule.id}`)"
      >
        <span class="grid aspect-[4/3] place-items-center rounded-xl bg-[#ecfaff]">
          <img class="h-16 w-20 object-contain" :src="capsule.image" :alt="capsule.imageAlt" />
        </span>
        <strong class="mt-4 block truncate text-base text-[var(--color-text-primary)]">
          {{ capsule.name }}
        </strong>
        <time class="mt-3 block text-xs text-[var(--color-text-secondary)]">
          {{ capsule.createdAt }}
        </time>
        <p class="mt-3 text-xs font-bold text-[var(--color-selected-text)]">
          저축 금액 {{ capsule.savedAmount.toLocaleString('ko-KR') }}원
        </p>
      </button>
    </section>

    <div class="mt-auto flex justify-end px-5 pt-10 pb-5">
      <button
        class="min-h-12 rounded-xl bg-[var(--color-brand-primary)] px-5 text-sm font-bold text-white shadow-[0_8px_20px_rgba(85,192,244,0.25)] active:bg-[var(--color-brand-primary-pressed)]"
        type="button"
        @click="router.push('/time-capsules/new')"
      >
        타임캡슐 생성하기
      </button>
    </div>
  </main>
</template>
