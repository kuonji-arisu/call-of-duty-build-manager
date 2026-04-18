import type { Weapon, WeaponOption } from "../types";

export function toWeaponOption(weapon: Weapon): WeaponOption {
  return {
    id: weapon.id,
    name: weapon.name,
    weaponType: weapon.weaponType,
    generations: weapon.generations,
    slots: weapon.slots,
  };
}
