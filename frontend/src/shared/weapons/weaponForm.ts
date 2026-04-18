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
  };
}

export function cloneWeaponToForm(weapon: Weapon): WeaponFormState {
  return {
    id: weapon.id,
    name: weapon.name,
    weaponType: weapon.weaponType,
    tags: [...weapon.tags],
    generations: [...weapon.generations],
    slots: [...weapon.slots],
    sortOrder: weapon.sortOrder,
    isFavorite: weapon.isFavorite,
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
