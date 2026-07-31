<script setup lang="ts">
withDefaults(
  defineProps<{
    id?: string
    label?: string
    modelValue: string
    placeholder?: string
    type?: 'text' | 'email' | 'password' | 'number' | 'tel'
    disabled?: boolean
    errorMessage?: string
  }>(),
  {
    id: undefined,
    label: undefined,
    placeholder: '',
    type: 'text',
    disabled: false,
    errorMessage: undefined,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <label class="base-input">
    <span v-if="label" class="base-input__label">{{ label }}</span>
    <input
      class="base-input__field"
      :id="id"
      :type="type"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      :aria-invalid="Boolean(errorMessage)"
      @input="emit('update:modelValue', ($event.target as HTMLInputElement).value)"
    />
    <span v-if="errorMessage" class="base-input__error">{{ errorMessage }}</span>
  </label>
</template>

<style scoped>
.base-input {
  display: grid;
  gap: var(--space-2);
}

.base-input__label {
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: 700;
}

.base-input__field {
  width: 100%;
  min-height: 48px;
  padding: 0 var(--space-4);
  color: var(--color-text-primary);
  background: var(--color-unselected-background);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}

.base-input__field:focus {
  outline: 2px solid var(--color-selected-background);
  border-color: var(--color-selected-text);
}

.base-input__field:disabled {
  cursor: not-allowed;
  background: var(--color-disabled-background);
  border-color: var(--color-disabled-border);
}

.base-input__error {
  color: #e5484d;
  font-size: var(--font-size-xs);
}
</style>
