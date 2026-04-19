<script setup lang="ts">
import { ref, watch } from "vue";
import { useRoute } from "vue-router";

import BuildDetailView from "../components/builds/BuildDetailView.vue";
import EmptyState from "../components/common/EmptyState.vue";
import { publicAttachmentsApi } from "../api/public/attachments";
import { publicWeaponsApi } from "../api/public/weapons";
import { useLatestRequest } from "../composables/useLatestRequest";
import { useBuildsStore } from "../stores/builds";
import type { Attachment, Build, BuildDetail, BuildItem, Weapon } from "../shared/types";

const route = useRoute();
const buildsStore = useBuildsStore();
const detail = ref<BuildDetail | null>(null);
const loading = ref(false);
const errorMessage = ref("");
const detailRequest = useLatestRequest();

function intersects<T>(left: T[], right: T[]) {
  return left.some((value) => right.includes(value));
}

function isReadableLocalBuildItem(
  item: BuildItem,
  build: Build,
  weapon: Weapon,
  attachment: Attachment | undefined,
) {
  if (!attachment) {
    return false;
  }

  return weapon.slots.includes(item.slot)
    && attachment.slot === item.slot
    && attachment.weaponIds.includes(weapon.id)
    && intersects(attachment.generations, weapon.generations)
    && attachment.generations.includes(build.generation);
}

async function loadLocalAttachments(weapon: Weapon, items: BuildItem[]) {
  const attachmentIds = [...new Set(items.map((item) => item.attachmentId).filter(Boolean))];
  if (!attachmentIds.length) {
    return new Map<string, Attachment>();
  }

  const attachments = await publicAttachmentsApi.listWeaponAttachmentsByIds(weapon.id, attachmentIds);
  return new Map(attachments.map((attachment) => [attachment.id, attachment]));
}

async function loadLocalDetail(id: string): Promise<BuildDetail> {
  buildsStore.initialize();
  const build = buildsStore.getBuildById(id);
  if (!build) {
    throw new Error("本地配装不存在");
  }

  const weapon = await publicWeaponsApi.getWeapon(build.weaponId);
  if (!weapon.generations.includes(build.generation)) {
    throw new Error("配装代际与所属武器不匹配");
  }

  const localItems = buildsStore.getBuildItems(build.id);
  const attachmentsById = await loadLocalAttachments(weapon, localItems);
  const readableItems = localItems
    .filter((item) => isReadableLocalBuildItem(item, build, weapon, attachmentsById.get(item.attachmentId)))
    .map((item) => ({
      item,
      attachment: attachmentsById.get(item.attachmentId) as Attachment,
    }));

  return {
    build,
    weapon,
    items: readableItems,
  };
}

async function loadDetail(id: string) {
  const request = detailRequest.next();
  loading.value = true;
  errorMessage.value = "";
  detail.value = null;

  try {
    const result = await loadLocalDetail(id);
    if (!request.isLatest()) {
      return;
    }
    detail.value = result;
  } catch (error) {
    if (request.isLatest()) {
      detail.value = null;
      errorMessage.value = error instanceof Error ? error.message : String(error);
    }
  } finally {
    if (request.isLatest()) {
      loading.value = false;
    }
  }
}

watch(
  () => route.params.id,
  (id) => {
    void loadDetail(String(id));
  },
  { immediate: true },
);
</script>

<template>
  <EmptyState
    v-if="!detail && !loading"
    title="找不到本地配装"
    :description="errorMessage || '这个本地配装可能已经被删除，或者链接中的 ID 已失效。'"
  >
    <RouterLink class="secondary-button" to="/my-builds">返回我的配装</RouterLink>
  </EmptyState>

  <BuildDetailView
    v-else-if="detail"
    :detail="detail"
    eyebrow="Local Build Detail"
    back-link="/my-builds"
    back-text="返回我的配装"
  />
</template>
