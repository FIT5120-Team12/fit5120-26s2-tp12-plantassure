export interface PlantSearchResult {
  plantId: number;
  scientificName: string;
  commonName: string | null;
}

export interface PlantSearchResponse {
  query: string;
  results: PlantSearchResult[];
}

export interface PlantIdentity {
  plantId: number;
  scientificName: string;
  commonName: string | null;
  family: string | null;
  establishmentMeans: 'Native' | 'Introduced' | 'Uncertain' | null;
  degreeOfEstablishment: 'Native' | 'Naturalised' | 'Adventive' | null;
}

export type LocalOccurrenceStatus = 'FOUND' | 'NOT_FOUND' | 'UNAVAILABLE';

export interface LocalOccurrence {
  status: LocalOccurrenceStatus;
  recordCount: number | null;
  mostRecentRecordYear: number | null;
  source: string;
}

export type EnvironmentalRiskStatus = 'ASSESSED' | 'NOT_ASSESSED' | 'UNAVAILABLE';

export interface EnvironmentalRisk {
  assessmentStatus: EnvironmentalRiskStatus;
  rating: string | null;
  explanation: string;
  source: string;
}

export type RecommendationLevel =
  'RECONSIDER_PLANTING' | 'USE_CAUTION' | 'LOWER_CONCERN' | 'NOT_ASSESSED';

export interface Recommendation {
  level: RecommendationLevel;
  displayLabel: string;
  explanation: string;
}

export interface DataSource {
  name: string;
  role: string;
}

export interface PlantAssessmentResponse {
  plant: PlantIdentity;
  localOccurrence: LocalOccurrence;
  environmentalRisk: EnvironmentalRisk;
  recommendation: Recommendation;
  sources: DataSource[];
  warnings: string[];
}
