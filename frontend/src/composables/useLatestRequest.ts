import { onBeforeUnmount } from "vue";

export function useLatestRequest() {
  let requestSeq = 0;
  let disposed = false;

  function next() {
    const seq = ++requestSeq;
    return {
      // 只让最后一次请求回写状态，避免慢响应覆盖用户后来触发的新筛选结果。
      isLatest: () => !disposed && seq === requestSeq,
    };
  }

  function invalidate() {
    requestSeq += 1;
  }

  onBeforeUnmount(() => {
    disposed = true;
    invalidate();
  });

  return {
    next,
    invalidate,
  };
}
