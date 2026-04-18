import { computed, ref, type Ref } from "vue";

import {
  createFormSnapshot,
  hasSnapshotChanged,
  type FormSnapshotFactory,
} from "../shared/forms/snapshot";

export function useDirtyForm<T extends object>(
  form: T,
  active: Ref<boolean>,
  snapshotFactory: FormSnapshotFactory<T> = (value) => value,
) {
  const snapshot = ref("");
  const isDirty = computed(() => active.value && hasSnapshotChanged(form, snapshot.value, snapshotFactory));

  function captureSnapshot() {
    snapshot.value = createFormSnapshot(form, snapshotFactory);
  }

  return {
    snapshot,
    isDirty,
    captureSnapshot,
  };
}
