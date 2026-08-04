<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChevronLeft, PiggyBank } from 'lucide-vue-next'
import { getTimeCapsuleRecord } from '@/data/timeCapsuleDummyData'

const route = useRoute()
const router = useRouter()
const carousel = ref<HTMLElement | null>(null)
const activePhotoIndex = ref(0)

const accountId = computed(() => String(route.params.capsuleListId))
const recordId = computed(() => String(route.params.capsuleId))
const record = computed(() => getTimeCapsuleRecord(accountId.value, recordId.value))

const updateActivePhoto = (event: Event) => {
  const target = event.currentTarget as HTMLElement
  if (!target.clientWidth) return
  activePhotoIndex.value = Math.round(target.scrollLeft / target.clientWidth)
}

const showPhoto = (index: number) => {
  if (!carousel.value) return
  activePhotoIndex.value = index
  carousel.value.scrollTo({ left: carousel.value.clientWidth * index, behavior: 'smooth' })
}

const goToList = () => router.push(`/time-capsules/${accountId.value}`)
const editRecord = () => router.push(`/time-capsules/${recordId.value}/edit`)
</script>

<template>
  <main
    class="flex min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] flex-col bg-white"
  >
    <article class="flex flex-1 flex-col px-5 pt-7 pb-5">
      <div class="flex items-start gap-4 border-b border-[var(--color-border)] pb-5">
        <div class="min-w-0 flex-1">
          <h1 class="truncate text-[24px] font-bold tracking-[-0.025em]">{{ record.title }}</h1>
          <time class="mt-1.5 block text-sm text-[var(--color-text-secondary)]">
            {{ record.date.replaceAll('-', '.') }}
          </time>
        </div>
        <strong class="shrink-0 pt-1 text-xl text-[var(--color-selected-text)]">
          {{ record.amount.toLocaleString('ko-KR') }}원
        </strong>
      </div>

      <div
        ref="carousel"
        class="mt-6 flex aspect-[6/7] snap-x snap-mandatory overflow-x-auto rounded-2xl bg-white [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
        @scroll.passive="updateActivePhoto"
      >
        <template v-if="record.photos.length">
          <template v-for="(photo, index) in record.photos" :key="`${photo.src}-${index}`">
            <video
              v-if="photo.type === 'video'"
              class="h-full w-full shrink-0 snap-center bg-white"
              :class="photo.orientation === 'landscape' ? 'object-contain' : 'object-cover'"
              :src="photo.src"
              controls
            ></video>
            <img
              v-else
              class="h-full w-full shrink-0 snap-center bg-white"
              :class="photo.orientation === 'landscape' ? 'object-contain' : 'object-cover'"
              :src="photo.src"
              :alt="`${record.title} 사진 ${index + 1}`"
            />
          </template>
        </template>
        <div v-else class="grid h-full w-full shrink-0 place-items-center bg-gradient-to-br from-[#fff9dc] to-[#eaf7ff]">
          <div class="text-center text-[var(--color-brand-primary)]">
            <PiggyBank :size="52" class="mx-auto" />
            <p class="mt-3 text-sm font-semibold text-[var(--color-text-secondary)]">소중한 오늘을 저장했어요</p>
          </div>
        </div>
      </div>

      <div v-if="record.photos.length > 1" class="mt-3 flex justify-center gap-1.5">
        <button
          v-for="(_, index) in record.photos"
          :key="index"
          class="size-2 rounded-full transition-colors"
          :class="index === activePhotoIndex ? 'bg-[#159fe3]' : 'bg-[#d6e3e9]'"
          type="button"
          :aria-label="`${index + 1}번째 사진 보기`"
          :aria-current="index === activePhotoIndex ? 'true' : undefined"
          @click="showPhoto(index)"
        ></button>
      </div>

      <p class="mt-7 whitespace-pre-wrap text-[15px] leading-relaxed text-[var(--color-text-primary)]">
        {{ record.letter }}
      </p>

      <div class="mt-auto grid grid-cols-2 gap-3 pt-10">
        <button
          class="min-h-13 rounded-xl border border-[var(--color-border)] bg-white text-sm font-bold text-[var(--color-text-secondary)]"
          type="button"
          @click="goToList"
        >
          목록으로 이동
        </button>
        <button
          class="min-h-13 rounded-xl bg-[var(--color-brand-primary)] text-sm font-bold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[#c9d5dc]"
          type="button"
          :disabled="record.remainingEdits === 0"
          @click="editRecord"
        >
          수정하기 ({{ record.remainingEdits }}/1)
        </button>
      </div>
    </article>
  </main>
</template>
