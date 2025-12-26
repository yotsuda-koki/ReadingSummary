<script setup lang="ts">
import { ref } from "vue";
import { api } from "../lib/api";

const email = ref("");
const password = ref("");
const loading = ref(false);
const error = ref<string | null>(null);

async function login() {
  error.value = null;
  loading.value = true;
  try {
    const res = await api.post("/api/auth/login", {
      email: email.value,
      password: password.value,
    });
    localStorage.setItem("accessToken", res.data.accessToken);
    location.href = "/books";
  } catch (e: any) {
    error.value = "ログインに失敗しました";
  } finally {
    loading.value = false;
    }
  }
</script>

<template>
  <main class="auth">
    <section class="card auth-card">
      <div class="auth-header">
        <div class="logo">📚</div>
        <div>
          <p class="eyebrow">Welcome back</p>
          <h1>Reading Summary</h1>
          <p class="subtle">日々の読書を美しく記録しましょう。</p>
        </div>
      </div>

      <div class="form-grid">
        <label class="field">
          <span>Email</span>
          <input v-model="email" placeholder="you@example.com" />
        </label>
        <label class="field">
          <span>Password</span>
          <input v-model="password" type="password" placeholder="••••••••" />
        </label>
        <button @click="login" :disabled="loading">
          {{ loading ? "Signing in..." : "Login" }}
        </button>
        <p v-if="error" class="error">{{ error }}</p>
        <p class="muted">
          初めて利用する方は
          <RouterLink to="/register">新規登録</RouterLink>
        </p>
      </div>
    </section>
  </main>
</template>

<style scoped>
.auth {
  display: grid;
  place-items: center;
  min-height: 100vh;
  padding: 32px 16px 48px;
}

.auth-card {
  width: min(520px, 100%);
  padding: 28px;
  display: grid;
  gap: 18px;
}

.auth-header {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo {
  width: 54px;
  height: 54px;
  display: grid;
  place-items: center;
  font-size: 26px;
  background: linear-gradient(135deg, #eef2ff, #e0f2fe);
  border-radius: 16px;
}

.form-grid {
  display: grid;
  gap: 12px;
}

.field {
  display: grid;
  gap: 6px;
  font-weight: 700;
  color: #0f172a;
}

.error {
  color: #dc2626;
  font-weight: 700;
}
</style>
