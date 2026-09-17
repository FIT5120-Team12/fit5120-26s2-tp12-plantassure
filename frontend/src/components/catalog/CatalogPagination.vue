<script setup lang="ts">
  const page = defineModel<number>({ default: 1 });

  defineProps<{
    length: number;
    disabled?: boolean;
  }>();
</script>

<template>
  <nav class="catalog-pagination" aria-label="Plant catalogue pages">
    <v-pagination
      v-model="page"
      :length="length"
      :disabled="disabled"
      :total-visible="5"
      rounded="circle"
    />
  </nav>
</template>

<style scoped>
  .catalog-pagination {
    display: flex;
    justify-content: center;
    width: 100%;
    padding-block: var(--space-lg);
  }

  @media (max-width: 479px) {
    .catalog-pagination {
      padding-block: var(--space-md);
    }
  }
</style>
