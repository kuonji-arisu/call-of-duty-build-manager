import type { BuildEditorFormState } from "../buildEditor";

export function createBuildFormSnapshot(form: BuildEditorFormState) {
  return {
    id: form.id,
    weaponId: form.weaponId,
    name: form.name,
    generations: [...form.generations].sort(),
    notes: form.notes,
    sortOrder: form.sortOrder,
    isFavorite: form.isFavorite,
    items: Object.fromEntries(
      Object.entries(form.items)
        .filter(([, attachmentId]) => Boolean(attachmentId))
        .sort(([left], [right]) => left.localeCompare(right)),
    ),
  };
}
