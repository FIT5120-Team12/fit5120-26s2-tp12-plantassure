import type { LegalStatus } from '@/types/plant';

export function getLegalStatusLabel(status: LegalStatus): string {
  switch (status) {
    case 'NOT_REGULATED':
      return 'Not regulated';
    case 'REGULATED':
      return 'Regulated';
    case 'UNAVAILABLE':
      return 'Legal status unavailable';
  }
}
