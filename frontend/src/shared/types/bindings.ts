import type { AttachmentTag, Generation, Slot } from "./common";

export interface AttachmentBindingCandidate {
  attachmentId: string;
  attachmentName: string;
  slot: Slot;
  generations: Generation[];
  tags: AttachmentTag[];
  sortOrder: number;
  bound: boolean;
}

export interface AttachmentBindingUpdatePayload {
  weaponId: string;
  attachmentIds: string[];
  bound: boolean;
}
