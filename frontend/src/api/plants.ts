import { http } from '@/api/http';

import type {
  PlantAlternativesParams,
  PlantAlternativesResponse,
  PlantAssessmentResponse,
  PlantCatalogParams,
  PlantCatalogResponse,
  PlantComparisonResponse,
  PlantSearchResponse,
} from '@/types/plant';

const DEFAULT_SEARCH_LIMIT = 8;

export async function searchPlants(
  keyword: string,
  limit = DEFAULT_SEARCH_LIMIT,
): Promise<PlantSearchResponse> {
  const query = keyword.trim();
  const requestedLimit = Number.isSafeInteger(limit) && limit > 0 ? limit : DEFAULT_SEARCH_LIMIT;

  if (!query) {
    return { query, items: [] };
  }

  const { data } = await http.get<PlantSearchResponse>('/plants/search', {
    params: { q: query, limit: requestedLimit },
  });

  return data;
}

export async function getPlantAssessment(plantId: number): Promise<PlantAssessmentResponse> {
  const { data } = await http.get<PlantAssessmentResponse>(`/plants/${plantId}/assessment`);

  return data;
}

export async function getPlants(params: PlantCatalogParams): Promise<PlantCatalogResponse> {
  const { data } = await http.get<PlantCatalogResponse>('/plants', {
    params,
    paramsSerializer: { indexes: null },
  });

  return data;
}

export async function getAlternatives(
  plantId: number,
  params?: PlantAlternativesParams,
): Promise<PlantAlternativesResponse> {
  if (!Number.isSafeInteger(plantId) || plantId <= 0) {
    throw new RangeError('plantId must be a positive integer.');
  }

  if (params?.limit !== undefined && (!Number.isSafeInteger(params.limit) || params.limit <= 0)) {
    throw new RangeError('limit must be a positive integer.');
  }

  const { data } = await http.get<PlantAlternativesResponse>(`/plants/${plantId}/alternatives`, {
    params,
  });

  return data;
}

export async function comparePlants(plantIds: number[]): Promise<PlantComparisonResponse> {
  const hasValidPlantIds =
    Array.isArray(plantIds) &&
    plantIds.length >= 2 &&
    plantIds.length <= 3 &&
    plantIds.every((plantId) => Number.isSafeInteger(plantId) && plantId > 0) &&
    new Set(plantIds).size === plantIds.length;

  if (!hasValidPlantIds) {
    throw new RangeError('plantIds must contain two or three unique positive integers.');
  }

  const { data } = await http.get<PlantComparisonResponse>('/plants/compare', {
    params: { plantIds: plantIds.join(',') },
  });

  return data;
}
