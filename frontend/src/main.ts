import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { AUTH_EXPIRED_EVENT } from './api/http'
import 'pretendard/dist/web/static/pretendard.css'
import './styles/base.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

window.addEventListener(AUTH_EXPIRED_EVENT, () => {
  const redirect = router.currentRoute.value.fullPath
  void router.replace({ name: 'Login', query: { redirect } })
})

// 현재 URL의 라우트와 meta가 확정되기 전에 레이아웃이 먼저 그려지면
// 로그인 화면에서도 공통 헤더/하단 내비게이션이 순간적으로 노출된다.
router.isReady().then(() => {
  app.mount('#app')
})
