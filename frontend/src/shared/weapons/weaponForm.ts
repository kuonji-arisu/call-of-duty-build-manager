import type { Generation, Slot, Weapon, WeaponTag, WeaponType } from "../types";

export interface WeaponFormState {
  id: string;
  name: string;
  weaponType: WeaponType;
  tags: WeaponTag[];
  generations: Generation[];
  slots: Slot[];
  sortOrder: number;
  isFavorite: boolean;
  createdAt: string;
  updatedAt: string;
}

export function createEmptyWeaponForm(): WeaponFormState {
  return {
    id: "",
    name: "",
    weaponType: "assault-rifle",
    tags: [],
    generations: ["BO7"],
    slots: ["optic", "muzzle", "underbarrel"],
    sortOrder: 0,
    isFavorite: false,
    createdAt: "",
    updatedAt: "",
  };
}

export function cloneWeaponToForm(weapon: Weapon): WeaponFormState {
  return {
    ...weapon,
    tags: [...weapon.tags],
    generations: [...weapon.generations],
    slots: [...weapon.slots],
  };
}

export function createWeaponFormSnapshot(form: WeaponFormState) {
  return {
    id: form.id,
    name: form.name,
    weaponType: form.weaponType,
    tags: [...form.tags].sort(),
    generations: [...form.generations].sort(),
    slots: [...form.slots].sort(),
    sortOrder: form.sortOrder,
    isFavorite: form.isFavorite,
  };
}
