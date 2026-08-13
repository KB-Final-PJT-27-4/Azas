import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

const timeCapsuleHeaderMeta = {
  requiresAuth: true,
  headerTitle: '타임캡슐',
  showHeaderBack: true,
  showHeaderNotification: false,
}

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Root',
    redirect: '/login',
  },

  // 인증
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { requiresAuth: false, hideNavigation: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: { requiresAuth: false, hideNavigation: true },
  },
  {
    path: '/onboarding',
    name: 'Onboarding',
    component: () => import('@/views/auth/OnboardingView.vue'),
    meta: { requiresAuth: true, hideNavigation: true },
  },

  // 홈
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/home/HomeView.vue'),
    meta: { requiresAuth: true },
  },

  // 목표
  {
    path: '/goals',
    name: 'Goals',
    component: () => import('@/views/goals/GoalsView.vue'),
    meta: { requiresAuth: true, hideNavigation: true },
  },

  // 계좌
  {
    path: '/accounts',
    name: 'Accounts',
    component: () => import('@/views/accounts/AccountsView.vue'),
    meta: {
      requiresAuth: true,
      headerTitle: '계좌 연동',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },

  // 자산
  {
    path: '/assets',
    name: 'Assets',
    component: () => import('@/views/assets/AssetsView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/assets/:assetId',
    name: 'AssetDetail',
    component: () => import('@/views/assets/AssetDetailView.vue'),
    props: true,
    meta: { requiresAuth: true },
  },

  // 타임캡슐
  {
    path: '/time-capsules',
    name: 'TimeCapsuleArchive',
    component: () => import('@/views/timeCapsules/TimeCapsuleArchiveView.vue'),
    meta: timeCapsuleHeaderMeta,
  },
  {
    path: '/time-capsules/new',
    name: 'TimeCapsuleCreate',
    component: () => import('@/views/timeCapsules/TimeCapsuleCreateView.vue'),
    meta: timeCapsuleHeaderMeta,
  },
  {
    path: '/time-capsules/preview',
    name: 'TimeCapsulePreview',
    component: () => import('@/views/timeCapsules/TimeCapsulePreviewView.vue'),
    meta: timeCapsuleHeaderMeta,
  },
  ...(import.meta.env.DEV
    ? [
        {
          path: '/__dev/time-capsules-api-test',
          name: 'TimeCapsuleApiTest',
          component: () => import('@/views/timeCapsules/TimeCapsuleApiTestView.vue'),
          meta: { ...timeCapsuleHeaderMeta, hideNavigation: true },
        },
      ]
    : []),
  {
    path: '/time-capsules/:capsuleId/edit',
    name: 'TimeCapsuleEdit',
    component: () => import('@/views/timeCapsules/TimeCapsuleEditView.vue'),
    props: true,
    meta: timeCapsuleHeaderMeta,
  },
  {
    path: '/time-capsules/:capsuleListId',
    name: 'TimeCapsuleList',
    component: () => import('@/views/timeCapsules/TimeCapsuleListView.vue'),
    props: true,
    meta: timeCapsuleHeaderMeta,
  },
  {
    path: '/time-capsules/:capsuleListId/open',
    name: 'TimeCapsuleOpen',
    component: () => import('@/views/timeCapsules/TimeCapsuleOpenView.vue'),
    props: true,
    meta: { requiresAuth: true, hideNavigation: true },
  },
  {
    path: '/time-capsules/:capsuleListId/:capsuleId',
    name: 'TimeCapsuleDetail',
    component: () => import('@/views/timeCapsules/TimeCapsuleDetailView.vue'),
    props: true,
    meta: timeCapsuleHeaderMeta,
  },

  // 마이페이지
  {
    path: '/mypage',
    name: 'Mypage',
    component: () => import('@/views/mypage/MypageView.vue'),
    meta: { requiresAuth: true, headerTitle: '마이페이지' },
  },
  {
    path: '/mypage/edit',
    name: 'MypageEdit',
    component: () => import('@/views/mypage/MypageEditView.vue'),
    meta: {
      requiresAuth: true,
      hideBottomNavigation: true,
      headerTitle: '마이페이지',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },
  {
    path: '/mypage/child-edit',
    name: 'ChildEdit',
    component: () => import('@/views/mypage/ChildEditView.vue'),
    meta: {
      requiresAuth: true,
      hideBottomNavigation: true,
      headerTitle: '마이페이지',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },
  {
    path: '/mypage/family',
    name: 'FamilyManagement',
    component: () => import('@/views/mypage/FamilyManagementView.vue'),
    meta: {
      requiresAuth: true,
      hideBottomNavigation: true,
      headerTitle: '마이페이지',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },
  {
    path: '/mypage/permissions',
    name: 'ParentPermissions',
    component: () => import('@/views/mypage/ParentPermissionsView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['PARENT'],
      hideBottomNavigation: true,
      headerTitle: '아이 이용 권한 설정',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },
  {
    path: '/mypage/accounts',
    name: 'MypageAccounts',
    component: () => import('@/views/mypage/MypageAccountsView.vue'),
    meta: {
      requiresAuth: true,
      hideBottomNavigation: true,
      headerTitle: '마이페이지',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },
  {
    path: '/mypage/goals',
    name: 'MypageGoals',
    component: () => import('@/views/mypage/MypageGoalsView.vue'),
    meta: {
      requiresAuth: true,
      hideBottomNavigation: true,
      headerTitle: '마이페이지',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },
  {
    path: '/mypage/goals/:goalsId',
    name: 'MypageGoalEdit',
    component: () => import('@/views/mypage/MypageGoalEditView.vue'),
    props: true,
    meta: {
      requiresAuth: true,
      hideBottomNavigation: true,
      headerTitle: '마이페이지',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },
  {
    path: '/mypage/alarm',
    name: 'AlarmSettings',
    component: () => import('@/views/mypage/AlarmSettingsView.vue'),
    meta: {
      requiresAuth: true,
      hideBottomNavigation: true,
      headerTitle: '마이페이지',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },
  {
    path: '/mypage/guide',
    name: 'Guide',
    component: () => import('@/views/mypage/GuideView.vue'),
    meta: {
      requiresAuth: true,
      hideBottomNavigation: true,
      headerTitle: '도움말',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },

  // 알림
  {
    path: '/alarm',
    name: 'Alarm',
    component: () => import('@/views/alarm/AlarmView.vue'),
    meta: {
      requiresAuth: true,
      hideBottomNavigation: true,
      headerTitle: '알림',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },
  {
    path: '/allowance-requests/:requestId',
    name: 'AllowanceRequest',
    component: () => import('@/views/allowance/AllowanceRequestView.vue'),
    props: true,
    meta: {
      requiresAuth: true,
      roles: ['PARENT'],
      hideBottomNavigation: true,
      headerTitle: '용돈 요청',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },

  // 아이
  {
    path: '/child/accounts',
    name: 'ChildAccountCreate',
    component: () => import('@/views/child/ChildAccountCreateView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['CHILD'],
      headerTitle: '아이계좌등록',
      showHeaderBack: true,
      showHeaderNotification: false,
      hideBottomNavigation: true,
    },
  },
  {
    path: '/child/home',
    name: 'ChildHome',
    component: () => import('@/views/child/ChildHomeView.vue'),
    meta: { requiresAuth: true, roles: ['CHILD'], hideBottomNavigation: true, headerTitle: '홈' },
  },
  {
    path: '/child/assets',
    name: 'ChildAssets',
    component: () => import('@/views/child/ChildAssetsView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['CHILD'],
      hideBottomNavigation: true,
      headerTitle: '최근 돈 기록',
      showHeaderBack: true,
    },
  },
  {
    path: '/child/allowance',
    name: 'ChildAllowance',
    component: () => import('@/views/child/ChildAllowanceView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['CHILD'],
      hideBottomNavigation: true,
      headerTitle: '용돈 조르기',
      showHeaderBack: true,
    },
  },
  {
    path: '/child/allowance-done',
    name: 'ChildAllowanceDone',
    component: () => import('@/views/child/ChildAllowanceDoneView.vue'),
    meta: {
      requiresAuth: true,
      roles: ['CHILD'],
      hideBottomNavigation: true,
      headerTitle: '용돈 조르기',
      showHeaderBack: false,
    },
  },

  // 자산 리포트
  {
    path: '/reports',
    name: 'Reports',
    component: () => import('@/views/reports/ReportsView.vue'),
    meta: { requiresAuth: true },
  },

  // 체크리스트
  {
    path: '/checklists',
    name: 'Checklists',
    component: () => import('@/views/checklists/ChecklistsView.vue'),
    meta: { requiresAuth: true },
  },

  // 전체 메뉴
  {
    path: '/menu',
    name: 'AllMenu',
    component: () => import('@/views/menu/AllMenuView.vue'),
    meta: {
      requiresAuth: true,
      headerTitle: '전체 메뉴',
      showHeaderNotification: false,
    },
  },

  // 금융상품
  {
    path: '/savings-recommendations',
    name: 'SavingsRecommendation',
    component: () => import('@/views/products/SavingsRecommendationView.vue'),
    meta: {
      requiresAuth: true,
      hideBottomNavigation: true,
      headerTitle: '적금 추천',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },
  {
    path: '/savings-recommendations/complete',
    name: 'SavingsOpenComplete',
    component: () => import('@/views/products/SavingsOpenCompleteView.vue'),
    meta: {
      requiresAuth: true,
      hideBottomNavigation: true,
      headerTitle: '적금 개설 완료',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },
  {
    path: '/products',
    name: 'Products',
    component: () => import('@/views/products/ProductsView.vue'),
    meta: {
      requiresAuth: true,
      hideBottomNavigation: true,
      headerTitle: '금융상품 추천',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },
  {
    path: '/products/:productId',
    name: 'ProductDetail',
    component: () => import('@/views/products/ProductDetailView.vue'),
    props: true,
    meta: {
      requiresAuth: true,
      hideBottomNavigation: true,
      headerTitle: '상품 상세',
      showHeaderBack: true,
      showHeaderNotification: false,
    },
  },

  // 404
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/common/NotFoundView.vue'),
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

export default router
