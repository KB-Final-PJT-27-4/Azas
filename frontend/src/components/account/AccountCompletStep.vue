<script setup lang="ts">
import { useRouter } from 'vue-router'

import { useAccountStore } from '@/stores/account'

const router = useRouter()
const accountStore = useAccountStore()

async function createGoalPlan(): Promise<void> {
  accountStore.resetAccountForm()
  await router.push('/goal/create')
}
</script>

<template>
  <section class="complete-step">
    <div>
      <h1>계좌 등록이 완료되었어요!</h1>
      <p>
        이제 아이의 저축 목표를 설정하고<br />
        함께 성장해봐요
      </p>
    </div>

    <img
      src="@/assets/images/account-complete-pig.png"
      alt="계좌 등록 완료"
      class="complete-step__image"
    />

    <div class="complete-step__notice">
      등록된 계좌로 목표 저축과<br />
      저축 기록을 관리할 수 있어요.
    </div>

    <button class="button button--primary" @click="createGoalPlan">맞춤 플랜 생성하기</button>
  </section>
</template>
