<script setup lang="ts">
import { useAccountStore } from '@/stores/account'

const emit = defineEmits<{
  edit: []
  complete: []
}>()

const accountStore = useAccountStore()

async function registerAccount(): Promise<void> {
  try {
    // TODO: 실제 계좌 등록 API 호출
    // await accountApi.register({
    //   bankCode: accountStore.form.bankCode,
    //   accountNumber: accountStore.form.accountNumber,
    //   accountAlias: accountStore.form.accountAlias,
    // })

    emit('complete')
  } catch (error) {
    console.error('계좌 등록 실패', error)
  }
}
</script>

<template>
  <section class="step">
    <div class="step-indicator">
      <span class="step-indicator__bar" />
      <span class="step-indicator__bar is-active" />
    </div>

    <h1>입력한 정보를 확인해주세요</h1>
    <p class="description">등록한 계좌 정보가 맞는지 확인해주세요.</p>

    <dl class="account-summary">
      <div>
        <dt>은행</dt>
        <dd>{{ accountStore.form.bankName }}</dd>
      </div>

      <div>
        <dt>계좌번호</dt>
        <dd>{{ accountStore.form.accountNumber }}</dd>
      </div>

      <div>
        <dt>계좌별칭</dt>
        <dd>{{ accountStore.form.accountAlias }}</dd>
      </div>
    </dl>

    <div class="bottom-actions">
      <button class="button button--secondary" @click="emit('edit')">수정하기</button>

      <button class="button button--primary" @click="registerAccount">등록하기</button>
    </div>
  </section>
</template>
