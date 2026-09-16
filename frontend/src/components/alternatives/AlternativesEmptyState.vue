<script setup lang="ts">
  const emit = defineEmits<{
    browse: [];
  }>();
</script>

<template>
  <v-sheet class="alternatives-empty-state" border rounded="md" color="surface">
    <v-icon icon="mdi-leaf-off-outline" size="32" aria-hidden="true" />
    <div>
      <h2>No suitable lower-concern alternatives found</h2>
      <p>
        PlantAssure only shows alternatives supported by verified records. Not Assessed and
        regulated plants are not treated as lower-concern options.
      </p>
    </div>
    <v-btn color="primary" variant="outlined" type="button" @click="emit('browse')">
      Browse assessed plants
    </v-btn>
  </v-sheet>
</template>

<style scoped>
  .alternatives-empty-state {
    display: grid;
    grid-template-columns: auto minmax(0, 1fr) auto;
    align-items: center;
    gap: var(--space-lg);
    padding: var(--space-lg);
    border-color: var(--color-border);
    color: var(--color-primary);
  }

  .alternatives-empty-state h2 {
    margin: 0;
    font-size: 1.25rem;
    line-height: 1.2;
  }

  .alternatives-empty-state p {
    max-width: 50rem;
    margin: var(--space-xs) 0 0;
    color: var(--color-ink-soft);
    font-size: 0.9375rem;
  }

  @media (max-width: 767px) {
    .alternatives-empty-state {
      grid-template-columns: auto minmax(0, 1fr);
    }

    .alternatives-empty-state .v-btn {
      grid-column: 1 / -1;
      justify-self: start;
    }
  }

  @media (max-width: 479px) {
    .alternatives-empty-state {
      grid-template-columns: 1fr;
      justify-items: start;
    }

    .alternatives-empty-state .v-btn {
      grid-column: auto;
      width: 100%;
    }
  }
</style>
