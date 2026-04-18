import { nextTick, ref, watch, type Ref } from "vue";
import type { DialogApiInjection } from "naive-ui/es/dialog/src/DialogProvider";

import type { AttachmentOption, BuildItem, Generation, Slot } from "../../shared/types";
import type { BuildEditorFormState } from "../../shared/buildEditor";

interface UseBuildGenerationGuardOptions {
  form: BuildEditorFormState;
  editorVisible: Ref<boolean>;
  selectedAttachmentsBySlot: Ref<Partial<Record<Slot, AttachmentOption>>>;
  currentItems: Ref<BuildItem[]>;
  dialog: Pick<DialogApiInjection, "warning">;
}

export function useBuildGenerationGuard({
  form,
  editorVisible,
  selectedAttachmentsBySlot,
  currentItems,
  dialog,
}: UseBuildGenerationGuardOptions) {
  const generationSnapshotKey = ref("");
  const revertingGenerationChange = ref(false);

  function captureGenerationSnapshot() {
    generationSnapshotKey.value = form.generations.join("|");
  }

  function hasSelectedSlotAttachments() {
    return Object.values(form.items).some(Boolean) || currentItems.value.length > 0;
  }

  function revertGenerationChange() {
    revertingGenerationChange.value = true;
    form.generations = generationSnapshotKey.value.split("|").filter(Boolean) as Generation[];
    void nextTick(() => {
      revertingGenerationChange.value = false;
    });
  }

  function clearSlotAttachmentsForGeneration(value: string) {
    form.items = {};
    selectedAttachmentsBySlot.value = {};
    currentItems.value = [];
    generationSnapshotKey.value = value;
  }

  function confirmGenerationChange(value: string) {
    if (!hasSelectedSlotAttachments()) {
      generationSnapshotKey.value = value;
      return;
    }

    dialog.warning({
      title: "清空槽位配件？",
      content: "修改代际可能导致已选配件不再可用。是否清空当前槽位配件并继续修改？",
      positiveText: "清空并继续",
      negativeText: "保留原代际",
      closable: false,
      maskClosable: false,
      onPositiveClick: () => clearSlotAttachmentsForGeneration(value),
      onNegativeClick: revertGenerationChange,
    });
  }

  watch(
    () => form.generations.join("|"),
    (value) => {
      if (!editorVisible.value || revertingGenerationChange.value || value === generationSnapshotKey.value) {
        return;
      }
      confirmGenerationChange(value);
    },
  );

  return {
    captureGenerationSnapshot,
  };
}
