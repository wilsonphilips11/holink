<template>
  <div class="dashboard">
    <header class="dashboard-header">
      <div class="container header-inner">
        <h1>HoLink</h1>
        <div class="header-actions">
          <a
            v-if="profileStore.profile"
            :href="`/${profileStore.profile.username}`"
            target="_blank"
            class="btn btn-secondary btn-sm"
          >
            View public page
          </a>
          <button class="btn btn-secondary btn-sm" @click="handleLogout">Logout</button>
        </div>
      </div>
    </header>

    <main class="container dashboard-main">
      <ProfileForm
        :profile="profileStore.profile"
        :loading="profileStore.profileLoading"
        :success="profileStore.profileSuccessMessage"
        :error="profileStore.profileErrorMessage"
        @save="onSaveProfile"
      />

      <LinkManager
        v-if="profileStore.profile"
        :links="profileStore.links"
        :loading="profileStore.linksLoading"
        :success="profileStore.linksSuccessMessage"
        :error="profileStore.linksErrorMessage"
        :create-link="onCreateLink"
        :update-link="onUpdateLink"
        :delete-link="onDeleteLink"
        :reorder-links="onReorderLinks"
      />

      <AnalyticsSummary
        v-if="profileStore.profile"
        :analytics="profileStore.analytics"
        :loading="profileStore.analyticsLoading"
      />
    </main>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { useProfileStore } from '@/stores/profile';
import ProfileForm from '@/components/ProfileForm.vue';
import LinkManager from '@/components/LinkManager.vue';
import AnalyticsSummary from '@/components/AnalyticsSummary.vue';
import type { CreateLinkPayload, CreateProfilePayload } from '@/types';

const auth = useAuthStore();
const profileStore = useProfileStore();
const router = useRouter();

onMounted(async () => {
  await profileStore.loadProfile();
  if (profileStore.profile) {
    await Promise.all([profileStore.loadLinks(), profileStore.loadAnalytics()]);
  }
});

async function onSaveProfile(payload: CreateProfilePayload) {
  await profileStore.saveProfile(payload);
  await profileStore.loadProfile();
  if (profileStore.profile) {
    await Promise.all([profileStore.loadLinks(), profileStore.loadAnalytics()]);
  }
}

async function onCreateLink(payload: CreateLinkPayload) {
  await profileStore.createLink(payload);
  await profileStore.loadAnalytics();
}

async function onUpdateLink(id: string, payload: CreateLinkPayload) {
  await profileStore.updateLink(id, payload);
  await profileStore.loadAnalytics();
}

async function onDeleteLink(id: string) {
  await profileStore.deleteLink(id);
  await profileStore.loadAnalytics();
}

async function onReorderLinks(linkIds: string[]) {
  await profileStore.reorderLinks(linkIds);
}

function handleLogout() {
  auth.logout();
  router.push('/login');
}
</script>

<style scoped>
.dashboard-header {
  border-bottom: 1px solid var(--color-border);
  background: var(--color-surface);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 1rem;
  padding-bottom: 1rem;
}

h1 {
  margin: 0;
  font-size: 1.25rem;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
}

.dashboard-main {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding-top: 1.5rem;
  padding-bottom: 2rem;
}
</style>
