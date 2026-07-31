<script setup lang="ts">
export interface SelectOption {
  label: string
  value: string
}

withDefaults(
  defineProps<{
    label?: string
    modelValue: string
    options: SelectOption[]
    placeholder?: string
    disabled?: boolean
    errorMessage?: string
  }>(),
  {
    label: undefined,
    placeholder: '선택해 주세요',
    disabled: false,
    errorMessage: undefined,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <label class="base-select">
    <span v-if="label" class="base-select__label">{{ label }}</span>
    <select
      class="base-select__field"
      :value="modelValue"
      :disabled="disabled"
      :aria-invalid="Boolean(errorMessage)"
      @change="emit('update:modelValue', ($event.target as HTMLSelectElement).value)"
    >
      <option value="" disabled>{{ placeholder }}</option>
      <option v-for="option in options" :key="option.value" :value="option.value">
        {{ option.label }}
      </option>
    </select>
    <span v-if="errorMessage" class="base-select__error">{{ errorMessage }}</span>
  </label>
</template>

<style scoped>
.base-select {
  display: grid;
  gap: var(--space-2);
}

.base-select__label {
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: 700;
}

.base-select__field {
  width: 100%;
  min-height: 48px;
  padding: 0 var(--space-4);
  color: var(--color-text-primary);
  background: var(--color-unselected-background);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.base-select__field:focus {
  outline: 2px solid var(--color-selected-background);
  border-color: var(--color-selected-text);
}

.base-select__field:disabled {
  cursor: not-allowed;
  background: var(--color-disabled-background);
  border-color: var(--color-disabled-border);
}

.base-select__error {
  color: #e5484d;
  font-size: var(--font-size-xs);
}
</style>
