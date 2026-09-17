<script setup lang="ts">
  import { storeToRefs } from 'pinia';
  import { computed, onBeforeUnmount, ref, watch } from 'vue';
  import { RouterLink, useRoute, useRouter } from 'vue-router';

  import AssessmentRecommendation from '@/components/assessment/AssessmentRecommendation.vue';
  import AssessmentSummaryCard from '@/components/assessment/AssessmentSummaryCard.vue';
  import AppFooter from '@/components/layout/AppFooter.vue';
  import AppHeader from '@/components/layout/AppHeader.vue';
  import { useAssessmentStore } from '@/stores/assessment';
  import {
    getEnvironmentalConcernPresentation,
    getEstablishmentPresentation,
    getLocalOccurrencePresentation,
    getRecommendationPresentation,
  } from '@/utils/assessmentPresentation';

  const route = useRoute();
  const router = useRouter();
  const searchRoute = { name: 'home', hash: '#plant-search-input' } as const;
  const assessmentStore = useAssessmentStore();
  const { assessment, error, isLoading } = storeToRefs(assessmentStore);
  const routeError = ref<string | null>(null);
  const pageError = computed(() => routeError.value ?? error.value);
  const assessmentData = assessment;
  const establishmentPresentation = computed(() =>
    assessmentData.value
      ? getEstablishmentPresentation(assessmentData.value.victorianEstablishment)
      : null,
  );
  const localOccurrencePresentation = computed(() =>
    assessmentData.value
      ? getLocalOccurrencePresentation(assessmentData.value.localOccurrence)
      : null,
  );
  const environmentalConcernPresentation = computed(() =>
    assessmentData.value
      ? getEnvironmentalConcernPresentation(assessmentData.value.environmentalConcern)
      : null,
  );
  const recommendationPresentation = computed(() =>
    assessmentData.value
      ? getRecommendationPresentation(assessmentData.value.recommendation)
      : null,
  );
  const assessmentNotices = computed(() => {
    if (!assessmentData.value) return [];

    const notices: string[] = [];
    if (assessmentData.value.localOccurrence.status === 'UNAVAILABLE') {
      notices.push('Local occurrence data is currently unavailable.');
    }
    if (assessmentData.value.environmentalConcern.status === 'UNAVAILABLE') {
      notices.push(
        environmentalConcernPresentation.value?.explanation ??
          'Environmental concern information is currently unavailable.',
      );
    }
    return notices;
  });

  function resolvePlantId(value: string | string[] | undefined): number | null {
    if (typeof value !== 'string' || value.trim() === '') return null;
    const plantId = Number(value);
    return Number.isSafeInteger(plantId) && plantId > 0 ? plantId : null;
  }

  async function loadAssessment(value: string | string[] | undefined) {
    routeError.value = null;
    const plantId = resolvePlantId(value);

    if (plantId === null) {
      assessmentStore.clearAssessment();
      routeError.value = 'We couldn’t load this plant assessment. Please try again.';
      return;
    }

    await assessmentStore.fetchAssessment(plantId);
  }

  function retryAssessment() {
    void loadAssessment(route.params.plantId);
  }

  function goToSearch() {
    void router.push(searchRoute);
  }

  function findBetterPlant() {
    const plantId = route.params.plantId;
    void router.push({ name: 'plant-alternatives', params: { plantId } });
  }

  watch(
    () => route.params.plantId,
    (plantId) => void loadAssessment(plantId),
    { immediate: true },
  );

  onBeforeUnmount(() => assessmentStore.clearAssessment());
</script>

<template>
  <div id="top" class="assessment-page">
    <AppHeader action-label="Check another plant" @check-plant="goToSearch" />

    <main class="assessment-main">
      <div class="app-container">
        <RouterLink class="assessment-back-link" :to="searchRoute">
          <v-icon icon="mdi-arrow-left" size="18" aria-hidden="true" />
          Back to plant search
        </RouterLink>

        <section v-if="isLoading" class="assessment-page-state" role="status" aria-live="polite">
          <v-progress-circular
            indeterminate
            :size="32"
            :width="3"
            color="accent"
            class="assessment-page-state__icon"
            aria-hidden="true"
          />
          <h1>Loading plant assessment…</h1>
          <p>Gathering the available planting evidence.</p>
        </section>

        <section v-else-if="pageError" class="assessment-page-state" role="alert">
          <v-icon
            icon="mdi-alert-circle-outline"
            size="32"
            class="assessment-page-state__icon"
            aria-hidden="true"
          />
          <h1>Assessment unavailable</h1>
          <p>{{ pageError }}</p>
          <div class="assessment-page-state__actions">
            <v-btn
              type="button"
              class="assessment-action--primary"
              color="primary"
              variant="flat"
              @click="retryAssessment"
            >
              Retry
            </v-btn>
            <RouterLink class="assessment-action assessment-action--secondary" :to="searchRoute">
              Return to plant search
            </RouterLink>
          </div>
        </section>

        <template v-else-if="assessmentData">
          <section class="assessment-identity" aria-labelledby="plant-identity-heading">
            <div class="assessment-identity__content">
              <p class="assessment-eyebrow">CITY OF MONASH · PLANT ASSESSMENT</p>
              <h1 id="plant-identity-heading" v-if="assessmentData.plant.commonName">
                {{ assessmentData.plant.commonName }}
              </h1>
              <h1 v-else>
                <em>{{ assessmentData.plant.scientificName }}</em>
              </h1>
              <p v-if="assessmentData.plant.commonName" class="assessment-identity__scientific">
                <em>{{ assessmentData.plant.scientificName }}</em>
              </p>
              <p v-if="assessmentData.plant.family" class="assessment-identity__family">
                Family: {{ assessmentData.plant.family }}
              </p>
            </div>
            <aside
              v-if="assessmentData.plant.imageUrl"
              class="assessment-identity__visual"
              aria-label="Plant visual"
            >
              <v-img
                :src="assessmentData.plant.imageUrl"
                alt=""
                cover
                class="assessment-identity__image"
              />
            </aside>
          </section>

          <AssessmentRecommendation
            :recommendation="assessmentData.recommendation"
            :warnings="assessmentNotices"
            @find-better-plant="findBetterPlant"
          />

          <section
            class="assessment-section assessment-section--overview"
            aria-labelledby="overview-heading"
          >
            <div class="assessment-section__intro">
              <p class="assessment-eyebrow">ASSESSMENT SUMMARY</p>
              <h2 id="overview-heading">At a glance</h2>
            </div>

            <div class="assessment-overview-grid">
              <AssessmentSummaryCard
                icon="mdi-leaf"
                title="Victorian Status"
                :value="establishmentPresentation?.label ?? 'Not provided'"
                :supporting="establishmentPresentation?.supporting"
                source="VicFlora"
                :tone="establishmentPresentation?.tone"
              />
              <AssessmentSummaryCard
                :icon="localOccurrencePresentation?.icon ?? 'mdi-map-marker-outline'"
                title="Seen Locally"
                :value="localOccurrencePresentation?.label ?? 'Not provided'"
                :supporting="localOccurrencePresentation?.supporting"
                source="Victorian Biodiversity Atlas"
                :tone="localOccurrencePresentation?.tone"
              />
              <AssessmentSummaryCard
                :icon="environmentalConcernPresentation?.icon ?? 'mdi-sprout-outline'"
                title="Environmental Concern"
                :value="environmentalConcernPresentation?.label ?? 'Not provided'"
                :supporting="environmentalConcernPresentation?.supporting"
                source="2022 Advisory List"
                :tone="environmentalConcernPresentation?.tone"
              />
            </div>
          </section>

          <section class="assessment-meaning" aria-labelledby="meaning-heading">
            <v-icon icon="mdi-lightbulb-outline" size="40" aria-hidden="true" />
            <div>
              <h2 id="meaning-heading">What this means</h2>
              <p>{{ recommendationPresentation?.guidance }}</p>
            </div>
          </section>
        </template>
      </div>
    </main>

    <AppFooter />
  </div>
</template>

<style scoped>
  .assessment-page {
    display: flex;
    min-height: 100vh;
    flex-direction: column;
    background: var(--color-canvas);
  }

  .assessment-main {
    flex: 1;
    padding-block: var(--space-xl) var(--space-4xl);
  }

  .assessment-back-link {
    display: inline-flex;
    min-height: 44px;
    align-items: center;
    gap: var(--space-xs);
    color: var(--color-primary);
    font-weight: 700;
    text-decoration: none;
  }

  .assessment-back-link:hover {
    text-decoration: underline;
    text-underline-offset: 4px;
  }

  .assessment-page-state {
    display: grid;
    min-height: 26rem;
    place-items: center;
    align-content: center;
    gap: var(--space-sm);
    padding: var(--space-2xl) var(--space-lg);
    text-align: center;
  }

  .assessment-page-state__icon {
    color: var(--color-accent);
  }

  .assessment-page-state h1,
  .assessment-page-state p {
    margin: 0;
  }

  .assessment-page-state h1,
  .assessment-section h2 {
    font-size: clamp(2rem, 4vw, 2.75rem);
  }

  .assessment-page-state p {
    max-width: 34rem;
    color: var(--color-ink-soft);
  }

  .assessment-page-state__actions {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: var(--space-sm);
    margin-top: var(--space-md);
  }

  .assessment-action {
    display: inline-flex;
    min-height: 44px;
    align-items: center;
    justify-content: center;
    padding: 0 var(--space-lg);
    border: 1px solid var(--color-primary);
    border-radius: var(--radius-md);
    font: inherit;
    font-weight: 700;
    text-decoration: none;
    cursor: pointer;
  }

  .assessment-action--secondary {
    background: transparent;
    color: var(--color-primary);
  }

  .assessment-action--secondary:hover {
    background: var(--color-surface-muted);
  }

  .assessment-eyebrow {
    margin: 0;
    color: var(--color-accent);
    font-size: 0.75rem;
    font-weight: 800;
    letter-spacing: 0.14em;
  }

  .assessment-identity {
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(18rem, 42%);
    align-items: center;
    gap: var(--space-xl);
    padding-block: 40px var(--space-2xl);
  }

  .assessment-identity__content {
    min-width: 0;
  }

  .assessment-identity__visual {
    position: relative;
    min-width: 0;
    aspect-ratio: 1.5;
    overflow: hidden;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    background: var(--color-surface-muted);
  }

  .assessment-identity__image {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
  }

  .assessment-identity h1 {
    max-width: 14ch;
    margin: var(--space-xs) 0 0;
    overflow-wrap: anywhere;
    font-size: clamp(2.75rem, 6vw, 4.5rem);
    line-height: 0.98;
  }

  .assessment-identity__scientific {
    margin: var(--space-xs) 0 0;
    color: var(--color-ink-soft);
    font-family: var(--font-display);
    font-size: 1.5rem;
    overflow-wrap: anywhere;
  }

  .assessment-identity__family {
    margin: var(--space-xs) 0 0;
    color: var(--color-muted);
  }

  .assessment-section--overview {
    padding-top: var(--space-2xl);
  }

  .assessment-section__intro {
    max-width: 40rem;
    margin-bottom: var(--space-xl);
  }

  .assessment-section h2 {
    margin: var(--space-xs) 0 0;
  }

  .assessment-overview-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 0;
    border-block: 1px solid var(--color-border);
  }

  .assessment-overview-grid > :not(:last-child) {
    border-right: 1px solid var(--color-border);
  }

  .assessment-meaning {
    display: flex;
    align-items: flex-start;
    gap: var(--space-lg);
    margin-top: var(--space-2xl);
    padding: var(--space-lg);
    border-radius: var(--radius-md);
    background: var(--color-surface-warm);
    color: var(--color-accent);
  }

  .assessment-meaning > div {
    min-width: 0;
  }

  .assessment-meaning h2 {
    margin: 0;
    font-size: 1.75rem;
    line-height: 1.1;
  }

  .assessment-meaning p {
    max-width: 100%;
    margin: var(--space-xs) 0 0;
    color: var(--color-ink-soft);
    overflow-wrap: anywhere;
  }

  @media (max-width: 999px) {
    .assessment-overview-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 767px) {
    .assessment-main {
      padding-block: 20px var(--space-2xl);
    }

    .assessment-page-state {
      min-height: 22rem;
      padding-inline: 0;
    }

    .assessment-identity {
      grid-template-columns: 1fr;
      gap: var(--space-lg);
      padding-block: var(--space-xl) 40px;
    }

    .assessment-identity__visual {
      aspect-ratio: 16 / 9;
    }

    .assessment-identity h1 {
      font-size: clamp(2.5rem, 13vw, 3.5rem);
    }

    .assessment-section--overview {
      padding-top: var(--space-2xl);
    }

    .assessment-overview-grid {
      grid-template-columns: minmax(0, 1fr);
    }

    .assessment-overview-grid > :not(:last-child) {
      border-right: 0;
      border-bottom: 1px solid var(--color-border);
    }

    .assessment-page-state__actions {
      width: 100%;
      flex-direction: column;
    }

    .assessment-action,
    .assessment-action--primary {
      width: 100%;
    }
  }
</style>
