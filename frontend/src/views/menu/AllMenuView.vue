<script setup lang="ts">
import {
  Baby,
  Bell,
  BellRing,
  BookOpen,
  ChevronRight,
  CircleCheckBig,
  CircleHelp,
  ClipboardCheck,
  Clock3,
  Landmark,
  Search,
  Settings,
  ShieldCheck,
  Sparkles,
  Target,
  UserRound,
  UsersRound,
  WalletCards,
  X,
} from 'lucide-vue-next'
import { computed, ref, type Component } from 'vue'
import type { RouteLocationRaw } from 'vue-router'

type MenuItem = {
  label: string
  description: string
  keywords: string[]
  icon: Component
  to: RouteLocationRaw
}

type MenuGroup = {
  title: string
  description: string
  iconClass: string
  items: MenuItem[]
}

const menuGroups: MenuGroup[] = [
  {
    title: '자산·금융',
    description: '계좌와 금융상품을 관리해요',
    iconClass: 'bg-[#eaf8ff] text-[#2aa7e4]',
    items: [
      {
        label: '계좌 관리',
        description: '가족의 연결 계좌와 자동이체를 관리해요.',
        keywords: ['자산', '계좌', '자동이체'],
        icon: WalletCards,
        to: { name: 'Assets' },
      },
      {
        label: '계좌 연동',
        description: '은행 계좌를 서비스에 연결해요.',
        keywords: ['계좌 등록', '은행'],
        icon: Landmark,
        to: { name: 'Accounts' },
      },
      {
        label: '금융상품 추천',
        description: '적금과 입출금계좌를 비교해요.',
        keywords: ['상품', '적금', '입출금'],
        icon: Sparkles,
        to: { name: 'Products' },
      },
    ],
  },
  {
    title: '목표·생활',
    description: '목표와 아이의 일상을 함께 관리해요',
    iconClass: 'bg-[#fff5d9] text-[#b88516]',
    items: [
      {
        label: '목표 관리',
        description: '목표별 저축 현황과 연결 계좌를 관리해요.',
        keywords: ['목표 목록', '저축 현황', '연결 계좌'],
        icon: Target,
        to: { name: 'MypageGoals' },
      },
      {
        label: '체크리스트',
        description: '가족이 함께할 할 일을 확인하고 관리해요.',
        keywords: ['할 일', '생활 습관', '목록'],
        icon: ClipboardCheck,
        to: { name: 'Checklists' },
      },
      {
        label: '타임캡슐',
        description: '계좌별로 쌓인 추억을 확인해요.',
        keywords: ['추억', '기록', '아카이브'],
        icon: Clock3,
        to: { name: 'TimeCapsuleArchive' },
      },
      {
        label: '용돈 미션',
        description: '아이에게 용돈 미션을 만들고 보상해요.',
        keywords: ['부모 미션', '보상', '용돈'],
        icon: CircleCheckBig,
        to: { name: 'ParentMissions' },
      },
    ],
  },
  {
    title: '리포트',
    description: '우리 가족의 자산과 양육비 흐름을 살펴봐요',
    iconClass: 'bg-[#eafbf3] text-[#2aa876]',
    items: [
      {
        label: '자산 리포트',
        description: '자산과 목표별 저축 현황을 확인해요.',
        keywords: ['자산 분석', '저축 현황', '인사이트'],
        icon: WalletCards,
        to: { name: 'Reports' },
      },
      {
        label: '양육비 리포트',
        description: '아이 관련 지출과 양육비 인사이트를 확인해요.',
        keywords: ['양육비 분석', '아이 지출', '인사이트'],
        icon: BookOpen,
        to: { name: 'Reports', query: { tab: 'allowance' } },
      },
    ],
  },
  {
    title: '가족·설정',
    description: '가족과 서비스 이용 정보를 관리해요',
    iconClass: 'bg-[#fff0f4] text-[#d76585]',
    items: [
      {
        label: '마이페이지',
        description: '내 정보와 가족 관리 메뉴를 확인해요.',
        keywords: ['내 정보', '설정', '프로필'],
        icon: Settings,
        to: { name: 'Mypage' },
      },
      {
        label: '내 정보 수정',
        description: '보호자 프로필과 연락처를 수정해요.',
        keywords: ['프로필', '개인 정보'],
        icon: UserRound,
        to: { name: 'MypageEdit' },
      },
      {
        label: '자녀 정보 수정',
        description: '자녀의 프로필과 기본 정보를 수정해요.',
        keywords: ['아이 정보', '프로필'],
        icon: Baby,
        to: { name: 'ChildEdit' },
      },
      {
        label: '가족 관리',
        description: '함께 자산을 관리할 가족을 초대해요.',
        keywords: ['보호자', '가족 초대'],
        icon: UsersRound,
        to: { name: 'FamilyManagement' },
      },
      {
        label: '아이 이용 권한',
        description: '자녀가 이용할 수 있는 기능을 설정해요.',
        keywords: ['권한 설정', '보호자'],
        icon: ShieldCheck,
        to: { name: 'ParentPermissions' },
      },
      {
        label: '알림',
        description: '새로운 요청과 서비스 알림을 확인해요.',
        keywords: ['알림 목록', 'notification'],
        icon: Bell,
        to: { name: 'Alarm' },
      },
      {
        label: '알림 설정',
        description: '받고 싶은 알림 종류를 설정해요.',
        keywords: ['푸시', 'notification', '설정'],
        icon: BellRing,
        to: { name: 'AlarmSettings' },
      },
      {
        label: '도움말',
        description: '서비스 이용 방법과 자주 묻는 질문을 봐요.',
        keywords: ['가이드', 'FAQ', '문의'],
        icon: CircleHelp,
        to: { name: 'Guide' },
      },
    ],
  },
  {
    title: '아이 전용',
    description: '아이 모드에서 사용하는 화면',
    iconClass: 'bg-[#eef2ff] text-[#687bd8]',
    items: [
      {
        label: '아이 홈',
        description: '아이의 계좌와 용돈 현황을 확인해요.',
        keywords: ['자녀 홈', '아이 모드'],
        icon: Baby,
        to: { name: 'ChildHome' },
      },
      {
        label: '아이 계좌 등록',
        description: '아이 명의 계좌를 등록해요.',
        keywords: ['자녀 계좌', '은행 계좌'],
        icon: Landmark,
        to: { name: 'ChildAccountCreate' },
      },
      {
        label: '최근 돈 기록',
        description: '아이의 최근 입출금 기록을 확인해요.',
        keywords: ['아이 자산', '거래 내역'],
        icon: WalletCards,
        to: { name: 'ChildAssets' },
      },
      {
        label: '용돈 조르기',
        description: '보호자에게 용돈을 요청해요.',
        keywords: ['용돈 요청', '아이'],
        icon: Baby,
        to: { name: 'ChildAllowance' },
      },
      {
        label: '용돈 요청 내역',
        description: '보호자에게 보낸 용돈 요청을 확인해요.',
        keywords: ['용돈 내역', '요청 목록'],
        icon: CircleCheckBig,
        to: { name: 'ChildAllowanceRequests' },
      },
      {
        label: '아이 용돈 미션',
        description: '부모님이 보내준 미션을 수행하고 보상받아요.',
        keywords: ['아이 미션', '보상', '용돈'],
        icon: CircleCheckBig,
        to: { name: 'ChildMissions' },
      },
      {
        label: '아이 체크리스트',
        description: '오늘 해야 할 체크리스트를 확인해요.',
        keywords: ['아이 할 일', '체크'],
        icon: ClipboardCheck,
        to: { name: 'ChildChecklists' },
      },
      {
        label: '금융 퀴즈',
        description: '재미있는 퀴즈로 금융 지식을 익혀요.',
        keywords: ['아이 퀴즈', '금융 교육'],
        icon: Sparkles,
        to: { name: 'ChildQuiz' },
      },
      {
        label: '아이 이체하기',
        description: '아이 계좌에서 안전하게 이체해요.',
        keywords: ['아이 송금', '계좌 이체'],
        icon: WalletCards,
        to: { name: 'ChildTransfer' },
      },
    ],
  },
]

const query = ref('')
const normalizedQuery = computed(() => query.value.trim().toLocaleLowerCase('ko-KR'))

const filteredGroups = computed(() => {
  if (!normalizedQuery.value) return menuGroups

  return menuGroups
    .map((group) => ({
      ...group,
      items: group.items.filter((item) =>
        [group.title, item.label, item.description, ...item.keywords]
          .join(' ')
          .toLocaleLowerCase('ko-KR')
          .includes(normalizedQuery.value),
      ),
    }))
    .filter((group) => group.items.length > 0)
})

const totalMenuCount = computed(() =>
  menuGroups.reduce((sum, group) => sum + group.items.length, 0),
)
const resultCount = computed(() =>
  filteredGroups.value.reduce((sum, group) => sum + group.items.length, 0),
)
</script>

<template>
  <main
    class="min-h-[calc(100dvh-var(--app-header-height)-var(--app-bottom-nav-height))] bg-[var(--color-surface)] px-5 pt-5 pb-8 text-[var(--color-text-primary)]"
  >
    <section aria-labelledby="all-menu-title">
      <div class="flex justify-between">
        <h1
          id="all-menu-title"
          class="mb-0 text-[24px] leading-[1.35] font-extrabold tracking-[-0.035em]"
        >
          서비스 한눈에 보기
        </h1>

        <p class="mt-3 mb-0 text-[11px] text-[var(--color-text-secondary)]" aria-live="polite">
          {{ query ? `${resultCount}개의 검색 결과` : `총 ${totalMenuCount}개의 메뉴` }}
        </p>
      </div>

      <label class="relative mt-5 block">
        <span class="sr-only">메뉴 검색</span>
        <Search
          class="pointer-events-none absolute top-1/2 left-4 -translate-y-1/2 text-[var(--color-text-secondary)]"
          :size="20"
          aria-hidden="true"
        />
        <input
          v-model="query"
          class="h-13 w-full rounded-[15px] border border-[var(--color-border)] pr-12 pl-12 text-[14px] font-medium outline-none transition-colors placeholder:text-[var(--color-text-secondary)] [&::-webkit-search-cancel-button]:hidden [&::-webkit-search-cancel-button]:appearance-none"
          type="search"
          inputmode="search"
          autocomplete="off"
          placeholder="메뉴명 또는 기능을 검색해 보세요"
        />
        <button
          v-if="query"
          class="absolute top-1/2 right-2 grid size-9 -translate-y-1/2 place-items-center rounded-full border-0 bg-transparent text-[var(--color-text-secondary)] transition-colors active:bg-[#f3f6f8]"
          type="button"
          aria-label="검색어 지우기"
          @click="query = ''"
        >
          <X :size="18" />
        </button>
      </label>
    </section>

    <div v-if="filteredGroups.length" class="mt-7 grid gap-8">
      <section
        v-for="group in filteredGroups"
        :key="group.title"
        :aria-labelledby="`menu-group-${group.title}`"
      >
        <div class="flex items-end justify-between gap-3 px-1">
          <h2 :id="`menu-group-${group.title}`" class="m-0 text-[16px] font-extrabold">
            {{ group.title }}
          </h2>
        </div>

        <ul
          class="mt-3 mb-0 list-none divide-y divide-[var(--color-border)] overflow-hidden rounded-[18px] border border-[var(--color-border)] bg-white px-4 py-0 shadow-sm"
        >
          <li v-for="item in group.items" :key="item.label">
            <RouterLink
              class="flex min-h-[76px] items-center gap-3 py-3 !text-[var(--color-text-primary)] active:bg-[var(--color-surface-muted)]"
              :to="item.to"
            >
              <span
                class="grid size-10 shrink-0 place-items-center rounded-[13px]"
                :class="group.iconClass"
                aria-hidden="true"
              >
                <component :is="item.icon" :size="20" :stroke-width="2.2" />
              </span>
              <span class="min-w-0 flex-1">
                <strong class="block text-[14px] font-extrabold">{{ item.label }}</strong>
                <span class="mt-1 block truncate text-[11px] text-[var(--color-text-secondary)]">
                  {{ item.description }}
                </span>
              </span>
              <ChevronRight
                class="shrink-0 text-[var(--color-text-secondary)]"
                :size="18"
                aria-hidden="true"
              />
            </RouterLink>
          </li>
        </ul>
      </section>
    </div>

    <section
      v-else
      class="mt-8 rounded-[18px] bg-[var(--color-surface-muted)] px-5 py-14 text-center"
      role="status"
    >
      <h2 class="mb-0 text-[15px] font-extrabold">검색 결과가 없어요</h2>
      <p class="mt-2 mb-0 text-[12px] text-[var(--color-text-secondary)]">
        다른 메뉴명이나 기능으로 검색해 주세요.
      </p>
    </section>
  </main>
</template>
