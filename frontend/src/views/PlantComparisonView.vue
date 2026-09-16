<script setup lang="ts">
  import { computed, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';

  import { comparePlants } from '@/api/plants';
  import ComparisonPlantHeader from '@/components/compare/ComparisonPlantHeader.vue';
  import ComparisonTable from '@/components/compare/ComparisonTable.vue';
  import AppFooter from '@/components/layout/AppFooter.vue';
  import AppHeader from '@/components/layout/AppHeader.vue';
  import type { ComparisonPlant } from '@/types/plant';

  const COMPARISON_ERROR_MESSAGE = 'We couldn’t load plant comparisons. Please try again.';

  const route = useRoute();
  const router = useRouter();

  const comparisonPlants = ref<ComparisonPlant[]>([]);
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  const hasLoaded = ref(false);
  let requestId = 0;

  const selectedPlantIds = computed(() => {
    const queryValues = Array.isArray(route.query.plants)
      ? route.query.plants
      : [route.query.plants];
    const ids = queryValues
      .flatMap((value) => value?.split(',') ?? [])
      .map((value) => Number(value.trim()))
      .filter((value) => Number.isSafeInteger(value) && value > 0);

    return [...new Set(ids)].slice(0, 3);
  });

  const hasValidSelection = computed(() => selectedPlantIds.value.length >= 2);
  const hasComparisonData = computed(
    () =>
      comparisonPlants.value.length === selectedPlantIds.value.length &&
      selectedPlantIds.value.every((plantId) =>
        comparisonPlants.value.some((plant) => plant.plantId === plantId),
      ),
  );

  async function loadComparison(): Promise<void> {
    const plantIds = selectedPlantIds.value;
    const currentRequestId = ++requestId;

    if (plantIds.length < 2) {
      comparisonPlants.value = [];
      isLoading.value = false;
      error.value = null;
      hasLoaded.value = false;
      return;
    }

    isLoading.value = true;
    error.value = null;
    comparisonPlants.value = [];
    hasLoaded.value = false;

    try {
      const response = await comparePlants(plantIds);

      if (currentRequestId !== requestId) return;

      comparisonPlants.value = response.plants;
      hasLoaded.value = true;
    } catch {
      if (currentRequestId !== requestId) return;

      error.value = COMPARISON_ERROR_MESSAGE;
      hasLoaded.value = true;
    } finally {
      if (currentRequestId === requestId) isLoading.value = false;
    }
  }

  watch(selectedPlantIds, () => {
    void loadComparison();
  }, { immediate: true });

  const alternativesRoute = computed(() => {
    const value = route.query.fromPlantId;
    if (typeof value !== 'string') return null;

    const plantId = Number(value);
    return Number.isSafeInteger(plantId) && plantId > 0
      ? { name: 'plant-alternatives', params: { plantId } }
      : null;
  });

  function returnToAlternatives() {
    if (alternativesRoute.value) {
      void router.push(alternativesRoute.value);
      return;
    }

    router.back();
  }

  function viewAssessment(plantId: number) {
    void router.push({ name: 'plant-assessment', params: { plantId } });
  }

  function retryComparison() {
    void loadComparison();
  }

  function goToSearch() {
    void router.push({ name: 'home', hash: '#plant-search-input' });
  }
</script>

<template>
  <div class="comparison-page">
    <AppHeader @check-plant="goToSearch" />

    <main class="comparison-page__main">
      <div class="app-container">
        <v-btn
          class="comparison-page__back"
          color="primary"
          variant="text"
          prepend-icon="mdi-arrow-left"
          @click="returnToAlternatives"
        >
          Back to alternatives
        </v-btn>

        <header class="comparison-page__intro">
          <p class="comparison-page__eyebrow">COMPARE PLANTS</p>
          <h1>Compare your options</h1>
          <p>Review verified plant information side by side before choosing.</p>
        </header>

        <section
          v-if="!hasValidSelection"
          class="comparison-page__state"
          aria-labelledby="compare-state-title"
        >
          <v-sheet border rounded="md" color="surface" class="comparison-page__state-card">
            <v-icon icon="mdi-information-outline" size="28" color="secondary" aria-hidden="true" />
            <div>
              <h2 id="compare-state-title">Choose at least two plants to compare</h2>
              <p>
                Select two or three alternatives to review their verified information side by side.
              </p>
            </div>
            <v-btn color="primary" variant="outlined" @click="returnToAlternatives">
              Return to alternatives
            </v-btn>
          </v-sheet>
        </section>

        <div v-else-if="isLoading" class="comparison-page__loading" aria-live="polite">
          <v-progress-circular indeterminate color="primary" size="28" aria-hidden="true" />
          <span>Loading plant comparisons…</span>
        </div>

        <v-alert v-else-if="error" class="comparison-page__data-state" type="error" variant="tonal">
          {{ error }}
          <template #append>
            <v-btn color="primary" variant="outlined" size="small" @click="retryComparison">
              Try again
            </v-btn>
          </template>
        </v-alert>

        <template v-else-if="hasLoaded && !hasComparisonData">
          <v-alert
            class="comparison-page__data-state"
            type="info"
            variant="tonal"
            title="Comparison data is unavailable"
          >
            Verified comparison information is not available for the selected plants.
          </v-alert>
        </template>

        <template v-else>
          <section class="comparison-page__headers" aria-label="Plants being compared">
            <ComparisonPlantHeader
              v-for="plant in comparisonPlants"
              :key="plant.plantId"
              :plant-id="plant.plantId"
              :common-name="plant.commonName ?? null"
              :scientific-name="plant.scientificName"
              :image-url="plant.imageUrl ?? null"
              :environmental-concern="plant.environmentalConcern ?? null"
              :origin-status="plant.originStatus ?? null"
              @select="viewAssessment"
            />
          </section>

          <ComparisonTable :plants="comparisonPlants" />

          <v-sheet
            class="comparison-page__occurrence-note"
            border
            rounded="md"
            color="surface"
          >
            <v-icon icon="mdi-information-outline" size="24" color="primary" aria-hidden="true" />
            <div>
              <h2>About local occurrence</h2>
              <p>
                Local occurrence is evidence of recorded presence, not a measure of environmental
                risk.
              </p>
            </div>
          </v-sheet>
        </template>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
  .comparison-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background: var(--color-canvas);
  }

  .comparison-page__main {
    flex: 1;
    padding-block: var(--space-xl) var(--space-4xl);
  }

  .comparison-page__back {
    margin-left: calc(var(--space-md) * -1);
  }

  .comparison-page__intro {
    max-width: 48rem;
    margin: var(--space-xl) 0 var(--space-2xl);
  }

  .comparison-page__eyebrow {
    margin: 0 0 var(--space-sm);
    color: var(--color-accent);
    font-size: 0.75rem;
    font-weight: 700;
    letter-spacing: 0.1em;
  }

  .comparison-page__intro h1 {
    margin: 0;
    color: var(--color-primary);
    font-size: clamp(2.75rem, 6vw, 4.5rem);
    line-height: 0.98;
  }

  .comparison-page__intro > p:last-child {
    margin: var(--space-md) 0 0;
    color: var(--color-ink-soft);
    font-size: 1.0625rem;
  }

  .comparison-page__state-card,
  .comparison-page__occurrence-note {
    display: grid;
    grid-template-columns: auto minmax(0, 1fr) auto;
    align-items: center;
    gap: var(--space-lg);
    padding: var(--space-lg);
  }

  .comparison-page__state-card h2,
  .comparison-page__occurrence-note h2 {
    margin: 0;
    color: var(--color-primary);
    font-family: var(--font-body);
    font-size: 1.125rem;
    font-weight: 700;
  }

  .comparison-page__state-card p,
  .comparison-page__occurrence-note p {
    margin: var(--space-xs) 0 0;
    color: var(--color-ink-soft);
  }

  .comparison-page__data-state {
    margin-top: var(--space-lg);
  }

  .comparison-page__loading {
    display: flex;
    align-items: center;
    gap: var(--space-sm);
    color: var(--color-ink-soft);
  }

  .comparison-page__headers {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(min(100%, 16rem), 1fr));
    gap: var(--space-md);
    margin-bottom: var(--space-lg);
  }

  .comparison-page__occurrence-note {
    margin-top: var(--space-lg);
  }

  @media (max-width: 767px) {
    .comparison-page__main {
      padding-block: var(--space-lg) var(--space-3xl);
    }

    .comparison-page__state-card,
    .comparison-page__occurrence-note {
      grid-template-columns: auto minmax(0, 1fr);
    }

    .comparison-page__state-card .v-btn {
      grid-column: 1 / -1;
      justify-self: start;
    }
  }

  @media (max-width: 479px) {
    .comparison-page__state-card,
    .comparison-page__occurrence-note {
      grid-template-columns: 1fr;
      justify-items: start;
    }

    .comparison-page__state-card .v-btn {
      grid-column: auto;
      width: 100%;
    }
  }
</style>
