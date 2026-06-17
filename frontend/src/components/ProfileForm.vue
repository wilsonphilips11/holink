<template>
  <section class="card">
    <h2>Profile</h2>

    <div v-if="loading" class="alert alert-info">Loading profile...</div>

    <div v-if="success" class="alert alert-success">{{ success }}</div>
    <div v-if="error || localError" class="alert alert-error">{{ error || localError }}</div>

    <form v-if="!loading" @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="username">Username</label>
        <input
          id="username"
          v-model="form.username"
          class="form-input"
          placeholder="johndoe"
          required
        />
        <small class="hint">Use letters and numbers. "John Doe" becomes johndoe; avoid all-caps or spaced lowercase.</small>
      </div>

      <div class="form-group">
        <label for="displayName">Display name</label>
        <input
          id="displayName"
          v-model="form.displayName"
          class="form-input"
          placeholder="John Doe"
          required
        />
      </div>

      <div class="form-group">
        <label for="bio">Bio</label>
        <textarea
          id="bio"
          v-model="form.bio"
          class="form-input"
          rows="3"
          maxlength="500"
          placeholder="Tell visitors about yourself"
        />
      </div>

      <div class="form-group">
        <label for="avatar">Avatar URL</label>
        <input
          id="avatar"
          v-model="form.avatarUrl"
          class="form-input"
          type="url"
          placeholder="https://example.com/avatar.jpg"
        />
      </div>

      <button type="submit" class="btn btn-primary" :disabled="saving">
        {{ saving ? 'Saving...' : 'Save profile' }}
      </button>
    </form>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import type { Profile } from '@/types';
import { isFreeOfHtml, validateUsernameInput } from '@/utils/validation';

const props = defineProps<{
  profile: Profile | null;
  loading: boolean;
  success: string | null;
  error: string | null;
}>();

const emit = defineEmits<{
  save: [payload: { username: string; displayName: string; bio?: string; avatarUrl?: string }];
}>();

const form = reactive({
  username: '',
  displayName: '',
  bio: '',
  avatarUrl: '',
});

const saving = ref(false);
const localError = ref<string | null>(null);

watch(
  () => props.profile,
  (profile) => {
    if (profile) {
      form.username = profile.username;
      form.displayName = profile.displayName;
      form.bio = profile.bio || '';
      form.avatarUrl = profile.avatarUrl || '';
    }
  },
  { immediate: true }
);

async function handleSubmit() {
  localError.value = null;

  const usernameError = validateUsernameInput(form.username);
  if (usernameError) {
    localError.value = usernameError;
    return;
  }

  if (!isFreeOfHtml(form.displayName) || !isFreeOfHtml(form.bio) || !isFreeOfHtml(form.username)) {
    localError.value = 'Invalid content detected';
    return;
  }

  saving.value = true;
  try {
    emit('save', {
      username: form.username,
      displayName: form.displayName,
      bio: form.bio || undefined,
      avatarUrl: form.avatarUrl || undefined,
    });
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
h2 {
  margin: 0 0 1rem;
  font-size: 1.25rem;
}

.hint {
  display: block;
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

textarea.form-input {
  resize: vertical;
  min-height: 80px;
}
</style>
