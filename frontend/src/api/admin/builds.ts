import { request } from "../client";
import { toQueryString } from "../query";
import type { BuildListQuery } from "../domainTypes";
import type { Build, BuildDetail, BuildSummary, PageResult, RecommendedBuildSavePayload } from "../../shared/types";

export const adminBuildsApi = {
  listBuilds: (query?: BuildListQuery) =>
    request<PageResult<BuildSummary>>(`/admin/builds${toQueryString(query)}`, undefined, { auth: true }),
  getBuildDetail: (buildId: string) =>
    request<BuildDetail>(`/admin/builds/${buildId}`, undefined, { auth: true }),
  saveBuild: (build: RecommendedBuildSavePayload) =>
    request<Build>("/admin/builds", {
      method: "POST",
      body: JSON.stringify(build),
    }, { auth: true }),
  deleteBuild: (buildId: string) =>
    request<void>(`/admin/builds/${buildId}`, { method: "DELETE" }, { auth: true }),
};
