import { createRouter, createWebHistory } from "vue-router";
import Login from "./pages/Login.vue";
import Books from "./pages/Books.vue";
import BookDetail from "./pages/BookDetail.vue";
import Dashboard from "./pages/Dashboard.vue";

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/login", component: Login },
    { path: "/books", component: Books },
    { path: "/:pathMatch(.*)*", redirect: "/books" },
    { path: "/login", component: Login },
    { path: "/books", component: Books },
    { path: "/books/:id", component: BookDetail },
    { path: "/:pathMatch(.*)*", redirect: "/books" },
  { path: "/dashboard", component: Dashboard },
  ],
});

router.beforeEach((to) => {
  const token = localStorage.getItem("accessToken");
  if (!token && to.path !== "/login") return "/login";
  if (token && to.path === "/login") return "/books";
});
