<script setup lang="ts">
withDefaults(
  defineProps<{
    modelValue: boolean
    label: string
    disabled?: boolean
  }>(),
  {
    disabled: false,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>

<template>
  <label class="base-checkbox" :class="{ 'base-checkbox--disabled': disabled }">
    <input
      class="base-checkbox__input"
      type="checkbox"
      :checked="modelValue"
      :disabled="disabled"
      @change="emit('update:modelValue', ($event.target as HTMLInputElement).checked)"
    />
    <span class="base-checkbox__box" aria-hidden="true"></span>
    <span>{{ label }}</span>
  </label>
</template>

<style scoped>
.base-checkbox {
  display: inline-grid;
  grid-template-columns: 20px 1fr;
  gap: var(--space-2);
  align-items: center;
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: 600;
  cursor: pointer;
}

.base-checkbox__input {
  position: absolute;
  opacity: 0;
}

.base-checkbox__box {
  width: 20px;
  height: 20px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
}

.base-checkbox__input:checked + .base-checkbox__box {
  background: var(--color-selected-text);
  border-color: var(--color-selected-text);
  box-shadow: inset 0 0 0 4px var(--color-surface);
}

.base-checkbox--disabled {
  color: var(--color-unselected-text);
  cursor: not-allowed;
}
</style>
