import type { OriginStatus } from '@/types/plant';

export function getOriginStatusLabel(status: OriginStatus | null): string {
  switch (status) {
    case 'NATIVE':
      return 'Native';
    case 'INTRODUCED':
      return 'Introduced';
    case 'UNCERTAIN':
      return 'Uncertain';
    default:
      return 'Unavailable';
  }
}
