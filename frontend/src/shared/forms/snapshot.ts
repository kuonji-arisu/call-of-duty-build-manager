export type FormSnapshotFactory<T> = (value: T) => unknown;

export function createFormSnapshot<T>(value: T, snapshotFactory: FormSnapshotFactory<T>): string {
  return JSON.stringify(snapshotFactory(value));
}

export function hasSnapshotChanged<T>(
  value: T,
  snapshot: string,
  snapshotFactory: FormSnapshotFactory<T>,
): boolean {
  return createFormSnapshot(value, snapshotFactory) !== snapshot;
}
