<script setup lang="ts">
  import { ref } from 'vue';

  defineProps<{
    errorMessage: string | null;
  }>();

  const emit = defineEmits<{
    select: [file: File];
  }>();

  const input = ref<HTMLInputElement | null>(null);
  const inputId = 'plant-image-upload';
  const errorId = 'plant-image-upload-error';

  function openFileDialog() {
    input.value?.click();
  }

  function selectFile(event: Event) {
    const inputElement = event.target as HTMLInputElement;
    const file = inputElement.files?.[0];

    if (file) emit('select', file);
    inputElement.value = '';
  }

  defineExpose({ openFileDialog });
</script>

<template>
  <div class="plant-image-uploader">
    <label :for="inputId" class="visually-hidden">Upload a plant photo</label>
    <input
      :id="inputId"
      ref="input"
      class="visually-hidden"
      type="file"
      accept="image/jpeg,image/png,image/webp"
      :aria-describedby="errorMessage ? errorId : undefined"
      :aria-invalid="Boolean(errorMessage)"
      @change="selectFile"
    />

    <button
      class="plant-image-uploader__action"
      type="button"
      :aria-describedby="errorMessage ? errorId : undefined"
      @click="openFileDialog"
    >
      <v-icon icon="mdi-image-plus-outline" size="32" color="primary" aria-hidden="true" />
      <span class="plant-image-uploader__title">Upload a plant photo</span>
      <span class="plant-image-uploader__description">Click to choose an image from your device</span>
      <span class="plant-image-uploader__supporting">Supports JPEG, PNG and WebP images</span>
    </button>

    <p v-if="errorMessage" :id="errorId" class="plant-image-uploader__error" role="alert">
      {{ errorMessage }}
    </p>
  </div>
</template>

<style scoped>
  .plant-image-uploader__action {
    width: 100%;
    min-height: 15rem;
    display: grid;
    place-content: center;
    gap: var(--space-xs);
    padding: var(--space-xl);
    border: 1px dashed var(--color-border-strong);
    border-radius: var(--radius-md);
    background: var(--color-surface);
    color: var(--color-primary);
    text-align: center;
    cursor: pointer;
  }

  .plant-image-uploader__action:hover {
    border-color: var(--color-primary);
    background: var(--color-surface-muted);
  }

  .plant-image-uploader__title {
    font-family: var(--font-display);
    font-size: 1.5rem;
    font-weight: 500;
  }

  .plant-image-uploader__description {
    color: var(--color-ink-soft);
  }

  .plant-image-uploader__supporting {
    color: var(--color-muted);
    font-size: 0.8125rem;
  }

  .plant-image-uploader__error {
    margin: var(--space-sm) 0 0;
    color: var(--color-error);
    font-size: 0.875rem;
  }
</style>
