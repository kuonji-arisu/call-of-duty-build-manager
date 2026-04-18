import type { SelectOption } from "naive-ui";

import type { OptionItem } from "../types";

export function toSelectOptions<T extends string>(items: OptionItem<T>[]): SelectOption[] {
  return items.map((item) => ({
    label: item.label,
    value: item.value,
  }));
}
