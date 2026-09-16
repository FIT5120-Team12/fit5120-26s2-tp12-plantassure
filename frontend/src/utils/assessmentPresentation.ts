import type {
  EnvironmentalConcernDetails,
  LocalOccurrence,
  Recommendation,
  VictorianEstablishment,
} from '@/types/plant';

export type AssessmentTone = 'concern' | 'caution' | 'lower' | 'neutral' | 'unavailable';

export interface RecommendationPresentation {
  label: string;
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
  level: Recommendation | string,
): RecommendationPresentation {
  switch (level) {
    case 'RECONSIDER_PLANTING':
      return {
        label: 'Reconsider Planting',
        tone: 'concern',
        icon: 'mdi-alert-outline',
        guidance:
          'This species can establish and spread in the local environment, so another plant may be a better choice for your garden.',
      };
    case 'USE_CAUTION':
      return {
        label: 'Use Caution',
        tone: 'caution',
        icon: 'mdi-alert-circle-outline',
        guidance:
          'Review the available evidence carefully and consider how the plant will be monitored and managed over time.',
      };
    case 'LOWER_CONCERN':
      return {
        label: 'Lower Concern',
        tone: 'lower',
        icon: 'mdi-leaf-circle-outline',
        guidance:
          'Available data indicates a lower assessed weed-risk profile. Continue to monitor for unexpected spread over time.',
      };
    case 'NOT_ASSESSED':
      return {
        label: 'Not Assessed',
        tone: 'neutral',
        icon: 'mdi-information-outline',
        guidance:
          'Environmental risk information is limited for this plant. This result does not indicate that it is free of environmental risk.',
      };
    default:
      return {
        label: 'Assessment unavailable',
        tone: 'neutral',
        icon: 'mdi-information-outline',
        guidance: 'Review the available evidence before making a planting decision.',
      };
  }
}

export function getEstablishmentPresentation(
  establishment: VictorianEstablishment,
): EstablishmentPresentation {
  return {
    label: establishment.label,
    supporting: formatRawLabel(establishment.status),
    tone: 'neutral',
  };
}

export function getLocalOccurrencePresentation(occurrence: LocalOccurrence): EvidencePresentation {
  switch (occurrence.status) {
    case 'FOUND': {
      const latest = occurrence.latestRecordYear;
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
        label: 'No matching local records found',
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

export function getEnvironmentalRiskTone(rating: string | null): AssessmentTone {
  switch (rating?.toLowerCase()) {
    case 'very_high':
    case 'very high':
    case 'high':
      return 'concern';
    case 'moderately_high':
    case 'moderately high':
    case 'medium':
      return 'caution';
    case 'lower':
      return 'lower';
    case 'unavailable':
      return 'unavailable';
    default:
      return 'neutral';
  }
}

export function getEnvironmentalConcernChipColor(
  concern: string | null,
): 'accent' | 'secondary' | 'primary' | undefined {
  switch (getEnvironmentalRiskTone(concern)) {
    case 'concern':
      return 'accent';
    case 'caution':
      return 'secondary';
    case 'lower':
      return 'primary';
    default:
      return undefined;
  }
}

export function getEnvironmentalConcernPresentation(
  concern: EnvironmentalConcernDetails,
): EvidencePresentation {
  switch (concern.status) {
    case 'VERY_HIGH':
    case 'HIGH':
    case 'MODERATELY_HIGH':
    case 'MEDIUM':
    case 'LOWER':
      return {
        label: formatRawLabel(concern.status),
        supporting: 'Environmental concern assessment',
        explanation: 'Environmental concern information is available for this plant.',
        tone: getEnvironmentalRiskTone(concern.status),
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
        supporting: 'The environmental concern check could not be completed',
        explanation:
          'Environmental concern information is currently unavailable. This check could not be completed; the available establishment and occurrence evidence remains shown.',
        tone: 'unavailable',
        icon: 'mdi-information-outline',
      };
    default:
      return {
        label: formatRawLabel(String(concern.status)),
        supporting: 'Environmental concern status',
        explanation: 'Review the available environmental concern information.',
        tone: 'neutral',
        icon: 'mdi-sprout-outline',
      };
  }
}
