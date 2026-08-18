<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { AlertTriangle, Check, ChevronDown, ImagePlus, Landmark, Trash2, X } from 'lucide-vue-next'
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
const initialPhoto = initialData.record.photos.find(({ type }) => type === 'image')
const mediaItems = ref<EditMedia[]>(initialPhoto ? [{ ...initialPhoto }] : [])
const hasSaved = ref(false)
const isAccountMenuOpen = ref(false)
const isTransferMenuOpen = ref(false)
const isDeleteDialogOpen = ref(false)
const isDeleting = ref(false)

const accounts = Object.values(timeCapsuleAccounts)
const selectedAccount = computed(() => timeCapsuleAccounts[selectedAccountId.value] ?? initialData.account)
const transferOptions = computed(() => selectedAccount.value.records)
const selectedTransfer = computed(
  () => transferOptions.value.find(({ id }) => id === selectedTransferId.value) ?? transferOptions.value[0]!,
)
const hasChanges = computed(() => {
  const initialPhotos = initialPhoto ? [initialPhoto] : []
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

const closeSelectMenus = () => {
  isAccountMenuOpen.value = false
  isTransferMenuOpen.value = false
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
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    showToast('사진 파일만 선택할 수 있어요.', 'error')
    input.value = ''
    return
  }

  mediaItems.value.filter(({ isNew }) => isNew).forEach(({ src }) => URL.revokeObjectURL(src))
  const item: EditMedia = {
    src: URL.createObjectURL(file),
    type: 'image',
    orientation: 'portrait',
    isNew: true,
  }
  mediaItems.value = [item]
  detectOrientation(item)
  input.value = ''
}

const removeMedia = (index: number) => {
  const item = mediaItems.value[index]
  if (item?.isNew) URL.revokeObjectURL(item.src)
  mediaItems.value.splice(index, 1)
}

const cancelEdit = () => router.back()

const openDeleteDialog = () => {
  isAccountMenuOpen.value = false
  isTransferMenuOpen.value = false
  isDeleteDialogOpen.value = true
}

const closeDeleteDialog = () => {
  if (!isDeleting.value) isDeleteDialogOpen.value = false
}

const deleteRecord = async () => {
  if (isDeleting.value) return
  isDeleting.value = true

  try {
    const sourceAccount = initialData.account
    const sourceIndex = sourceAccount.records.findIndex(({ id }) => id === initialData.record.id)
    if (sourceIndex < 0) throw new Error('Time capsule record not found')

    sourceAccount.records.splice(sourceIndex, 1)
    isDeleteDialogOpen.value = false
    await router.replace(`/time-capsules/${sourceAccount.id}`)
    showToast('타임캡슐을 삭제했습니다.', 'success')
  } catch {
    showToast('삭제에 실패했습니다. 다시 시도해주세요.', 'error')
  } finally {
    isDeleting.value = false
  }
}

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
    <Teleport to="#app-header-action">
      <button
        class="grid size-11 place-items-center rounded-full text-[#df5a5f] transition-colors active:bg-[#fff0f1]"
        type="button"
        aria-label="타임캡슐 삭제"
        title="타임캡슐 삭제"
        @click="openDeleteDialog"
      >
        <Trash2 :size="21" :stroke-width="2.1" />
      </button>
    </Teleport>

    <section class="flex flex-1 flex-col px-5 pt-5 pb-[calc(24px+env(safe-area-inset-bottom))]">
      <h1 class="text-[24px] leading-tight font-extrabold tracking-[-0.035em] text-[var(--color-text-primary)]">
        기록한 순간을 다듬어볼까요?
      </h1>
      <p class="mt-2 text-[13px] leading-5 text-[var(--color-text-secondary)]">
        저장한 내용과 대표 사진을 다시 확인하고 수정해요.
      </p>

    <form class="mt-6 flex flex-1 flex-col gap-7" @submit.prevent="saveEdit">
      <button
        v-if="isAccountMenuOpen || isTransferMenuOpen"
        class="fixed inset-0 z-10 cursor-default border-0 bg-transparent"
        type="button"
        aria-label="선택 목록 닫기"
        @click="closeSelectMenus"
      ></button>

      <div class="relative">
        <span class="mb-3 block text-sm font-bold">연결 계좌</span>
        <button
          class="flex h-14 w-full items-center gap-3 rounded-2xl border bg-white px-4 text-left transition-colors"
          :class="
            isAccountMenuOpen
              ? 'border-[#91d5f1] bg-[#fbfeff]'
              : 'border-[#d6e3e9]'
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
            class="absolute top-[calc(100%+8px)] right-0 left-0 z-20 m-0 max-h-56 list-none overflow-y-auto rounded-[20px] border border-[#d6e3e9] bg-white p-2 shadow-[0_14px_36px_rgba(45,77,94,0.12)]"
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
                class="flex min-h-[62px] w-full items-center gap-3 rounded-2xl px-4 py-2.5 text-left transition-colors"
                :class="
                  String(account.id) === selectedAccountId
                    ? 'bg-[#f0faff]'
                    : 'active:bg-[#f5f8fa]'
                "
                type="button"
                @click="selectAccount(account.id)"
              >
                <span class="min-w-0 flex-1">
                  <strong class="block truncate text-[13px]">
                    {{ accountDisplayName(account.bankName, account.name) }}
                  </strong>
                  <span class="mt-0.5 block truncate text-[11px] text-[var(--color-text-secondary)]">
                    {{ account.bankName }} · {{ account.accountNumber }}
                  </span>
                </span>
                <span
                  class="grid size-6 shrink-0 place-items-center rounded-full border"
                  :class="
                    String(account.id) === selectedAccountId
                      ? 'border-[var(--color-brand-primary)] bg-[var(--color-brand-primary)] text-white'
                      : 'border-[#d5e1e7] bg-white text-transparent'
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
        <span class="mb-3 block text-sm font-bold">제목 <em class="not-italic text-red-500">*</em></span>
        <input
          v-model="title"
            class="h-14 w-full rounded-2xl border border-[#d6e3e9] bg-white px-4 text-sm outline-none transition focus:border-[#91d5f1] focus:ring-2 focus:ring-[#edf9fe] placeholder:text-[#a1a9b4]"
          maxlength="30"
          placeholder="제목을 입력해주세요"
          required
        />
      </label>

      <fieldset class="relative">
        <legend class="mb-3 text-sm font-bold">
          이체 내역 <em class="not-italic text-red-500">*</em>
        </legend>
        <button
          class="flex h-14 w-full items-center gap-3 rounded-2xl border bg-white px-4 text-left transition-colors"
          :class="
            isTransferMenuOpen
              ? 'border-[#91d5f1] bg-[#fbfeff]'
              : 'border-[#d6e3e9]'
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
            class="absolute top-[calc(100%+8px)] right-0 left-0 z-20 m-0 max-h-56 list-none overflow-y-auto rounded-[20px] border border-[#d6e3e9] bg-white p-2 shadow-[0_14px_36px_rgba(45,77,94,0.12)]"
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
                class="flex min-h-[58px] w-full items-center gap-3 rounded-2xl px-4 py-2.5 text-left transition-colors"
                :class="
                  transfer.id === selectedTransferId
                    ? 'bg-[#f0faff]'
                    : 'active:bg-[#f5f8fa]'
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
        <span class="mb-3 block text-sm font-bold">부모의 편지 <em class="not-italic text-red-500">*</em></span>
        <textarea
          v-model="letter"
          class="min-h-[120px] w-full resize-none rounded-2xl border border-[#d6e3e9] bg-white px-4 py-3.5 text-sm leading-relaxed outline-none transition focus:border-[#91d5f1] focus:ring-2 focus:ring-[#edf9fe] placeholder:text-[#a1a9b4]"
          maxlength="300"
          placeholder="오늘 깨비가 처음 걸었어요.&#10;앞으로도 건강하게 자라길 바래"
          required
        ></textarea>
      </label>

      <div>
        <span class="mb-3 block text-sm font-bold">대표 사진</span>
        <div
          v-if="mediaItems[0]"
          class="relative overflow-hidden rounded-[20px] border border-[#d6e3e9] bg-[#f5f8fa]"
        >
          <img
            class="aspect-[16/10] w-full bg-[#f4f7f9] object-contain"
            :src="mediaItems[0].src"
            alt="선택한 대표 사진"
          />
          <div class="absolute inset-x-0 bottom-0 flex items-center justify-end gap-2 bg-gradient-to-t from-black/55 to-transparent px-3 pt-8 pb-3">
            <label class="cursor-pointer rounded-lg bg-white/95 px-3 py-2 text-[11px] font-bold text-[var(--color-text-primary)] active:bg-white">
              사진 바꾸기
              <input class="sr-only" type="file" accept="image/*" @change="addMedia" />
            </label>
            <button
              class="grid size-8 place-items-center rounded-lg bg-black/55 text-white"
              type="button"
              aria-label="대표 사진 삭제"
              @click="removeMedia(0)"
            >
              <X :size="15" />
            </button>
          </div>
        </div>
        <label
          v-else
          class="flex min-h-36 cursor-pointer flex-col items-center justify-center rounded-[20px] border border-dashed border-[#cbdde6] bg-[#f5fbfe] text-[var(--color-text-secondary)] transition-colors active:bg-[#ebf8fd]"
        >
          <ImagePlus :size="30" class="text-[var(--color-brand-primary)]" />
          <span class="mt-3 text-xs font-semibold">대표 사진 한 장을 추가해주세요</span>
          <span class="mt-1 text-[10px] text-[#98a5ad]">새 사진을 선택하면 기존 사진이 교체돼요.</span>
          <input class="sr-only" type="file" accept="image/*" @change="addMedia" />
        </label>
      </div>

      <div class="mt-auto grid grid-cols-2 gap-3 pt-2">
        <button
          class="h-14 rounded-2xl border border-[#d6e3e9] bg-white text-sm font-bold text-[var(--color-text-secondary)] active:bg-[#f5f7f8]"
          type="button"
          @click="cancelEdit"
        >
          취소
        </button>
        <button
          class="h-14 rounded-2xl bg-[var(--color-brand-primary)] text-sm font-bold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[#c9d5dc]"
          type="submit"
          :disabled="!canSave"
        >
          저장하기
        </button>
      </div>
    </form>
    </section>

    <Teleport to="body">
      <Transition name="delete-sheet">
        <div v-if="isDeleteDialogOpen" class="fixed inset-0 z-[var(--z-index-overlay)] flex items-end justify-center bg-black/40" @click.self="closeDeleteDialog">
          <section class="delete-sheet-panel w-full max-w-[var(--app-max-width)] rounded-t-[26px] bg-white px-5 pt-3 pb-[calc(24px+env(safe-area-inset-bottom))]" role="alertdialog" aria-modal="true" aria-labelledby="delete-capsule-title" aria-describedby="delete-capsule-description">
            <span class="mx-auto block h-1 w-10 rounded-full bg-[#d7dfe4]"></span>
            <div class="mt-5 flex items-start gap-3.5">
              <span class="grid size-11 shrink-0 place-items-center rounded-full bg-[#fff0f1] text-[#e2535a]"><AlertTriangle :size="22" /></span>
              <div class="min-w-0 flex-1 pt-0.5">
                <h2 id="delete-capsule-title" class="m-0 text-[19px] font-bold">타임캡슐을 삭제할까요?</h2>
                <p id="delete-capsule-description" class="mt-1.5 mb-0 text-xs leading-relaxed text-[var(--color-text-secondary)]">삭제한 타임캡슐은 다시 복구할 수 없어요.</p>
              </div>
              <button class="grid size-9 shrink-0 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[#f2f5f7] disabled:opacity-40" type="button" aria-label="삭제 확인창 닫기" :disabled="isDeleting" @click="closeDeleteDialog"><X :size="20" /></button>
            </div>

            <div class="mt-5 rounded-2xl bg-[#f7f9fa] px-4 py-3.5">
              <strong class="mt-1 block truncate text-sm">{{ initialData.record.title }}</strong>
              <span class="mt-1 block text-[11px] text-[var(--color-text-secondary)]">{{ initialData.record.date.replaceAll('-', '.') }} · 사진 {{ mediaItems.length }}장</span>
            </div>

            <div class="mt-5 grid grid-cols-2 gap-3">
              <button class="h-[52px] rounded-xl border border-[var(--color-border)] bg-white text-sm font-bold text-[var(--color-text-secondary)] active:bg-[#f5f7f8] disabled:opacity-50" type="button" :disabled="isDeleting" @click="closeDeleteDialog">취소</button>
              <button class="flex h-[52px] items-center justify-center gap-1.5 rounded-xl border-0 bg-[#e85b61] text-sm font-bold text-white active:bg-[#cf484e] disabled:opacity-55" type="button" :disabled="isDeleting" @click="deleteRecord">
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
