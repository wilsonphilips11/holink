export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  errors?: string[];
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface Profile {
  id: string;
  username: string;
  displayName: string;
  bio?: string;
  avatarUrl?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface Link {
  id: string;
  title: string;
  url: string;
  active: boolean;
  position: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface PublicProfile {
  profile: Profile;
  links: Link[];
}

export interface LinkAnalyticsItem {
  linkId: string;
  title: string;
  clicks: number;
}

export interface DailyClickItem {
  date: string;
  clicks: number;
}

export interface LinkAnalytics {
  totalClicks: number;
  totalProfileViews: number;
  links: LinkAnalyticsItem[];
  topLink: LinkAnalyticsItem | null;
  clicksByDay: DailyClickItem[];
}

export interface ProfileAnalytics {
  username: string;
  displayName: string;
  totalClicks: number;
  totalProfileViews: number;
  totalLinks: number;
  activeLinks: number;
}

export interface CreateProfilePayload {
  username: string;
  displayName: string;
  bio?: string;
  avatarUrl?: string;
}

export interface CreateLinkPayload {
  title: string;
  url: string;
  active?: boolean;
  position?: number;
}

export interface ClickResponse {
  redirectUrl: string;
}
