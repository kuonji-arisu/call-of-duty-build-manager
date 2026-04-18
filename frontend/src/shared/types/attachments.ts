import type { AttachmentTag, Generation, Slot } from "./common";
import type { AttachmentEffect, AttachmentEffectSavePayload } from "./effects";

export interface AttachmentOption {
  id: string;
  name: string;
  slot: Slot;
}

export interface Attachment {
  id: string;
  name: string;
  subtitle: string;
  slot: Slot;
  weaponIds: string[];
  generations: Generation[];
  tags: AttachmentTag[];
  effects: AttachmentEffect[];
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
}

export interface AttachmentSavePayload {
  name: string;
  subtitle: string;
  slot: Slot;
  generations: Generation[];
  tags: AttachmentTag[];
  effects: AttachmentEffectSavePayload[];
  sortOrder: number;
}
