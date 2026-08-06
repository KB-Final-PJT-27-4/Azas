<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Check, ChevronDown, ImagePlus, Landmark, X } from 'lucide-vue-next'
import {
  findTimeCapsuleRecord,
  timeCapsuleAccounts,
  type TimeCapsulePhoto,
} from '@/data/timeCapsuleDummyData'
import { useToast } from '@/composables/useToast'

type EditMedia = TimeCapsulePhoto & {
  isNew?: boolean
}

const route = useRoute()
const router = useRouter()
const { showToast } = useToast()
const recordId = String(route.params.capsuleId)
const initialData = findTimeCapsuleRecord(recordId)

const selectedAccountId = ref(String(initialData.account.id))
const selectedTransferId = ref(initialData.record.id)
const title = ref(initialData.record.title)
const letter = ref(initialData.record.letter)
const mediaItems = ref<EditMedia[]>(initialData.record.photos.map((photo) => ({ ...photo })))
const hasSaved = ref(false)
const isAccountMenuOpen = ref(false)
const isTransferMenuOpen = ref(false)

const accounts = Object.values(timeCapsuleAccounts)
const selectedAccount = computed(() => timeCapsuleAccounts[selectedAccountId.value] ?? initialData.account)
const transferOptions = computed(() => selectedAccount.value.records)
const selectedTransfer = computed(
  () => transferOptions.value.find(({ id }) => id === selectedTransferId.value) ?? transferOptions.value[0]!,
)
const hasChanges = computed(() => {
  const initialPhotos = initialData.record.photos
  const photosChanged =
    mediaItems.value.length !== initialPhotos.length ||
    mediaItems.value.some((media, index) => {
      const initialPhoto = initialPhotos[index]

      return (
        !initialPhoto ||
        media.src !== initialPhoto.src ||
        media.type !== initialPhoto.type ||
        media.orientation !== initialPhoto.orientation
      )
    })

  return (
    selectedAccountId.value !== String(initialData.account.id) ||
    selectedTransferId.value !== initialData.record.id ||
    title.value !== initialData.record.title ||
    letter.value !== initialData.record.letter ||
    photosChanged
  )
})
const canSave = computed(() =>
  Boolean(
    hasChanges.value &&
      title.value.trim() &&
      letter.value.trim() &&
      selectedTransfer.value &&
      initialData.record.remainingEdits > 0,
  ),
)
const accountDisplayName = (bankName: string, accountName: string) =>
  bankName === 'KB국민은행' ? `KB ${accountName}` : accountName

watch(selectedAccountId, () => {
  if (!transferOptions.value.some(({ id }) => id === selectedTransferId.value)) {
    selectedTransferId.value = transferOptions.value[0]!.id
  }
})

const toggleAccountMenu = () => {
  isAccountMenuOpen.value = !isAccountMenuOpen.value
  isTransferMenuOpen.value = false
}

const selectAccount = (accountId: number) => {
  selectedAccountId.value = String(accountId)
  isAccountMenuOpen.value = false
}

const toggleTransferMenu = () => {
  isTransferMenuOpen.value = !isTransferMenuOpen.value
  isAccountMenuOpen.value = false
}

const selectTransfer = (transferId: number) => {
  selectedTransferId.value = transferId
  isTransferMenuOpen.value = false
}

const detectOrientation = (item: EditMedia) => {
  const update = (width: number, height: number) => {
    const target = mediaItems.value.find(({ src }) => src === item.src)
    if (target) target.orientation = width > height ? 'landscape' : 'portrait'
  }

  const image = new Image()
  image.onload = () => update(image.naturalWidth, image.naturalHeight)
  image.src = item.src
}

const addMedia = (event: Event) => {
  const input = event.target as HTMLInputElement
  const files = Array.from(input.files ?? [])
  const items = files.map<EditMedia>((file) => ({
    src: URL.createObjectURL(file),
    type: file.type.startsWith('video/') ? 'video' : 'image',
    orientation: 'portrait',
    isNew: true,
  }))
  mediaItems.value.push(...items)
  items.forEach(detectOrientation)
  input.value = ''
}

const removeMedia = (index: number) => {
  const item = mediaItems.value[index]
  if (item?.isNew) URL.revokeObjectURL(item.src)
  mediaItems.value.splice(index, 1)
}

const cancelEdit = () => router.back()

const saveEdit = async () => {
  if (!canSave.value) {
    showToast('필수 내용을 모두 입력해주세요.', 'error')
    return
  }

  if (initialData.record.remainingEdits <= 0) {
    showToast('수정 가능 횟수를 모두 사용했습니다.', 'error')
    return
  }

  const sourceAccount = initialData.account
  const targetAccount = selectedAccount.value
  const sourceIndex = sourceAccount.records.findIndex(({ id }) => id === initialData.record.id)

  try {
    if (sourceIndex < 0) throw new Error('Time capsule record not found')

    const updatedRecord = {
      ...initialData.record,
      title: title.value.trim(),
      date: selectedTransfer.value.date,
      amount: selectedTransfer.value.amount,
      transferName: selectedTransfer.value.transferName,
      letter: letter.value.trim(),
      photos: mediaItems.value.map(({ src, orientation, type }) => ({ src, orientation, type })),
      thumbnail: mediaItems.value[0]?.src ?? initialData.record.thumbnail,
      remainingEdits: Math.max(initialData.record.remainingEdits - 1, 0),
    }

    if (sourceAccount.id === targetAccount.id) {
      sourceAccount.records.splice(sourceIndex, 1, updatedRecord)
    } else {
      sourceAccount.records.splice(sourceIndex, 1)
      targetAccount.records.push(updatedRecord)
    }

    hasSaved.value = true
    await router.replace(`/time-capsules/${targetAccount.id}/${updatedRecord.id}`)
    showToast('수정되었습니다.', 'success')
  } catch {
    hasSaved.value = false
    showToast('수정에 실패했습니다. 다시 시도해주세요.', 'error')
  }
}

onBeforeUnmount(() => {
  if (hasSaved.value) return
  mediaItems.value.filter(({ isNew }) => isNew).forEach(({ src }) => URL.revokeObjectURL(src))
})
</script>

<template>
  <main
    class="flex min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] flex-col bg-white"
  >
    <section class="flex flex-1 flex-col px-5 py-5">
      <h1 class="text-[23px] leading-tight font-bold tracking-[-0.025em] text-[var(--color-text-primary)]">
        오늘 어떤 순간을 기록할까요?
      </h1>
      <p class="mt-1.5 text-xs text-[var(--color-text-secondary)]">
        최근 저축 내역과 사진, 마음의 편지를 함께 남겨요.
      </p>

    <form class="mt-7 flex flex-1 flex-col gap-5" @submit.prevent="saveEdit">
      <div class="relative">
        <button
          class="flex h-14 w-full items-center gap-3 rounded-[12px] border bg-white px-3 text-left transition-colors"
          :class="
            isAccountMenuOpen
              ? 'border-[var(--color-brand-primary)] ring-2 ring-[#e5f7ff]'
              : 'border-[#dce8ee]'
          "
          type="button"
          aria-haspopup="listbox"
          :aria-expanded="isAccountMenuOpen"
          @click="toggleAccountMenu"
        >
          <span
            class="grid size-7 shrink-0 place-items-center rounded-full border border-[#ffad20] bg-[#fffaf0] text-[#ff9f00]"
            aria-hidden="true"
          >
            <Landmark :size="15" />
          </span>
          <span class="min-w-0 flex-1">
            <strong class="block truncate text-sm">
              {{ accountDisplayName(selectedAccount.bankName, selectedAccount.name) }}
            </strong>
            <span class="mt-0.5 block text-[11px] text-[var(--color-text-secondary)]">
              {{ selectedAccount.bankName }} · {{ selectedAccount.accountNumber }}
            </span>
          </span>
          <ChevronDown
            :size="20"
            class="shrink-0 text-[var(--color-text-secondary)] transition-transform duration-150"
            :class="isAccountMenuOpen ? 'rotate-180' : ''"
            aria-hidden="true"
          />
        </button>

        <Transition
          enter-active-class="transition duration-150 ease-out"
          enter-from-class="-translate-y-1 opacity-0"
          leave-active-class="transition duration-100 ease-in"
          leave-to-class="-translate-y-1 opacity-0"
        >
          <ul
            v-if="isAccountMenuOpen"
            class="absolute top-[calc(100%+6px)] right-0 left-0 z-20 m-0 max-h-48 list-none overflow-y-auto rounded-[14px] border border-[#dce8ee] bg-white p-1.5 shadow-[0_10px_28px_rgba(45,77,94,0.16)]"
            role="listbox"
            aria-label="계좌 목록"
          >
            <li
              v-for="account in accounts"
              :key="account.id"
              role="option"
              :aria-selected="String(account.id) === selectedAccountId"
            >
              <button
                class="flex w-full items-center gap-2.5 rounded-[10px] px-2.5 py-2 text-left"
                :class="
                  String(account.id) === selectedAccountId
                    ? 'bg-[#effaff]'
                    : 'hover:bg-[#f6f9fb] active:bg-[#edf3f6]'
                "
                type="button"
                @click="selectAccount(account.id)"
              >
                <span
                  class="grid size-7 shrink-0 place-items-center rounded-full border border-[#ffad20] bg-[#fffaf0] text-[#ff9f00]"
                >
                  <Landmark :size="14" />
                </span>
                <span class="min-w-0 flex-1">
                  <strong class="block truncate text-[13px]">
                    {{ accountDisplayName(account.bankName, account.name) }}
                  </strong>
                  <span class="mt-0.5 block truncate text-[11px] text-[var(--color-text-secondary)]">
                    {{ account.bankName }} · {{ account.accountNumber }}
                  </span>
                </span>
                <span
                  class="grid size-5 shrink-0 place-items-center rounded-full"
                  :class="
                    String(account.id) === selectedAccountId
                      ? 'bg-[var(--color-brand-primary)] text-white'
                      : 'text-transparent'
                  "
                  aria-hidden="true"
                >
                  <Check :size="13" :stroke-width="3" />
                </span>
              </button>
            </li>
          </ul>
        </Transition>
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
          class="flex h-12 w-full items-center gap-3 rounded-[12px] border bg-white px-3 text-left transition-colors"
          :class="
            isTransferMenuOpen
              ? 'border-[var(--color-brand-primary)] ring-2 ring-[#e5f7ff]'
              : 'border-[#dce8ee]'
          "
          type="button"
          aria-haspopup="listbox"
          :aria-expanded="isTransferMenuOpen"
          @click="toggleTransferMenu"
        >
          <span class="text-xs text-[var(--color-text-secondary)]">{{ selectedTransfer.date.replaceAll('-', '.') }}</span>
          <strong class="min-w-0 flex-1 truncate text-sm">{{ selectedTransfer.transferName }}</strong>
          <strong class="text-xs text-[var(--color-selected-text)]">
            +{{ selectedTransfer.amount.toLocaleString('ko-KR') }}원
          </strong>
          <ChevronDown
            :size="20"
            class="shrink-0 text-[var(--color-text-secondary)] transition-transform duration-150"
            :class="isTransferMenuOpen ? 'rotate-180' : ''"
          />
        </button>

        <Transition
          enter-active-class="transition duration-150 ease-out"
          enter-from-class="-translate-y-1 opacity-0"
          leave-active-class="transition duration-100 ease-in"
          leave-to-class="-translate-y-1 opacity-0"
        >
          <ul
            v-if="isTransferMenuOpen"
            class="absolute top-[calc(100%+6px)] right-0 left-0 z-20 m-0 max-h-48 list-none overflow-y-auto rounded-[14px] border border-[#dce8ee] bg-white p-1.5 shadow-[0_10px_28px_rgba(45,77,94,0.16)]"
            role="listbox"
            aria-label="이체 내역 목록"
          >
            <li
              v-for="transfer in transferOptions"
              :key="transfer.id"
              role="option"
              :aria-selected="transfer.id === selectedTransferId"
            >
              <button
                class="flex w-full items-center gap-3 rounded-[10px] px-2.5 py-2.5 text-left"
                :class="
                  transfer.id === selectedTransferId
                    ? 'bg-[#effaff]'
                    : 'hover:bg-[#f6f9fb] active:bg-[#edf3f6]'
                "
                type="button"
                @click="selectTransfer(transfer.id)"
              >
                <span class="w-[74px] text-[11px] text-[var(--color-text-secondary)]">
                  {{ transfer.date.replaceAll('-', '.') }}
                </span>
                <strong class="min-w-0 flex-1 truncate text-[13px]">{{ transfer.transferName }}</strong>
                <strong class="shrink-0 text-xs text-[var(--color-selected-text)]">
                  +{{ transfer.amount.toLocaleString('ko-KR') }}원
                </strong>
              </button>
            </li>
          </ul>
        </Transition>
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
            :key="`${media.src}-${index}`"
            class="relative aspect-square overflow-hidden rounded-xl bg-[var(--color-surface-muted)]"
          >
            <video v-if="media.type === 'video'" class="size-full object-cover" :src="media.src"></video>
            <img v-else class="size-full object-cover" :src="media.src" :alt="`첨부 사진 ${index + 1}`" />
            <button
              class="absolute top-1.5 right-1.5 grid size-7 place-items-center rounded-full bg-black/55 text-white"
              type="button"
              :aria-label="`${index + 1}번째 사진 삭제`"
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
            <input class="sr-only" type="file" accept="image/*,video/*" multiple @change="addMedia" />
          </label>
        </div>
        <label
          v-else
          class="flex min-h-32 cursor-pointer flex-col items-center justify-center rounded-xl border border-dashed border-[var(--color-border)] bg-[var(--color-surface-muted)] text-[var(--color-text-secondary)]"
        >
          <ImagePlus :size="30" class="text-[var(--color-brand-primary)]" />
          <span class="mt-3 text-xs">사진 또는 영상을 여러 개 추가해주세요</span>
          <input class="sr-only" type="file" accept="image/*,video/*" multiple @change="addMedia" />
        </label>
      </div>

      <div class="mt-auto grid grid-cols-2 gap-3 pt-5">
        <button
          class="min-h-13 rounded-xl border border-[var(--color-border)] bg-white text-sm font-bold text-[var(--color-text-secondary)]"
          type="button"
          @click="cancelEdit"
        >
          취소
        </button>
        <button
          class="min-h-13 rounded-xl bg-[var(--color-brand-primary)] text-sm font-bold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[#c9d5dc]"
          type="submit"
          :disabled="!canSave"
        >
          저장하기
        </button>
      </div>
    </form>
    </section>
  </main>
</template>
