<script setup lang="ts">
  import { getEnvironmentalConcernChipColor } from '@/utils/assessmentPresentation';
  import type { LegalStatus } from '@/types/plant';

  interface AlternativePlantCardProps {
    plantId: number;
    commonName: string | null;
    scientificName: string;
    imageUrl?: string | null;
    environmentalConcern: string | null;
    originStatus: string | null;
    legalStatus: LegalStatus;
    matchReasons: string[];
    growthForm?: string | null;
    lifeHistory?: string | null;
    height?: string | null;
    selected?: boolean;
    compareDisabled?: boolean;
  }

  const props = withDefaults(defineProps<AlternativePlantCardProps>(), {
    imageUrl: null,
    environmentalConcern: null,
    originStatus: null,
    growthForm: null,
    lifeHistory: null,
    height: null,
    selected: false,
    compareDisabled: false,
  });

  const emit = defineEmits<{
    select: [plantId: number];
    toggleCompare: [plantId: number];
  }>();

  function formatLegalStatus(status: LegalStatus): string {
    switch (status) {
      case 'NOT_REGULATED':
        return 'Not regulated';
      case 'REGULATED':
        return 'Regulated';
      case 'UNAVAILABLE':
        return 'Legal status unavailable';
    }
  }

  const traits = [
    { label: 'Growth form', value: props.growthForm, icon: 'mdi-leaf-outline' },
    { label: 'Life history', value: props.lifeHistory, icon: 'mdi-calendar-outline' },
    { label: 'Height', value: props.height, icon: 'mdi-arrow-expand-vertical' },
  ].filter((trait) => trait.value);

</script>

<template>
  <article class="alternative-plant-card" :class="{ 'alternative-plant-card--selected': selected }">
    <div class="alternative-plant-card__image">
      <v-img v-if="imageUrl" :src="imageUrl" :alt="commonName ?? scientificName" cover />
      <div v-else class="alternative-plant-card__image-fallback" aria-hidden="true">
        <v-icon icon="mdi-image-off-outline" size="32" />
      </div>
    </div>

    <div class="alternative-plant-card__body">
      <div>
        <h2 v-if="commonName">{{ commonName }}</h2>
        <h2 v-else>
          <em>{{ scientificName }}</em>
        </h2>
        <p v-if="commonName" class="alternative-plant-card__scientific-name">
          <em>{{ scientificName }}</em>
        </p>
      </div>

      <div class="alternative-plant-card__badges">
        <v-chip
          v-if="environmentalConcern"
          size="small"
          variant="tonal"
          :color="getEnvironmentalConcernChipColor(environmentalConcern)"
        >
          {{ environmentalConcern }}
        </v-chip>
        <v-chip v-if="originStatus" size="small" variant="outlined" color="primary">
          {{ originStatus }}
        </v-chip>
      </div>

      <p class="alternative-plant-card__legal-status">
        Legal status: {{ formatLegalStatus(legalStatus) }}
      </p>

      <section v-if="matchReasons.length" class="alternative-plant-card__matches">
        <h3>Why it matches</h3>
        <ul>
          <li v-for="reason in matchReasons" :key="reason">
            <v-icon icon="mdi-check-circle-outline" size="16" aria-hidden="true" />
            <span>{{ reason }}</span>
          </li>
        </ul>
      </section>

      <dl v-if="traits.length" class="alternative-plant-card__traits">
        <div v-for="trait in traits" :key="trait.label" class="alternative-plant-card__trait">
          <dt>
            <v-icon :icon="trait.icon" size="16" aria-hidden="true" />
            {{ trait.label }}
          </dt>
          <dd>{{ trait.value }}</dd>
        </div>
      </dl>

      <div class="alternative-plant-card__actions">
        <v-btn
          color="primary"
          variant="outlined"
          block
          type="button"
          @click="emit('select', plantId)"
        >
          View assessment
        </v-btn>
        <v-btn
          color="primary"
          :variant="selected ? 'flat' : 'outlined'"
          block
          type="button"
          :aria-pressed="selected"
          :disabled="compareDisabled"
          @click="emit('toggleCompare', plantId)"
        >
          {{ selected ? 'Remove from compare' : 'Add to compare' }}
        </v-btn>
      </div>
    </div>
  </article>
</template>

<style scoped>
  .alternative-plant-card {
    min-width: 0;
    overflow: hidden;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    background: var(--color-surface);
  }

  .alternative-plant-card--selected {
    border-color: var(--color-primary);
  }

  .alternative-plant-card__image {
    aspect-ratio: 4 / 3;
    background: var(--color-surface-muted);
  }

  .alternative-plant-card__image :deep(.v-img) {
    height: 100%;
  }

  .alternative-plant-card__image-fallback {
    height: 100%;
    display: grid;
    place-items: center;
    color: var(--color-muted);
  }

  .alternative-plant-card__body {
    display: flex;
    flex-direction: column;
    gap: var(--space-md);
    padding: var(--space-lg);
  }

  .alternative-plant-card h2,
  .alternative-plant-card h3 {
    margin: 0;
  }

  .alternative-plant-card h2 {
    color: var(--color-primary);
    font-size: 1.375rem;
    line-height: 1.15;
    overflow-wrap: anywhere;
  }

  .alternative-plant-card__scientific-name {
    margin: var(--space-xs) 0 0;
    color: var(--color-ink-soft);
    overflow-wrap: anywhere;
  }

  .alternative-plant-card__badges {
    display: flex;
    flex-wrap: wrap;
    gap: var(--space-xs);
  }

  .alternative-plant-card__legal-status {
    margin: 0;
    color: var(--color-muted);
    font-size: 0.8125rem;
  }

  .alternative-plant-card__matches {
    padding-top: var(--space-sm);
    border-top: 1px solid var(--color-border);
  }

  .alternative-plant-card__matches h3 {
    color: var(--color-primary);
    font-size: 0.9375rem;
    font-weight: 600;
  }

  .alternative-plant-card__matches ul {
    display: grid;
    gap: var(--space-xs);
    margin: var(--space-sm) 0 0;
    padding: 0;
    list-style: none;
    color: var(--color-ink-soft);
    font-size: 0.875rem;
  }

  .alternative-plant-card__matches li {
    display: flex;
    align-items: flex-start;
    gap: var(--space-xs);
  }

  .alternative-plant-card__traits {
    display: grid;
    gap: var(--space-xs);
    margin: 0;
    font-size: 0.8125rem;
  }

  .alternative-plant-card__trait {
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
    gap: var(--space-sm);
  }

  .alternative-plant-card__trait dt {
    display: flex;
    align-items: center;
    gap: var(--space-xs);
    color: var(--color-muted);
  }

  .alternative-plant-card__trait dd {
    margin: 0;
    color: var(--color-ink-soft);
    overflow-wrap: anywhere;
  }

  .alternative-plant-card__actions {
    display: grid;
    gap: var(--space-xs);
    margin-top: var(--space-xs);
  }

  @media (max-width: 479px) {
    .alternative-plant-card__body {
      padding: var(--space-md);
    }
  }
</style>
