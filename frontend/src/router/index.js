import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import PostDetailView from '../views/PostDetailView.vue'
import ProfileView from '../views/ProfileView.vue'
import PublishView from '../views/PublishView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'home', component: HomeView },
    { path: '/posts/:id', name: 'post-detail', component: PostDetailView, props: true },
    { path: '/users/:id', name: 'profile', component: ProfileView, props: true },
    { path: '/publish', name: 'publish', component: PublishView, meta: { requiresAuth: true } }
  ],
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !localStorage.getItem('bbook_token')) {
    return '/'
  }
})

export default router
