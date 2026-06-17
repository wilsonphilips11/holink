<template>
  <div class="auth-layout">
    <div class="auth-card card">
      <h1>Create account</h1>
      <p class="subtitle">Start building your link-in-bio page</p>

      <div v-if="auth.error" class="alert alert-error">{{ auth.error }}</div>

      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <label for="email">Email</label>
          <input id="email" v-model="email" type="email" class="form-input" required />
        </div>
        <div class="form-group">
          <label for="password">Password</label>
          <input
            id="password"
            v-model="password"
            type="password"
            class="form-input"
            required
            minlength="8"
          />
        </div>
        <button type="submit" class="btn btn-primary btn-block" :disabled="auth.loading">
          {{ auth.loading ? 'Creating account...' : 'Register' }}
        </button>
      </form>

      <p class="footer-link">
        Already have an account?
        <router-link to="/login">Sign in</router-link>
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const auth = useAuthStore();
const router = useRouter();

const email = ref('');
const password = ref('');

async function handleRegister() {
  try {
    await auth.register(email.value, password.value);
    router.push('/dashboard');
  } catch {
    // error handled in store
  }
}
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.auth-card {
  width: 100%;
  max-width: 400px;
}

h1 {
  margin: 0;
  font-size: 1.75rem;
  text-align: center;
}

.subtitle {
  text-align: center;
  color: var(--color-text-muted);
  margin: 0.5rem 0 1.5rem;
}

.btn-block {
  width: 100%;
}

.footer-link {
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.875rem;
  color: var(--color-text-muted);
}

.footer-link a {
  color: var(--color-primary);
}
</style>
