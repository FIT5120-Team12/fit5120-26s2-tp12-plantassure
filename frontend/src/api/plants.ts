import axios from 'axios'

import type { PlantAssessmentResponse, PlantSearchResponse } from '@/types/plant'

export const plantApi = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '/api/v1',
})

export async function searchPlants(keyword: string): Promise<PlantSearchResponse> {
  const { data } = await plantApi.get<PlantSearchResponse>('/plants/search', {
    params: { q: keyword },
  })
  return data
}

export async function getPlantAssessment(plantId: number): Promise<PlantAssessmentResponse> {
  const { data } = await plantApi.get<PlantAssessmentResponse>(`/plants/${plantId}/assessment`)
  return data
}
