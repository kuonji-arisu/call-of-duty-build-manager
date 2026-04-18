export function createId(_prefix: string): string {
  return crypto.randomUUID();
}

export function nowIso(): string {
  return new Date().toISOString();
}

export function splitTextareaValues(source: string): string[] {
  return source
    .split(/\r?\n|,/)
    .map((item) => item.trim())
    .filter(Boolean);
}
