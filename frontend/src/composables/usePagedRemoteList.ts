import { ref, type Ref } from "vue";

import type { PageResult } from "../shared/types";
import { useLatestRequest } from "./useLatestRequest";

interface UsePagedRemoteListOptions<T> {
  pageSize?: number;
  fetcher: (page: number, pageSize: number) => Promise<PageResult<T>>;
  onError?: (message: string) => void;
}

export function usePagedRemoteList<T>({ pageSize: initialPageSize = 10, fetcher, onError }: UsePagedRemoteListOptions<T>) {
  const items = ref<T[]>([]) as Ref<T[]>;
  const total = ref(0);
  const page = ref(1);
  const pageSize = ref(initialPageSize);
  const loading = ref(false);
  const requestGuard = useLatestRequest();

  async function load() {
    const request = requestGuard.next();
    loading.value = true;
    onError?.("");
    try {
      const result = await fetcher(page.value, pageSize.value);
      if (!request.isLatest()) {
        return;
      }
      items.value = result.items;
      total.value = result.total;
      page.value = result.page;
      pageSize.value = result.pageSize;
    } catch (error) {
      if (request.isLatest()) {
        onError?.(error instanceof Error ? error.message : String(error));
      }
    } finally {
      if (request.isLatest()) {
        loading.value = false;
      }
    }
  }

  async function refreshAfterDelete() {
    await load();
    if (!items.value.length && total.value > 0 && page.value > 1) {
      page.value -= 1;
      await load();
    }
  }

  function resetAndLoad() {
    if (page.value === 1) {
      void load();
    } else {
      page.value = 1;
    }
  }

  return {
    items,
    total,
    page,
    pageSize,
    loading,
    load,
    resetAndLoad,
    refreshAfterDelete,
  };
}
