import { defineStore } from 'pinia';
import { ref } from 'vue';
import { profileService, linkService, analyticsService } from '@/services';
import { getErrorMessage } from '@/services/api';
import type { CreateLinkPayload, CreateProfilePayload, Link, LinkAnalytics, Profile } from '@/types';

export const useProfileStore = defineStore('profile', () => {
  const profile = ref<Profile | null>(null);
  const links = ref<Link[]>([]);
  const analytics = ref<LinkAnalytics | null>(null);
  const profileLoading = ref(false);
  const linksLoading = ref(false);
  const analyticsLoading = ref(false);
  const profileSuccessMessage = ref<string | null>(null);
  const profileErrorMessage = ref<string | null>(null);
  const linksSuccessMessage = ref<string | null>(null);
  const linksErrorMessage = ref<string | null>(null);
  const analyticsErrorMessage = ref<string | null>(null);
  let profileSuccessMessageTimeout: ReturnType<typeof setTimeout> | null = null;
  let linksSuccessMessageTimeout: ReturnType<typeof setTimeout> | null = null;

  function clearMessages() {
    clearProfileMessages();
    clearLinksMessages();
    analyticsErrorMessage.value = null;
  }

  function clearProfileMessages() {
    if (profileSuccessMessageTimeout) {
      clearTimeout(profileSuccessMessageTimeout);
      profileSuccessMessageTimeout = null;
    }
    profileSuccessMessage.value = null;
    profileErrorMessage.value = null;
  }

  function clearLinksMessages() {
    if (linksSuccessMessageTimeout) {
      clearTimeout(linksSuccessMessageTimeout);
      linksSuccessMessageTimeout = null;
    }
    linksSuccessMessage.value = null;
    linksErrorMessage.value = null;
  }

  function setProfileSuccessMessage(message: string) {
    if (profileSuccessMessageTimeout) {
      clearTimeout(profileSuccessMessageTimeout);
    }
    profileSuccessMessage.value = message;
    profileSuccessMessageTimeout = setTimeout(() => {
      profileSuccessMessage.value = null;
      profileSuccessMessageTimeout = null;
    }, 3000);
  }

  function setLinksSuccessMessage(message: string) {
    if (linksSuccessMessageTimeout) {
      clearTimeout(linksSuccessMessageTimeout);
    }
    linksSuccessMessage.value = message;
    linksSuccessMessageTimeout = setTimeout(() => {
      linksSuccessMessage.value = null;
      linksSuccessMessageTimeout = null;
    }, 3000);
  }

  async function loadProfile() {
    profileLoading.value = true;
    profileErrorMessage.value = null;
    try {
      const { data } = await profileService.getMine();
      profile.value = data.data!;
    } catch (e) {
      profileErrorMessage.value = getErrorMessage(e, 'Failed to load profile');
      profile.value = null;
    } finally {
      profileLoading.value = false;
    }
  }

  async function saveProfile(payload: CreateProfilePayload) {
    clearProfileMessages();
    profileLoading.value = true;
    try {
      if (profile.value?.id) {
        const { data } = await profileService.update(profile.value.id, payload);
        profile.value = data.data!;
        setProfileSuccessMessage('Profile saved successfully');
      } else {
        const { data } = await profileService.create(payload);
        profile.value = data.data!;
        setProfileSuccessMessage('Profile saved successfully');
      }
    } catch (e) {
      profileErrorMessage.value = getErrorMessage(e, 'Failed to save profile');
      throw e;
    } finally {
      profileLoading.value = false;
    }
  }

  async function loadLinks(showLoading = true) {
    if (showLoading) {
      linksLoading.value = true;
    }
    linksErrorMessage.value = null;
    try {
      const { data } = await linkService.list();
      links.value = data.data ?? [];
    } catch (e) {
      linksErrorMessage.value = getErrorMessage(e, 'Failed to load links');
      links.value = [];
    } finally {
      if (showLoading) {
        linksLoading.value = false;
      }
    }
  }

  async function createLink(payload: CreateLinkPayload) {
    clearLinksMessages();
    try {
      const { data } = await linkService.create(payload);
      if (data.data) {
        links.value = [...links.value, data.data].sort((a, b) => a.position - b.position);
      }
      setLinksSuccessMessage('Link created successfully');
      await loadLinks(false);
    } catch (e) {
      linksErrorMessage.value = getErrorMessage(e, 'Failed to create link');
      throw e;
    }
  }

  async function updateLink(id: string, payload: CreateLinkPayload) {
    clearLinksMessages();
    try {
      const { data } = await linkService.update(id, payload);
      if (data.data) {
        links.value = links.value.map((link) => (link.id === id ? data.data! : link));
      }
      setLinksSuccessMessage('Link updated successfully');
      await loadLinks(false);
    } catch (e) {
      linksErrorMessage.value = getErrorMessage(e, 'Failed to update link');
      throw e;
    }
  }

  async function deleteLink(id: string) {
    clearLinksMessages();
    try {
      await linkService.remove(id);
      links.value = links.value.filter((link) => link.id !== id);
      setLinksSuccessMessage('Link deleted successfully');
      await loadLinks(false);
    } catch (e) {
      linksErrorMessage.value = getErrorMessage(e, 'Failed to delete link');
      throw e;
    }
  }

  async function reorderLinks(linkIds: string[]) {
    clearLinksMessages();
    const previousLinks = [...links.value];
    links.value = linkIds
      .map((id) => links.value.find((link) => link.id === id))
      .filter((link): link is Link => Boolean(link))
      .map((link, position) => ({ ...link, position }));
    try {
      const { data } = await linkService.reorder(linkIds);
      links.value = data.data ?? [];
    } catch (e) {
      links.value = previousLinks;
      linksErrorMessage.value = getErrorMessage(e, 'Failed to reorder links');
      throw e;
    }
  }

  async function loadAnalytics() {
    analyticsLoading.value = true;
    try {
      const { data } = await analyticsService.getLinks();
      analytics.value = data.data ?? null;
    } catch (e) {
      analyticsErrorMessage.value = getErrorMessage(e, 'Failed to load analytics');
      analytics.value = null;
    } finally {
      analyticsLoading.value = false;
    }
  }

  return {
    profile,
    links,
    analytics,
    profileLoading,
    linksLoading,
    analyticsLoading,
    profileSuccessMessage,
    profileErrorMessage,
    linksSuccessMessage,
    linksErrorMessage,
    analyticsErrorMessage,
    clearMessages,
    clearProfileMessages,
    clearLinksMessages,
    loadProfile,
    saveProfile,
    loadLinks,
    createLink,
    updateLink,
    deleteLink,
    reorderLinks,
    loadAnalytics,
  };
});
