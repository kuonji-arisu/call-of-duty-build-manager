import {
  ATTACHMENT_EFFECT_TYPES,
  ATTACHMENT_TAGS,
  SLOTS,
  WEAPON_TAGS,
  WEAPON_TYPES,
  type AttachmentEffectType,
  type AttachmentTag,
  type Generation,
  type OptionItem,
  type Slot,
  type WeaponTag,
  type WeaponType,
} from "../types/common";

let generationOptions: OptionItem<Generation>[] = [];

const weaponTypeLabels: Record<WeaponType, string> = {
  "assault-rifle": "突击步枪",
  smg: "冲锋枪",
  lmg: "轻机枪",
  marksman: "射手步枪",
  sniper: "狙击枪",
  shotgun: "霰弹枪",
  pistol: "手枪",
  launcher: "发射器",
  melee: "近战",
};

const weaponTagLabels: Record<WeaponTag, string> = {
  seasonal: "赛季",
};

const slotLabels: Record<Slot, string> = {
  optic: "瞄具",
  muzzle: "枪口",
  barrel: "枪管",
  underbarrel: "下挂",
  magazine: "弹匣",
  "rear-grip": "后握把",
  stock: "枪托",
  laser: "激光",
  "shooting-module": "射击模块",
};

const attachmentTagLabels: Record<AttachmentTag, string> = {
  prestige: "巅峰",
  seasonal: "赛季",
};

const attachmentEffectTypeLabels: Record<AttachmentEffectType, string> = {
  pro: "优点",
  con: "缺点",
};

export function getGenerationLabel(value: Generation): string {
  return generationOptions.find((option) => option.value === value)?.label ?? value;
}

export function setGenerationOptions(options: OptionItem<Generation>[]) {
  generationOptions = options.map((option) => ({ ...option }));
}

export function getWeaponTypeLabel(value: WeaponType): string {
  return weaponTypeLabels[value];
}

export function getWeaponTagLabel(tag: WeaponTag): string {
  return weaponTagLabels[tag];
}

export function getWeaponTagOptions(): OptionItem<WeaponTag>[] {
  return WEAPON_TAGS.map((value) => ({
    value,
    label: getWeaponTagLabel(value),
  }));
}

export function getSlotLabel(value: Slot): string {
  return slotLabels[value];
}

export function normalizeAttachmentTags(tags: string[]): AttachmentTag[] {
  return ATTACHMENT_TAGS.filter((tag) => tags.includes(tag));
}

export function getAttachmentTagLabel(tag: AttachmentTag): string {
  return attachmentTagLabels[tag];
}

export function getAttachmentTagOptions(): OptionItem<AttachmentTag>[] {
  return ATTACHMENT_TAGS.map((value) => ({
    value,
    label: getAttachmentTagLabel(value),
  }));
}

export function getAttachmentEffectTypeLabel(value: AttachmentEffectType): string {
  return attachmentEffectTypeLabels[value];
}

export function getAttachmentEffectTypeOptions(): OptionItem<AttachmentEffectType>[] {
  return ATTACHMENT_EFFECT_TYPES.map((value) => ({
    value,
    label: getAttachmentEffectTypeLabel(value),
  }));
}

export function getGenerationOptions(): OptionItem<Generation>[] {
  return generationOptions.map((option) => ({ ...option }));
}

export function getWeaponTypeOptions(): OptionItem<WeaponType>[] {
  return WEAPON_TYPES.map((value) => ({ value, label: getWeaponTypeLabel(value) }));
}

export function getSlotOptions(): OptionItem<Slot>[] {
  return SLOTS.map((value) => ({ value, label: getSlotLabel(value) }));
}
