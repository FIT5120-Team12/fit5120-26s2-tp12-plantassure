<template>
  <aside class="identification-notice" aria-labelledby="identification-notice-title">
    <v-icon icon="mdi-information-outline" color="primary" size="28" aria-hidden="true" />
    <div>
      <h2 id="identification-notice-title">Identification and assessment are separate</h2>
      <p>
        AI identification suggests the plant identity. Its confidence describes how strongly the
        uploaded image matches a candidate species; it does not indicate environmental concern,
        risk, or planting suitability. Verified PlantAssure assessment information is available
        after a mapped species is confirmed.
      </p>
    </div>
  </aside>
</template>

<style scoped>
  .identification-notice {
    display: grid;
    grid-template-columns: auto minmax(0, 1fr);
    gap: var(--space-md);
    margin-top: var(--space-3xl);
    padding: var(--space-lg);
    border-radius: var(--radius-md);
    background: var(--color-surface-muted);
  }

  .identification-notice h2 {
    margin: 0;
    color: var(--color-primary);
  }

  .identification-notice p {
    margin: var(--space-sm) 0 0;
    color: var(--color-ink-soft);
  }

  @media (max-width: 479px) {
    .identification-notice {
      grid-template-columns: 1fr;
      gap: var(--space-sm);
    }
  }
</style>
