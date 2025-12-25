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
  <main style="max-width: 520px; margin: 24px auto;">
    <h1>Register</h1>

    <div style="display: grid; gap: 8px;">
      <input v-model="email" placeholder="email" />
      <input v-model="password" type="password" placeholder="password" />
      <button @click="register" :disabled="loading">
        {{ loading ? "Registering..." : "Register" }}
      </button>
      <p v-if="error" style="color: red;">{{ error }}</p>
      <p>
        すでにアカウントをお持ちの方は
        <RouterLink to="/login">ログイン</RouterLink>
      </p>
    </div>
  </main>
</template>