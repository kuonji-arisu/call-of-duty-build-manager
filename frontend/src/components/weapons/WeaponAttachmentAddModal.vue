<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";

import EmptyState from "../common/EmptyState.vue";
import PaginationFooter from "../common/PaginationFooter.vue";
import ResultSummary from "../common/ResultSummary.vue";
import TagBadge from "../common/TagBadge.vue";
import { adminAttachmentBindingsApi } from "../../api/admin/attachmentBindings";
import { useAttachmentBindingSelection } from "../../composables/bindings/useAttachmentBindingSelection";
import { useLatestRequest } from "../../composables/useLatestRequest";
import { debounce } from "../../shared/utils/debounce";
import { toSelectOptions } from "../../shared/utils/naive";
import {
  getAttachmentTagLabel,
  getAttachmentTagOptions,
  getGenerationLabel,
  getGenerationOptions,
  getSlotLabel,
} from "../../shared/utils/labels";
import type {
  AttachmentBindingCandidate,
  AttachmentTag,
  GenerationFilter,
  Slot,
  WeaponOption,
} from "../../shared/types";

type TagFilter = AttachmentTag | "ALL";

const props = defineProps<{
  show: boolean;
  weapon: WeaponOption | null;
  attachmentSlot: Slot | null;
  saving: boolean;
  errorMessage: string;
}>();

const emit = defineEmits<{
  close: [];
  submit: [attachmentIds: string[]];
}>();

const items = ref<AttachmentBindingCandidate[]>([]);
const total = ref(0);
const loading = ref(false);
const page = ref(1);
const pageSize = ref(10);
const loadErrorMessage = ref("");
const candidatesRequest = useLatestRequest();
const filters = reactive({
  keyword: "",
  generation: "ALL" as GenerationFilter,
  tag: "ALL" as TagFilter,
});
const pageSizes = [10, 20, 50, 100];
const generationOptions = computed(() => toSelectOptions(getGenerationOptions()));
const attachmentTagOptions = computed(() => toSelectOptions(getAttachmentTagOptions()));
const slotLabel = computed(() => (props.attachmentSlot ? getSlotLabel(props.attachmentSlot) : ""));
const displayErrorMessage = computed(() => props.errorMessage || loadErrorMessage.value);
const {
  selectedIds,
  allVisibleSelected,
  clearSelection,
  isSelected,
  setSelected,
  setAllVisible,
} = useAttachmentBindingSelection(items);

async function loadCandidates() {
  const currentWeapon = props.weapon;
  const currentSlot = props.attachmentSlot;
  if (!props.show || !currentWeapon || !currentSlot) {
    candidatesRequest.invalidate();
    items.value = [];
    total.value = 0;
    loading.value = false;
    loadErrorMessage.value = "";
    return;
  }

  const request = candidatesRequest.next();
  clearSelection();
  loading.value = true;
  loadErrorMessage.value = "";

  try {
    const result = await adminAttachmentBindingsApi.listAttachmentBindings({
      page: page.value,
      pageSize: pageSize.value,
      weaponId: currentWeapon.id,
      keyword: filters.keyword,
      slot: currentSlot,
      generation: filters.generation,
      tag: filters.tag,
      bound: false,
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
      items.value = [];
      total.value = 0;
      clearSelection();
      loadErrorMessage.value = error instanceof Error ? error.message : String(error);
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

function submitSelected() {
  emit("submit", [...selectedIds.value]);
}

const debouncedResetAndLoad = debounce(resetAndLoad, 300);

watch(
  () => props.show,
  (show) => {
    if (show) {
      resetAndLoad();
    } else {
      clearSelection();
      loadErrorMessage.value = "";
    }
  },
);

watch(
  () => [props.weapon?.id, props.attachmentSlot] as const,
  () => {
    if (props.show) {
      resetAndLoad();
    }
  },
);

watch(() => filters.keyword, debouncedResetAndLoad);
watch(() => [filters.generation, filters.tag], resetAndLoad);
watch(page, () => {
  void loadCandidates();
});
watch(pageSize, resetAndLoad);
</script>

<template>
  <n-modal
    :show="show"
    :mask-closable="false"
    preset="card"
    :title="`添加${slotLabel}配件`"
    class="max-w-5xl"
    @close="emit('close')"
  >
    <div class="space-y-5">
      <div class="space-y-1">
        <p class="text-sm text-slate-400">{{ weapon?.name }}</p>
        <h3 class="text-lg font-semibold text-slate-50">选择未绑定候选</h3>
      </div>

      <n-form label-placement="top">
        <div class="grid gap-4 md:grid-cols-3">
          <n-form-item label="搜索配件">
            <n-input v-model:value="filters.keyword" clearable placeholder="输入配件名称" />
          </n-form-item>
          <n-form-item label="代际">
            <n-select
              v-model:value="filters.generation"
              :options="[{ label: '全部代际', value: 'ALL' }, ...generationOptions]"
            />
          </n-form-item>
          <n-form-item label="标签">
            <n-select
              v-model:value="filters.tag"
              :options="[{ label: '全部标签', value: 'ALL' }, ...attachmentTagOptions]"
            />
          </n-form-item>
        </div>
      </n-form>

      <n-alert v-if="displayErrorMessage" :bordered="false" type="error">
        {{ displayErrorMessage }}
      </n-alert>

      <div class="flex flex-wrap items-center justify-between gap-3">
        <ResultSummary
          :loading="loading"
          :page="page"
          :page-size="pageSize"
          :total="total"
          label="候选配件"
        />
        <div class="flex items-center gap-3">
          <n-checkbox :checked="allVisibleSelected" :disabled="loading" @update:checked="setAllVisible">
            选择本页
          </n-checkbox>
          <span class="text-sm text-slate-400">已选择 {{ selectedIds.length }} 个</span>
        </div>
      </div>

      <n-spin :show="loading">
        <div class="min-h-72">
          <div v-if="items.length" class="overflow-x-auto rounded-2xl border border-white/8">
            <table class="w-full min-w-180 text-left text-sm">
              <thead class="bg-white/6 text-xs uppercase text-slate-400">
                <tr>
                  <th class="w-12 px-4 py-3 font-medium">选择</th>
                  <th class="px-4 py-3 font-medium">配件</th>
                  <th class="px-4 py-3 font-medium">槽位</th>
                  <th class="px-4 py-3 font-medium">代际</th>
                  <th class="px-4 py-3 font-medium">标签</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-white/8">
                <tr v-for="candidate in items" :key="candidate.attachmentId" class="bg-white/3">
                  <td class="px-4 py-3">
                    <n-checkbox
                      :checked="isSelected(candidate.attachmentId)"
                      :disabled="loading"
                      @update:checked="setSelected(candidate.attachmentId, $event)"
                    />
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
                </tr>
              </tbody>
            </table>
          </div>

          <EmptyState
            v-else-if="!loading"
            description="当前槽位没有可添加的未绑定配件。"
            title="没有候选配件"
          />
        </div>
      </n-spin>

      <PaginationFooter
        v-model:page="page"
        v-model:page-size="pageSize"
        :disabled="loading"
        :page-sizes="pageSizes"
        :total="total"
      />

      <div class="flex justify-end gap-3">
        <n-button round :disabled="saving" @click="emit('close')">取消</n-button>
        <n-button
          round
          type="primary"
          :disabled="!selectedIds.length || loading"
          :loading="saving"
          @click="submitSelected"
        >
          添加选中配件
        </n-button>
      </div>
    </div>
  </n-modal>
</template>
