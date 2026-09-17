import { http } from '@/api/http';

import type { PlantIdentificationResponse } from '@/types/identification';

const SUPPORTED_IMAGE_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp']);
export const IDENTIFICATION_MAX_IMAGE_SIZE_BYTES = 10 * 1024 * 1024;

export async function identifyPlant(image: File): Promise<PlantIdentificationResponse> {
  if (!(image instanceof File)) {
    throw new TypeError('image must be a File.');
  }

  if (!SUPPORTED_IMAGE_TYPES.has(image.type)) {
    throw new TypeError('image must be a JPEG, PNG, or WebP file.');
  }

  if (image.size > IDENTIFICATION_MAX_IMAGE_SIZE_BYTES) {
    throw new RangeError('image must not exceed 10 MB.');
  }

  const formData = new FormData();
  formData.append('image', image);

  const { data } = await http.post<PlantIdentificationResponse>('/plants/identify', formData);

  return data;
}
