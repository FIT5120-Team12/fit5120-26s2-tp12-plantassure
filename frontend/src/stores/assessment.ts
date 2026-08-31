import { defineStore } from 'pinia';
import { ref } from 'vue';

import { getPlantAssessment } from '@/api/plants';
import type { PlantAssessmentResponse } from '@/types/plant';

const ASSESSMENT_ERROR_MESSAGE = 'We couldn’t load this plant assessment. Please try again.';

export const useAssessmentStore = defineStore('assessment', () => {
  const selectedPlantId = ref<number | null>(null);
  const assessment = ref<PlantAssessmentResponse | null>(null);
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  let assessmentRequestId = 0;

  async function fetchAssessment(plantId: number) {
    const requestId = ++assessmentRequestId;

    selectedPlantId.value = plantId;
    assessment.value = null;
    isLoading.value = true;
    error.value = null;

    try {
      const response = await getPlantAssessment(plantId);

      if (requestId !== assessmentRequestId || selectedPlantId.value !== plantId) return;

      assessment.value = response;
    } catch {
      if (requestId !== assessmentRequestId || selectedPlantId.value !== plantId) return;
      error.value = ASSESSMENT_ERROR_MESSAGE;
    } finally {
      if (requestId === assessmentRequestId) {
        isLoading.value = false;
      }
    }
  }

  function clearAssessment() {
    assessmentRequestId += 1;
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
