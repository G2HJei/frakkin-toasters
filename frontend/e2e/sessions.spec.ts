import { expect, test, type Browser, type BrowserContext, type Page } from '@playwright/test';
import { openGame } from './tabs';

// Logs a new browser in through the dev login and opens the games. Its page becomes the game's tab once it opens one.
async function user(browser: Browser, name: string): Promise<{ context: BrowserContext; page: Page }> {
  const context = await browser.newContext();
  const page = await context.newPage();
  await page.goto('/login');
  await page.getByLabel('name').fill(name);
  await page.getByRole('button', { name: 'Log in (dev)' }).click();
  await expect(page.getByRole('link', { name: 'New sandbox' })).toBeVisible();
  // the seats panel asks before a hand-over, a ban or an abandon, in the game's own tab
  page.on('dialog', (dialog) => void dialog.accept());
  context.on('page', (tab) => tab.on('dialog', (dialog) => void dialog.accept()));
  return { context, page };
}

function seat(page: Page, number: number) {
  return page.getByRole('region', { name: 'seats' }).locator(`[data-seat="${number}"]`);
}

async function openSeats(page: Page) {
  const details = page.getByRole('region', { name: 'seats' }).locator('details');
  if ((await details.getAttribute('open')) === null) {
    await details.locator('summary').click();
  }
}

test('a session: presence, chat, a seat handed over to an observer, and an abandon', async ({ browser }) => {
  const suffix = `${Date.now()}`;
  const game = `Session ${suffix}`;
  const host = await user(browser, `Adama ${suffix}`);
  const roslin = await user(browser, `Roslin ${suffix}`);
  const starbuck = await user(browser, `Starbuck ${suffix}`);
  const dualla = await user(browser, `Dualla ${suffix}`);

  await host.page.getByLabel('game name').fill(game);
  host.page = await openGame(host.page, (lobby) => lobby.getByRole('button', { name: 'Host a game' }).click());
  const url = host.page.url();
  for (const joiner of [roslin, starbuck]) {
    await joiner.page.goto(url);
    await joiner.page.getByRole('button', { name: 'Take a seat' }).click();
  }
  await dualla.page.goto(url);
  await dualla.page.getByRole('button', { name: 'Observe' }).click();
  // the room shows who is there: their room pages poll
  await expect(host.page.getByRole('region', { name: 'seats' }).getByRole('img', { name: 'here' })).toHaveCount(4);

  await host.page.getByRole('button', { name: 'Start with 3 players' }).click();
  for (const someone of [host, roslin, starbuck, dualla]) {
    await expect(someone.page.getByRole('region', { name: 'turn' })).toBeVisible();
  }
  await expect(host.page.locator('.player[data-player="3"]').getByRole('img', { name: 'connected' })).toBeVisible();

  // one chat for everyone at the table: a player's message, then an observer's, reach all of them
  await roslin.page.getByRole('button', { name: /^Chat/ }).click();
  await roslin.page.getByLabel('message').fill('So say we all');
  await roslin.page.getByLabel('message').press('Enter');
  await expect(host.page.getByRole('button', { name: /^Chat/ })).toContainText('1');
  await host.page.getByRole('button', { name: /^Chat/ }).click();
  await expect(host.page.getByRole('region', { name: 'chat' })).toContainText('So say we all');
  // the switch goes back to the log from the chat
  await expect(host.page.getByRole('button', { name: 'Logs' })).toBeVisible();
  await dualla.page.getByRole('button', { name: /^Chat/ }).click();
  await expect(dualla.page.getByRole('region', { name: 'chat' })).toContainText('So say we all');
  await dualla.page.getByLabel('message').fill('Starbuck looks nervous');
  await dualla.page.getByLabel('message').press('Enter');
  await expect(host.page.getByRole('region', { name: 'chat' })).toContainText('Starbuck looks nervous');
  await expect(roslin.page.getByRole('region', { name: 'chat' })).toContainText('observer');

  // Starbuck goes away: her mat says so, a vote and the host hand her seat to Dualla, who then sees it as her own
  await starbuck.context.close();
  await expect(host.page.locator('.player[data-player="3"]').locator('.presence-chip')).toHaveText('away', { timeout: 15_000 });
  await openSeats(roslin.page);
  await seat(roslin.page, 3).getByRole('button', { name: 'Vote to hand over' }).click();
  await expect(seat(host.page, 3)).toContainText('1 of 2');
  await openSeats(host.page);
  await seat(host.page, 3).getByRole('button', { name: 'Hand over' }).click();
  await expect(seat(dualla.page, 3)).toContainText('vacant');
  await seat(dualla.page, 3).getByRole('button', { name: 'Take this seat' }).click();
  await expect(dualla.page.locator('.player[data-player="3"] .you')).toBeVisible();
  await expect(host.page.locator('.player[data-player="3"] .user')).toHaveText(`Dualla ${suffix}`);

  // the host abandons the game: everyone gets the results of a game without a winner
  await openSeats(host.page);
  await host.page.getByRole('button', { name: 'Abandon the game' }).click();
  for (const someone of [host, roslin, dualla]) {
    await expect(someone.page.getByRole('dialog', { name: 'results' }).getByRole('heading', { name: 'The game was abandoned' })).toBeVisible();
  }
  await expect(host.page.getByRole('dialog', { name: 'results' })).toContainText(`Starbuck ${suffix}, then Dualla ${suffix}`);
  for (const someone of [host, roslin, dualla]) {
    await someone.context.close();
  }
});
