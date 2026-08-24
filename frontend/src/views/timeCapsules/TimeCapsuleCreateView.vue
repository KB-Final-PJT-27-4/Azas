<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import axios from 'axios'
import { useRoute, useRouter } from 'vue-router'
import { Check, ChevronDown, ImagePlus, Landmark, X } from 'lucide-vue-next'
import { useToast } from '@/composables/useToast'
import { api, getApiErrorMessage } from '@/api'
import { resolveCurrentChildId } from '@/api/context'

const router = useRouter()
const route = useRoute()
const { showToast } = useToast()
const step = ref<'form' | 'preview'>('form')
const slideDirection = ref<'forward' | 'backward'>('forward')
const isPageLeaving = ref(false)
const isAccountMenuOpen = ref(false)
const isTransferMenuOpen = ref(false)

const accounts = ref<Array<{ id: number; accountId: number; bank: string; name: string; number: string }>>([])
const transfers = ref<Array<{ id: number; date: string; name: string; amount: number }>>([])

const requestedTimeCapsuleId = Number(route.query.account)
const requestedFinancialAccountId = Number(route.query.account_id)
const shouldSelectLatestTransfer = route.query.transfer === 'latest'
const selectedAccountId = ref(requestedTimeCapsuleId || 0)
const selectedTransferId = ref(0)
const selectedAccount = computed(() => accounts.value.find(({ id }) => id === selectedAccountId.value) ?? { id: 0, accountId: 0, bank: '', name: '계좌를 선택해주세요', number: '' })
const selectedTransfer = computed(() => transfers.value.find(({ id }) => id === selectedTransferId.value) ?? { id: 0, date: '', name: '거래를 선택해주세요', amount: 0 })
const title = ref('')
const letter = ref('')
const childName = ref('아이')
type MediaItem = {
  file: File
  url: string
  type: 'image'
  orientation: 'landscape' | 'portrait'
}

const mediaItems = ref<MediaItem[]>([])
const hasCreated = ref(false)

const canPreview = computed(() =>
  selectedAccountId.value > 0
    && selectedTransferId.value > 0
    && Boolean(title.value.trim())
    && Boolean(letter.value.trim()),
)
const formattedAmount = computed(() => `${(selectedTransfer.value?.amount ?? 0).toLocaleString('ko-KR')}원`)
const formatAccountDetails = (account: { bank: string; number: string }) =>
  [account.bank, account.number].filter(Boolean).join(' · ')

const selectAccount = (id: number) => {
  selectedAccountId.value = id
  isAccountMenuOpen.value = false
}

const selectTransfer = (id: number) => {
  selectedTransferId.value = id
  isTransferMenuOpen.value = false
}

const toggleAccountMenu = () => {
  isAccountMenuOpen.value = !isAccountMenuOpen.value
  isTransferMenuOpen.value = false
}

const toggleTransferMenu = () => {
  isTransferMenuOpen.value = !isTransferMenuOpen.value
  isAccountMenuOpen.value = false
}

const closeSelectMenus = () => {
  isAccountMenuOpen.value = false
  isTransferMenuOpen.value = false
}

const selectMedia = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    showToast('사진 파일만 선택할 수 있어요.', 'error')
    input.value = ''
    return
  }
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type) || file.size > 10 * 1024 * 1024) {
    showToast('10MB 이하의 JPG, PNG, WEBP 사진을 선택해 주세요.', 'error')
    input.value = ''
    return
  }

  mediaItems.value.forEach(({ url }) => URL.revokeObjectURL(url))
  const item: MediaItem = {
    file,
    url: URL.createObjectURL(file),
    type: 'image',
    orientation: 'portrait',
  }
  mediaItems.value = [item]
  detectMediaOrientation(item)
  input.value = ''
}

const detectMediaOrientation = (item: MediaItem) => {
  const updateOrientation = (width: number, height: number) => {
    const target = mediaItems.value.find(({ url }) => url === item.url)
    if (target) target.orientation = width > height ? 'landscape' : 'portrait'
  }

  const image = new Image()
  image.onload = () => updateOrientation(image.naturalWidth, image.naturalHeight)
  image.src = item.url
}

const removeMedia = (index: number) => {
  URL.revokeObjectURL(mediaItems.value[index]!.url)
  mediaItems.value.splice(index, 1)
}

const showPreview = () => {
  if (!canPreview.value) return
  slideDirection.value = 'forward'
  step.value = 'preview'
}

const showForm = () => {
  slideDirection.value = 'backward'
  step.value = 'form'
}

const createTimeCapsule = async () => {
  if (isPageLeaving.value) return
  if (!canPreview.value) {
    showToast('계좌·입금 거래와 필수 내용을 모두 입력해 주세요.', 'error')
    return
  }

  try {
    const { data } = await api.createTimeCapsuleEntryUsingPOST(selectedAccountId.value, {
      account_transaction_id: selectedTransferId.value,
      title: title.value.trim(),
      message: letter.value.trim(),
    })
    const entryId = data.time_capsule_entry_id
    const media = mediaItems.value[0]
    if (!entryId) throw new Error('타임캡슐 기록 정보를 받지 못했습니다.')
    if (media) {
      const { data: upload } = await api.createMediaUploadUrlUsingPOST(entryId, {
        file_size: media.file.size,
        mime_type: media.file.type,
      })
      if (!upload.upload_url || !upload.time_capsule_media_id) {
        throw new Error('사진 업로드 정보를 받지 못했습니다.')
      }
      await axios.put(upload.upload_url, media.file, { headers: upload.required_headers })
      await api.completeMediaUploadUsingPOST(entryId, { time_capsule_media_id: upload.time_capsule_media_id })
    }
    await api.sealTimeCapsuleEntryUsingPATCH(entryId)
    hasCreated.value = true
    isPageLeaving.value = true
    await router.push({
      name: 'TimeCapsuleList',
      params: { capsuleListId: String(selectedAccountId.value) },
    })
    showToast('저장되었습니다.', 'success', 2200, 'above-actions')
  } catch (error) {
    hasCreated.value = false
    isPageLeaving.value = false
    showToast(getApiErrorMessage(error, '캡슐 저장에 실패했습니다.'), 'error')
  }
}

const loadTransfers = async () => {
  const account = selectedAccount.value
  if (!account) return
  const { data } = await api.getTransactionsUsingGET(account.accountId, undefined, undefined, 50)
  transfers.value = data.transactions
    .filter((transaction) => transaction.direction === 'CREDIT' && transaction.amount > 0)
    .map((transaction) => ({
    id: transaction.account_transaction_id,
    date: new Date(transaction.occurred_at).toLocaleDateString('ko-KR'),
    name: transaction.counterparty_name ?? '계좌 거래',
    amount: transaction.amount,
    }))
  if (shouldSelectLatestTransfer || !transfers.value.some(({ id }) => id === selectedTransferId.value)) {
    selectedTransferId.value = transfers.value[0]?.id ?? 0
  }
}

watch(selectedAccountId, () => void loadTransfers())

onMounted(async () => {
  try {
    const childId = await resolveCurrentChildId()
    const [{ data: capsules }, { data: childAccounts }, { data: parentAccounts }, { data: child }] = await Promise.all([
      api.getTimeCapsulesUsingGET(childId),
      api.getChildAccountsUsingGET(childId),
      api.getMyAccountsUsingGET(),
      api.getChildUsingGET(childId),
    ])
    childName.value = child.name?.trim() || '아이'
    const linkedAccounts = [...childAccounts.accounts, ...parentAccounts.accounts]
    accounts.value = (capsules.time_capsules ?? []).map((capsule) => {
      const linked = linkedAccounts.find(({ account_id }) => account_id === capsule.account_id)
      return {
        id: capsule.time_capsule_id ?? 0,
        accountId: capsule.account_id ?? 0,
        bank: linked ? 'KB국민은행' : '',
        name: capsule.title ?? linked?.account_name ?? '타임캡슐',
        number: linked?.account_number ?? '',
      }
    })
    if (requestedFinancialAccountId > 0) {
      selectedAccountId.value = accounts.value.find(({ accountId }) => accountId === requestedFinancialAccountId)?.id ?? 0
    }
    if (!accounts.value.some(({ id }) => id === selectedAccountId.value)) selectedAccountId.value = accounts.value[0]?.id ?? 0
    await loadTransfers()
  } catch (error) {
    showToast(getApiErrorMessage(error, '타임캡슐 정보를 불러오지 못했습니다.'), 'error')
  }
})

onBeforeUnmount(() => {
  if (hasCreated.value) return
  mediaItems.value.forEach(({ url }) => URL.revokeObjectURL(url))
})
</script>

<template>
  <main
    class="flex min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] flex-col bg-white"
    :class="isPageLeaving ? 'capsule-page-leaving pointer-events-none' : ''"
  >
    <Transition :name="`capsule-slide-${slideDirection}`" mode="out-in">
    <section v-if="step === 'form'" key="form" class="flex flex-1 flex-col px-5 pt-5 pb-[calc(24px+env(safe-area-inset-bottom))]">
      <h1 class="text-[24px] leading-tight font-extrabold tracking-[-0.035em] text-[var(--color-text-primary)]">
        오늘 어떤 순간을 기록할까요?
      </h1>
      <p class="mt-2 text-[13px] leading-5 text-[var(--color-text-secondary)]">
        최근 저축 내역과 사진, 마음의 편지를 함께 남겨요.
      </p>

      <form class="mt-6 flex flex-1 flex-col gap-5" @submit.prevent="showPreview">
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
              <strong class="block truncate text-sm">{{ selectedAccount.name }}</strong>
              <span
                v-if="formatAccountDetails(selectedAccount)"
                class="mt-0.5 block text-[11px] text-[var(--color-text-secondary)]"
              >
                {{ formatAccountDetails(selectedAccount) }}
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
              <li v-for="item in accounts" :key="item.id" role="option" :aria-selected="item.id === selectedAccountId">
                <button
                  class="flex min-h-[62px] w-full items-center gap-3 rounded-2xl px-4 py-2.5 text-left transition-colors"
                  :class="item.id === selectedAccountId ? 'bg-[#f0faff]' : 'active:bg-[#f5f8fa]'"
                  type="button"
                  @click="selectAccount(item.id)"
                >
                  <span class="min-w-0 flex-1">
                    <strong class="block truncate text-[13px]">{{ item.name }}</strong>
                    <span
                      v-if="formatAccountDetails(item)"
                      class="mt-0.5 block truncate text-[11px] text-[var(--color-text-secondary)]"
                    >
                      {{ formatAccountDetails(item) }}
                    </span>
                  </span>
                  <span
                    class="grid size-6 shrink-0 place-items-center rounded-full border"
                    :class="
                      item.id === selectedAccountId
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
            <span class="text-xs text-[var(--color-text-secondary)]">{{ selectedTransfer.date }}</span>
            <strong class="min-w-0 flex-1 truncate text-sm">{{ selectedTransfer.name }}</strong>
            <strong class="text-xs text-[var(--color-selected-text)]">+{{ formattedAmount }}</strong>
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
              <li v-for="item in transfers" :key="item.id" role="option" :aria-selected="item.id === selectedTransferId">
                <button
                  class="flex min-h-[58px] w-full items-center gap-3 rounded-2xl px-4 py-2.5 text-left transition-colors"
                  :class="item.id === selectedTransferId ? 'bg-[#f0faff]' : 'active:bg-[#f5f8fa]'"
                  type="button"
                  @click="selectTransfer(item.id)"
                >
                  <span class="w-[74px] text-[11px] text-[var(--color-text-secondary)]">{{ item.date }}</span>
                  <strong class="min-w-0 flex-1 truncate text-[13px]">{{ item.name }}</strong>
                  <strong class="shrink-0 text-xs text-[var(--color-selected-text)]">+{{ item.amount.toLocaleString('ko-KR') }}원</strong>

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
            :placeholder="`${childName}의 첫걸음을 기록해요.\n앞으로도 건강하게 자라길 바라`"
            required
          ></textarea>
        </label>

        <div>
          <div class="mb-3 flex items-center justify-between">
            <span class="text-sm font-bold">대표 사진 <em class="text-xs font-semibold not-italic text-[var(--color-text-secondary)]">(선택)</em></span>
          </div>
          <div v-if="mediaItems[0]" class="relative overflow-hidden rounded-[20px] border border-[#d6e3e9] bg-[#f5f8fa]">
            <img :src="mediaItems[0].url" alt="선택한 대표 사진" class="aspect-[16/10] w-full bg-[#f4f7f9] object-contain" />
            <div class="absolute inset-x-0 bottom-0 flex items-center justify-end gap-2 bg-gradient-to-t from-black/35 via-black/10 to-transparent px-3 pt-16 pb-3">
              <label class="cursor-pointer rounded-lg bg-white/95 px-3 py-2 text-[11px] font-bold text-[var(--color-text-primary)] shadow-sm active:bg-white">
                사진 바꾸기
                <input class="sr-only" type="file" accept="image/*" @change="selectMedia" />
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
            <span class="mt-3 text-xs font-semibold">대표 사진을 추가할 수 있어요</span>
            <span class="mt-1 text-[10px] text-[#98a5ad]">사진은 나중에 다시 변경할 수 있어요.</span>
            <input class="sr-only" type="file" accept="image/*" @change="selectMedia" />
          </label>
        </div>

        <div class="mt-auto grid grid-cols-2 gap-3 pt-2">
          <button
            class="h-14 rounded-2xl border border-[#d6e3e9] bg-white text-sm font-bold text-[var(--color-text-secondary)] active:bg-[#f5f7f8]"
            type="button"
            @click="router.push('/time-capsules')"
          >
            취소
          </button>
          <button
            class="h-14 rounded-2xl bg-[var(--color-brand-primary)] text-sm font-bold text-white active:bg-[var(--color-brand-primary-pressed)] disabled:cursor-not-allowed disabled:bg-[#c9d5dc]"
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
      key="preview"
      class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-white px-5 pt-5 pb-[calc(24px+env(safe-area-inset-bottom))]"
    >
      <div class="flex min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height)-40px)] flex-col">
        <h1 class="text-[24px] font-extrabold tracking-[-0.035em] text-[var(--color-text-primary)]">타임캡슐 미리보기</h1>
        <section
          class="mt-5 flex min-h-[72px] items-center justify-between gap-4 rounded-[20px] bg-[#eefaff] px-4"
          aria-label="연결된 저축 정보"
        >
          <div class="min-w-0">
            <strong class="mt-1 block truncate text-[15px] text-[var(--color-text-primary)]">
              {{ selectedTransfer.name }}
            </strong>
          </div>
          <div class="shrink-0 text-right">
            <strong class="block text-[16px] font-extrabold text-[var(--color-selected-text)]">
              +{{ formattedAmount }}
            </strong>
            <time class="mt-1 block text-[11px] text-[var(--color-text-secondary)]">
              {{ selectedTransfer.date }}
            </time>
          </div>
        </section>

        <article class="mt-4 overflow-hidden rounded-[24px] border border-[#d6e3e9] bg-white">
          <div v-if="mediaItems[0]" class="bg-[#f5f8fa] p-3">
            <img
              :src="mediaItems[0].url"
              alt="타임캡슐 대표 사진"
              class="max-h-[300px] w-full rounded-[18px] bg-white object-contain"
            />
          </div>

          <div class="p-5">
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0 flex-1">
                <span class="text-[13px] font-bold text-[var(--color-brand-primary)]">오늘의 기록</span>
                <h2 class="mt-2 break-words text-[20px] leading-snug font-extrabold tracking-[-0.025em]">
                  {{ title }}
                </h2>
              </div>
              <time
                class="shrink-0 rounded-full bg-[#f3f6f7] px-2.5 py-1.5 text-[10px] font-medium text-[var(--color-text-secondary)]"
              >
                {{ selectedTransfer.date }}
              </time>
            </div>

            <div class="my-4 h-px bg-[#edf1f3]"></div>
            <p class="whitespace-pre-wrap break-words text-[14px] leading-7 text-[var(--color-text-primary)]">
              {{ letter }}
            </p>
          </div>
        </article>

        <div class="mt-auto grid grid-cols-2 gap-3 pt-5">
          <button
            class="h-14 rounded-2xl border border-[#d6e3e9] bg-white text-sm font-bold text-[var(--color-text-secondary)] active:bg-[#f5f7f8]"
            type="button"
            @click="showForm"
          >
            수정하기
          </button>
          <button
            class="h-14 rounded-2xl bg-[var(--color-brand-primary)] text-sm font-bold text-white active:bg-[var(--color-brand-primary-pressed)]"
            type="button"
            :disabled="isPageLeaving"
            @click="createTimeCapsule"
          >
            생성하기
          </button>
        </div>
      </div>
    </section>
    </Transition>
  </main>
</template>

<style scoped>
.capsule-page-leaving {
  transform: translateX(-18px);
  opacity: 0;
  transition:
    transform 150ms cubic-bezier(0.25, 0.8, 0.25, 1),
    opacity 120ms ease-out;
}

.capsule-slide-forward-enter-active,
.capsule-slide-forward-leave-active,
.capsule-slide-backward-enter-active,
.capsule-slide-backward-leave-active {
  transition:
    transform 150ms cubic-bezier(0.25, 0.8, 0.25, 1),
    opacity 120ms ease-out;
}

.capsule-slide-forward-enter-from,
.capsule-slide-backward-leave-to {
  transform: translateX(18px);
  opacity: 0;
}

.capsule-slide-forward-leave-to,
.capsule-slide-backward-enter-from {
  transform: translateX(-18px);
  opacity: 0;
}

@media (prefers-reduced-motion: reduce) {
  .capsule-page-leaving,
  .capsule-slide-forward-enter-active,
  .capsule-slide-forward-leave-active,
  .capsule-slide-backward-enter-active,
  .capsule-slide-backward-leave-active {
    transition-duration: 1ms;
  }
}
</style>
