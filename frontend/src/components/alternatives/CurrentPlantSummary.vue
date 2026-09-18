<script setup lang="ts">
  import {
    getEnvironmentalConcernChipColor,
    getEnvironmentalConcernLabel,
  } from '@/utils/assessmentPresentation';

  interface CurrentPlantSummaryProps {
    commonName: string | null;
    scientificName: string;
    imageUrl?: string | null;
    environmentalConcern: string | null;
    growthForm?: string | null;
    lifeHistory?: string | null;
    height?: string | null;
  }

  const props = defineProps<CurrentPlantSummaryProps>();

  const traits = [
    { label: 'Growth form', value: props.growthForm, icon: 'mdi-leaf-outline' },
    { label: 'Life history', value: props.lifeHistory, icon: 'mdi-calendar-outline' },
    { label: 'Height', value: props.height, icon: 'mdi-arrow-expand-vertical' },
  ].filter((trait) => trait.value);

</script>

<template>
  <article class="current-plant-summary" aria-label="Current plant summary">
    <div class="current-plant-summary__image">
      <v-img v-if="imageUrl" :src="imageUrl" :alt="commonName ?? scientificName" cover />
      <div v-else class="current-plant-summary__image-fallback" aria-hidden="true">
        <v-icon icon="mdi-image-off-outline" size="28" />
      </div>
    </div>

    <div class="current-plant-summary__identity">
      <h2 v-if="commonName">{{ commonName }}</h2>
      <h2 v-else>
        <em>{{ scientificName }}</em>
      </h2>
      <p v-if="commonName" class="current-plant-summary__scientific-name">
        <em>{{ scientificName }}</em>
      </p>
      <v-chip
        v-if="environmentalConcern"
        size="small"
        variant="tonal"
        :color="getEnvironmentalConcernChipColor(environmentalConcern)"
      >
        {{ getEnvironmentalConcernLabel(environmentalConcern) }}
      </v-chip>
    </div>

    <dl v-if="traits.length" class="current-plant-summary__traits">
      <div v-for="trait in traits" :key="trait.label" class="current-plant-summary__trait">
        <dt>
          <v-icon :icon="trait.icon" size="16" aria-hidden="true" />
          {{ trait.label }}
        </dt>
        <dd>{{ trait.value }}</dd>
      </div>
    </dl>
  </article>
</template>

<style scoped>
  .current-plant-summary {
    display: grid;
    grid-template-columns: 9rem minmax(0, 1fr) minmax(14rem, 1fr);
    align-items: center;
    gap: var(--space-lg);
    min-width: 0;
    padding: var(--space-md);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    background: var(--color-surface);
  }

  .current-plant-summary__image {
    width: 100%;
    aspect-ratio: 4 / 3;
    overflow: hidden;
    border-radius: var(--radius-sm);
    background: var(--color-surface-muted);
  }

  .current-plant-summary__image :deep(.v-img) {
    width: 100%;
    height: 100%;
  }

  .current-plant-summary__image-fallback {
    width: 100%;
    height: 100%;
    display: grid;
    place-items: center;
    color: var(--color-muted);
  }

  .current-plant-summary__identity {
    min-width: 0;
  }

  .current-plant-summary h2 {
    margin: 0;
    color: var(--color-primary);
    font-size: 1.5rem;
    line-height: 1.15;
    overflow-wrap: anywhere;
  }

  .current-plant-summary__scientific-name {
    margin: var(--space-xs) 0 var(--space-sm);
    color: var(--color-ink-soft);
    overflow-wrap: anywhere;
  }

  .current-plant-summary__traits {
    display: grid;
    gap: var(--space-sm);
    min-width: 0;
    margin: 0;
  }

  .current-plant-summary__trait {
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
    gap: var(--space-sm);
    font-size: 0.875rem;
  }

  .current-plant-summary__trait dt {
    display: flex;
    align-items: center;
    gap: var(--space-xs);
    color: var(--color-muted);
  }

  .current-plant-summary__trait dd {
    margin: 0;
    color: var(--color-ink-soft);
    overflow-wrap: anywhere;
  }

  @media (max-width: 767px) {
    .current-plant-summary {
      grid-template-columns: 7rem minmax(0, 1fr);
    }

    .current-plant-summary__traits {
      grid-column: 1 / -1;
    }
  }

  @media (max-width: 479px) {
    .current-plant-summary {
      grid-template-columns: 1fr;
    }

    .current-plant-summary__image {
      width: min(100%, 14rem);
    }

    .current-plant-summary__traits {
      grid-column: auto;
    }
  }
</style>
