import { request } from "../client";
import { toQueryString } from "../query";
import type { OptionSearchQuery, WeaponListQuery } from "../domainTypes";
import type { PageResult, Weapon, WeaponOption, WeaponSavePayload } from "../../shared/types";

export const adminWeaponsApi = {
  listWeapons: (query?: WeaponListQuery) =>
    request<PageResult<Weapon>>(`/admin/weapons${toQueryString(query)}`, undefined, { auth: true }),
  listWeaponOptions: () =>
    request<WeaponOption[]>("/admin/weapons/options", undefined, { auth: true }),
  searchWeaponOptions: (query?: OptionSearchQuery) =>
    request<PageResult<WeaponOption>>(`/admin/weapons/search-options${toQueryString(query)}`, undefined, { auth: true }),
  saveWeapon: (weaponId: string, weapon: WeaponSavePayload) =>
    request<Weapon>(weaponId ? `/admin/weapons/${weaponId}` : "/admin/weapons", {
      method: weaponId ? "PUT" : "POST",
      body: JSON.stringify(weapon),
    }, { auth: true }),
  deleteWeapon: (weaponId: string) =>
    request<void>(`/admin/weapons/${weaponId}`, { method: "DELETE" }, { auth: true }),
};
