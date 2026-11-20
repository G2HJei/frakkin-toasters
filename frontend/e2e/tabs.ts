import type { Page } from '@playwright/test';

// A game opens in a tab of its own: does on the lobby's page what opens it, and returns the tab once it shows the game.
export async function openGame(lobby: Page, open: (lobby: Page) => Promise<unknown>): Promise<Page> {
  const [tab] = await Promise.all([lobby.context().waitForEvent('page'), open(lobby)]);
  await tab.waitForURL(/\/games\/[^/]+$/);
  return tab;
}
