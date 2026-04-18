import { request } from "../client";
import { toQueryString } from "../query";
import type { AttachmentBindingListQuery } from "../domainTypes";
import type { AttachmentBindingCandidate, AttachmentBindingUpdatePayload, PageResult } from "../../shared/types";

export const adminAttachmentBindingsApi = {
  listAttachmentBindings: (query?: AttachmentBindingListQuery) =>
    request<PageResult<AttachmentBindingCandidate>>(
      `/admin/weapon-attachment-bindings${toQueryString(query)}`,
      undefined,
      { auth: true },
    ),
  updateAttachmentBindings: (payload: AttachmentBindingUpdatePayload) =>
    request<void>("/admin/weapon-attachment-bindings", {
      method: "POST",
      body: JSON.stringify(payload),
    }, { auth: true }),
};
