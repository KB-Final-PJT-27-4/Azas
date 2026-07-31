<script setup lang="ts">
withDefaults(
  defineProps<{
    open: boolean
    title?: string
  }>(),
  {
    title: undefined,
  },
)

const emit = defineEmits<{
  close: []
}>()
</script>

<template>
  <Teleport to="body">
    <div v-if="open" class="base-bottom-sheet" role="presentation" @click.self="emit('close')">
      <section class="base-bottom-sheet__panel" role="dialog" aria-modal="true" :aria-label="title">
        <div class="base-bottom-sheet__handle" aria-hidden="true"></div>
        <header v-if="title || $slots.header" class="base-bottom-sheet__header">
          <slot name="header">
            <h2>{{ title }}</h2>
          </slot>
        </header>
        <slot />
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.base-bottom-sheet {
  position: fixed;
  inset: 0;
  z-index: var(--z-index-overlay);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  background: rgb(51 51 51 / 40%);
}

.base-bottom-sheet__panel {
  width: 100%;
  max-width: var(--app-max-width);
  padding: var(--space-4) var(--space-5) calc(var(--space-5) + env(safe-area-inset-bottom));
  color: var(--color-text-secondary);
  background: var(--color-surface);
  border-radius: var(--radius-lg) var(--radius-lg) 0 0;
}

.base-bottom-sheet__handle {
  width: 40px;
  height: 4px;
  margin: 0 auto var(--space-4);
  background: var(--color-border);
  border-radius: 999px;
}

.base-bottom-sheet__header h2 {
  margin: 0 0 var(--space-3);
  color: var(--color-text-primary);
  font-size: var(--font-size-lg);
}
</style>
