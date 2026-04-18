import type { Generation, Slot } from "./common";
import type { Attachment } from "./attachments";
import type { Weapon } from "./weapons";

export interface Build {
  id: string;
  weaponId: string;
  name: string;
  generations: Generation[];
  notes?: string | null;
  sortOrder: number;
  isFavorite: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface BuildSavePayload {
  id: string;
  weaponId: string;
  name: string;
  generations: Generation[];
  notes?: string | null;
  sortOrder: number;
  isFavorite: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface BuildSummary extends Build {
  weaponName: string;
  itemCount: number;
}

export interface BuildItem {
  id: string;
  buildId: string;
  slot: Slot;
  attachmentId: string;
  createdAt: string;
}

export interface BuildItemSavePayload {
  id: string;
  slot: Slot;
  attachmentId: string;
  createdAt: string;
}

export interface RecommendedBuildSavePayload extends BuildSavePayload {
  items: BuildItemSavePayload[];
}

export interface BuildDetailItem {
  item: BuildItem;
  attachment: Attachment;
}

export interface BuildDetail {
  build: Build;
  weapon: Weapon;
  items: BuildDetailItem[];
}
