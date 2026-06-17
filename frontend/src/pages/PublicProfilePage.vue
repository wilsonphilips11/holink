<template>
  <div class="public-page">
    <div v-if="loading" class="container loading">
      <p>Loading profile...</p>
    </div>

    <div v-else-if="error" class="container error">
      <div class="card empty-card">
        <h1>Profile not found</h1>
        <p>{{ error }}</p>
        <router-link to="/" class="btn btn-primary">Go home</router-link>
      </div>
    </div>

    <div v-else-if="publicProfile" class="container profile-container">
      <div class="profile-header">
        <img
          v-if="publicProfile.profile.avatarUrl"
          :src="publicProfile.profile.avatarUrl"
          :alt="`${publicProfile.profile.displayName} avatar`"
          class="avatar"
        />
        <div v-else class="avatar placeholder">
          {{ initials }}
        </div>
        <h1>{{ publicProfile.profile.displayName }}</h1>
        <p v-if="publicProfile.profile.bio" class="bio">{{ publicProfile.profile.bio }}</p>
        <p class="username">@{{ publicProfile.profile.username }}</p>
      </div>

      <div v-if="activeLinks.length === 0" class="empty-state card">No links available yet.</div>

      <div v-else class="links">
        <button v-for="link in activeLinks" :key="link.id" class="link-button" @click="handleLinkClick(link.id)">
          {{ link.title }}
        </button>
      </div>

      <footer class="footer">
        <span>Powered by HoLink</span>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { linkService, profileService } from "@/services";
import { getErrorMessage } from "@/services/api";
import type { PublicProfile } from "@/types";
import { computed, onMounted, ref } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const loading = ref(true);
const error = ref<string | null>(null);
const publicProfile = ref<PublicProfile | null>(null);

const initials = computed(() => {
  const name = publicProfile.value?.profile.displayName || "";
  return name
    .split(" ")
    .map((n) => n[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
});

const activeLinks = computed(() => publicProfile.value?.links.filter((link) => link.active) ?? []);

onMounted(async () => {
  const username = route.params.username as string;
  try {
    const { data } = await profileService.getPublic(username);
    publicProfile.value = data.data!;
  } catch (e) {
    error.value = getErrorMessage(e, "Profile not found");
  } finally {
    loading.value = false;
  }
});

async function handleLinkClick(linkId: string) {
  try {
    const routeQuery = route.query;
    const { data } = await linkService.trackClick(linkId, {
      utmSource: routeQuery.utm_source as string | undefined,
      utmMedium: routeQuery.utm_medium as string | undefined,
      utmCampaign: routeQuery.utm_campaign as string | undefined,
    });
    if (data.data?.redirectUrl) {
      window.location.href = data.data.redirectUrl;
    }
  } catch {
    const link = publicProfile.value?.links.find((l) => l.id === linkId);
    if (link) {
      window.location.href = link.url;
    }
  }
}
</script>

<style scoped>
.public-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #0f172a 0%, #1e1b4b 100%);
}

.loading,
.error {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  text-align: center;
}

.profile-container {
  max-width: 480px;
  padding: 2rem 1rem 3rem;
}

.profile-header {
  text-align: center;
  margin-bottom: 2rem;
}

.avatar {
  width: 96px;
  height: 96px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 1rem;
  border: 3px solid var(--color-primary);
}

.avatar.placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--color-primary);
  font-weight: 700;
  font-size: 1.5rem;
}

h1 {
  margin: 0 0 0.5rem;
  font-size: 1.5rem;
}

.bio {
  color: var(--color-text-muted);
  margin: 0 0 0.5rem;
  white-space: pre-wrap;
}

.username {
  color: var(--color-text-muted);
  font-size: 0.875rem;
  margin: 0;
}

.links {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.link-button {
  width: 100%;
  padding: 1rem 1.25rem;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  color: var(--color-text);
  font-weight: 600;
  cursor: pointer;
  transition:
    transform 0.15s,
    background 0.15s;
  overflow-wrap: anywhere;
}

.link-button:hover {
  background: var(--color-surface-hover);
  transform: translateY(-1px);
}

.link-button:active {
  transform: translateY(0);
}

.empty-card {
  text-align: center;
  padding: 2rem;
}

.empty-state {
  text-align: center;
  padding: 2rem;
}

.footer {
  text-align: center;
  margin-top: 2rem;
  font-size: 0.75rem;
  color: var(--color-text-muted);
}
</style>
