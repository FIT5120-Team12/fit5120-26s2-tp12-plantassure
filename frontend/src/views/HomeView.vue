<script setup lang="ts">
  import { nextTick, onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';

  import ctaBotanicalDecorationUrl from '@/assets/images/cta-botanical-decoration.png';
  import heroImageUrl from '@/assets/images/home-hero-botanical.png';
  import FeatureCard from '@/components/home/FeatureCard.vue';
  import AppFooter from '@/components/layout/AppFooter.vue';
  import AppHeader from '@/components/layout/AppHeader.vue';
  import PlantSearchInput from '@/components/search/PlantSearchInput.vue';

  const searchInput = ref<InstanceType<typeof PlantSearchInput> | null>(null);
  const route = useRoute();
  const router = useRouter();

  const processSteps = [
    {
      title: 'Find your plant',
      description: 'Search by name, browse assessed plants, or identify a plant from a photo.',
    },
    {
      title: 'Review the assessment',
      description: 'See verified assessment information and documented environmental concern.',
    },
    {
      title: 'Compare alternatives when needed',
      description:
        'Explore lower-concern alternatives and compare options before deciding what to plant.',
    },
  ];

  function focusSearch() {
    searchInput.value?.focusInput();
  }

  function browsePlants() {
    void router.push({ name: 'plant-catalog' });
  }

  function identifyFromPhoto() {
    void router.push({ name: 'plant-identification' });
  }

  onMounted(async () => {
    if (route.hash !== '#plant-search-input') return;
    await nextTick();
    focusSearch();
  });
</script>

<template>
  <div id="top" class="home-page">
    <AppHeader @check-plant="focusSearch" />

    <main>
      <section class="hero" aria-labelledby="home-title">
        <div class="app-container hero__inner">
          <div class="hero__content">
            <div class="hero__content-inner">
              <p class="eyebrow">CITY OF MONASH · PLANT ASSESSMENT TOOL</p>
              <h1 id="home-title">Check before<br />you plant</h1>
              <p class="hero__description">
                Search, browse assessed plants, or identify a plant from a photo to check its
                verified assessment before planting.
              </p>
              <PlantSearchInput ref="searchInput" />
              <div class="hero__secondary-actions">
                <v-btn
                  class="plant-btn--secondary"
                  color="primary"
                  variant="outlined"
                  height="52"
                  block
                  @click="browsePlants"
                >
                  Browse assessed plants
                </v-btn>
                <v-btn
                  class="plant-btn--secondary"
                  color="primary"
                  variant="outlined"
                  height="52"
                  block
                  prepend-icon="mdi-camera-outline"
                  @click="identifyFromPhoto"
                >
                  Identify from Photo
                </v-btn>
              </div>
              <p class="hero__helper">
                Upload a photo to identify the plant and check its assessment information.
              </p>
            </div>
          </div>
          <div class="hero__visual">
            <v-img
              :src="heroImageUrl"
              alt="Flowering native foliage representing thoughtful garden planting"
              contain
              position="right bottom"
            />
          </div>
        </div>
      </section>

      <section class="value-section" aria-label="What PlantAssure helps you understand">
        <div class="app-container value-grid">
          <FeatureCard icon="mdi-leaf" title="Understand the plant">
            See its identity, status in Victoria, and key assessment information.
          </FeatureCard>
          <FeatureCard
            icon="mdi-clipboard-text-outline"
            title="Understand environmental concern"
            tone="warm"
          >
            See whether the plant has documented environmental weed concern and what that means for
            planting.
          </FeatureCard>
          <FeatureCard icon="mdi-sprout-outline" title="Make an informed choice">
            Use verified assessment information and lower-concern alternatives to support your
            planting decision.
          </FeatureCard>
        </div>
      </section>

      <section id="how-it-works" class="process-section" aria-labelledby="process-title">
        <div class="app-container process-section__inner">
          <div class="section-intro">
            <h2 id="process-title">How it works</h2>
          </div>
          <ol class="process-grid">
            <li v-for="(step, index) in processSteps" :key="step.title" class="process-item">
              <article class="process-step">
                <span class="process-step__number" aria-hidden="true">{{ index + 1 }}</span>
                <h3>{{ step.title }}</h3>
                <p>{{ step.description }}</p>
              </article>
            </li>
          </ol>
        </div>
        <img
          class="process-section__decoration process-section__decoration--left"
          :src="ctaBotanicalDecorationUrl"
          alt=""
          aria-hidden="true"
        />
        <img
          class="process-section__decoration process-section__decoration--right"
          :src="ctaBotanicalDecorationUrl"
          alt=""
          aria-hidden="true"
        />
      </section>
    </main>
    <AppFooter />
  </div>
</template>

<style scoped>
  .eyebrow {
    margin: 0 0 var(--space-sm);
    color: var(--color-accent);
    font-size: 0.75rem;
    font-weight: 700;
    letter-spacing: 0.1em;
  }
  .hero {
    position: relative;
  }
  .hero::before,
  .hero::after {
    content: '';
    position: absolute;
    right: 0;
    left: 0;
    z-index: 3;
    height: 1px;
    background: color-mix(in srgb, var(--color-border) 60%, transparent);
    pointer-events: none;
  }
  .hero::before {
    top: -1px;
  }
  .hero::after {
    bottom: 0;
  }
  .hero__inner {
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
  .hero__content {
    width: 100%;
    display: flex;
    align-items: center;
    padding: var(--space-3xl) var(--space-2xl) var(--space-3xl) 0;
  }
  .hero__content-inner {
    width: 100%;
    max-width: 560px;
  }
  .hero h1 {
    max-width: 9ch;
    margin: 0;
    font-size: 3.25rem;
    line-height: 1.05;
    letter-spacing: -0.025em;
  }
  .hero__description {
    max-width: 520px;
    margin: var(--space-md) 0 var(--space-lg);
    color: var(--color-ink-soft);
    font-size: 1.0625rem;
    line-height: 1.6;
  }
  .hero__secondary-actions {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: var(--space-md);
    margin-top: var(--space-md);
  }
  .hero__helper {
    display: flex;
    align-items: center;
    max-width: 460px;
    margin: var(--space-sm) 0 0;
    color: var(--color-muted);
    font-size: 0.8125rem;
    line-height: 1.55;
  }
  .hero__visual {
    align-self: stretch;
    min-width: 0;
  }
  .hero__visual :deep(.v-img) {
    height: 100%;
  }
  .value-section {
    padding-block: clamp(var(--space-2xl), 5vw, var(--space-3xl));
  }
  .value-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 20px;
  }
  .process-section {
    position: relative;
    scroll-margin-top: 96px;
    padding-block: clamp(var(--space-3xl), 6vw, var(--space-4xl));
    background: var(--color-surface-muted);
    overflow: hidden;
  }
  .process-section__inner {
    position: relative;
    z-index: 1;
  }
  .process-section__decoration {
    position: absolute;
    top: 0;
    bottom: 0;
    width: clamp(160px, 22vw, 300px);
    height: 100%;
    object-fit: cover;
    opacity: 0.3;
    pointer-events: none;
    user-select: none;
  }
  .process-section__decoration--left {
    left: 0;
    object-position: right center;
    transform: scaleX(-1);
  }
  .process-section__decoration--right {
    right: 0;
    object-position: left center;
  }
  .section-intro {
    margin-bottom: var(--space-lg);
  }
  .section-intro h2 {
    margin: 0;
    font-size: 2rem;
    line-height: 1.1;
    letter-spacing: -0.015em;
  }
  .process-grid {
    --process-gap: clamp(var(--space-lg), 2.5vw, var(--space-xl));

    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: var(--process-gap);
    margin: 0;
    padding: 0;
    list-style: none;
  }
  .process-item {
    min-width: 0;
  }
  .process-step {
    max-width: 320px;
  }
  .process-step__number {
    width: 44px;
    height: 44px;
    display: grid;
    place-items: center;
    margin-bottom: var(--space-sm);
    background: var(--color-success-soft);
    border-radius: var(--radius-pill);
    color: var(--color-primary);
    font-size: 1rem;
    font-weight: 600;
  }
  .process-item:nth-child(2) .process-step__number {
    background: var(--color-accent-soft);
    color: var(--color-accent);
  }
  .process-step h3 {
    margin: 0 0 var(--space-xs);
    font-family: var(--font-body);
    font-size: 1rem;
    font-weight: 600;
    line-height: 1.3;
  }
  .process-step p {
    margin: 0;
    color: var(--color-ink-soft);
    font-size: 0.9375rem;
    line-height: 1.55;
  }
  @media (max-width: 1199px) {
    .hero__inner {
      width: min(100% - 48px, var(--hero-max-width));
      max-width: var(--hero-max-width);
      min-height: 470px;
      grid-template-columns: minmax(0, 60fr) minmax(0, 40fr);
      padding-left: 0;
    }
    .hero__content {
      padding: var(--space-2xl) var(--space-xl) var(--space-2xl) 0;
    }
    .process-section__decoration {
      width: 18vw;
      opacity: 0.06;
    }
    .value-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
    .value-grid > :last-child {
      width: calc((100% - 20px) / 2);
      grid-column: 1 / -1;
      justify-self: center;
    }
  }
  @media (max-width: 899px) {
    .hero__inner {
      min-height: auto;
      grid-template-columns: 1fr;
    }
    .hero__visual :deep(.v-img) {
      height: auto;
    }
    .hero__content {
      width: 100%;
      padding: var(--space-2xl) 0 var(--space-xl);
    }
    .process-grid {
      grid-template-columns: 1fr;
      gap: var(--space-xl);
    }
    .process-step {
      max-width: 520px;
    }
  }
  @media (max-width: 767px) {
    .process-section {
      scroll-margin-top: 88px;
    }
    .process-section__decoration {
      display: none;
    }
    .hero__inner {
      width: calc(100% - 32px);
    }
    .hero h1 {
      font-size: 2.75rem;
    }
    .hero__description {
      font-size: 1rem;
    }
    .hero__secondary-actions {
      grid-template-columns: 1fr;
    }
    .value-section,
    .process-section {
      padding-block: var(--space-2xl);
    }
    .value-grid,
    .process-grid {
      grid-template-columns: 1fr;
      gap: var(--space-md);
    }
    .value-grid > :last-child {
      width: auto;
      grid-column: auto;
    }
    .section-intro h2 {
      font-size: 2.25rem;
    }
  }
</style>
