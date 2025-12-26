<script setup lang="ts">
import { ref } from "vue";
import { RouterLink } from "vue-router";
import { api } from "../lib/api";

const email = ref("");
const password = ref("");
const loading = ref(false);
const error = ref<string | null>(null);

async function register() {
  error.value = null;
  loading.value = true;
  try {
    const res = await api.post("/api/auth/register", {
      email: email.value,
      password: password.value,
    });
    localStorage.setItem("accessToken", res.data.accessToken);
    location.href = "/books";
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? "登録に失敗しました";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <main class="auth">
    <section class="card auth-card">
      <div class="auth-header">
        <div class="logo">✨</div>
        <div>
          <p class="eyebrow">Create account</p>
          <h1>はじめる準備をしましょう</h1>
          <p class="subtle">メールアドレスとパスワードを設定してください。</p>
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
        <button @click="register" :disabled="loading">
          {{ loading ? "Registering..." : "Register" }}
        </button>
        <p v-if="error" class="error">{{ error }}</p>
        <p class="muted">
          すでにアカウントをお持ちの方は
          <RouterLink to="/login">ログイン</RouterLink>
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
  background: linear-gradient(135deg, #dcfce7, #e0f2fe);
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