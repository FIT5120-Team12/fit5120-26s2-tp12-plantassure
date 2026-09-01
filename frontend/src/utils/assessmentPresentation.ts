import type {
  EnvironmentalRisk,
  LocalOccurrence,
  PlantIdentity,
  RecommendationLevel,
} from '@/types/plant';

export type AssessmentTone = 'concern' | 'caution' | 'lower' | 'neutral' | 'unavailable';

export interface RecommendationPresentation {
  tone: AssessmentTone;
  icon: string;
  guidance: string;
}

export interface EvidencePresentation {
  label: string | number;
  supporting: string;
  explanation: string;
  tone: AssessmentTone;
  icon: string;
}

export interface EstablishmentPresentation {
  label: string;
  supporting: string;
  explanation: string;
  badges: string[];
  tone: AssessmentTone;
}

function formatRawLabel(value: string): string {
  return value
    .toLowerCase()
    .split('_')
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}

export function getRecommendationPresentation(
  level: RecommendationLevel | string,
): RecommendationPresentation {
  switch (level) {
    case 'RECONSIDER_PLANTING':
      return {
        tone: 'concern',
        icon: 'mdi-alert-outline',
        guidance:
          'Before purchasing or planting this species, consider whether another plant may be a more suitable choice.',
      };
    case 'USE_CAUTION':
      return {
        tone: 'caution',
        icon: 'mdi-alert-circle-outline',
        guidance:
          'Review the available evidence carefully and consider how the plant will be monitored and managed over time.',
      };
    case 'LOWER_CONCERN':
      return {
        tone: 'lower',
        icon: 'mdi-leaf-circle-outline',
        guidance:
          'Available data indicates a lower assessed weed-risk profile. Continue to monitor for unexpected spread over time.',
      };
    case 'NOT_ASSESSED':
      return {
        tone: 'neutral',
        icon: 'mdi-information-outline',
        guidance:
          'Environmental risk information is limited for this plant. This result does not indicate that it is free of environmental risk.',
      };
    default:
      return {
        tone: 'neutral',
        icon: 'mdi-information-outline',
        guidance: 'Review the available evidence before making a planting decision.',
      };
  }
}

export function getEstablishmentPresentation(plant: PlantIdentity): EstablishmentPresentation {
  const badges: string[] = [];
  if (plant.establishmentMeans) badges.push(plant.establishmentMeans);
  if (plant.degreeOfEstablishment) badges.push(plant.degreeOfEstablishment);
  const label = plant.establishmentMeans ?? 'Not provided';
  const supporting = plant.degreeOfEstablishment ?? 'Establishment detail not provided';

  switch (plant.degreeOfEstablishment) {
    case 'Naturalised':
      return {
        label,
        supporting,
        badges,
        tone: 'neutral',
        explanation:
          'Naturalised means the species has established self-sustaining populations outside cultivation in Victoria.',
      };
    case 'Native':
      return {
        label,
        supporting,
        badges,
        tone: 'lower',
        explanation:
          'VicFlora identifies this species as native and naturally occurring in Victoria.',
      };
    case 'Adventive':
      return {
        label,
        supporting,
        badges,
        tone: 'neutral',
        explanation: 'VicFlora reports the degree of establishment as Adventive.',
      };
    case null:
      return {
        label,
        supporting,
        badges,
        tone: 'neutral',
        explanation: plant.establishmentMeans
          ? `VicFlora identifies this plant as ${plant.establishmentMeans.toLowerCase()} in Victoria.`
          : 'Victorian establishment information was not provided.',
      };
    default:
      return {
        label,
        supporting,
        badges,
        tone: 'neutral',
        explanation: `VicFlora reports the establishment detail as ${String(plant.degreeOfEstablishment)}.`,
      };
  }
}

export function getLocalOccurrencePresentation(occurrence: LocalOccurrence): EvidencePresentation {
  switch (occurrence.status) {
    case 'FOUND': {
      const latest = occurrence.mostRecentRecordYear;
      return {
        label: occurrence.recordCount ?? 'Records found',
        supporting:
          latest === null ? 'documented records' : `documented records · latest ${latest}`,
        explanation:
          'These are documented Victorian Biodiversity Atlas records matching this species within the City of Monash.',
        tone: 'neutral',
        icon: 'mdi-map-marker-outline',
      };
    }
    case 'NOT_FOUND':
      return {
        label: 'No matching VBA records found',
        supporting: 'No matching records in the City of Monash',
        explanation:
          'No matching VBA records were found in the City of Monash. Absence of matching records does not confirm that the species is absent from the area.',
        tone: 'neutral',
        icon: 'mdi-map-marker-off-outline',
      };
    case 'UNAVAILABLE':
      return {
        label: 'Unavailable',
        supporting: 'The occurrence check could not be completed',
        explanation:
          'Local occurrence information is currently unavailable. This lookup could not be completed at this time.',
        tone: 'unavailable',
        icon: 'mdi-information-outline',
      };
    default:
      return {
        label: formatRawLabel(String(occurrence.status)),
        supporting: 'Local occurrence status',
        explanation: 'Review the available local occurrence information.',
        tone: 'neutral',
        icon: 'mdi-map-marker-outline',
      };
  }
}

function assessedRiskTone(rating: string | null): AssessmentTone {
  switch (rating?.toLowerCase()) {
    case 'very high':
    case 'high':
      return 'concern';
    case 'moderately high':
    case 'medium':
      return 'caution';
    case 'lower':
      return 'lower';
    default:
      return 'neutral';
  }
}

export function getEnvironmentalRiskPresentation(risk: EnvironmentalRisk): EvidencePresentation {
  switch (risk.assessmentStatus) {
    case 'ASSESSED':
      return {
        label: risk.rating ?? 'Assessed',
        supporting: 'Environmental weed-risk rating',
        explanation: risk.explanation,
        tone: assessedRiskTone(risk.rating),
        icon: 'mdi-sprout-outline',
      };
    case 'NOT_ASSESSED':
      return {
        label: 'Not Assessed',
        supporting: 'No exact matching Advisory List assessment',
        explanation:
          'No exact matching assessment was found in the 2022 Advisory List of Environmental Weeds in Victoria. This does not indicate that the plant is free of environmental risk.',
        tone: 'neutral',
        icon: 'mdi-help-circle-outline',
      };
    case 'UNAVAILABLE':
      return {
        label: 'Unavailable',
        supporting: 'The environmental-risk check could not be completed',
        explanation:
          'Environmental weed risk information is currently unavailable. This check could not be completed; the available establishment and occurrence evidence remains shown.',
        tone: 'unavailable',
        icon: 'mdi-information-outline',
      };
    default:
      return {
        label: formatRawLabel(String(risk.assessmentStatus)),
        supporting: 'Environmental weed-risk status',
        explanation:
          risk.explanation || 'Review the available environmental weed-risk information.',
        tone: 'neutral',
        icon: 'mdi-sprout-outline',
      };
  }
}
