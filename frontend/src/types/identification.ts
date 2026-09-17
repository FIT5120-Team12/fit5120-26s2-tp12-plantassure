export type IdentificationStatus = 'MATCHES_FOUND' | 'NO_CONFIDENT_MATCH';

export interface IdentificationCandidate {
  scientificName: string;
  commonName: string | null;
  identificationConfidence: number;
  plantId: number | null;
  plantAssureMatch: boolean;
}

export interface PlantIdentificationResponse {
  status: IdentificationStatus;
  matches: IdentificationCandidate[];
}
