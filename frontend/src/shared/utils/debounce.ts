export function debounce<T extends (...args: never[]) => void>(callback: T, delay = 300) {
  let timeoutId: number | undefined;

  return (...args: Parameters<T>) => {
    window.clearTimeout(timeoutId);
    timeoutId = window.setTimeout(() => callback(...args), delay);
  };
}
