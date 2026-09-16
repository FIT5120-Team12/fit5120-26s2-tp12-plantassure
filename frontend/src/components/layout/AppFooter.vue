<script setup lang="ts">
  import logoUrl from '@/assets/images/plantassure-logo.png';
</script>

<template>
  <footer class="app-footer">
    <div class="app-container app-footer__inner">
      <div class="app-footer__brand">
        <img :src="logoUrl" alt="" aria-hidden="true" />
        <p>Designed for residential gardeners in the City of Monash.</p>
      </div>
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
    min-height: 96px;
    display: grid;
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
    align-items: center;
    gap: var(--space-xl);
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

    .app-footer__note {
      justify-self: start;
      text-align: left;
    }
  }
</style>
