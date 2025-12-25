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
  <main style="max-width: 900px; margin: 24px auto;">
    <header style="display:flex; justify-content: space-between; align-items:center;">
      <h1>Dashboard</h1>
      <button @click="goBooks">Books</button>
    </header>

    <p v-if="error" style="color: red;">{{ error }}</p>
    <div v-if="!data && !error">Loading...</div>

    <div v-if="data" style="display:grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; margin-top: 12px;">
      <section style="border: 1px solid #ddd; padding: 12px;">
        <h2>Streak</h2>
        <div style="font-size: 28px; font-weight: 700;">{{ data.streakDays }} days</div>
        <div style="opacity: .8;">（今日を含む連続日数）</div>
      </section>

      <section style="border: 1px solid #ddd; padding: 12px;">
        <h2>This month</h2>
        <div style="font-size: 28px; font-weight: 700;">{{ data.thisMonthMinutes }} min</div>
        <div style="opacity: .8;">読書日数: {{ data.thisMonthReadingDays }} days</div>
      </section>

      <section style="border: 1px solid #ddd; padding: 12px;">
        <h2>Books</h2>
        <ul style="margin: 8px 0;">
          <li>Total: {{ data.totalBooks }}</li>
          <li>UNREAD: {{ data.unreadBooks }}</li>
          <li>READING: {{ data.readingBooks }}</li>
          <li>DONE: {{ data.doneBooks }}</li>
          <li>Total summaries: {{ data.totalSummaries }}</li>
        </ul>
      </section>

      <section style="border: 1px solid #ddd; padding: 12px;">
        <h2>Next</h2>
        <div style="opacity: .9;">
          Backlog 5（要約）が入ると「要約数」や「読了PDF化」などもここに足せます。
        </div>
      </section>
    </div>
  </main>
</template>
