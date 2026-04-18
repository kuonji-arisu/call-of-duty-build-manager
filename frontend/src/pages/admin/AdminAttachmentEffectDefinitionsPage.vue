<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useDialog, useMessage } from "naive-ui";

import EmptyState from "../../components/common/EmptyState.vue";
import PaginationFooter from "../../components/common/PaginationFooter.vue";
import ResultSummary from "../../components/common/ResultSummary.vue";
import { adminEffectDefinitionsApi } from "../../api/admin/effectDefinitions";
import { useDeleteConfirm } from "../../composables/useDeleteConfirm";
import { usePagedRemoteList } from "../../composables/usePagedRemoteList";
import { debounce } from "../../shared/utils/debounce";
import { requireText } from "../../shared/utils/validation";
import type { AttachmentEffectDefinition, AttachmentEffectDefinitionSavePayload } from "../../shared/types";

interface DefinitionFormState {
  id: string;
  label: string;
  sortOrder: number;
}

const dialog = useDialog();
const message = useMessage();
const saving = ref(false);
const deletingId = ref("");
const editorVisible = ref(false);
const formSnapshot = ref("");
const errorMessage = ref("");
const filters = reactive({
  keyword: "",
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
  load: loadDefinitions,
  resetAndLoad,
  refreshAfterDelete,
} = usePagedRemoteList<AttachmentEffectDefinition>({
  fetcher: (currentPage, currentPageSize) =>
    adminEffectDefinitionsApi.listAttachmentEffectDefinitions({
      page: currentPage,
      pageSize: currentPageSize,
      keyword: filters.keyword,
    }),
  onError: setErrorMessage,
});
const adminPageSizes = [10, 20, 50, 100];

function createEmptyForm(): DefinitionFormState {
  return {
    id: "",
    label: "",
    sortOrder: 0,
  };
}

const form = reactive<DefinitionFormState>(createEmptyForm());
const editorTitle = computed(() => (form.id ? "编辑属性词条" : "新建属性词条"));
const hasUnsavedChanges = computed(() => editorVisible.value && JSON.stringify(form) !== formSnapshot.value);

function resetForm(nextForm: DefinitionFormState) {
  Object.assign(form, nextForm);
  formSnapshot.value = JSON.stringify(form);
  errorMessage.value = "";
}

function openCreate() {
  resetForm(createEmptyForm());
  editorVisible.value = true;
}

function openEdit(definition: AttachmentEffectDefinition) {
  resetForm({
    id: definition.id,
    label: definition.label,
    sortOrder: definition.sortOrder,
  });
  editorVisible.value = true;
}

function closeEditor() {
  editorVisible.value = false;
  resetForm(createEmptyForm());
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
watch(page, () => {
  void loadDefinitions();
});
watch(pageSize, resetAndLoad);

onMounted(() => {
  void loadDefinitions();
});

async function saveDefinition() {
  saving.value = true;
  errorMessage.value = "";

  try {
    const payload: AttachmentEffectDefinitionSavePayload = {
      label: requireText(form.label, "请输入属性词条名称"),
      sortOrder: Number.isNaN(form.sortOrder) ? 0 : form.sortOrder,
    };

    await adminEffectDefinitionsApi.saveAttachmentEffectDefinition(form.id, payload);
    await loadDefinitions();
    message.success("属性词条已保存。");
    closeEditor();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : String(error);
  } finally {
    saving.value = false;
  }
}

const { confirmDelete } = useDeleteConfirm<AttachmentEffectDefinition>({
  dialog,
  message,
  deletingId,
  getId: (definition) => definition.id,
  title: "删除属性词条？",
  content: (definition) => `确认删除 ${definition.label} 吗？如果仍被配件效果引用，后端会拒绝删除。`,
  successText: "属性词条已删除。",
  deleteAction: async (definition) => {
    await adminEffectDefinitionsApi.deleteAttachmentEffectDefinition(definition.id);
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
          <p class="eyebrow-text">Admin / Effect Definitions</p>
          <h1 class="page-title">属性词条</h1>
          <p class="max-w-2xl text-sm leading-7 text-slate-300">
            维护配件效果会引用的属性词条名称。
          </p>
        </div>
        <n-button round type="primary" @click="openCreate">新建词条</n-button>
      </div>
    </n-card>

    <n-card :bordered="false" class="glass-panel card-xl">
      <div class="space-y-5">
        <n-form label-placement="top">
          <n-form-item label="搜索">
            <n-input v-model:value="filters.keyword" clearable placeholder="输入词条名称" />
          </n-form-item>
        </n-form>

        <n-alert v-if="errorMessage" :bordered="false" type="error">
          {{ errorMessage }}
        </n-alert>

        <ResultSummary :loading="loading" :page="page" :page-size="pageSize" :total="total" label="词条" />

        <div v-if="items.length" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5">
          <article
            v-for="definition in items"
            :key="definition.id"
            class="min-h-36 w-full rounded-2xl border border-white/8 bg-white/4 p-5 transition hover:bg-white/8"
          >
            <div class="flex h-full flex-col gap-4">
              <div class="space-y-1">
                <p class="eyebrow-text">Effect</p>
                <h3 class="text-base font-semibold text-slate-50">{{ definition.label }}</h3>
              </div>

              <div class="mt-auto flex items-center justify-between gap-3 pt-2">
                <span class="text-xs text-slate-500">排序 {{ definition.sortOrder }}</span>
                <div class="flex gap-2">
                  <n-button quaternary round size="small" @click="openEdit(definition)">编辑</n-button>
                  <n-button
                    :loading="deletingId === definition.id"
                    quaternary
                    round
                    size="small"
                    type="error"
                    @click="confirmDelete(definition)"
                  >
                    删除
                  </n-button>
                </div>
              </div>
            </div>
          </article>
        </div>

        <EmptyState v-if="!loading && !items.length" description="当前筛选条件下没有属性词条。" title="暂无属性词条" />

        <PaginationFooter v-model:page="page" v-model:page-size="pageSize" :disabled="loading"
          :page-sizes="adminPageSizes" :total="total" />
      </div>
    </n-card>

    <n-modal :show="editorVisible" :mask-closable="false" preset="card" :title="editorTitle" class="max-w-2xl"
      @close="requestCloseEditor">
      <div class="space-y-5">
        <n-alert v-if="errorMessage" :bordered="false" type="error">
          {{ errorMessage }}
        </n-alert>

        <n-form label-placement="top">
          <div class="grid gap-4 md:grid-cols-2">
            <n-form-item class="md:col-span-2" label="词条名称">
              <n-input v-model:value="form.label" placeholder="例如 开镜速度 / 后坐力控制 / 子弹初速" />
            </n-form-item>
            <n-form-item label="排序">
              <n-input-number :min="0" :value="form.sortOrder" class="w-full"
                @update:value="form.sortOrder = $event ?? 0" />
            </n-form-item>
          </div>
        </n-form>

        <div class="flex justify-end gap-3">
          <n-button round :disabled="saving" @click="requestCloseEditor">取消</n-button>
          <n-button round type="primary" :loading="saving" @click="saveDefinition">保存词条</n-button>
        </div>
      </div>
    </n-modal>
  </section>
</template>
