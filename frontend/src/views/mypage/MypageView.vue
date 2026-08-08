<script setup lang="ts">
import { ref } from 'vue'
import { ChevronRight, LogOut, ShieldCheck, UserRound, X } from 'lucide-vue-next'
import { useRouter } from 'vue-router'

import childProfileUrl from '@/assets/images/home/home-profile-baby.png'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const { showToast } = useToast()
const accountAction = ref<'logout' | 'withdrawal' | null>(null)

const familyMembers = [
  { id: 1, name: '김둘', relation: '부', initials: '김', color: 'bg-[#eaf7ff] text-[#419ac5]' },
  { id: 2, name: '김다섯', relation: '보호자', initials: '김', color: 'bg-[#fff6dc] text-[#b88a20]' },
]

const serviceMenus = [
  { label: '계좌 관리', description: '등록된 계좌를 확인하고 관리해요', to: { name: 'MypageAccounts' } },
  { label: '목표 관리', description: '아이의 저축 목표를 관리해요', to: { name: 'MypageGoals' } },
  { label: '아이 이용 권한', description: '이체와 용돈 요청 권한을 설정해요', to: { name: 'ParentPermissions' } },
  { label: '알림 설정', description: '받고 싶은 알림을 선택해요', to: { name: 'AlarmSettings' } },
  { label: '도움말', description: '서비스 이용 방법을 확인해요', to: { name: 'Guide' } },
]

const logout = () => {
  accountAction.value = null
  showToast('로그아웃되었습니다.', 'info')
  router.push({ name: 'Login' })
}

const withdraw = () => {
  accountAction.value = null
  showToast('회원 탈퇴가 완료되었습니다.', 'info')
  router.push({ name: 'Login' })
}
</script>

<template>
  <main class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))]  px-[18px] pt-[18px] pb-3 text-[var(--color-text-primary)]">
    <h1 class="sr-only">마이페이지</h1>

    <section class="overflow-hidden rounded-[22px] border border-[var(--color-border)] bg-white shadow-[0_8px_28px_rgba(0,0,0,0.07)]">
      <RouterLink
        class="group flex items-center gap-3.5 px-4 py-[17px] !text-[var(--color-text-primary)] transition-colors active:bg-[#f4f9fb]"
        :to="{ name: 'MypageEdit' }"
      >
        <span class="grid size-[46px] shrink-0 place-items-center rounded-full bg-[#edf8fd] text-[var(--color-selected-text)]">
          <UserRound :size="23" :stroke-width="2.1" />
        </span>
        <span class="min-w-0 flex-1">
          <strong class="block text-[16px] font-extrabold">김하나</strong>
          <span class="mt-1 block text-[11px] text-[var(--color-text-secondary)]">깨비의 보호자 · 가족 대표</span>
        </span>
        <span class="inline-flex items-center gap-0.5 text-[11px] font-bold text-[var(--color-selected-text)]">
          내 정보 수정
          <ChevronRight :size="14" />
        </span>
      </RouterLink>

      <div class="mx-4 border-t border-[#edf2f5]"></div>

      <RouterLink
        class="group flex items-center gap-3.5 px-4 py-[17px] !text-[var(--color-text-primary)] transition-colors active:bg-[#f4f9fb]"
        :to="{ name: 'ChildEdit' }"
      >
        <span class="size-[46px] shrink-0 overflow-hidden rounded-full bg-[var(--color-selected-background)]">
          <img class="size-full object-cover" :src="childProfileUrl" alt="깨비 프로필" />
        </span>
        <span class="min-w-0 flex-1">
          <strong class="block text-[16px] font-extrabold">깨비</strong>
          <span class="mt-1 block text-[11px] text-[var(--color-text-secondary)]">12세 · 초등학생</span>
        </span>
        <span class="inline-flex items-center gap-0.5 text-[11px] font-bold text-[var(--color-selected-text)]">
          자녀 정보 수정
          <ChevronRight :size="14" />
        </span>
      </RouterLink>
    </section>

    <section class="mt-6">
      <div class="mb-3 flex items-end justify-between px-0.5">
        <div>
          <h2 class="m-0 text-[17px] font-extrabold">우리 가족</h2>
          <p class="mt-1 mb-0 text-[11px] text-[var(--color-text-secondary)]">깨비와 연결된 보호자예요.</p>
        </div>
        <RouterLink
          class="inline-flex items-center text-[11px] font-bold text-[var(--color-selected-text)]"
          :to="{ name: 'FamilyManagement' }"
        >
          가족 관리
          <ChevronRight :size="14" />
        </RouterLink>
      </div>

      <div class="rounded-[20px] border border-[var(--color-border)] bg-white px-4 shadow-[0_6px_22px_rgba(0,0,0,0.06)]">
        <div
          v-for="(member, index) in familyMembers"
          :key="member.id"
          class="flex min-h-[66px] items-center gap-3"
          :class="index ? 'border-t border-[#edf2f5]' : ''"
        >
          <span class="grid size-9 shrink-0 place-items-center rounded-full text-[12px] font-extrabold" :class="member.color">
            {{ member.initials }}
          </span>
          <strong class="min-w-0 flex-1 text-[14px]">{{ member.name }}</strong>
          <span class="rounded-full bg-[#f3f6f8] px-2.5 py-1 text-[10px] font-semibold text-[var(--color-text-secondary)]">
            {{ member.relation }}
          </span>
        </div>
      </div>
    </section>

    <section class="mt-6">
      <div class="mb-3 px-0.5">
        <h2 class="m-0 text-[17px] font-extrabold">서비스 설정</h2>
        <p class="mt-1 mb-0 text-[11px] text-[var(--color-text-secondary)]">계정과 서비스 이용 환경을 관리해요.</p>
      </div>

      <nav class="overflow-hidden rounded-[20px] border border-[var(--color-border)] bg-white px-4 shadow-[0_6px_22px_rgba(0,0,0,0.06)]" aria-label="서비스 설정 메뉴">
        <RouterLink
          v-for="(menu, index) in serviceMenus"
          :key="menu.label"
          class="flex min-h-[64px] items-center gap-3 !text-[var(--color-text-primary)] transition-colors active:bg-[#f4f9fb]"
          :class="index ? 'border-t border-[#edf2f5]' : ''"
          :to="menu.to"
        >
          <span class="min-w-0 flex-1">
            <strong class="block text-[13px] font-bold">{{ menu.label }}</strong>
            <span class="mt-1 block truncate text-[10px] text-[var(--color-text-secondary)]">{{ menu.description }}</span>
          </span>
          <ChevronRight class="shrink-0 text-[#a4b1ba]" :size="17" :stroke-width="2.2" />
        </RouterLink>
      </nav>
    </section>

    <section class="mt-6 grid gap-2.5" aria-label="계정 관리">
      <button
        class="flex h-[52px] w-full items-center justify-center gap-2 rounded-[14px] border-0 bg-[var(--color-brand-primary)] text-[14px] font-bold text-white shadow-[0_6px_16px_rgba(0,0,0,0.08)] transition-colors active:bg-[var(--color-brand-primary-pressed)]"
        type="button"
        @click="accountAction = 'logout'"
      >
        <LogOut :size="17" />
        로그아웃
      </button>
      <button
        class="h-10 w-full text-[13px] font-semibold text-[#e26a6a] underline decoration-[#f2bcbc] underline-offset-4"
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
</style>
