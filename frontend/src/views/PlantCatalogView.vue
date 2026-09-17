<script setup lang="ts">
  import { storeToRefs } from 'pinia';
  import { computed, onMounted, watch } from 'vue';
  import { useRouter } from 'vue-router';

  import AppFooter from '@/components/layout/AppFooter.vue';
  import AppHeader from '@/components/layout/AppHeader.vue';
  import CatalogSearchBar from '@/components/catalog/CatalogSearchBar.vue';
  import PlantCard from '@/components/catalog/PlantCard.vue';
  import PlantCatalogFilters from '@/components/catalog/PlantCatalogFilters.vue';
  import CatalogPagination from '@/components/catalog/CatalogPagination.vue';
  import catalogHeroBotanical from '@/assets/images/catalog-hero-botanical.png';
  import { usePlantCatalogStore } from '@/stores/catalog';
  import type { EnvironmentalConcern, OriginStatus } from '@/types/plant';

  const router = useRouter();
  const catalogStore = usePlantCatalogStore();
  const {
    items,
    query,
    environmentalConcern,
    originStatus,
    page,
    totalElements,
    totalPages,
    isLoading,
    error,
    hasLoaded,
  } = storeToRefs(catalogStore);
  const homeSearchRoute = { name: 'home', hash: '#plant-search-input' } as const;

  const selectedEnvironmentalConcern = computed<EnvironmentalConcern | null>({
    get: () => environmentalConcern.value[0] ?? null,
    set: (value) => {
      environmentalConcern.value = value ? [value] : [];
    },
  });
  const selectedOriginStatus = computed<OriginStatus | null>({
    get: () => originStatus.value[0] ?? null,
    set: (value) => {
      originStatus.value = value ? [value] : [];
    },
  });
  const catalogPage = computed({
    get: () => page.value + 1,
    set: (value: number) => {
      page.value = Math.max(0, value - 1);
      void catalogStore.fetchPlants();
    },
  });

  function selectPlant(plantId: number) {
    void router.push({ name: 'plant-assessment', params: { plantId } });
  }

  function goToSearch() {
    void router.push(homeSearchRoute);
  }

  function searchCatalog(value: string) {
    query.value = value.trim();
    resetPageAndFetch();
  }

  function resetPageAndFetch() {
    page.value = 0;
    void catalogStore.fetchPlants();
  }

  watch([environmentalConcern, originStatus], resetPageAndFetch);

  onMounted(() => {
    void catalogStore.fetchPlants();
  });
</script>

<template>
  <div class="catalog-page">
    <AppHeader @check-plant="goToSearch" />
    <main>
      <section class="catalog-intro" aria-labelledby="catalog-title">
        <div class="app-container">
          <div class="catalog-intro__content">
            <div class="catalog-intro__copy">
              <p class="catalog-eyebrow">CITY OF MONASH · PLANT CATALOG</p>
              <h1 id="catalog-title">Browse assessed plants</h1>
              <p>
                Explore plants with verified assessment information to support your next planting
                decision.
              </p>
            </div>
          </div>
          <div class="catalog-intro__visual">
            <v-img
              :src="catalogHeroBotanical"
              alt=""
              contain
              position="right bottom"
              aria-hidden="true"
            />
          </div>
        </div>
      </section>

      <section class="catalog-content" aria-label="Assessed plant catalogue">
        <div class="app-container">
          <CatalogSearchBar v-model="query" @search="searchCatalog" />
          <PlantCatalogFilters
            v-model:environmental-concern="selectedEnvironmentalConcern"
            v-model:origin-status="selectedOriginStatus"
          />

          <div class="catalog-results-heading">
            <p v-if="hasLoaded && !isLoading && !error">{{ totalElements }} assessed plants</p>
          </div>

          <section v-if="isLoading" class="catalog-state" role="status" aria-live="polite">
            <v-progress-circular indeterminate :size="28" :width="3" color="primary" />
            <span>Loading assessed plants…</span>
          </section>
          <v-alert
            v-else-if="error"
            type="error"
            variant="tonal"
            title="Assessed plants unavailable"
          >
            {{ error }}
            <template #append>
              <v-btn
                color="primary"
                variant="outlined"
                @click="() => catalogStore.fetchPlants()"
              >
                Try again
              </v-btn>
            </template>
          </v-alert>
          <div v-else-if="hasLoaded && items.length" class="catalog-grid">
            <PlantCard
              v-for="plant in items"
              :key="plant.plantId"
              v-bind="plant"
              @select="selectPlant"
            />
          </div>
          <v-alert
            v-else-if="hasLoaded"
            type="info"
            variant="tonal"
            title="No assessed plants found"
          >
            Try a different search or clear one of the filters.
          </v-alert>
          <CatalogPagination
            v-if="hasLoaded && !error && totalPages > 1"
            v-model="catalogPage"
            :length="totalPages"
            :disabled="isLoading"
          />
        </div>
      </section>
    </main>
    <AppFooter />
  </div>
</template>

<style scoped>
  .catalog-page {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background: var(--color-canvas);
  }

  main {
    flex: 1;
  }

  .catalog-intro {
    border-bottom: 1px solid var(--color-border);
  }

  .catalog-intro > .app-container {
    --hero-grid-gutter: max(var(--space-xl), calc((100vw - var(--content-max-width)) / 2));

    width: 100%;
    max-width: none;
    min-height: 600px;
    display: grid;
    grid-template-columns:
      minmax(0, calc(56vw - var(--hero-grid-gutter)))
      minmax(0, 44vw);
    align-items: center;
    padding-left: var(--hero-grid-gutter);
  }

  .catalog-intro__content {
    width: 100%;
    display: flex;
    align-items: center;
    padding: var(--space-3xl) var(--space-2xl) var(--space-3xl) 0;
  }

  .catalog-intro__copy {
    width: 100%;
    max-width: 560px;
  }

  .catalog-intro__visual {
    align-self: stretch;
    min-width: 0;
    padding-left: var(--space-lg);
  }

  .catalog-intro__visual :deep(.v-img) {
    height: 100%;
  }

  .catalog-eyebrow {
    margin: 0 0 var(--space-sm);
    color: var(--color-accent);
    font-size: 0.75rem;
    font-weight: 800;
    letter-spacing: 0.12em;
  }

  .catalog-intro h1 {
    max-width: 16ch;
    margin: 0;
    font-size: clamp(2.75rem, 6vw, 4.5rem);
    line-height: 0.98;
  }

  .catalog-intro p:last-child {
    max-width: 42rem;
    margin: var(--space-md) 0 0;
    color: var(--color-ink-soft);
    font-size: 1.0625rem;
  }

  .catalog-content {
    padding-block: var(--space-2xl) var(--space-4xl);
  }

  :deep(.plant-catalog-filters) {
    margin-top: var(--space-lg);
  }

  .catalog-results-heading {
    margin: var(--space-2xl) 0 var(--space-lg);
    border-top: 1px solid var(--color-border);
  }

  .catalog-results-heading p {
    margin: var(--space-lg) 0 0;
    color: var(--color-ink-soft);
    font-size: 0.875rem;
  }

  .catalog-state {
    display: flex;
    align-items: center;
    gap: var(--space-sm);
    min-height: 96px;
    color: var(--color-ink-soft);
  }

  .catalog-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: var(--space-lg);
  }

  @media (max-width: 999px) {
    .catalog-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 1199px) {
    .catalog-intro > .app-container {
      width: min(100% - 48px, var(--hero-max-width));
      max-width: var(--hero-max-width);
      min-height: 470px;
      grid-template-columns: minmax(0, 60fr) minmax(0, 40fr);
      padding-left: 0;
    }

    .catalog-intro__content {
      padding: var(--space-2xl) var(--space-xl) var(--space-2xl) 0;
    }
  }

  @media (max-width: 899px) {
    .catalog-intro > .app-container {
      min-height: auto;
      grid-template-columns: 1fr;
    }

    .catalog-intro__content {
      padding: var(--space-2xl) 0 var(--space-xl);
    }

    .catalog-intro__visual {
      padding-left: 0;
    }

    .catalog-intro__visual :deep(.v-img) {
      height: auto;
    }
  }

  @media (max-width: 767px) {
    .catalog-content {
      padding-block: var(--space-xl) var(--space-3xl);
    }

    .catalog-intro > .app-container {
      grid-template-columns: 1fr;
    }

    .catalog-grid {
      grid-template-columns: 1fr;
    }
  }
</style>
