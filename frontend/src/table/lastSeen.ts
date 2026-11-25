// Where the viewer left off, per game, user and kind (the log, the chat), kept in this browser. Storage can be off or
// full (a private window, blocked site data): the table then simply forgets.

const PREFIX = 'frakkin:lastSeen:';

export function readLastSeen(key: string): number | null {
  try {
    const value = window.localStorage.getItem(PREFIX + key);
    return value === null || Number.isNaN(Number(value)) ? null : Number(value);
  } catch {
    return null;
  }
}

export function writeLastSeen(key: string, value: number): void {
  try {
    window.localStorage.setItem(PREFIX + key, String(value));
  } catch {
    // forgotten, see above
  }
}
