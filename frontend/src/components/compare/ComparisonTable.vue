<script setup lang="ts">
  import {
    getEnvironmentalRiskTone,
    getLocalOccurrencePresentation,
  } from '@/utils/assessmentPresentation';
  import type { ComparisonPlant, LocalOccurrence } from '@/types/plant';

  defineProps<{
    plants: ComparisonPlant[];
  }>();

  const comparisonRows: Array<{
    key: Exclude<keyof ComparisonPlant, 'plantId' | 'commonName'>;
    label: string;
  }> = [
    { key: 'environmentalConcern', label: 'Environmental concern' },
    { key: 'legalStatus', label: 'Legal status' },
    { key: 'originStatus', label: 'Origin status' },
    { key: 'growthForm', label: 'Growth form' },
    { key: 'lifeHistory', label: 'Life history' },
    { key: 'woodiness', label: 'Woodiness' },
    { key: 'height', label: 'Height' },
    { key: 'localOccurrence', label: 'Local occurrence' },
  ];

  function displayValue(value: unknown): string {
    if (typeof value !== 'string' || value.trim() === '') return 'Unavailable';
    return value === 'NOT_ASSESSED' ? 'Not Assessed' : value;
  }

  function displayLocalOccurrence(occurrence: LocalOccurrence): string {
    const presentation = getLocalOccurrencePresentation(occurrence);

    return `${presentation.label} · ${presentation.supporting}`;
  }

  function plantLabel(plant: ComparisonPlant, index: number): string {
    return plant.commonName?.trim() || `Plant ${index + 1}`;
  }
</script>

<template>
  <div class="comparison-table__scroll" tabindex="0" aria-label="Plant comparison table">
    <table class="comparison-table">
      <caption class="visually-hidden">
        Compare environmental concern, legal status, origin, form, life history, woodiness, height,
        and local occurrence for selected plants.
      </caption>
      <thead>
        <tr>
          <th scope="col">Attribute</th>
          <th v-for="(plant, index) in plants" :key="plant.plantId" scope="col">
            {{ plantLabel(plant, index) }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in comparisonRows" :key="row.key">
          <th scope="row">{{ row.label }}</th>
          <td v-for="plant in plants" :key="plant.plantId">
            <template v-if="row.key === 'environmentalConcern'">
              <span class="comparison-table__concern">
                <v-icon
                  :class="`comparison-table__concern-icon--${getEnvironmentalRiskTone(plant.environmentalConcern ?? null)}`"
                  icon="mdi-circle"
                  size="10"
                  aria-hidden="true"
                />
                <span>{{ displayValue(plant.environmentalConcern) }}</span>
              </span>
            </template>
            <template v-else-if="row.key === 'localOccurrence'">
              {{ displayLocalOccurrence(plant.localOccurrence) }}
            </template>
            <template v-else>
              {{ displayValue(plant[row.key]) }}
            </template>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
  .comparison-table__scroll {
    overflow-x: auto;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    background: var(--color-surface);
  }

  .comparison-table {
    width: max-content;
    min-width: 100%;
    border-collapse: separate;
    border-spacing: 0;
    color: var(--color-ink-soft);
  }

  .comparison-table th,
  .comparison-table td {
    min-width: 11rem;
    padding: var(--space-md);
    border-bottom: 1px solid var(--color-border);
    border-right: 1px solid var(--color-border);
    text-align: left;
    vertical-align: middle;
  }

  .comparison-table th {
    color: var(--color-primary);
    font-size: 0.875rem;
    font-weight: 700;
  }

  .comparison-table thead th {
    background: var(--color-surface-muted);
  }

  .comparison-table th:first-child {
    position: sticky;
    left: 0;
    z-index: 1;
    min-width: 10rem;
    background: var(--color-surface-muted);
  }

  .comparison-table tbody th:first-child {
    background: var(--color-surface);
  }

  .comparison-table tr > :last-child {
    border-right: 0;
  }

  .comparison-table tbody tr:last-child > * {
    border-bottom: 0;
  }

  .comparison-table__concern {
    display: inline-flex;
    align-items: center;
    gap: var(--space-xs);
  }

  .comparison-table__concern-icon--concern,
  .comparison-table__concern-icon--caution {
    color: var(--color-accent);
  }

  .comparison-table__concern-icon--lower {
    color: var(--color-primary);
  }

  .comparison-table__concern-icon--neutral,
  .comparison-table__concern-icon--unavailable {
    color: var(--color-muted);
  }

  @media (max-width: 767px) {
    .comparison-table th,
    .comparison-table td {
      min-width: 10rem;
      padding: var(--space-sm) var(--space-md);
    }

    .comparison-table th:first-child {
      min-width: 9rem;
    }
  }
</style>
