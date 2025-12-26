<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { api } from "../lib/api";

type Book = { id: number; title: string; status: string };

const router = useRouter();

const title = ref("");
const books = ref<Book[]>([]);
const error = ref<string | null>(null);

function statusClass(status: string) {
  if (status === "READING") return "blue";
  if (status === "DONE") return "green";
  if (status === "UNREAD") return "yellow";
  return "gray";
}

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
  <main class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Library</p>
        <h1>Books</h1>
        <p class="muted">ステータス別に本の進捗をまとめています。</p>
      </div>
      <div class="actions">
        <button class="btn-ghost" @click="goDashboard">Dashboard</button>
        <button class="btn-ghost" @click="logout">Logout</button>
      </div>
    </header>

    <section class="card form-card">
      <div class="section-heading">Add a new book</div>
      <div class="form-row">
        <input v-model="title" placeholder="読みたい本のタイトル" />
        <button @click="createBook">Add</button>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
    </section>

    <section class="list-section">
      <div class="section-heading">Your books</div>
      <ul class="list-reset book-list">
        <li v-for="b in books" :key="b.id" class="card book-item">
          <div class="book-main">
            <RouterLink :to="`/books/${b.id}`" class="title">{{ b.title }}</RouterLink>
            <p class="muted">ID: {{ b.id }}</p>
          </div>
          <span :class="['badge', statusClass(b.status)]">{{ b.status }}</span>
        </li>
      </ul>
    </section>
  </main>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
}

.actions {
  display: flex;
  gap: 8px;
}

.form-card {
  margin: 16px 0 18px;
  display: grid;
  gap: 10px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 10px;
}

.list-section {
  display: grid;
  gap: 12px;
}

.book-list {
  display: grid;
  gap: 10px;
}

.book-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
}

.book-main .title {
  font-weight: 800;
  color: #0f172a;
}

.book-main .title:hover {
  text-decoration: underline;
}

.error {
  color: #dc2626;
  font-weight: 700;
}
</style>
