import { createRouter, createWebHistory } from "vue-router";
import Login from "./pages/Login.vue";
import Books from "./pages/Books.vue";
import BookDetail from "./pages/BookDetail.vue";
import Dashboard from "./pages/Dashboard.vue";
import Register from "./pages/Register.vue";

const publicPaths = ["/login", "/register"];

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/login", component: Login },
    { path: "/register", component: Register },    { path: "/books", component: Books },
    { path: "/books/:id", component: BookDetail },
    { path: "/dashboard", component: Dashboard },
    { path: "/:pathMatch(.*)*", redirect: "/books" },
  ],
});

router.beforeEach((to) => {
  const token = localStorage.getItem("accessToken");
  const isPublic = publicPaths.includes(to.path);
  if (!token && !isPublic) return "/login";
  if (token && isPublic) return "/books";
});
