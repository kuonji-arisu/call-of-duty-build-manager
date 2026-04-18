import { request } from "../client";
import { toQueryString } from "../query";
import type { BuildListQuery } from "../domainTypes";
import type { BuildDetail, BuildSummary, PageResult } from "../../shared/types";

export const publicBuildsApi = {
  listWeaponBuilds: (weaponId: string, query?: Omit<BuildListQuery, "weaponId">) =>
    request<PageResult<BuildSummary>>(`/public/weapons/${weaponId}/builds${toQueryString(query)}`),
  getBuildDetail: (buildId: string) => request<BuildDetail>(`/public/builds/${buildId}`),
};
