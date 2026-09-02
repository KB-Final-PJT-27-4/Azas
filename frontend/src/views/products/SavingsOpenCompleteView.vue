<script setup lang="ts">
import { Landmark } from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CompletionPigAnimation from '@/components/feedback/CompletionPigAnimation.vue'
import { getCurrentChild } from '@/api/context'

const router = useRouter()
const route = useRoute()
const availableProductNames = [
  'KB Young Youth 적금',
  'KB아이사랑적금',
  '내 아이를 위한 280일 적금',
] as const
const selectedProductName = computed(() => {
  const productName = String(route.query.product ?? '')
  return availableProductNames.find((name) => name === productName) ?? 'KB아이사랑적금'
})
const childName = ref('아이')

onMounted(async () => {
  try {
    const child = await getCurrentChild()
    childName.value = child.name?.trim() || '아이'
  } catch {
    // 완료 화면은 계좌 개설 결과를 우선 보여주므로 자녀명 조회 실패 시 일반 문구를 사용합니다.
  }
})
</script>

<template>
  <main class="flex min-h-[calc(100dvh-var(--app-header-height))] flex-col bg-white text-[var(--color-text-primary)]">
    <section class="flex flex-1 flex-col px-6 pb-[calc(24px+env(safe-area-inset-bottom))] text-center" aria-labelledby="savings-complete-title">
      <div class="my-auto w-full pb-6">
        <CompletionPigAnimation class="mx-auto" />

        <h1 id="savings-complete-title" class="mt-5 text-[28px] leading-tight font-extrabold tracking-[-0.035em]">
          자녀 적금이 개설되었어요!
        </h1>
        <p class="mt-3 text-sm leading-6 text-[var(--color-text-secondary)]">
          이제 {{ childName }}의 첫 적금을<br />서비스에 연결해볼까요?
        </p>

        <div class="mt-6 overflow-hidden rounded-[20px] border border-[var(--color-border)] bg-white text-left">
          <article class="flex items-center px-4 py-3.5" aria-label="개설된 적금 정보">
            <span class="grid size-12 shrink-0 place-items-center rounded-full bg-[#f1f3f4] text-[#8a9298]">
              <Landmark :size="23" :stroke-width="2" aria-hidden="true" />
            </span>
            <div class="ml-3 min-w-0 flex-1">
              <strong class="block truncate text-[15px] font-extrabold">{{ selectedProductName }}</strong>
              <span class="mt-1 block text-xs text-[var(--color-text-secondary)]">123-456-789012</span>
            </div>
            <strong class="ml-3 shrink-0 text-sm font-bold">0원</strong>
          </article>
        </div>
      </div>

      <button class="min-h-14 w-full rounded-xl bg-[var(--color-brand-primary)] text-base font-bold text-white transition-colors active:bg-[var(--color-brand-primary-pressed)]" type="button" @click="router.push({ name: 'Goals', query: route.query.resumeGoal === 'true' ? { resumeGoal: 'true' } : {} })">
        목표 이어서 설정하기
      </button>
    </section>
  </main>
</template>
