<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    value: number
    max?: number
    label?: string
  }>(),
  {
    max: 100,
    label: undefined,
  },
)

const percentage = computed(() => Math.min(Math.max((props.value / props.max) * 100, 0), 100))
</script>

<template>
  <div class="base-progress-bar">
    <div v-if="label" class="base-progress-bar__label">
      <span>{{ label }}</span>
      <strong>{{ Math.round(percentage) }}%</strong>
    </div>
    <div class="base-progress-bar__track" role="progressbar" :aria-valuenow="value" aria-valuemin="0" :aria-valuemax="max">
      <span class="base-progress-bar__fill" :style="{ width: `${percentage}%` }"></span>
    </div>
  </div>
</template>

<style scoped>
.base-progress-bar {
  display: grid;
  gap: var(--space-2);
}

.base-progress-bar__label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
}

.base-progress-bar__label strong {
  color: var(--color-selected-text);
}

.base-progress-bar__track {
  height: 10px;
  overflow: hidden;
  background: var(--color-unselected-background);
  border-radius: 999px;
}

.base-progress-bar__fill {
  display: block;
  height: 100%;
  background: var(--color-brand-primary);
  border-radius: inherit;
}
</style>
