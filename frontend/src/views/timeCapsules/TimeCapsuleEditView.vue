<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChevronDown, ImagePlus, PiggyBank, X } from 'lucide-vue-next'
import {
  findTimeCapsuleRecord,
  timeCapsuleAccounts,
  type TimeCapsulePhoto,
} from '@/data/timeCapsuleDummyData'

type EditMedia = TimeCapsulePhoto & {
  isNew?: boolean
}

const route = useRoute()
const router = useRouter()
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
const canSave = computed(() => Boolean(title.value.trim() && letter.value.trim() && selectedTransfer.value))
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

const saveEdit = () => {
  if (!canSave.value) return

  const sourceAccount = initialData.account
  const targetAccount = selectedAccount.value
  const sourceIndex = sourceAccount.records.findIndex(({ id }) => id === initialData.record.id)
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
  router.replace(`/time-capsules/${targetAccount.id}/${updatedRecord.id}`)
}

onBeforeUnmount(() => {
  if (hasSaved.value) return
  mediaItems.value.filter(({ isNew }) => isNew).forEach(({ src }) => URL.revokeObjectURL(src))
})
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-white"
  >
    <section class="px-5 py-5">
      <h1 class="text-[23px] leading-tight font-bold tracking-[-0.025em] text-[var(--color-text-primary)]">
        오늘 어떤 순간을 기록할까요?
      </h1>
      <p class="mt-1.5 text-xs text-[var(--color-text-secondary)]">
        최근 저축 내역과 사진, 마음의 편지를 함께 남겨요.
      </p>

    <form class="mt-7 space-y-5" @submit.prevent="saveEdit">
      <div class="relative">
        <button
          class="flex min-h-14 w-full items-center rounded-xl border border-[var(--color-border)] bg-white px-4 text-left"
          type="button"
          aria-haspopup="listbox"
          :aria-expanded="isAccountMenuOpen"
          @click="toggleAccountMenu"
        >
          <span
            class="mr-3 grid size-8 shrink-0 place-items-center rounded-full bg-[#fff7dc] text-[#f5a300]"
            aria-hidden="true"
          >
            <PiggyBank :size="18" />
          </span>
          <span class="min-w-0 flex-1">
            <strong class="block truncate text-sm">
              {{ accountDisplayName(selectedAccount.bankName, selectedAccount.name) }}
            </strong>
            <span class="mt-0.5 block text-[11px] text-[var(--color-text-secondary)]">
              {{ selectedAccount.bankName }} · {{ selectedAccount.accountNumber }}
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
            v-for="account in accounts"
            :key="account.id"
            class="flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left active:bg-[var(--color-selected-background)]"
            :class="String(account.id) === selectedAccountId ? 'bg-[var(--color-selected-background)]' : ''"
            type="button"
            role="option"
            :aria-selected="String(account.id) === selectedAccountId"
            @click="selectAccount(account.id)"
          >
            <span class="grid size-8 shrink-0 place-items-center rounded-full bg-[#fff7dc] text-[#f5a300]">
              <PiggyBank :size="17" />
            </span>
            <span class="min-w-0">
              <strong class="block truncate text-sm">
                {{ accountDisplayName(account.bankName, account.name) }}
              </strong>
              <span class="text-[11px] text-[var(--color-text-secondary)]">
                {{ account.bankName }} · {{ account.accountNumber }}
              </span>
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
          @click="toggleTransferMenu"
        >
          <span class="text-xs text-[var(--color-text-secondary)]">{{ selectedTransfer.date.replaceAll('-', '.') }}</span>
          <strong class="min-w-0 flex-1 truncate text-sm">{{ selectedTransfer.transferName }}</strong>
          <strong class="text-xs text-[var(--color-selected-text)]">
            +{{ selectedTransfer.amount.toLocaleString('ko-KR') }}원
          </strong>
          <ChevronDown :size="17" class="shrink-0 text-[var(--color-text-secondary)]" />
        </button>

        <div
          v-if="isTransferMenuOpen"
          class="absolute top-[calc(100%+6px)] right-0 left-0 z-20 max-h-64 overflow-y-auto rounded-xl border border-[var(--color-border)] bg-white p-1.5 shadow-lg"
          role="listbox"
          aria-label="이체 내역 목록"
        >
          <button
            v-for="transfer in transferOptions"
            :key="transfer.id"
            class="flex w-full items-center gap-3 rounded-lg px-3 py-3 text-left active:bg-[var(--color-selected-background)]"
            :class="transfer.id === selectedTransferId ? 'bg-[var(--color-selected-background)]' : ''"
            type="button"
            role="option"
            :aria-selected="transfer.id === selectedTransferId"
            @click="selectTransfer(transfer.id)"
          >
            <span class="w-[74px] text-[11px] text-[var(--color-text-secondary)]">
              {{ transfer.date.replaceAll('-', '.') }}
            </span>
            <strong class="min-w-0 flex-1 truncate text-sm">{{ transfer.transferName }}</strong>
            <strong class="text-xs text-[var(--color-selected-text)]">
              +{{ transfer.amount.toLocaleString('ko-KR') }}원
            </strong>
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

      <div class="grid grid-cols-2 gap-3 pt-2">
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
