<script setup lang="ts">
  const query = defineModel<string>({ default: '' });

  const emit = defineEmits<{
    search: [query: string];
  }>();

  function submitSearch() {
    emit('search', query.value.trim());
  }
</script>

<template>
  <form class="catalog-search-bar" role="search" @submit.prevent="submitSearch">
    <v-text-field
      v-model="query"
      class="catalog-search-bar__input"
      variant="outlined"
      color="primary"
      density="comfortable"
      hide-details
      placeholder="Search by common or scientific name"
      aria-label="Search by common or scientific name"
    />
    <v-btn
      class="catalog-search-bar__submit"
      type="submit"
      color="primary"
      variant="flat"
      height="52"
    >
      Search
    </v-btn>
  </form>
</template>

<style scoped>
  .catalog-search-bar {
    display: grid;
    grid-template-columns: minmax(0, 1fr) auto;
    align-items: start;
    gap: var(--space-sm);
    width: 100%;
  }

  .catalog-search-bar__submit {
    min-width: 84px;
  }

  @media (max-width: 479px) {
    .catalog-search-bar {
      grid-template-columns: 1fr;
    }

    .catalog-search-bar__submit {
      width: 100%;
    }
  }
</style>
