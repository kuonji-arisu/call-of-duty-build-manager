import { ref } from "vue";

import { useLatestRequest } from "../../composables/useLatestRequest";
import { debounce } from "./debounce";

export function useGuardedRemoteSearch<T>(
  fetcher: (keyword: string) => Promise<T>,
  applyResult: (result: T) => void,
  options: { delay?: number; onError?: (error: unknown) => void } = {},
) {
  const loading = ref(false);
  const requestGuard = useLatestRequest();

  async function search(keyword = "") {
    const request = requestGuard.next();
    loading.value = true;

    try {
      const result = await fetcher(keyword);
      if (request.isLatest()) {
        applyResult(result);
      }
    } catch (error) {
      if (request.isLatest()) {
        options.onError?.(error);
      }
    } finally {
      if (request.isLatest()) {
        loading.value = false;
      }
    }
  }

  const debouncedSearch = debounce((keyword: string) => {
    void search(keyword);
  }, options.delay ?? 250);

  return {
    loading,
    search,
    debouncedSearch,
  };
}
