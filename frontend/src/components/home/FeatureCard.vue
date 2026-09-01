<script setup lang="ts">
  defineProps<{
    icon: string;
    title: string;
  }>();
</script>

<template>
  <article class="feature-card">
    <div class="feature-card__icon" aria-hidden="true">
      <i :class="['mdi', icon]" aria-hidden="true"></i>
    </div>
    <h3>{{ title }}</h3>
    <p><slot /></p>
  </article>
</template>

<style scoped>
  .feature-card {
    min-height: 214px;
    padding: var(--space-lg);
    background: var(--color-surface);
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
  }

  .feature-card__icon {
    width: 44px;
    height: 44px;
    display: grid;
    place-items: center;
    margin-bottom: var(--space-md);
    border: 1px solid var(--color-border-strong);
    border-radius: var(--radius-pill);
    color: var(--color-ink-soft);
    font-size: 20px;
  }

  .feature-card h3 {
    margin: 0 0 var(--space-xs);
    color: var(--color-primary);
    font-family: var(--font-body);
    font-size: 0.9375rem;
    font-weight: 600;
    line-height: 1.5;
  }

  .feature-card p {
    margin: 0;
    color: var(--color-ink-soft);
    font-size: 0.875rem;
    line-height: 1.6;
  }

  @media (max-width: 767px) {
    .feature-card {
      min-height: 0;
      padding: var(--space-lg);
    }
  }
</style>
