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
    await load();         // currentPageが増える可能性
    await loadSessions(); // 履歴更新
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? "セッション追加に失敗しました";
  }
}

// ---------- Summaries ----------
const summaries = ref<Summary[]>([]);
const summaryScope = ref<"CHAPTER" | "BOOK">("CHAPTER");
const summaryChapter = ref<number>(1);
const summaryContent = ref<string>("");

// Backlog6: 編集状態
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
  <main style="max-width: 720px; margin: 24px auto;">
    <button @click="back">← Back</button>

    <h1 style="margin-top: 12px;">Book Detail</h1>

    <div v-if="!book">Loading...</div>

    <div v-else style="display:grid; gap: 10px; margin-top: 12px;">
      <div>
        <div style="font-weight: 700;">{{ book.title }}</div>
        <div style="opacity: .8;">
          {{ book.author || "-" }} / {{ book.language || "-" }} / {{ book.level || "-" }}
        </div>
      </div>

      <label>
        Status
        <select v-model="status">
          <option value="UNREAD">UNREAD</option>
          <option value="READING">READING</option>
          <option value="DONE">DONE</option>
        </select>
      </label>

      <label>
        Total pages
        <input
          type="number"
          :value="totalPages ?? ''"
          @input="(e:any)=> totalPages = e.target.value === '' ? null : Number(e.target.value)"
          min="0"
        />
      </label>

      <label>
        Current page
        <input type="number" v-model.number="currentPage" min="0" />
      </label>

      <button @click="save" :disabled="saving">
        {{ saving ? "Saving..." : "Save" }}
      </button>

      <p v-if="error" style="color: red;">{{ error }}</p>
    </div>

    <!-- Sessions -->
    <section v-if="book" style="margin-top: 24px; border-top: 1px solid #ddd; padding-top: 16px;">
      <h2>Reading Sessions</h2>

      <div style="display:grid; gap: 8px; margin: 12px 0;">
        <label>
          Date
          <input type="date" v-model="sessionDate" />
        </label>

        <label>
          Minutes
          <input type="number" v-model.number="sessionMinutes" min="1" />
        </label>

        <label>
          Pages read (optional)
          <input
            type="number"
            :value="sessionPagesRead ?? ''"
            @input="(e:any)=> sessionPagesRead = e.target.value === '' ? null : Number(e.target.value)"
            min="0"
          />
        </label>

        <label>
          Memo (optional)
          <input v-model="sessionMemo" placeholder="memo" />
        </label>

        <button @click="addSession">Add Session</button>
      </div>

      <ul>
        <li v-for="s in sessions" :key="s.id">
          {{ s.sessionDate }} / {{ s.minutes }} min
          <span v-if="s.pagesRead !== null"> / {{ s.pagesRead }} pages</span>
          <span v-if="s.memo"> / {{ s.memo }}</span>
        </li>
      </ul>
    </section>

    <!-- Summaries -->
    <section v-if="book" style="margin-top: 24px; border-top: 1px solid #ddd; padding-top: 16px;">
      <h2>Summaries</h2>

      <div style="display:grid; gap: 8px; margin: 12px 0;">
        <label>
          Scope
          <select v-model="summaryScope">
            <option value="CHAPTER">CHAPTER</option>
            <option value="BOOK">BOOK</option>
          </select>
        </label>

        <label v-if="summaryScope === 'CHAPTER'">
          Chapter
          <input type="number" v-model.number="summaryChapter" min="1" />
        </label>

        <label>
          Content (Markdown)
          <textarea v-model="summaryContent" rows="8" placeholder="Write summary in Markdown"></textarea>
        </label>

        <div style="display:flex; gap:8px;">
          <button @click="saveSummary">Save Summary</button>
          <button @click="clearSummaryForm" type="button">Clear</button>
        </div>
      </div>

      <div style="margin-top: 12px;">
        <h3>Saved</h3>
        <ul>
          <li v-for="s in summaries" :key="s.id" style="margin-bottom: 10px; border:1px solid #eee; padding:10px;">
            <div style="display:flex; justify-content: space-between; align-items:center; gap: 8px;">
              <div style="font-weight:700;">
                {{ s.scope }} <span v-if="s.chapter !== null">#{{ s.chapter }}</span>
                <span v-if="selectedSummaryId === s.id" style="opacity:.7;">（編集中）</span>
              </div>

              <div style="display:flex; gap: 8px;">
                <button @click="editSummary(s)">Edit</button>
                <button @click="deleteSummary(s)">Delete</button>
              </div>
            </div>

            <pre style="white-space: pre-wrap; background:#f7f7f7; padding:8px; margin-top: 8px;">{{ s.contentMd }}</pre>
          </li>
        </ul>
      </div>

      <button @click="downloadPdf">Export PDF</button>

    </section>
  </main>
</template>
