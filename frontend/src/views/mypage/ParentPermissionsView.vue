<script setup lang="ts">
import { computed, ref } from 'vue'
import childProfileImage from '@/assets/images/child/baby.png'
import { useToast } from '@/composables/useToast'

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
const children: ChildProfile[] = [
  { id: 1, name: '아이 1', age: 12, schoolLevel: '초등학생' },
  { id: 2, name: '아이 2', age: 10, schoolLevel: '초등학생' },
  { id: 3, name: '아이 3', age: 7, schoolLevel: '초등학생' },
]
const selectedChildId = ref(1)
const isChildSheetOpen = ref(false)
const sheetTouchStartY = ref<number | null>(null)
const sheetDragOffset = ref(0)
const isSheetDragging = ref(false)
const selectedChild = computed(() =>
  children.find(({ id }) => id === selectedChildId.value) ?? children[0]!,
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

const savePermissions = () => {
  showToast('아이 이용 권한이 저장되었습니다.', 'success')
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
  showToast(`${child.name}의 권한 설정을 불러왔습니다.`, 'info')
}

const disconnectChild = () => {
  showToast('아이 계정 연결 해제는 보호자 확인이 필요해요.', 'error')
}
</script>

<template>
  <main
    class="h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height)-env(safe-area-inset-bottom))] overflow-hidden bg-white px-5 pt-4 pb-20"
  >
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
    </form>

    <div
      class="pointer-events-none fixed bottom-[calc(var(--app-bottom-nav-height)+12px+env(safe-area-inset-bottom))] left-1/2 z-20 w-full max-w-[var(--app-max-width)] -translate-x-1/2 px-5"
    >
      <button
        class="pointer-events-auto min-h-14 w-full rounded-2xl bg-[var(--color-brand-primary)] text-[15px] font-bold text-white active:bg-[var(--color-brand-primary-pressed)]"
        type="button"
        @click="savePermissions"
      >
        권한 설정 저장
      </button>
    </div>

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
