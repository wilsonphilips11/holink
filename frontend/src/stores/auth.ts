import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { authService } from '@/services';
import { getErrorMessage } from '@/services/api';
import type { AuthResponse } from '@/types';

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(localStorage.getItem('accessToken'));
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'));
  const loading = ref(false);
  const error = ref<string | null>(null);

  const isAuthenticated = computed(() => !!accessToken.value);

  function setTokens(auth: AuthResponse) {
    accessToken.value = auth.accessToken;
    refreshToken.value = auth.refreshToken;
    localStorage.setItem('accessToken', auth.accessToken);
    localStorage.setItem('refreshToken', auth.refreshToken);
  }

  function clearTokens() {
    accessToken.value = null;
    refreshToken.value = null;
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }

  async function register(email: string, password: string) {
    loading.value = true;
    error.value = null;
    try {
      const { data } = await authService.register(email, password);
      setTokens(data.data!);
    } catch (e) {
      error.value = getErrorMessage(e, 'Registration failed');
      throw e;
    } finally {
      loading.value = false;
    }
  }

  async function login(email: string, password: string) {
    loading.value = true;
    error.value = null;
    try {
      const { data } = await authService.login(email, password);
      setTokens(data.data!);
    } catch (e) {
      error.value = getErrorMessage(e, 'Invalid email or password');
      throw e;
    } finally {
      loading.value = false;
    }
  }

  function logout() {
    clearTokens();
  }

  return {
    accessToken,
    refreshToken,
    loading,
    error,
    isAuthenticated,
    register,
    login,
    logout,
  };
});
