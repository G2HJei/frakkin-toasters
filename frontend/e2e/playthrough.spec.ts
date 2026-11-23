import { expect, test, type Locator, type Page } from '@playwright/test';
import { openGame } from './tabs';

// The exit of milestone C (roadmap.md): a seeded four-player sandbox plays from setup to an ending through the
// answers alone (the options, the answers by kind, the counters and the pickers), never through the event builder
// under "Advanced". The answers are picked at random from a seeded generator, so a run is the same game every time.
const SEED = Number(process.env.PLAYTHROUGH_SEED ?? 7);
const MAX_PAUSES = 4000;

// A click that never finds its element fails the run instead of waiting for the test's timeout. No trace: a whole game
// is thousands of actions on a large page, and writing their snapshots takes longer than the game.
test.use({ actionTimeout: 20_000, trace: 'off' });

test('a seeded sandbox plays to an ending through the answers alone', async ({ page: lobby }) => {
  test.setTimeout(30 * 60_000);
  await lobby.goto('/login');
  await lobby.getByLabel('name').fill(`Playthrough ${Date.now()}`);
  await lobby.getByRole('button', { name: 'Log in (dev)' }).click();
  await lobby.getByRole('link', { name: 'New sandbox' }).click();
  await lobby.getByLabel('players').selectOption('4');
  await lobby.getByLabel('seed').fill(String(SEED));
  const page = await openGame(lobby, (from) => from.getByRole('button', { name: 'Start the sandbox' }).click());

  const random = generator(SEED);
  let rejected = 0;
  let pauses = 0;
  for (; pauses < MAX_PAUSES; pauses++) {
    await expect(input(page)).toHaveAttribute('aria-busy', 'false');
    if ((await input(page).getByRole('heading').textContent()) === 'The game is over') {
      break;
    }
    // every input has answers: "Advanced" opens by itself only for one that has none
    await expect(input(page).locator('details.advanced[open]')).toHaveCount(0);
    const before = await signature(page);
    await answer(page, random);
    await expect.poll(() => signature(page), { timeout: 15_000 }).not.toBe(before);
    if ((await input(page).getByRole('alert').count()) > 0) {
      // a picker's filling the engine refused: the same input asks again
      rejected++;
    }
  }

  await expect(input(page).getByRole('heading')).toHaveText('The game is over');
  await expect(input(page).locator('.outcome')).toHaveText(/^The (humans|Cylons) win/);
  console.log(`seed ${SEED}: ${pauses} pauses, ${rejected} refused fillings, ${await input(page).locator('.outcome').textContent()}`);
  expect(rejected).toBeLessThan(pauses / 10 + 5);
});

function input(page: Page): Locator {
  return page.getByRole('region', { name: 'input' });
}

// what changes when the game moves on, or refuses an answer: the log too, because the next input can look like the
// last, e.g. the second hit of "A Guilty Verdict" asks again which battlestar
async function signature(page: Page): Promise<string> {
  return page.evaluate(() => {
    const panel = document.querySelector('[aria-label="input"]');
    const entries = document.querySelectorAll('[aria-label="log"] li.scope, [aria-label="log"] li.choice').length;
    return `${panel?.getAttribute('aria-busy')}|${entries}|${panel?.innerHTML ?? ''}`;
  });
}

// mulberry32, in exact 32-bit integers: a float LCG loses its low bits past 2^53, and then gives the same number for
// every n that is a small power of 2, e.g. the same one of 8 picker values over and over
function generator(seed: number): (n: number) => number {
  let state = (seed * 7919 + 17) >>> 0;
  return (n) => {
    state = (state + 0x6d2b79f5) >>> 0;
    let t = Math.imul(state ^ (state >>> 15), state | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return Math.floor((((t ^ (t >>> 14)) >>> 0) / 4294967296) * n);
  };
}

// One answer: now and then another section of a choice, then an answer at random; a counter is counted up to an answer,
// an answer of parts built part by part and a picker filled until it may be sent.
async function answer(page: Page, random: (n: number) => number) {
  const answers = input(page).getByRole('group', { name: 'answers' });
  const heads = answers.locator('button.section-head');
  const headCount = await heads.count();
  if (headCount > 1 && random(3) === 0) {
    const head = heads.nth(random(headCount));
    if ((await head.getAttribute('aria-expanded')) === 'false') {
      await head.click();
    }
  }
  // a part's value only picks, it sends nothing
  const candidates = answers.locator('button.answer, button.chip:not(.part-value), .options > button.option:not(.section-head)');
  const count = await candidates.count();
  const counters = answers.locator('.counts-widget');
  if ((await counters.count()) > 0 && (count === 0 || random(2) === 0)) {
    await countUp(counters.first(), random);
    return;
  }
  const builders = answers.locator('.parts-widget');
  const builderCount = await builders.count();
  if (builderCount > 0 && (count === 0 || random(2) === 0)) {
    await build(builders.nth(random(builderCount)), random);
    await fillPicker(answers, random);
    return;
  }
  if (count === 0) {
    // every section is closed: open the first
    await heads.first().click();
    return answer(page, random);
  }
  await candidates.nth(random(count)).click();
  await fillPicker(answers, random);
}

// the picker a shape opened
async function fillPicker(answers: Locator, random: (n: number) => number) {
  const picker = answers.locator('.pick-widget');
  if ((await picker.count()) > 0) {
    await fill(picker, random);
  }
}

// an answer of parts: an open value at random until the answer may be sent, or until the answers the picks name differ
// only in a shape, one of which is picked; a dead end starts over
async function build(builder: Locator, random: (n: number) => number) {
  const send = builder.locator('.actions button.primary');
  for (let step = 0; step < 40 && !(await send.isEnabled()); step++) {
    const named = builder.locator('.part-moves button.chip');
    const namedCount = await named.count();
    if (namedCount > 0) {
      await named.nth(random(namedCount)).click();
      return;
    }
    const open = builder.locator('button.part-value[aria-pressed="false"]:enabled');
    const n = await open.count();
    if (n === 0) {
      await builder.getByRole('button', { name: 'Clear' }).click();
      continue;
    }
    await open.nth(random(n)).click();
  }
  // the picks changed the panel: an answer not sent yet is asked again
  if (await send.isEnabled()) {
    await send.click();
  }
}

// the counts per color: a click at random on a color that takes one more, until the counts are an answer
async function countUp(counter: Locator, random: (n: number) => number) {
  const send = counter.locator('button.primary');
  for (let step = 0; step < 20 && !(await send.isEnabled()); step++) {
    const more = counter.locator('.count-key:enabled:not([data-full])');
    const n = await more.count();
    if (n === 0) {
      await counter.getByRole('button', { name: 'Clear' }).click();
      continue;
    }
    await more.nth(random(n)).click();
  }
  await send.click();
}

// a picker: tiles at random until it may be sent; one that can't be filled goes back to the answers
async function fill(picker: Locator, random: (n: number) => number) {
  const send = picker.getByRole('button', { name: 'Send' });
  const tiles = picker.locator('.card-pick');
  for (let step = 0; step < 40 && !(await send.isEnabled()); step++) {
    const free = picker.locator('.card-pick[aria-pressed="false"]');
    const n = await free.count();
    if (n === 0) {
      break;
    }
    await free.nth(random(n)).click();
    // once the bounds are met, stop at random so the counts vary
    if ((await send.isEnabled()) && random(2) === 0) {
      break;
    }
  }
  if (await send.isEnabled()) {
    await send.click();
  } else {
    await tiles.first().page().getByRole('button', { name: 'Back to the answers' }).click();
  }
}
