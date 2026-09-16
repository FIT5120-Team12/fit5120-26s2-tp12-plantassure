<script setup lang="ts">
  import type { EnvironmentalConcern, OriginStatus } from '@/types/plant';

  const environmentalConcern = defineModel<EnvironmentalConcern | null>('environmentalConcern', {
    default: null,
  });
  const originStatus = defineModel<OriginStatus | null>('originStatus', { default: null });

  const emit = defineEmits<{
    clear: [];
  }>();

  const environmentalConcernOptions: Array<{ title: string; value: EnvironmentalConcern }> = [
    { title: 'Very High', value: 'VERY_HIGH' },
    { title: 'High', value: 'HIGH' },
    { title: 'Moderately High', value: 'MODERATELY_HIGH' },
    { title: 'Medium', value: 'MEDIUM' },
    { title: 'Lower', value: 'LOWER' },
  ];
  const originStatusOptions: Array<{ title: string; value: OriginStatus }> = [
    { title: 'Native', value: 'NATIVE' },
    { title: 'Introduced', value: 'INTRODUCED' },
    { title: 'Uncertain', value: 'UNCERTAIN' },
  ];

  function clearFilters() {
    environmentalConcern.value = null;
    originStatus.value = null;
    emit('clear');
  }
</script>

<template>
  <div class="plant-catalog-filters" aria-label="Filter plants">
    <v-select
      v-model="environmentalConcern"
      label="Environmental concern"
      :items="environmentalConcernOptions"
      variant="outlined"
      color="primary"
      density="comfortable"
      hide-details
      clearable
    />
    <v-select
      v-model="originStatus"
      label="Origin status"
      :items="originStatusOptions"
      variant="outlined"
      color="primary"
      density="comfortable"
      hide-details
      clearable
    />
    <v-btn
      class="plant-catalog-filters__clear"
      type="button"
      variant="text"
      color="primary"
      @click="clearFilters"
    >
      Clear filters
    </v-btn>
  </div>
</template>

<style scoped>
  .plant-catalog-filters {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr)) auto;
    align-items: center;
    gap: var(--space-md);
    width: 100%;
  }

  .plant-catalog-filters__clear {
    justify-self: start;
  }

  @media (max-width: 767px) {
    .plant-catalog-filters {
      grid-template-columns: 1fr;
    }

    .plant-catalog-filters__clear {
      justify-self: start;
    }
  }
</style>
