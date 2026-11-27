import { expect, test, type Browser, type BrowserContext, type Page } from '@playwright/test';
import { openGame } from './tabs';

// Logs a new browser in through the dev login, as the login page does, and opens the games. Its page becomes the game's
// tab once it opens one.
async function user(browser: Browser, name: string): Promise<{ context: BrowserContext; page: Page }> {
  const context = await browser.newContext();
  const page = await context.newPage();
  await page.goto('/login');
  await page.getByLabel('name').fill(name);
  await page.getByRole('button', { name: 'Log in (dev)' }).click();
  await expect(page.getByRole('link', { name: 'New sandbox' })).toBeVisible();
  return { context, page };
}

function lobbyEntry(page: Page, list: string, game: string) {
  return page.getByRole('region', { name: list }).getByRole('listitem').filter({ hasText: game });
}

test('players host, join, observe, leave and start a game, and nobody sees another player\'s cards', async ({ browser }) => {
  const suffix = `${Date.now()}`;
  const game = `Lobby ${suffix}`;
  const host = await user(browser, `Adama ${suffix}`);
  const second = await user(browser, `Roslin ${suffix}`);
  const third = await user(browser, `Starbuck ${suffix}`);
  const observer = await user(browser, `Dualla ${suffix}`);
  const watcher = await user(browser, `Gaeta ${suffix}`);

  await host.page.getByLabel('game name').fill(game);
  host.page = await openGame(host.page, (lobby) => lobby.getByRole('button', { name: 'Host a game' }).click());
  await expect(host.page.getByRole('heading', { name: game })).toBeVisible();

  // the others find it among the open lobbies, which poll
  for (const joiner of [second, third]) {
    await joiner.page.goto('/play');
    joiner.page = await openGame(joiner.page, (lobby) => lobbyEntry(lobby, 'Open lobbies', game).getByRole('button', { name: 'Join' }).click());
    await expect(joiner.page.getByRole('heading', { name: game })).toBeVisible();
  }
  await third.page.getByRole('button', { name: 'Leave' }).click();
  await expect(host.page.getByRole('region', { name: 'seats' })).not.toContainText(`Starbuck ${suffix}`);
  await third.page.getByRole('button', { name: 'Take a seat' }).click();
  await observer.page.goto('/play');
  observer.page = await openGame(observer.page, (lobby) => lobbyEntry(lobby, 'Open lobbies', game).getByRole('button', { name: 'Observe' }).click());
  await expect(host.page.getByRole('region', { name: 'seats' })).toContainText(`Dualla ${suffix}`);

  await host.page.getByRole('button', { name: 'Start with 3 players' }).click();
  for (const seat of [host, second, third, observer]) {
    await expect(seat.page.getByRole('region', { name: 'turn' })).toBeVisible();
  }
  // a running game shows under "Watch" for anyone else
  await watcher.page.goto('/play');
  await expect(lobbyEntry(watcher.page, 'Watch', game)).toBeVisible();

  // the characters, picked by whoever is asked, through the UI
  const players = [host, second, third];
  const characters = ['WILLIAM_ADAMA', 'LAURA_ROSLIN', 'KARA_STARBUCK_THRACE'];
  for (const character of characters) {
    const asked = await whoIsAsked(players);
    for (const other of players.filter((player) => player !== asked)) {
      await expect(other.page.getByRole('region', { name: 'input' }).getByRole('heading')).toContainText('Waiting for');
    }
    await asked.page.getByRole('region', { name: 'input' }).getByRole('button', { name: answerName(character), exact: true }).click();
    // a mat names its character first: the user names, e.g. "Starbuck …", show under it
    await expect(asked.page.getByRole('region', { name: 'players' }).locator('.who strong', { hasText: characterName(character) })).toHaveCount(1);
  }

  // after the starting Skill Cards, each player sees his own cards and only how many the others hold, not their colors
  const asked = await whoIsAsked(players);
  await expect(asked.page.getByRole('region', { name: 'input' })).toContainText('Draw 3 cards');
  for (const [index, seat] of players.entries()) {
    const others = seat.page.locator(`.player:not([data-player="${index + 1}"])`);
    await expect(others.locator('.tile:not(.back)')).toHaveCount(0);
    await expect(others.locator('.hand [class*=" back-"]')).toHaveCount(0);
  }
  // an observer never gets an input
  await expect(observer.page.getByRole('region', { name: 'input' }).getByRole('button')).toHaveCount(0);
  await expect(observer.page.getByRole('region', { name: 'input' }).locator('select, input')).toHaveCount(0);

  for (const seat of [host, second, third, observer, watcher]) {
    await seat.context.close();
  }
});

test('two users race for the last seat, and one of them gets it', async ({ browser }) => {
  const suffix = `${Date.now()}`;
  const game = `Race ${suffix}`;
  const host = await user(browser, `Host ${suffix}`);
  await host.page.getByLabel('game name').fill(game);
  host.page = await openGame(host.page, (lobby) => lobby.getByRole('button', { name: 'Host a game' }).click());
  await expect(host.page.getByRole('heading', { name: game })).toBeVisible();
  const gameUrl = host.page.url();
  for (let seat = 2; seat <= 6; seat++) {
    const player = await user(browser, `Pilot ${seat} ${suffix}`);
    await player.page.goto(gameUrl);
    await player.page.getByRole('button', { name: 'Take a seat' }).click();
    await expect(host.page.getByRole('region', { name: 'seats' })).toContainText(`Pilot ${seat} ${suffix}`);
    await player.context.close();
  }
  const racers = [await user(browser, `Kat ${suffix}`), await user(browser, `Hotdog ${suffix}`)];
  for (const racer of racers) {
    await racer.page.goto(gameUrl);
  }

  await Promise.all(racers.map((racer) => racer.page.getByRole('button', { name: 'Take a seat' }).click()));

  await expect(host.page.locator('.seats li.taken')).toHaveCount(7);
  // the other racer is told why
  await expect
    .poll(async () => (await Promise.all(racers.map((racer) => racer.page.getByText('The game is full').count()))).reduce((a, b) => a + b, 0))
    .toBe(1);
  const refused = await Promise.all(racers.map((racer) => racer.page.getByText('The game is full').count()));
  expect(refused.sort()).toEqual([0, 1]);
  for (const seat of [host, ...racers]) {
    await seat.context.close();
  }
});

async function whoIsAsked<T extends { page: Page }>(players: T[]): Promise<T> {
  await expect
    .poll(async () => {
      for (const player of players) {
        if ((await player.page.getByRole('region', { name: 'input' }).getByRole('group', { name: 'answers' }).count()) > 0) {
          return true;
        }
      }
      return false;
    })
    .toBeTruthy();
  for (const player of players) {
    if ((await player.page.getByRole('region', { name: 'input' }).getByRole('group', { name: 'answers' }).count()) > 0) {
      return player;
    }
  }
  throw new Error('Nobody is asked');
}

function answerName(constant: string): string {
  return { WILLIAM_ADAMA: 'William Adama', LAURA_ROSLIN: 'Laura Roslin', KARA_STARBUCK_THRACE: 'Kara "Starbuck" Thrace' }[constant] ?? constant;
}

function characterName(constant: string): string {
  return { WILLIAM_ADAMA: 'William Adama', LAURA_ROSLIN: 'Laura Roslin', KARA_STARBUCK_THRACE: 'Starbuck' }[constant] ?? constant;
}
