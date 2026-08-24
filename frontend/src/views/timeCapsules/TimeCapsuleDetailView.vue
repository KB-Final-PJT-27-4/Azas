<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { AlertTriangle, Trash2, X } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import { api, getApiErrorMessage } from '@/api'
import {
  getStoredTimeCapsuleEntry,
  removeStoredTimeCapsuleEntry,
} from '@/utils/timeCapsuleTextEntries'

const route = useRoute()
const router = useRouter()
const { showToast } = useToast()
const carousel = ref<HTMLElement | null>(null)
const activePhotoIndex = ref(0)
const isDeleteDialogOpen = ref(false)
const isDeleting = ref(false)
const deleteSheetOffset = ref(0)
const isDeleteSheetDragging = ref(false)
let deleteSheetDragStartY = 0
let deleteSheetDragStartTime = 0

const accountId = computed(() => String(route.params.capsuleListId))
const recordId = computed(() => String(route.params.capsuleId))
const record = ref({ title: '타임캡슐', date: '', amount: 0, letter: '', photos: [] as Array<{ src: string; type: 'image' | 'video'; orientation: 'portrait' | 'landscape' }> })

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
  if (!isDeleting.value) {
    deleteSheetOffset.value = 0
    isDeleteSheetDragging.value = false
    isDeleteDialogOpen.value = false
  }
}

const startDeleteSheetDrag = (event: PointerEvent) => {
  if (isDeleting.value || event.button !== 0) return
  deleteSheetDragStartY = event.clientY
  deleteSheetDragStartTime = performance.now()
  isDeleteSheetDragging.value = true
  ;(event.currentTarget as HTMLElement).setPointerCapture(event.pointerId)
}

const moveDeleteSheetDrag = (event: PointerEvent) => {
  if (!isDeleteSheetDragging.value) return
  deleteSheetOffset.value = Math.max(0, event.clientY - deleteSheetDragStartY)
}

const endDeleteSheetDrag = (event: PointerEvent) => {
  if (!isDeleteSheetDragging.value) return
  const elapsed = Math.max(performance.now() - deleteSheetDragStartTime, 1)
  const velocity = deleteSheetOffset.value / elapsed
  isDeleteSheetDragging.value = false

  if (deleteSheetOffset.value >= 72 || velocity >= 0.45) {
    closeDeleteDialog()
    return
  }

  deleteSheetOffset.value = 0
  const target = event.currentTarget as HTMLElement
  if (target.hasPointerCapture(event.pointerId)) target.releasePointerCapture(event.pointerId)
}

const deleteRecord = async () => {
  if (isDeleting.value) return
  isDeleting.value = true

  try {
    await api.deleteTimeCapsuleEntryUsingDELETE(Number(recordId.value))
    removeStoredTimeCapsuleEntry(Number(recordId.value))
    isDeleteDialogOpen.value = false
    await router.replace(`/time-capsules/${accountId.value}`)
    showToast('타임캡슐을 삭제했습니다.', 'success')
  } catch (error) {
    const storedEntry = getStoredTimeCapsuleEntry(Number(recordId.value))
    if (storedEntry) {
      removeStoredTimeCapsuleEntry(storedEntry.id)
      isDeleteDialogOpen.value = false
      await router.replace(`/time-capsules/${accountId.value}`)
      showToast('타임캡슐을 삭제했습니다.', 'success')
      return
    }
    showToast(getApiErrorMessage(error, '삭제에 실패했습니다.'), 'error')
  } finally {
    isDeleting.value = false
  }
}

onMounted(async () => {
  try {
    const { data } = await api.getTimeCapsuleEntryUsingGET(Number(recordId.value))
    record.value = {
      title: data.title ?? '타임캡슐',
      date: data.contributed_at?.slice(0, 10) ?? '',
      amount: data.contribution_amount ?? 0,
      letter: data.message ?? '',
      photos: data.image?.url ? [{
        src: data.image.url,
        type: 'image',
        orientation: 'portrait',
      }] : [],
    }
  } catch (error) {
    const storedEntry = getStoredTimeCapsuleEntry(Number(recordId.value))
    if (storedEntry) {
      record.value = {
        title: storedEntry.title || '타임캡슐',
        date: storedEntry.contributedAt.slice(0, 10),
        amount: storedEntry.contributionAmount,
        letter: storedEntry.message,
        photos: [],
      }
      return
    }
    showToast(getApiErrorMessage(error, '타임캡슐 기록을 불러오지 못했습니다.'), 'error')
  }
})
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
          <section
            class="delete-sheet-panel w-full max-w-[var(--app-max-width)] rounded-t-[26px] bg-white px-5 pt-3 pb-[calc(24px+env(safe-area-inset-bottom))]"
            :class="isDeleteSheetDragging ? 'delete-sheet-panel--dragging' : ''"
            :style="deleteSheetOffset ? { transform: `translateY(${deleteSheetOffset}px)` } : undefined"
            role="alertdialog"
            aria-modal="true"
            aria-labelledby="detail-delete-title"
            aria-describedby="detail-delete-description"
          >
            <div
              class="-mx-5 -mt-3 flex h-10 touch-none cursor-grab items-center justify-center active:cursor-grabbing"
              role="button"
              tabindex="0"
              aria-label="아래로 밀어 삭제 확인창 닫기"
              @pointerdown="startDeleteSheetDrag"
              @pointermove="moveDeleteSheetDrag"
              @pointerup="endDeleteSheetDrag"
              @pointercancel="endDeleteSheetDrag"
            >
              <span class="block h-1 w-10 rounded-full bg-[#d7dfe4]"></span>
            </div>
            <div class="mt-2 flex items-start gap-3.5">
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
.delete-sheet-panel { transition: transform 180ms cubic-bezier(0.22, 1, 0.36, 1); }
.delete-sheet-panel--dragging { transition: none !important; }

@media (prefers-reduced-motion: reduce) {
  .delete-sheet-enter-active,
  .delete-sheet-leave-active,
  .delete-sheet-enter-active .delete-sheet-panel,
  .delete-sheet-leave-active .delete-sheet-panel { transition-duration: 1ms; }
}
</style>
