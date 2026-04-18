import { request } from "../client";
import type { AppSetting } from "../../shared/types";

export const adminSettingsApi = {
  saveSetting: (key: string, value: string) =>
    request<AppSetting>(`/admin/settings/${key}`, {
      method: "POST",
      body: JSON.stringify({ value }),
    }, { auth: true }),
};
