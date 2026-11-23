import { useMemo } from 'react';
import { ArtStrip } from '../../art/Art';
import { factArt, optionArt, recordArt, unique } from '../../art/refs';
import { partArt, Words } from '../../art/Words';
import { constantLabel, eventParts, factParts, optionLabel, partsText, typeLabel, unnamedSource, type LabelContext } from '../../labels/describe';
import { useGameStore } from '../../live/gameStore';
import type { FactProjection, LogEntry } from '../../wire/types';
import { entryTargets, useFlash } from '../flash';
import { useTable } from '../tableContext';
import { useFollowBottom } from './useFollowBottom';

interface TurnGroup {
  turn: number;
  entries: LogEntry[];
}

interface StepGroup {
  // the step the entries happen in, null before the first step of a turn
  step: string | null;
  entries: LogEntry[];
}

// A new group starts with the scope that records the start of a turn.
export function groupByTurn(entries: LogEntry[]): TurnGroup[] {
  const groups: TurnGroup[] = [{ turn: 0, entries: [] }];
  for (const entry of entries) {
    const started = shownFact(entry, 'GameFact.TurnStarted');
    if (started) {
      groups.push({ turn: started.turn as number, entries: [] });
    }
    groups[groups.length - 1].entries.push(entry);
  }
  return groups.filter((group) => group.entries.length > 0);
}

/** A turn's entries by the step they happen in: a new group with each scope that records a step change. */
export function groupBySteps(entries: LogEntry[]): StepGroup[] {
  const groups: StepGroup[] = [{ step: null, entries: [] }];
  for (const entry of entries) {
    const changed = shownFact(entry, 'GameFact.StepChanged');
    if (changed) {
      groups.push({ step: changed.step as string, entries: [] });
    }
    groups[groups.length - 1].entries.push(entry);
  }
  return groups.filter((group) => group.entries.length > 0);
}

function shownFact(entry: LogEntry, kind: string): Record<string, unknown> | null {
  if (entry.kind !== 'ScopeLog') {
    return null;
  }
  const found = (entry.facts ?? []).find((fact) => fact.kind === 'Shown' && fact.fact?.kind === kind);
  return found?.kind === 'Shown' ? (found.fact as unknown as Record<string, unknown>) : null;
}

// The facts that tell the story: what changed on the table that everyone talks about. The rest is mechanics (cards
// drawn and discarded, decks shuffled, limits used), folded under the line.
const STORY = new Set([
  'BoardFact.ResourceDecreased',
  'BoardFact.ResourceIncreased',
  'BoardFact.LocationDamaged',
  'BoardFact.LocationRepaired',
  'BoardFact.PegasusDestroyed',
  'BoardFact.JumpPreparationIncreased',
  'BoardFact.JumpPreparationDecreased',
  'BoardFact.JumpPreparationReset',
  'BoardFact.BoardingPartyAdvanced',
  'BoardFact.PursuitAdvanced',
  'GameFact.DieRolled',
  'GameFact.DieRollReplaced',
  'GameFact.DieRollCardPlayed',
  'GameFact.CrisisCardPlayed',
  'GameFact.DestinationReached',
  'GameFact.GameEnded',
  'GameFact.TurnEndedEarly',
  'GameFact.NukeDiscarded',
  'PlayerFact.CharacterMoved',
  'PlayerFact.CharacterRemoved',
  'PlayerFact.LoyaltyCardRevealed',
  'PlayerFact.FinalFiveCardShown',
  'PlayerFact.MotiveCardRevealed',
  'PlayerFact.AbilityUsed',
  'SkillCheckFact.SkillCheckStarted',
  'SkillCheckFact.DifficultyAdjusted',
  'SkillCheckFact.TotalStrengthDetermined',
  'SkillCheckFact.ResultDetermined',
  'TitleFact.PresidentAssigned',
  'TitleFact.AdmiralAssigned',
  'TitleFact.CagAssigned',
  'TitleFact.QuorumCardPutInPlay',
  'InterruptFact.InterruptCardResolved',
  'InterruptFact.RollAbilityResolved',
]);

// what the headers say already
const HEADERS = new Set(['GameFact.TurnStarted', 'GameFact.StepChanged']);

/** The facts of a scope that tell the story, and the rest. A ship that is destroyed, damaged or launched is story. */
export function splitFacts(facts: FactProjection[]): { story: FactProjection[]; rest: FactProjection[] } {
  const story: FactProjection[] = [];
  const rest: FactProjection[] = [];
  for (const fact of facts) {
    const kind = fact.kind === 'Shown' ? fact.fact?.kind ?? '' : '';
    if (HEADERS.has(kind)) {
      continue;
    }
    const ship = kind === 'ShipFact.ShipMoved' ? (fact as { fact: { from: string | null; to: string | null } }).fact : null;
    const shipStory = ship !== null && ship.to !== ship.from && ship.to !== 'CYLON_FLEET_SPACE' && ship.from !== 'HUMAN_SUPPLY' && ship.from !== 'CIVILIAN_DECK';
    (STORY.has(kind) || shipStory ? story : rest).push(fact);
  }
  return { story, rest };
}

// A scope that only marks a step or a turn: its header says it.
function onlyHeader(entry: LogEntry): boolean {
  return entry.kind === 'ScopeLog' && !entry.details && (entry.facts ?? []).length > 0 && (entry.facts ?? []).every((fact) => fact.kind === 'Shown' && HEADERS.has(fact.fact?.kind ?? ''));
}

/**
 * The story of the game: grouped by turn, every turn open, and by step within a turn. Each scope is one line, its actor,
 * its event and what it names, with its facts under it, the ones that change the table first. Card and character names
 * show their image on hover. A click on an entry lights what it touched on the board. The log follows the newest entry
 * until the user scrolls up, and the arrow at its bottom right follows again, lit while new entries came below.
 */
export function LogPanel() {
  const table = useTable();
  const log = useGameStore((state) => state.log);
  const groups = useMemo(() => groupByTurn(log), [log]);
  const { scrollRef, contentRef, following, unseen, onScroll, toBottom } = useFollowBottom(log);
  const words: LabelContext = { ...table, artOf: partArt };
  return (
    <section className="panel cf log" aria-label="log">
      {/* the body scrolls, so the log can take whatever height its column leaves it */}
      <div className="log-body" ref={scrollRef} onScroll={onScroll}>
        <div ref={contentRef}>
          {groups.map((group, index) => (
            // open once: a turn the user closes stays closed
            <details key={`${group.turn}-${index}`} className="turn" open>
              <summary>{group.turn === 0 ? 'Setup' : `Turn ${group.turn}`}</summary>
              {groupBySteps(group.entries).map((step, at) => (
                <div key={at} className="log-step">
                  {step.step && <h4 className="step-name">{constantLabel('GameStep', step.step)}</h4>}
                  <ol className="entries">
                    {step.entries
                      .filter((entry) => !onlyHeader(entry))
                      .map((entry, position) => (
                        <Entry key={position} entry={entry} words={words} />
                      ))}
                  </ol>
                </div>
              ))}
            </details>
          ))}
        </div>
      </div>
      {!following && (
        <button
          type="button"
          className={unseen ? 'log-to-bottom cf unseen' : 'log-to-bottom cf'}
          aria-label="Go to bottom"
          title={unseen ? 'New entries below' : 'Go to bottom'}
          onClick={toBottom}
        >
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M12 4v12M6 10l6 6 6-6M5 20h14" />
          </svg>
        </button>
      )}
    </section>
  );
}

function Entry({ entry, words }: { entry: LogEntry; words: LabelContext }) {
  const table = useTable();
  const flash = () => useFlash.getState().flash(entryTargets(entry, table.game));
  if (entry.kind === 'ChoiceLog') {
    const chooser = table.playerName(entry.chooser);
    const option = entry.option ? optionLabel(entry.option, table) : 'an option';
    return (
      <li className="choice" onClick={flash}>
        <span>
          {/* "William Adama chooses the Top of …" names its chooser itself */}
          {option.startsWith(chooser) ? option : `${chooser} picks ${option}`}
          <ArtStrip refs={optionArt(entry.option)} />
        </span>
      </li>
    );
  }
  const details = entry.details as unknown as Record<string, unknown> | null;
  const parts = details ? eventParts(details, words) : [{ text: typeLabel(entry.event ?? '') }];
  const actor = entry.actor > 0 ? table.playerName(entry.actor) : 'Game';
  // "William Adama moves to the Armory" names its actor itself
  const named = parts[0]?.text === actor;
  const line = partsText(parts);
  // the card or ability the entry exists for, e.g. "Forward Thinker" beside "Lee Adama may activate his location"
  const source = unnamedSource(entry.event ?? '', details, parts, words);
  const split = splitFacts(entry.facts ?? []);
  // a fact that says what the line says, e.g. the move of a character who moved
  const facts = [...split.story.filter((fact) => partsText(factParts(fact, words)) !== line), ...split.rest];
  return (
    <li className="scope" onClick={flash}>
      {named ? (
        <span className="event">
          <span className="actor">{actor}</span>
          <Words parts={parts.slice(1)} />
        </span>
      ) : (
        <>
          <span className="actor">{actor}</span>{' '}
          <span className="event">
            <Words parts={parts} />
          </span>
        </>
      )}
      {source && (
        <span className="source-tag">
          <Words parts={[source]} />
        </span>
      )}
      {/* the server answered for him: it was his only answer */}
      {entry.automatic && (
        <span className="auto-tag" title="The only answer, taken automatically">
          automatic
        </span>
      )}
      <ArtStrip refs={unique([...recordArt(details), ...(entry.facts ?? []).flatMap(factArt)])} />
      {facts.length > 0 && (
        <ul className="facts">
          {facts.map((fact, index) => (
            <li key={index}>
              <Words parts={factParts(fact, words)} />
            </li>
          ))}
        </ul>
      )}
    </li>
  );
}
