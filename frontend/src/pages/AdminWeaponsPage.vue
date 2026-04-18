<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useDialog, useMessage } from "naive-ui";

import EmptyState from "../components/common/EmptyState.vue";
import PaginationFooter from "../components/common/PaginationFooter.vue";
import ResultSummary from "../components/common/ResultSummary.vue";
import WeaponCard from "../components/weapons/WeaponCard.vue";
import WeaponEditorModal from "../components/weapons/WeaponEditorModal.vue";
import { adminWeaponsApi } from "../api/admin/weapons";
import { useDeleteConfirm } from "../composables/useDeleteConfirm";
import { useDirtyForm } from "../composables/useDirtyForm";
import { usePagedRemoteList } from "../composables/usePagedRemoteList";
import { debounce } from "../shared/utils/debounce";
import { toSelectOptions } from "../shared/utils/naive";
import {
  getGenerationOptions,
  getWeaponTypeOptions,
} from "../shared/utils/labels";
import type { GenerationFilter, Weapon, WeaponType } from "../shared/types";
import {
  cloneWeaponToForm,
  createEmptyWeaponForm,
  createWeaponFormSnapshot,
  type WeaponFormState,
} from "../shared/weapons/weaponForm";
import { buildWeaponSavePayload } from "../shared/weapons/weaponPayload";

type WeaponTypeFilter = WeaponType | "ALL";
type FavoriteFilter = "ALL" | "true" | "false";

const dialog = useDialog();
const message = useMessage();
const saving = ref(false);
const deletingId = ref("");
const errorMessage = ref("");
const filters = reactive({
  keyword: "",
  weaponType: "ALL" as WeaponTypeFilter,
  generation: "ALL" as GenerationFilter,
  favorite: "ALL" as FavoriteFilter,
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
  load: loadWeapons,
  resetAndLoad,
  refreshAfterDelete,
} = usePagedRemoteList<Weapon>({
  fetcher: (currentPage, currentPageSize) =>
    adminWeaponsApi.listWeapons({
      page: currentPage,
      pageSize: currentPageSize,
      keyword: filters.keyword,
      weaponType: filters.weaponType,
      generation: filters.generation,
      favorite: favoriteQueryValue(),
    }),
  onError: setErrorMessage,
});

const generationSelectOptions = computed(() => toSelectOptions(getGenerationOptions()));
const weaponTypeOptions = computed(() => toSelectOptions(getWeaponTypeOptions()));
const favoriteOptions = [
  { label: "全部", value: "ALL" },
  { label: "仅收藏", value: "true" },
  { label: "未收藏", value: "false" },
];
const adminPageSizes = [10, 20, 50, 100];

const form = reactive<WeaponFormState>(createEmptyWeaponForm());
const editorVisible = ref(false);
const editorTitle = computed(() => (form.id ? "编辑武器" : "新建武器"));
const { isDirty: hasUnsavedChanges, captureSnapshot } = useDirtyForm(
  form,
  editorVisible,
  createWeaponFormSnapshot,
);

function favoriteQueryValue() {
  if (filters.favorite === "ALL") {
    return null;
  }
  return filters.favorite === "true";
}

function resetForm(nextForm: WeaponFormState) {
  Object.assign(form, nextForm);
  form.tags = [...nextForm.tags];
  form.generations = [...nextForm.generations];
  form.slots = [...nextForm.slots];
  captureSnapshot();
  errorMessage.value = "";
}

function openCreate() {
  resetForm(createEmptyWeaponForm());
  editorVisible.value = true;
}

function openEdit(weapon: Weapon) {
  resetForm(cloneWeaponToForm(weapon));
  editorVisible.value = true;
}

function closeEditor() {
  editorVisible.value = false;
  resetForm(createEmptyWeaponForm());
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
watch(() => [filters.weaponType, filters.generation, filters.favorite], resetAndLoad);
watch(page, () => {
  void loadWeapons();
});
watch(pageSize, resetAndLoad);

onMounted(() => {
  void loadWeapons();
});

async function saveWeapon() {
  saving.value = true;
  errorMessage.value = "";

  try {
    await adminWeaponsApi.saveWeapon(form.id, buildWeaponSavePayload(form));
    await loadWeapons();
    message.success("武器已保存。");
    closeEditor();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : String(error);
  } finally {
    saving.value = false;
  }
}

const { confirmDelete } = useDeleteConfirm<Weapon>({
  dialog,
  message,
  deletingId,
  getId: (weapon) => weapon.id,
  title: "删除武器？",
  content: (weapon) => `确认删除 ${weapon.name} 吗？如果仍被配装或配件绑定引用，后端会拒绝删除。`,
  successText: "武器已删除。",
  deleteAction: async (weapon) => {
    await adminWeaponsApi.deleteWeapon(weapon.id);
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
          <p class="eyebrow-text">Admin / Weapons</p>
          <h1 class="page-title">武器管理</h1>
          <p class="max-w-2xl text-sm leading-7 text-slate-300">
            维护武器基础信息、槽位、代际和收藏状态。
          </p>
        </div>
        <n-button round type="primary" @click="openCreate">新建武器</n-button>
      </div>
    </n-card>

    <n-card :bordered="false" class="glass-panel card-xl">
      <div class="space-y-5">
        <n-form label-placement="top">
          <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
            <n-form-item label="搜索">
              <n-input v-model:value="filters.keyword" clearable placeholder="输入武器名称" />
            </n-form-item>
            <n-form-item label="分类">
              <n-select v-model:value="filters.weaponType"
                :options="[{ label: '全部分类', value: 'ALL' }, ...weaponTypeOptions]" />
            </n-form-item>
            <n-form-item label="代际">
              <n-select v-model:value="filters.generation"
                :options="[{ label: '全部代际', value: 'ALL' }, ...generationSelectOptions]" />
            </n-form-item>
            <n-form-item label="收藏">
              <n-select v-model:value="filters.favorite" :options="favoriteOptions" />
            </n-form-item>
          </div>
        </n-form>

        <n-alert v-if="errorMessage" :bordered="false" type="error">
          {{ errorMessage }}
        </n-alert>

        <ResultSummary :loading="loading" :page="page" :page-size="pageSize" :total="total" label="武器" />

        <div v-if="items.length" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5">
          <WeaponCard
            v-for="weapon in items"
            :key="weapon.id"
            :weapon="weapon"
          >
            <template #actions>
              <n-button quaternary round size="small" @click="openEdit(weapon)">编辑</n-button>
              <n-button
                :loading="deletingId === weapon.id"
                quaternary
                round
                size="small"
                type="error"
                @click="confirmDelete(weapon)"
              >
                删除
              </n-button>
            </template>
          </WeaponCard>
        </div>

        <EmptyState v-if="!loading && !items.length" description="当前筛选条件下没有武器。" title="没有武器" />

        <PaginationFooter v-model:page="page" v-model:page-size="pageSize" :disabled="loading"
          :page-sizes="adminPageSizes" :total="total" />
      </div>
    </n-card>

    <WeaponEditorModal
      :form="form"
      :show="editorVisible"
      :title="editorTitle"
      :saving="saving"
      :error-message="errorMessage"
      @close="requestCloseEditor"
      @save="saveWeapon"
    />
  </section>
</template>
