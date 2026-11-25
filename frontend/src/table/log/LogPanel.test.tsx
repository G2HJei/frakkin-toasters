import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { LogPanel } from './LogPanel';
import { useFlash } from '../flash';
import { useGameStore } from '../../live/gameStore';
import { Table, tableContext } from '../tableContext';
import type { GameProjection, LogEntry } from '../../wire/types';

const scope = (scopeId: number) => ({ kind: 'ScopeLog', scopeId, parentId: 0, actor: 1, event: 'NoOpEvent', details: null, facts: [], automatic: false }) as LogEntry;
const turnStarted = (scopeId: number, turn: number) =>
  ({ ...scope(scopeId), event: 'StartTurnEvent', facts: [{ kind: 'Shown', fact: { kind: 'GameFact.TurnStarted', turn } }] }) as LogEntry;

// jsdom lays nothing out: the log body is 100px high and its content as high as the test says, with a scroll position
// that clamps like a browser's
const layout = { scrollHeight: 500 };
const positions = new WeakMap<Element, number>();
const clientHeight = 100;
const patched = ['scrollHeight', 'clientHeight', 'scrollTop'] as const;
const originals = new Map<string, PropertyDescriptor | undefined>();

beforeEach(() => {
  layout.scrollHeight = 500;
  for (const name of patched) originals.set(name, Object.getOwnPropertyDescriptor(HTMLElement.prototype, name));
  Object.defineProperty(HTMLElement.prototype, 'scrollHeight', { configurable: true, get: () => layout.scrollHeight });
  Object.defineProperty(HTMLElement.prototype, 'clientHeight', { configurable: true, get: () => clientHeight });
  Object.defineProperty(HTMLElement.prototype, 'scrollTop', {
    configurable: true,
    get(this: Element) {
      return positions.get(this) ?? 0;
    },
    set(this: Element, value: number) {
      positions.set(this, Math.max(0, Math.min(value, layout.scrollHeight - clientHeight)));
    },
  });
});

afterEach(() => {
  for (const name of patched) {
    const original = originals.get(name);
    if (original) Object.defineProperty(HTMLElement.prototype, name, original);
    else delete (HTMLElement.prototype as unknown as Record<string, unknown>)[name];
  }
  useGameStore.getState().reset();
});

function renderLog(entries: LogEntry[], players = [{ number: 1, character: 'WILLIAM_ADAMA' }], sandbox = false, userNames: Partial<Record<number, string>> = {}) {
  useGameStore.setState({ log: entries });
  const game = { viewer: { kind: 'PlayerViewer', playerNumber: 1 }, players } as unknown as GameProjection;
  const container = render(
    <Table.Provider value={tableContext(game, sandbox, 1, userNames)}>
      <LogPanel />
    </Table.Provider>,
  ).container;
  return container.querySelector('.log-body') as HTMLElement;
}

// the lines' words
function lines(body: HTMLElement): string[] {
  return [...body.querySelectorAll('.lines > li .event')].map((line) => line.textContent ?? '');
}

// each line's player, or null, and its words
function rows(body: HTMLElement): [string | null, string][] {
  return [...body.querySelectorAll('.lines > li')].map((line) => [line.querySelector('.actor')?.textContent ?? null, line.querySelector('.event')?.textContent ?? '']);
}

function addEntry(entry: LogEntry, scrollHeight: number) {
  layout.scrollHeight = scrollHeight;
  act(() => useGameStore.setState((state) => ({ log: [...state.log, entry] })));
}

function scrollTo(body: HTMLElement, top: number) {
  body.scrollTop = top;
  fireEvent.scroll(body);
}

describe('the log', () => {
  it('has no title and keeps every turn open', () => {
    const body = renderLog([scope(1), turnStarted(2, 1), scope(3), turnStarted(4, 2), scope(5)]);
    const turns = [...body.querySelectorAll<HTMLDetailsElement>('details.turn')];

    expect(document.querySelector('.log .tab')).toBeNull();
    expect(turns.map((turn) => turn.querySelector('summary')?.textContent)).toEqual(['Setup', 'Turn 1 · William Adama', 'Turn 2 · William Adama']);
    expect(turns.every((turn) => turn.open)).toBe(true);
  });

  it('starts at the newest entry and follows the new ones', () => {
    const body = renderLog([scope(1), scope(2)]);

    expect(body.scrollTop).toBe(400);

    addEntry(scope(3), 600);

    expect(body.scrollTop).toBe(500);
    expect(screen.queryByRole('button', { name: 'Go to bottom' })).toBeNull();
  });

  it('stays where the user scrolled up to, and goes to the bottom on the button', () => {
    const body = renderLog([scope(1), scope(2)]);
    scrollTo(body, 120);

    addEntry(scope(3), 600);

    expect(body.scrollTop).toBe(120);

    fireEvent.click(screen.getByRole('button', { name: 'Go to bottom' }));

    expect(body.scrollTop).toBe(500);
    expect(screen.queryByRole('button', { name: 'Go to bottom' })).toBeNull();

    addEntry(scope(4), 700);

    expect(body.scrollTop).toBe(600);
  });

  it('keeps following when the end moves before the event of its own scroll arrives', () => {
    const body = renderLog([scope(1), scope(2)]);
    addEntry(scope(3), 600);

    // an image of the new entry loads before the browser reports the scroll to the end
    layout.scrollHeight = 650;
    fireEvent.scroll(body);

    expect(screen.queryByRole('button', { name: 'Go to bottom' })).toBeNull();

    addEntry(scope(4), 700);

    expect(body.scrollTop).toBe(600);
  });

  it('lights the arrow once new entries come below, until the user is at the end again', () => {
    const body = renderLog([scope(1), scope(2)]);
    scrollTo(body, 120);

    expect(screen.getByRole('button', { name: 'Go to bottom' })).not.toHaveClass('unseen');

    addEntry(scope(3), 600);

    expect(screen.getByRole('button', { name: 'Go to bottom' })).toHaveClass('unseen');

    fireEvent.click(screen.getByRole('button', { name: 'Go to bottom' }));
    scrollTo(body, 120);

    expect(screen.getByRole('button', { name: 'Go to bottom' })).not.toHaveClass('unseen');
  });

  it('follows again once the user scrolls back down to the end', () => {
    const body = renderLog([scope(1), scope(2)]);
    scrollTo(body, 0);

    expect(screen.getByRole('button', { name: 'Go to bottom' })).toBeInTheDocument();

    scrollTo(body, 396);

    expect(screen.queryByRole('button', { name: 'Go to bottom' })).toBeNull();

    addEntry(scope(3), 600);

    expect(body.scrollTop).toBe(500);
  });

  it('groups a turn by its steps, and shows a sandbox the facts under their line, what changes the table first', () => {
    const step = (scopeId: number, name: string) => ({ ...scope(scopeId), event: 'MovementStepEvent', facts: [{ kind: 'Shown', fact: { kind: 'GameFact.StepChanged', step: name } }] }) as LogEntry;
    const move = {
      ...scope(7),
      event: 'MoveEvent',
      details: { kind: 'MoveEvent', playerNumber: 1, destination: 'ARMORY', discardCard: null },
      facts: [
        { kind: 'Shown', fact: { kind: 'GameFact.TurnLimitUsed', playerNumber: 1, limit: 'CAG_PILOT_ACTION' } },
        { kind: 'Shown', fact: { kind: 'PlayerFact.CardsSeen', playerNumber: 1, deck: 'CRISIS', cards: ['RIOTS_NO_JUMP'] } },
        { kind: 'Shown', fact: { kind: 'PlayerFact.CharacterMoved', character: 'WILLIAM_ADAMA', to: 'ARMORY' } },
        { kind: 'Shown', fact: { kind: 'BoardFact.ResourceDecreased', resource: 'FOOD', amount: 1 } },
      ],
    } as LogEntry;
    const entries = [turnStarted(1, 1), step(2, 'MOVEMENT'), move, step(8, 'ACTION')];

    const body = renderLog(entries, undefined, true);
    // a step without a line has no header
    expect([...body.querySelectorAll('.step-name')].map((name) => name.textContent)).toEqual(['Movement']);
    expect(lines(body)).toEqual(['moves to the Armory']);
    // hidden until the sandbox asks for them
    expect(body.querySelector('.facts')).toBeNull();

    fireEvent.click(screen.getByRole('button', { name: 'Facts' }));

    // the move says what its line says, the turn limit keeps the books
    expect([...body.querySelectorAll('.facts li')].map((fact) => fact.textContent)).toEqual(['The fleet loses 1 food', 'looks at Riots No Jump']);
    // the destination's card shows on hover
    expect(body.querySelector('.event .named')?.textContent).toBe('Armory');
  });

  it('shows the facts only in a sandbox, through a switch outside the lines', () => {
    const lost = {
      ...scope(2),
      event: 'DrawMutinyCardEvent',
      details: { kind: 'DrawMutinyCardEvent', playerNumber: 1 },
      facts: [{ kind: 'Shown', fact: { kind: 'BoardFact.ResourceDecreased', resource: 'FOOD', amount: 1 } }],
    } as LogEntry;

    const game = renderLog([lost]);
    expect(screen.queryByRole('button', { name: 'Facts' })).toBeNull();
    expect(game.querySelector('.facts')).toBeNull();
    cleanup();

    const sandbox = renderLog([lost], undefined, true);
    const toggle = screen.getByRole('button', { name: 'Facts' });
    // over the log, not in its body, so it pushes no line down
    expect(sandbox.contains(toggle)).toBe(false);
    expect(toggle).toHaveAttribute('aria-pressed', 'false');

    fireEvent.click(toggle);

    expect(toggle).toHaveAttribute('aria-pressed', 'true');
    expect([...sandbox.querySelectorAll('.facts li')].map((fact) => fact.textContent)).toEqual(['The fleet loses 1 food']);

    fireEvent.click(toggle);

    expect(sandbox.querySelector('.facts')).toBeNull();
  });

  it("names the player in front of each of his lines, and nobody in front of the game's", () => {
    const draw = (scopeId: number, player: number) =>
      ({ ...scope(scopeId), actor: player, event: 'DrawMutinyCardEvent', details: { kind: 'DrawMutinyCardEvent', playerNumber: player } }) as LogEntry;
    const loss = { ...scope(4), event: 'LoseResourceEvent', facts: [{ kind: 'Shown', fact: { kind: 'BoardFact.ResourceDecreased', resource: 'MORALE', amount: 1 } }] } as LogEntry;
    const body = renderLog([draw(1, 1), draw(2, 1), draw(3, 2), loss, draw(5, 2)], [
      { number: 1, character: 'WILLIAM_ADAMA' },
      { number: 2, character: 'KARL_HELO_AGATHON_ALT' },
    ]);

    expect(rows(body)).toEqual([
      ['William Adama', 'draws a Mutiny Card'],
      ['William Adama', 'draws a Mutiny Card'],
      ['Karl "Helo" Agathon', 'draws a Mutiny Card'],
      [null, 'The fleet loses 1 morale'],
      ['Karl "Helo" Agathon', 'draws a Mutiny Card'],
    ]);
    // the token, then the name and the words on the same row
    const first = body.querySelector('.lines > li')!;
    expect(first.querySelector('.line-token img')?.getAttribute('data-art')).toBe('Character/player-token/WILLIAM_ADAMA');
    expect(first.querySelector('.actor')?.parentElement).toBe(first.querySelector('.event')?.parentElement);
    expect(body.querySelectorAll('.lines > li.game .line-token, .lines > li.game .actor')).toHaveLength(0);
  });

  it('tells a choice by the event it picked, under the player who picked it', () => {
    const area = { kind: 'ActivateRaiderAreaEvent', area: 'GALACTICA_SPACE_8_OCLOCK', raiderShipIds: [] };
    const choice = { kind: 'ChoiceLog', scopeId: 3, chooser: 1, option: { kind: 'Single', event: area } } as LogEntry;
    const picked = { ...scope(4), parentId: 3, event: 'ActivateRaiderAreaEvent', details: area } as LogEntry;
    const body = renderLog([choice, picked]);

    expect(rows(body)).toEqual([['William Adama', "activates the raiders at Space 8 o'clock"]]);
  });

  it('leaves out a pass on a card every player is asked about, and says what other passes passed on', () => {
    const total = { ...scope(10), event: 'TotalSkillCheckStrengthEvent' } as LogEntry;
    const movement = { ...scope(20), event: 'MovementStepEvent' } as LogEntry;
    const pass = (scopeId: number, parentId: number) => ({ ...scope(scopeId), parentId, details: { kind: 'NoOpEvent', playerNumber: 1 } }) as LogEntry;
    const body = renderLog([total, pass(11, 10), movement, pass(21, 20)]);

    expect(rows(body)).toEqual([['William Adama', "doesn't move"]]);
  });

  it('tells an event about the game by its facts, and joins ships that moved alike', () => {
    const raider = (scopeId: number, shipId: number) =>
      ({
        ...scope(scopeId),
        event: 'ActivateRaiderEvent',
        details: { kind: 'ActivateRaiderEvent', raiderShipId: shipId },
        facts: [{ kind: 'Shown', fact: { kind: 'ShipFact.ShipMoved', shipId, from: 'GALACTICA_SPACE', to: 'GALACTICA_SPACE', location: 'GALACTICA_SPACE_6_OCLOCK' } }],
      }) as LogEntry;
    const body = renderLog([raider(1, 31), raider(2, 32)]);

    // the table doesn't show them, but the activations named them as raiders
    expect(rows(body)).toEqual([[null, "Raider #31 and Raider #32 move to Space 6 o'clock"]]);
    expect([...body.querySelectorAll('.event .part-icon')].map((icon) => icon.getAttribute('data-icon'))).toEqual(['ShipType/RAIDER', 'ShipType/RAIDER']);
  });

  it('shows every line, and lights what a line touched on a click', () => {
    const other = { ...scope(2), actor: 2 } as LogEntry;
    const body = renderLog([scope(1), other]);

    expect(body.querySelectorAll('.lines > li')).toHaveLength(2);

    fireEvent.click(body.querySelector('.lines > li')!);
    expect([...useFlash.getState().keys]).toEqual([]);
  });

  it('marks an answer the server took because it was the only one', () => {
    const taken = { ...scope(2), automatic: true } as LogEntry;
    const body = renderLog([scope(1), taken]);

    const icons = body.querySelectorAll('.auto-icon');
    expect(icons).toHaveLength(1);
    // after the name, in front of the words it marks
    const line = body.querySelectorAll('.lines > li')[1];
    expect(icons[0].previousElementSibling).toBe(line.querySelector('.actor'));
    expect(icons[0].nextElementSibling).toBe(line.querySelector('.event'));
    expect(icons[0].getAttribute('aria-label')).toBe('automatic');
    expect(icons[0].getAttribute('title')).toBe('The only answer, taken automatically');
    // the answers' pass icon: it skipped ahead
    expect(icons[0].querySelector('svg path')?.getAttribute('d')).toBe('M3 5.5 11 12l-8 6.5zM11 5.5l8 6.5-8 6.5z');
  });

  it('words what happened to a ship, a Cylon ship destroyed as good news', () => {
    const destroyed = {
      ...scope(3),
      event: 'DestroyRaidersEvent',
      facts: [{ kind: 'Shown', fact: { kind: 'ShipFact.ShipMoved', shipId: 34, from: 'GALACTICA_SPACE', to: 'CYLON_SUPPLY', location: null } }],
    } as LogEntry;
    const body = renderLog([destroyed]);

    expect(lines(body)).toEqual(['ship #34 is destroyed']);
    expect(body.querySelector('.event .tone-good')?.textContent).toBe('destroyed');
  });

  it("puts a resource's icon in front of it, green when the fleet gains it and red when it loses it", () => {
    const resource = (scopeId: number, kind: string, resource: string) =>
      ({ ...scope(scopeId), event: 'LoseResourceEvent', facts: [{ kind: 'Shown', fact: { kind, resource, amount: 1 } }] }) as LogEntry;
    const body = renderLog([resource(1, 'BoardFact.ResourceDecreased', 'FOOD'), resource(2, 'BoardFact.ResourceIncreased', 'MORALE')]);
    const [lost, gained] = body.querySelectorAll('.lines > li');

    expect(lines(body)).toEqual(['The fleet loses 1 food', 'The fleet gains 1 morale']);
    expect([...lost.querySelectorAll('.tone-bad')].map((part) => part.textContent)).toEqual(['1 ', 'food']);
    expect(lost.querySelector('.tone-bad .part-icon.tint')?.getAttribute('data-icon')).toBe('ResourceIcon/FOOD');
    expect(gained.querySelector('.tone-good .part-icon.tint')?.getAttribute('data-icon')).toBe('ResourceIcon/MORALE');
  });

  it("colors a skill check's result: a pass or a partial pass green, a fail red", () => {
    const result = (scopeId: number, value: string) =>
      ({ ...scope(scopeId), event: 'DetermineSkillCheckResultEvent', facts: [{ kind: 'Shown', fact: { kind: 'SkillCheckFact.ResultDetermined', result: value } }] }) as LogEntry;
    const body = renderLog([result(1, 'PASS'), result(2, 'PARTIAL_PASS'), result(3, 'FAIL')]);

    expect([...body.querySelectorAll('.lines > li')].map((line) => [line.querySelector('.tone-good, .tone-bad')?.className, line.querySelector('.tone-good, .tone-bad')?.textContent])).toEqual([
      ['tone-good', 'passes'],
      ['tone-good', 'partially passes'],
      ['tone-bad', 'fails'],
    ]);
  });

  it('shows the die with the number it rolled', () => {
    const roll = { ...scope(1), event: 'RollDieEvent', facts: [{ kind: 'Shown', fact: { kind: 'GameFact.DieRolled', value: 6 } }] } as LogEntry;
    const body = renderLog([roll]);

    expect(lines(body)).toEqual(['The die shows 6']);
    expect(body.querySelector('.event img.part-icon.icon-die')?.getAttribute('data-icon')).toBe('Die/6');
  });

  it('shows no image after a line that its words name already', () => {
    const move = {
      ...scope(1),
      event: 'MoveEvent',
      details: { kind: 'MoveEvent', playerNumber: 1, destination: 'ARMORY', discardCard: { type: 'REPAIR', value: 1 } },
      facts: [{ kind: 'Shown', fact: { kind: 'PlayerFact.CardsSeen', playerNumber: 1, deck: 'CRISIS', cards: ['RIOTS_NO_JUMP'] } }],
    } as LogEntry;
    const body = renderLog([move]);
    const strip = [...body.querySelectorAll('.art-strip img')].map((image) => image.getAttribute('data-art'));

    expect([...body.querySelectorAll('.event .named')].map((name) => name.textContent)).toEqual(['Armory', 'Repair 1']);
    // the Crisis Card it looked at isn't in its words
    expect(strip).toEqual(['CrisisCard/RIOTS_NO_JUMP']);
  });

  it("shows no player's token after a line that it leads", () => {
    const admiral = { ...scope(1), event: 'MakeAdmiralEvent', facts: [{ kind: 'Shown', fact: { kind: 'TitleFact.AdmiralAssigned', character: 'WILLIAM_ADAMA' } }] } as LogEntry;
    const body = renderLog([admiral]);

    expect(rows(body)).toEqual([['William Adama', 'becomes Admiral']]);
    expect(body.querySelector('.line-token img')?.getAttribute('data-art')).toBe('Character/player-token/WILLIAM_ADAMA');
    expect(body.querySelector('.event .part-icon')?.getAttribute('data-icon')).toBe('TitleIcon/ADMIRAL');
    expect(body.querySelector('.art-strip')).toBeNull();
  });

  it('names the user in his seat when he chooses a character, and his seat without one', () => {
    const choose = { ...scope(1), event: 'SelectCharacterEvent', details: { kind: 'SelectCharacterEvent', playerNumber: 1, selectedCharacter: 'WILLIAM_ADAMA' } } as LogEntry;

    expect(rows(renderLog([choose], undefined, false, { 1: 'User123' }))).toEqual([['User123', 'chooses William Adama']]);

    cleanup();

    // a sandbox has nobody in its seats
    expect(rows(renderLog([choose], undefined, true))).toEqual([['Player 1', 'chooses William Adama']]);
  });

  it("puts the portrait of another player in front of his name, with his sheet on hover, and not the line's own", () => {
    const players = [
      { number: 1, character: 'WILLIAM_ADAMA' },
      { number: 2, character: 'LAURA_ROSLIN' },
    ];
    const moved = { kind: 'Shown', fact: { kind: 'PlayerFact.CharacterMoved', character: 'LAURA_ROSLIN', to: 'ARMORY' } };
    const leaves = { ...scope(1), event: 'MoveFromBrigEvent', details: { kind: 'MoveFromBrigEvent', targetPlayerNumber: 2, destination: 'ARMORY' }, facts: [moved] } as LogEntry;
    const sends = { ...scope(2), event: 'ChooseCharacterForBrigEvent', details: { kind: 'ChooseCharacterForBrigEvent', playerNumber: 1, targetPlayerNumber: 2 } } as LogEntry;
    const executed = { ...scope(3), event: 'EmotionallyFragileEvent', details: { kind: 'EmotionallyFragileEvent', playerNumber: 1 } } as LogEntry;
    const body = renderLog([leaves, sends, executed], players);
    const portraits = (line: Element) => [...line.querySelectorAll('.event img.icon-portrait')].map((image) => image.getAttribute('data-icon'));
    const named = (line: Element) => [...line.querySelectorAll('.event .named')].map((name) => name.textContent);
    const [left, sent, fragile] = [...body.querySelectorAll('.lines > li')];

    expect(rows(body)).toEqual([
      [null, 'Laura Roslin leaves the Brig for the Armory'],
      ['William Adama', 'sends Laura Roslin to the Brig'],
      ['William Adama', 'Morale is down to 2 or less: William Adama is executed'],
    ]);
    expect(portraits(left)).toEqual(['Character/player-token/LAURA_ROSLIN']);
    expect(named(left)).toEqual(['Laura Roslin', 'Brig', 'Armory']);
    expect(portraits(sent)).toEqual(['Character/player-token/LAURA_ROSLIN']);
    expect(named(sent)).toEqual(['Laura Roslin', 'Brig']);
    // his portrait leads the line already; his name keeps his sheet on hover
    expect(portraits(fragile)).toEqual([]);
    expect(named(fragile)).toEqual(['William Adama']);
    // a name keeps the color of the words around it
    expect(left.querySelector('.event .named')?.className).toBe('named');
  });

  it('colors a type of Skill Cards by its deck, and keeps a card a name to hover', () => {
    const selection = { kind: 'DrawSkillCardsEvent', playerNumber: 1, selection: { POLITICS: 1, TACTICS: 2 }, drawLimit: null };
    const draw = { ...scope(1), event: 'DrawSkillCardsEvent', details: selection } as LogEntry;
    const hidden = { kind: 'Hidden', playerNumber: 1, otherPlayerNumber: null, skillCheckPile: null, colors: { PILOTING: 2 } };
    const unseen = {
      ...scope(2),
      event: 'ResolveCrisisSectionThenEvent',
      facts: [
        { ...hidden, fact: 'DeckFact.CardsDrawn', deck: 'PILOTING', pile: null, count: 2 },
        { ...hidden, fact: 'PlayerFact.CardsGained', deck: null, pile: 'SKILL', count: 2 },
      ],
    } as LogEntry;
    const played = {
      ...scope(3),
      event: 'ConsolidatePowerEvent',
      details: { kind: 'ConsolidatePowerEvent', playerNumber: 1, card: { type: 'CONSOLIDATE_POWER', value: 1 } },
    } as LogEntry;
    const body = renderLog([draw, unseen, played]);
    const colored = (line: Element) => [...line.querySelectorAll('.event [class*="skill-type-"]')].map((type) => [type.className, type.textContent]);
    const [drew, drewUnseen, play] = [...body.querySelectorAll('.lines > li')];

    expect(rows(body)).toEqual([
      ['William Adama', 'draws 1 Politics and 2 Tactics cards'],
      ['William Adama', 'draws 2 Piloting cards'],
      ['William Adama', 'plays Consolidate Power 1'],
    ]);
    expect(colored(drew)).toEqual([
      ['skill-type-politics', 'Politics'],
      ['skill-type-tactics', 'Tactics'],
    ]);
    expect(colored(drewUnseen)).toEqual([['skill-type-piloting', 'Piloting']]);
    expect(colored(play)).toEqual([]);
    expect(play.querySelector('.event .named')?.textContent).toBe('Consolidate Power 1');
  });

  it('tells a tiebreak nobody was asked about under the current player', () => {
    const players = [
      { number: 1, character: 'WILLIAM_ADAMA' },
      { number: 2, character: 'LAURA_ROSLIN' },
    ];
    const kept = { kind: 'TiebreakCardsBeforeSkillCheckEvent', keptCards: [{ playerNumber: 2, skillCard: { type: 'INVESTIGATIVE_COMMITTEE', value: 3 } }] };
    const body = renderLog([turnStarted(1, 4), { ...scope(2), event: 'TiebreakCardsBeforeSkillCheckEvent', details: kept } as LogEntry], players);

    expect(rows(body)).toEqual([['William Adama', "keeps Laura Roslin's Investigative Committee 3"]]);
  });

  it('says what damage a player chose for Galactica, in red, and pictures no damage token', () => {
    const players = [
      { number: 1, character: 'WILLIAM_ADAMA' },
      { number: 2, character: 'LAURA_ROSLIN' },
    ];
    const damaged = { kind: 'Shown', fact: { kind: 'BoardFact.LocationDamaged', location: 'ARMORY' } };
    const chose = {
      ...scope(1),
      event: 'ChooseGalacticaDamageTokensEvent',
      details: { kind: 'ChooseGalacticaDamageTokensEvent', playerNumber: 2, resolved: ['ARMORY', 'FUEL'], returned: ['COMMAND'] },
      facts: [damaged],
    } as LogEntry;
    const body = renderLog([chose], players);

    expect(rows(body)).toEqual([['Laura Roslin', 'chooses to damage the Armory and lose 1 fuel']]);
    expect([...body.querySelectorAll('.event .tone-bad')].map((part) => part.textContent)).toEqual(['damage', '1 ', 'fuel']);
    expect([...body.querySelectorAll('.art-strip img')].map((image) => image.getAttribute('data-art')).filter((art) => art?.startsWith('GalacticaDamage/'))).toEqual([]);
  });
});
