export interface PlantSearchResult {
  plantId: number;
  scientificName: string;
  commonName: string | null;
  family: string | null;
  imageUrl: string | null;
}

export interface PlantSearchResponse {
  query: string;
  items: PlantSearchResult[];
}

export type EnvironmentalConcern =
  'VERY_HIGH' | 'HIGH' | 'MODERATELY_HIGH' | 'MEDIUM' | 'LOWER' | 'NOT_ASSESSED' | 'UNAVAILABLE';

export type Recommendation =
  'RECONSIDER_PLANTING' | 'USE_CAUTION' | 'LOWER_CONCERN' | 'NOT_ASSESSED';

export type OriginStatus = 'NATIVE' | 'INTRODUCED' | 'UNCERTAIN';

export interface PlantCatalogParams {
  q?: string;
  /** Catalog is assessed-only; callers should not request NOT_ASSESSED values. */
  environmentalConcern?: EnvironmentalConcern[];
  originStatus?: OriginStatus[];
  page?: number;
  size?: number;
  sort?: string;
}

export interface PlantCatalogItem {
  plantId: number;
  commonName: string | null;
  scientificName: string;
  imageUrl: string | null;
  environmentalConcern: EnvironmentalConcern;
  originStatus: OriginStatus | null;
  growthForm: string | null;
  lifeHistory: string | null;
  height: string | null;
}

export interface PlantCatalogResponse {
  items: PlantCatalogItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  sort: string;
}

export interface PlantAlternativesParams {
  limit?: number;
}

export type AlternativesStatus =
  | 'matched'
  | 'no_strict_match_found'
  | 'insufficient_trait_data'
  | 'not_applicable';

export interface CurrentPlantAlternativeSummary {
  plantId: number;
  commonName: string | null;
  scientificName: string;
  imageUrl: string | null;
  environmentalConcern: EnvironmentalConcern;
  growthForm: string | null;
  lifeHistory: string | null;
  woodiness: string | null;
  height: string | null;
}

export interface AlternativePlant {
  plantId: number;
  commonName: string | null;
  scientificName: string;
  imageUrl: string | null;
  environmentalConcern: EnvironmentalConcern;
  originStatus: OriginStatus | null;
  legalStatus: LegalStatus;
  growthForm: string | null;
  lifeHistory: string | null;
  woodiness: string | null;
  height: string | null;
  matchReasons: string[];
}

/** Backend returns only eligible lower-concern, non-regulated assessed candidates. */
export interface PlantAlternativesResponse {
  status: AlternativesStatus;
  currentPlant: CurrentPlantAlternativeSummary;
  alternatives: AlternativePlant[];
}

export interface ComparisonPlant {
  plantId: number;
  commonName: string | null;
  scientificName: string;
  imageUrl: string | null;
  environmentalConcern: EnvironmentalConcern;
  legalStatus: LegalStatus;
  originStatus: OriginStatus;
  growthForm: string | null;
  lifeHistory: string | null;
  woodiness: string | null;
  height: string | null;
  localOccurrence: LocalOccurrence;
}

export interface PlantComparisonResponse {
  plants: ComparisonPlant[];
}

export interface PlantIdentity {
  plantId: number;
  scientificName: string;
  commonName: string | null;
  family: string | null;
  imageUrl: string | null;
}

export type LocalOccurrenceStatus = 'FOUND' | 'NOT_FOUND' | 'UNAVAILABLE';

export type LegalStatus = 'NOT_REGULATED' | 'REGULATED' | 'UNAVAILABLE';

export interface VictorianEstablishment {
  status: string;
  label: string;
}

export interface LocalOccurrence {
  status: LocalOccurrenceStatus;
  recordCount: number | null;
  latestRecordYear: number | null;
}

export interface EnvironmentalConcernDetails {
  status: EnvironmentalConcern;
  source: string;
}

export interface PlantAssessmentResponse {
  plant: PlantIdentity;
  originStatus: OriginStatus;
  victorianEstablishment: VictorianEstablishment;
  localOccurrence: LocalOccurrence;
  environmentalConcern: EnvironmentalConcernDetails;
  legalStatus: LegalStatus;
  recommendation: Recommendation;
}
