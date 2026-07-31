<template>
  <div class="base-loading-state" role="status" aria-live="polite">
    <span class="base-loading-state__spinner" aria-hidden="true"></span>
    <p>불러오는 중입니다</p>
  </div>
</template>

<style scoped>
.base-loading-state {
  display: grid;
  place-items: center;
  gap: var(--space-3);
  min-height: 160px;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.base-loading-state p {
  margin: 0;
}

.base-loading-state__spinner {
  width: 28px;
  height: 28px;
  border: 3px solid var(--color-border);
  border-top-color: var(--color-brand-primary);
  border-radius: 999px;
  animation: base-loading-spin 0.8s linear infinite;
}

@keyframes base-loading-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
