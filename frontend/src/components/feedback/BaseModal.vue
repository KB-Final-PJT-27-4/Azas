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
    <div v-if="open" class="base-modal" role="presentation" @click.self="emit('close')">
      <section class="base-modal__panel" role="dialog" aria-modal="true" :aria-label="title">
        <header v-if="title || $slots.header" class="base-modal__header">
          <slot name="header">
            <h2>{{ title }}</h2>
          </slot>
        </header>
        <div class="base-modal__body">
          <slot />
        </div>
        <footer v-if="$slots.footer" class="base-modal__footer">
          <slot name="footer" />
        </footer>
      </section>
    </div>
  </Teleport>
</template>

<style scoped>
.base-modal {
  position: fixed;
  inset: 0;
  z-index: var(--z-index-overlay);
  display: grid;
  place-items: center;
  padding: var(--space-5);
  background: rgb(51 51 51 / 40%);
}

.base-modal__panel {
  width: min(100%, 360px);
  padding: var(--space-5);
  background: var(--color-surface);
  border-radius: var(--radius-lg);
}

.base-modal__header h2 {
  margin: 0 0 var(--space-3);
  font-size: var(--font-size-lg);
}

.base-modal__body {
  color: var(--color-text-secondary);
  line-height: 1.6;
}

.base-modal__footer {
  display: flex;
  gap: var(--space-2);
  justify-content: flex-end;
  margin-top: var(--space-5);
}
</style>
