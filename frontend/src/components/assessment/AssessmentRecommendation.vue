<script setup lang="ts">
  import { computed } from 'vue';

  import type { Recommendation } from '@/types/plant';
  import { getRecommendationPresentation } from '@/utils/assessmentPresentation';

  const props = defineProps<{
    recommendation: Recommendation;
    warnings: string[];
  }>();

  const presentation = computed(() => getRecommendationPresentation(props.recommendation.level));
</script>

<template>
  <section
    class="assessment-guidance"
    :class="`assessment-guidance--${presentation.tone}`"
    aria-labelledby="recommendation-heading"
  >
    <div class="assessment-guidance__main">
      <p class="assessment-guidance__eyebrow">PLANTING GUIDANCE</p>
      <div class="assessment-guidance__title-row">
        <span class="mdi assessment-guidance__icon" :class="presentation.icon" aria-hidden="true" />
        <h2 id="recommendation-heading">{{ recommendation.displayLabel }}</h2>
      </div>
      <p class="assessment-guidance__explanation">{{ recommendation.explanation }}</p>

      <div v-if="warnings.length" class="assessment-guidance__notice">
        <span class="mdi mdi-information-outline" aria-hidden="true" />
        <ul>
          <li v-for="warning in warnings" :key="warning">{{ warning }}</li>
        </ul>
      </div>
    </div>

    <div class="assessment-guidance__meaning">
      <p>WHAT THIS MEANS FOR YOU</p>
      <span>{{ presentation.guidance }}</span>
    </div>
  </section>
</template>

<style scoped>
  .assessment-guidance {
    overflow: hidden;
    border: 2px solid var(--color-border-strong);
    border-radius: var(--radius-lg);
    background: var(--color-surface-muted);
  }

  .assessment-guidance--concern,
  .assessment-guidance--caution {
    border-color: color-mix(in srgb, var(--color-accent) 48%, var(--color-border));
    background: var(--color-surface-warm);
  }

  .assessment-guidance--lower {
    border-color: color-mix(in srgb, var(--color-primary) 20%, var(--color-border));
    background: var(--color-success-soft);
  }

  .assessment-guidance__main {
    padding: 28px var(--space-xl) var(--space-lg);
  }

  .assessment-guidance__eyebrow,
  .assessment-guidance__meaning p {
    margin: 0;
    color: var(--color-muted);
    font-size: 0.75rem;
    font-weight: 800;
    letter-spacing: 0.1em;
  }

  .assessment-guidance__title-row {
    min-width: 0;
    display: flex;
    align-items: center;
    gap: var(--space-sm);
    margin-top: var(--space-sm);
  }

  .assessment-guidance__icon {
    flex: 0 0 auto;
    color: var(--color-primary);
    font-size: 1.5rem;
  }

  .assessment-guidance--concern .assessment-guidance__icon,
  .assessment-guidance--concern h2,
  .assessment-guidance--caution .assessment-guidance__icon,
  .assessment-guidance--caution h2 {
    color: color-mix(in srgb, var(--color-accent) 65%, var(--color-primary));
  }

  .assessment-guidance h2 {
    min-width: 0;
    margin: 0;
    overflow-wrap: anywhere;
    font-size: clamp(2rem, 4vw, 2.5rem);
    line-height: 1.05;
  }

  .assessment-guidance__explanation {
    max-width: 48rem;
    margin: var(--space-md) 0 0;
    color: var(--color-ink-soft);
    overflow-wrap: anywhere;
  }

  .assessment-guidance__notice {
    min-width: 0;
    display: flex;
    align-items: flex-start;
    gap: var(--space-xs);
    margin-top: var(--space-md);
    padding: var(--space-sm) var(--space-md);
    border: 1px solid color-mix(in srgb, var(--color-accent) 24%, transparent);
    border-radius: var(--radius-sm);
    background: color-mix(in srgb, var(--color-accent) 8%, transparent);
    color: var(--color-ink-soft);
    font-size: 0.8125rem;
  }

  .assessment-guidance__notice ul {
    min-width: 0;
    margin: 0;
    padding-left: var(--space-md);
    overflow-wrap: anywhere;
  }

  .assessment-guidance__meaning {
    padding: 18px var(--space-xl);
    border-top: 1px solid var(--color-border-strong);
  }

  .assessment-guidance__meaning span {
    display: block;
    max-width: 44rem;
    margin-top: 6px;
    color: var(--color-ink-soft);
    font-size: 0.875rem;
    overflow-wrap: anywhere;
  }

  @media (max-width: 767px) {
    .assessment-guidance__main,
    .assessment-guidance__meaning {
      padding-inline: var(--space-lg);
    }

    .assessment-guidance__title-row {
      align-items: flex-start;
    }

    .assessment-guidance__icon {
      margin-top: 4px;
    }

    .assessment-guidance__notice {
      padding-inline: var(--space-sm);
    }
  }
</style>
