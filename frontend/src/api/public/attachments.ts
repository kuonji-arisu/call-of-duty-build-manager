import { request } from "../client";
import { toQueryString } from "../query";
import type { AttachmentListQuery, WeaponAttachmentOptionQuery } from "../domainTypes";
import type { Attachment, AttachmentOption, PageResult, Slot } from "../../shared/types";

export const publicAttachmentsApi = {
  listWeaponAttachments: (weaponId: string, query?: Omit<AttachmentListQuery, "weaponId">) =>
    request<PageResult<Attachment>>(`/public/weapons/${weaponId}/attachments${toQueryString(query)}`),
  listWeaponAttachmentsByIds: (weaponId: string, ids: string[]) =>
    request<Attachment[]>(`/public/weapons/${weaponId}/attachments/by-ids${toQueryString({ ids })}`),
  listWeaponAttachmentOptions: (weaponId: string, slot?: Slot | "ALL") =>
    request<AttachmentOption[]>(`/public/weapons/${weaponId}/attachment-options${toQueryString({ slot })}`),
  searchWeaponAttachmentOptions: (weaponId: string, query?: WeaponAttachmentOptionQuery) =>
    request<PageResult<AttachmentOption>>(
      `/public/weapons/${weaponId}/attachment-search-options${toQueryString(query)}`,
    ),
  listAttachmentOptionsByIds: (ids: string[]) =>
    request<AttachmentOption[]>(`/public/attachments/options${toQueryString({ ids })}`),
};
