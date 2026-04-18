<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useRoute } from "vue-router";

import AttachmentCard from "../components/attachments/AttachmentCard.vue";
import BuildCard from "../components/builds/BuildCard.vue";
import EmptyState from "../components/common/EmptyState.vue";
import PaginationFooter from "../components/common/PaginationFooter.vue";
import ResultSummary from "../components/common/ResultSummary.vue";
import TagBadge from "../components/common/TagBadge.vue";
import { publicAttachmentsApi } from "../api/public/attachments";
import { publicBuildsApi } from "../api/public/builds";
import { publicWeaponsApi } from "../api/public/weapons";
import { useLatestRequest } from "../composables/useLatestRequest";
import { debounce } from "../shared/utils/debounce";
import { toSelectOptions } from "../shared/utils/naive";
import {
  getAttachmentTagOptions,
  getGenerationLabel,
  getGenerationOptions,
  getSlotLabel,
  getWeaponTagLabel,
  getWeaponTypeLabel,
} from "../shared/utils/labels";
import type {
  Attachment,
  AttachmentTag,
  BuildSummary,
  GenerationFilter,
  Slot,
  Weapon,
} from "../shared/types";

type SlotFilter = Slot | "ALL";
type TagFilter = AttachmentTag | "ALL";
type FavoriteFilter = "ALL" | "true" | "false";

const route = useRoute();
const weaponId = computed(() => String(route.params.id));
const weapon = ref<Weapon | null>(null);
const weaponLoading = ref(false);
const weaponErrorMessage = ref("");
const weaponRequest = useLatestRequest();
const attachmentsRequest = useLatestRequest();
const buildsRequest = useLatestRequest();

const attachments = ref<Attachment[]>([]);
const attachmentsTotal = ref(0);
const attachmentsLoading = ref(false);
const attachmentsErrorMessage = ref("");
const attachmentsPage = ref(1);
const attachmentsPageSize = ref(10);
const publicPageSizes = [10, 20, 50, 100];
const attachmentFilters = reactive({
  keyword: "",
  slot: "ALL" as SlotFilter,
  generation: "ALL" as GenerationFilter,
  tag: "ALL" as TagFilter,
});

const builds = ref<BuildSummary[]>([]);
const buildsTotal = ref(0);
const buildsLoading = ref(false);
const buildsErrorMessage = ref("");
const buildsPage = ref(1);
const buildsPageSize = ref(10);
const buildFilters = reactive({
  keyword: "",
  generation: "ALL" as GenerationFilter,
  favorite: "ALL" as FavoriteFilter,
});

const generationOptions = computed(() => toSelectOptions(getGenerationOptions()));
const attachmentTagOptions = computed(() => toSelectOptions(getAttachmentTagOptions()));
const slotOptions = computed(() =>
  (weapon.value?.slots ?? []).map((slot) => ({
    label: getSlotLabel(slot),
    value: slot,
  })),
);
const favoriteOptions = [
  { label: "全部", value: "ALL" },
  { label: "仅收藏", value: "true" },
  { label: "未收藏", value: "false" },
];

function favoriteQueryValue() {
  if (buildFilters.favorite === "ALL") {
    return null;
  }
  return buildFilters.favorite === "true";
}

async function loadWeapon(id: string) {
  const request = weaponRequest.next();
  weaponLoading.value = true;
  weaponErrorMessage.value = "";
  weapon.value = null;

  try {
    const result = await publicWeaponsApi.getWeapon(id);
    if (!request.isLatest()) {
      return false;
    }
    weapon.value = result;
    return true;
  } catch (error) {
    if (request.isLatest()) {
      weapon.value = null;
      weaponErrorMessage.value = error instanceof Error ? error.message : String(error);
    }
    return false;
  } finally {
    if (request.isLatest()) {
      weaponLoading.value = false;
    }
  }
}

async function loadAttachments() {
  if (!weapon.value) {
    attachmentsRequest.invalidate();
    attachmentsLoading.value = false;
    return;
  }

  const request = attachmentsRequest.next();
  const currentWeaponId = weapon.value.id;
  attachmentsLoading.value = true;
  attachmentsErrorMessage.value = "";

  try {
    const result = await publicAttachmentsApi.listWeaponAttachments(currentWeaponId, {
      page: attachmentsPage.value,
      pageSize: attachmentsPageSize.value,
      keyword: attachmentFilters.keyword,
      slot: attachmentFilters.slot,
      generation: attachmentFilters.generation,
      tag: attachmentFilters.tag,
    });
    if (!request.isLatest()) {
      return;
    }
    attachments.value = result.items;
    attachmentsTotal.value = result.total;
    attachmentsPage.value = result.page;
    attachmentsPageSize.value = result.pageSize;
  } catch (error) {
    if (request.isLatest()) {
      attachments.value = [];
      attachmentsTotal.value = 0;
      attachmentsErrorMessage.value = error instanceof Error ? error.message : String(error);
    }
  } finally {
    if (request.isLatest()) {
      attachmentsLoading.value = false;
    }
  }
}

async function loadBuilds() {
  if (!weapon.value) {
    buildsRequest.invalidate();
    buildsLoading.value = false;
    return;
  }

  const request = buildsRequest.next();
  const currentWeaponId = weapon.value.id;
  buildsLoading.value = true;
  buildsErrorMessage.value = "";

  try {
    const result = await publicBuildsApi.listWeaponBuilds(currentWeaponId, {
      page: buildsPage.value,
      pageSize: buildsPageSize.value,
      keyword: buildFilters.keyword,
      generation: buildFilters.generation,
      favorite: favoriteQueryValue(),
    });
    if (!request.isLatest()) {
      return;
    }
    builds.value = result.items;
    buildsTotal.value = result.total;
    buildsPage.value = result.page;
    buildsPageSize.value = result.pageSize;
  } catch (error) {
    if (request.isLatest()) {
      builds.value = [];
      buildsTotal.value = 0;
      buildsErrorMessage.value = error instanceof Error ? error.message : String(error);
    }
  } finally {
    if (request.isLatest()) {
      buildsLoading.value = false;
    }
  }
}

watch(
  weaponId,
  async (id) => {
    attachmentsRequest.invalidate();
    buildsRequest.invalidate();
    attachments.value = [];
    attachmentsTotal.value = 0;
    attachmentsLoading.value = false;
    attachmentsErrorMessage.value = "";
    builds.value = [];
    buildsTotal.value = 0;
    buildsLoading.value = false;
    buildsErrorMessage.value = "";

    const loaded = await loadWeapon(id);
    if (!loaded || !weapon.value) {
      return;
    }
    const shouldLoadAttachments = attachmentsPage.value === 1;
    const shouldLoadBuilds = buildsPage.value === 1;
    attachmentsPage.value = 1;
    buildsPage.value = 1;
    await Promise.all([
      shouldLoadAttachments ? loadAttachments() : Promise.resolve(),
      shouldLoadBuilds ? loadBuilds() : Promise.resolve(),
    ]);
  },
  { immediate: true },
);

function resetAttachmentsAndLoad() {
  if (attachmentsPage.value === 1) {
    void loadAttachments();
  } else {
    attachmentsPage.value = 1;
  }
}

const debouncedResetAttachmentsAndLoad = debounce(resetAttachmentsAndLoad, 300);

watch(() => attachmentFilters.keyword, debouncedResetAttachmentsAndLoad);

watch(
  () => [attachmentFilters.slot, attachmentFilters.generation, attachmentFilters.tag],
  resetAttachmentsAndLoad,
);

watch(attachmentsPage, () => {
  void loadAttachments();
});

watch(attachmentsPageSize, resetAttachmentsAndLoad);

function resetBuildsAndLoad() {
  if (buildsPage.value === 1) {
    void loadBuilds();
  } else {
    buildsPage.value = 1;
  }
}

const debouncedResetBuildsAndLoad = debounce(resetBuildsAndLoad, 300);

watch(() => buildFilters.keyword, debouncedResetBuildsAndLoad);

watch(
  () => [buildFilters.generation, buildFilters.favorite],
  resetBuildsAndLoad,
);

watch(buildsPage, () => {
  void loadBuilds();
});

watch(buildsPageSize, resetBuildsAndLoad);
</script>

<template>
  <EmptyState
    v-if="!weapon && !weaponLoading"
    title="找不到武器"
    :description="weaponErrorMessage || '这把武器可能已经被删除，或者链接中的 ID 已失效。'"
  >
    <RouterLink class="secondary-button" to="/">返回武器库</RouterLink>
  </EmptyState>

  <section v-else-if="weapon" class="space-y-6">
    <n-card :bordered="false" class="hero-panel glass-panel card-xl">
      <div class="space-y-5">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
          <div class="space-y-2">
            <p class="eyebrow-text">Weapon Detail</p>
            <h1 class="page-title">{{ weapon.name }}</h1>
            <p class="text-base text-slate-300">{{ getWeaponTypeLabel(weapon.weaponType) }}</p>
          </div>
          <RouterLink :to="`/my-builds?weaponId=${weapon.id}`">
            <n-button round size="large" type="primary">新建本地配装</n-button>
          </RouterLink>
        </div>

        <div class="flex flex-wrap gap-2">
          <TagBadge v-for="generation in weapon.generations" :key="generation" tone="accent">
            {{ getGenerationLabel(generation) }}
          </TagBadge>
          <TagBadge v-for="tag in weapon.tags" :key="tag" tone="muted">
            {{ getWeaponTagLabel(tag) }}
          </TagBadge>
          <TagBadge v-if="weapon.isFavorite" tone="muted">收藏</TagBadge>
        </div>
      </div>
    </n-card>

    <n-card :bordered="false" class="glass-panel card-lg">
      <div class="space-y-4">
        <div class="space-y-1">
          <p class="eyebrow-text">Slots</p>
          <h3 class="text-lg font-semibold text-slate-50">武器槽位</h3>
        </div>
        <div class="flex flex-wrap gap-2">
          <TagBadge v-for="slot in weapon.slots" :key="slot" tone="muted">
            {{ getSlotLabel(slot) }}
          </TagBadge>
        </div>
      </div>
    </n-card>

    <n-card :bordered="false" class="glass-panel card-xl">
      <div class="space-y-4">
        <div class="space-y-1">
          <p class="eyebrow-text">Available Attachments</p>
          <h3 class="text-xl font-semibold text-slate-50">可用配件</h3>
        </div>

        <n-form label-placement="top">
          <div class="grid gap-4 lg:grid-cols-4">
            <n-form-item label="搜索配件">
              <n-input v-model:value="attachmentFilters.keyword" clearable placeholder="输入配件名称" />
            </n-form-item>
            <n-form-item label="槽位">
              <n-select
                v-model:value="attachmentFilters.slot"
                :options="[{ label: '全部槽位', value: 'ALL' }, ...slotOptions]"
              />
            </n-form-item>
            <n-form-item label="代际">
              <n-select
                v-model:value="attachmentFilters.generation"
                :options="[{ label: '全部代际', value: 'ALL' }, ...generationOptions]"
              />
            </n-form-item>
            <n-form-item label="标签">
              <n-select
                v-model:value="attachmentFilters.tag"
                :options="[{ label: '全部标签', value: 'ALL' }, ...attachmentTagOptions]"
              />
            </n-form-item>
          </div>
        </n-form>

        <n-alert v-if="attachmentsErrorMessage" :bordered="false" type="error">
          {{ attachmentsErrorMessage }}
        </n-alert>

        <ResultSummary
          :loading="attachmentsLoading"
          :page="attachmentsPage"
          :page-size="attachmentsPageSize"
          :total="attachmentsTotal"
          label="配件"
        />

        <div v-if="attachments.length" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5">
          <AttachmentCard
            v-for="attachment in attachments"
            :key="attachment.id"
            :attachment="attachment"
            :effects-limit="null"
            :show-sort-order="false"
            :show-weapon-count="false"
          />
        </div>

        <EmptyState
          v-else-if="!attachmentsLoading"
          description="当前筛选条件下没有可用配件。"
          title="暂无配件"
        />

        <PaginationFooter
          v-model:page="attachmentsPage"
          v-model:page-size="attachmentsPageSize"
          :disabled="attachmentsLoading"
          :page-sizes="publicPageSizes"
          :total="attachmentsTotal"
        />
      </div>
    </n-card>

    <n-card :bordered="false" class="glass-panel card-xl">
      <div class="space-y-4">
        <div class="space-y-1">
          <p class="eyebrow-text">Recommended Builds</p>
          <h3 class="text-xl font-semibold text-slate-50">推荐配装</h3>
        </div>

        <n-form label-placement="top">
          <div class="grid gap-4 lg:grid-cols-3">
            <n-form-item label="搜索配装">
              <n-input v-model:value="buildFilters.keyword" clearable placeholder="输入配装名称" />
            </n-form-item>
            <n-form-item label="代际">
              <n-select
                v-model:value="buildFilters.generation"
                :options="[{ label: '全部代际', value: 'ALL' }, ...generationOptions]"
              />
            </n-form-item>
            <n-form-item label="收藏">
              <n-select v-model:value="buildFilters.favorite" :options="favoriteOptions" />
            </n-form-item>
          </div>
        </n-form>

        <n-alert v-if="buildsErrorMessage" :bordered="false" type="error">
          {{ buildsErrorMessage }}
        </n-alert>

        <ResultSummary
          :loading="buildsLoading"
          :page="buildsPage"
          :page-size="buildsPageSize"
          :total="buildsTotal"
          label="推荐配装"
        />

        <div v-if="builds.length" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5">
          <RouterLink
            v-for="build in builds"
            :key="build.id"
            class="block"
            :to="`/builds/${build.id}`"
          >
            <BuildCard
              :build="build"
              :item-count="build.itemCount"
              :show-sort-order="false"
              :weapon-name="build.weaponName"
            />
          </RouterLink>
        </div>

        <EmptyState
          v-else-if="!buildsLoading"
          description="当前筛选条件下没有推荐配装。"
          title="暂无推荐配装"
        />

        <PaginationFooter
          v-model:page="buildsPage"
          v-model:page-size="buildsPageSize"
          :disabled="buildsLoading"
          :total="buildsTotal"
        />
      </div>
    </n-card>
  </section>
</template>
