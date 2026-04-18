export function requireText(value: string, message: string): string {
  const normalized = value.trim();
  if (!normalized) {
    throw new Error(message);
  }
  return normalized;
}

export function requireSelection<T>(values: T[], message: string): T[] {
  if (!values.length) {
    throw new Error(message);
  }
  return values;
}
