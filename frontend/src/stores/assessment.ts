import axios from 'axios';
import { defineStore } from 'pinia';
import { ref } from 'vue';

import { getPlantAssessment } from '@/api/plants';
import type { PlantAssessmentResponse } from '@/types/plant';

const ASSESSMENT_ERROR_MESSAGE = "We couldn't load this plant assessment. Please try again.";

export const useAssessmentStore = defineStore('assessment', () => {
  const selectedPlantId = ref<number | null>(null);
  const assessment = ref<PlantAssessmentResponse | null>(null);
  const isLoading = ref(false);
  const error = ref<string | null>(null);

  async function fetchAssessment(plantId: number) {
    selectedPlantId.value = plantId;
    assessment.value = null;
    isLoading.value = true;
    error.value = null;

    try {
      assessment.value = await getPlantAssessment(plantId);
    } catch (cause: unknown) {
      if (axios.isAxiosError(cause) && cause.response?.status === 404) {
        error.value = 'Plant not found.';
      } else {
        error.value = ASSESSMENT_ERROR_MESSAGE;
      }
    } finally {
      isLoading.value = false;
    }
  }

  function clearAssessment() {
    selectedPlantId.value = null;
    assessment.value = null;
    isLoading.value = false;
    error.value = null;
  }

  return {
    selectedPlantId,
    assessment,
    isLoading,
    error,
    fetchAssessment,
    clearAssessment,
  };
});
