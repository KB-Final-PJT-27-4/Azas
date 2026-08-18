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
  <label class="base-toggle" :class="{ 'base-toggle--disabled': disabled }">
    <span>{{ label }}</span>
    <input
      class="base-toggle__input"
      type="checkbox"
      :checked="modelValue"
      :disabled="disabled"
      @change="emit('update:modelValue', ($event.target as HTMLInputElement).checked)"
    />
    <span class="base-toggle__track" aria-hidden="true">
      <span class="base-toggle__thumb"></span>
    </span>
  </label>
</template>

<style scoped>
.base-toggle {
  display: inline-flex;
  gap: var(--space-3);
  align-items: center;
  justify-content: space-between;
  color: var(--color-text-primary);
  font-size: var(--font-size-sm);
  font-weight: 600;
  cursor: pointer;
}

.base-toggle__input {
  position: absolute;
  opacity: 0;
}

.base-toggle__track {
  position: relative;
  width: 44px;
  height: 26px;
  background: var(--color-disabled-border);
  border-radius: 999px;
  transition: background-color 0.2s ease;
}

.base-toggle__thumb {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 20px;
  height: 20px;
  background: var(--color-surface);
  border-radius: 999px;
  transition: transform 0.2s ease;
}

.base-toggle__input:checked + .base-toggle__track {
  background: var(--color-selected-text);
}

.base-toggle__input:checked + .base-toggle__track .base-toggle__thumb {
  transform: translateX(18px);
}

.base-toggle--disabled {
  color: var(--color-unselected-text);
  cursor: not-allowed;
}
</style>
