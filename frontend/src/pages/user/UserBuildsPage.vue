<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { storeToRefs } from "pinia";
import { useRoute } from "vue-router";
import { useDialog, useMessage } from "naive-ui";

import WeaponRemoteSelect from "../../components/weapons/WeaponRemoteSelect.vue";
import BuildCard from "../../components/builds/BuildCard.vue";
import BuildEditorModal from "../../components/builds/BuildEditorModal.vue";
import EmptyState from "../../components/common/EmptyState.vue";
import PaginationFooter from "../../components/common/PaginationFooter.vue";
import ResultSummary from "../../components/common/ResultSummary.vue";
import { publicAttachmentsApi } from "../../api/public/attachments";
import { publicWeaponsApi } from "../../api/public/weapons";
import { useBuildGenerationGuard } from "../../composables/builds/useBuildGenerationGuard";
import { useLatestRequest } from "../../composables/useLatestRequest";
import { useDirtyForm } from "../../composables/useDirtyForm";
import { createEmptyBuildEditorForm, type BuildEditorFormState } from "../../shared/buildEditor";
import { createBuildFormSnapshot } from "../../shared/builds/buildForm";
import { buildBuildItemPayload, buildBuildSavePayload } from "../../shared/builds/buildItemPayload";
import { toSelectOptions } from "../../shared/utils/naive";
import { getGenerationOptions } from "../../shared/utils/labels";
import type {
  AttachmentOption,
  Build,
  BuildItem,
  Generation,
  GenerationFilter,
  Slot,
  Weapon,
  WeaponOption,
} from "../../shared/types";
import { useBuildsStore } from "../../stores/builds";

type FavoriteFilter = "ALL" | "true" | "false";

const route = useRoute();
const dialog = useDialog();
const message = useMessage();
const buildsStore = useBuildsStore();
buildsStore.initialize();
const { builds } = storeToRefs(buildsStore);

const selectedFormWeapon = ref<WeaponOption | null>(null);
const selectedFilterWeapon = ref<WeaponOption | null>(null);
const selectedAttachmentsBySlot = ref<Partial<Record<Slot, AttachmentOption>>>({});
const preferredWeapon = ref<WeaponOption | null>(null);
const weaponCache = ref<Record<string, WeaponOption>>({});
const currentItems = ref<BuildItem[]>([]);
const editorVisible = ref(false);
const saving = ref(false);
const deletingId = ref("");
const loadingWeapons = ref(false);
const errorMessage = ref("");
const preferredWeaponRequest = useLatestRequest();
const editRequest = useLatestRequest();
const page = ref(1);
const pageSize = ref(10);
const filters = reactive({
  keyword: "",
  weaponId: "",
  generation: "ALL" as GenerationFilter,
  favorite: "ALL" as FavoriteFilter,
});

const generationSelectOptions = computed(() => toSelectOptions(getGenerationOptions()));
const favoriteOptions = [
  { label: "全部", value: "ALL" },
  { label: "仅收藏", value: "true" },
  { label: "未收藏", value: "false" },
];
const userPageSizes = [10, 20, 50, 100];

const form = reactive<BuildEditorFormState>(createEmptyBuildEditorForm());
const editorTitle = computed(() => (form.id ? "编辑本地配装" : "新建本地配装"));
const { isDirty: hasUnsavedChanges, captureSnapshot } = useDirtyForm(
  form,
  editorVisible,
  createBuildFormSnapshot,
);
const { captureGenerationSnapshot } = useBuildGenerationGuard({
  form,
  editorVisible,
  selectedAttachmentsBySlot,
  currentItems,
  dialog,
});
const selectedWeaponMap = computed(() => new Map(Object.entries(weaponCache.value)));
const itemCountByBuildId = computed(() => {
  const counts = new Map<string, number>();
  for (const item of buildsStore.buildItems) {
    counts.set(item.buildId, (counts.get(item.buildId) ?? 0) + 1);
  }
  return counts;
});
const filteredBuilds = computed(() => {
  const search = filters.keyword.trim().toLowerCase();

  return [...builds.value]
    .filter((build) => {
      const weaponName = selectedWeaponMap.value.get(build.weaponId)?.name ?? "";
      const matchesSearch =
        !search ||
        build.name.toLowerCase().includes(search) ||
        weaponName.toLowerCase().includes(search);
      const matchesWeapon = !filters.weaponId || build.weaponId === filters.weaponId;
      const matchesGeneration = filters.generation === "ALL" || build.generations.includes(filters.generation);
      const matchesFavorite =
        filters.favorite === "ALL" ||
        (filters.favorite === "true" ? build.isFavorite : !build.isFavorite);
      return matchesSearch && matchesWeapon && matchesGeneration && matchesFavorite;
    })
    .sort((left, right) => left.sortOrder - right.sortOrder || left.name.localeCompare(right.name, "zh-CN"));
});
const pagedBuilds = computed(() =>
  filteredBuilds.value.slice((page.value - 1) * pageSize.value, page.value * pageSize.value),
);
function toWeaponOption(weapon: Weapon): WeaponOption {
  return {
    id: weapon.id,
    name: weapon.name,
    weaponType: weapon.weaponType,
    generations: weapon.generations,
    slots: weapon.slots,
  };
}

function cacheWeapon(weapon: WeaponOption) {
  weaponCache.value = {
    ...weaponCache.value,
    [weapon.id]: weapon,
  };
}

async function ensureWeaponOptions(ids: string[]) {
  const missingIds = [...new Set(ids)].filter((id) => id && !weaponCache.value[id]);
  if (!missingIds.length) {
    return;
  }

  loadingWeapons.value = true;
  try {
    const weapons = await Promise.allSettled(missingIds.map((id) => publicWeaponsApi.getWeapon(id)));
    const nextCache = { ...weaponCache.value };
    for (const result of weapons) {
      if (result.status === "fulfilled") {
        const option = toWeaponOption(result.value);
        nextCache[option.id] = option;
      }
    }
    weaponCache.value = nextCache;
  } finally {
    loadingWeapons.value = false;
  }
}

async function loadAttachmentOptions(ids: string[]) {
  const normalizedIds = [...new Set(ids)].filter(Boolean);
  if (!normalizedIds.length) {
    return new Map<string, AttachmentOption>();
  }

  const options = await publicAttachmentsApi.listAttachmentOptionsByIds(normalizedIds);
  return new Map(options.map((option) => [option.id, option]));
}

function resetForm(
  nextForm: BuildEditorFormState,
  weapon: WeaponOption | null,
  attachments: Partial<Record<Slot, AttachmentOption>> = {},
  items: BuildItem[] = [],
) {
  Object.assign(form, nextForm);
  form.generations = [...nextForm.generations];
  form.items = { ...nextForm.items };
  selectedFormWeapon.value = weapon;
  selectedAttachmentsBySlot.value = { ...attachments };
  currentItems.value = [...items];
  captureSnapshot();
  captureGenerationSnapshot();
  errorMessage.value = "";
}

function createBuildFormForWeapon(weapon: WeaponOption | null) {
  const nextForm = createEmptyBuildEditorForm(weapon?.id ?? "");
  if (weapon) {
    nextForm.generations = defaultGenerationsForWeapon(weapon, nextForm.generations);
  }
  return nextForm;
}

function defaultGenerationsForWeapon(weapon: WeaponOption, currentGenerations: Generation[]) {
  const allowed = new Set(weapon.generations);
  const nextGenerations = currentGenerations.filter((generation) => allowed.has(generation));
  return (nextGenerations.length ? nextGenerations : weapon.generations.slice(0, 1)) as Generation[];
}

function normalizeFormGenerationsForWeapon(weapon: WeaponOption | null) {
  if (!weapon) {
    return;
  }
  form.generations = defaultGenerationsForWeapon(weapon, form.generations);
}

async function openCreate() {
  const weapon = selectedFilterWeapon.value ?? preferredWeapon.value;
  if (weapon) {
    cacheWeapon(weapon);
  }
  resetForm(createBuildFormForWeapon(weapon), weapon);
  editorVisible.value = true;
}

async function openEdit(build: Build) {
  const request = editRequest.next();
  errorMessage.value = "";
  try {
    await ensureWeaponOptions([build.weaponId]);
    const weapon = weaponCache.value[build.weaponId] ?? null;
    const items = buildsStore.getBuildItems(build.id);
    const optionsById = await loadAttachmentOptions(items.map((item) => item.attachmentId));
    const selectedItems = Object.fromEntries(
      items.map((item) => [item.slot, item.attachmentId]),
    ) as Partial<Record<Slot, string>>;
    const selectedAttachments = Object.fromEntries(
      items
        .map((item) => [item.slot, optionsById.get(item.attachmentId)] as const)
        .filter((entry): entry is readonly [Slot, AttachmentOption] => Boolean(entry[1])),
    ) as Partial<Record<Slot, AttachmentOption>>;

    if (!request.isLatest()) {
      return;
    }
    resetForm({
      ...build,
      notes: build.notes ?? "",
      generations: [...build.generations],
      items: selectedItems,
    }, weapon, selectedAttachments, items);
    editorVisible.value = true;
  } catch (error) {
    if (request.isLatest()) {
      errorMessage.value = error instanceof Error ? error.message : String(error);
    }
  }
}

function closeEditor() {
  editorVisible.value = false;
  resetForm(createBuildFormForWeapon(preferredWeapon.value), preferredWeapon.value);
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

function handleFormWeaponUpdate(weapon: WeaponOption | null) {
  selectedFormWeapon.value = weapon;
  normalizeFormGenerationsForWeapon(weapon);
  form.items = {};
  selectedAttachmentsBySlot.value = {};
  currentItems.value = [];
  if (weapon) {
    cacheWeapon(weapon);
  }
}

function handleFilterWeaponUpdate(weapon: WeaponOption | null) {
  selectedFilterWeapon.value = weapon;
  if (weapon) {
    cacheWeapon(weapon);
  }
}

function updateSlotAttachmentId(slot: Slot, attachmentId: string) {
  form.items[slot] = attachmentId;
}

function updateSlotAttachment(slot: Slot, attachment: AttachmentOption | null) {
  selectedAttachmentsBySlot.value = {
    ...selectedAttachmentsBySlot.value,
    [slot]: attachment ?? undefined,
  };
}

function validateSelectedAttachments(items: { slot: Slot; attachmentId: string }[]) {
  const missingSlot = items.find((item) => selectedAttachmentsBySlot.value[item.slot]?.id !== item.attachmentId)?.slot;
  if (missingSlot) {
    throw new Error("请重新搜索并选择槽位配件");
  }
}

function refreshPageBounds() {
  if (!pagedBuilds.value.length && filteredBuilds.value.length && page.value > 1) {
    page.value -= 1;
  }
}

watch(
  () => [filters.keyword, filters.weaponId, filters.generation, filters.favorite],
  () => {
    page.value = 1;
  },
);

watch(pageSize, () => {
  page.value = 1;
});

watch(
  () => builds.value.map((build) => build.weaponId).join("|"),
  () => {
    void ensureWeaponOptions(builds.value.map((build) => build.weaponId));
  },
  { immediate: true },
);

watch(
  () => route.query.weaponId,
  async (value) => {
    const request = preferredWeaponRequest.next();
    if (typeof value !== "string" || !value) {
      preferredWeapon.value = null;
      return;
    }
    await ensureWeaponOptions([value]);
    if (request.isLatest()) {
      preferredWeapon.value = weaponCache.value[value] ?? null;
    }
  },
  { immediate: true },
);

async function saveBuild() {
  saving.value = true;
  errorMessage.value = "";

  try {
    const buildPayload = buildBuildSavePayload(form);
    const weaponId = buildPayload.weaponId;
    const weapon = selectedFormWeapon.value;
    if (!weapon || weapon.id !== weaponId) {
      throw new Error("请重新搜索并选择所属武器");
    }
    const invalidGeneration = buildPayload.generations.find((generation) => !weapon.generations.includes(generation));
    if (invalidGeneration) {
      throw new Error("配装代际必须属于所属武器");
    }
    const buildItems = buildBuildItemPayload(buildPayload.id, form, weapon, currentItems.value);
    validateSelectedAttachments(buildItems);

    await buildsStore.saveBuildWithItems(
      buildPayload,
      buildItems,
    );
    cacheWeapon(weapon);
    message.success("本地配装已保存到当前浏览器。");
    refreshPageBounds();
    closeEditor();
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : String(error);
  } finally {
    saving.value = false;
  }
}

function confirmDelete(build: Build) {
  dialog.error({
    title: "删除本地配装？",
    content: `确认删除 ${build.name} 吗？`,
    positiveText: "删除",
    negativeText: "取消",
    positiveButtonProps: { type: "error" },
    onPositiveClick: async () => {
      deletingId.value = build.id;
      errorMessage.value = "";
      try {
        await buildsStore.deleteBuild(build.id);
        refreshPageBounds();
        message.success("本地配装已删除。");
      } catch (error) {
        errorMessage.value = error instanceof Error ? error.message : String(error);
      } finally {
        deletingId.value = "";
      }
    },
  });
}
</script>

<template>
  <section class="space-y-6">
    <n-card :bordered="false" class="hero-panel glass-panel card-xl">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div class="space-y-2">
          <p class="eyebrow-text">My Local Builds</p>
          <h1 class="page-title">我的配装</h1>
          <p class="max-w-2xl text-sm leading-7 text-slate-300">
            在当前浏览器里保存、筛选和维护自己的配装。
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
              <n-input v-model:value="filters.keyword" clearable placeholder="输入配装或武器名称" />
            </n-form-item>
            <n-form-item label="所属武器">
              <WeaponRemoteSelect
                v-model:value="filters.weaponId"
                :selected-weapon="selectedFilterWeapon"
                source="public"
                placeholder="全部武器"
                @update:selected-weapon="handleFilterWeaponUpdate"
              />
            </n-form-item>
            <n-form-item label="代际">
              <n-select
                v-model:value="filters.generation"
                :options="[{ label: '全部代际', value: 'ALL' }, ...generationSelectOptions]"
              />
            </n-form-item>
            <n-form-item label="收藏">
              <n-select v-model:value="filters.favorite" :options="favoriteOptions" />
            </n-form-item>
          </div>
        </n-form>

        <n-alert v-if="errorMessage" :bordered="false" type="error">
          {{ errorMessage }}
        </n-alert>

        <ResultSummary
          :loading="loadingWeapons"
          :page="page"
          :page-size="pageSize"
          :total="filteredBuilds.length"
          label="本地配装"
        />

        <div v-if="pagedBuilds.length" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5">
          <BuildCard
            v-for="build in pagedBuilds"
            :key="build.id"
            :build="build"
            :item-count="itemCountByBuildId.get(build.id) ?? 0"
            :weapon-name="selectedWeaponMap.get(build.weaponId)?.name ?? '未知武器'"
          >
            <template #actions>
              <RouterLink :to="`/my-builds/${build.id}`">
                <n-button quaternary round size="small">展示</n-button>
              </RouterLink>
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

        <EmptyState
          v-if="!loadingWeapons && !pagedBuilds.length"
          description="当前筛选条件下没有本地配装。"
          title="暂无本地配装"
        />

        <PaginationFooter
          v-model:page="page"
          v-model:page-size="pageSize"
          :disabled="loadingWeapons"
          :page-sizes="userPageSizes"
          :total="filteredBuilds.length"
        />
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
      weapon-locked-hint="已有本地配装不在编辑时切换武器；请新建一条配装来维护另一把武器。"
      save-text="保存到浏览器"
      source="public"
      @close="requestCloseEditor"
      @save="saveBuild"
      @weapon-update="handleFormWeaponUpdate"
      @slot-attachment-id-update="updateSlotAttachmentId"
      @slot-attachment-update="updateSlotAttachment"
    />
  </section>
</template>
