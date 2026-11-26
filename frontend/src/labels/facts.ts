// The facts of a scope as sentences. The journal records the mechanics one card move at a time (a card leaves a deck,
// then a hand gets it); here a move that takes two facts is one sentence ("Laura Roslin draws Consolidate Power 2"),
// moves of the same kind in a row are one ("discards A, B and C"), and what only keeps the books is left out.
import {
  cardParts,
  cardType,
  capitalizedParts,
  deckNamePart,
  deckParts,
  factParts,
  hiddenCardsParts,
  holderPart,
  joinParts,
  partsText,
  playerPart,
  shipMove,
  shipMovesParts,
  shipPart,
  SKILL_COLORS,
  withoutSubject,
  type LabelContext,
  type Part,
  type ShipMove,
} from './describe';
import { recordNamed, type TypeRef } from '../wire/schema';
import type { FactProjection } from '../wire/types';

/** A fact as one line under its event. */
export interface Sentence {
  parts: Part[];
  // the player it is about, whom a line under his name leaves out
  subject: number | null;
  // the cards it moves out of a hand or into a skill check, to drop it when the event names them already
  given: unknown[];
  // the facts it tells, for what changed on the table first
  facts: FactProjection[];
  // ships that moved alike, which the next such sentence may join
  ships?: { names: Part[]; move: ShipMove };
}

/** What the facts of a scope are worded with: the labels' context, which can name a character's player. */
export type FactContext = LabelContext;

// Facts that only keep the books, or that a header or the event's line says already.
const QUIET = new Set([
  'GameFact.TurnStarted',
  'GameFact.StepChanged',
  'GameFact.CurrentPlayerChanged',
  // the line under him says what he does
  'GameFact.ActingPlayerChanged',
  'GameFact.DieRollStarted',
  'GameFact.CrisisCardsCleared',
  'GameFact.TurnLimitUsed',
  'GameFact.StartLocationNoted',
  'GameFact.ActionCostPaid',
  'GameFact.MoraleLostToExecution',
  'PlayerFact.OwnTurnStarted',
  'PlayerFact.CharacterSelected',
  'PlayerFact.AbilityTriggered',
  'PlayerFact.AbilityTriggerCleared',
  'PlayerFact.CivilianShipForgotten',
  'ShipFact.BasestarDamageCleared',
  'InterruptFact.InterruptWindowOpened',
  // "uses Hotshot" says it: the ability's use is recorded once more when the window resolves it and on the roll
  'InterruptFact.RollAbilityResolved',
  'GameFact.DieRollAbilityUsed',
  'InterruptFact.InterruptWindowClosed',
  'SkillCheckFact.CardsDivided',
  'SkillCheckFact.AbilityResolved',
  'SkillCheckFact.EmergencyDeclared',
  'SkillCheckFact.SkillCheckEnded',
]);

// The damage token piles: the damage a token does says what it was.
const DAMAGE_DECKS = new Set(['GALACTICA_DAMAGE', 'PEGASUS_DAMAGE', 'BASESTAR_DAMAGE']);

/** Whether a fact says nothing a reader wants: bookkeeping, or what a header says. */
export function isQuiet(fact: FactProjection): boolean {
  const kind = factKind(fact);
  if (kind.startsWith('DeckFact.')) {
    const deck = fact.kind === 'Hidden' ? fact.deck : ((fact.fact as unknown as { deck?: string }).deck ?? null);
    if (DAMAGE_DECKS.has(deck ?? '')) {
      return true;
    }
  }
  return QUIET.has(kind);
}

export function factKind(fact: FactProjection): string {
  return fact.kind === 'Shown' ? String((fact.fact as unknown as { kind: string } | null)?.kind ?? '') : (fact.fact ?? '');
}

// Cards a fact moves: the list where the viewer sees them, otherwise how many, and the colors of a draw by its decks.
interface Cards {
  list: unknown[] | null;
  count: number;
  colors: Record<string, number> | null;
  type: TypeRef;
  deck: string | null;
  pile: string | null;
}

interface DeckMove {
  t: 'drawn' | 'discarded' | 'onTop' | 'toBottom' | 'pulled' | 'removedFromGame';
  deck: string | null;
  cards: Cards;
}

type Move =
  | DeckMove
  | { t: 'gained' | 'lost' | 'seen' | 'playedBefore' | 'played'; who: number | null; cards: Cards }
  | { t: 'placed'; pile: string | null; cards: Cards }
  | { t: 'checkDrawn' }
  | { t: 'removed'; who: number | null; cards: Cards }
  | { t: 'presidentGained' | 'presidentLost' | 'crisis'; cards: Cards }
  | { t: 'shuffled'; deck: string | null }
  | { t: 'shipSeen'; who: number | null; ship: number }
  | { t: 'other' };

const SKILL_CARD: TypeRef = { t: 'ref', nullable: false, name: 'SkillCard' };

function cardsOf(fact: FactProjection, list: unknown, type: TypeRef, deck: string | null, pile: string | null): Cards {
  if (fact.kind === 'Hidden') {
    return { list: null, count: fact.count, colors: null, type, deck, pile };
  }
  const cards = Array.isArray(list) ? list : list === null || list === undefined ? [] : [list];
  return { list: cards, count: cards.length, colors: null, type, deck, pile };
}

function moveOf(fact: FactProjection): Move {
  const kind = factKind(fact);
  const shown = fact.kind === 'Shown' ? (fact.fact as unknown as Record<string, unknown>) : null;
  const field = (name: string) => (shown ? shown[name] : null);
  const deck = (fact.kind === 'Hidden' ? fact.deck : (field('deck') as string | null)) ?? null;
  const pile = (fact.kind === 'Hidden' ? fact.pile : (field('pile') as string | null)) ?? null;
  const who = (fact.kind === 'Hidden' ? fact.playerNumber : (field('playerNumber') as number | null)) ?? null;
  const deckCards = () => cardsOf(fact, field('cards') ?? field('drawOrder'), cardType(deck), deck, null);
  switch (kind) {
    case 'DeckFact.CardsDrawn':
      return { t: 'drawn', deck, cards: deckCards() };
    case 'DeckFact.CardsDiscarded':
      return { t: 'discarded', deck, cards: deckCards() };
    case 'DeckFact.CardsAddedOnTop':
      return { t: 'onTop', deck, cards: deckCards() };
    case 'DeckFact.CardsAddedToBottom':
      return { t: 'toBottom', deck, cards: deckCards() };
    case 'DeckFact.CardsPulled':
      return { t: 'pulled', deck, cards: deckCards() };
    case 'DeckFact.CardsRemovedFromGame':
      return { t: 'removedFromGame', deck, cards: deckCards() };
    case 'DeckFact.DeckShuffled':
      return { t: 'shuffled', deck };
    case 'PlayerFact.CardsGained':
      return { t: 'gained', who, cards: cardsOf(fact, field('cards'), cardType(null, pile), null, pile) };
    case 'PlayerFact.CardsLost':
      return { t: 'lost', who, cards: cardsOf(fact, field('cards'), cardType(null, pile), null, pile) };
    case 'PlayerFact.CardsSeen':
      return { t: 'seen', who, cards: cardsOf(fact, field('cards'), cardType(deck), deck, null) };
    case 'GameFact.SkillCardPlayed':
      return { t: 'played', who, cards: cardsOf(fact, field('card'), SKILL_CARD, null, 'SKILL') };
    case 'GameFact.CrisisCardPlayed':
      return { t: 'crisis', cards: cardsOf(fact, field('card'), cardType('CRISIS'), 'CRISIS', null) };
    case 'SkillCheckFact.CardsPlaced':
      return { t: 'placed', pile: (fact.kind === 'Hidden' ? fact.skillCheckPile : (field('pile') as string | null)) ?? null, cards: cardsOf(fact, field('cards'), SKILL_CARD, null, 'SKILL') };
    case 'SkillCheckFact.CardsDrawn':
      return { t: 'checkDrawn' };
    case 'SkillCheckFact.CardRemoved':
      return { t: 'removed', who, cards: cardsOf(fact, field('card'), SKILL_CARD, null, 'SKILL') };
    case 'SkillCheckFact.CardsPlayedBefore':
      return { t: 'playedBefore', who, cards: cardsOf(fact, field('cards'), SKILL_CARD, null, 'SKILL') };
    case 'TitleFact.PresidentQuorumCardGained':
      return { t: 'presidentGained', cards: cardsOf(fact, field('card'), cardType('QUORUM'), 'QUORUM', null) };
    case 'TitleFact.PresidentQuorumCardsLost':
      return { t: 'presidentLost', cards: cardsOf(fact, field('cards'), cardType('QUORUM'), 'QUORUM', null) };
    case 'PlayerFact.CivilianShipRevealed':
      return { t: 'shipSeen', who, ship: field('shipId') as number };
    default:
      return { t: 'other' };
  }
}

// A sentence before it is worded, so that the ones of a kind in a row can take each other's cards.
interface Draft {
  verb: string;
  who: number | null;
  target: number | null;
  deck: string | null;
  cards: Cards | null;
  // the names of the shuffled decks
  decks: string[];
  facts: FactProjection[];
  ships?: { names: Part[]; move: ShipMove };
  // the civilian ships looked at
  seen?: number[];
}

const draft = (verb: string, facts: FactProjection[], fields: Partial<Draft> = {}): Draft => ({ verb, who: null, target: null, deck: null, cards: null, decks: [], facts, ...fields });

/** The facts of a scope as sentences, the mechanics told as moves of cards and the bookkeeping left out. */
export function factSentences(facts: FactProjection[], context: FactContext): Sentence[] {
  const shown = facts.filter((fact) => !isQuiet(fact) && !emptyCards(fact));
  const moves = shown.map(moveOf);
  const drafts: Draft[] = [];
  let at = 0;
  while (at < shown.length) {
    at += pair(moves, shown, at, drafts, context);
  }
  // a reshuffle in the middle of draws is told after them
  const reordered = [...drafts.filter((one) => one.verb !== 'shuffled'), ...drafts.filter((one) => one.verb === 'shuffled')];
  return combine(reordered).map((one) => sentence(one, context));
}

// A move of no cards, e.g. a player who played none before a skill check.
function emptyCards(fact: FactProjection): boolean {
  if (fact.kind === 'Hidden') {
    return fact.count === 0 && fact.fact !== 'DeckFact.DeckShuffled';
  }
  const shown = fact.fact as unknown as Record<string, unknown> | null;
  const cards = shown?.cards;
  return Array.isArray(cards) && cards.length === 0;
}

// Words the moves from `at` on, as one draft or several, and says how many facts it took.
function pair(moves: Move[], facts: FactProjection[], at: number, out: Draft[], context: LabelContext): number {
  const move = moves[at];
  const next = moves[at + 1];
  const third = moves[at + 2];
  switch (move.t) {
    case 'drawn': {
      // the cards drawn in a row, and where they went
      let end = at;
      let count = 0;
      while (moves[end]?.t === 'drawn') {
        count += (moves[end] as { cards: Cards }).cards.count;
        end++;
      }
      const drawn = moves.slice(at, end) as DeckMove[];
      let target = end;
      while (moves[target]?.t === 'shuffled') {
        target++;
      }
      const into = moves[target];
      const reshuffles = facts.slice(end, target);
      const used = [...facts.slice(at, end), ...(into ? [facts[target]] : [])];
      const taken = target - at + 1;
      const told = (one: Draft) => {
        out.push(one, ...reshuffles.map((fact, index) => draft('shuffled', [fact], { decks: [(moves[end + index] as { deck: string | null }).deck ?? ''] })));
        return taken;
      };
      const fits = into && 'cards' in into && into.cards.count === count;
      const list = drawnList(drawn, fits ? into.cards : null);
      const deck = drawn[0].deck;
      if (fits && into.t === 'gained') {
        // a face down card doesn't show its color, the decks drawn from do
        return told(draft('draws', used, { who: into.who, cards: { ...into.cards, list, deck, colors: deckColors(drawn) } }));
      }
      if (fits && (into.t === 'onTop' || into.t === 'toBottom')) {
        return told(draft(into.t, used, { deck: into.deck, cards: { ...into.cards, list } }));
      }
      if (fits && into.t === 'placed' && into.pile === 'CARDS') {
        return told(draft('intoCheck', used, { deck, cards: { ...into.cards, list, deck } }));
      }
      if (fits && into.t === 'presidentGained') {
        return told(draft('presidentDraws', used, { cards: into.cards }));
      }
      if (fits && into.t === 'crisis') {
        return told(draft('crisis', used, { cards: into.cards }));
      }
      if (fits && into.t === 'seen') {
        return told(draft('sees', used, { who: into.who, deck, cards: { ...into.cards, list } }));
      }
      if (fits && into.t === 'discarded') {
        return told(draft('drawnAndDiscarded', used, { deck, cards: { ...into.cards, list } }));
      }
      for (const [index, one] of drawn.entries()) {
        out.push(draft('drawn', [facts[at + index]], { deck: one.deck, cards: one.cards }));
      }
      return end - at;
    }
    case 'lost': {
      if (next?.t === 'played' && third?.t === 'discarded') {
        out.push(draft('plays', facts.slice(at, at + 3), { who: move.who, cards: known(move.cards, next.cards, third.cards) }));
        return 3;
      }
      // the cards he lost, discarded one pile at a time
      let end = at + 1;
      let discarded = 0;
      while (moves[end]?.t === 'discarded' && discarded < move.cards.count) {
        discarded += (moves[end] as DeckMove).cards.count;
        end++;
      }
      if (end > at + 1 && discarded === move.cards.count) {
        const piles = (moves.slice(at + 1, end) as DeckMove[]).map((one) => one.cards);
        const list = piles.every((cards) => cards.list !== null) ? piles.flatMap((cards) => cards.list ?? []) : null;
        out.push(draft('discards', facts.slice(at, end), { who: move.who, cards: known(move.cards, { ...move.cards, list: move.cards.list ?? list }) }));
        return end - at;
      }
      if (next?.t === 'placed' && next.pile === 'CARDS') {
        out.push(draft('putsIn', facts.slice(at, at + 2), { who: move.who, cards: known(move.cards, next.cards) }));
        return 2;
      }
      if (next?.t === 'playedBefore' && next.who === move.who) {
        out.push(draft('playsBefore', facts.slice(at, at + 2), { who: move.who, cards: known(move.cards, next.cards) }));
        return 2;
      }
      if (next?.t === 'gained' && next.who !== move.who && next.cards.count === move.cards.count) {
        out.push(draft('gives', facts.slice(at, at + 2), { who: move.who, target: next.who, cards: known(move.cards, next.cards) }));
        return 2;
      }
      if (next?.t === 'played') {
        out.push(draft('plays', facts.slice(at, at + 2), { who: move.who, cards: known(move.cards, next.cards) }));
        return 2;
      }
      out.push(draft('loses', [facts[at]], { who: move.who, cards: move.cards }));
      return 1;
    }
    case 'removed': {
      if (next?.t === 'discarded') {
        out.push(draft('discarded', facts.slice(at, at + 2), { cards: known(move.cards, next.cards) }));
        return 2;
      }
      if (next?.t === 'gained' && move.who !== null && next.who === move.who) {
        out.push(draft('takesBack', facts.slice(at, at + 2), { who: move.who, cards: known(move.cards, next.cards) }));
        return 2;
      }
      out.push(draft('leavesCheck', [facts[at]], { cards: move.cards }));
      return 1;
    }
    case 'presidentLost':
      if (next?.t === 'discarded') {
        out.push(draft('presidentDiscards', facts.slice(at, at + 2), { cards: known(move.cards, next.cards) }));
        return 2;
      }
      out.push(draft('presidentLoses', [facts[at]], { cards: move.cards }));
      return 1;
    case 'checkDrawn':
      // the shuffle of the skill check: its piles say where each card went
      return 1;
    case 'placed':
      out.push(draft(move.pile === 'MATCHING' ? 'matching' : move.pile === 'NON_MATCHING' ? 'notMatching' : 'intoCheck', [facts[at]], { cards: move.cards }));
      return 1;
    case 'shuffled':
      out.push(draft('shuffled', [facts[at]], { decks: [move.deck ?? ''] }));
      return 1;
    case 'gained':
      out.push(draft('gets', [facts[at]], { who: move.who, cards: move.cards }));
      return 1;
    case 'played':
      out.push(draft('plays', [facts[at]], { who: move.who, cards: move.cards }));
      return 1;
    case 'playedBefore':
      out.push(draft('playsBefore', [facts[at]], { who: move.who, cards: move.cards }));
      return 1;
    case 'seen':
      out.push(draft('sees', [facts[at]], { who: move.who, cards: move.cards }));
      return 1;
    case 'discarded':
    case 'onTop':
    case 'toBottom':
    case 'pulled':
    case 'removedFromGame':
      out.push(draft(move.t, [facts[at]], { deck: move.deck, cards: move.cards }));
      return 1;
    case 'presidentGained':
      out.push(draft('presidentGets', [facts[at]], { cards: move.cards }));
      return 1;
    case 'shipSeen':
      out.push(draft('seesShips', [facts[at]], { who: move.who, seen: [move.ship] }));
      return 1;
    case 'crisis':
      out.push(draft('crisis', [facts[at]], { cards: move.cards }));
      return 1;
    default: {
      const fact = facts[at];
      if (factKind(fact) === 'ShipFact.ShipMoved' && fact.kind === 'Shown') {
        const one = shipMove(fact.fact as unknown as Record<string, unknown>, context);
        out.push(draft('ship', [fact], { ships: { names: [one.ship], move: one } }));
        return 1;
      }
      out.push(draft('other', [fact]));
      return 1;
    }
  }
}

// The cards of a move told by several facts: the list where one of them shows it, the colors where one counts them.
function known(...all: Cards[]): Cards {
  const list = all.find((cards) => cards.list !== null)?.list ?? null;
  const colors = all.find((cards) => cards.colors !== null)?.colors ?? null;
  return { ...all[0], list, colors, count: list ? list.length : all[0].count };
}

// The colors of cards drawn from the Skill Card decks, which their decks name; none when one of them is another deck.
function deckColors(drawn: DeckMove[]): Record<string, number> | null {
  if (!drawn.every((one) => SKILL_COLORS.has(one.deck ?? ''))) {
    return null;
  }
  const colors: Record<string, number> = {};
  for (const one of drawn) {
    colors[one.deck!] = (colors[one.deck!] ?? 0) + one.cards.count;
  }
  return colors;
}

function drawnList(drawn: DeckMove[], into: Cards | null): unknown[] | null {
  if (into?.list) {
    return into.list;
  }
  return drawn.every((one) => one.cards.list !== null) ? drawn.flatMap((one) => one.cards.list ?? []) : null;
}

// Drafts of a kind in a row as one: "draws A and B" for two draws, every shuffled deck in one sentence.
function combine(drafts: Draft[]): Draft[] {
  const out: Draft[] = [];
  const joins = new Set(['draws', 'discards', 'plays', 'putsIn', 'playsBefore', 'gets', 'loses', 'discarded', 'matching', 'notMatching', 'intoCheck', 'presidentDraws', 'presidentDiscards']);
  for (const one of drafts) {
    const last = out[out.length - 1];
    if (last?.seen && one.seen && last.who === one.who) {
      last.seen = [...last.seen, ...one.seen];
      last.facts.push(...one.facts);
      continue;
    }
    if (last?.ships && one.ships && sameShipMove(last.ships.move, one.ships.move)) {
      last.ships = { ...last.ships, names: [...last.ships.names, ...one.ships.names] };
      last.facts.push(...one.facts);
      continue;
    }
    // a skill check's piles, however its cards were sorted into them
    const pile = (one.verb === 'matching' || one.verb === 'notMatching') && one.cards ? out.find((other) => other.verb === one.verb && other.cards) : undefined;
    if (pile?.cards && one.cards && (pile.cards.list === null) === (one.cards.list === null)) {
      pile.cards = { ...pile.cards, list: pile.cards.list ? [...pile.cards.list, ...(one.cards.list ?? [])] : null, count: pile.cards.count + one.cards.count };
      pile.facts.push(...one.facts);
      continue;
    }
    if (last && one.verb === 'shuffled' && last.verb === 'shuffled') {
      last.decks.push(...one.decks);
      last.facts.push(...one.facts);
      continue;
    }
    const sameDeck = one.verb !== 'intoCheck' || last?.deck === one.deck;
    if (last && joins.has(one.verb) && last.verb === one.verb && last.who === one.who && last.target === one.target && sameDeck && last.cards && one.cards) {
      const bothShown = last.cards.list !== null && one.cards.list !== null;
      const bothHidden = last.cards.list === null && one.cards.list === null;
      if (bothShown || bothHidden) {
        last.cards = {
          ...last.cards,
          list: bothShown ? [...(last.cards.list ?? []), ...(one.cards.list ?? [])] : null,
          count: last.cards.count + one.cards.count,
          colors: mergeColors(last.cards.colors, one.cards.colors),
          deck: last.cards.deck === one.cards.deck ? last.cards.deck : null,
        };
        last.facts.push(...one.facts);
        continue;
      }
    }
    out.push({ ...one, decks: [...one.decks], facts: [...one.facts], seen: one.seen ? [...one.seen] : undefined });
  }
  return out;
}

/** Whether two ships moved alike: the same verb to the same place. */
export function sameShipMove(first: ShipMove, second: ShipMove): boolean {
  return first.one === second.one && partsText(first.tail) === partsText(second.tail);
}

function mergeColors(first: Record<string, number> | null, second: Record<string, number> | null): Record<string, number> | null {
  if (!first && !second) {
    return null;
  }
  const merged: Record<string, number> = { ...(first ?? {}) };
  for (const [color, count] of Object.entries(second ?? {})) {
    merged[color] = (merged[color] ?? 0) + count;
  }
  return merged;
}

// The cards of a sentence: their names with their images, or how many of what kind.
function cardsParts(cards: Cards, context: LabelContext): Part[] {
  if (cards.list === null) {
    return hiddenCardsParts(cards.count, cards.deck, cards.pile, cards.colors);
  }
  if (cards.list.length === 0) {
    return [{ text: 'nothing' }];
  }
  // the same card three times or more is counted: "You Are Not a Cylon ×19"
  const counted: { card: unknown; times: number }[] = [];
  for (const card of cards.list) {
    const same = counted.find((entry) => sameCard(entry.card, card));
    if (same) {
      same.times++;
    } else {
      counted.push({ card, times: 1 });
    }
  }
  const items = counted.flatMap(({ card, times }) => {
    const parts = cardParts(card, cards.type, context);
    if (times >= 3) {
      return [[...parts, { text: ` ×${times}` }]];
    }
    return Array.from({ length: times }, () => parts);
  });
  const parts: Part[] = [];
  items.forEach((item, index) => {
    if (index > 0) {
      parts.push({ text: index === items.length - 1 ? ' and ' : ', ' });
    }
    parts.push(...item);
  });
  return parts;
}

function sentence(one: Draft, context: FactContext): Sentence {
  const name = (player: number | null): Part[] => [player !== null ? playerPart(player, context) : { text: 'Someone' }];
  const cards = one.cards ? cardsParts(one.cards, context) : [];
  const many = (one.cards?.count ?? 0) !== 1;
  const words = (...parts: (string | Part[])[]): Part[] => parts.flatMap((part) => (typeof part === 'string' ? [{ text: part }] : part));
  const given = one.cards?.list ?? [];
  const make = (parts: Part[], subject: number | null, moves = false): Sentence => ({ parts, subject, given: moves ? given : [], facts: one.facts });
  switch (one.verb) {
    case 'draws':
      return make(words(name(one.who), ` draws `, cards), one.who);
    case 'gets':
      return make(words(name(one.who), ` gets `, cards), one.who);
    case 'loses':
      return make(words(name(one.who), ` loses `, cards), one.who, true);
    case 'discards':
      return make(words(name(one.who), ` discards `, cards), one.who, true);
    case 'plays':
      return make(words(name(one.who), ` plays `, cards), one.who, true);
    case 'putsIn':
      return make(words(name(one.who), ` puts `, cards, ' into the skill check'), one.who, true);
    case 'playsBefore':
      return make(words(name(one.who), ` plays `, cards, ' before the skill check'), one.who, true);
    case 'gives':
      return make(words(name(one.who), ` gives `, cards, ` to `, name(one.target)), one.who, true);
    case 'takesBack':
      return make(words(name(one.who), ` takes `, cards, ' back'), one.who);
    case 'sees':
      return make(words(name(one.who), ` looks at `, cards), one.who);
    case 'drawn':
      return make(words(cards, ` ${many ? 'are' : 'is'} drawn from `, deckParts(one.deck)), null);
    case 'discarded':
      return make(words(cards, ` ${many ? 'are' : 'is'} discarded`), null);
    case 'onTop':
      return make(words(cards, ` ${many ? 'go' : 'goes'} on top of `, deckParts(one.deck)), null);
    case 'toBottom':
      return make(words(cards, ` ${many ? 'go' : 'goes'} to the bottom of `, deckParts(one.deck)), null);
    case 'pulled':
      return make(words(cards, ` ${many ? 'are' : 'is'} taken out of `, deckParts(one.deck)), null);
    case 'removedFromGame':
      return make(words(cards, ` ${many ? 'leave' : 'leaves'} the game`), null);
    case 'intoCheck':
      // a hidden card's kind names its deck already
      return make(words(cards, one.deck && one.cards?.list ? words(' from ', deckParts(one.deck)) : [], ` ${many ? 'go' : 'goes'} into the skill check`), null);
    case 'seesShips':
      return make(words(name(one.who), ` looks at `, joinParts((one.seen ?? []).map((ship) => [shipPart(ship, context, true)]))), one.who);
    case 'drawnAndDiscarded':
      return make(words(cards, ` ${many ? 'are' : 'is'} drawn from `, deckParts(one.deck), ' and discarded'), null);
    case 'ship': {
      const ships = one.ships!;
      return { parts: shipMovesParts(ships.names, ships.move), subject: null, given: [], facts: one.facts, ships };
    }
    case 'leavesCheck':
      return make(words(cards, ` ${many ? 'leave' : 'leaves'} the skill check`), null);
    case 'matching':
      return make(words('Matching: ', cards), null);
    case 'notMatching':
      return make(words('Not matching: ', cards), null);
    case 'presidentDraws':
      return make(words('The ', [holderPart('PRESIDENT', context)], ' draws ', cards), null);
    case 'presidentGets':
      return make(words('The ', [holderPart('PRESIDENT', context)], ' gets ', cards), null);
    case 'presidentLoses':
      return make(words('The ', [holderPart('PRESIDENT', context)], ' loses ', cards), null);
    case 'presidentDiscards':
      return make(words('The ', [holderPart('PRESIDENT', context)], ' discards ', cards), null);
    case 'crisis':
      return make(words('The Crisis Card is ', cards), null);
    case 'shuffled':
      return make(
        one.decks.length === 1 ? capitalizedParts(words(deckParts(one.decks[0]), ' is shuffled')) : words('The ', joinParts(one.decks.map((deck) => [deckNamePart(deck)])), ' decks are shuffled'),
        null,
      );
    default: {
      const fact = one.facts[0];
      return make(factParts(fact, context), subjectOf(fact, context));
    }
  }
}


// The player a fact is about: the one it names first, by number or by character.
function subjectOf(fact: FactProjection, context: FactContext): number | null {
  if (fact.kind === 'Hidden') {
    return fact.playerNumber;
  }
  const shown = fact.fact as unknown as Record<string, unknown> | null;
  if (typeof shown?.playerNumber === 'number') {
    return shown.playerNumber;
  }
  return typeof shown?.character === 'string' ? (context.playerOf?.(shown.character) ?? null) : null;
}

/**
 * A sentence as a line under the player the log names above it: without his name where it starts with it.
 */
export function sentenceParts(one: Sentence, actor: number | null, context: LabelContext): Part[] {
  if (actor === null) {
    return one.parts;
  }
  return withoutSubject(one.parts, context.playerName(actor)) ?? one.parts;
}

/** The cards an event names, the ones its player moves: a fact that moves only those says what its line says. */
export function namedCards(details: Record<string, unknown> | null): unknown[] {
  if (!details) {
    return [];
  }
  // a Quorum or a Mutiny Card's record is its card
  const record = recordNamed(String(details.kind));
  const cards: unknown[] = [record?.quorumCard, record?.mutinyCard].filter((card) => card);
  for (const [name, value] of Object.entries(details)) {
    if (name === 'kind') continue;
    for (const element of Array.isArray(value) ? value : [value]) {
      if (element && typeof element === 'object' && 'type' in element && 'value' in element) {
        cards.push(element);
      } else if (typeof element === 'string' && /^[A-Z0-9_]+$/.test(element)) {
        cards.push(element);
      }
    }
  }
  return cards;
}

export function sameCard(first: unknown, second: unknown): boolean {
  if (typeof first === 'string' || typeof second === 'string') {
    return first === second;
  }
  const a = first as { type?: unknown; value?: unknown } | null;
  const b = second as { type?: unknown; value?: unknown } | null;
  return !!a && !!b && a.type === b.type && a.value === b.value;
}

/** Whether a sentence only says what the line says: the same words, or the cards the event names moving. */
export function repeats(one: Sentence, line: Part[], named: unknown[], actor: number | null, context: LabelContext): boolean {
  const text = partsText(sentenceParts(one, actor, context)).toLowerCase();
  const said = partsText(line).toLowerCase();
  if (text === said || said.startsWith(text)) {
    return true;
  }
  return one.given.length > 0 && one.subject === actor && one.given.every((card) => named.some((other) => sameCard(card, other)));
}

