<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ChevronRight, LogOut, ShieldCheck, UserRound, X } from 'lucide-vue-next'
import { useRouter } from 'vue-router'

import childProfileUrl from '@/assets/images/home/home-profile-baby.png'
import { useToast } from '@/composables/useToast'
import { api, getApiErrorMessage } from '@/api'
import { clearAuthSession, getRefreshToken } from '@/api/auth'
import { resolveCurrentChildId } from '@/api/context'
import { disablePushNotifications } from '@/services/pushNotifications'

const router = useRouter()
const { showToast } = useToast()
const accountAction = ref<'logout' | 'withdrawal' | null>(null)
const isProfileLoading = ref(true)

const profileName = ref('')
const childName = ref('')
const childAgeText = ref('')
const familyMembers = ref<Array<{ id: number; name: string; relation: string; initials: string; color: string }>>([])

const serviceMenus = [
  { label: '목표 관리', description: '아이의 저축 목표를 확인해요', to: { name: 'MypageGoals' } },
  { label: '아이 이용 권한', description: '이체와 용돈 요청 권한을 설정해요', to: { name: 'ParentPermissions' } },
  { label: '알림 설정', description: '받고 싶은 알림을 선택해요', to: { name: 'AlarmSettings' } },
  { label: '도움말', description: '서비스 이용 방법을 확인해요', to: { name: 'Guide' } },
]

const logout = async () => {
  accountAction.value = null
  try {
    try {
      await disablePushNotifications()
    } catch (error) {
      console.warn('로그아웃 중 푸시 기기 해제에 실패했습니다.', error)
    }

    const refreshToken = getRefreshToken()
    if (refreshToken) await api.logoutUsingPOST({ refresh_token: refreshToken })
  } finally {
    clearAuthSession()
    showToast('로그아웃되었습니다.', 'info')
    await router.push({ name: 'Login' })
  }
}

const withdraw = async () => {
  accountAction.value = null
  try {
    await api.withdrawMyMembershipUsingDELETE()
    clearAuthSession()
    showToast('회원 탈퇴가 완료되었습니다.', 'info')
    await router.push({ name: 'Login' })
  } catch (error) {
    showToast(getApiErrorMessage(error, '회원 탈퇴에 실패했습니다.'), 'error')
  }
}

onMounted(async () => {
  try {
    const [{ data: profile }, selectedChildId] = await Promise.all([
      api.getMyProfileUsingGET(),
      resolveCurrentChildId(),
    ])
    profileName.value = profile.name
    const [{ data: child }, { data: guardians }] = await Promise.all([
      api.getChildUsingGET(selectedChildId),
      api.getFamilyMembersUsingGET(selectedChildId),
    ])
    childName.value = child.name ?? '아이'
    childAgeText.value = typeof child.age === 'number' ? `${child.age}세` : '나이 정보 없음'
    familyMembers.value = (guardians.items ?? []).map((member, index) => ({
      id: member.member_id ?? index,
      name: member.name ?? '보호자',
      relation: member.relation_type === 'FATHER' ? '부' : member.relation_type === 'MOTHER' ? '모' : '보호자',
      initials: (member.name ?? '보').slice(0, 1),
      color: index % 3 === 0 ? 'bg-[#eaf7ff] text-[#419ac5]' : index % 3 === 1 ? 'bg-[#fff6dc] text-[#b88a20]' : 'bg-[#f4edff] text-[#8b68bd]',
    }))
  } catch (error) {
    showToast(getApiErrorMessage(error, '마이페이지 정보를 불러오지 못했습니다.'), 'error')
  } finally {
    isProfileLoading.value = false
  }
})
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] px-5 pt-5 pb-5 text-[var(--color-text-primary)]">
    <h1 class="sr-only">마이페이지</h1>

    <section class="min-h-[200px] overflow-hidden rounded-[28px] border border-[#cfe8f3] bg-[#eaf8fe] p-5">
      <div v-if="isProfileLoading" aria-label="프로필 정보를 불러오는 중" aria-busy="true">
        <div class="flex items-start gap-4">
          <span class="size-15 shrink-0 animate-pulse rounded-full bg-white/80"></span>
          <span class="min-w-0 flex-1 pt-1">
            <span class="block h-3 w-16 animate-pulse rounded-full bg-[#d7edf6]"></span>
            <span class="mt-2 block h-6 w-28 animate-pulse rounded-lg bg-[#d7edf6]"></span>
            <span class="mt-2 block h-3 w-40 animate-pulse rounded-full bg-[#d7edf6]"></span>
          </span>
          <span class="h-8 w-14 shrink-0 animate-pulse rounded-full bg-white/80"></span>
        </div>
        <div class="mt-5 flex items-center gap-3 rounded-[20px] bg-white p-3.5">
          <span class="size-13 shrink-0 animate-pulse rounded-full bg-[#edf4f7]"></span>
          <span class="min-w-0 flex-1">
            <span class="block h-3 w-12 animate-pulse rounded-full bg-[#e3ebef]"></span>
            <span class="mt-2 block h-5 w-24 animate-pulse rounded-md bg-[#e3ebef]"></span>
          </span>
          <span class="h-3 w-14 shrink-0 animate-pulse rounded-full bg-[#e3ebef]"></span>
        </div>
      </div>

      <template v-else>
      <div class="flex items-start gap-4">
        <span class="grid size-15 shrink-0 place-items-center rounded-full bg-white text-[var(--color-selected-text)]">
          <UserRound :size="28" :stroke-width="2" />
        </span>
        <div class="min-w-0 flex-1 pt-0.5">
          <span class="text-xs font-semibold text-[var(--color-selected-text)]">가족 대표</span>
          <h2 class="mt-0.5 text-[22px] font-extrabold tracking-[-0.035em]">{{ profileName }}님</h2>
          <p class="mt-1 text-xs text-[var(--color-text-secondary)]">{{ childName }}의 든든한 보호자예요.</p>
        </div>
        <RouterLink
          class="rounded-full bg-white px-3 py-2 text-xs font-bold !text-[var(--color-selected-text)] active:bg-[#f5fbfe]"
          :to="{ name: 'MypageEdit' }"
        >내 정보</RouterLink>
      </div>
      <RouterLink
        class="mt-5 flex items-center gap-3 rounded-[20px] bg-white p-3.5 !text-[var(--color-text-primary)] active:bg-[#f9fcfd]"
        :to="{ name: 'ChildEdit' }"
      >
        <span class="size-13 shrink-0 overflow-hidden rounded-full bg-[var(--color-selected-background)]">
          <img class="size-full object-cover" :src="childProfileUrl" :alt="`${childName} 프로필`" />
        </span>
        <span class="min-w-0 flex-1">
          <span class="block text-[11px] font-semibold text-[var(--color-text-secondary)]">
            {{ childAgeText }}
          </span>
          <strong class="mt-0.5 block text-[16px]">{{ childName }}</strong>
        </span>
        <span
          class="inline-flex shrink-0 items-center self-center text-[11px] font-bold text-[var(--color-selected-text)]"
        >
          정보 수정 <ChevronRight :size="13" />
        </span>
      </RouterLink>
      </template>
    </section>

    <section class="mt-6">
      <div class="mb-3 flex items-center justify-between px-0.5">
        <h2 class="m-0 text-[18px] font-extrabold">우리 가족</h2>
        <RouterLink
          class="inline-flex items-center py-1.5 pl-2 text-xs font-bold !text-[#7b8794]"
          :to="{ name: 'FamilyManagement' }"
        >
          가족 관리
          <ChevronRight :size="14" />
        </RouterLink>
      </div>

      <div class="family-scroll" aria-label="우리 가족 목록">
        <template v-if="isProfileLoading">
          <div
            v-for="index in 2"
            :key="`family-skeleton-${index}`"
            class="family-card flex shrink-0 snap-start items-center gap-3 rounded-[16px] border border-[#d9e2e7] bg-white p-3"
            aria-hidden="true"
          >
            <span class="size-10 shrink-0 animate-pulse rounded-full bg-[#edf3f6]"></span>
            <span class="min-w-0 flex-1">
              <span class="block h-3.5 w-16 animate-pulse rounded-full bg-[#e5ecef]"></span>
              <span class="mt-2 block h-2.5 w-10 animate-pulse rounded-full bg-[#edf2f4]"></span>
            </span>
          </div>
        </template>
        <div
          v-else
          v-for="member in familyMembers"
          :key="member.id"
          class="family-card flex shrink-0 snap-start items-center gap-3 rounded-[16px] border border-[#d9e2e7] bg-white p-3"
        >
          <span class="grid size-10 shrink-0 place-items-center rounded-full text-sm font-extrabold" :class="member.color">
            {{ member.initials }}
          </span>
          <span class="min-w-0">
            <strong class="block truncate text-sm">{{ member.name }}</strong>
            <span class="mt-0.5 block text-[11px] text-[var(--color-text-secondary)]">{{ member.relation }}</span>
          </span>
        </div>
      </div>
    </section>

    <section class="mt-6">
      <div class="mb-3 px-0.5">
        <h2 class="m-0 text-[18px] font-extrabold">서비스 설정</h2>
      </div>

      <nav class="overflow-hidden rounded-[20px] border border-[#d9e2e7] bg-white px-4" aria-label="서비스 설정 메뉴">
        <RouterLink
          v-for="(menu, index) in serviceMenus"
          :key="menu.label"
          class="flex min-h-[72px] items-center gap-3 !text-[var(--color-text-primary)] transition-colors active:bg-[#f6fafb]"
          :class="index ? 'border-t border-[#edf1f3]' : ''"
          :to="menu.to"
        >
          <span class="min-w-0 flex-1">
            <strong class="block text-[15px] font-bold">{{ menu.label }}</strong>
            <span class="mt-1 block truncate text-xs text-[var(--color-text-secondary)]">{{ menu.description }}</span>
          </span>
          <ChevronRight class="shrink-0 text-[#a4b1ba]" :size="17" :stroke-width="2.2" />
        </RouterLink>
      </nav>
    </section>

    <section class="mt-6 grid gap-1 border-t border-[#e5e8e9] pt-5" aria-label="계정 관리">
      <button
        class="flex h-12 w-full items-center justify-center gap-1.5 rounded-[14px] border border-[#d9e2e7] bg-white text-sm font-semibold text-[var(--color-text-secondary)] active:bg-[#f5f7f8]"
        type="button"
        @click="accountAction = 'logout'"
      >
        <LogOut :size="17" />
        로그아웃
      </button>
      <button
        class="h-10 w-full text-xs font-semibold text-[#d87979]"
        type="button"
        @click="accountAction = 'withdrawal'"
      >
        회원 탈퇴
      </button>
    </section>

    <Teleport to="body">
      <Transition name="mypage-sheet">
        <div
          v-if="accountAction"
          class="fixed inset-0 z-[var(--z-index-overlay)] flex items-end justify-center bg-black/35"
          @click.self="accountAction = null"
        >
          <section class="w-full max-w-[var(--app-max-width)] rounded-t-[24px] bg-white px-5 pt-3 pb-[calc(24px+env(safe-area-inset-bottom))]">
            <span class="mx-auto block h-1 w-10 rounded-full bg-[#d5dce1]"></span>
            <header class="mt-4 flex items-center justify-between">
              <div>
                <h2 class="m-0 text-[19px] font-extrabold">
                  {{ accountAction === 'logout' ? '로그아웃할까요?' : '정말 탈퇴할까요?' }}
                </h2>
                <p class="mt-1.5 mb-0 text-[11px] text-[var(--color-text-secondary)]">
                  {{
                    accountAction === 'logout'
                      ? '언제든 다시 로그인할 수 있어요.'
                      : '탈퇴하면 지금까지 저장한 정보를 다시 복구할 수 없어요.'
                  }}
                </p>
              </div>
              <button class="grid size-9 place-items-center rounded-full text-[var(--color-text-secondary)] active:bg-[#f0f3f5]" type="button" aria-label="닫기" @click="accountAction = null">
                <X :size="20" />
              </button>
            </header>
            <div
              class="mt-5 flex items-center gap-3 rounded-[16px] p-4"
              :class="accountAction === 'logout' ? 'bg-[#f1f9fd]' : 'bg-[#fff1f1]'"
            >
              <ShieldCheck
                class="shrink-0"
                :class="accountAction === 'logout' ? 'text-[var(--color-brand-primary)]' : 'text-[#e45c5c]'"
                :size="25"
              />
              <p class="m-0 text-[12px] leading-[1.55] text-[var(--color-text-secondary)]">
                {{
                  accountAction === 'logout'
                    ? '저장한 목표와 타임캡슐은 안전하게 보관돼요.'
                    : '계좌 연결, 저축 목표, 타임캡슐 등 모든 서비스 정보가 삭제돼요.'
                }}
              </p>
            </div>
            <div class="mt-5 grid grid-cols-2 gap-3">
              <button class="h-12 rounded-[13px] border border-[var(--color-border)] bg-white text-[14px] font-bold text-[var(--color-unselected-text)]" type="button" @click="accountAction = null">취소</button>
              <button
                class="h-12 rounded-[13px] text-[14px] font-bold text-white"
                :class="
                  accountAction === 'logout'
                    ? 'bg-[var(--color-brand-primary)] active:bg-[var(--color-brand-primary-pressed)]'
                    : 'bg-[#ef6666] active:bg-[#d95151]'
                "
                type="button"
                @click="accountAction === 'logout' ? logout() : withdraw()"
              >
                {{ accountAction === 'logout' ? '로그아웃' : '회원 탈퇴' }}
              </button>
            </div>
          </section>
        </div>
      </Transition>
    </Teleport>
  </main>
</template>

<style scoped>
.mypage-sheet-enter-active,
.mypage-sheet-leave-active {
  transition: background-color 240ms ease;
}

.mypage-sheet-enter-active > section,
.mypage-sheet-leave-active > section {
  transition: transform 280ms cubic-bezier(0.22, 1, 0.36, 1);
}

.mypage-sheet-enter-from,
.mypage-sheet-leave-to {
  background-color: transparent;
}

.mypage-sheet-enter-from > section,
.mypage-sheet-leave-to > section {
  transform: translateY(100%);
}

.family-scroll {
  display: flex;
  min-height: 76px;
  gap: 12px;
  margin-right: -20px;
  padding-right: 20px;
  overflow-x: auto;
  scroll-snap-type: x mandatory;
  scrollbar-width: none;
}

.family-scroll::-webkit-scrollbar {
  display: none;
}

.family-card {
  width: calc((100% - 12px) / 2);
  min-height: 76px;
}
</style>
