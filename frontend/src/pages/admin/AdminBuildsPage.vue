<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { useDialog, useMessage } from "naive-ui";

import WeaponRemoteSelect from "../../components/weapons/WeaponRemoteSelect.vue";
import BuildCard from "../../components/builds/BuildCard.vue";
import BuildEditorModal from "../../components/builds/BuildEditorModal.vue";
import EmptyState from "../../components/common/EmptyState.vue";
import PaginationFooter from "../../components/common/PaginationFooter.vue";
import ResultSummary from "../../components/common/ResultSummary.vue";
import { adminBuildsApi } from "../../api/admin/builds";
import { publicWeaponsApi } from "../../api/public/weapons";
import {
  buildFormFromBuild,
  useBuildEditorSession,
} from "../../composables/builds/useBuildEditorSession";
import { useDeleteConfirm } from "../../composables/useDeleteConfirm";
import { useLatestRequest } from "../../composables/useLatestRequest";
import { usePagedRemoteList } from "../../composables/usePagedRemoteList";
import { buildRecommendedBuildSavePayload } from "../../shared/builds/buildItemPayload";
import { debounce } from "../../shared/utils/debounce";
import { toSelectOptions } from "../../shared/utils/naive";
import { getGenerationOptions } from "../../shared/utils/labels";
import { toWeaponOption } from "../../shared/weapons/weaponOption";
import type {
  AttachmentOption,
  BuildSummary,
  GenerationFilter,
  Slot,
  WeaponOption,
} from "../../shared/types";

type FavoriteFilter = "ALL" | "true" | "false";

const route = useRoute();
const dialog = useDialog();
const message = useMessage();
const selectedFilterWeapon = ref<WeaponOption | null>(null);
const preferredWeapon = ref<WeaponOption | null>(null);
const saving = ref(false);
const deletingId = ref("");
const errorMessage = ref("");
const preferredWeaponRequest = useLatestRequest();
const editRequest = useLatestRequest();
const filters = reactive({
  keyword: "",
  weaponId: "",
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
  load: loadBuilds,
  resetAndLoad,
  refreshAfterDelete,
} = usePagedRemoteList<BuildSummary>({
  fetcher: (currentPage, currentPageSize) =>
    adminBuildsApi.listBuilds({
      page: currentPage,
      pageSize: currentPageSize,
      keyword: filters.keyword,
      weaponId: filters.weaponId,
      generation: filters.generation,
      favorite: favoriteQueryValue(),
    }),
  onError: setErrorMessage,
});

const generationSelectOptions = computed(() => toSelectOptions(getGenerationOptions()));
const favoriteOptions = [
  { label: "全部", value: "ALL" },
  { label: "仅收藏", value: "true" },
  { label: "未收藏", value: "false" },
];
const adminPageSizes = [10, 20, 50, 100];

const {
  form,
  selectedFormWeapon,
  selectedAttachmentsBySlot,
  editorVisible,
  resetForm,
  openCreate: openBuildEditorCreate,
  closeEditor: closeBuildEditor,
  requestCloseEditor: requestCloseBuildEditor,
  handleFormWeaponUpdate,
  updateSlotAttachmentId,
  updateSlotAttachment,
  validateSelectedAttachments,
} = useBuildEditorSession({
  dialog,
  saving,
  errorMessage,
  closeWarningTitle: "放弃未保存修改？",
});
const editorTitle = computed(() => (form.id ? "编辑推荐配装" : "新建推荐配装"));

function favoriteQueryValue() {
  if (filters.favorite === "ALL") {
    return null;
  }
  return filters.favorite === "true";
}

function openCreate() {
  openBuildEditorCreate(preferredWeapon.value);
}

async function openEdit(build: BuildSummary) {
  const request = editRequest.next();
  errorMessage.value = "";
  try {
    const detail = await adminBuildsApi.getBuildDetail(build.id);
    const selectedItems = Object.fromEntries(
      detail.items.map((entry) => [entry.item.slot, entry.item.attachmentId]),
    ) as Partial<Record<Slot, string>>;
    const selectedAttachments = Object.fromEntries(
      detail.items.map((entry) => [
        entry.item.slot,
        {
          id: entry.attachment.id,
          name: entry.attachment.name,
          slot: entry.attachment.slot,
        } satisfies AttachmentOption,
      ]),
    ) as Partial<Record<Slot, AttachmentOption>>;
    if (!request.isLatest()) {
      return;
    }
    resetForm(buildFormFromBuild(detail.build, selectedItems), {
      weapon: toWeaponOption(detail.weapon),
      attachments: selectedAttachments,
      items: detail.items.map((entry) => entry.item),
      build: detail.build,
    });
    editorVisible.value = true;
  } catch (error) {
    if (request.isLatest()) {
      errorMessage.value = error instanceof Error ? error.message : String(error);
    }
  }
}

function closeEditor() {
  closeBuildEditor(preferredWeapon.value);
}

function requestCloseEditor() {
  requestCloseBuildEditor(preferredWeapon.value);
}

async function loadPreferredWeapon(value: unknown) {
  const request = preferredWeaponRequest.next();
  if (typeof value !== "string" || !value) {
    preferredWeapon.value = null;
    return;
  }

  try {
    const weapon = await publicWeaponsApi.getWeapon(value);
    if (request.isLatest()) {
      preferredWeapon.value = toWeaponOption(weapon);
    }
  } catch {
    if (request.isLatest()) {
      preferredWeapon.value = null;
    }
  }
}

const debouncedResetAndLoad = debounce(resetAndLoad, 300);

watch(() => filters.keyword, debouncedResetAndLoad);
watch(() => [filters.weaponId, filters.generation, filters.favorite], resetAndLoad);
watch(page, () => {
  void loadBuilds();
});
watch(pageSize, resetAndLoad);
watch(
  () => route.query.weaponId,
  (value) => {
    void loadPreferredWeapon(value);
  },
  { immediate: true },
);
onMounted(() => {
  void loadBuilds();
});

async function saveBuild() {
  saving.value = true;
  errorMessage.value = "";

  try {
    const weapon = selectedFormWeapon.value;
    if (!weapon) {
      throw new Error("请重新搜索并选择所属武器");
    }
    const buildPayload = buildRecommendedBuildSavePayload(form, weapon);
    const weaponId = buildPayload.weaponId;
    if (!weapon || weapon.id !== weaponId) {
      throw new Error("请重新搜索并选择所属武器");
    }
    const invalidGeneration = buildPayload.generations.find((generation) => !weapon.generations.includes(generation));
    if (invalidGeneration) {
      throw new Error("配装代际必须属于所属武器");
    }
    validateSelectedAttachments(buildPayload.items);

    await adminBuildsApi.saveBuild(form.id, buildPayload);
    await loadBuilds();
    message.success("推荐配装已保存。");
    closeEditor();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : String(error);
  } finally {
    saving.value = false;
  }
}

const { confirmDelete } = useDeleteConfirm<BuildSummary>({
  dialog,
  message,
  deletingId,
  getId: (build) => build.id,
  title: "删除推荐配装？",
  content: (build) => `确认删除 ${build.name} 吗？`,
  successText: "推荐配装已删除。",
  deleteAction: async (build) => {
    await adminBuildsApi.deleteBuild(build.id);
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
          <p class="eyebrow-text">Admin / Recommended Builds</p>
          <h1 class="page-title">后台推荐配装</h1>
          <p class="max-w-2xl text-sm leading-7 text-slate-300">
            维护后台可持久化的推荐配装。
          </p>
        </div>
        <n-button round type="primary" @click="openCreate">新建配装</n-button>
      </div>
    </n-card>

    <n-card :bordered="false" class="glass-panel card-xl">
      <div class="space-y-5">
        <n-form label-placement="top">
          <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
            <n-form-item label="搜索">
              <n-input v-model:value="filters.keyword" clearable placeholder="输入配装名称" />
            </n-form-item>
            <n-form-item label="所属武器">
              <WeaponRemoteSelect v-model:value="filters.weaponId" v-model:selected-weapon="selectedFilterWeapon"
                placeholder="全部武器" />
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

        <ResultSummary :loading="loading" :page="page" :page-size="pageSize" :total="total" label="推荐配装" />

        <div v-if="items.length" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5">
          <BuildCard
            v-for="build in items"
            :key="build.id"
            :build="build"
            :item-count="build.itemCount"
            :show-notes="false"
            :weapon-name="build.weaponName"
          >
            <template #actions>
              <n-button quaternary round size="small" @click="openEdit(build)">编辑</n-button>
              <n-button
                :loading="deletingId === build.id"
                quaternary
                round
                size="small"
                type="error"
                @click="confirmDelete(build)"
              >
                删除
              </n-button>
            </template>
          </BuildCard>
        </div>

        <EmptyState v-if="!loading && !items.length" description="当前筛选条件下没有推荐配装。" title="暂无推荐配装" />

        <PaginationFooter v-model:page="page" v-model:page-size="pageSize" :disabled="loading"
          :page-sizes="adminPageSizes" :total="total" />
      </div>
    </n-card>

    <BuildEditorModal
      :show="editorVisible"
      :title="editorTitle"
      :form="form"
      :selected-weapon="selectedFormWeapon"
      :selected-attachments="selectedAttachmentsBySlot"
      :saving="saving"
      :error-message="errorMessage"
      :disable-weapon-change="Boolean(form.id)"
      weapon-locked-hint="已有推荐配装不在编辑时切换武器；请新建一条配装来维护另一把武器。"
      save-text="保存推荐配装"
      source="admin"
      @close="requestCloseEditor"
      @save="saveBuild"
      @weapon-update="handleFormWeaponUpdate"
      @slot-attachment-id-update="updateSlotAttachmentId"
      @slot-attachment-update="updateSlotAttachment"
    />
  </section>
</template>
