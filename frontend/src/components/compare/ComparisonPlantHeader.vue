<script setup lang="ts">
  import { computed, ref, watch } from 'vue';

  import {
    getEnvironmentalConcernLabel,
    getEnvironmentalRiskTone,
  } from '@/utils/assessmentPresentation';
  import { getOriginStatusLabel } from '@/utils/originStatusPresentation';
  import type { ComparisonPlant } from '@/types/plant';

  type ComparisonPlantHeaderProps = Pick<
    ComparisonPlant,
    | 'plantId'
    | 'commonName'
    | 'scientificName'
    | 'imageUrl'
    | 'environmentalConcern'
    | 'originStatus'
  >;

  const props = defineProps<ComparisonPlantHeaderProps>();

  const emit = defineEmits<{
    select: [plantId: number];
  }>();

  const imageFailed = ref(false);
  const concernTone = computed(() => getEnvironmentalRiskTone(props.environmentalConcern));

  watch(
    () => props.imageUrl,
    () => {
      imageFailed.value = false;
    },
  );
</script>

<template>
  <article class="comparison-plant-header">
    <div class="comparison-plant-header__identity">
      <div class="comparison-plant-header__image">
        <v-img
          v-if="imageUrl && !imageFailed"
          :src="imageUrl"
          :alt="commonName ?? scientificName"
          cover
          @error="imageFailed = true"
        />
        <v-sheet
          v-else
          class="comparison-plant-header__image-fallback"
          color="surface-variant"
          rounded="sm"
          aria-hidden="true"
        >
          <v-icon icon="mdi-image-off-outline" size="24" color="secondary" />
        </v-sheet>
      </div>

      <div class="comparison-plant-header__copy">
        <h2 v-if="commonName">{{ commonName }}</h2>
        <h2 v-else>
          <em>{{ scientificName }}</em>
        </h2>
        <p v-if="commonName" class="comparison-plant-header__scientific-name">
          <em>{{ scientificName }}</em>
        </p>

        <div class="comparison-plant-header__chips">
          <v-chip
            v-if="environmentalConcern"
            size="small"
            variant="tonal"
            :class="`comparison-plant-header__concern--${concernTone}`"
          >
            {{ getEnvironmentalConcernLabel(environmentalConcern) }}
          </v-chip>
          <v-chip v-if="originStatus" size="small" variant="outlined" color="primary">
            {{ getOriginStatusLabel(originStatus) }}
          </v-chip>
        </div>
      </div>
    </div>

    <v-btn
      class="comparison-plant-header__action"
      color="primary"
      variant="outlined"
      block
      type="button"
      append-icon="mdi-arrow-right"
      @click="emit('select', plantId)"
    >
      View assessment
    </v-btn>
  </article>
</template>

<style scoped>
  .comparison-plant-header {
    display: grid;
    gap: var(--space-md);
    padding: var(--space-md);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    background: var(--color-surface);
  }

  .comparison-plant-header__identity {
    display: grid;
    grid-template-columns: minmax(72px, 0.7fr) minmax(0, 1fr);
    align-items: start;
    gap: var(--space-sm);
  }

  .comparison-plant-header__image {
    width: 100%;
    aspect-ratio: 4 / 3;
    overflow: hidden;
    border-radius: var(--radius-sm);
    background: var(--color-surface-muted);
  }

  .comparison-plant-header__image :deep(.v-img),
  .comparison-plant-header__image-fallback {
    width: 100%;
    height: 100%;
  }

  .comparison-plant-header__image-fallback {
    display: grid;
    place-items: center;
  }

  .comparison-plant-header__copy {
    min-width: 0;
  }

  .comparison-plant-header h2 {
    margin: 0;
    color: var(--color-primary);
    font-family: var(--font-body);
    font-size: 1rem;
    font-weight: 700;
    line-height: 1.25;
    overflow-wrap: anywhere;
  }

  .comparison-plant-header__scientific-name {
    margin: 2px 0 0;
    color: var(--color-ink-soft);
    font-size: 0.8125rem;
    overflow-wrap: anywhere;
  }

  .comparison-plant-header__chips {
    display: flex;
    flex-wrap: wrap;
    gap: var(--space-xs);
    margin-top: var(--space-sm);
  }

  .comparison-plant-header__concern--concern {
    background: var(--color-accent-soft);
    color: var(--color-accent);
  }

  .comparison-plant-header__concern--caution {
    background: var(--color-surface-warm);
    color: var(--color-accent);
  }

  .comparison-plant-header__concern--lower {
    background: var(--color-success-soft);
    color: var(--color-primary);
  }

  .comparison-plant-header__action {
    justify-self: stretch;
  }

  @media (max-width: 479px) {
    .comparison-plant-header__identity {
      grid-template-columns: 1fr;
    }

    .comparison-plant-header__image {
      max-width: 12rem;
    }
  }
</style>
