import { request } from "../client";
import { toQueryString } from "../query";
import type { AttachmentListQuery, WeaponAttachmentOptionQuery } from "../domainTypes";
import type { Attachment, AttachmentOption, AttachmentSavePayload, PageResult } from "../../shared/types";

export const adminAttachmentsApi = {
  listAttachments: (query?: AttachmentListQuery) =>
    request<PageResult<Attachment>>(`/admin/attachments${toQueryString(query)}`, undefined, { auth: true }),
  searchWeaponAttachmentOptions: (weaponId: string, query?: WeaponAttachmentOptionQuery) =>
    request<PageResult<AttachmentOption>>(
      `/admin/attachments/weapon-options/${weaponId}${toQueryString(query)}`,
      undefined,
      { auth: true },
    ),
  saveAttachment: (attachment: AttachmentSavePayload) =>
    request<Attachment>("/admin/attachments", {
      method: "POST",
      body: JSON.stringify(attachment),
    }, { auth: true }),
  deleteAttachment: (attachmentId: string) =>
    request<void>(`/admin/attachments/${attachmentId}`, { method: "DELETE" }, { auth: true }),
};
