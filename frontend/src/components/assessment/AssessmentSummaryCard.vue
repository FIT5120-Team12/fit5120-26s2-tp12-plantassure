<script setup lang="ts">
  import type { AssessmentTone } from '@/utils/assessmentPresentation';

  defineProps<{
    icon: string;
    title: string;
    value: string | number;
    supporting?: string;
    source: string;
    tone?: AssessmentTone;
  }>();
</script>

<template>
  <article class="assessment-summary-card" :class="`assessment-summary-card--${tone ?? 'neutral'}`">
    <span class="mdi assessment-summary-card__icon" :class="icon" aria-hidden="true" />
    <h3>{{ title }}</h3>
    <p>{{ value }}</p>
    <span v-if="supporting" class="assessment-summary-card__supporting">{{ supporting }}</span>
    <small>{{ source }}</small>
  </article>
</template>

<style scoped>
  .assessment-summary-card {
    display: flex;
    min-width: 0;
    flex-direction: column;
    padding: 26px;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
    background: var(--color-surface);
  }

  .assessment-summary-card__icon {
    display: inline-grid;
    width: 44px;
    height: 44px;
    flex: 0 0 auto;
    place-items: center;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-pill);
    color: var(--color-primary);
    font-size: 1.25rem;
  }

  .assessment-summary-card--concern .assessment-summary-card__icon,
  .assessment-summary-card--caution .assessment-summary-card__icon {
    border-color: color-mix(in srgb, var(--color-accent) 38%, var(--color-border));
    background: var(--color-accent-soft);
  }

  .assessment-summary-card--lower .assessment-summary-card__icon {
    background: var(--color-success-soft);
  }

  .assessment-summary-card--unavailable .assessment-summary-card__icon {
    background: var(--color-surface-muted);
    color: var(--color-muted);
  }

  .assessment-summary-card h3 {
    margin: var(--space-md) 0 0;
    font-family: var(--font-body);
    font-size: 0.9375rem;
    font-weight: 700;
    overflow-wrap: anywhere;
  }

  .assessment-summary-card p {
    margin: 6px 0 0;
    overflow-wrap: anywhere;
    color: var(--color-primary);
    font-size: 1.125rem;
    font-weight: 800;
  }

  .assessment-summary-card__supporting {
    margin-top: 4px;
    color: var(--color-ink-soft);
    font-size: 0.875rem;
    overflow-wrap: anywhere;
  }

  .assessment-summary-card small {
    margin-top: auto;
    padding-top: var(--space-md);
    color: var(--color-muted);
    font-size: 0.6875rem;
    overflow-wrap: anywhere;
  }
</style>
