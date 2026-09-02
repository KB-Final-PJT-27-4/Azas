import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { AUTH_EXPIRED_EVENT } from './api/http'
import 'pretendard/dist/web/static/pretendard.css'
import './styles/base.css'

const app = createApp(App)
const pinia = createPinia()
const DYNAMIC_IMPORT_RELOAD_KEY = 'azas:dynamic-import-reload'

const isDynamicImportFailure = (error: unknown) =>
  error instanceof Error
  && /(?:dynamically imported module|failed to fetch dynamically imported module|importing a module script failed)/i.test(error.message)

app.use(pinia)
app.use(router)

window.addEventListener(AUTH_EXPIRED_EVENT, () => {
  const redirect = router.currentRoute.value.fullPath
  void router.replace({ name: 'Login', query: { redirect } })
})

// 배포 직후 열려 있던 탭은 이전 해시의 lazy-load 청크를 요청할 수 있다.
// 최신 index.html을 한 번만 다시 받아오도록 해 화면 전체가 멈추는 것을 막는다.
router.onError((error) => {
  if (!isDynamicImportFailure(error) || sessionStorage.getItem(DYNAMIC_IMPORT_RELOAD_KEY)) return
  sessionStorage.setItem(DYNAMIC_IMPORT_RELOAD_KEY, '1')
  window.location.reload()
})

router.afterEach(() => {
  sessionStorage.removeItem(DYNAMIC_IMPORT_RELOAD_KEY)
})

// 현재 URL의 라우트와 meta가 확정되기 전에 레이아웃이 먼저 그려지면
// 로그인 화면에서도 공통 헤더/하단 내비게이션이 순간적으로 노출된다.
router.isReady().then(() => {
  app.mount('#app')
})
