import api from './api';
import type {
  ApiResponse,
  AuthResponse,
  ClickResponse,
  CreateLinkPayload,
  CreateProfilePayload,
  Link,
  LinkAnalytics,
  Profile,
  ProfileAnalytics,
  PublicProfile,
} from '@/types';

export const authService = {
  register(email: string, password: string) {
    return api.post<ApiResponse<AuthResponse>>('/auth/register', { email, password });
  },
  login(email: string, password: string) {
    return api.post<ApiResponse<AuthResponse>>('/auth/login', { email, password });
  },
};

export const profileService = {
  create(payload: CreateProfilePayload) {
    return api.post<ApiResponse<Profile>>('/profiles', payload);
  },
  update(id: string, payload: CreateProfilePayload) {
    return api.put<ApiResponse<Profile>>(`/profiles/${id}`, payload);
  },
  getMine() {
    return api.get<ApiResponse<Profile>>('/profiles/me');
  },
  getPublic(username: string) {
    return api.get<ApiResponse<PublicProfile>>(`/public/${username}`);
  },
};

export const linkService = {
  list() {
    return api.get<ApiResponse<Link[]>>('/links');
  },
  create(payload: CreateLinkPayload) {
    return api.post<ApiResponse<Link>>('/links', payload);
  },
  update(id: string, payload: CreateLinkPayload) {
    return api.put<ApiResponse<Link>>(`/links/${id}`, payload);
  },
  remove(id: string) {
    return api.delete<ApiResponse<void>>(`/links/${id}`);
  },
  reorder(linkIds: string[]) {
    return api.put<ApiResponse<Link[]>>('/links/reorder', { linkIds });
  },
  trackClick(id: string, utm?: { utmSource?: string; utmMedium?: string; utmCampaign?: string }) {
    return api.post<ApiResponse<ClickResponse>>(`/links/${id}/click`, utm || {});
  },
};

export const analyticsService = {
  getLinks() {
    return api.get<ApiResponse<LinkAnalytics>>('/analytics/links');
  },
  getProfile() {
    return api.get<ApiResponse<ProfileAnalytics>>('/analytics/profile');
  },
};
