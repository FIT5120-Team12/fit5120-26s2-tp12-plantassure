import { defineStore } from 'pinia';
import { ref } from 'vue';

import { getPlants } from '@/api/plants';
import type { EnvironmentalConcern, OriginStatus, PlantCatalogItem } from '@/types/plant';

const CATALOG_ERROR_MESSAGE = 'We couldn’t load assessed plants. Please try again.';

export const usePlantCatalogStore = defineStore('plantCatalog', () => {
  const items = ref<PlantCatalogItem[]>([]);
  const query = ref('');
  const environmentalConcern = ref<EnvironmentalConcern[]>([]);
  const originStatus = ref<OriginStatus[]>([]);
  const page = ref(0);
  const size = ref(12);
  const totalElements = ref(0);
  const totalPages = ref(0);
  const sort = ref('commonName,asc');
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  const hasLoaded = ref(false);
  let requestId = 0;

  async function fetchPlants() {
    const currentRequestId = ++requestId;
    const requestedPage = page.value;

    isLoading.value = true;
    error.value = null;

    try {
      const response = await getPlants({
        q: query.value.trim() || undefined,
        environmentalConcern: environmentalConcern.value.length
          ? environmentalConcern.value
          : undefined,
        originStatus: originStatus.value.length ? originStatus.value : undefined,
        page: page.value,
        size: size.value,
        sort: sort.value,
      });

      if (currentRequestId !== requestId) return;

      const lastPage = Math.max(0, response.totalPages - 1);
      const resolvedPage = Math.min(Math.max(response.page, 0), lastPage);

      if (
        response.totalPages > 0 &&
        response.page !== resolvedPage &&
        requestedPage !== resolvedPage
      ) {
        page.value = resolvedPage;
        void fetchPlants();
        return;
      }

      items.value = response.items;
      page.value = resolvedPage;
      size.value = response.size;
      totalElements.value = response.totalElements;
      totalPages.value = response.totalPages;
      sort.value = response.sort;
      hasLoaded.value = true;
    } catch {
      if (currentRequestId !== requestId) return;

      error.value = CATALOG_ERROR_MESSAGE;
      hasLoaded.value = true;
    } finally {
      if (currentRequestId === requestId) isLoading.value = false;
    }
  }

  return {
    items,
    query,
    environmentalConcern,
    originStatus,
    page,
    size,
    totalElements,
    totalPages,
    sort,
    isLoading,
    error,
    hasLoaded,
    fetchPlants,
  };
});
