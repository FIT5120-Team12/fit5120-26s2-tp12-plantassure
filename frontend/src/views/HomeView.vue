<script setup lang="ts">
  import { ref } from 'vue';

  import heroImageUrl from '@/assets/images/home-hero-botanical.png';
  import whyImageUrl from '@/assets/images/why-it-matters-garden.png';
  import FeatureCard from '@/components/home/FeatureCard.vue';
  import AppFooter from '@/components/layout/AppFooter.vue';
  import AppHeader from '@/components/layout/AppHeader.vue';
  import CtaBanner from '@/components/layout/CtaBanner.vue';
  import PlantSearchInput from '@/components/search/PlantSearchInput.vue';

  const searchInput = ref<InstanceType<typeof PlantSearchInput> | null>(null);

  const processSteps = [
    {
      icon: 'mdi-magnify',
      title: 'Search for a plant',
      description: 'Enter a common or scientific name.',
    },
    {
      icon: 'mdi-sprout-outline',
      title: 'Check its Victorian status',
      description: 'See whether it is native or introduced and its degree of establishment.',
    },
    {
      icon: 'mdi-map-marker-outline',
      title: 'Review local records and weed risk',
      description:
        'Check City of Monash occurrence evidence and the Victorian environmental weed-risk assessment.',
    },
    {
      icon: 'mdi-shield-check-outline',
      title: 'Get a clear recommendation',
      description: 'Receive clear planting guidance with a plain-language explanation.',
    },
  ];

  function focusSearch() {
    searchInput.value?.focusInput();
  }
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
              <h1 id="home-title">Check before you plant</h1>
              <p class="hero__description">
                Search a plant to understand its Victorian establishment status, local occurrence in
                the City of Monash, and environmental weed risk before planting.
              </p>
              <PlantSearchInput ref="searchInput" />
              <p class="hero__audience">
                Designed for residential gardeners in the City of Monash.
              </p>
            </div>
          </div>
          <div class="hero__visual">
            <img
              :src="heroImageUrl"
              alt="Flowering native foliage representing thoughtful garden planting"
            />
          </div>
        </div>
      </section>

      <section class="value-section" aria-label="What PlantAssure helps you understand">
        <div class="app-container value-grid">
          <FeatureCard icon="mdi-book-open-page-variant-outline" title="Understand its status">
            See the plant's scientific name, common name, and whether it is native or introduced in
            Victoria.
          </FeatureCard>
          <FeatureCard icon="mdi-map-marker-outline" title="Check local evidence">
            See whether the species has been recorded in the City of Monash using Victorian
            Biodiversity Atlas records.
          </FeatureCard>
          <FeatureCard icon="mdi-alert-outline" title="Understand environmental risk">
            View the plant's assessed environmental weed risk and receive clear planting guidance.
          </FeatureCard>
        </div>
      </section>

      <section id="how-it-works" class="process-section" aria-labelledby="process-title">
        <div class="app-container">
          <div class="section-intro">
            <p class="eyebrow">HOW IT WORKS</p>
            <h2 id="process-title">From search to informed decision</h2>
            <p>
              A simple four-step process that brings together Victorian plant data and local
              occurrence evidence.
            </p>
          </div>
          <ol class="process-grid">
            <li v-for="(step, index) in processSteps" :key="step.title" class="process-item">
              <article class="process-step">
                <div class="process-step__icon" aria-hidden="true">
                  <i :class="['mdi', step.icon]"></i>
                </div>
                <h3>{{ step.title }}</h3>
                <p>{{ step.description }}</p>
              </article>
              <i
                v-if="index < processSteps.length - 1"
                class="process-item__arrow mdi mdi-arrow-right"
                aria-hidden="true"
              ></i>
            </li>
          </ol>
        </div>
      </section>

      <section id="why-it-matters" class="why-section" aria-labelledby="why-title">
        <div class="app-container why-section__inner">
          <div class="why-section__visual">
            <img
              :src="whyImageUrl"
              alt="A residential garden filled with a variety of potted plants"
            />
          </div>
          <div class="why-section__content">
            <p class="eyebrow">WHY THIS MATTERS</p>
            <h2 id="why-title">Small planting choices can have wider impacts</h2>
            <ul class="why-list">
              <li>Introduced garden plants can sometimes establish beyond garden boundaries.</li>
              <li>
                Plant status, local occurrence, and environmental weed-risk information often comes
                from different sources.
              </li>
              <li>
                PlantAssure brings those sources together to support more informed planting
                decisions.
              </li>
            </ul>
            <aside id="about-data" class="data-note" aria-label="About the data">
              <strong>About the data:</strong> Plant identity and establishment information comes
              from VicFlora, local occurrence evidence comes from the Victorian Biodiversity Atlas,
              and environmental weed-risk information comes from the 2022 Advisory List of
              Environmental Weeds in Victoria.
            </aside>
          </div>
        </div>
      </section>

      <div class="app-container home-cta">
        <CtaBanner
          title="Thinking about planting something? Check it first."
          description="Search by common or scientific name to review Victorian establishment status, local occurrence, and environmental weed risk before planting."
          @action="focusSearch"
        />
      </div>
    </main>
    <AppFooter />
  </div>
</template>

<style scoped>
  .home-page {
    background: var(--color-canvas);
  }
  .eyebrow {
    margin: 0 0 var(--space-md);
    color: var(--color-accent);
    font-size: 0.75rem;
    font-weight: 700;
    letter-spacing: 0.1em;
  }
  .hero {
    border-bottom: 1px solid color-mix(in srgb, var(--color-border) 60%, transparent);
  }
  .hero__inner {
    width: min(100% - 96px, var(--hero-max-width));
    min-height: 600px;
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1.5fr);
    align-items: center;
    gap: 0;
  }
  .hero__content {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    padding: var(--space-3xl) var(--space-2xl) var(--space-3xl) 0;
  }
  .hero__content-inner {
    width: 100%;
    max-width: 540px;
  }
  .hero h1 {
    max-width: 9ch;
    margin: 0;
    font-size: 3.25rem;
    line-height: 1.05;
    letter-spacing: -0.025em;
  }
  .hero__description {
    max-width: 460px;
    margin: 20px 0 var(--space-xl);
    color: var(--color-ink-soft);
    font-size: 1.0625rem;
    line-height: 1.6;
  }
  .hero__audience {
    display: flex;
    align-items: center;
    gap: var(--space-xs);
    margin: var(--space-md) 0 0;
    color: var(--color-muted);
    font-size: 0.84rem;
  }
  .hero__visual {
    position: relative;
    align-self: stretch;
    min-width: 0;
    overflow: hidden;
  }
  .hero__visual img {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    display: block;
    object-fit: cover;
    object-position: center;
    -webkit-mask-image: linear-gradient(90deg, transparent 0%, #000 3%, #000 97%, transparent 100%);
    mask-image: linear-gradient(90deg, transparent 0%, #000 3%, #000 97%, transparent 100%);
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
    scroll-margin-top: 96px;
    padding-block: clamp(var(--space-3xl), 6vw, var(--space-4xl));
    background: var(--color-surface-muted);
  }
  .section-intro {
    max-width: 520px;
    margin-bottom: var(--space-2xl);
  }
  .section-intro h2,
  .why-section h2 {
    margin: 0;
    font-size: 2rem;
    line-height: 1.1;
    letter-spacing: -0.015em;
  }
  .section-intro > p:last-child {
    margin: var(--space-md) 0 0;
    color: var(--color-ink-soft);
    font-size: 1rem;
  }
  .process-grid {
    --process-gap: clamp(var(--space-lg), 2.5vw, var(--space-xl));

    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: var(--process-gap);
    margin: 0;
    padding: 0;
    list-style: none;
  }
  .process-item {
    position: relative;
    min-width: 0;
  }
  .process-step {
    min-height: 268px;
    height: 100%;
    padding: var(--space-lg) 20px;
    background: var(--color-surface);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
  }
  .process-step__icon {
    width: 44px;
    height: 44px;
    display: grid;
    place-items: center;
    margin-bottom: 20px;
    border: 1px solid var(--color-border-strong);
    border-radius: var(--radius-pill);
    color: var(--color-ink-soft);
    font-size: 18px;
  }
  .process-step h3 {
    min-height: 42px;
    margin: 0 0 10px;
    font-family: var(--font-body);
    font-size: 0.875rem;
    font-weight: 600;
  }
  .process-step > p:last-of-type {
    margin: 0;
    color: var(--color-ink-soft);
    font-size: 0.8125rem;
    line-height: 1.6;
  }
  .process-item__arrow {
    position: absolute;
    top: 50%;
    right: calc(0px - var(--process-gap));
    z-index: 1;
    width: var(--process-gap);
    display: grid;
    place-items: center;
    color: var(--color-muted);
    font-size: 20px;
    transform: translateY(-50%);
  }
  .why-section {
    scroll-margin-top: 96px;
    padding-block: clamp(var(--space-3xl), 6.5vw, var(--space-4xl));
  }
  .why-section__inner {
    display: grid;
    grid-template-columns: minmax(0, 38fr) minmax(0, 55fr);
    align-items: center;
    justify-content: center;
    gap: clamp(var(--space-xl), 5vw, var(--space-3xl));
  }
  .why-section__visual img {
    width: 100%;
    aspect-ratio: 4 / 3;
    display: block;
    object-fit: cover;
  }
  .why-section__visual {
    overflow: hidden;
    background: var(--color-success-soft);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
  }
  .why-section__content {
    max-width: 640px;
  }
  .why-list {
    display: grid;
    gap: 18px;
    margin: var(--space-lg) 0 var(--space-xl);
    padding: 0;
    list-style: none;
  }
  .why-list li {
    position: relative;
    padding-left: var(--space-xl);
    color: var(--color-ink-soft);
  }
  .why-list li::before {
    content: '';
    position: absolute;
    top: 0.38em;
    left: 4px;
    width: 13px;
    height: 13px;
    border: 3px solid var(--color-success-soft);
    border-radius: var(--radius-pill);
    box-shadow: inset 0 0 0 2px var(--color-focus);
  }
  .data-note {
    padding: var(--space-md) 20px;
    scroll-margin-top: 96px;
    background: var(--color-surface-muted);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    color: var(--color-ink-soft);
    font-size: 0.86rem;
  }
  .data-note strong {
    color: var(--color-primary);
  }
  .home-cta {
    padding-block: 0 clamp(var(--space-3xl), 6.5vw, var(--space-4xl));
  }
  @media (max-width: 1439px) {
    .hero__inner {
      width: min(100% - 64px, var(--hero-max-width));
    }
  }
  @media (max-width: 1199px) {
    .hero__inner {
      width: min(100% - 48px, var(--hero-max-width));
      height: auto;
      min-height: 470px;
      grid-template-columns: minmax(0, 60fr) minmax(0, 40fr);
    }
    .hero__content {
      padding: 56px var(--space-xl) 48px 0;
    }
    .value-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
    .value-grid > :last-child {
      width: calc((100% - 20px) / 2);
      grid-column: 1 / -1;
      justify-self: center;
    }
    .process-grid {
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: var(--space-lg);
    }
    .process-step {
      min-height: 230px;
    }
    .process-item__arrow {
      display: none;
    }
    .why-section__inner {
      grid-template-columns: minmax(0, 38fr) minmax(0, 55fr);
      gap: var(--space-2xl);
    }
    .why-section__visual {
      width: 100%;
    }
    .why-section__content {
      max-width: 640px;
    }
  }
  @media (max-width: 899px) {
    .hero__inner {
      min-height: auto;
      grid-template-columns: 1fr;
    }
    .hero__content {
      width: 100%;
      padding: var(--space-2xl) var(--space-lg) var(--space-xl);
    }
    .hero__visual {
      height: clamp(300px, 40vw, 340px);
    }
    .hero__visual img {
      -webkit-mask-image: linear-gradient(
        90deg,
        transparent 0%,
        #000 1%,
        #000 99%,
        transparent 100%
      );
      mask-image: linear-gradient(90deg, transparent 0%, #000 1%, #000 99%, transparent 100%);
    }
    .why-section__inner {
      grid-template-columns: 1fr;
      gap: var(--space-xl);
    }
    .why-section__visual {
      width: min(100%, 480px);
      margin-inline: auto;
    }
    .why-section__content {
      max-width: 640px;
      margin-inline: auto;
    }
  }
  @media (max-width: 767px) {
    .process-section,
    .why-section,
    .data-note {
      scroll-margin-top: 88px;
    }
    .hero__inner {
      width: calc(100% - 32px);
      min-height: auto;
      grid-template-columns: 1fr;
      gap: 0;
    }
    .hero__content {
      padding: var(--space-2xl) var(--space-md) var(--space-xl);
    }
    .hero h1 {
      font-size: 2.75rem;
    }
    .hero__description {
      margin-block: var(--space-md) var(--space-lg);
      font-size: 1rem;
    }
    .hero__audience {
      align-items: flex-start;
    }
    .hero__visual {
      height: 290px;
    }
    .hero__visual img {
      -webkit-mask-image: none;
      mask-image: none;
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
    .process-step {
      min-height: 0;
    }
    .section-intro {
      margin-bottom: var(--space-xl);
    }
    .section-intro h2,
    .why-section h2 {
      font-size: 2.25rem;
    }
    .why-section {
      padding-block: var(--space-2xl);
    }
    .why-section__visual img {
      max-height: 360px;
    }
    .why-list {
      gap: var(--space-md);
      margin-block: var(--space-lg);
    }
    .home-cta {
      padding-block: 0 var(--space-2xl);
    }
  }
</style>
