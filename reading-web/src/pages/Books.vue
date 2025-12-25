<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { api } from "../lib/api";

type Book = { id: number; title: string; status: string };

const router = useRouter();

const title = ref("");
const books = ref<Book[]>([]);
const error = ref<string | null>(null);

async function load() {
  const res = await api.get("/api/books");
  books.value = res.data;
}

async function createBook() {
  error.value = null;
  if (!title.value.trim()) {
    error.value = "タイトルを入力してください";
    return;
  }
  await api.post("/api/books", { title: title.value, status: "UNREAD" });
  title.value = "";
  await load();
}

function goDashboard() {
  router.push("/dashboard");
}

function logout() {
  localStorage.removeItem("accessToken");
  router.push("/login");
}

onMounted(load);
</script>

<template>
  <main style="max-width: 720px; margin: 24px auto;">
    <header style="display:flex; justify-content: space-between; align-items:center;">
      <h1>Books</h1>
      <div style="display:flex; gap: 8px;">
        <button @click="goDashboard">Dashboard</button>
        <button @click="logout">Logout</button>
      </div>
    </header>

    <section style="display:flex; gap: 8px; margin: 12px 0;">
      <input v-model="title" placeholder="title" style="flex:1;" />
      <button @click="createBook">Add</button>
    </section>

    <p v-if="error" style="color: red;">{{ error }}</p>

    <ul>
      <li v-for="b in books" :key="b.id">
        <RouterLink :to="`/books/${b.id}`">{{ b.title }} ({{ b.status }})</RouterLink>
      </li>
    </ul>
  </main>
</template>
