import { expect, test, type Page } from '@playwright/test';

// A seeded sandbox from the start: the characters for every seat, a full turn with a stacked Crisis Card, undo, export
// and import.
test('a seeded sandbox is played through a turn, undone, exported and imported', async ({ page }) => {
  await page.goto('/login');
  await page.getByLabel('name').fill(`E2E ${Date.now()}`);
  await page.getByRole('button', { name: 'Log in (dev)' }).click();
  await expect(page.getByRole('link', { name: 'New sandbox' })).toBeVisible();

  await page.getByRole('link', { name: 'New sandbox' }).click();
  await page.getByLabel('players').selectOption('4');
  await page.getByLabel('seed').fill('42');
  await page.getByRole('button', { name: 'Start the sandbox' }).click();
  await expect(input(page).getByRole('heading')).toHaveText('Player 1 decides');

  // the characters, one per seat, answered by the sandbox's one user
  for (const character of ['WILLIAM_ADAMA', 'LAURA_ROSLIN', 'KARA_STARBUCK_THRACE', 'SAUL_TIGH']) {
    await input(page).getByLabel('selected character').selectOption(character);
    await send(page);
  }
  // the starting Skill Cards of everyone but the first player
  await draw(page, { POLITICS: 3 });
  await draw(page, { TACTICS: 2, PILOTING: 1 });
  await draw(page, { LEADERSHIP: 2, TACTICS: 1 });

  // turn 1: Adama draws, moves to the Research Lab, stacks a Crisis Card, draws there, and resolves the stacked card
  await expect(page.getByRole('region', { name: 'turn' }).getByRole('heading')).toHaveText('Turn 1 · Receive Skills');
  await draw(page, { LEADERSHIP: 3, TACTICS: 2 });
  await input(page).getByRole('button', { name: 'Move' }).click();
  await input(page).getByLabel('destination').selectOption('RESEARCH_LAB');
  await send(page);
  await page.getByRole('region', { name: 'sandbox' }).locator('summary', { hasText: 'Setup' }).click();
  await page.getByLabel('setup').selectOption('PutCrisisCardOnTopEvent');
  await page.getByRole('region', { name: 'sandbox' }).getByLabel('card').selectOption('RIOTS_NO_JUMP');
  await page.getByRole('button', { name: 'Inject' }).click();
  // the sandbox shows the Crisis deck's draw pile on hover, top first
  await page.locator('[data-reveal="deck-CRISIS"]').hover();
  await expect(page.locator('.reveal-pop ol.pile li').first()).toHaveText('Riots No Jump');
  await input(page).getByRole('button', { name: 'Take an action' }).click();
  await input(page).getByLabel('skill card color').selectOption('ENGINEERING');
  await send(page);
  await input(page).getByRole('button', { name: /Top of Riots No Jump/ }).click();

  await expect(page.getByRole('region', { name: 'turn' }).getByRole('heading')).toHaveText('Turn 2 · Receive Skills');
  await expect(resource(page, 'Food')).toHaveText('7');
  await expect(resource(page, 'Morale')).toHaveText('9');

  // undo goes back to the section, and the same answer gives the same game
  await page.getByRole('button', { name: 'Undo' }).click();
  await expect(page.getByRole('region', { name: 'turn' }).getByRole('heading')).toHaveText('Turn 1 · Crisis');
  await expect(resource(page, 'Food')).toHaveText('8');
  await input(page).getByRole('button', { name: /Top of Riots No Jump/ }).click();
  await expect(page.getByRole('region', { name: 'turn' }).getByRole('heading')).toHaveText('Turn 2 · Receive Skills');
  await expect(resource(page, 'Food')).toHaveText('7');

  // export, then import as a new sandbox: the same game
  const id = page.url().split('/').pop();
  const exported = await page.request.get(`/api/games/${id}/journal`);
  expect(exported.ok()).toBeTruthy();
  await page.goto('/sandbox/new');
  await page.getByLabel('journal file').setInputFiles({ name: 'sandbox.json', mimeType: 'application/json', buffer: await exported.body() });
  await expect(page).not.toHaveURL(/sandbox\/new/);
  await expect(page.getByRole('region', { name: 'turn' }).getByRole('heading')).toHaveText('Turn 2 · Receive Skills');
  await expect(resource(page, 'Food')).toHaveText('7');
  await expect(input(page).getByRole('heading')).toHaveText('Laura Roslin decides');
});

function input(page: Page) {
  return page.getByRole('region', { name: 'input' });
}

async function send(page: Page) {
  const before = await input(page).getByRole('heading').textContent();
  await input(page).getByRole('button', { name: 'Send' }).click();
  await expect(input(page).getByRole('alert')).toHaveCount(0);
  // the next game came when the question changed, or the same player is asked something new
  await expect.poll(async () => (await input(page).getByRole('heading').textContent()) !== before || (await input(page).getByRole('button', { name: 'Send' }).count()) === 0).toBeTruthy();
}

async function draw(page: Page, counts: Record<string, number>) {
  for (const [color, count] of Object.entries(counts)) {
    await input(page).getByLabel(color, { exact: true }).fill(String(count));
  }
  await send(page);
}

function resource(page: Page, name: string) {
  return page.locator('svg.board-svg .dial', { hasText: name.toLowerCase() }).locator('.dial-value');
}
