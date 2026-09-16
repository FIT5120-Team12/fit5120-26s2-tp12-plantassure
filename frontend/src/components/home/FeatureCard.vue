<script setup lang="ts">
  defineProps<{
    icon: string;
    title: string;
    tone?: 'green' | 'warm';
  }>();
</script>

<template>
  <article class="feature-card">
    <v-avatar
      size="44"
      rounded="circle"
      class="feature-card__icon"
      :class="{ 'feature-card__icon--warm': tone === 'warm' }"
      aria-hidden="true"
    >
      <v-icon :icon="icon" :size="20" aria-hidden="true" />
    </v-avatar>
    <h3>{{ title }}</h3>
    <p><slot></slot></p>
  </article>
</template>

<style scoped>
  .feature-card__icon {
    margin-bottom: var(--space-sm);
    background: var(--color-success-soft);
    color: var(--color-primary);
  }

  .feature-card__icon--warm {
    background: var(--color-accent-soft);
    color: var(--color-accent);
  }

  .feature-card h3 {
    margin: 0 0 var(--space-xs);
    color: var(--color-primary);
    font-family: var(--font-body);
    font-size: 1.125rem;
    font-weight: 600;
    line-height: 1.3;
  }

  .feature-card p {
    margin: 0;
    color: var(--color-ink-soft);
    font-size: 0.9375rem;
    line-height: 1.55;
  }
</style>
