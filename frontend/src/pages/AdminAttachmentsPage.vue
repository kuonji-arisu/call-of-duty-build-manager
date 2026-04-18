<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useDialog, useMessage } from "naive-ui";

import AttachmentCard from "../components/attachments/AttachmentCard.vue";
import AttachmentEditorModal from "../components/attachments/AttachmentEditorModal.vue";
import WeaponRemoteSelect from "../components/weapons/WeaponRemoteSelect.vue";
import EmptyState from "../components/common/EmptyState.vue";
import PaginationFooter from "../components/common/PaginationFooter.vue";
import ResultSummary from "../components/common/ResultSummary.vue";
import { adminAttachmentsApi } from "../api/admin/attachments";
import { useDeleteConfirm } from "../composables/useDeleteConfirm";
import { useDirtyForm } from "../composables/useDirtyForm";
import { usePagedRemoteList } from "../composables/usePagedRemoteList";
import { debounce } from "../shared/utils/debounce";
import { toSelectOptions } from "../shared/utils/naive";
import {
  getAttachmentTagOptions,
  getGenerationOptions,
  getSlotOptions,
} from "../shared/utils/labels";
import type {
  Attachment,
  AttachmentTag,
  GenerationFilter,
  Slot,
  WeaponOption,
} from "../shared/types";
import {
  cloneAttachmentToForm,
  createEmptyAttachmentForm,
  createAttachmentFormSnapshot,
  type AttachmentFormState,
} from "../shared/attachments/attachmentForm";
import { buildAttachmentSavePayload } from "../shared/attachments/attachmentPayload";

type SlotFilter = Slot | "ALL";
type TagFilter = AttachmentTag | "ALL";

const dialog = useDialog();
const message = useMessage();
const saving = ref(false);
const deletingId = ref("");
const errorMessage = ref("");
const selectedFilterWeapon = ref<WeaponOption | null>(null);
const filters = reactive({
  keyword: "",
  slot: "ALL" as SlotFilter,
  generation: "ALL" as GenerationFilter,
  tag: "ALL" as TagFilter,
  weaponId: "",
});
const setErrorMessage = (value: string) => {
  errorMessage.value = value;
};
const {
  items,
  total,
  page,
  pageSize,
  loading,
  load: loadAttachments,
  resetAndLoad,
  refreshAfterDelete,
} = usePagedRemoteList<Attachment>({
  fetcher: (currentPage, currentPageSize) =>
    adminAttachmentsApi.listAttachments({
      page: currentPage,
      pageSize: currentPageSize,
      keyword: filters.keyword,
      slot: filters.slot,
      generation: filters.generation,
      tag: filters.tag,
      weaponId: filters.weaponId,
    }),
  onError: setErrorMessage,
});

const generationSelectOptions = computed(() => toSelectOptions(getGenerationOptions()));
const attachmentTagSelectOptions = computed(() => toSelectOptions(getAttachmentTagOptions()));
const slotOptions = computed(() => toSelectOptions(getSlotOptions()));
const adminPageSizes = [10, 20, 50, 100];

const form = reactive<AttachmentFormState>(createEmptyAttachmentForm());
const editorVisible = ref(false);
const editorTitle = computed(() => (form.id ? "编辑配件" : "新建配件"));
const { isDirty: hasUnsavedChanges, captureSnapshot } = useDirtyForm(
  form,
  editorVisible,
  createAttachmentFormSnapshot,
);

function resetForm(nextForm: AttachmentFormState) {
  Object.assign(form, nextForm);
  form.generations = [...nextForm.generations];
  form.tags = [...nextForm.tags];
  form.effects = nextForm.effects.map((effect) => ({ ...effect }));
  captureSnapshot();
  errorMessage.value = "";
}

function openCreate() {
  resetForm(createEmptyAttachmentForm());
  editorVisible.value = true;
}

function openEdit(attachment: Attachment) {
  resetForm(cloneAttachmentToForm(attachment));
  editorVisible.value = true;
}

function closeEditor() {
  editorVisible.value = false;
  resetForm(createEmptyAttachmentForm());
}

function requestCloseEditor() {
  if (saving.value) {
    return;
  }
  if (!hasUnsavedChanges.value) {
    closeEditor();
    return;
  }

  dialog.warning({
    title: "放弃未保存修改？",
    content: "关闭后，当前表单里的修改不会保留。",
    positiveText: "放弃修改",
    negativeText: "继续编辑",
    onPositiveClick: closeEditor,
  });
}

const debouncedResetAndLoad = debounce(resetAndLoad, 300);

watch(() => filters.keyword, debouncedResetAndLoad);
watch(() => [filters.slot, filters.generation, filters.tag, filters.weaponId], resetAndLoad);
watch(page, () => {
  void loadAttachments();
});
watch(pageSize, resetAndLoad);

onMounted(() => {
  void loadAttachments();
});

async function saveAttachment() {
  saving.value = true;
  errorMessage.value = "";

  try {
    await adminAttachmentsApi.saveAttachment(form.id, buildAttachmentSavePayload(form));
    await loadAttachments();
    message.success("配件已保存。");
    closeEditor();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : String(error);
  } finally {
    saving.value = false;
  }
}

const { confirmDelete } = useDeleteConfirm<Attachment>({
  dialog,
  message,
  deletingId,
  getId: (attachment) => attachment.id,
  title: "删除配件？",
  content: (attachment) => `确认删除 ${attachment.name} 吗？如果仍被推荐配装使用，后端会拒绝删除。`,
  successText: "配件已删除。",
  deleteAction: async (attachment) => {
    await adminAttachmentsApi.deleteAttachment(attachment.id);
    await refreshAfterDelete();
  },
  onError: setErrorMessage,
});
</script>

<template>
  <section class="space-y-6">
    <n-card :bordered="false" class="hero-panel glass-panel card-xl">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div class="space-y-2">
          <p class="eyebrow-text">Admin / Attachments</p>
          <h1 class="page-title">配件管理</h1>
          <p class="max-w-2xl text-sm leading-7 text-slate-300">
            维护配件本体、代际、标签和属性效果。适用武器请在绑定维护页批量管理。
          </p>
        </div>
        <n-button round type="primary" @click="openCreate">新建配件</n-button>
      </div>
    </n-card>

    <n-card :bordered="false" class="glass-panel card-xl">
      <div class="space-y-5">
        <n-form label-placement="top">
          <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-5">
            <n-form-item label="搜索">
              <n-input v-model:value="filters.keyword" clearable placeholder="输入配件名称" />
            </n-form-item>
            <n-form-item label="槽位">
              <n-select v-model:value="filters.slot" :options="[{ label: '全部槽位', value: 'ALL' }, ...slotOptions]" />
            </n-form-item>
            <n-form-item label="代际">
              <n-select v-model:value="filters.generation"
                :options="[{ label: '全部代际', value: 'ALL' }, ...generationSelectOptions]" />
            </n-form-item>
            <n-form-item label="标签">
              <n-select v-model:value="filters.tag"
                :options="[{ label: '全部标签', value: 'ALL' }, ...attachmentTagSelectOptions]" />
            </n-form-item>
            <n-form-item label="适用武器">
              <WeaponRemoteSelect v-model:value="filters.weaponId" v-model:selected-weapon="selectedFilterWeapon"
                placeholder="全部武器" />
            </n-form-item>
          </div>
        </n-form>

        <n-alert v-if="errorMessage" :bordered="false" type="error">
          {{ errorMessage }}
        </n-alert>

        <ResultSummary :loading="loading" :page="page" :page-size="pageSize" :total="total" label="配件" />

        <div v-if="items.length" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5">
          <AttachmentCard
            v-for="attachment in items"
            :key="attachment.id"
            :attachment="attachment"
          >
            <template #actions>
              <n-button quaternary round size="small" @click="openEdit(attachment)">编辑</n-button>
              <n-button
                :loading="deletingId === attachment.id"
                quaternary
                round
                size="small"
                type="error"
                @click="confirmDelete(attachment)"
              >
                删除
              </n-button>
            </template>
          </AttachmentCard>
        </div>

        <EmptyState v-if="!loading && !items.length" description="当前筛选条件下没有配件。" title="没有配件" />

        <PaginationFooter v-model:page="page" v-model:page-size="pageSize" :disabled="loading"
          :page-sizes="adminPageSizes" :total="total" />
      </div>
    </n-card>

    <AttachmentEditorModal
      :form="form"
      :show="editorVisible"
      :title="editorTitle"
      :saving="saving"
      :error-message="errorMessage"
      @close="requestCloseEditor"
      @save="saveAttachment"
    />
  </section>
</template>
