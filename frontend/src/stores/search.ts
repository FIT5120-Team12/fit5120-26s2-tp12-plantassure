import { defineStore } from 'pinia';
import { ref } from 'vue';

import { searchPlants } from '@/api/plants';
import type { PlantSearchResult } from '@/types/plant';

const SEARCH_ERROR_MESSAGE = 'We couldn’t load plant results. Please try again.';

export const useSearchStore = defineStore('search', () => {
  const query = ref('');
  const suggestions = ref<PlantSearchResult[]>([]);
  const isSuggesting = ref(false);
  const suggestionError = ref<string | null>(null);
  const results = ref<PlantSearchResult[]>([]);
  const isSearching = ref(false);
  const error = ref<string | null>(null);
  let suggestionRequestId = 0;
  let searchRequestId = 0;

  function setQuery(value: string) {
    query.value = value;
  }

  async function fetchSuggestions() {
    const keyword = query.value.trim();
    const requestId = ++suggestionRequestId;

    if (keyword.length < 3) {
      suggestions.value = [];
      isSuggesting.value = false;
      suggestionError.value = null;
      return;
    }

    isSuggesting.value = true;
    suggestionError.value = null;

    try {
      const response = await searchPlants(keyword);

      if (requestId !== suggestionRequestId || query.value.trim() !== keyword) return;

      suggestions.value = response.results;
    } catch {
      if (requestId !== suggestionRequestId || query.value.trim() !== keyword) return;

      suggestions.value = [];
      suggestionError.value = SEARCH_ERROR_MESSAGE;
    } finally {
      if (requestId === suggestionRequestId) isSuggesting.value = false;
    }
  }

  async function runSearch() {
    const keyword = query.value.trim();
    const requestId = ++searchRequestId;

    if (!keyword) {
      results.value = [];
      isSearching.value = false;
      error.value = null;
      return;
    }

    results.value = [];
    isSearching.value = true;
    error.value = null;

    try {
      const response = await searchPlants(keyword);

      if (requestId !== searchRequestId || query.value.trim() !== keyword) return;

      results.value = response.results;
    } catch {
      if (requestId !== searchRequestId || query.value.trim() !== keyword) return;

      results.value = [];
      error.value = SEARCH_ERROR_MESSAGE;
    } finally {
      if (requestId === searchRequestId) isSearching.value = false;
    }
  }

  function clearSuggestions() {
    suggestionRequestId += 1;
    suggestions.value = [];
    isSuggesting.value = false;
    suggestionError.value = null;
  }

  function clearResults() {
    searchRequestId += 1;
    results.value = [];
    isSearching.value = false;
    error.value = null;
  }

  function clearSearch() {
    query.value = '';
    clearSuggestions();
    clearResults();
  }

  return {
    query,
    suggestions,
    isSuggesting,
    suggestionError,
    results,
    isSearching,
    error,
    setQuery,
    fetchSuggestions,
    runSearch,
    clearSuggestions,
    clearResults,
    clearSearch,
  };
});
