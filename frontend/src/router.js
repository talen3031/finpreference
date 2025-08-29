import { createRouter, createWebHistory } from 'vue-router';
import LoginView from './views/LoginView.vue';
import ProductsView from './views/ProductsView.vue';
import LikesView from './views/LikesView.vue';
import RegisterView from './views/RegisterView.vue'; 
const routes = [
  { path: '/', redirect: '/app/likes' },
  { path: '/login', component: LoginView },
  { path: '/register', component: RegisterView }, 
  { path: '/app/products', component: ProductsView, meta: { requiresAuth: true } },
  { path: '/app/likes', component: LikesView, meta: { requiresAuth: true } },
];

const router = createRouter({ history: createWebHistory(), routes });

// 沒有 token 就回登入頁
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('accessToken');
  if (to.meta.requiresAuth && !token) return next('/login');
  next();
});

export default router;
