import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'
import PlantAssessmentView from '@/views/PlantAssessmentView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    {
      path: '/plants/:plantId/assessment',
      name: 'plant-assessment',
      component: PlantAssessmentView,
    },
  ],
})

export default router
