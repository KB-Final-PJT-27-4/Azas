<script setup lang="ts">
import googleLoginImage from '@/assets/images/login/google-login.png'
import kakaoLoginImage from '@/assets/images/login/kakao_login.png'
</script>

<template>
  <main class="login-view">
    <section class="login-hero" aria-labelledby="login-title">
      <div class="login-hero__copy">
        <p class="login-hero__service">
          <strong>우리 아이</strong>
          <span>자산 관리 서비스</span>
        </p>

        <h1 id="login-title" class="login-hero__title">
          아이의 오늘을 기록하고
          <strong>내일의 자산을 키워요</strong>
        </h1>
      </div>
    </section>

    <section class="login-panel" aria-labelledby="login-panel-title">
      <div class="login-panel__heading">
        <p class="login-panel__welcome" aria-hidden="true">
          <span>♥</span>
          안녕하세요!
          <span>♥</span>
        </p>
        <h2 id="login-panel-title">우리 아이의 금융성장을 시작해요</h2>
        <p>간편하게 로그인하고 필요한 정보를 차근차근 채워볼게요.</p>
      </div>

      <div class="login-actions">
        <button class="login-button login-button--google" type="button">
          <img :src="googleLoginImage" alt="" />
          <span>Google로 시작하기</span>
        </button>

        <button class="login-button login-button--kakao" type="button">
          <img :src="kakaoLoginImage" alt="" />
          <span>카카오로 시작하기</span>
        </button>
      </div>
    </section>
  </main>
</template>

<style scoped>
.login-view {
  min-height: 100dvh;
  overflow: hidden;
  color: #102b50;
  background: #eaf7ff;
}

.login-hero {
  position: relative;
  min-height: 420px;
  padding: 62px 32px 48px;
  background-image: url('@/assets/images/login/bg-hero.png');
  background-position: center bottom;
  background-size: cover;
}

.login-hero__copy {
  position: relative;
  z-index: 1;
}

.login-hero__service {
  display: grid;
  gap: 4px;
  margin: 0 0 30px;
  font-size: 18px;
  font-weight: 650;
  line-height: 1.35;
}

.login-hero__service strong {
  font-size: 25px;
  font-weight: 800;
}

.login-hero__title {
  display: grid;
  gap: 2px;
  margin: 0;
  font-size: clamp(29px, 8.2vw, 38px);
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: -0.045em;
}

.login-hero__title strong {
  color: #438be7;
}

.login-panel {
  position: relative;
  z-index: 2;
  min-height: 500px;
  margin-top: -32px;
  padding: 52px 24px 190px;
  text-align: center;
  background-color: #fff;
  background-image: url('@/assets/images/login/bg-default.png');
  background-repeat: no-repeat;
  background-position: center bottom;
  background-size: auto 195px;
}

.login-panel__heading {
  display: grid;
  gap: 12px;
}

.login-panel__welcome {
  display: flex;
  gap: 10px;
  align-items: center;
  justify-content: center;
  margin: 0;
  color: #438be7;
  font-size: 20px;
  font-weight: 750;
}

.login-panel__welcome span {
  color: #77b9f4;
  font-size: 12px;
}

.login-panel h2 {
  margin: 0;
  color: #10213c;
  font-size: 23px;
  line-height: 1.4;
  letter-spacing: -0.035em;
}

.login-panel__heading > p:last-child {
  margin: 0;
  color: #687587;
  font-size: 15px;
  line-height: 1.6;
  letter-spacing: -0.025em;
}

.login-actions {
  display: grid;
  gap: 16px;
  margin-top: 48px;
}

.login-button {
  width: 100%;
  height: 64px;
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  border: 0;
  border-radius: 999px;
  transition:
    transform 120ms ease,
    filter 120ms ease;
}

.login-button:active {
  transform: scale(0.985);
}

.login-button:focus-visible {
  outline: 3px solid rgb(67 139 231 / 35%);
  outline-offset: 3px;
}

.login-button--google {
  display: grid;
  grid-template-columns: 42px 1fr 42px;
  align-items: center;
  padding: 0 20px;
  color: #132239;
  font-size: 18px;
  font-weight: 700;
  background: #fff;
  border: 1px solid #d9dde3;
  box-shadow: 0 4px 10px rgb(31 48 69 / 12%);
}

.login-button--google img {
  width: 36px;
  height: 36px;
}

.login-button--google svg {
  justify-self: end;
  color: #687587;
}

.login-button--kakao {
  display: grid;
  grid-template-columns: 42px 1fr 42px;
  align-items: center;
  padding: 0 20px;
  color: #171600;
  font-size: 18px;
  font-weight: 700;
  background: #fee500;
  box-shadow: 0 4px 10px rgb(31 48 69 / 10%);
}

.login-button--kakao img {
  width: 36px;
  height: 36px;
}

@media (max-width: 360px) {
  .login-hero {
    min-height: 380px;
    padding: 44px 24px;
  }

  .login-hero__service {
    margin-bottom: 22px;
  }

  .login-panel {
    padding-inline: 18px;
  }

  .login-panel h2 {
    font-size: 20px;
  }

  .login-button--google,
  .login-button--kakao {
    font-size: 16px;
  }
}
</style>
