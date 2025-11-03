<template>
  <div id="app">
    <Navbar />
    <main class="container mt-4">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import Navbar from './components/Navbar.vue'
</script>

<style>
#app {
  min-height: 100vh;
  background-color: #f8f9fa;
}

main {
  padding-bottom: 2rem;
}
</style>
