import { expect, test, type Page } from '@playwright/test';
import { openGame } from './tabs';

// A seeded sandbox from the start, in a tab of its own: the characters for every seat, a full turn with a stacked
// Crisis Card, undo, export and import.
test('a seeded sandbox is played through a turn, undone, exported and imported', async ({ page: lobby }) => {
  await lobby.goto('/login');
  await lobby.getByLabel('name').fill(`E2E ${Date.now()}`);
  await lobby.getByRole('button', { name: 'Log in (dev)' }).click();
  await expect(lobby.getByRole('link', { name: 'New sandbox' })).toBeVisible();

  await lobby.getByRole('link', { name: 'New sandbox' }).click();
  await lobby.getByLabel('players').selectOption('4');
  await lobby.getByLabel('seed').fill('42');
  let page = await openGame(lobby, (from) => from.getByRole('button', { name: 'Start the sandbox' }).click());
  // the first tab goes back to the lobby
  await expect(lobby.getByRole('link', { name: 'New sandbox' })).toBeVisible();
  // the seed seats the first player, before anyone has a character
  await expect(input(page).getByRole('heading')).toHaveText(/^Player [1-4] decides$/);

  // the characters, one per seat, answered by the sandbox's one user
  for (const character of ['William Adama', 'Laura Roslin', 'Kara "Starbuck" Thrace', 'Saul Tigh']) {
    await answer(page, character);
  }
  // the starting Skill Cards of everyone but the first player
  await draw(page, { POLITICS: 3 });
  await draw(page, { TACTICS: 2, PILOTING: 1 });
  await draw(page, { LEADERSHIP: 2, TACTICS: 1 });

  // turn 1: Adama draws, moves to the Research Lab, stacks a Crisis Card, draws there, and resolves the stacked card
  // his skill set leaves one draw, which the game takes for him and logs as taken
  await expectTurn(page, 'T1 · William Adama', 'Movement');
  // the line after its player's name
  const automatic = page.getByRole('region', { name: 'log' }).locator('.scope', { has: page.locator('.auto-icon') }).last();
  await expect(automatic).toContainText('draws');
  await expect(automatic.locator('.actor')).toHaveText('William Adama');
  // the Movement step's move opens on a click: its destinations are the answers
  await openSection(page, 'Move');
  await answer(page, 'Research Lab');
  await page.getByRole('region', { name: 'sandbox' }).locator('summary', { hasText: 'Setup' }).click();
  await page.getByLabel('setup').selectOption('PutCrisisCardOnTopEvent');
  await page.getByRole('region', { name: 'sandbox' }).getByLabel('card').selectOption('RIOTS_NO_JUMP');
  await page.getByRole('button', { name: 'Inject' }).click();
  // the sandbox shows the Crisis deck's draw pile on hover, top first
  await page.locator('[data-reveal="deck-CRISIS"]').hover();
  await expect(page.locator('.reveal-pop ol.pile li img').first()).toHaveAttribute('alt', 'Riots No Jump');
  // the Action step's action opens on a click, and the Research Lab's row on another: its colors
  await openSection(page, 'Take an action');
  await openGroup(page, 'Research Lab');
  await answer(page, 'Engineering');
  // "Secret Destiny": Starbuck lets the revealed card resolve
  await input(page).getByRole('button', { name: /Let Riots No Jump resolve/ }).click();
  await input(page).getByRole('button', { name: /top option of Riots No Jump/ }).click();

  await expectTurn(page, 'T2 · Laura Roslin', 'Movement');
  await expect(resource(page, 'Food')).toHaveAttribute('aria-label', 'Food: 7');
  await expect(resource(page, 'Morale')).toHaveAttribute('aria-label', 'Morale: 9');

  // undo goes back to the section, and the same answer gives the same game
  await page.getByRole('button', { name: 'Undo' }).click();
  await expectTurn(page, 'T1 · William Adama', 'Crisis');
  await expect(resource(page, 'Food')).toHaveAttribute('aria-label', 'Food: 8');
  await input(page).getByRole('button', { name: /top option of Riots No Jump/ }).click();
  await expectTurn(page, 'T2 · Laura Roslin', 'Movement');
  await expect(resource(page, 'Food')).toHaveAttribute('aria-label', 'Food: 7');

  // export, then import as a new sandbox: the same game
  const id = page.url().split('/').pop();
  const exported = await page.request.get(`/api/games/${id}/journal`);
  expect(exported.ok()).toBeTruthy();
  await lobby.goto('/sandbox/new');
  const file = { name: 'sandbox.json', mimeType: 'application/json', buffer: await exported.body() };
  page = await openGame(lobby, (from) => from.getByLabel('journal file').setInputFiles(file));
  await expectTurn(page, 'T2 · Laura Roslin', 'Movement');
  await expect(resource(page, 'Food')).toHaveAttribute('aria-label', 'Food: 7');
  await expect(input(page).getByRole('heading')).toHaveText('Laura Roslin chooses');
});

function input(page: Page) {
  return page.getByRole('region', { name: 'input' });
}

// clicks an answer, and waits for the game to ask the next thing
async function answer(page: Page, name: string) {
  const before = await page.evaluate(() => document.querySelector('[aria-label="input"]')?.innerHTML ?? '');
  await input(page).getByRole('group', { name: 'answers' }).getByRole('button', { name, exact: true }).click();
  await expect(input(page).getByRole('alert')).toHaveCount(0);
  await expect.poll(async () => page.evaluate(() => document.querySelector('[aria-label="input"]')?.innerHTML ?? '')).not.toBe(before);
}

// opens a choice's section, which starts closed, so its answers show. Its name has its icon's too, so its words find it
async function openSection(page: Page, name: string) {
  const head = input(page).getByRole('group', { name: 'answers' }).locator('button.section-head', { hasText: name });
  await expect(head).toHaveAttribute('aria-expanded', 'false');
  await head.click();
  await expect(head).toHaveAttribute('aria-expanded', 'true');
}

// opens a group's row among the answers: its answers come next
async function openGroup(page: Page, name: string) {
  await input(page).getByRole('group', { name: 'answers' }).locator('button.group-row').getByText(name, { exact: true }).click();
  await expect(input(page).locator('.answer-group .group-title', { hasText: name })).toBeVisible();
}

// a draw counts the cards of each color, from what every answer has (e.g. Laura Roslin's 1 Politics of 3), a click on
// its color per card more, then draws
async function draw(page: Page, counts: Record<string, number>) {
  const names: Record<string, string> = { POLITICS: 'Politics', LEADERSHIP: 'Leadership', TACTICS: 'Tactics', PILOTING: 'Piloting', ENGINEERING: 'Engineering' };
  for (const [color, count] of Object.entries(counts)) {
    const key = input(page).locator('.counts-widget').getByRole('button', { name: names[color], exact: true });
    await expect(key).toBeVisible();
    for (let at = await key.locator('.pip.on').count(); at < count; at++) {
      await key.click();
    }
  }
  await answer(page, 'Draw Skill Cards');
}

// the turn's line (its number, the current character and the acting one when it's another) and the step it is at
async function expectTurn(page: Page, line: string, step: string) {
  const turn = page.getByRole('region', { name: 'turn' });
  // the heading sets the turn and the character apart by their styles alone
  await expect(turn.getByRole('heading')).toHaveText(line.replace(' · ', ''));
  await expect(turn.locator('.steps [aria-current="step"]')).toHaveText(step);
}

// a resource's gauge on the board, which names its amount
function resource(page: Page, name: string) {
  return page.locator(`svg.board-svg [data-gauge="${name.toLowerCase()}"]`);
}
