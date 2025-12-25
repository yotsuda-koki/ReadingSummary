<script setup lang="ts">
import { ref } from "vue";
import { api } from "../lib/api";

const email = ref("");
const password = ref("");
const error = ref<string | null>(null);

async function login() {
  error.value = null;
  try {
    const res = await api.post("/api/auth/login", {
      email: email.value,
      password: password.value,
    });
    localStorage.setItem("accessToken", res.data.accessToken);
    location.href = "/books";
  } catch (e: any) {
    error.value = "ログインに失敗しました";
  }
}
</script>

<template>
  <main style="max-width: 520px; margin: 24px auto;">
    <h1>Login</h1>

    <div style="display: grid; gap: 8px;">
      <input v-model="email" placeholder="email" />
      <input v-model="password" type="password" placeholder="password" />
      <button @click="login">Login</button>
      <p v-if="error" style="color: red;">{{ error }}</p>
    </div>
  </main>
</template>
