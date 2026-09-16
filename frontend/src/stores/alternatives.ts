import { defineStore } from 'pinia';
import { ref } from 'vue';

import { getAlternatives } from '@/api/plants';
import type {
  AlternativePlant,
  CurrentPlantAlternativeSummary,
  PlantAlternativesParams,
} from '@/types/plant';

const ALTERNATIVES_ERROR_MESSAGE = 'We couldn’t load alternative plants. Please try again.';

export const usePlantAlternativesStore = defineStore('plantAlternatives', () => {
  const currentPlant = ref<CurrentPlantAlternativeSummary | null>(null);
  const alternatives = ref<AlternativePlant[]>([]);
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  const hasLoaded = ref(false);
  let requestId = 0;

  async function fetchAlternatives(
    plantId: number,
    params?: PlantAlternativesParams,
  ): Promise<void> {
    const currentRequestId = ++requestId;

    isLoading.value = true;
    error.value = null;
    currentPlant.value = null;
    alternatives.value = [];

    try {
      const response = await getAlternatives(plantId, params);

      if (currentRequestId !== requestId) return;

      currentPlant.value = response.currentPlant;
      alternatives.value = response.alternatives;
      hasLoaded.value = true;
    } catch {
      if (currentRequestId !== requestId) return;

      error.value = ALTERNATIVES_ERROR_MESSAGE;
      hasLoaded.value = true;
    } finally {
      if (currentRequestId === requestId) isLoading.value = false;
    }
  }

  return {
    currentPlant,
    alternatives,
    isLoading,
    error,
    hasLoaded,
    fetchAlternatives,
  };
});
