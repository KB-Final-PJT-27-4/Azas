<script setup lang="ts">
withDefaults(
  defineProps<{
    type?: 'button' | 'submit' | 'reset'
    variant?: 'primary' | 'secondary' | 'ghost'
    disabled?: boolean
  }>(),
  {
    type: 'button',
    variant: 'primary',
    disabled: false,
  },
)
</script>

<template>
  <button class="base-button" :class="`base-button--${variant}`" :type="type" :disabled="disabled">
    <slot />
  </button>
</template>

<style scoped>
.base-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
  padding: 0 var(--space-5);
  border: 1px solid transparent;
  border-radius: var(--radius-md);
  font-size: var(--font-size-md);
  font-weight: 700;
  cursor: pointer;
}

.base-button--primary {
  color: var(--color-text-inverse);
  background: var(--color-brand-primary);
}

.base-button--secondary {
  color: var(--color-selected-text);
  background: var(--color-selected-background);
}

.base-button--ghost {
  color: var(--color-unselected-text);
  background: var(--color-unselected-background);
}

.base-button:disabled {
  color: var(--color-unselected-text);
  cursor: not-allowed;
  background: var(--color-disabled-background);
  border-color: var(--color-disabled-border);
}
</style>
