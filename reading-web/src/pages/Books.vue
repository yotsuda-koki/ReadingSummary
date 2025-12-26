<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { api } from "../lib/api";

type Book = { id: number; title: string; status: string };

const router = useRouter();

const title = ref("");
const books = ref<Book[]>([]);
const error = ref<string | null>(null);
const editError = ref<string | null>(null);
const editingId = ref<number | null>(null);
const editingTitle = ref("");

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

function startEdit(b: Book) {
  editingId.value = b.id;
  editingTitle.value = b.title;
  editError.value = null;
}

function cancelEdit() {
  editingId.value = null;
  editingTitle.value = "";
  editError.value = null;
}

async function saveEdit() {
  if (editingId.value === null) return;
  editError.value = null;
  if (!editingTitle.value.trim()) {
    editError.value = "タイトルを入力してください";
    return;
  }

  try {
    await api.patch(`/api/books/${editingId.value}`, { title: editingTitle.value.trim() });
    cancelEdit();
    await load();
  } catch (e: any) {
    editError.value = e?.response?.data?.message ?? "名前の更新に失敗しました";
  }
}

async function deleteBook(id: number) {
  const ok = confirm("この本を削除しますか？");
  if (!ok) return;

  editError.value = null;
  try {
    await api.delete(`/api/books/${id}`);
    if (editingId.value === id) cancelEdit();
    await load();
  } catch (e: any) {
    editError.value = e?.response?.data?.message ?? "削除に失敗しました";
  }
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
      <p v-if="editError" class="error">{{ editError }}</p>
      <ul class="list-reset book-list">
        <li v-for="b in books" :key="b.id" class="card book-item">
          <div class="book-main">
            <template v-if="editingId === b.id">
              <input v-model="editingTitle" />
              <!-- <p class="muted">ID: {{ b.id }}</p> -->
            </template>
            <template v-else>
              <RouterLink :to="`/books/${b.id}`" class="title">{{ b.title }}</RouterLink>
              <!-- <p class="muted">ID: {{ b.id }}</p> -->
            </template>
          </div>
          <div class="book-actions">
            <span :class="['badge', statusClass(b.status)]">{{ b.status }}</span>
            <div class="action-buttons">
              <template v-if="editingId === b.id">
                <button class="btn-ghost" @click="cancelEdit">Cancel</button>
                <button @click="saveEdit">Save</button>
              </template>
              <template v-else>
                <button class="btn-ghost" @click="startEdit(b)">Edit name</button>
                <button class="btn-ghost danger" @click="deleteBook(b.id)">Delete</button>
              </template>
            </div>
          </div>
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

.book-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.book-main .title {
  font-weight: 800;
  color: #0f172a;
}

.book-main .title:hover {
  text-decoration: underline;
}

.danger {
  background: rgba(239, 68, 68, 0.1);
  color: #b91c1c;
}

.error {
  color: #dc2626;
  font-weight: 700;
}
</style>
