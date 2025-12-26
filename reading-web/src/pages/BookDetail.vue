<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { api } from "../lib/api";

type Book = {
  id: number;
  title: string;
  author: string | null;
  language: string | null;
  level: string | null;
  status: string;
  totalPages: number | null;
  currentPage: number;
};

type ReadingSession = {
  id: number;
  sessionDate: string; // "YYYY-MM-DD"
  minutes: number;
  pagesRead: number | null;
  memo: string | null;
};

type Summary = {
  id: number;
  scope: "CHAPTER" | "BOOK";
  chapter: number | null;
  contentMd: string;
  createdAt: string;
  updatedAt: string;
};

const route = useRoute();
const id = computed(() => Number(route.params.id));

const book = ref<Book | null>(null);
const status = ref("UNREAD");
const totalPages = ref<number | null>(null);
const currentPage = ref<number>(0);

const error = ref<string | null>(null);
const saving = ref(false);

function statusClass(value: string) {
  if (value === "READING") return "blue";
  if (value === "DONE") return "green";
  if (value === "UNREAD") return "yellow";
  return "gray";
}

// ---------- Book ----------
async function load() {
  error.value = null;
  const res = await api.get(`/api/books/${id.value}`);
  book.value = res.data;

  status.value = res.data.status ?? "UNREAD";
  totalPages.value = res.data.totalPages ?? null;
  currentPage.value = res.data.currentPage ?? 0;
}

async function save() {
  error.value = null;
  saving.value = true;
  try {
    await api.patch(`/api/books/${id.value}`, {
      status: status.value,
      currentPage: currentPage.value,
      totalPages: totalPages.value,
    });
    await load();
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? "更新に失敗しました";
  } finally {
    saving.value = false;
  }
}

// ---------- Sessions ----------
const sessions = ref<ReadingSession[]>([]);

function todayLocalISO(): string {
  const d = new Date();
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, "0");
  const dd = String(d.getDate()).padStart(2, "0");
  return `${yyyy}-${mm}-${dd}`;
}

const sessionDate = ref<string>(todayLocalISO());
const sessionMinutes = ref<number>(30);
const sessionPagesRead = ref<number | null>(null);
const sessionMemo = ref<string>("");

async function loadSessions() {
  const res = await api.get(`/api/books/${id.value}/sessions`);
  sessions.value = res.data;
}

async function addSession() {
  error.value = null;
  try {
    await api.post(`/api/books/${id.value}/sessions`, {
      sessionDate: sessionDate.value,
      minutes: sessionMinutes.value,
      pagesRead: sessionPagesRead.value,
      memo: sessionMemo.value || null,
    });
    sessionPagesRead.value = null;
    sessionMemo.value = "";
    await load();
    await loadSessions();
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? "セッション追加に失敗しました";
  }
}

// ---------- Summaries ----------
const summaries = ref<Summary[]>([]);
const summaryScope = ref<"CHAPTER" | "BOOK">("CHAPTER");
const summaryChapter = ref<number>(1);
const summaryContent = ref<string>("");

const selectedSummaryId = ref<number | null>(null);

async function loadSummaries() {
  const res = await api.get(`/api/books/${id.value}/summaries`);
  summaries.value = res.data;
}

async function saveSummary() {
  error.value = null;
  try {
    const payload: any = {
      scope: summaryScope.value,
      contentMd: summaryContent.value,
    };
    if (summaryScope.value === "CHAPTER") payload.chapter = summaryChapter.value;

    await api.post(`/api/books/${id.value}/summaries`, payload);
    await loadSummaries();
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? "要約保存に失敗しました";
  }
}

function editSummary(s: Summary) {
  selectedSummaryId.value = s.id;
  summaryScope.value = s.scope;
  summaryChapter.value = s.chapter ?? 1;
  summaryContent.value = s.contentMd;
}

function clearSummaryForm() {
  selectedSummaryId.value = null;
  summaryScope.value = "CHAPTER";
  summaryChapter.value = 1;
  summaryContent.value = "";
}

async function deleteSummary(s: Summary) {
  const ok = confirm("この要約を削除しますか？");
  if (!ok) return;

  error.value = null;
  try {
    await api.delete(`/api/books/${id.value}/summaries/${s.id}`);
    if (selectedSummaryId.value === s.id) clearSummaryForm();
    await loadSummaries();
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? "要約削除に失敗しました";
  }
}

async function downloadPdf() {
  error.value = null;
  try {
    const res = await api.get(`/api/books/${id.value}/report.pdf`, { responseType: "blob" });
    const blob = new Blob([res.data], { type: "application/pdf" });
    const url = URL.createObjectURL(blob);

    const a = document.createElement("a");
    a.href = url;
    a.download = `reading-report-${id.value}.pdf`;
    a.click();

    URL.revokeObjectURL(url);
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? "PDF出力に失敗しました";
  }
}

function back() {
  location.href = "/books";
}

onMounted(async () => {
  await load();
  await loadSessions();
  await loadSummaries();
});
</script>

<template>
  <main class="page">
    <button class="btn-ghost back" @click="back">← Booksに戻る</button>

    <div v-if="!book" class="muted">Loading...</div>

    <template v-else>
      <section class="card hero">
        <div>
          <p class="eyebrow">Book detail</p>
          <h1>{{ book.title }}</h1>
          <p class="muted info">{{ book.author || "-" }} / {{ book.language || "-" }} / {{ book.level || "-" }}</p>
        </div>
        <div class="badge" :class="statusClass(book.status)">{{ book.status }}</div>
      </section>

      <section class="card form-grid">
        <div class="section-heading">現在のステータス</div>
        <div class="grid two-col">
          <label class="field">
            <span>Status</span>
            <select v-model="status">
              <option value="UNREAD">UNREAD</option>
              <option value="READING">READING</option>
              <option value="DONE">DONE</option>
            </select>
          </label>
          <label class="field">
            <span>Total pages</span>
            <input
              type="number"
              :value="totalPages ?? ''"
              @input="(e:any)=> totalPages = e.target.value === '' ? null : Number(e.target.value)"
              min="0"
            />
          </label>
          <label class="field">
            <span>Current page</span>
            <input type="number" v-model.number="currentPage" min="0" />
          </label>        </div>
        <div class="action-row">
          <button @click="save" :disabled="saving">
            {{ saving ? "Saving..." : "Save" }}
          </button>
          <p v-if="error" class="error">{{ error }}</p>
        </div>
      </section>

      <section class="card">
        <div class="section-heading">Reading Sessions</div>
        <div class="form-grid">
          <div class="grid two-col">
            <label class="field">
              <span>Date</span>
              <input type="date" v-model="sessionDate" />
            </label>
            <label class="field">
              <span>Minutes</span>
              <input type="number" v-model.number="sessionMinutes" min="1" />
            </label>
            <label class="field">
              <span>Pages read (optional)</span>
              <input
                type="number"
                :value="sessionPagesRead ?? ''"
                @input="(e:any)=> sessionPagesRead = e.target.value === '' ? null : Number(e.target.value)"
                min="0"
              />
            </label>
            <label class="field">
              <span>Memo (optional)</span>
              <input v-model="sessionMemo" placeholder="メモを追加" />
            </label>
          </div>
          <button class="stretch" @click="addSession">Add Session</button>
        </div>
        <ul class="list-reset session-list">
          <li v-for="s in sessions" :key="s.id" class="session-item">
            <div>
              <strong>{{ s.sessionDate }}</strong>
              <p class="muted">{{ s.minutes }} min<span v-if="s.pagesRead !== null"> / {{ s.pagesRead }} pages</span></p>
            </div>

<p class="muted" v-if="s.memo">{{ s.memo }}</p>          </li>
        </ul>
      </section>

      <section class="card">
        <div class="section-heading">Summaries</div>
        <div class="form-grid">
          <div class="grid two-col">
            <label class="field">
              <span>Scope</span>
              <select v-model="summaryScope">
                <option value="CHAPTER">CHAPTER</option>
                <option value="BOOK">BOOK</option>
              </select>
            </label>
            <label v-if="summaryScope === 'CHAPTER'" class="field">
              <span>Chapter</span>
              <input type="number" v-model.number="summaryChapter" min="1" />
            </label>
          </div>
          <label class="field">
            <span>Content (Markdown)</span>
            <textarea v-model="summaryContent" rows="8" placeholder="Write summary in Markdown"></textarea>
          </label>
          <div class="action-row">
            <button @click="saveSummary">Save Summary</button>
            <button type="button" class="btn-ghost" @click="clearSummaryForm">Clear</button>
          </div>
        </div>

        <div class="saved">
          <div class="section-heading">Saved</div>
          <ul class="list-reset saved-list">
            <li v-for="s in summaries" :key="s.id" class="saved-item">
              <div>
                <div class="title-row">
                  <div>
                    <span class="badge" :class="statusClass(s.scope === 'BOOK' ? 'DONE' : 'READING')">
                      {{ s.scope }}<span v-if="s.chapter !== null"> #{{ s.chapter }}</span>
                    </span>
                    <span v-if="selectedSummaryId === s.id" class="muted">（編集中）</span>
                  </div>
                  <div class="actions">
                    <button class="btn-ghost" @click="editSummary(s)">Edit</button>
                    <button class="btn-ghost" @click="deleteSummary(s)">Delete</button>
                  </div>
                </div>
                <pre class="summary-content">{{ s.contentMd }}</pre>
              </div>
            </li>
          </ul>
        </div>
        <button class="stretch" @click="downloadPdf">Export PDF</button>
      </section>
    </template>  </main>
</template>

<style scoped>
.back {
  margin-bottom: 12px;
}

.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
}

.info {
  margin-top: 6px;
}

.form-grid {
  display: grid;
  gap: 12px;
}

.grid.two-col {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.field {
  display: grid;
  gap: 6px;
  font-weight: 700;
  color: #0f172a;
}

.actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}

.action-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stretch {
  width: fit-content;
}

.session-list {
  margin-top: 14px;
  display: grid;
  gap: 10px;
}

.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #e5e7eb;
}

.saved {
  margin-top: 16px;
  display: grid;
  gap: 12px;
}

.saved-list {
  display: grid;
  gap: 12px;
}

.saved-item {
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px 14px;
  background: #f8fafc;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.summary-content {
  white-space: pre-wrap;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  padding: 10px;
  margin-top: 8px;
  font-family: "Inter", system-ui, sans-serif;
}

.error {
  color: #dc2626;
  font-weight: 700;
}
</style>