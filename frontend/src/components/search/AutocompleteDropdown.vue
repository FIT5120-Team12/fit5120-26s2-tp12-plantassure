<script setup lang="ts">
  import type { PlantSearchResult } from '@/types/plant';

  defineProps<{
    id: string;
    suggestions: PlantSearchResult[];
    isLoading: boolean;
    error: string | null;
    activeIndex: number;
  }>();

  defineEmits<{
    select: [suggestion: PlantSearchResult];
    activate: [index: number];
    retry: [];
  }>();
</script>

<template>
  <div :id="id" class="autocomplete-dropdown" role="listbox" aria-label="Plant suggestions">
    <div v-if="isLoading" class="autocomplete-dropdown__status" role="status">
      <i class="mdi mdi-loading mdi-spin" aria-hidden="true"></i>
      <span>Loading plant results…</span>
    </div>

    <div v-else-if="error" class="autocomplete-dropdown__error" role="alert">
      <span>{{ error }}</span>
      <button type="button" @click="$emit('retry')">Try again</button>
    </div>

    <div v-else-if="suggestions.length === 0" class="autocomplete-dropdown__status" role="status">
      No matches found
    </div>

    <button
      v-for="(suggestion, index) in suggestions"
      v-else
      :id="`${id}-option-${index}`"
      :key="suggestion.plantId"
      type="button"
      class="autocomplete-dropdown__option"
      :class="{ 'autocomplete-dropdown__option--active': activeIndex === index }"
      role="option"
      :aria-selected="activeIndex === index"
      tabindex="-1"
      @mousemove="$emit('activate', index)"
      @click="$emit('select', suggestion)"
    >
      <span v-if="suggestion.commonName" class="autocomplete-dropdown__common-name">
        {{ suggestion.commonName }}
      </span>
      <em class="autocomplete-dropdown__scientific-name">{{ suggestion.scientificName }}</em>
    </button>
  </div>
</template>

<style scoped>
  .autocomplete-dropdown {
    position: absolute;
    top: calc(100% + var(--space-xs));
    right: 0;
    left: 0;
    z-index: 5;
    max-height: min(320px, calc(100vh - 160px));
    overflow-x: hidden;
    overflow-y: auto;
    background: var(--color-surface);
    border: 1px solid var(--color-border-strong);
    border-radius: var(--radius-md);
    box-shadow: 0 8px 20px rgb(23 63 46 / 10%);
  }

  .autocomplete-dropdown__status,
  .autocomplete-dropdown__error {
    min-height: 56px;
    display: flex;
    align-items: center;
    gap: var(--space-sm);
    padding: var(--space-sm) var(--space-md);
    color: var(--color-ink-soft);
    font-size: 0.875rem;
  }

  .autocomplete-dropdown__error {
    justify-content: space-between;
    background: var(--color-error-soft);
  }

  .autocomplete-dropdown__error button {
    min-height: 44px;
    flex: 0 0 auto;
    padding-inline: var(--space-md);
    border: 1px solid var(--color-border-strong);
    border-radius: var(--radius-sm);
    background: var(--color-surface);
    color: var(--color-primary);
    font-weight: 600;
    cursor: pointer;
  }

  .autocomplete-dropdown__error button:hover {
    background: var(--color-surface-muted);
  }

  .autocomplete-dropdown__option {
    width: 100%;
    min-height: 64px;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    justify-content: center;
    padding: 10px var(--space-md);
    border: 0;
    border-bottom: 1px solid var(--color-border);
    background: var(--color-surface);
    color: var(--color-ink);
    text-align: left;
    cursor: pointer;
  }

  .autocomplete-dropdown__option:last-child {
    border-bottom: 0;
  }

  .autocomplete-dropdown__option:hover,
  .autocomplete-dropdown__option--active {
    background: var(--color-surface-muted);
  }

  .autocomplete-dropdown__common-name {
    font-size: 0.9375rem;
    font-weight: 600;
  }

  .autocomplete-dropdown__scientific-name {
    color: var(--color-ink-soft);
    font-size: 0.8125rem;
  }

  @media (max-width: 479px) {
    .autocomplete-dropdown__error {
      align-items: flex-start;
      flex-direction: column;
    }
  }
</style>
