import type { AttachmentEffectType } from "./common";

export interface AttachmentEffect {
  definitionId: string;
  effectType: AttachmentEffectType;
  level: 1 | 2 | 3 | 4;
  label?: string | null;
  sortOrder?: number | null;
}

export interface AttachmentEffectSavePayload {
  definitionId: string;
  effectType: AttachmentEffectType;
  level: 1 | 2 | 3 | 4;
}

export interface AttachmentEffectDefinition {
  id: string;
  label: string;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
}

export interface AttachmentEffectDefinitionSavePayload {
  label: string;
  sortOrder: number;
}

export interface AttachmentEffectDefinitionOption {
  id: string;
  label: string;
  sortOrder: number;
}
