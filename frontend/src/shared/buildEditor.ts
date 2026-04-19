import type { Generation, Slot } from "./types";

export interface BuildEditorFormState {
  id: string;
  weaponId: string;
  name: string;
  generation: Generation;
  notes: string;
  sortOrder: number;
  isFavorite: boolean;
  items: Partial<Record<Slot, string>>;
}

export function createEmptyBuildEditorForm(preferredWeaponId = ""): BuildEditorFormState {
  return {
    id: "",
    weaponId: preferredWeaponId,
    name: "",
    generation: "",
    notes: "",
    sortOrder: 0,
    isFavorite: false,
    items: {},
  };
}
