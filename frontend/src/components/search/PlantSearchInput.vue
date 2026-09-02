<script setup lang="ts">
  import { storeToRefs } from 'pinia';
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';

  import AutocompleteDropdown from '@/components/search/AutocompleteDropdown.vue';
import PlantSearchResults from '@/components/search/PlantSearchResults.vue';
import { useSearchStore } from '@/stores/search';
import type { PlantSearchResult } from '@/types/plant';

  const AUTOCOMPLETE_DELAY = 275;
  const LISTBOX_ID = 'plant-search-suggestions';

  const router = useRouter();
  const searchStore = useSearchStore();
  const { query, suggestions, isSuggesting, suggestionError, results, isSearching, error } =
    storeToRefs(searchStore);
  const root = ref<HTMLFormElement | null>(null);
  const input = ref<HTMLInputElement | null>(null);
  const isOpen = ref(false);
  const activeIndex = ref(-1);
  const suggestionQuery = ref('');
  const explicitQuery = ref('');
  const showExplicitResults = ref(false);
  let debounceTimer: ReturnType<typeof setTimeout> | null = null;
  let suggestionLoadId = 0;

  function focusInput() {
    input.value?.focus();
  }

  const activeDescendant = computed(() =>
    isOpen.value && activeIndex.value >= 0
      ? `${LISTBOX_ID}-option-${activeIndex.value}`
      : undefined,
  );

  function clearDebounce() {
    if (debounceTimer === null) return;
    clearTimeout(debounceTimer);
    debounceTimer = null;
  }

  function closeDropdown() {
    isOpen.value = false;
    activeIndex.value = -1;
  }

  async function loadSuggestions(expectedQuery: string) {
    if (query.value.trim() !== expectedQuery || expectedQuery.length < 3) return;
    const loadId = ++suggestionLoadId;

    activeIndex.value = -1;
    isOpen.value = true;
    await searchStore.fetchSuggestions();

    if (loadId !== suggestionLoadId || query.value.trim() !== expectedQuery) return;
    suggestionQuery.value = expectedQuery;
    isOpen.value = true;
  }

  function handleInput(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    const normalizedQuery = value.trim();

    searchStore.setQuery(value);
    searchStore.clearResults();
    explicitQuery.value = '';
    showExplicitResults.value = false;
    clearDebounce();
    closeDropdown();

    if (normalizedQuery.length < 3) {
      suggestionLoadId += 1;
      suggestionQuery.value = '';
      void searchStore.fetchSuggestions();
      return;
    }

    if (suggestionQuery.value === normalizedQuery) {
      isOpen.value = true;
      return;
    }

    debounceTimer = setTimeout(() => {
      debounceTimer = null;
      void loadSuggestions(normalizedQuery);
    }, AUTOCOMPLETE_DELAY);
  }

  function handleFocus() {
    const normalizedQuery = query.value.trim();

    if (normalizedQuery.length >= 3 && suggestionQuery.value === normalizedQuery) {
      isOpen.value = true;
    }
  }

  function moveActiveSelection(direction: 1 | -1) {
    if (suggestions.value.length === 0) return;

    if (!isOpen.value && suggestionQuery.value === query.value.trim()) isOpen.value = true;
    if (!isOpen.value) return;

    if (activeIndex.value === -1) {
      activeIndex.value = direction === 1 ? 0 : suggestions.value.length - 1;
      return;
    }

    activeIndex.value =
      (activeIndex.value + direction + suggestions.value.length) % suggestions.value.length;
  }

  function handleKeydown(event: KeyboardEvent) {
    if (event.key === 'ArrowDown') {
      event.preventDefault();
      moveActiveSelection(1);
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      moveActiveSelection(-1);
    } else if (event.key === 'Enter' && isOpen.value && activeIndex.value >= 0) {
      event.preventDefault();
      const activeSuggestion = suggestions.value[activeIndex.value];
      if (activeSuggestion) void selectSuggestion(activeSuggestion);
    } else if (event.key === 'Escape' && isOpen.value) {
      event.preventDefault();
      closeDropdown();
    }
  }

  async function selectSuggestion(suggestion: PlantSearchResult) {
    closeDropdown();
    await router.push({ name: 'plant-assessment', params: { plantId: suggestion.plantId } });
  }

  function retrySuggestions() {
    const normalizedQuery = query.value.trim();
    if (normalizedQuery.length < 3) return;
    void loadSuggestions(normalizedQuery);
  }

  async function executeExplicitSearch() {
    const normalizedQuery = query.value.trim();

    if (isSearching.value && explicitQuery.value === normalizedQuery) return;

    clearDebounce();
    suggestionLoadId += 1;
    closeDropdown();
    searchStore.clearSuggestions();
    suggestionQuery.value = '';

    if (!normalizedQuery) {
      showExplicitResults.value = false;
      searchStore.clearResults();
      input.value?.focus();
      return;
    }

    explicitQuery.value = normalizedQuery;
    showExplicitResults.value = true;
    await searchStore.runSearch();

    if (
      !showExplicitResults.value ||
      explicitQuery.value !== normalizedQuery ||
      query.value.trim() !== normalizedQuery ||
      error.value
    ) {
      return;
    }

    const [onlyResult] = results.value;
    if (results.value.length === 1 && onlyResult) await selectSuggestion(onlyResult);
  }

  function retryExplicitSearch() {
    if (query.value.trim() !== explicitQuery.value) return;
    void executeExplicitSearch();
  }

  function handleDocumentPointerDown(event: PointerEvent) {
    if (root.value?.contains(event.target as Node)) return;
    closeDropdown();
  }

  onMounted(() => document.addEventListener('pointerdown', handleDocumentPointerDown));
  onBeforeUnmount(() => {
    clearDebounce();
    suggestionLoadId += 1;
    document.removeEventListener('pointerdown', handleDocumentPointerDown);
  });

  defineExpose({ focusInput });
</script>

<template>
  <form
    ref="root"
    class="plant-search"
    role="search"
    :aria-busy="isSearching"
    @submit.prevent="executeExplicitSearch"
  >
    <label class="visually-hidden" for="plant-search-input">
      Search by common or scientific name
    </label>
    <div class="plant-search__autocomplete">
      <div class="plant-search__control">
        <i class="plant-search__icon mdi mdi-magnify" aria-hidden="true"></i>
        <input
          id="plant-search-input"
          ref="input"
          :value="query"
          type="search"
          name="plant-search"
          required
          maxlength="255"
          autocomplete="off"
          placeholder="Search by common or scientific name"
          role="combobox"
          aria-autocomplete="list"
          :aria-expanded="isOpen"
          :aria-controls="LISTBOX_ID"
          :aria-activedescendant="activeDescendant"
          @input="handleInput"
          @focus="handleFocus"
          @keydown="handleKeydown"
        />
        <button type="submit" :disabled="isSearching">Search</button>
      </div>
      <AutocompleteDropdown
        v-if="isOpen"
        :id="LISTBOX_ID"
        :suggestions="suggestions"
        :is-loading="isSuggesting"
        :error="suggestionError"
        :active-index="activeIndex"
        @activate="activeIndex = $event"
        @select="selectSuggestion"
        @retry="retrySuggestions"
      />
    </div>
    <PlantSearchResults
      v-if="showExplicitResults"
      :results="results"
      :is-loading="isSearching"
      :error="error"
      @select="selectSuggestion"
      @retry="retryExplicitSearch"
    />
  </form>
</template>

<style scoped>
  .plant-search {
    position: relative;
    z-index: 2;
    width: 100%;
  }

  .plant-search__autocomplete {
    position: relative;
  }

  .plant-search__control {
    min-height: 52px;
    display: grid;
    grid-template-columns: auto minmax(0, 1fr) auto;
    align-items: center;
    gap: 0;
    padding: 3px 6px 3px var(--space-md);
    background: var(--color-surface);
    border: 1px solid var(--color-border-strong);
    border-radius: var(--radius-md);
    transition:
      border-color 160ms ease,
      box-shadow 160ms ease;
  }

  .plant-search__control:focus-within {
    border-color: var(--color-focus);
    box-shadow: 0 0 0 3px color-mix(in srgb, var(--color-focus) 24%, transparent);
  }

  .plant-search__icon {
    margin-right: var(--space-sm);
    color: var(--color-muted);
    font-size: 22px;
  }

  .plant-search input {
    min-width: 0;
    height: 44px;
    padding: 0;
    border: 0;
    outline: 0;
    background: transparent;
    color: var(--color-ink);
    font-size: 0.9375rem;
  }

  .plant-search input::placeholder {
    color: var(--color-muted);
    opacity: 1;
  }

  .plant-search button {
    min-width: 84px;
    min-height: 44px;
    padding: 0 18px;
    border: 0;
    border-radius: var(--radius-sm);
    background: var(--color-primary);
    color: var(--color-on-primary);
    font-size: 0.875rem;
    font-weight: 600;
    cursor: pointer;
    transition: background-color 160ms ease;
  }

  .plant-search button:hover {
    background: var(--color-primary-hover);
  }

  .plant-search button:disabled {
    cursor: wait;
    opacity: 0.72;
  }

  .plant-search button:active {
    transform: translateY(1px);
  }

  .plant-search input:focus-visible {
    outline: 0;
  }

  @media (max-width: 479px) {
    .plant-search__control {
      grid-template-columns: auto minmax(0, 1fr);
      padding: 3px 4px 3px var(--space-md);
    }

    .plant-search button {
      grid-column: 1 / -1;
      width: 100%;
      border-radius: var(--radius-sm);
    }
  }
</style>
