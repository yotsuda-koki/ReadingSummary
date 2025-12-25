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
  <main style="max-width: 520px; margin: 24px auto;">
    <h1>Login</h1>

    <div style="display: grid; gap: 8px;">
      <input v-model="email" placeholder="email" />
      <input v-model="password" type="password" placeholder="password" />
      <button @click="login" :disabled="loading">
        {{ loading ? "Signing in..." : "Login" }}
      </button>
      <p v-if="error" style="color: red;">{{ error }}</p>
      <p>
        初めて利用する方は
        <RouterLink to="/register">新規登録</RouterLink>
      </p>
    </div>
  </main>
</template>
