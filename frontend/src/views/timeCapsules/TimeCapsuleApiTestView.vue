<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  completeMediaUpload,
  createMediaUploadUrls,
  deleteTimeCapsuleEntry,
  getTimeCapsule,
  getTimeCapsuleEntries,
  getTimeCapsuleEntry,
  getTimeCapsules,
  sealTimeCapsuleEntry,
  type TimeCapsule,
  type TimeCapsuleEntry,
  type TimeCapsuleEntrySummary,
  type TimeCapsuleSummary,
  updateTimeCapsuleEntry,
  uploadMediaFile,
} from '@/api/timeCapsules'

const childId = Number(import.meta.env.VITE_TIME_CAPSULE_CHILD_ID)
const capsules = ref<TimeCapsuleSummary[]>([])
const selectedCapsule = ref<TimeCapsule | null>(null)
const entries = ref<TimeCapsuleEntrySummary[]>([])
const selectedEntry = ref<TimeCapsuleEntry | null>(null)
const isLoading = ref(false)
const errorMessage = ref('')
const draftEntryId = ref('')
const deleteEntryId = ref('')
const testFile = ref<File | null>(null)
const isMutating = ref(false)
const mutationMessage = ref('')
const mutationErrorMessage = ref('')

const hasTestChildId = computed(() => Number.isSafeInteger(childId) && childId > 0)
const formatDate = (value: string | null) => value?.replace('T', ' ').slice(0, 19) ?? '-'
const formatAmount = (value: number) => Number(value).toLocaleString('ko-KR')

const getEntryIdOrThrow = (value: string) => {
  const entryId = Number(value)
  if (!Number.isSafeInteger(entryId) || entryId <= 0) {
    throw new Error('DRAFT entry ID is required.')
  }

  return entryId
}

const refreshEntry = async (entryId: number) => {
  selectedEntry.value = await getTimeCapsuleEntry(entryId)
}

const runMutation = async (testCase: string, action: () => Promise<void>) => {
  isMutating.value = true
  mutationMessage.value = ''
  mutationErrorMessage.value = ''

  try {
    await action()
    mutationMessage.value = `${testCase} succeeded.`
  } catch (error) {
    mutationErrorMessage.value = error instanceof Error
      ? `${testCase} failed: ${error.message}`
      : `${testCase} failed. Check the Network response.`
  } finally {
    isMutating.value = false
  }
}

const runTc2 = async () => {
  if (!hasTestChildId.value) {
    errorMessage.value = 'VITE_TIME_CAPSULE_CHILD_ID를 .env.local에 설정해 주세요.'
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  selectedCapsule.value = null
  selectedEntry.value = null
  entries.value = []

  try {
    capsules.value = (await getTimeCapsules(childId, { view: 'CARD', size: 50 })).items
  } catch {
    errorMessage.value = 'TC-2 요청에 실패했습니다. 임시 토큰과 Tomcat 실행 상태를 확인해 주세요.'
  } finally {
    isLoading.value = false
  }
}

const runTc3AndTc4 = async (timeCapsuleId: number) => {
  isLoading.value = true
  errorMessage.value = ''
  selectedEntry.value = null

  try {
    const [capsule, entryResponse] = await Promise.all([
      getTimeCapsule(timeCapsuleId),
      getTimeCapsuleEntries(timeCapsuleId),
    ])

    selectedCapsule.value = capsule
    entries.value = entryResponse.entries
  } catch {
    errorMessage.value = 'TC-3 또는 TC-4 요청에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

const runTc14 = async (entryId: number) => {
  isLoading.value = true
  errorMessage.value = ''

  try {
    selectedEntry.value = await getTimeCapsuleEntry(entryId)
  } catch {
    errorMessage.value = 'TC-14 요청에 실패했습니다.'
  } finally {
    isLoading.value = false
  }
}

const runTc14FromDraft = () => {
  const entryId = Number(draftEntryId.value)
  if (Number.isSafeInteger(entryId) && entryId > 0) {
    void runTc14(entryId)
  }
}

const runTc12 = () => runMutation('TC-12', async () => {
  const entryId = getEntryIdOrThrow(draftEntryId.value)
  await updateTimeCapsuleEntry(entryId, {
    title: '[API TEST] updated entry',
    message: 'Updated once through the local frontend API test page.',
  })
  await refreshEntry(entryId)
})

const onTestFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  testFile.value = input.files?.[0] ?? null
}

const runTc7ToTc8 = () => runMutation('TC-7 → S3 PUT → TC-8', async () => {
  const entryId = getEntryIdOrThrow(draftEntryId.value)
  const file = testFile.value
  if (!file) {
    throw new Error('Select an image file first.')
  }
  if (!file.type.startsWith('image/')) {
    throw new Error('Select an image file for this test.')
  }

  const uploadResponse = await createMediaUploadUrls(entryId, [{
    mime_type: file.type,
    file_size: file.size,
    slot_no: 1,
  }])
  const upload = uploadResponse.uploads[0]
  if (!upload) {
    throw new Error('The upload URL response is empty.')
  }

  await uploadMediaFile(upload, file)
  await completeMediaUpload(entryId, [upload.time_capsule_media_id])
  await refreshEntry(entryId)
})

const runTc15 = () => runMutation('TC-15', async () => {
  const entryId = getEntryIdOrThrow(draftEntryId.value)
  await sealTimeCapsuleEntry(entryId)
  await refreshEntry(entryId)
})

const runTc13 = () => runMutation('TC-13', async () => {
  const entryId = getEntryIdOrThrow(deleteEntryId.value)
  if (!window.confirm(`Delete DRAFT entry ${entryId}? This cannot be undone.`)) {
    throw new Error('Cancelled by user.')
  }

  await deleteTimeCapsuleEntry(entryId)
})
</script>

<template>
  <main class="mx-auto min-h-screen max-w-3xl bg-slate-50 px-5 py-10 text-slate-900">
    <header>
      <p class="text-sm font-bold text-sky-600">LOCAL API TEST ONLY</p>
      <h1 class="mt-1 text-2xl font-bold">타임캡슐 API 통신 확인</h1>
      <p class="mt-2 text-sm text-slate-600">
        기존 타임캡슐 화면과 더미 데이터는 변경하지 않습니다. 이 페이지는 읽기 전용 API만 호출합니다.
      </p>
    </header>

    <section class="mt-7 rounded-2xl bg-white p-5 shadow-sm">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 class="font-bold">TC-2 · 타임캡슐 목록</h2>
          <p class="mt-1 text-sm text-slate-500">테스트 child_id: {{ hasTestChildId ? childId : '미설정' }}</p>
        </div>
        <button class="rounded-lg bg-sky-500 px-4 py-2 text-sm font-bold text-white disabled:bg-slate-300" :disabled="isLoading" @click="runTc2">
          TC-2 실행
        </button>
      </div>

      <p v-if="!capsules.length" class="mt-4 text-sm text-slate-500">목록 조회 전입니다.</p>
      <div v-else class="mt-4 space-y-2">
        <button
          v-for="capsule in capsules"
          :key="capsule.time_capsule_id"
          class="flex w-full items-center justify-between rounded-xl border border-slate-200 px-4 py-3 text-left hover:bg-slate-50"
          :disabled="isLoading"
          @click="runTc3AndTc4(capsule.time_capsule_id)"
        >
          <span>
            <strong class="block">{{ capsule.title }}</strong>
            <span class="mt-1 block text-xs text-slate-500">capsule_id: {{ capsule.time_capsule_id }} · 기록 {{ capsule.entry_count }}개</span>
          </span>
          <span class="text-sm font-bold text-sky-600">TC-3 · TC-4</span>
        </button>
      </div>
    </section>

    <section v-if="selectedCapsule" class="mt-5 rounded-2xl bg-white p-5 shadow-sm">
      <h2 class="font-bold">TC-3 · 타임캡슐 상세</h2>
      <dl class="mt-3 grid grid-cols-2 gap-3 text-sm">
        <div><dt class="text-slate-500">상태</dt><dd class="font-medium">{{ selectedCapsule.status }}</dd></div>
        <div><dt class="text-slate-500">공개 예정</dt><dd class="font-medium">{{ formatDate(selectedCapsule.expected_release_at) }}</dd></div>
        <div><dt class="text-slate-500">금융 계좌 ID</dt><dd class="font-medium">{{ selectedCapsule.financial_account_id }}</dd></div>
        <div><dt class="text-slate-500">기록 수</dt><dd class="font-medium">{{ selectedCapsule.entry_count }}</dd></div>
      </dl>
    </section>

    <section v-if="selectedCapsule" class="mt-5 rounded-2xl bg-white p-5 shadow-sm">
      <h2 class="font-bold">TC-4 · 타임캡슐 기록 목록</h2>
      <p v-if="!entries.length" class="mt-3 text-sm text-slate-500">기록이 없습니다.</p>
      <div v-else class="mt-3 space-y-2">
        <button
          v-for="entry in entries"
          :key="entry.time_capsule_entry_id"
          class="flex w-full items-center justify-between rounded-xl border border-slate-200 px-4 py-3 text-left hover:bg-slate-50"
          :disabled="isLoading"
          @click="runTc14(entry.time_capsule_entry_id)"
        >
          <span>
            <strong class="block">{{ entry.title }}</strong>
            <span class="mt-1 block text-xs text-slate-500">entry_id: {{ entry.time_capsule_entry_id }} · {{ formatDate(entry.contributed_at) }}</span>
          </span>
          <span class="text-sm font-bold text-sky-600">{{ formatAmount(entry.contribution_amount) }}원 · TC-14</span>
        </button>
      </div>
    </section>

    <section v-if="selectedEntry" class="mt-5 rounded-2xl bg-white p-5 shadow-sm">
      <h2 class="font-bold">TC-14 · 타임캡슐 기록 상세</h2>
      <dl class="mt-3 grid gap-3 text-sm">
        <div><dt class="text-slate-500">제목</dt><dd class="font-medium">{{ selectedEntry.title }}</dd></div>
        <div><dt class="text-slate-500">상태 / 수정 횟수</dt><dd class="font-medium">{{ selectedEntry.status }} / {{ selectedEntry.edit_count }}</dd></div>
        <div><dt class="text-slate-500">편지</dt><dd class="mt-1 whitespace-pre-wrap">{{ selectedEntry.message ?? '(아직 작성되지 않음)' }}</dd></div>
        <div><dt class="text-slate-500">미디어</dt><dd class="font-medium">{{ selectedEntry.media.length }}개</dd></div>
      </dl>
    </section>

    <p v-if="isLoading" class="mt-5 text-sm font-bold text-sky-600">요청 중...</p>
    <section class="mt-5 rounded-2xl border border-amber-200 bg-amber-50 p-5 shadow-sm">
      <p class="text-sm font-bold text-amber-700">Data-changing test area</p>
      <h2 class="mt-1 font-bold">TC-12 · TC-7 · TC-8 · TC-15</h2>
      <p class="mt-2 text-sm text-amber-800">
        Use only one dedicated DRAFT entry here. TC-12 can be run once, and a sealed entry cannot be edited or deleted.
      </p>

      <label class="mt-4 block text-sm font-medium">
        DRAFT entry ID
        <input
          v-model.trim="draftEntryId"
          type="number"
          min="1"
          class="mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
          placeholder="Dedicated DRAFT entry ID"
        >
      </label>

      <div class="mt-3 flex flex-wrap gap-2">
        <button class="rounded-lg bg-sky-600 px-4 py-2 text-sm font-bold text-white disabled:bg-slate-300" :disabled="isMutating" @click="runTc12">
          TC-12 update
        </button>
        <button class="rounded-lg bg-slate-700 px-4 py-2 text-sm font-bold text-white disabled:bg-slate-300" :disabled="isMutating || !draftEntryId" @click="runTc14FromDraft">
          TC-14 refresh
        </button>
      </div>

      <label class="mt-5 block text-sm font-medium">
        One image to upload
        <input class="mt-1 block text-sm" type="file" accept="image/png,image/jpeg,image/webp" @change="onTestFileChange">
      </label>
      <button class="mt-3 rounded-lg bg-violet-600 px-4 py-2 text-sm font-bold text-white disabled:bg-slate-300" :disabled="isMutating || !draftEntryId || !testFile" @click="runTc7ToTc8">
        TC-7 → S3 PUT → TC-8
      </button>
      <button class="ml-2 mt-3 rounded-lg bg-emerald-600 px-4 py-2 text-sm font-bold text-white disabled:bg-slate-300" :disabled="isMutating || !draftEntryId" @click="runTc15">
        TC-15 seal
      </button>
    </section>

    <section class="mt-5 rounded-2xl border border-red-200 bg-red-50 p-5 shadow-sm">
      <p class="text-sm font-bold text-red-700">Delete test area</p>
      <h2 class="mt-1 font-bold">TC-13</h2>
      <p class="mt-2 text-sm text-red-800">Enter a separate DRAFT entry ID. Do not use the entry that you sealed above.</p>
      <label class="mt-4 block text-sm font-medium">
        DRAFT entry ID for deletion
        <input
          v-model.trim="deleteEntryId"
          type="number"
          min="1"
          class="mt-1 w-full rounded-lg border border-slate-300 bg-white px-3 py-2"
          placeholder="Separate DRAFT entry ID"
        >
      </label>
      <button class="mt-3 rounded-lg bg-red-600 px-4 py-2 text-sm font-bold text-white disabled:bg-red-300" :disabled="isMutating || !deleteEntryId" @click="runTc13">
        TC-13 delete
      </button>
    </section>

    <p v-if="mutationMessage" class="mt-5 rounded-xl bg-emerald-50 p-4 text-sm text-emerald-700">{{ mutationMessage }}</p>
    <p v-if="mutationErrorMessage" class="mt-5 rounded-xl bg-red-50 p-4 text-sm text-red-700">{{ mutationErrorMessage }}</p>
    <p v-if="errorMessage" class="mt-5 rounded-xl bg-red-50 p-4 text-sm text-red-700">{{ errorMessage }}</p>
  </main>
</template>
