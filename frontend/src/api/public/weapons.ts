import { request } from "../client";
import { toQueryString } from "../query";
import type { OptionSearchQuery, WeaponListQuery } from "../domainTypes";
import type { PageResult, Weapon, WeaponOption } from "../../shared/types";

export const publicWeaponsApi = {
  listWeapons: (query?: WeaponListQuery) =>
    request<PageResult<Weapon>>(`/public/weapons${toQueryString(query)}`),
  listWeaponOptions: () => request<WeaponOption[]>("/public/weapons/options"),
  searchWeaponOptions: (query?: OptionSearchQuery) =>
    request<PageResult<WeaponOption>>(`/public/weapons/search-options${toQueryString(query)}`),
  getWeapon: (weaponId: string) => request<Weapon>(`/public/weapons/${weaponId}`),
};
