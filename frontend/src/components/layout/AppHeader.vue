<script setup lang="ts">
  import logoUrl from '@/assets/images/plantassure-logo.png';

  withDefaults(
    defineProps<{
      homeHrefPrefix?: string;
      actionLabel?: string;
    }>(),
    {
      homeHrefPrefix: '',
      actionLabel: 'Check a Plant',
    },
  );

  defineEmits<{
    checkPlant: [];
  }>();
</script>

<template>
  <header class="app-header">
    <div class="app-container app-header__inner">
      <a class="app-header__brand" :href="`${homeHrefPrefix}#top`" aria-label="PlantAssure home">
        <img :src="logoUrl" alt="PlantAssure" />
      </a>

      <nav class="app-header__nav" aria-label="Primary navigation">
        <a class="app-header__link" :href="`${homeHrefPrefix}#how-it-works`">How it works</a>
        <a
          class="app-header__link app-header__link--about"
          :href="`${homeHrefPrefix}#why-it-matters`"
        >
          About / Data Sources
        </a>
        <button class="app-header__action" type="button" @click="$emit('checkPlant')">
          <span>{{ actionLabel }}</span>
          <i class="mdi mdi-arrow-right" aria-hidden="true"></i>
        </button>
      </nav>
    </div>
  </header>
</template>

<style scoped>
  .app-header {
    position: sticky;
    top: 0;
    z-index: 10;
    min-height: 72px;
    background: var(--color-canvas);
    border-bottom: 1px solid var(--color-border);
  }

  .app-header__inner {
    min-height: 72px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: var(--space-lg);
  }

  .app-header__brand {
    display: inline-flex;
    align-items: center;
    width: 68px;
    height: 68px;
    flex: 0 0 auto;
    border-radius: var(--radius-sm);
  }

  .app-header__brand img {
    display: block;
    width: 68px;
    height: 68px;
    object-fit: contain;
  }

  .app-header__nav {
    display: flex;
    align-items: center;
    gap: 28px;
  }

  .app-header__link,
  .app-header__action {
    min-height: 44px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: var(--radius-md);
    font-size: 0.9rem;
    font-weight: 600;
    text-decoration: none;
    cursor: pointer;
  }

  .app-header__link {
    padding: 0;
    color: var(--color-ink-soft);
  }

  .app-header__link:hover {
    color: var(--color-primary);
    text-decoration: underline;
    text-underline-offset: 4px;
  }

  .app-header__action {
    gap: var(--space-xs);
    padding: 0 20px;
    border: 0;
    background: var(--color-primary);
    color: var(--color-on-primary);
    transition: background-color 160ms ease;
  }

  .app-header__action:hover {
    background: var(--color-primary-hover);
  }

  .app-header__action:active {
    transform: translateY(1px);
  }

  @media (max-width: 1199px) {
    .app-header__nav {
      gap: var(--space-md);
    }

    .app-header__action {
      padding-inline: var(--space-md);
    }
  }

  @media (max-width: 899px) {
    .app-header__link {
      display: none;
    }
  }

  @media (max-width: 767px) {
    .app-header,
    .app-header__inner {
      min-height: 64px;
    }

    .app-header__brand {
      width: 56px;
      height: 56px;
    }

    .app-header__brand img {
      width: 52px;
      height: 52px;
    }

    .app-header__action {
      padding-inline: var(--space-md);
    }
  }
</style>
