<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from "vue";

import EmptyState from "../components/common/EmptyState.vue";
import PaginationFooter from "../components/common/PaginationFooter.vue";
import ResultSummary from "../components/common/ResultSummary.vue";
import WeaponCard from "../components/weapons/WeaponCard.vue";
import { publicWeaponsApi } from "../api/public/weapons";
import { usePagedRemoteList } from "../composables/usePagedRemoteList";
import { debounce } from "../shared/utils/debounce";
import { toSelectOptions } from "../shared/utils/naive";
import {
  getGenerationOptions,
  getWeaponTypeOptions,
} from "../shared/utils/labels";
import type { GenerationFilter, Weapon, WeaponType } from "../shared/types";

type WeaponTypeFilter = WeaponType | "ALL";
type FavoriteFilter = "ALL" | "true" | "false";

const errorMessage = ref("");
const filters = reactive({
  keyword: "",
  weaponType: "ALL" as WeaponTypeFilter,
  generation: "ALL" as GenerationFilter,
  favorite: "ALL" as FavoriteFilter,
});

const generationOptions = computed(() => toSelectOptions(getGenerationOptions()));
const weaponTypeOptions = computed(() => toSelectOptions(getWeaponTypeOptions()));
const favoriteOptions = [
  { label: "全部", value: "ALL" },
  { label: "仅收藏", value: "true" },
  { label: "未收藏", value: "false" },
];
const publicPageSizes = [10, 20, 50, 100];

function favoriteQueryValue() {
  if (filters.favorite === "ALL") {
    return null;
  }
  return filters.favorite === "true";
}

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
} = usePagedRemoteList<Weapon>({
  fetcher: (currentPage, currentPageSize) =>
    publicWeaponsApi.listWeapons({
      page: currentPage,
      pageSize: currentPageSize,
      keyword: filters.keyword,
      weaponType: filters.weaponType,
      generation: filters.generation,
      favorite: favoriteQueryValue(),
    }),
  onError: setErrorMessage,
});

const debouncedResetAndLoad = debounce(resetAndLoad, 300);

watch(() => filters.keyword, debouncedResetAndLoad);

watch(
  () => [filters.weaponType, filters.generation, filters.favorite],
  resetAndLoad,
);

watch(page, () => {
  void loadWeapons();
});

watch(pageSize, () => {
  resetAndLoad();
});

onMounted(() => {
  void loadWeapons();
});
</script>

<template>
  <section class="space-y-6">
    <n-card :bordered="false" class="hero-panel glass-panel card-xl">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
        <div class="space-y-2">
          <p class="eyebrow-text">Weapon Library</p>
          <h1 class="page-title">武器库</h1>
          <p class="max-w-2xl text-sm leading-7 text-slate-300">
            按名称、武器分类、代际和收藏状态筛选武器，结果由服务端分页返回。
          </p>
        </div>
        <RouterLink to="/my-builds">
          <n-button round type="primary">管理我的配装</n-button>
        </RouterLink>
      </div>
    </n-card>

    <n-card :bordered="false" class="glass-panel card-xl">
      <div class="space-y-5">
        <n-form label-placement="top">
          <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
            <n-form-item label="搜索">
              <n-input v-model:value="filters.keyword" clearable placeholder="例如 XM4 / C9" />
            </n-form-item>

            <n-form-item label="分类">
              <n-select
                v-model:value="filters.weaponType"
                :options="[{ label: '全部分类', value: 'ALL' }, ...weaponTypeOptions]"
              />
            </n-form-item>

            <n-form-item label="代际">
              <n-select
                v-model:value="filters.generation"
                :options="[{ label: '全部代际', value: 'ALL' }, ...generationOptions]"
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
          :loading="loading"
          :page="page"
          :page-size="pageSize"
          :total="total"
          label="武器"
        />

        <div v-if="items.length" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5">
          <WeaponCard v-for="weapon in items" :key="weapon.id" :weapon="weapon">
            <template #actions>
              <RouterLink :to="`/weapons/${weapon.id}`">
                <n-button quaternary round size="small">查看详情</n-button>
              </RouterLink>
              <RouterLink :to="`/my-builds?weaponId=${weapon.id}`">
                <n-button quaternary round size="small">创建配装</n-button>
              </RouterLink>
            </template>
          </WeaponCard>
        </div>

        <EmptyState
          v-else-if="!loading"
          description="当前筛选条件下没有可见武器。"
          title="没有找到武器"
        />

        <PaginationFooter
          v-model:page="page"
          v-model:page-size="pageSize"
          :disabled="loading"
          :page-sizes="publicPageSizes"
          :total="total"
        />
      </div>
    </n-card>
  </section>
</template>
