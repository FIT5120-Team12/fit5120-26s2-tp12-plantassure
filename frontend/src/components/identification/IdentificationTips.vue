<template>
  <section class="identification-tips" aria-labelledby="identification-tips-title">
    <div class="identification-tips__intro">
      <h2 id="identification-tips-title">Tips for better identification</h2>
      <p>A few simple choices can make plant identification more reliable.</p>
    </div>

    <ul class="identification-tips__list">
      <li class="identification-tips__item">
        <v-icon icon="mdi-camera-outline" color="primary" aria-hidden="true" />
        <div>
          <h3>Use a clear, focused photo</h3>
          <p>Make sure the plant is in focus and well lit. Avoid blurry or dark photos.</p>
        </div>
      </li>
      <li class="identification-tips__item">
        <v-icon icon="mdi-leaf" color="primary" aria-hidden="true" />
        <div>
          <h3>Include key features</h3>
          <p>Where possible, show leaves, flowers, or fruit to help with identification.</p>
        </div>
      </li>
      <li class="identification-tips__item">
        <v-icon icon="mdi-sprout" color="primary" aria-hidden="true" />
        <div>
          <h3>Photograph one plant at a time</h3>
          <p>Keep the plant clear of clutter or other plants in the background.</p>
        </div>
      </li>
    </ul>
  </section>
</template>

<style scoped>
  .identification-tips {
    margin-top: var(--space-4xl);
    padding-top: var(--space-2xl);
    border-top: 1px solid var(--color-border);
  }

  .identification-tips__intro h2,
  .identification-tips__item h3 {
    margin: 0;
    color: var(--color-primary);
  }

  .identification-tips__intro p,
  .identification-tips__item p {
    margin: var(--space-sm) 0 0;
    color: var(--color-ink-soft);
  }

  .identification-tips__list {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: var(--space-xl);
    margin: var(--space-xl) 0 0;
    padding: 0;
    list-style: none;
  }

  .identification-tips__item {
    display: grid;
    grid-template-columns: auto minmax(0, 1fr);
    gap: var(--space-sm);
    align-items: start;
  }

  .identification-tips__item :deep(.v-icon) {
    margin-top: 0.125rem;
  }

  .identification-tips__item h3 {
    font-family: var(--font-body);
    font-size: 1rem;
    font-weight: 700;
  }

  @media (max-width: 767px) {
    .identification-tips {
      margin-top: var(--space-3xl);
    }

    .identification-tips__list {
      grid-template-columns: 1fr;
      gap: var(--space-lg);
    }
  }
</style>
