export interface PageQuery {
  page?: number;
  pageSize?: number;
}

export function toQueryString(query?: object) {
  const params = new URLSearchParams();

  Object.entries(query ?? {}).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "" || value === "ALL") {
      return;
    }
    if (Array.isArray(value)) {
      value.forEach((entry) => {
        if (entry !== undefined && entry !== null && entry !== "" && entry !== "ALL") {
          params.append(key, String(entry));
        }
      });
      return;
    }
    params.set(key, String(value));
  });

  const text = params.toString();
  return text ? `?${text}` : "";
}
