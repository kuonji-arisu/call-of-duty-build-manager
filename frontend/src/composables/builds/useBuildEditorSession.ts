import { reactive, ref, type Ref } from "vue";
import type { DialogApiInjection } from "naive-ui/es/dialog/src/DialogProvider";

import { createBuildFormSnapshot } from "../../shared/builds/buildForm";
import { createEmptyBuildEditorForm, type BuildEditorFormState } from "../../shared/buildEditor";
import type { AttachmentOption, Build, BuildItem, Generation, Slot, WeaponOption } from "../../shared/types";
import { useDirtyForm } from "../useDirtyForm";
import { useBuildGenerationGuard } from "./useBuildGenerationGuard";

interface ResetBuildEditorOptions {
  weapon?: WeaponOption | null;
  attachments?: Partial<Record<Slot, AttachmentOption>>;
  items?: BuildItem[];
  build?: Build | null;
}

interface UseBuildEditorSessionOptions {
  dialog: Pick<DialogApiInjection, "warning">;
  saving: Ref<boolean>;
  errorMessage: Ref<string>;
  closeWarningTitle: string;
}

export function defaultGenerationsForWeapon(
  weapon: WeaponOption,
  currentGenerations: Generation[],
): Generation[] {
  const allowed = new Set(weapon.generations);
  const nextGenerations = currentGenerations.filter((generation) => allowed.has(generation));
  return (nextGenerations.length ? nextGenerations : weapon.generations.slice(0, 1)) as Generation[];
}

export function createBuildFormForWeapon(weapon: WeaponOption | null) {
  const nextForm = createEmptyBuildEditorForm(weapon?.id ?? "");
  if (weapon) {
    nextForm.generations = defaultGenerationsForWeapon(weapon, nextForm.generations);
  }
  return nextForm;
}

export function buildFormFromBuild(build: Build, items: Partial<Record<Slot, string>>): BuildEditorFormState {
  return {
    id: build.id,
    weaponId: build.weaponId,
    name: build.name,
    generations: [...build.generations],
    notes: build.notes ?? "",
    sortOrder: build.sortOrder,
    isFavorite: build.isFavorite,
    items,
  };
}

export function useBuildEditorSession({
  dialog,
  saving,
  errorMessage,
  closeWarningTitle,
}: UseBuildEditorSessionOptions) {
  const form = reactive<BuildEditorFormState>(createEmptyBuildEditorForm());
  const currentBuild = ref<Build | null>(null);
  const currentItems = ref<BuildItem[]>([]);
  const selectedFormWeapon = ref<WeaponOption | null>(null);
  const selectedAttachmentsBySlot = ref<Partial<Record<Slot, AttachmentOption>>>({});
  const editorVisible = ref(false);
  const { isDirty: hasUnsavedChanges, captureSnapshot } = useDirtyForm(
    form,
    editorVisible,
    createBuildFormSnapshot,
  );
  const { captureGenerationSnapshot } = useBuildGenerationGuard({
    form,
    editorVisible,
    selectedAttachmentsBySlot,
    currentItems,
    dialog,
  });

  function resetForm(nextForm: BuildEditorFormState, options: ResetBuildEditorOptions = {}) {
    Object.assign(form, nextForm);
    form.generations = [...nextForm.generations];
    form.items = { ...nextForm.items };
    currentBuild.value = options.build ?? null;
    currentItems.value = [...(options.items ?? [])];
    selectedFormWeapon.value = options.weapon ?? null;
    selectedAttachmentsBySlot.value = { ...(options.attachments ?? {}) };
    captureSnapshot();
    captureGenerationSnapshot();
    errorMessage.value = "";
  }

  function openCreate(weapon: WeaponOption | null) {
    resetForm(createBuildFormForWeapon(weapon), { weapon });
    editorVisible.value = true;
  }

  function closeEditor(weapon: WeaponOption | null) {
    editorVisible.value = false;
    resetForm(createBuildFormForWeapon(weapon), { weapon });
  }

  function requestCloseEditor(weapon: WeaponOption | null) {
    if (saving.value) {
      return;
    }
    if (!hasUnsavedChanges.value) {
      closeEditor(weapon);
      return;
    }

    dialog.warning({
      title: closeWarningTitle,
      content: "关闭后，当前表单里的修改不会保留。",
      positiveText: "放弃修改",
      negativeText: "继续编辑",
      onPositiveClick: () => closeEditor(weapon),
    });
  }

  function normalizeFormGenerationsForWeapon(weapon: WeaponOption | null) {
    if (!weapon) {
      return;
    }
    form.generations = defaultGenerationsForWeapon(weapon, form.generations);
  }

  function handleFormWeaponUpdate(weapon: WeaponOption | null) {
    selectedFormWeapon.value = weapon;
    normalizeFormGenerationsForWeapon(weapon);
    form.items = {};
    selectedAttachmentsBySlot.value = {};
    currentItems.value = [];
  }

  function updateSlotAttachmentId(slot: Slot, attachmentId: string) {
    form.items[slot] = attachmentId;
  }

  function updateSlotAttachment(slot: Slot, attachment: AttachmentOption | null) {
    selectedAttachmentsBySlot.value = {
      ...selectedAttachmentsBySlot.value,
      [slot]: attachment ?? undefined,
    };
  }

  function validateSelectedAttachments(items: { slot: Slot; attachmentId: string }[]) {
    const missingSlot = items.find((item) => selectedAttachmentsBySlot.value[item.slot]?.id !== item.attachmentId)?.slot;
    if (missingSlot) {
      throw new Error("请重新搜索并选择槽位配件");
    }
  }

  return {
    form,
    currentBuild,
    currentItems,
    selectedFormWeapon,
    selectedAttachmentsBySlot,
    editorVisible,
    hasUnsavedChanges,
    resetForm,
    openCreate,
    closeEditor,
    requestCloseEditor,
    handleFormWeaponUpdate,
    updateSlotAttachmentId,
    updateSlotAttachment,
    validateSelectedAttachments,
  };
}
