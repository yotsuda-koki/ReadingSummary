<script setup lang="ts">
import { onMounted, ref } from "vue";
import { api } from "../lib/api";

type Dashboard = {
  streakDays: number;
  thisMonthMinutes: number;
  thisMonthReadingDays: number;
  totalBooks: number;
  unreadBooks: number;
  readingBooks: number;
  doneBooks: number;
  totalSummaries: number;
};

const data = ref<Dashboard | null>(null);
const error = ref<string | null>(null);

async function load() {
  error.value = null;
  try {
    const res = await api.get("/api/dashboard");
    data.value = res.data;
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? "ダッシュボード取得に失敗しました";
  }
}

function goBooks() {
  location.href = "/books";
}

onMounted(load);
</script>

<template>
  <main class="page">
    <header class="page-header">
      <div>
        <p class="eyebrow">Overview</p>
        <h1>Dashboard</h1>
        <p class="muted">読み続けた日数や、今月のペースを確認しましょう。</p>
      </div>
      <button class="btn-ghost" @click="goBooks">📚 Books</button>
    </header>

    <p v-if="error" class="error">{{ error }}</p>
    <div v-if="!data && !error" class="muted">Loading...</div>

    <div v-if="data" class="grid">
      <section class="card stat-card highlight">
        <div class="section-heading">Streak</div>
        <div class="stat-value">{{ data.streakDays }} days</div>
        <p class="subtle">今日を含む連続日数</p>
      </section>

      <section class="card stat-card">
        <div class="section-heading">This month</div>
        <div class="stat-value">{{ data.thisMonthMinutes }} min</div>
        <p class="subtle">読書日数: {{ data.thisMonthReadingDays }} days</p>      </section>

      <section class="card book-card">
        <div class="section-heading">Books</div>
        <div class="book-stats">
          <div class="stat-pill">
            <span class="badge gray">Total</span>
            <strong>{{ data.totalBooks }}</strong>
          </div>
          <div class="stat-pill">
            <span class="badge yellow">UNREAD</span>
            <strong>{{ data.unreadBooks }}</strong>
          </div>
          <div class="stat-pill">
            <span class="badge blue">READING</span>
            <strong>{{ data.readingBooks }}</strong>
          </div>
          <div class="stat-pill">
            <span class="badge green">DONE</span>
            <strong>{{ data.doneBooks }}</strong>
          </div>
          <div class="stat-pill">
            <span class="chip">Summaries</span>
            <strong>{{ data.totalSummaries }}</strong>
          </div>
        </div>
      </section>

      <section class="card next-card">
        <div class="section-heading">Next</div>
        <p class="muted">
          追加予定
        </p>
      </section>
    </div>
  </main>
</template>


<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  margin-top: 18px;
}

.stat-card {
  display: grid;
  gap: 6px;
}

.stat-value {
  font-size: 2rem;
  font-weight: 800;
  color: #0f172a;
}

.book-card .book-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 10px;
}

.stat-pill {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  background: #f8fafc;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
}

.stat-pill strong {
  font-size: 1.4rem;
  color: #0b1627;
}

.highlight {
  background: linear-gradient(135deg, #eff6ff, #ecfdf3);
  border-color: transparent;
}

.next-card {
  display: grid;
  gap: 8px;
  background: linear-gradient(120deg, rgba(59, 130, 246, 0.08), rgba(16, 185, 129, 0.08));
}

.error {
  color: #dc2626;
  font-weight: 700;
}
</style>
