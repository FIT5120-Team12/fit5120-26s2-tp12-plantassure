<script setup lang="ts">
  import type { AssessmentTone } from '@/utils/assessmentPresentation';

  defineProps<{
    title: string;
    source?: string;
    tone?: AssessmentTone;
  }>();
</script>

<template>
  <article
    class="assessment-evidence-card"
    :class="`assessment-evidence-card--${tone ?? 'neutral'}`"
  >
    <h3>{{ title }}</h3>
    <div class="assessment-evidence-card__surface">
      <div class="assessment-evidence-card__body">
        <slot />
      </div>
      <p v-if="source" class="assessment-evidence-card__source">Source: {{ source }}</p>
    </div>
  </article>
</template>

<style scoped>
  .assessment-evidence-card {
    display: flex;
    min-width: 0;
    flex-direction: column;
    gap: var(--space-sm);
  }

  .assessment-evidence-card__surface {
    display: flex;
    flex: 1;
    flex-direction: column;
    padding: var(--space-lg);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    background: var(--color-surface);
  }

  .assessment-evidence-card--unavailable .assessment-evidence-card__surface {
    background: var(--color-surface-muted);
  }

  .assessment-evidence-card h3 {
    margin: 0;
    color: var(--color-primary);
    font-size: 1.375rem;
    line-height: 1.2;
    overflow-wrap: anywhere;
  }

  .assessment-evidence-card__body {
    flex: 1;
  }

  .assessment-evidence-card__source {
    margin: 0;
    padding-top: var(--space-md);
    border-top: 1px solid var(--color-border);
    color: var(--color-muted);
    font-size: 0.8125rem;
    overflow-wrap: anywhere;
  }
</style>
