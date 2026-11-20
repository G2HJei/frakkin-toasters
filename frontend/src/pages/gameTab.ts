import type { NavigateFunction } from 'react-router';

export interface GameTab {
  /** Shows the game in the tab, or in this one when the browser blocked the tab. */
  go(id: string): void;
  /** Closes the tab, when the request failed. */
  close(): void;
}

/**
 * A tab of its own for a game that a request makes or joins. A browser opens a tab only while it handles the click, so
 * the tab opens blank at once and goes to the game when the request answers.
 */
export function openGameTab(navigate: NavigateFunction): GameTab {
  const tab = window.open('', '_blank');
  if (tab) {
    tab.opener = null;
  }
  return {
    go: (id) => {
      const path = `/games/${id}`;
      if (!tab) {
        navigate(path);
      } else if (!tab.closed) {
        tab.location.href = new URL(path, window.location.origin).href;
      }
    },
    close: () => tab?.close(),
  };
}
