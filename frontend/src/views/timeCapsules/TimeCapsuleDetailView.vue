<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { AlertTriangle, Trash2, X } from 'lucide-vue-next'
import { getTimeCapsuleAccount, getTimeCapsuleRecord } from '@/data/timeCapsuleDummyData'
import { useToast } from '@/composables/useToast'

const route = useRoute()
const router = useRouter()
const { showToast } = useToast()
const carousel = ref<HTMLElement | null>(null)
const activePhotoIndex = ref(0)
const isDeleteDialogOpen = ref(false)
const isDeleting = ref(false)

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
const closeDeleteDialog = () => {
  if (!isDeleting.value) isDeleteDialogOpen.value = false
}

const deleteRecord = async () => {
  if (isDeleting.value) return
  isDeleting.value = true

  try {
    const account = getTimeCapsuleAccount(accountId.value)
    const recordIndex = account.records.findIndex(({ id }) => String(id) === recordId.value)
    if (recordIndex < 0) throw new Error('Time capsule record not found')

    account.records.splice(recordIndex, 1)
    isDeleteDialogOpen.value = false
    await router.replace(`/time-capsules/${accountId.value}`)
    showToast('타임캡슐을 삭제했습니다.', 'success')
  } catch {
    showToast('삭제에 실패했습니다. 다시 시도해주세요.', 'error')
  } finally {
    isDeleting.value = false
  }
}
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
        v-if="record.photos.length"
        ref="carousel"
        class="mt-6 flex aspect-[6/7] snap-x snap-mandatory overflow-x-auto rounded-2xl bg-white [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
        @scroll.passive="updateActivePhoto"
      >
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
          class="flex min-h-13 items-center justify-center gap-1.5 rounded-xl bg-[#e85b61] text-sm font-bold text-white active:bg-[#cf484e]"
          type="button"
          @click="isDeleteDialogOpen = true"
        >
          <Trash2 :size="16" />
          삭제하기
        </button>
      </div>
    </article>

    <Teleport to="body">
      <Transition name="delete-sheet">
        <div v-if="isDeleteDialogOpen" class="fixed inset-0 z-[var(--z-index-overlay)] flex items-end justify-center bg-black/40" @click.self="closeDeleteDialog">
          <section class="delete-sheet-panel w-full max-w-[var(--app-max-width)] rounded-t-[26px] bg-white px-5 pt-3 pb-[calc(24px+env(safe-area-inset-bottom))]" role="alertdialog" aria-modal="true" aria-labelledby="detail-delete-title" aria-describedby="detail-delete-description">
            <span class="mx-auto block h-1 w-10 rounded-full bg-[#d7dfe4]"></span>
            <div class="mt-5 flex items-start gap-3.5">
              <span class="grid size-11 shrink-0 place-items-center rounded-full bg-[#fff0f1] text-[#e2535a]"><AlertTriangle :size="22" /></span>
              <div class="min-w-0 flex-1 pt-0.5">
                <h2 id="detail-delete-title" class="m-0 text-[19px] font-bold">타임캡슐을 삭제할까요?</h2>
                <p id="detail-delete-description" class="mt-1.5 mb-0 text-xs leading-relaxed text-[var(--color-text-secondary)]">삭제한 타임캡슐은 다시 복구할 수 없어요.</p>
              </div>
              <button class="grid size-9 shrink-0 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[#f2f5f7] disabled:opacity-40" type="button" aria-label="삭제 확인창 닫기" :disabled="isDeleting" @click="closeDeleteDialog"><X :size="20" /></button>
            </div>

            <div class="mt-5 rounded-2xl bg-[#f7f9fa] px-4 py-3.5">

              <strong class="mt-1 block truncate text-sm">{{ record.title }}</strong>
              <span class="mt-1 block text-[11px] text-[var(--color-text-secondary)]">{{ record.date.replaceAll('-', '.') }}</span>
            </div>

            <div class="mt-5 grid grid-cols-2 gap-3">
              <button class="h-[52px] rounded-xl border border-[var(--color-border)] bg-white text-sm font-bold text-[var(--color-text-secondary)] active:bg-[#f5f7f8] disabled:opacity-50" type="button" :disabled="isDeleting" @click="closeDeleteDialog">취소</button>
              <button class="flex h-[52px] items-center justify-center gap-1.5 rounded-xl bg-[#e85b61] text-sm font-bold text-white active:bg-[#cf484e] disabled:opacity-55" type="button" :disabled="isDeleting" @click="deleteRecord">
                <Trash2 v-if="!isDeleting" :size="16" />
                {{ isDeleting ? '삭제 중...' : '삭제하기' }}
              </button>
            </div>
          </section>
        </div>
      </Transition>
    </Teleport>
  </main>
</template>

<style scoped>
.delete-sheet-enter-active,
.delete-sheet-leave-active { transition: background-color 180ms ease; }
.delete-sheet-enter-active .delete-sheet-panel,
.delete-sheet-leave-active .delete-sheet-panel { transition: transform 220ms cubic-bezier(0.22, 1, 0.36, 1); }
.delete-sheet-enter-from,
.delete-sheet-leave-to { background-color: transparent; }
.delete-sheet-enter-from .delete-sheet-panel,
.delete-sheet-leave-to .delete-sheet-panel { transform: translateY(100%); }

@media (prefers-reduced-motion: reduce) {
  .delete-sheet-enter-active,
  .delete-sheet-leave-active,
  .delete-sheet-enter-active .delete-sheet-panel,
  .delete-sheet-leave-active .delete-sheet-panel { transition-duration: 1ms; }
}
</style>
