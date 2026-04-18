import type { Generation, Slot, WeaponTag, WeaponType } from "./common";

export interface Weapon {
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

export interface WeaponOption {
  id: string;
  name: string;
  weaponType: WeaponType;
  generations: Generation[];
  slots: Slot[];
}

export interface WeaponSavePayload {
  name: string;
  weaponType: WeaponType;
  tags: WeaponTag[];
  generations: Generation[];
  slots: Slot[];
  sortOrder: number;
  isFavorite: boolean;
}
