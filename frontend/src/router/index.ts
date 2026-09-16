import HomeView from '@/views/HomeView.vue';
import PlantCatalogView from '@/views/PlantCatalogView.vue';
import PlantAssessmentView from '@/views/PlantAssessmentView.vue';
import BetterPlantAlternativesView from '@/views/BetterPlantAlternativesView.vue';
import PlantComparisonView from '@/views/PlantComparisonView.vue';
import { createRouter, createWebHistory } from 'vue-router';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/plants', name: 'plant-catalog', component: PlantCatalogView },
    {
      path: '/plants/:plantId/assessment',
      name: 'plant-assessment',
      component: PlantAssessmentView,
    },
    {
      path: '/plants/:plantId/alternatives',
      name: 'plant-alternatives',
      component: BetterPlantAlternativesView,
    },
    { path: '/compare', name: 'plant-comparison', component: PlantComparisonView },
  ],
});

export default router;
