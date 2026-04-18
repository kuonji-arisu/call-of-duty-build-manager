import { computed, ref, type Ref } from "vue";

import type { AttachmentBindingCandidate } from "../../shared/types";

export function useAttachmentBindingSelection(items: Ref<AttachmentBindingCandidate[]>) {
  const selectedIds = ref<string[]>([]);
  const visibleIds = computed(() => items.value.map((item) => item.attachmentId));
  const allVisibleSelected = computed(() =>
    visibleIds.value.length > 0 && visibleIds.value.every((id) => selectedIds.value.includes(id)),
  );

  function clearSelection() {
    selectedIds.value = [];
  }

  function isSelected(id: string) {
    return selectedIds.value.includes(id);
  }

  function setSelected(id: string, checked: boolean) {
    if (checked) {
      selectedIds.value = [...new Set([...selectedIds.value, id])];
    } else {
      selectedIds.value = selectedIds.value.filter((item) => item !== id);
    }
  }

  function setAllVisible(checked: boolean) {
    if (checked) {
      selectedIds.value = [...new Set([...selectedIds.value, ...visibleIds.value])];
    } else {
      const visible = new Set(visibleIds.value);
      selectedIds.value = selectedIds.value.filter((id) => !visible.has(id));
    }
  }

  return {
    selectedIds,
    visibleIds,
    allVisibleSelected,
    clearSelection,
    isSelected,
    setSelected,
    setAllVisible,
  };
}
