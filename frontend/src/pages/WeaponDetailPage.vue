<script setup lang="ts">
import { computed, reactive, ref, watch } from "vue";
import { useRoute } from "vue-router";

import BuildCard from "../components/builds/BuildCard.vue";
import EmptyState from "../components/common/EmptyState.vue";
import PaginationFooter from "../components/common/PaginationFooter.vue";
import ResultSummary from "../components/common/ResultSummary.vue";
import TagBadge from "../components/common/TagBadge.vue";
import WeaponAttachmentSection from "../components/weapons/WeaponAttachmentSection.vue";
import { publicBuildsApi } from "../api/public/builds";
import { publicWeaponsApi } from "../api/public/weapons";
import { useLatestRequest } from "../composables/useLatestRequest";
import { debounce } from "../shared/utils/debounce";
import { toSelectOptions } from "../shared/utils/naive";
import {
  getGenerationLabel,
  getGenerationOptions,
  getSlotLabel,
  getWeaponTagLabel,
  getWeaponTypeLabel,
} from "../shared/utils/labels";
import type {
  BuildSummary,
  GenerationFilter,
  Weapon,
} from "../shared/types";

type FavoriteFilter = "ALL" | "true" | "false";

const route = useRoute();
const weaponId = computed(() => String(route.params.id));
const weapon = ref<Weapon | null>(null);
const weaponLoading = ref(false);
const weaponErrorMessage = ref("");
const weaponRequest = useLatestRequest();
const buildsRequest = useLatestRequest();

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

const generationOptions = computed(() => {
  const options = getGenerationOptions();
  return toSelectOptions(
    weapon.value ? options.filter((option) => weapon.value?.generations.includes(option.value)) : options,
  );
});
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
    buildsRequest.invalidate();
    builds.value = [];
    buildsTotal.value = 0;
    buildsLoading.value = false;
    buildsErrorMessage.value = "";

    const loaded = await loadWeapon(id);
    if (!loaded || !weapon.value) {
      return;
    }
    if (buildFilters.generation !== "ALL" && !weapon.value.generations.includes(buildFilters.generation)) {
      buildFilters.generation = "ALL";
    }
    const shouldLoadBuilds = buildsPage.value === 1;
    buildsPage.value = 1;
    if (shouldLoadBuilds) {
      await loadBuilds();
    }
  },
  { immediate: true },
);

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
      <WeaponAttachmentSection :weapon="weapon" />
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
