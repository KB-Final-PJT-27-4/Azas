<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import childProfileImage from '@/assets/images/child/baby.png'
import { useToast } from '@/composables/useToast'
import { api, getApiErrorMessage } from '@/api'

type PermissionItem = {
  id: string
  title: string
  description: string
  enabled: boolean
}

type ChildProfile = {
  id: number
  name: string
  age: number
  schoolLevel: string
}

const { showToast } = useToast()
const isLoading = ref(true)
const children = ref<ChildProfile[]>([])
const selectedChildId = ref(0)
const selectedAccountId = ref<number | null>(null)
const isChildSheetOpen = ref(false)
const sheetTouchStartY = ref<number | null>(null)
const sheetDragOffset = ref(0)
const isSheetDragging = ref(false)
const selectedChild = computed(() =>
  children.value.find(({ id }) => id === selectedChildId.value) ?? children.value[0] ?? { id: 0, name: '아이', age: 0, schoolLevel: '' },
)
const permissions = ref<PermissionItem[]>([
  {
    id: 'allowance',
    title: '용돈 요청',
    description: '부모님에게 용돈을 요청할 수 있어요.',
    enabled: true,
  },
  {
    id: 'limit',
    title: '사용 금액 한도 보기',
    description: '설정된 한 달 사용 한도를 확인할 수 있어요.',
    enabled: true,
  },
])

const monthlyLimit = ref(100000)
const limitPresets = [50000, 100000, 200000, 300000]

const formatAmount = (amount: number) => amount.toLocaleString('ko-KR')

const updateLimit = (event: Event) => {
  const input = event.target as HTMLInputElement
  const amount = Number(input.value.replace(/[^0-9]/g, ''))
  monthlyLimit.value = Number.isFinite(amount) ? amount : 0
  input.value = formatAmount(monthlyLimit.value)
}

const setMonthlyLimit = (amount: number) => {
  monthlyLimit.value = amount
}

const savePermissions = async () => {
  if (!selectedAccountId.value) return
  try {
    const usageMode = permissions.value.find(({ id }) => id === 'limit')?.enabled
      ? 'CO_MANAGED'
      : 'UNRESTRICTED'
    await api.updateUsagePolicyUsingPATCH(selectedAccountId.value, {
      child_usage_mode: usageMode,
      child_monthly_budget_amount: usageMode === 'CO_MANAGED' ? monthlyLimit.value : undefined,
    })
    showToast('아이 이용 권한이 저장되었습니다.', 'success')
  } catch (error) {
    showToast(getApiErrorMessage(error, '권한 설정을 저장하지 못했습니다.'), 'error')
  }
}

const changeChild = () => {
  isChildSheetOpen.value = true
}

const startSheetDrag = (event: TouchEvent) => {
  sheetTouchStartY.value = event.touches[0]?.clientY ?? null
  sheetDragOffset.value = 0
  isSheetDragging.value = true
}

const moveSheetDrag = (event: TouchEvent) => {
  if (sheetTouchStartY.value === null) return
  const currentY = event.touches[0]?.clientY ?? sheetTouchStartY.value
  sheetDragOffset.value = Math.max(0, currentY - sheetTouchStartY.value)
}

const endSheetDrag = () => {
  if (sheetDragOffset.value > 80) isChildSheetOpen.value = false
  sheetTouchStartY.value = null
  sheetDragOffset.value = 0
  isSheetDragging.value = false
}

const selectChild = (child: ChildProfile) => {
  selectedChildId.value = child.id
  isChildSheetOpen.value = false
  void loadUsagePolicy(child.id)
}

const loadUsagePolicy = async (childId: number) => {
  try {
    const { data: accounts } = await api.getChildAccountsUsingGET(childId)
    selectedAccountId.value = accounts.accounts.find(
      ({ account_product_type }) => account_product_type === 'DEMAND_DEPOSIT',
    )?.account_id ?? null
    if (!selectedAccountId.value) {
      showToast('이용 권한을 설정할 입출금계좌가 없습니다.', 'error')
      return
    }
    const { data } = await api.getUsagePolicyUsingGET(selectedAccountId.value)
    monthlyLimit.value = data.child_monthly_budget_amount ?? 0
    const limitPermission = permissions.value.find(({ id }) => id === 'limit')
    if (limitPermission) limitPermission.enabled = data.child_usage_mode === 'CO_MANAGED'
  } catch (error) {
    showToast(getApiErrorMessage(error, '권한 설정을 불러오지 못했습니다.'), 'error')
  }
}

onMounted(async () => {
  try {
    const { data } = await api.getChildrenUsingGET()
    children.value = (data.items ?? []).map((child) => ({
      id: child.child_id ?? 0,
      name: child.name ?? '아이',
      age: child.age ?? 0,
      schoolLevel: child.age && child.age >= 14 ? '중학생 이상' : '초등학생',
    }))
    selectedChildId.value = children.value[0]?.id ?? 0
    if (selectedChildId.value) await loadUsagePolicy(selectedChildId.value)
  } catch (error) {
    showToast(getApiErrorMessage(error, '자녀 목록을 불러오지 못했습니다.'), 'error')
  } finally {
    isLoading.value = false
  }
})

const disconnectChild = () => {
  showToast('아이 계정 연결 해제는 보호자 확인이 필요해요.', 'error')
}
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height)-env(safe-area-inset-bottom))] bg-white px-5 pt-4 pb-5"
  >
    <div v-if="isLoading" aria-label="아이 이용 권한을 불러오는 중" aria-busy="true">
      <section class="flex min-h-[74px] items-center gap-3 rounded-[20px] border border-[#cfe8f3] bg-[#eaf8fe] p-3">
        <span class="size-12 shrink-0 animate-pulse rounded-full bg-white/80"></span>
        <span class="min-w-0 flex-1">
          <span class="block h-4 w-20 animate-pulse rounded-md bg-[#d6ebf4]"></span>
          <span class="mt-2 block h-3 w-10 animate-pulse rounded-full bg-[#dceef5]"></span>
        </span>
        <span class="h-8 w-16 shrink-0 animate-pulse rounded-lg bg-white/80"></span>
      </section>

      <section class="mt-6" aria-hidden="true">
        <span class="block h-[23px] w-32 animate-pulse rounded-md bg-[#e3eaed]"></span>
        <span class="mt-2 block h-3 w-48 animate-pulse rounded-full bg-[#edf2f4]"></span>
        <div class="mt-4 overflow-hidden rounded-[20px] border border-[#d9e2e7] bg-white px-5">
          <div
            v-for="index in 2"
            :key="index"
            class="flex min-h-16 items-center gap-3 py-2.5"
            :class="index > 1 ? 'border-t border-[#edf1f3]' : ''"
          >
            <span class="min-w-0 flex-1">
              <span class="block h-4 w-28 animate-pulse rounded-md bg-[#e5ecef]"></span>
              <span class="mt-2 block h-3 w-52 max-w-full animate-pulse rounded-full bg-[#edf2f4]"></span>
            </span>
            <span class="h-[30px] w-[54px] shrink-0 animate-pulse rounded-full bg-[#e1e9ed]"></span>
          </div>
        </div>
      </section>

      <section class="mt-7" aria-hidden="true">
        <span class="block h-[23px] w-32 animate-pulse rounded-md bg-[#e3eaed]"></span>
        <span class="mt-2 block h-3 w-56 max-w-full animate-pulse rounded-full bg-[#edf2f4]"></span>
        <div class="mt-4 rounded-[20px] border border-[#d9e2e7] bg-white p-4">
          <span class="block h-4 w-24 animate-pulse rounded-md bg-[#e5ecef]"></span>
          <span class="mt-2 block h-12 w-full animate-pulse rounded-xl bg-[#edf2f4]"></span>
          <span class="mt-3 block h-9 w-full animate-pulse rounded-lg bg-[#edf2f4]"></span>
          <span class="mt-3 block h-3 w-52 max-w-full animate-pulse rounded-full bg-[#f0f3f5]"></span>
        </div>
      </section>

      <div class="mt-14 h-14 w-full animate-pulse rounded-2xl bg-[#dce8ed]" aria-hidden="true"></div>
    </div>

    <template v-else>
    <section
      class="flex items-center gap-3 rounded-[20px] border border-[#cfe8f3] bg-[#eaf8fe] p-3"
    >
      <span
        class="grid size-12 shrink-0 place-items-center overflow-hidden rounded-full bg-white"
        aria-hidden="true"
      >
        <img class="size-9 object-contain" :src="childProfileImage" alt="" />
      </span>
      <div class="min-w-0 flex-1">
        <strong class="block truncate text-[16px] font-bold text-[var(--color-text-primary)]">
          {{ selectedChild.name }}
        </strong>
        <span class="mt-0.5 block text-[11px] text-[var(--color-text-secondary)]">
          {{ selectedChild.age }}세
        </span>
      </div>
      <button
        class="shrink-0 rounded-lg border border-[var(--color-border)] bg-white px-3 py-2 text-[11px] font-bold text-[var(--color-text-secondary)] active:bg-[#f5f7f8]"
        type="button"
        @click="changeChild"
      >
        아이 변경
      </button>
    </section>

    <form class="mt-6" @submit.prevent="savePermissions">
      <section>
        <h1 class="text-[19px] font-extrabold tracking-[-0.02em] text-[var(--color-text-primary)]">
          기본 이용 권한
        </h1>
        <p class="mt-1 text-[11px] text-[var(--color-text-secondary)]">
          아이 화면에서 사용할 기능을 선택해주세요.
        </p>

        <ul class="mt-4 m-0 list-none overflow-hidden rounded-[20px] border border-[#d9e2e7] bg-white p-0">
          <li
            v-for="permission in permissions"
            :key="permission.id"
            class="relative flex min-h-16 items-center gap-3 px-5 py-2.5 after:absolute after:right-5 after:bottom-0 after:left-5 after:h-px after:bg-[#edf1f3] last:after:hidden"
          >
            <label class="min-w-0 flex-1 cursor-pointer" :for="`permission-${permission.id}`">
              <strong class="block text-[14px] font-bold text-[var(--color-text-primary)]">
                {{ permission.title }}
              </strong>
              <span class="mt-1 block text-[11px] text-[var(--color-text-secondary)]">
                {{ permission.description }}
              </span>
            </label>

            <label class="relative h-[30px] w-[54px] shrink-0 cursor-pointer">
              <input
                :id="`permission-${permission.id}`"
                v-model="permission.enabled"
                class="peer sr-only"
                type="checkbox"
                role="switch"
                :aria-label="`${permission.title} ${permission.enabled ? '끄기' : '켜기'}`"
              />
              <span
                class="absolute inset-0 rounded-full bg-[#dfe8ed] transition-colors duration-200 peer-checked:bg-[var(--color-brand-primary)] peer-focus-visible:ring-2 peer-focus-visible:ring-[#bcecff] peer-focus-visible:ring-offset-2"
              ></span>
              <span
                class="absolute top-[3px] left-[3px] size-6 rounded-full bg-white shadow-[0_1px_4px_rgba(31,52,62,0.18)] transition-transform duration-200 peer-checked:translate-x-6"
              ></span>
            </label>
          </li>
        </ul>
      </section>

      <section class="mt-7">
        <h2 class="text-[19px] font-extrabold tracking-[-0.02em] text-[var(--color-text-primary)]">
          사용 금액 한도
        </h2>
        <p class="mt-1 text-[11px] text-[var(--color-text-secondary)]">
          아이가 한 달 동안 사용할 수 있는 최대 금액이에요.
        </p>

        <div class="mt-4 rounded-[20px] border border-[#d9e2e7] bg-white p-4">
          <label class="block">
            <strong class="text-[13px] font-bold">한 달 사용 한도</strong>
            <span
              class="mt-2 flex min-h-12 items-center rounded-xl border border-[var(--color-border)] bg-[#f7f9fa] px-4 focus-within:border-[var(--color-brand-primary)] focus-within:bg-white focus-within:ring-2 focus-within:ring-[#e1f5fe]"
            >
              <input
                class="min-w-0 flex-1 bg-transparent text-right text-[20px] font-extrabold outline-none"
                inputmode="numeric"
                :value="formatAmount(monthlyLimit)"
                aria-label="한 달 사용 한도"
                @input="updateLimit"
              />
              <span class="ml-2 text-sm font-bold text-[var(--color-text-secondary)]">원</span>
            </span>
          </label>

          <div class="mt-3 grid grid-cols-4 gap-2" aria-label="한 달 사용 한도 빠른 선택">
            <button
              v-for="amount in limitPresets"
              :key="amount"
              class="min-h-9 rounded-lg text-[11px] font-bold transition-colors"
              :class="
                monthlyLimit === amount
                  ? 'bg-[var(--color-selected-background)] text-[var(--color-selected-text)]'
                  : 'bg-[#f2f5f6] text-[var(--color-text-secondary)] active:bg-[#e8edef]'
              "
              type="button"
              @click="setMonthlyLimit(amount)"
            >
              {{ amount / 10000 }}만원
            </button>
          </div>

          <p class="mt-2 text-[10px] leading-4 text-[var(--color-text-secondary)]">
            한도를 초과하면 아이와 보호자에게 알림을 보내드려요.
          </p>
        </div>
      </section>

      <button
        class="mt-2 min-h-9 w-full text-[12px] font-semibold text-[#dc6b6b] underline decoration-[#efcaca] underline-offset-4"
        type="button"
        @click="disconnectChild"
      >
        아이 계정 연결 해제
      </button>

      <button
        class="mt-3 min-h-14 w-full rounded-2xl bg-[var(--color-brand-primary)] text-[15px] font-bold text-white active:bg-[var(--color-brand-primary-pressed)]"
        type="submit"
      >
        권한 설정 저장
      </button>
    </form>
    </template>

    <Transition name="child-sheet">
      <div
        v-if="isChildSheetOpen"
        class="fixed inset-y-0 left-1/2 z-[var(--z-index-overlay)] w-full max-w-[var(--app-max-width)] -translate-x-1/2"
      >
        <button
          class="absolute inset-0 size-full border-0 bg-black/35"
          type="button"
          aria-label="자녀 선택 닫기"
          @click="isChildSheetOpen = false"
        ></button>

        <section
          class="child-sheet__panel absolute right-0 bottom-0 left-0 rounded-t-[28px] bg-white px-5 pt-4 pb-[calc(24px+env(safe-area-inset-bottom))] shadow-[0_-12px_36px_rgba(31,52,62,0.14)]"
          :class="{ 'is-dragging': isSheetDragging }"
          :style="isSheetDragging ? { transform: `translateY(${sheetDragOffset}px)` } : undefined"
          @touchstart="startSheetDrag"
          @touchmove.prevent="moveSheetDrag"
          @touchend="endSheetDrag"
          @touchcancel="endSheetDrag"
          role="dialog"
          aria-modal="true"
          aria-labelledby="child-sheet-title"
        >
          <div class="mx-auto h-1.5 w-16 rounded-full bg-[#d8e0e5]" aria-hidden="true"></div>
          <div class="mt-5 flex items-center justify-between">
            <div>
              <h2 id="child-sheet-title" class="text-[20px] font-extrabold text-[var(--color-text-primary)]">
                자녀 선택
              </h2>
              <p class="mt-1 text-xs text-[var(--color-text-secondary)]">
                권한을 설정할 아이를 선택해주세요.
              </p>
            </div>
            <button
              class="grid size-10 place-items-center rounded-full text-2xl leading-none text-[var(--color-text-secondary)] active:bg-[var(--color-unselected-background)]"
              type="button"
              aria-label="닫기"
              @click="isChildSheetOpen = false"
            >
              ×
            </button>
          </div>

          <ul class="mt-5 m-0 list-none space-y-3 p-0">
            <li v-for="child in children" :key="child.id">
              <button
                class="flex min-h-[72px] w-full items-center gap-3 rounded-xl border px-4 text-left transition-colors"
                :class="
                  child.id === selectedChildId
                    ? 'border-[var(--color-brand-primary)] bg-[var(--color-selected-background)]'
                    : 'border-[var(--color-border)] bg-white active:bg-[var(--color-unselected-background)]'
                "
                type="button"
                @click="selectChild(child)"
              >
                <span
                  class="grid size-10 shrink-0 place-items-center overflow-hidden rounded-full bg-white"
                  aria-hidden="true"
                >
                  <img class="size-8 object-contain" :src="childProfileImage" alt="" />
                </span>
                <span class="min-w-0 flex-1">
                  <strong class="block truncate text-[14px] font-bold">{{ child.name }}</strong>
                  <span class="mt-1 block text-[11px] text-[var(--color-text-secondary)]">
                    {{ child.age }}세
                  </span>
                </span>
                <span
                  v-if="child.id === selectedChildId"
                  class="grid size-6 shrink-0 place-items-center rounded-full bg-[var(--color-brand-primary)] text-sm font-bold text-white"
                  aria-label="선택됨"
                >
                  ✓
                </span>
              </button>
            </li>
          </ul>
        </section>
      </div>
    </Transition>
  </main>
</template>

<style scoped>
.child-sheet-enter-active,
.child-sheet-leave-active {
  transition: opacity 200ms ease;
}

.child-sheet-enter-active .child-sheet__panel,
.child-sheet-leave-active .child-sheet__panel {
  transition: transform 260ms cubic-bezier(0.22, 1, 0.36, 1);
}

.child-sheet-enter-from,
.child-sheet-leave-to {
  opacity: 0;
}

.child-sheet-enter-from .child-sheet__panel,
.child-sheet-leave-to .child-sheet__panel {
  transform: translateY(100%);
}

.child-sheet__panel.is-dragging {
  transition: none;
}
</style>
