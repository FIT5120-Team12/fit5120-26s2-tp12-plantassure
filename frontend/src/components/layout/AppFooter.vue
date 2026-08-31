<script setup lang="ts">
  import logoUrl from '@/assets/images/plantassure-logo.png';

  withDefaults(
    defineProps<{
      homeHrefPrefix?: string;
    }>(),
    {
      homeHrefPrefix: '',
    },
  );
</script>

<template>
  <footer class="app-footer">
    <div class="app-container app-footer__inner">
      <div class="app-footer__brand">
        <img :src="logoUrl" alt="" aria-hidden="true" />
        <p>Designed for residential gardeners in the City of Monash.</p>
      </div>
      <nav class="app-footer__nav" aria-label="Footer navigation">
        <a :href="`${homeHrefPrefix}#why-it-matters`">About</a>
        <a :href="`${homeHrefPrefix}#about-data`">Data Sources</a>
      </nav>
      <p class="app-footer__note">Plant information for more informed planting decisions.</p>
    </div>
  </footer>
</template>

<style scoped>
  .app-footer {
    border-top: 1px solid var(--color-border);
    color: var(--color-ink-soft);
  }

  .app-footer__inner {
    min-height: 120px;
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto minmax(0, 1fr);
    align-items: center;
    gap: 20px;
    font-size: 0.82rem;
  }

  .app-footer__brand {
    display: flex;
    align-items: center;
    gap: var(--space-sm);
  }

  .app-footer__brand img {
    width: 48px;
    height: 48px;
    object-fit: contain;
  }

  .app-footer p {
    margin: 0;
  }

  .app-footer__nav {
    display: flex;
    gap: var(--space-lg);
  }

  .app-footer__nav a {
    min-height: 44px;
    display: inline-flex;
    align-items: center;
    border-radius: var(--radius-sm);
    color: var(--color-ink-soft);
    text-decoration: none;
  }

  .app-footer__nav a:hover {
    color: var(--color-primary);
    text-decoration: underline;
    text-underline-offset: 4px;
  }

  .app-footer__note {
    max-width: 250px;
    justify-self: end;
    text-align: right;
  }

  @media (max-width: 767px) {
    .app-footer__inner {
      padding-block: var(--space-xl);
      grid-template-columns: 1fr;
      gap: var(--space-md);
    }

    .app-footer__brand {
      align-items: flex-start;
    }

    .app-footer__nav {
      gap: var(--space-xl);
    }

    .app-footer__note {
      justify-self: start;
      text-align: left;
    }
  }
</style>
