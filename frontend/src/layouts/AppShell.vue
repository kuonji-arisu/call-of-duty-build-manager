<script setup lang="ts">
import AppSidebar from "../components/AppSidebar.vue";
</script>

<template>
  <div class="app-shell-bg">
    <div class="grid min-h-screen gap-4 p-4 lg:grid-cols-[290px_minmax(0,1fr)] lg:p-6">
      <AppSidebar />

      <main class="min-w-0">
        <RouterView />
      </main>
    </div>
  </div>
</template>
