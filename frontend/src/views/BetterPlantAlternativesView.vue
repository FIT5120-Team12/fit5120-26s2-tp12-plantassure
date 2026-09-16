<script setup lang="ts">
  import { computed, ref, watch } from 'vue';
  import { storeToRefs } from 'pinia';
  import { RouterLink, useRoute, useRouter } from 'vue-router';

  import AlternativePlantCard from '@/components/alternatives/AlternativePlantCard.vue';
  import AlternativesEmptyState from '@/components/alternatives/AlternativesEmptyState.vue';
  import CurrentPlantSummary from '@/components/alternatives/CurrentPlantSummary.vue';
  import AppFooter from '@/components/layout/AppFooter.vue';
  import AppHeader from '@/components/layout/AppHeader.vue';
  import { usePlantAlternativesStore } from '@/stores/alternatives';

  const route = useRoute();
  const router = useRouter();
  const alternativesStore = usePlantAlternativesStore();
  const { alternatives, currentPlant, error, hasLoaded, isLoading } = storeToRefs(alternativesStore);
  const selectedPlantIds = ref<number[]>([]);

  const plantId = computed<number | null>(() => {
    const value = Number(route.params.plantId);

    return Number.isSafeInteger(value) && value > 0 ? value : null;
  });
  const selectedCount = computed(() => selectedPlantIds.value.length);

  function loadAlternatives(id: number | null) {
    selectedPlantIds.value = [];

    if (id !== null) void alternativesStore.fetchAlternatives(id);
  }

  watch(plantId, loadAlternatives, { immediate: true });

  watch([alternatives, isLoading], ([plants, loading]) => {
    if (loading) return;

    const availablePlantIds = new Set(plants.map((plant) => plant.plantId));
    selectedPlantIds.value = selectedPlantIds.value.filter((id) => availablePlantIds.has(id));
  });

  function toggleCompare(id: number) {
    if (selectedPlantIds.value.includes(id)) {
      selectedPlantIds.value = selectedPlantIds.value.filter((plantId) => plantId !== id);
      return;
    }

    if (selectedPlantIds.value.length >= 3) return;
    selectedPlantIds.value = [...selectedPlantIds.value, id];
  }

  function compareSelected() {
    if (plantId.value === null || selectedPlantIds.value.length < 2) return;

    void router.push({
      name: 'plant-comparison',
      query: {
        plants: selectedPlantIds.value.join(','),
        fromPlantId: String(plantId.value),
      },
    });
  }

  function retryAlternatives() {
    if (plantId.value !== null) void alternativesStore.fetchAlternatives(plantId.value);
  }

  function browsePlants() {
    void router.push({ name: 'plant-catalog' });
  }

  function goToSearch() {
    void router.push({ name: 'home', hash: '#plant-search-input' });
  }
</script>

<template>
  <div class="alternatives-page">
    <AppHeader @check-plant="goToSearch" />

    <main class="alternatives-main">
      <div class="app-container">
        <RouterLink
          v-if="plantId !== null"
          class="alternatives-back-link"
          :to="{ name: 'plant-assessment', params: { plantId } }"
        >
          <v-icon icon="mdi-arrow-left" size="18" aria-hidden="true" />
          Back to assessment
        </RouterLink>

        <header class="alternatives-intro">
          <p class="alternatives-eyebrow">BETTER PLANT OPTIONS</p>
          <h1>Find a better plant</h1>
          <p>
            Explore alternatives with lower documented environmental concern and similar supported
            traits for your garden.
          </p>
        </header>

        <CurrentPlantSummary
          v-if="plantId !== null && currentPlant"
          :common-name="currentPlant.commonName"
          :scientific-name="currentPlant.scientificName"
          :image-url="currentPlant.imageUrl"
          :environmental-concern="currentPlant.environmentalConcern"
          :growth-form="currentPlant.growthForm"
          :life-history="currentPlant.lifeHistory"
          :height="currentPlant.height"
        />

        <section class="alternatives-results" aria-labelledby="alternatives-title">
          <div class="alternatives-results__header">
            <div>
              <p class="alternatives-eyebrow">ALTERNATIVES</p>
              <h2 id="alternatives-title">Consider these options</h2>
            </div>
            <v-btn
              v-if="selectedCount >= 2"
              color="primary"
              variant="outlined"
              type="button"
              @click="compareSelected"
            >
              Compare plants ({{ selectedCount }})
            </v-btn>
          </div>

          <v-alert v-if="plantId === null" type="warning" variant="tonal">
            We couldn’t find that plant.
          </v-alert>
          <div v-else-if="isLoading" class="alternatives-status" aria-live="polite">
            <v-progress-circular indeterminate color="primary" size="28" aria-hidden="true" />
            <span>Loading alternative plants…</span>
          </div>
          <v-alert v-else-if="error" type="error" variant="tonal">
            {{ error }}
            <template #append>
              <v-btn color="primary" variant="outlined" size="small" @click="retryAlternatives">
                Try again
              </v-btn>
            </template>
          </v-alert>
          <div v-else-if="hasLoaded && alternatives.length" class="alternatives-grid">
            <AlternativePlantCard
              v-for="plant in alternatives"
              :key="plant.plantId"
              :plant-id="plant.plantId"
              :common-name="plant.commonName"
              :scientific-name="plant.scientificName"
              :image-url="plant.imageUrl"
              :environmental-concern="plant.environmentalConcern"
              :origin-status="plant.originStatus"
              :match-reasons="plant.matchReasons"
              :growth-form="plant.growthForm"
              :life-history="plant.lifeHistory"
              :height="plant.height"
              :selected="selectedPlantIds.includes(plant.plantId)"
              :compare-disabled="selectedCount >= 3 && !selectedPlantIds.includes(plant.plantId)"
              @select="(id) => router.push({ name: 'plant-assessment', params: { plantId: id } })"
              @toggle-compare="toggleCompare"
            />
          </div>
          <AlternativesEmptyState
            v-else-if="hasLoaded && currentPlant"
            @browse="browsePlants"
          />
        </section>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
  .alternatives-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background: var(--color-canvas);
  }

  .alternatives-main {
    flex: 1;
    padding-block: var(--space-xl) var(--space-4xl);
  }

  .alternatives-back-link {
    display: inline-flex;
    align-items: center;
    gap: var(--space-xs);
    color: var(--color-ink-soft);
    font-size: 0.9375rem;
    text-decoration: none;
  }

  .alternatives-intro {
    max-width: 42rem;
    margin: var(--space-2xl) 0 var(--space-2xl);
  }

  .alternatives-eyebrow {
    margin: 0 0 var(--space-sm);
    color: var(--color-accent);
    font-size: 0.75rem;
    font-weight: 700;
    letter-spacing: 0.1em;
  }

  .alternatives-intro h1,
  .alternatives-results h2 {
    margin: 0;
    color: var(--color-primary);
  }

  .alternatives-intro h1 {
    font-size: clamp(2.75rem, 6vw, 4.5rem);
    line-height: 0.98;
  }

  .alternatives-intro > p:last-child {
    margin: var(--space-md) 0 0;
    color: var(--color-ink-soft);
    font-size: 1.0625rem;
  }

  .alternatives-results {
    margin-top: var(--space-3xl);
  }

  .alternatives-results__header {
    display: flex;
    align-items: end;
    justify-content: space-between;
    gap: var(--space-lg);
    margin-bottom: var(--space-lg);
  }

  .alternatives-results__header .alternatives-eyebrow {
    margin-bottom: var(--space-xs);
  }

  .alternatives-results h2 {
    font-size: 2rem;
    line-height: 1.1;
  }

  .alternatives-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: var(--space-lg);
  }

  .alternatives-status {
    display: flex;
    align-items: center;
    gap: var(--space-sm);
    color: var(--color-ink-soft);
  }

  @media (max-width: 999px) {
    .alternatives-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 767px) {
    .alternatives-main {
      padding-block: var(--space-lg) var(--space-3xl);
    }

    .alternatives-intro {
      margin-block: var(--space-xl);
    }

    .alternatives-results {
      margin-top: var(--space-2xl);
    }

    .alternatives-results__header {
      align-items: start;
      flex-direction: column;
    }

    .alternatives-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
