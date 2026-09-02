import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import vueDevTools from 'vite-plugin-vue-devtools'
import { VitePWA } from 'vite-plugin-pwa'

const apiTargetUrl = process.env.API_TARGET_URL ?? 'http://127.0.0.1:8080'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, fileURLToPath(new URL('.', import.meta.url)), '')

  return {
    plugins: [
      vue(),
      vueDevTools(),
      tailwindcss(),
      VitePWA({
        registerType: 'autoUpdate',
        injectRegister: 'auto',
        includeAssets: ['apple-touch-icon.png'],
        manifest: {
          id: '/',
          name: '아자스',
          short_name: '아자스',
          description: '부모와 자녀가 함께 만드는 미래 자산 관리 서비스',
          lang: 'ko-KR',
          theme_color: '#38bdf8',
          background_color: '#f8fbff',
          display: 'standalone',
          start_url: '/',
          scope: '/',
          icons: [
            {
              src: '/pwa-192x192.png',
              sizes: '192x192',
              type: 'image/png',
              purpose: 'any',
            },
            {
              src: '/pwa-512x512.png',
              sizes: '512x512',
              type: 'image/png',
              purpose: 'any maskable',
            },
          ],
        },
        workbox: {
          navigateFallback: '/index.html',
        },
      }),
    ],
    server: {
      proxy: {
        // Keep browser requests same-origin and keep the upstream host in env.
        '/api': {
          target: env.API_TARGET_URL || 'http://127.0.0.1:8080',
          changeOrigin: true,
        },
      },
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
  }
})
