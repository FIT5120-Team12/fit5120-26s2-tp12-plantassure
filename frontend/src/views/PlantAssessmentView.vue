<script setup lang="ts">
  import { storeToRefs } from 'pinia';
  import { computed, onBeforeUnmount, ref, watch } from 'vue';
  import { RouterLink, useRoute, useRouter } from 'vue-router';

  import AssessmentEvidenceCard from '@/components/assessment/AssessmentEvidenceCard.vue';
  import AssessmentRecommendation from '@/components/assessment/AssessmentRecommendation.vue';
  import AssessmentSummaryCard from '@/components/assessment/AssessmentSummaryCard.vue';
  import AppFooter from '@/components/layout/AppFooter.vue';
  import AppHeader from '@/components/layout/AppHeader.vue';
  import CtaBanner from '@/components/layout/CtaBanner.vue';
  import { useAssessmentStore } from '@/stores/assessment';
  import {
    getEnvironmentalRiskPresentation,
    getEstablishmentPresentation,
    getLocalOccurrencePresentation,
  } from '@/utils/assessmentPresentation';

  const route = useRoute();
  const router = useRouter();
  const searchRoute = { name: 'home', hash: '#plant-search-input' } as const;
  const assessmentStore = useAssessmentStore();
  const { assessment, error, isLoading } = storeToRefs(assessmentStore);
  const routeError = ref<string | null>(null);
  const isAboutOpen = ref(false);
  const pageError = computed(() => routeError.value ?? error.value);
  const establishmentPresentation = computed(() =>
    assessment.value ? getEstablishmentPresentation(assessment.value.plant) : null,
  );
  const localOccurrencePresentation = computed(() =>
    assessment.value ? getLocalOccurrencePresentation(assessment.value.localOccurrence) : null,
  );
  const environmentalRiskPresentation = computed(() =>
    assessment.value ? getEnvironmentalRiskPresentation(assessment.value.environmentalRisk) : null,
  );
  const assessmentNotices = computed(() => {
    if (!assessment.value) return [];
    if (assessment.value.warnings.length) return assessment.value.warnings;

    const notices: string[] = [];
    if (assessment.value.localOccurrence.status === 'UNAVAILABLE') {
      notices.push('Local occurrence data is currently unavailable.');
    }
    if (assessment.value.environmentalRisk.assessmentStatus === 'UNAVAILABLE') {
      notices.push(
        environmentalRiskPresentation.value?.explanation ??
          'Environmental weed risk information is currently unavailable.',
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
    isAboutOpen.value = false;
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

  watch(
    () => route.params.plantId,
    (plantId) => void loadAssessment(plantId),
    { immediate: true },
  );

  onBeforeUnmount(() => assessmentStore.clearAssessment());
</script>

<template>
  <div id="top" class="assessment-page">
    <AppHeader home-href-prefix="/" action-label="Check another plant" @check-plant="goToSearch" />

    <main class="assessment-main">
      <div class="app-container">
        <RouterLink class="assessment-back-link" :to="searchRoute">
          <span class="mdi mdi-arrow-left" aria-hidden="true" />
          Back to plant search
        </RouterLink>

        <section v-if="isLoading" class="assessment-page-state" role="status" aria-live="polite">
          <span class="mdi mdi-loading mdi-spin assessment-page-state__icon" aria-hidden="true" />
          <h1>Loading plant assessment…</h1>
          <p>Gathering the available planting evidence.</p>
        </section>

        <section v-else-if="pageError" class="assessment-page-state" role="alert">
          <span
            class="mdi mdi-alert-circle-outline assessment-page-state__icon"
            aria-hidden="true"
          />
          <h1>Assessment unavailable</h1>
          <p>{{ pageError }}</p>
          <div class="assessment-page-state__actions">
            <button
              type="button"
              class="assessment-action assessment-action--primary"
              @click="retryAssessment"
            >
              Retry
            </button>
            <RouterLink class="assessment-action assessment-action--secondary" :to="searchRoute">
              Return to plant search
            </RouterLink>
          </div>
        </section>

        <template v-else-if="assessment">
          <header class="assessment-identity">
            <p class="assessment-eyebrow">CITY OF MONASH · PLANT ASSESSMENT</p>
            <h1 v-if="assessment.plant.commonName">{{ assessment.plant.commonName }}</h1>
            <h1 v-else>
              <em>{{ assessment.plant.scientificName }}</em>
            </h1>
            <p v-if="assessment.plant.commonName" class="assessment-identity__scientific">
              <em>{{ assessment.plant.scientificName }}</em>
            </p>
            <p v-if="assessment.plant.family" class="assessment-identity__family">
              Family: {{ assessment.plant.family }}
            </p>
          </header>

          <AssessmentRecommendation
            :recommendation="assessment.recommendation"
            :warnings="assessmentNotices"
          />

          <section
            class="assessment-section assessment-section--overview"
            aria-labelledby="overview-heading"
          >
            <div class="assessment-section__intro">
              <p class="assessment-eyebrow">EVIDENCE AT A GLANCE</p>
              <h2 id="overview-heading">Evidence overview</h2>
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
                title="Local Occurrence"
                :value="localOccurrencePresentation?.label ?? 'Not provided'"
                :supporting="localOccurrencePresentation?.supporting"
                :source="assessment.localOccurrence.source"
                :tone="localOccurrencePresentation?.tone"
              />
              <AssessmentSummaryCard
                :icon="environmentalRiskPresentation?.icon ?? 'mdi-sprout-outline'"
                title="Environmental Weed Risk"
                :value="environmentalRiskPresentation?.label ?? 'Not provided'"
                :supporting="environmentalRiskPresentation?.supporting"
                :source="assessment.environmentalRisk.source"
                :tone="environmentalRiskPresentation?.tone"
              />
            </div>
          </section>

          <section class="assessment-section" aria-labelledby="details-heading">
            <div class="assessment-section__intro">
              <p class="assessment-eyebrow">SUPPORTING EVIDENCE</p>
              <h2 id="details-heading">Evidence details</h2>
            </div>

            <div class="assessment-details-grid">
              <AssessmentEvidenceCard title="Plant Identity" source="VicFlora">
                <dl class="assessment-fields">
                  <div v-if="assessment.plant.commonName">
                    <dt>Common name</dt>
                    <dd>{{ assessment.plant.commonName }}</dd>
                  </div>
                  <div>
                    <dt>Scientific name</dt>
                    <dd>
                      <em>{{ assessment.plant.scientificName }}</em>
                    </dd>
                  </div>
                  <div v-if="assessment.plant.family">
                    <dt>Family</dt>
                    <dd>{{ assessment.plant.family }}</dd>
                  </div>
                </dl>
              </AssessmentEvidenceCard>

              <AssessmentEvidenceCard
                title="Establishment in Victoria"
                source="VicFlora"
                :tone="establishmentPresentation?.tone"
              >
                <div v-if="establishmentPresentation?.badges.length" class="assessment-badges">
                  <span
                    v-for="badge in establishmentPresentation.badges"
                    :key="badge"
                    :class="`assessment-badge--${establishmentPresentation.tone}`"
                  >
                    {{ badge }}
                  </span>
                </div>
                <p class="assessment-evidence-copy">
                  {{
                    establishmentPresentation?.explanation ?? 'Establishment data was not provided.'
                  }}
                </p>
              </AssessmentEvidenceCard>

              <AssessmentEvidenceCard
                title="City of Monash occurrence evidence"
                :source="assessment.localOccurrence.source"
                :tone="localOccurrencePresentation?.tone"
              >
                <template v-if="assessment.localOccurrence.status === 'FOUND'">
                  <p class="assessment-record-count">
                    {{ localOccurrencePresentation?.label }}
                  </p>
                  <p class="assessment-record-meta">
                    {{ localOccurrencePresentation?.supporting }}
                  </p>
                </template>
                <p
                  v-else-if="assessment.localOccurrence.status === 'NOT_FOUND'"
                  class="assessment-evidence-lead"
                >
                  {{ localOccurrencePresentation?.label }}
                </p>
                <p v-else class="assessment-evidence-lead">
                  {{ localOccurrencePresentation?.label }}
                </p>
                <p class="assessment-evidence-copy">
                  {{ localOccurrencePresentation?.explanation }}
                </p>
                <p class="assessment-evidence-note">
                  Local occurrence provides evidence that the species has been recorded locally, but
                  it does not measure environmental-risk severity.
                </p>
              </AssessmentEvidenceCard>

              <AssessmentEvidenceCard
                title="Environmental weed-risk evidence"
                :source="assessment.environmentalRisk.source"
                :tone="environmentalRiskPresentation?.tone"
              >
                <div class="assessment-badges">
                  <span :class="`assessment-badge--${environmentalRiskPresentation?.tone}`">
                    {{ environmentalRiskPresentation?.label }}
                  </span>
                </div>
                <p class="assessment-evidence-copy">
                  {{ environmentalRiskPresentation?.explanation }}
                </p>
              </AssessmentEvidenceCard>
            </div>
          </section>

          <section class="assessment-about" aria-labelledby="assessment-about-trigger">
            <button
              id="assessment-about-trigger"
              class="assessment-about__trigger"
              type="button"
              :aria-expanded="isAboutOpen"
              aria-controls="assessment-about-content"
              @click="isAboutOpen = !isAboutOpen"
            >
              <span>About this assessment</span>
              <span
                class="mdi mdi-chevron-down"
                :class="{ 'assessment-about__chevron--open': isAboutOpen }"
                aria-hidden="true"
              />
            </button>
            <div
              v-show="isAboutOpen"
              id="assessment-about-content"
              class="assessment-about__content"
              role="region"
              aria-labelledby="assessment-about-trigger"
            >
              <p>PlantAssure brings together the evidence sources supplied with this assessment.</p>
              <ul v-if="assessment.sources.length" class="assessment-source-list">
                <li v-for="source in assessment.sources" :key="`${source.name}-${source.role}`">
                  <strong>{{ source.name }}</strong>
                  <span>{{ source.role }}</span>
                </li>
              </ul>
              <p class="assessment-about__note">
                Victorian Biodiversity Atlas records provide local occurrence evidence; record
                counts do not measure environmental-risk severity. Environmental weed-risk evidence
                and planting guidance remain separate from local occurrence.
              </p>
            </div>
          </section>

          <div class="assessment-cta">
            <CtaBanner
              compact
              eyebrow="CHECK ANOTHER PLANT"
              title="Want to check another plant?"
              description="Search another common or scientific plant name before making your planting decision."
              action-label="Check another plant"
              @action="goToSearch"
            />
          </div>
        </template>
      </div>
    </main>

    <AppFooter home-href-prefix="/" />
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
    font-size: 2rem;
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

  .assessment-action--primary {
    background: var(--color-primary);
    color: var(--color-on-primary);
  }

  .assessment-action--primary:hover {
    background: var(--color-primary-hover);
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
    max-width: 52rem;
    padding-block: 40px var(--space-2xl);
  }

  .assessment-identity h1 {
    max-width: 14ch;
    margin: var(--space-sm) 0 0;
    overflow-wrap: anywhere;
    font-size: clamp(2.75rem, 6vw, 4.5rem);
    line-height: 0.98;
  }

  .assessment-identity__scientific {
    margin: var(--space-md) 0 0;
    color: var(--color-ink-soft);
    font-family: var(--font-display);
    font-size: 1.5rem;
    overflow-wrap: anywhere;
  }

  .assessment-identity__family {
    margin: var(--space-xs) 0 0;
    color: var(--color-muted);
  }

  .assessment-section {
    padding-top: var(--space-4xl);
  }

  .assessment-section--overview {
    padding-top: var(--space-lg);
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
    gap: 20px;
  }

  .assessment-details-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 20px;
  }

  .assessment-fields {
    display: grid;
    gap: var(--space-md);
    margin: 0;
  }

  .assessment-fields div {
    display: grid;
    grid-template-columns: minmax(8rem, 0.8fr) minmax(0, 1fr);
    gap: var(--space-md);
  }

  .assessment-fields dt {
    color: var(--color-muted);
  }

  .assessment-fields dd {
    min-width: 0;
    margin: 0;
    overflow-wrap: anywhere;
    color: var(--color-primary);
    font-weight: 700;
  }

  .assessment-evidence-copy {
    max-width: 48rem;
    margin: var(--space-md) 0 0;
    color: var(--color-ink-soft);
  }

  .assessment-badges {
    display: flex;
    flex-wrap: wrap;
    gap: var(--space-xs);
  }

  .assessment-badges span {
    max-width: 100%;
    display: inline-flex;
    min-height: 28px;
    align-items: center;
    padding: 0 var(--space-sm);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-pill);
    background: var(--color-surface-muted);
    color: var(--color-primary);
    font-size: 0.8125rem;
    font-weight: 700;
    overflow-wrap: anywhere;
    text-align: left;
  }

  .assessment-badges .assessment-badge--concern,
  .assessment-badges .assessment-badge--caution {
    border-color: color-mix(in srgb, var(--color-accent) 36%, var(--color-border));
    background: var(--color-accent-soft);
  }

  .assessment-badges .assessment-badge--lower {
    background: var(--color-success-soft);
  }

  .assessment-badges .assessment-badge--unavailable {
    background: var(--color-surface-muted);
    color: var(--color-muted);
  }

  .assessment-record-count {
    margin: 0;
    color: var(--color-primary);
    font-family: var(--font-display);
    font-size: 2rem;
    line-height: 1;
  }

  .assessment-record-meta {
    margin: 6px 0 0;
    color: var(--color-ink-soft);
    font-size: 0.875rem;
  }

  .assessment-evidence-lead {
    margin: 0;
    color: var(--color-primary);
    font-weight: 700;
  }

  .assessment-evidence-note {
    margin: var(--space-md) 0 0;
    padding-top: var(--space-md);
    border-top: 1px solid var(--color-border);
    color: var(--color-muted);
    font-size: 0.8125rem;
  }

  .assessment-about {
    margin-top: var(--space-2xl);
    border-block: 1px solid var(--color-border);
  }

  .assessment-about__trigger {
    display: flex;
    width: 100%;
    min-height: 64px;
    align-items: center;
    justify-content: space-between;
    gap: var(--space-md);
    padding: 0;
    border: 0;
    background: transparent;
    color: var(--color-ink-soft);
    font-family: var(--font-body);
    font-size: 0.875rem;
    font-weight: 700;
    text-align: left;
    cursor: pointer;
  }

  .assessment-about__trigger:hover {
    color: var(--color-primary);
  }

  .assessment-about__trigger .mdi {
    flex: 0 0 auto;
    transition: transform 160ms ease;
  }

  .assessment-about__chevron--open {
    transform: rotate(180deg);
  }

  .assessment-about__content {
    max-width: 64rem;
    padding-bottom: var(--space-xl);
  }

  .assessment-about__content > p {
    max-width: 42rem;
    margin: 0 0 var(--space-lg);
    color: var(--color-ink-soft);
  }

  .assessment-about__content > .assessment-about__note {
    margin-top: var(--space-lg);
    padding-top: var(--space-lg);
    border-top: 1px solid var(--color-border);
    font-size: 0.875rem;
  }

  .assessment-source-list {
    display: grid;
    gap: var(--space-sm);
    margin: 0;
    padding: 0;
    list-style: none;
  }

  .assessment-source-list li {
    display: grid;
    grid-template-columns: minmax(10rem, 0.45fr) minmax(0, 1fr);
    gap: 20px;
    padding: 20px;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    background: var(--color-surface);
  }

  .assessment-source-list span {
    min-width: 0;
    overflow-wrap: anywhere;
    color: var(--color-ink-soft);
  }

  .assessment-source-list strong {
    min-width: 0;
    overflow-wrap: anywhere;
  }

  .assessment-cta {
    padding-top: 52px;
  }

  @media (max-width: 999px) {
    .assessment-overview-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }

  @media (max-width: 899px) {
    .assessment-details-grid {
      grid-template-columns: minmax(0, 1fr);
    }
  }

  @media (max-width: 767px) {
    .assessment-main {
      padding-block: 20px var(--space-3xl);
    }

    .assessment-page-state {
      min-height: 22rem;
      padding-inline: 0;
    }

    .assessment-identity {
      padding-block: var(--space-xl) 40px;
    }

    .assessment-identity h1 {
      font-size: clamp(2.5rem, 13vw, 3.5rem);
    }

    .assessment-section {
      padding-top: var(--space-3xl);
    }

    .assessment-section--overview {
      padding-top: var(--space-xl);
    }

    .assessment-about {
      margin-top: var(--space-xl);
    }

    .assessment-overview-grid,
    .assessment-details-grid {
      grid-template-columns: minmax(0, 1fr);
    }

    .assessment-fields div,
    .assessment-source-list li {
      grid-template-columns: minmax(0, 1fr);
      gap: 4px;
    }

    .assessment-page-state__actions {
      width: 100%;
      flex-direction: column;
    }

    .assessment-action {
      width: 100%;
    }
  }

  @media (prefers-reduced-motion: reduce) {
    .assessment-about__trigger .mdi {
      transition: none;
    }
  }
</style>
