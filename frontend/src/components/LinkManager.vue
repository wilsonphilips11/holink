<template>
  <section class="card">
    <div class="header">
      <h2>Links</h2>
      <button class="btn btn-primary btn-sm" @click="showForm = !showForm">
        {{ showForm ? "Cancel" : "Add link" }}
      </button>
    </div>

    <div v-if="loading && localLinks.length === 0" class="alert alert-info">Loading links...</div>
    <div v-if="error || localError" class="alert alert-error">{{ error || localError }}</div>
    <div v-if="success" class="alert alert-success">{{ success }}</div>

    <form v-if="showForm" class="link-form" @submit.prevent="handleCreate">
      <div class="form-group">
        <label>Title</label>
        <input v-model="newLink.title" class="form-input" required maxlength="200" />
      </div>
      <div class="form-group">
        <label>URL</label>
        <input v-model="newLink.url" class="form-input" required placeholder="https://..." />
      </div>
      <label class="checkbox">
        <input v-model="newLink.active" type="checkbox" />
        Active
      </label>
      <button type="submit" class="btn btn-primary" :disabled="submitting">Create link</button>
    </form>

    <div v-if="!loading && localLinks.length === 0" class="empty-state">No links yet. Add your first link above.</div>

    <draggable
      v-else-if="localLinks.length > 0"
      v-model="localLinks"
      item-key="id"
      handle=".drag-handle"
      @end="onDragEnd"
    >
      <template #item="{ element }">
        <div class="link-item" :class="{ inactive: !element.active }">
          <span class="drag-handle" aria-label="Drag to reorder" title="Drag to reorder">::</span>
          <div class="link-content">
            <div class="link-title-row">
              <strong>{{ element.title }}</strong>
              <span class="status-pill" :class="{ inactive: !element.active }">
                {{ element.active ? "Active" : "Inactive" }}
              </span>
            </div>
            <span class="url">{{ element.url }}</span>
          </div>
          <div class="link-actions">
            <button class="btn btn-secondary btn-sm" @click="startEdit(element)">Edit</button>
            <button class="btn btn-danger btn-sm" @click="requestDelete(element.id)">Delete</button>
          </div>
        </div>
      </template>
    </draggable>

    <div v-if="editingLink" class="modal-overlay" @click.self="editingLink = null">
      <div class="modal">
        <h3>Edit link</h3>
        <div class="form-group">
          <label>Title</label>
          <input v-model="editForm.title" class="form-input" />
        </div>
        <div class="form-group">
          <label>URL</label>
          <input v-model="editForm.url" class="form-input" />
        </div>
        <label class="checkbox">
          <input v-model="editForm.active" type="checkbox" />
          Active
        </label>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="editingLink = null">Cancel</button>
          <button class="btn btn-primary" :disabled="submitting" @click="handleUpdate">Save</button>
        </div>
      </div>
    </div>

    <ConfirmModal
      :visible="deleteId !== null"
      title="Are you sure?"
      message="This link will be removed from your profile."
      @confirm="confirmDelete"
      @cancel="deleteId = null"
    />
  </section>
</template>

<script setup lang="ts">
import type { Link } from "@/types";
import { isFreeOfHtml, isSafeHttpUrl } from "@/utils/validation";
import { ref, watch } from "vue";
import draggable from "vuedraggable";
import ConfirmModal from "./ConfirmModal.vue";

const props = defineProps<{
  links: Link[];
  loading: boolean;
  success: string | null;
  error: string | null;
  createLink: (payload: { title: string; url: string; active: boolean }) => Promise<void>;
  updateLink: (id: string, payload: { title: string; url: string; active: boolean }) => Promise<void>;
  deleteLink: (id: string) => Promise<void>;
  reorderLinks: (linkIds: string[]) => Promise<void>;
}>();

const localLinks = ref<Link[]>([]);
const showForm = ref(false);
const submitting = ref(false);
const localError = ref<string | null>(null);
const deleteId = ref<string | null>(null);
const editingLink = ref<Link | null>(null);
const editForm = ref({ title: "", url: "", active: true });
const newLink = ref({ title: "", url: "", active: true });

watch(
  () => props.links,
  (links) => {
    localLinks.value = [...links];
  },
  { immediate: true, deep: true },
);

function validateLink(title: string, url: string): string | null {
  if (!title.trim()) return "Title is required";
  if (!isFreeOfHtml(title)) return "Title contains invalid content";
  if (!isSafeHttpUrl(url)) return "Enter a valid http or https URL";
  return null;
}

async function handleCreate() {
  localError.value = null;
  const validationError = validateLink(newLink.value.title, newLink.value.url);
  if (validationError) {
    localError.value = validationError;
    return;
  }
  submitting.value = true;
  try {
    await props.createLink({
      title: newLink.value.title.trim(),
      url: newLink.value.url.trim(),
      active: newLink.value.active,
    });
    newLink.value = { title: "", url: "", active: true };
    showForm.value = false;
  } catch {
    localError.value = props.error || "Failed to create link";
  } finally {
    submitting.value = false;
  }
}

function startEdit(link: Link) {
  editingLink.value = link;
  editForm.value = { title: link.title, url: link.url, active: link.active };
}

async function handleUpdate() {
  if (!editingLink.value) return;
  localError.value = null;
  const validationError = validateLink(editForm.value.title, editForm.value.url);
  if (validationError) {
    localError.value = validationError;
    return;
  }
  submitting.value = true;
  try {
    await props.updateLink(editingLink.value.id, {
      title: editForm.value.title.trim(),
      url: editForm.value.url.trim(),
      active: editForm.value.active,
    });
    editingLink.value = null;
  } catch {
    localError.value = props.error || "Failed to update link";
  } finally {
    submitting.value = false;
  }
}

function requestDelete(id: string) {
  deleteId.value = id;
}

async function confirmDelete() {
  if (deleteId.value) {
    try {
      await props.deleteLink(deleteId.value);
      deleteId.value = null;
    } catch {
      localError.value = props.error || "Failed to delete link";
    }
  }
}

async function onDragEnd() {
  try {
    await props.reorderLinks(localLinks.value.map((l) => l.id));
  } catch {
    localError.value = props.error || "Failed to reorder links";
  }
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

h2,
h3 {
  margin: 0;
}

.btn-sm {
  padding: 0.375rem 0.75rem;
  font-size: 0.875rem;
}

.link-form {
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

.link-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.875rem;
  background: var(--color-bg);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  margin-bottom: 0.5rem;
}

.link-item.inactive {
  opacity: 0.6;
}

.drag-handle {
  cursor: grab;
  color: var(--color-text-muted);
  user-select: none;
}

.link-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.link-title-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 0;
}

.link-title-row strong {
  overflow-wrap: anywhere;
}

.status-pill {
  flex: 0 0 auto;
  padding: 0.125rem 0.5rem;
  border: 1px solid rgba(34, 197, 94, 0.6);
  border-radius: 999px;
  color: #86efac;
  font-size: 0.75rem;
  line-height: 1.4;
}

.status-pill.inactive {
  border-color: rgba(148, 163, 184, 0.6);
  color: var(--color-text-muted);
}

.url {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link-actions {
  display: flex;
  gap: 0.5rem;
}

.checkbox {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  font-size: 0.875rem;
}
</style>
