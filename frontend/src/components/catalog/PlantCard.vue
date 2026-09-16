<script setup lang="ts">
  import { getEnvironmentalConcernChipColor } from '@/utils/assessmentPresentation';

  interface PlantCardProps {
    plantId: number;
    commonName: string | null;
    scientificName: string;
    imageUrl?: string | null;
    environmentalConcern: string | null;
    originStatus: string | null;
    growthForm?: string | null;
    lifeHistory?: string | null;
    height?: string | null;
  }

  const props = defineProps<PlantCardProps>();

  const emit = defineEmits<{
    select: [plantId: number];
  }>();

  const traits = [
    ['Growth form', props.growthForm],
    ['Life history', props.lifeHistory],
    ['Height', props.height],
  ] as const;
</script>

<template>
  <article class="plant-card">
    <div class="plant-card__image">
      <v-img v-if="imageUrl" :src="imageUrl" :alt="commonName ?? scientificName" cover />
      <div v-else class="plant-card__image-fallback" aria-hidden="true">
        <v-icon icon="mdi-image-off-outline" size="32" />
      </div>
    </div>

    <div class="plant-card__body">
      <h2 v-if="commonName">{{ commonName }}</h2>
      <h2 v-else>
        <em>{{ scientificName }}</em>
      </h2>
      <p v-if="commonName" class="plant-card__scientific-name">
        <em>{{ scientificName }}</em>
      </p>

      <div class="plant-card__badges">
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

      <dl v-if="traits.some(([, value]) => value)" class="plant-card__traits">
        <template v-for="[label, value] in traits" :key="label">
          <template v-if="value">
            <dt>{{ label }}</dt>
            <dd>{{ value }}</dd>
          </template>
        </template>
      </dl>

      <v-btn
        class="plant-card__action"
        type="button"
        color="primary"
        variant="outlined"
        block
        @click="emit('select', plantId)"
      >
        View assessment
      </v-btn>
    </div>
  </article>
</template>

<style scoped>
  .plant-card {
    min-width: 0;
    overflow: hidden;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    background: var(--color-surface);
  }

  .plant-card__image {
    aspect-ratio: 4 / 3;
    background: var(--color-surface-muted);
  }

  .plant-card__image :deep(.v-img) {
    height: 100%;
  }

  .plant-card__image-fallback {
    height: 100%;
    display: grid;
    place-items: center;
    color: var(--color-muted);
  }

  .plant-card__body {
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: var(--space-sm);
    padding: var(--space-lg);
  }

  .plant-card h2 {
    margin: 0;
    color: var(--color-primary);
    font-size: 1.375rem;
    line-height: 1.15;
    overflow-wrap: anywhere;
  }

  .plant-card__scientific-name {
    margin: calc(var(--space-sm) * -1) 0 0;
    color: var(--color-ink-soft);
    overflow-wrap: anywhere;
  }

  .plant-card__badges {
    display: flex;
    flex-wrap: wrap;
    gap: var(--space-xs);
  }

  .plant-card__traits {
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
    gap: var(--space-xs) var(--space-md);
    margin: 0;
    font-size: 0.8125rem;
  }

  .plant-card__traits dt {
    color: var(--color-muted);
  }

  .plant-card__traits dd {
    margin: 0;
    color: var(--color-ink-soft);
    overflow-wrap: anywhere;
  }

  .plant-card__action {
    margin-top: var(--space-xs);
  }

  @media (max-width: 479px) {
    .plant-card__body {
      padding: var(--space-md);
    }
  }
</style>
