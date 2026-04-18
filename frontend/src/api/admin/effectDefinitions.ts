import { request } from "../client";
import { toQueryString } from "../query";
import type { EffectDefinitionListQuery, OptionSearchQuery } from "../domainTypes";
import type {
  AttachmentEffectDefinition,
  AttachmentEffectDefinitionOption,
  PageResult,
} from "../../shared/types";

export const adminEffectDefinitionsApi = {
  listAttachmentEffectDefinitions: (query?: EffectDefinitionListQuery) =>
    request<PageResult<AttachmentEffectDefinition>>(
      `/admin/attachment-effect-definitions${toQueryString(query)}`,
      undefined,
      { auth: true },
    ),
  listAttachmentEffectDefinitionOptions: () =>
    request<AttachmentEffectDefinitionOption[]>(
      "/admin/attachment-effect-definitions/options",
      undefined,
      { auth: true },
    ),
  searchAttachmentEffectDefinitionOptions: (query?: OptionSearchQuery) =>
    request<PageResult<AttachmentEffectDefinitionOption>>(
      `/admin/attachment-effect-definitions/search-options${toQueryString(query)}`,
      undefined,
      { auth: true },
    ),
  saveAttachmentEffectDefinition: (definition: AttachmentEffectDefinition) =>
    request<AttachmentEffectDefinition>("/admin/attachment-effect-definitions", {
      method: "POST",
      body: JSON.stringify(definition),
    }, { auth: true }),
  deleteAttachmentEffectDefinition: (definitionId: string) =>
    request<void>(`/admin/attachment-effect-definitions/${definitionId}`, { method: "DELETE" }, { auth: true }),
};
