<script setup lang="ts">
  import { RouterLink } from 'vue-router';

  import logoUrl from '@/assets/images/plantassure-logo.png';

  withDefaults(
    defineProps<{
      actionLabel?: string;
    }>(),
    {
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
      <RouterLink
        class="app-header__brand"
        :to="{ name: 'home', hash: '#top' }"
        aria-label="PlantAssure home"
      >
        <img :src="logoUrl" alt="PlantAssure" />
      </RouterLink>

      <nav class="app-header__nav" aria-label="Primary navigation">
        <v-btn
          class="app-header__action"
          color="primary"
          variant="flat"
          append-icon="mdi-arrow-right"
          @click="$emit('checkPlant')"
        >
          {{ actionLabel }}
        </v-btn>
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
  }

  @media (max-width: 1199px) {
    .app-header__action {
      padding-inline: var(--space-md);
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
  }
</style>
