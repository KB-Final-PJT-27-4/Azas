<script setup lang="ts">
import { computed, onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ChevronDown, ImagePlus, PiggyBank, X } from 'lucide-vue-next'
import previewCloudBackground from '@/assets/images/timeCapsules/preview-cloud-background.png'

const router = useRouter()
const step = ref<'form' | 'preview'>('form')
const isAccountMenuOpen = ref(false)
const isTransferMenuOpen = ref(false)

const accounts = [
  { id: 1, bank: 'KB국민은행', name: 'KB 아이사랑적금', number: '123-456-789' },
  { id: 2, bank: '카카오뱅크', name: '깨비 저금통', number: '3333-16-888867' },
  { id: 3, bank: '신한은행', name: '아이행복적금', number: '110-542-938124' },
]

const transfers = [
  { id: 1, date: '2026.07.21', name: '대학자금', amount: 100000 },
  { id: 2, date: '2026.07.14', name: '생일 축하금', amount: 50000 },
  { id: 3, date: '2026.07.01', name: '정기 저축', amount: 30000 },
]

const selectedAccountId = ref(1)
const selectedTransferId = ref(1)
const selectedAccount = computed(() => accounts.find(({ id }) => id === selectedAccountId.value)!)
const selectedTransfer = computed(() => transfers.find(({ id }) => id === selectedTransferId.value)!)
const title = ref('')
const letter = ref('')
type MediaItem = {
  file: File
  url: string
  type: 'image' | 'video'
  orientation: 'landscape' | 'portrait'
}

const mediaItems = ref<MediaItem[]>([])
const previewCarousel = ref<HTMLElement | null>(null)
const activeMediaIndex = ref(0)

const canPreview = computed(() => Boolean(selectedAccount.value && title.value.trim() && letter.value.trim()))
const formattedAmount = computed(() => `${selectedTransfer.value.amount.toLocaleString('ko-KR')}원`)

const selectAccount = (id: number) => {
  selectedAccountId.value = id
  isAccountMenuOpen.value = false
}

const selectTransfer = (id: number) => {
  selectedTransferId.value = id
  isTransferMenuOpen.value = false
}

const selectMedia = (event: Event) => {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files ?? [])

  const newItems = files.map((file) => ({
      file,
      url: URL.createObjectURL(file),
      type: file.type.startsWith('video/') ? ('video' as const) : ('image' as const),
      orientation: 'portrait' as const,
    }))

  mediaItems.value.push(...newItems)
  newItems.forEach((item) => detectMediaOrientation(item))
  input.value = ''
}

const detectMediaOrientation = (item: MediaItem) => {
  const updateOrientation = (width: number, height: number) => {
    const target = mediaItems.value.find(({ url }) => url === item.url)
    if (target) target.orientation = width > height ? 'landscape' : 'portrait'
  }

  if (item.type === 'image') {
    const image = new Image()
    image.onload = () => updateOrientation(image.naturalWidth, image.naturalHeight)
    image.src = item.url
    return
  }

  const video = document.createElement('video')
  video.onloadedmetadata = () => updateOrientation(video.videoWidth, video.videoHeight)
  video.src = item.url
}

const removeMedia = (index: number) => {
  URL.revokeObjectURL(mediaItems.value[index]!.url)
  mediaItems.value.splice(index, 1)
  activeMediaIndex.value = Math.min(activeMediaIndex.value, Math.max(mediaItems.value.length - 1, 0))
}

const updateActiveMedia = (event: Event) => {
  const carousel = event.currentTarget as HTMLElement
  if (!carousel.clientWidth) return
  activeMediaIndex.value = Math.round(carousel.scrollLeft / carousel.clientWidth)
}

const showMedia = (index: number) => {
  const carousel = previewCarousel.value
  if (!carousel) return
  activeMediaIndex.value = index
  carousel.scrollTo({ left: carousel.clientWidth * index, behavior: 'smooth' })
}

const showPreview = () => {
  if (canPreview.value) step.value = 'preview'
}

const createTimeCapsule = () => {
  router.push('/time-capsules')
}

onBeforeUnmount(() => {
  mediaItems.value.forEach(({ url }) => URL.revokeObjectURL(url))
})
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-white">
    <section v-if="step === 'form'" class="px-5 py-5">
      <h1 class="text-[23px] leading-tight font-bold tracking-[-0.025em] text-[var(--color-text-primary)]">
        오늘 어떤 순간을 기록할까요?
      </h1>
      <p class="mt-1.5 text-xs text-[var(--color-text-secondary)]">
        최근 저축 내역과 사진, 마음의 편지를 함께 남겨요.
      </p>

      <form class="mt-7 space-y-5" @submit.prevent="showPreview">
        <div class="relative">
          <button
            class="flex min-h-14 w-full items-center rounded-xl border border-[var(--color-border)] bg-white px-4 text-left"
            type="button"
            aria-haspopup="listbox"
            :aria-expanded="isAccountMenuOpen"
            @click="isAccountMenuOpen = !isAccountMenuOpen"
          >
            <span
              class="mr-3 grid size-8 shrink-0 place-items-center rounded-full bg-[#fff7dc] text-[#f5a300]"
              aria-hidden="true"
            >
              <PiggyBank :size="18" />
            </span>
            <span class="min-w-0 flex-1">
              <strong class="block truncate text-sm">{{ selectedAccount.name }}</strong>
              <span class="mt-0.5 block text-[11px] text-[var(--color-text-secondary)]">
                {{ selectedAccount.bank }} · {{ selectedAccount.number }}
              </span>
            </span>
            <ChevronDown :size="18" class="ml-2 shrink-0" aria-hidden="true" />
          </button>
          <div
            v-if="isAccountMenuOpen"
            class="absolute top-[calc(100%+6px)] right-0 left-0 z-20 overflow-hidden rounded-xl border border-[var(--color-border)] bg-white p-1.5 shadow-lg"
            role="listbox"
            aria-label="계좌 목록"
          >
            <button
              v-for="item in accounts"
              :key="item.id"
              class="flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left active:bg-[var(--color-selected-background)]"
              :class="item.id === selectedAccountId ? 'bg-[var(--color-selected-background)]' : ''"
              type="button"
              role="option"
              :aria-selected="item.id === selectedAccountId"
              @click="selectAccount(item.id)"
            >
              <span class="grid size-8 shrink-0 place-items-center rounded-full bg-[#fff7dc] text-[#f5a300]">
                <PiggyBank :size="17" />
              </span>
              <span class="min-w-0">
                <strong class="block truncate text-sm">{{ item.name }}</strong>
                <span class="text-[11px] text-[var(--color-text-secondary)]">{{ item.bank }} · {{ item.number }}</span>
              </span>
            </button>
          </div>
        </div>

        <label class="block">
          <span class="mb-2 block text-sm font-bold">제목 <em class="not-italic text-red-500">*</em></span>
          <input
            v-model="title"
            class="min-h-13 w-full rounded-xl border border-[var(--color-border)] px-4 text-sm outline-none placeholder:text-[#a1a9b4] focus:border-[var(--color-brand-primary)]"
            maxlength="30"
            placeholder="제목을 입력해주세요"
            required
          />
        </label>

        <fieldset class="relative">
          <legend class="mb-2 text-sm font-bold">이체 내역</legend>
          <button
            class="flex min-h-14 w-full items-center gap-3 rounded-xl border border-[#d8eff9] bg-[var(--color-selected-background)] px-4 text-left"
            type="button"
            aria-haspopup="listbox"
            :aria-expanded="isTransferMenuOpen"
            @click="isTransferMenuOpen = !isTransferMenuOpen"
          >
            <span class="text-xs text-[var(--color-text-secondary)]">{{ selectedTransfer.date }}</span>
            <strong class="min-w-0 flex-1 truncate text-sm">{{ selectedTransfer.name }}</strong>
            <strong class="text-xs text-[var(--color-selected-text)]">+{{ formattedAmount }}</strong>
            <ChevronDown :size="17" class="shrink-0 text-[var(--color-text-secondary)]" />
          </button>
          <div
            v-if="isTransferMenuOpen"
            class="absolute top-[calc(100%+6px)] right-0 left-0 z-20 overflow-hidden rounded-xl border border-[var(--color-border)] bg-white p-1.5 shadow-lg"
            role="listbox"
            aria-label="이체 내역 목록"
          >
            <button
              v-for="item in transfers"
              :key="item.id"
              class="flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left active:bg-[var(--color-selected-background)]"
              :class="item.id === selectedTransferId ? 'bg-[var(--color-selected-background)]' : ''"
              type="button"
              role="option"
              :aria-selected="item.id === selectedTransferId"
              @click="selectTransfer(item.id)"
            >
              <span class="w-[74px] text-[11px] text-[var(--color-text-secondary)]">{{ item.date }}</span>
              <strong class="min-w-0 flex-1 truncate text-sm">{{ item.name }}</strong>
              <strong class="text-xs text-[var(--color-selected-text)]">+{{ item.amount.toLocaleString('ko-KR') }}원</strong>
            </button>
          </div>
        </fieldset>

        <label class="block">
          <span class="mb-2 block text-sm font-bold">부모의 편지 <em class="not-italic text-red-500">*</em></span>
          <textarea
            v-model="letter"
            class="min-h-28 w-full resize-none rounded-xl border border-[var(--color-border)] px-4 py-3 text-sm leading-relaxed outline-none placeholder:text-[#a1a9b4] focus:border-[var(--color-brand-primary)]"
            maxlength="300"
            placeholder="오늘 깨비가 처음 걸었어요.&#10;앞으로도 건강하게 자라길 바래"
            required
          ></textarea>
        </label>

        <div>
          <span class="mb-2 block text-sm font-bold">사진 · 영상</span>
          <div v-if="mediaItems.length" class="grid grid-cols-3 gap-2">
            <div
              v-for="(media, index) in mediaItems"
              :key="media.url"
              class="relative aspect-square overflow-hidden rounded-xl bg-[var(--color-surface-muted)]"
            >
              <img v-if="media.type === 'image'" :src="media.url" :alt="`선택한 사진 ${index + 1}`" class="size-full object-cover" />
              <video v-else :src="media.url" class="size-full object-cover"></video>
              <button
                class="absolute top-1.5 right-1.5 grid size-7 place-items-center rounded-full bg-black/55 text-white"
                type="button"
                :aria-label="`첨부 파일 ${index + 1} 삭제`"
                @click="removeMedia(index)"
              >
                <X :size="15" />
              </button>
            </div>
            <label
              class="flex aspect-square cursor-pointer flex-col items-center justify-center rounded-xl border border-dashed border-[var(--color-border)] bg-[var(--color-surface-muted)] text-[var(--color-text-secondary)]"
            >
              <ImagePlus :size="25" class="text-[var(--color-brand-primary)]" />
              <span class="mt-2 text-[11px]">추가하기</span>
              <input class="sr-only" type="file" accept="image/*,video/*" multiple @change="selectMedia" />
            </label>
          </div>
          <label
            v-else
            class="flex min-h-32 cursor-pointer flex-col items-center justify-center rounded-xl border border-dashed border-[var(--color-border)] bg-[var(--color-surface-muted)] text-[var(--color-text-secondary)]"
          >
            <ImagePlus :size="30" class="text-[var(--color-brand-primary)]" />
            <span class="mt-3 text-xs">사진 또는 영상을 여러 개 추가해주세요</span>
            <input class="sr-only" type="file" accept="image/*,video/*" multiple @change="selectMedia" />
          </label>
        </div>

        <div class="grid grid-cols-2 gap-3 pt-2">
          <button
            class="min-h-13 rounded-xl border border-[var(--color-border)] bg-white text-sm font-bold text-[var(--color-text-secondary)]"
            type="button"
            @click="router.push('/time-capsules')"
          >
            취소
          </button>
          <button
            class="min-h-13 rounded-xl bg-[var(--color-brand-primary)] text-sm font-bold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[#c9d5dc]"
            type="submit"
            :disabled="!canPreview"
          >
            미리보기
          </button>
        </div>
      </form>
    </section>

    <section
      v-else
      class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-[#e9f7ff] bg-cover bg-top px-5 py-5"
      :style="{ backgroundImage: `url(${previewCloudBackground})` }"
    >
      <div class="flex min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height)-40px)] flex-col">
        <h1 class="text-xl font-bold text-[var(--color-text-primary)]">타임캡슐 미리보기</h1>
        <p class="mt-1 text-xs text-[var(--color-text-secondary)]">작성한 타임캡슐을 미리 확인해요.</p>

        <article class="mt-5 rounded-2xl bg-white p-4 shadow-[0_8px_28px_rgba(44,118,153,0.10)]">
          <p class="text-sm font-bold text-[var(--color-selected-text)]">
            저축 <span class="text-[var(--color-text-primary)]">{{ formattedAmount }}</span>
          </p>

          <div class="mt-3 overflow-hidden rounded-xl border border-[var(--color-border)] bg-white">
            <div
              v-if="mediaItems.length"
              ref="previewCarousel"
              class="flex aspect-[6/7] snap-x snap-mandatory overflow-x-auto bg-white [scrollbar-width:none] [&::-webkit-scrollbar]:hidden"
              @scroll.passive="updateActiveMedia"
            >
              <template v-for="(media, index) in mediaItems" :key="media.url">
                <img
                  v-if="media.type === 'image'"
                  :src="media.url"
                  :alt="`타임캡슐 사진 ${index + 1}`"
                  class="h-full w-full shrink-0 snap-center bg-white"
                  :class="media.orientation === 'landscape' ? 'object-contain' : 'object-cover'"
                />
                <video
                  v-else
                  :src="media.url"
                  class="h-full w-full shrink-0 snap-center bg-white"
                  :class="media.orientation === 'landscape' ? 'object-contain' : 'object-cover'"
                  controls
                ></video>
              </template>
            </div>
            <div v-else class="grid h-52 place-items-center bg-gradient-to-br from-[#fff9dc] to-[#eaf7ff]">
              <div class="text-center">
                <div class="mx-auto grid size-20 place-items-center rounded-full bg-white text-[var(--color-brand-primary)] shadow-sm">
                  <PiggyBank :size="44" />
                </div>
                <p class="mt-3 text-sm font-semibold text-[var(--color-text-secondary)]">소중한 오늘을 저장했어요</p>
              </div>
            </div>

            <div class="bg-[#f1f8fc] p-4">
              <div v-if="mediaItems.length > 1" class="mb-3 flex justify-center gap-1.5" aria-label="첨부 파일 수">
                <button
                  v-for="(media, index) in mediaItems"
                  :key="`dot-${media.url}`"
                  class="size-2 rounded-full transition-colors"
                  :class="index === activeMediaIndex ? 'bg-[#159fe3]' : 'bg-[#b9dff1]'"
                  type="button"
                  :aria-label="`${index + 1}번째 첨부 파일 보기`"
                  :aria-current="index === activeMediaIndex ? 'true' : undefined"
                  @click="showMedia(index)"
                ></button>
              </div>
              <div class="flex items-center gap-2">
                <h2 class="min-w-0 flex-1 truncate text-base font-bold">{{ title }}</h2>
                
              </div>
              <time class="mt-1 block text-xs text-[var(--color-text-secondary)]">{{ selectedTransfer.date }}</time>
              <div class="my-3 h-px bg-[var(--color-border)]"></div>
              <p class="mt-2 whitespace-pre-wrap text-sm leading-relaxed text-[var(--color-text-primary)]">{{ letter }}</p>
            </div>
          </div>
        </article>

        <div class="mt-auto grid grid-cols-2 gap-3 pt-5">
          <button
            class="min-h-13 rounded-xl border border-[var(--color-border)] bg-white text-sm font-bold text-[var(--color-text-secondary)]"
            type="button"
            @click="step = 'form'"
          >
            수정하기
          </button>
          <button
            class="min-h-13 rounded-xl bg-[var(--color-brand-primary)] text-sm font-bold text-white active:bg-[var(--color-brand-primary-pressed)]"
            type="button"
            @click="createTimeCapsule"
          >
            생성하기
          </button>
        </div>
      </div>
    </section>
  </main>
</template>
