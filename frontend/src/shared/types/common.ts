export const GENERATIONS = ["BO7", "WZ"] as const;
export const WEAPON_TAGS = ["seasonal"] as const;
export const ATTACHMENT_TAGS = ["prestige", "seasonal"] as const;
export const ATTACHMENT_EFFECT_TYPES = ["pro", "con"] as const;
export const WEAPON_TYPES = [
  "assault-rifle",
  "smg",
  "lmg",
  "marksman",
  "sniper",
  "shotgun",
  "pistol",
  "launcher",
  "melee",
] as const;
export const SLOTS = [
  "optic",
  "muzzle",
  "barrel",
  "underbarrel",
  "magazine",
  "rear-grip",
  "stock",
  "laser",
  "shooting-module",
] as const;

export type Generation = (typeof GENERATIONS)[number];
export type WeaponTag = (typeof WEAPON_TAGS)[number];
export type AttachmentTag = (typeof ATTACHMENT_TAGS)[number];
export type AttachmentEffectType = (typeof ATTACHMENT_EFFECT_TYPES)[number];
export type WeaponType = (typeof WEAPON_TYPES)[number];
export type Slot = (typeof SLOTS)[number];
export type GenerationFilter = Generation | "ALL";
export type UserRole = "ADMIN" | "USER";

export interface OptionItem<T extends string = string> {
  label: string;
  value: T;
  description?: string;
}
