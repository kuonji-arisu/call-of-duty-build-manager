import type { Generation, GenerationFilter } from "../types";

export function matchesGenerationFilter(
  generations: Generation[],
  generationFilter: GenerationFilter,
): boolean {
  return generationFilter === "ALL" || generations.includes(generationFilter);
}

export function intersectsGenerations(
  left: Generation[],
  right: Generation[],
): boolean {
  return left.some((item) => right.includes(item));
}
