<script setup lang="ts">
  import type { PlantSearchResult } from '@/types/plant';

  defineProps<{
    results: PlantSearchResult[];
    isLoading: boolean;
    error: string | null;
  }>();

  defineEmits<{
    select: [result: PlantSearchResult];
    retry: [];
  }>();
</script>

<template>
  <section class="plant-results" aria-labelledby="plant-results-title">
    <h2 id="plant-results-title">Search results</h2>

    <div v-if="isLoading" class="plant-results__status" role="status" aria-live="polite">
      <i class="mdi mdi-loading mdi-spin" aria-hidden="true"></i>
      <span>Searching plants…</span>
    </div>

    <div v-else-if="error" class="plant-results__error" role="alert">
      <span>{{ error }}</span>
      <button type="button" @click="$emit('retry')">Try again</button>
    </div>

    <p v-else-if="results.length === 0" class="plant-results__status" role="status">
      No matches found
    </p>

    <template v-else-if="results.length > 1">
      <p class="plant-results__summary">
        {{ results.length }} matches found. Select a plant to view its assessment.
      </p>
      <ul class="plant-results__list">
        <li v-for="result in results" :key="result.plantId">
          <a
            class="plant-results__link"
            :href="`/plants/${result.plantId}/assessment`"
            @click.prevent="$emit('select', result)"
          >
            <span class="plant-results__identity">
              <span v-if="result.commonName" class="plant-results__common-name">
                {{ result.commonName }}
              </span>
              <em>{{ result.scientificName }}</em>
            </span>
            <span class="plant-results__action">
              View assessment
              <i class="mdi mdi-arrow-right" aria-hidden="true"></i>
            </span>
          </a>
        </li>
      </ul>
    </template>
  </section>
</template>

<style scoped>
  .plant-results {
    margin-top: var(--space-md);
    padding: var(--space-md);
    background: var(--color-surface);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
  }

  .plant-results h2 {
    margin: 0 0 var(--space-sm);
    font-family: var(--font-body);
    font-size: 1rem;
    font-weight: 600;
  }

  .plant-results__status,
  .plant-results__error {
    min-height: 48px;
    display: flex;
    align-items: center;
    gap: var(--space-sm);
    margin: 0;
    color: var(--color-ink-soft);
    font-size: 0.875rem;
  }

  .plant-results__error {
    justify-content: space-between;
    padding: var(--space-sm);
    background: var(--color-error-soft);
    border-radius: var(--radius-sm);
  }

  .plant-results__error button {
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

  .plant-results__error button:hover {
    background: var(--color-surface-muted);
  }

  .plant-results__summary {
    margin: 0 0 var(--space-sm);
    color: var(--color-ink-soft);
    font-size: 0.8125rem;
  }

  .plant-results__list {
    display: grid;
    gap: var(--space-xs);
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .plant-results__link {
    min-height: 64px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--space-md);
    padding: var(--space-sm) var(--space-md);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-sm);
    color: var(--color-ink);
    text-decoration: none;
    cursor: pointer;
  }

  .plant-results__link:hover {
    background: var(--color-surface-muted);
    border-color: var(--color-border-strong);
  }

  .plant-results__identity {
    min-width: 0;
    display: flex;
    flex-direction: column;
    overflow-wrap: anywhere;
  }

  .plant-results__common-name {
    font-size: 0.9375rem;
    font-weight: 600;
  }

  .plant-results__identity em {
    color: var(--color-ink-soft);
    font-size: 0.8125rem;
  }

  .plant-results__action {
    display: inline-flex;
    align-items: center;
    gap: var(--space-xs);
    flex: 0 0 auto;
    color: var(--color-primary);
    font-size: 0.8125rem;
    font-weight: 600;
  }

  @media (max-width: 479px) {
    .plant-results__error,
    .plant-results__link {
      align-items: flex-start;
      flex-direction: column;
    }

    .plant-results__action {
      min-height: 44px;
      align-items: center;
    }
  }
</style>
