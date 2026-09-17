<script setup lang="ts">
  import { computed } from 'vue';

  import type { IdentificationCandidate } from '@/types/identification';

  const props = defineProps<{
    candidate: IdentificationCandidate;
  }>();

  const emit = defineEmits<{
    confirm: [candidate: IdentificationCandidate];
  }>();

  const hasPlantAssureAssessment = computed(() => {
    const plantId = props.candidate.plantId;

    return (
      props.candidate.plantAssureMatch === true &&
      typeof plantId === 'number' &&
      Number.isSafeInteger(plantId) &&
      plantId > 0
    );
  });

  const confidenceLabel = computed(() => {
    const confidence = props.candidate.identificationConfidence;
    const percentage = confidence >= 0 && confidence <= 1 ? confidence * 100 : confidence;

    return `${Math.round(percentage)}%`;
  });
</script>

<template>
  <article class="identification-candidate-card">
    <v-sheet border rounded="md" color="surface" class="identification-candidate-card__surface">
      <div>
        <h3>{{ candidate.commonName ?? candidate.scientificName }}</h3>
        <p v-if="candidate.commonName" class="identification-candidate-card__scientific-name">
          {{ candidate.scientificName }}
        </p>
      </div>

      <p class="identification-candidate-card__confidence">
        Identification confidence: {{ confidenceLabel }}
      </p>

      <div class="identification-candidate-card__availability">
        <p v-if="hasPlantAssureAssessment">Verified PlantAssure assessment available</p>
        <p v-else>PlantAssure assessment unavailable</p>

        <v-btn
          v-if="hasPlantAssureAssessment"
          color="primary"
          variant="outlined"
          type="button"
          @click="emit('confirm', candidate)"
        >
          Confirm this plant
        </v-btn>
      </div>
    </v-sheet>
  </article>
</template>

<style scoped>
  .identification-candidate-card__surface {
    height: 100%;
    display: flex;
    flex-direction: column;
    gap: var(--space-md);
    padding: var(--space-lg);
    border-color: var(--color-border);
  }

  .identification-candidate-card h3 {
    margin: 0;
    color: var(--color-primary);
    font-size: 1.25rem;
  }

  .identification-candidate-card__scientific-name,
  .identification-candidate-card__confidence,
  .identification-candidate-card__availability p {
    margin: var(--space-xs) 0 0;
    color: var(--color-ink-soft);
  }

  .identification-candidate-card__scientific-name {
    font-style: italic;
  }

  .identification-candidate-card__confidence {
    font-weight: 600;
  }

  .identification-candidate-card__availability {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-md);
    margin-top: auto;
  }
</style>
