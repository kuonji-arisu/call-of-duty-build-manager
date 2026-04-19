<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";

import AttachmentCard from "../attachments/AttachmentCard.vue";
import EmptyState from "../common/EmptyState.vue";
import PaginationFooter from "../common/PaginationFooter.vue";
import ResultSummary from "../common/ResultSummary.vue";
import { publicAttachmentsApi } from "../../api/public/attachments";
import { useLatestRequest } from "../../composables/useLatestRequest";
import { debounce } from "../../shared/utils/debounce";
import { toSelectOptions } from "../../shared/utils/naive";
import {
  getAttachmentTagOptions,
  getGenerationOptions,
  getSlotLabel,
} from "../../shared/utils/labels";
import type {
  Attachment,
  AttachmentTag,
  GenerationFilter,
  Slot,
} from "../../shared/types";

type SlotFilter = Slot | "ALL";
type TagFilter = AttachmentTag | "ALL";

interface WeaponAttachmentSectionWeapon {
  id: string;
  slots: Slot[];
  generations: string[];
}

const props = withDefaults(defineProps<{
  weapon: WeaponAttachmentSectionWeapon | null;
  fixedSlot?: Slot | null;
  title?: string;
  eyebrow?: string;
  label?: string;
  emptyTitle?: string;
  emptyDescription?: string;
  showFilters?: boolean;
  showPagination?: boolean;
  pageSizes?: number[];
}>(), {
  fixedSlot: null,
  title: "可用配件",
  eyebrow: "Available Attachments",
  label: "配件",
  emptyTitle: "暂无配件",
  emptyDescription: "当前筛选条件下没有可用配件。",
  showFilters: true,
  showPagination: true,
  pageSizes: () => [10, 20, 50, 100],
});

const attachments = ref<Attachment[]>([]);
const total = ref(0);
const loading = ref(false);
const errorMessage = ref("");
const page = ref(1);
const pageSize = ref(10);
const requestGuard = useLatestRequest();
const filters = reactive({
  keyword: "",
  slot: "ALL" as SlotFilter,
  generation: "ALL" as GenerationFilter,
  tag: "ALL" as TagFilter,
});

const generationOptions = computed(() => {
  const availableGenerations = props.weapon?.generations ?? [];
  const options = getGenerationOptions();
  return toSelectOptions(
    availableGenerations.length
      ? options.filter((option) => availableGenerations.includes(option.value))
      : options,
  );
});
const attachmentTagOptions = computed(() => toSelectOptions(getAttachmentTagOptions()));
const slotOptions = computed(() =>
  (props.weapon?.slots ?? []).map((slot) => ({
    label: getSlotLabel(slot),
    value: slot,
  })),
);
const effectiveSlot = computed<SlotFilter>(() => props.fixedSlot ?? filters.slot);
const shouldShowSlotFilter = computed(() => props.showFilters && !props.fixedSlot);

async function load() {
  const currentWeapon = props.weapon;
  if (!currentWeapon) {
    requestGuard.invalidate();
    attachments.value = [];
    total.value = 0;
    loading.value = false;
    errorMessage.value = "";
    return;
  }

  const request = requestGuard.next();
  loading.value = true;
  errorMessage.value = "";

  try {
    const result = await publicAttachmentsApi.listWeaponAttachments(currentWeapon.id, {
      page: page.value,
      pageSize: pageSize.value,
      keyword: filters.keyword,
      slot: effectiveSlot.value,
      generation: filters.generation,
      tag: filters.tag,
    });
    if (!request.isLatest()) {
      return;
    }
    attachments.value = result.items;
    total.value = result.total;
    page.value = result.page;
    pageSize.value = result.pageSize;
  } catch (error) {
    if (request.isLatest()) {
      attachments.value = [];
      total.value = 0;
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
    void load();
  } else {
    page.value = 1;
  }
}

async function refreshAfterDelete() {
  await load();
  if (!attachments.value.length && total.value > 0 && page.value > 1) {
    page.value -= 1;
    await load();
  }
}

const debouncedResetAndLoad = debounce(resetAndLoad, 300);

watch(
  () => [props.weapon?.id, props.fixedSlot] as const,
  () => {
    if (filters.slot !== "ALL" && !props.weapon?.slots.includes(filters.slot)) {
      filters.slot = "ALL";
    }
    resetAndLoad();
  },
  { immediate: true },
);

watch(() => filters.keyword, debouncedResetAndLoad);
watch(() => [filters.slot, filters.generation, filters.tag], resetAndLoad);
watch(page, () => {
  void load();
});
watch(pageSize, resetAndLoad);

defineExpose({
  load,
  resetAndLoad,
  refreshAfterDelete,
});
</script>

<template>
  <div class="space-y-4">
    <div v-if="eyebrow || title" class="space-y-1">
      <p v-if="eyebrow" class="eyebrow-text">{{ eyebrow }}</p>
      <h3 v-if="title" class="text-xl font-semibold text-slate-50">{{ title }}</h3>
    </div>

    <n-form v-if="showFilters" label-placement="top">
      <div class="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <n-form-item label="搜索配件">
          <n-input v-model:value="filters.keyword" clearable placeholder="输入配件名称" />
        </n-form-item>
        <n-form-item v-if="shouldShowSlotFilter" label="槽位">
          <n-select
            v-model:value="filters.slot"
            :options="[{ label: '全部槽位', value: 'ALL' }, ...slotOptions]"
          />
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

    <n-alert v-if="errorMessage" :bordered="false" type="error">
      {{ errorMessage }}
    </n-alert>

    <ResultSummary
      :loading="loading"
      :page="page"
      :page-size="pageSize"
      :total="total"
      :label="label"
    />

    <div
      v-if="attachments.length || $slots.append"
      class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5"
    >
      <AttachmentCard
        v-for="attachment in attachments"
        :key="attachment.id"
        :attachment="attachment"
        :effects-limit="null"
        :show-sort-order="false"
        :show-weapon-count="false"
      >
        <template #actions="{ attachment: cardAttachment }">
          <slot name="card-actions" :attachment="cardAttachment" />
        </template>
      </AttachmentCard>

      <slot name="append" />
    </div>

    <EmptyState
      v-else-if="!loading"
      :description="emptyDescription"
      :title="emptyTitle"
    />

    <PaginationFooter
      v-if="showPagination"
      v-model:page="page"
      v-model:page-size="pageSize"
      :disabled="loading || !weapon"
      :page-sizes="pageSizes"
      :total="total"
    />
  </div>
</template>
