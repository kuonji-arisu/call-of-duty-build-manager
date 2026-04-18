import type { Generation, Slot } from "./types";

export interface BuildEditorFormState {
  id: string;
  weaponId: string;
  name: string;
  generations: Generation[];
  notes: string;
  sortOrder: number;
  isFavorite: boolean;
  createdAt: string;
  updatedAt: string;
  items: Partial<Record<Slot, string>>;
}

export function createEmptyBuildEditorForm(preferredWeaponId = ""): BuildEditorFormState {
  return {
    id: "",
    weaponId: preferredWeaponId,
    name: "",
    generations: ["BO7"],
    notes: "",
    sortOrder: 0,
    isFavorite: false,
    createdAt: "",
    updatedAt: "",
    items: {},
  };
}
