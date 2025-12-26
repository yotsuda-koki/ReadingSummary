<template>
  <div class="app-shell">
    <router-view />
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: radial-gradient(circle at 10% 10%, rgba(80, 129, 251, 0.08), transparent 30%),
    radial-gradient(circle at 90% 20%, rgba(52, 211, 153, 0.08), transparent 26%),
    #f5f7fb;
}
</style>