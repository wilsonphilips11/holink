<template>
  <section class="card">
    <h2>Analytics</h2>

    <div v-if="loading" class="alert alert-info">Loading analytics...</div>

    <template v-else-if="analytics">
      <div class="stats-grid">
        <div class="stat">
          <span class="stat-value">{{ analytics.totalProfileViews }}</span>
          <span class="stat-label">Profile views</span>
        </div>
        <div class="stat">
          <span class="stat-value">{{ analytics.totalClicks }}</span>
          <span class="stat-label">Total clicks</span>
        </div>
        <div v-if="analytics.topLink" class="stat">
          <span class="stat-value">{{ analytics.topLink.clicks }}</span>
          <span class="stat-label">Top link: {{ analytics.topLink.title }}</span>
        </div>
      </div>

      <div v-if="analytics.links.length === 0" class="empty-state">No click data yet.</div>

      <ul v-else class="analytics-list">
        <li v-for="item in analytics.links" :key="item.linkId">
          <span class="title" :title="item.title">
            {{ item.title }}
          </span>
          <span class="clicks">{{ item.clicks }} clicks</span>
        </li>
      </ul>

      <div v-if="analytics.clicksByDay.length" class="daily-chart">
        <h3>Clicks by day (last 30 days)</h3>
        <div class="bars">
          <div v-for="day in analytics.clicksByDay" :key="day.date" class="bar-item">
            <div
              class="bar"
              :style="{ height: barHeight(day.clicks) + '%' }"
              :title="`${day.date}: ${day.clicks} clicks`"
            />
            <span class="bar-label">{{ formatDate(day.date) }}</span>
          </div>
        </div>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import type { LinkAnalytics } from "@/types";
import { computed } from "vue";

const props = defineProps<{
  analytics: LinkAnalytics | null;
  loading: boolean;
}>();

const maxClicks = computed(() => {
  if (!props.analytics?.clicksByDay.length) return 1;
  return Math.max(...props.analytics.clicksByDay.map((d) => d.clicks), 1);
});

function barHeight(clicks: number): number {
  return (clicks / maxClicks.value) * 100;
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr);
  return date.toLocaleDateString(undefined, { month: "short", day: "numeric" });
}
</script>

<style scoped>
h2,
h3 {
  margin: 0 0 1rem;
  font-size: 1.25rem;
}

h3 {
  font-size: 1rem;
  margin-top: 1.5rem;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat {
  background: var(--color-bg);
  border-radius: 8px;
  padding: 1rem;
  text-align: center;
}

.stat-value {
  display: block;
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--color-primary);
}

.stat-label {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.analytics-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.analytics-list li {
  display: flex;
  justify-content: space-between;
  padding: 0.625rem 0;
  border-bottom: 1px solid var(--color-border);
  align-items: center;
  gap: 1rem;
}

.clicks {
  color: var(--color-text-muted);
  font-size: 0.875rem;
  flex-shrink: 0;
  white-space: nowrap;
}

.bars {
  display: flex;
  align-items: flex-end;
  gap: 4px;
  height: 120px;
  overflow-x: auto;
}

.bar-item {
  flex: 1;
  min-width: 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.bar {
  width: 100%;
  max-width: 24px;
  background: var(--color-primary);
  border-radius: 4px 4px 0 0;
  min-height: 4px;
  margin-top: auto;
}

.bar-label {
  font-size: 0.625rem;
  color: var(--color-text-muted);
  margin-top: 4px;
}

.title {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
