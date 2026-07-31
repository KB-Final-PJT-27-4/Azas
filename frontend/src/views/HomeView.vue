<template>
  <main class="home-view">
    <section class="home-view__hero">
      <p class="home-view__eyebrow">우리 아이 금융일기</p>
      <h1>아이의 금융 준비를 한눈에 관리해요</h1>
      <p>목표, 자산, 체크리스트를 모바일 기준 화면에서 확인할 수 있습니다.</p>
    </section>
  </main>
</template>

<style scoped>
.home-view {
  padding: var(--space-5);
}

.home-view__hero {
  display: grid;
  gap: var(--space-3);
  padding: var(--space-6) 0;
}

.home-view__eyebrow {
  margin: 0;
  color: var(--color-brand-primary);
  font-size: var(--font-size-sm);
  font-weight: 700;
}

.home-view h1 {
  margin: 0;
  font-size: var(--font-size-2xl);
  line-height: 1.3;
}

.home-view p {
  margin: 0;
  color: var(--color-text-secondary);
  line-height: 1.6;
}
</style>
