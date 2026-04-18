<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useMessage } from "naive-ui";

import WeaponRemoteSelect from "../components/weapons/WeaponRemoteSelect.vue";
import EmptyState from "../components/common/EmptyState.vue";
import PaginationFooter from "../components/common/PaginationFooter.vue";
import ResultSummary from "../components/common/ResultSummary.vue";
import TagBadge from "../components/common/TagBadge.vue";
import { adminAttachmentBindingsApi } from "../api/admin/attachmentBindings";
import { useAttachmentBindingSelection } from "../composables/bindings/useAttachmentBindingSelection";
import { useLatestRequest } from "../composables/useLatestRequest";
import { debounce } from "../shared/utils/debounce";
import { toSelectOptions } from "../shared/utils/naive";
import {
  getAttachmentTagLabel,
  getAttachmentTagOptions,
  getGenerationLabel,
  getGenerationOptions,
  getSlotLabel,
} from "../shared/utils/labels";
import type {
  AttachmentBindingCandidate,
  AttachmentTag,
  GenerationFilter,
  Slot,
  WeaponOption,
} from "../shared/types";

type SlotFilter = Slot | "ALL";
type TagFilter = AttachmentTag | "ALL";
type BoundFilter = "ALL" | "true" | "false";

const message = useMessage();
const items = ref<AttachmentBindingCandidate[]>([]);
const total = ref(0);
const loading = ref(false);
const updating = ref(false);
const updatingId = ref("");
const errorMessage = ref("");
const candidatesRequest = useLatestRequest();
const selectedWeapon = ref<WeaponOption | null>(null);
const page = ref(1);
const pageSize = ref(10);
const filters = reactive({
  weaponId: "",
  keyword: "",
  slot: "ALL" as SlotFilter,
  generation: "ALL" as GenerationFilter,
  tag: "ALL" as TagFilter,
  bound: "ALL" as BoundFilter,
});

const generationSelectOptions = computed(() => toSelectOptions(getGenerationOptions()));
const attachmentTagSelectOptions = computed(() => toSelectOptions(getAttachmentTagOptions()));
const slotSelectOptions = computed(() => {
  const slots = selectedWeapon.value?.slots ?? [];
  return slots.map((slot) => ({
    label: getSlotLabel(slot),
    value: slot,
  }));
});
const boundOptions = [
  { label: "全部", value: "ALL" },
  { label: "已绑定", value: "true" },
  { label: "未绑定", value: "false" },
];
const adminPageSizes = [10, 20, 50, 100];
const {
  selectedIds,
  allVisibleSelected,
  clearSelection,
  isSelected,
  setSelected,
  setAllVisible,
} = useAttachmentBindingSelection(items);

function boundQueryValue() {
  if (filters.bound === "ALL") {
    return null;
  }
  return filters.bound === "true";
}

async function loadCandidates() {
  clearSelection();
  const request = candidatesRequest.next();
  if (!filters.weaponId) {
    items.value = [];
    total.value = 0;
    loading.value = false;
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const result = await adminAttachmentBindingsApi.listAttachmentBindings({
      page: page.value,
      pageSize: pageSize.value,
      weaponId: filters.weaponId,
      keyword: filters.keyword,
      slot: filters.slot,
      generation: filters.generation,
      tag: filters.tag,
      bound: boundQueryValue(),
    });
    if (!request.isLatest()) {
      return;
    }
    items.value = result.items;
    total.value = result.total;
    page.value = result.page;
    pageSize.value = result.pageSize;
  } catch (error) {
    if (request.isLatest()) {
      errorMessage.value = error instanceof Error ? error.message : String(error);
    }
  } finally {
    if (request.isLatest()) {
      loading.value = false;
    }
  }
}

function resetAndLoad() {
  if (page.value === 1) {
    void loadCandidates();
  } else {
    page.value = 1;
  }
}

const debouncedResetAndLoad = debounce(resetAndLoad, 300);

watch(() => filters.keyword, debouncedResetAndLoad);
watch(() => [filters.weaponId, filters.slot, filters.generation, filters.tag, filters.bound], resetAndLoad);
watch(page, () => {
  void loadCandidates();
});
watch(pageSize, resetAndLoad);
watch(selectedWeapon, () => {
  if (filters.slot !== "ALL" && !selectedWeapon.value?.slots.includes(filters.slot)) {
    filters.slot = "ALL";
  }
});

async function updateBindings(attachmentIds: string[], bound: boolean) {
  if (!filters.weaponId || !attachmentIds.length) {
    return;
  }

  updating.value = true;
  errorMessage.value = "";
  try {
    await adminAttachmentBindingsApi.updateAttachmentBindings({
      weaponId: filters.weaponId,
      attachmentIds,
      bound,
    });
    await loadCandidates();
    message.success(bound ? "绑定已更新。" : "绑定已移除。");
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : String(error);
  } finally {
    updating.value = false;
  }
}

async function updateOne(candidate: AttachmentBindingCandidate, bound: boolean) {
  updatingId.value = candidate.attachmentId;
  try {
    await updateBindings([candidate.attachmentId], bound);
  } finally {
    updatingId.value = "";
  }
}
</script>

<template>
  <section class="space-y-6">
    <n-card :bordered="false" class="hero-panel glass-panel card-xl">
      <div class="space-y-2">
        <p class="eyebrow-text">Admin / Weapon Attachment Bindings</p>
        <h1 class="page-title">武器配件绑定</h1>
        <p class="max-w-2xl text-sm leading-7 text-slate-300">
          按武器和槽位批量维护可用配件，配件本体编辑不再承担绑定维护。
        </p>
      </div>
    </n-card>

    <n-card :bordered="false" class="glass-panel card-xl">
      <div class="space-y-5">
        <n-form label-placement="top">
          <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-6">
            <n-form-item class="xl:col-span-2" label="武器">
              <WeaponRemoteSelect v-model:value="filters.weaponId" v-model:selected-weapon="selectedWeapon"
                placeholder="搜索并选择武器" />
            </n-form-item>
            <n-form-item label="槽位">
              <n-select v-model:value="filters.slot" :disabled="!selectedWeapon"
                :options="[{ label: '全部槽位', value: 'ALL' }, ...slotSelectOptions]" />
            </n-form-item>
            <n-form-item label="搜索">
              <n-input v-model:value="filters.keyword" clearable placeholder="输入配件名称" />
            </n-form-item>
            <n-form-item label="代际">
              <n-select v-model:value="filters.generation"
                :options="[{ label: '全部代际', value: 'ALL' }, ...generationSelectOptions]" />
            </n-form-item>
            <n-form-item label="绑定">
              <n-select v-model:value="filters.bound" :options="boundOptions" />
            </n-form-item>
            <n-form-item label="标签">
              <n-select v-model:value="filters.tag"
                :options="[{ label: '全部标签', value: 'ALL' }, ...attachmentTagSelectOptions]" />
            </n-form-item>
          </div>
        </n-form>

        <div class="flex flex-wrap items-center gap-3">
          <n-button round type="primary" :disabled="!selectedIds.length || updating" :loading="updating"
            @click="updateBindings(selectedIds, true)">
            绑定选中候选项
          </n-button>
          <n-button round type="error" :disabled="!selectedIds.length || updating" :loading="updating"
            @click="updateBindings(selectedIds, false)">
            移除选中候选项
          </n-button>
          <span class="text-sm text-slate-400">已选择 {{ selectedIds.length }} 个候选项</span>
        </div>

        <n-alert v-if="errorMessage" :bordered="false" type="error">
          {{ errorMessage }}
        </n-alert>

        <ResultSummary :loading="loading" :page="page" :page-size="pageSize" :total="total" label="候选配件" />

        <div v-if="items.length" class="overflow-x-auto rounded-2xl border border-white/8">
          <table class="w-full min-w-245 text-left text-sm">
            <thead class="bg-white/6 text-xs uppercase text-slate-400">
              <tr>
                <th class="w-12 px-4 py-3 font-medium">
                  <div class="flex items-center gap-2">
                    <n-checkbox :checked="allVisibleSelected" @update:checked="setAllVisible" />
                    <n-tooltip trigger="hover">
                      <template #trigger>
                        <span class="cursor-help text-slate-500">批量</span>
                      </template>
                      勾选仅用于批量操作，不代表绑定状态。
                    </n-tooltip>
                  </div>
                </th>
                <th class="px-4 py-3 font-medium">配件</th>
                <th class="px-4 py-3 font-medium">槽位</th>
                <th class="px-4 py-3 font-medium">代际</th>
                <th class="px-4 py-3 font-medium">标签</th>
                <th class="px-4 py-3 font-medium">状态</th>
                <th class="px-4 py-3 text-right font-medium">操作</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-white/8">
              <tr v-for="candidate in items" :key="candidate.attachmentId" class="bg-white/3">
                <td class="px-4 py-3">
                  <n-checkbox :checked="isSelected(candidate.attachmentId)"
                    @update:checked="setSelected(candidate.attachmentId, $event)" />
                </td>
                <td class="px-4 py-3 font-medium text-slate-50">{{ candidate.attachmentName }}</td>
                <td class="px-4 py-3 text-slate-300">{{ getSlotLabel(candidate.slot) }}</td>
                <td class="px-4 py-3">
                  <div class="flex flex-wrap gap-2">
                    <TagBadge v-for="generation in candidate.generations" :key="generation" tone="muted">
                      {{ getGenerationLabel(generation) }}
                    </TagBadge>
                  </div>
                </td>
                <td class="px-4 py-3">
                  <div class="flex flex-wrap gap-2">
                    <TagBadge v-for="tag in candidate.tags" :key="tag" tone="accent">
                      {{ getAttachmentTagLabel(tag) }}
                    </TagBadge>
                    <span v-if="!candidate.tags.length" class="text-slate-500">-</span>
                  </div>
                </td>
                <td class="px-4 py-3">
                  <TagBadge :tone="candidate.bound ? 'accent' : 'muted'">
                    {{ candidate.bound ? "已绑定" : "未绑定" }}
                  </TagBadge>
                </td>
                <td class="px-4 py-3">
                  <div class="flex justify-end gap-2">
                    <n-button v-if="!candidate.bound" :loading="updatingId === candidate.attachmentId" quaternary round
                      size="small" @click="updateOne(candidate, true)">
                      绑定
                    </n-button>
                    <n-button v-else :loading="updatingId === candidate.attachmentId" quaternary round size="small"
                      type="error" @click="updateOne(candidate, false)">
                      移除
                    </n-button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <EmptyState v-else-if="!loading && filters.weaponId" description="当前筛选条件下没有候选配件。" title="没有候选配件" />
        <EmptyState v-else-if="!loading" description="先搜索并选择一把武器，再维护它的配件绑定。" title="请选择武器" />

        <PaginationFooter v-model:page="page" v-model:page-size="pageSize" :disabled="loading || !filters.weaponId"
          :page-sizes="adminPageSizes" :total="total" />
      </div>
    </n-card>
  </section>
</template>
