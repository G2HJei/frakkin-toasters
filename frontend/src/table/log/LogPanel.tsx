import { useMemo, useState, type ReactNode } from 'react';
import { ArtImg, ArtStrip } from '../../art/Art';
import { characterArt } from '../../art/images';
import { partArt, partIcon, Words } from '../../art/Words';
import { PassIcon } from '../../input/ArtSlot';
import { constantLabel } from '../../labels/describe';
import type { FactContext } from '../../labels/facts';
import { useGameStore } from '../../live/gameStore';
import { entryTargets, useFlash } from '../flash';
import { useTable } from '../tableContext';
import { logStory, type LogLine } from './story';
import { useFollowBottom } from './useFollowBottom';

/**
 * The story of the game: grouped by turn, every turn open, and by step within a turn. Each line is what an event did,
 * in words, after the portrait and name of the player it is about, every time; what happens to the game has none. A
 * sandbox can show the facts that tell more under each line, the ones that change the table first. Card and character
 * names show their image on hover; resources, ships, titles and the die have an icon in front of their words, and good
 * and bad news (a resource gained or lost, a skill check's result) are green or red. A click on a line lights what it
 * touched on the board. The log follows the newest
 * line until the user scrolls up, and the arrow at its bottom right follows again, lit while new lines came below.
 */
export interface LogPanelProps {
  // the switch to the chat, over the panel's top right; a sandbox has no chat and shows its "Facts" switch there
  toggle?: ReactNode;
  // how many entries the viewer had seen at his last decision: a red line goes above the first one after them
  seenBefore?: number | null;
  // a click on the red line: all is seen
  onSeen?(): void;
}

export function LogPanel({ toggle, seenBefore = null, onSeen }: LogPanelProps = {}) {
  const table = useTable();
  const log = useGameStore((state) => state.log);
  const players = table.game?.players;
  const titles = table.game?.titles;
  // a player named in a line shows his portrait and his sheet on hover, and so does a title's holder (the story knows
  // who held it then; these are the holders now)
  const words: FactContext = useMemo(() => {
    const playerOf = (character: string | null | undefined) => players?.find((player) => player.character === character)?.number ?? null;
    const holders: Record<string, string | null | undefined> = { PRESIDENT: titles?.president, ADMIRAL: titles?.admiral, CAG: titles?.cag };
    return {
      ...table,
      artOf: partArt,
      iconOf: partIcon,
      characterOf: (playerNumber: number) => players?.find((player) => player.number === playerNumber)?.character ?? null,
      playerOf: (character: string) => playerOf(character),
      titleHolder: (title: string) => (holders[title] ? playerOf(holders[title]) : null),
    };
  }, [table, players, titles]);
  const turns = useMemo(() => logStory(log, words), [log, words]);
  // the first line of an entry the viewer hadn't seen at his last decision
  const firstUnseen = useMemo(() => {
    if (seenBefore === null || seenBefore >= log.length) {
      return null;
    }
    const index = new Map(log.map((entry, at) => [entry, at]));
    for (const turn of turns) {
      for (const step of turn.steps) {
        const line = step.lines.find((candidate) => (index.get(candidate.entry) ?? -1) >= seenBefore);
        if (line) {
          return line.key;
        }
      }
    }
    return null;
  }, [log, turns, seenBefore]);
  const { scrollRef, contentRef, following, unseen, onScroll, toBottom } = useFollowBottom(log);
  const [factsShown, setFactsShown] = useState(false);
  const withFacts = table.sandbox && factsShown;
  return (
    <section className="panel cf log" aria-label="log">
      {/* over the top right of the lines, which scroll behind it */}
      {table.sandbox ? (
        <button
          type="button"
          className={factsShown ? 'log-toggle cf on' : 'log-toggle cf'}
          aria-pressed={factsShown}
          title={factsShown ? 'Hide the facts under each line' : 'Show the facts under each line'}
          onClick={() => setFactsShown(!factsShown)}
        >
          Facts
        </button>
      ) : (
        toggle
      )}
      {/* the body scrolls, so the log can take whatever height its column leaves it */}
      <div className="log-body" ref={scrollRef} onScroll={onScroll}>
        <div ref={contentRef}>
          {turns.map((turn, index) => (
            // open once: a turn the user closes stays closed
            <details key={`${turn.turn}-${index}`} className="turn" open>
              <summary>
                {turn.turn === 0 ? 'Setup' : `Turn ${turn.turn}`}
                {turn.turn > 0 && turn.player !== null && (
                  <>
                    {' · '}
                    <span className="turn-player">{table.playerName(turn.player)}</span>
                  </>
                )}
              </summary>
              {turn.steps.map((step, at) => (
                <div key={at} className="log-step">
                  {step.step && <h4 className="step-name">{constantLabel('GameStep', step.step)}</h4>}
                  <ol className="lines">
                    {step.lines.map((line) => (
                      <LineWithMark key={line.key} line={line} withFacts={withFacts} unseenFromHere={line.key === firstUnseen} onSeen={onSeen} />
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
            <path d="M12 5v14M6 13l6 6 6-6" />
          </svg>
        </button>
      )}
    </section>
  );
}

// The red line over the first line the viewer hadn't seen at his last decision; a click on it marks all seen.
function LineWithMark({ line, withFacts, unseenFromHere, onSeen }: { line: LogLine; withFacts: boolean; unseenFromHere: boolean; onSeen?(): void }) {
  return (
    <>
      {unseenFromHere && (
        <li className="last-seen" aria-label="new since your last move">
          <button type="button" className="link" title="Mark all as seen" onClick={() => onSeen?.()}>
            new since your last move
          </button>
        </li>
      )}
      <Line line={line} withFacts={withFacts} />
    </>
  );
}

// The player's token and name, then what happened; what happens to the game has neither.
function Line({ line, withFacts }: { line: LogLine; withFacts: boolean }) {
  const table = useTable();
  const flash = () => useFlash.getState().flash(entryTargets(line.entry, table.game));
  const character = line.actor !== null ? table.game?.players?.find((player) => player.number === line.actor)?.character : null;
  const token = character ? characterArt(character) : null;
  return (
    <li className={line.actor !== null ? 'scope' : 'scope game'} onClick={flash}>
      {line.actor !== null && <span className="line-token">{token && <ArtImg art={token} />}</span>}
      <div className="line-body">
        {line.actor !== null && (
          <>
            <span className="actor">{line.namesUser ? (table.userName(line.actor) ?? `Player ${line.actor}`) : table.playerName(line.actor)}</span>{' '}
          </>
        )}
        {/* the server answered for him: it was his only answer, so it skipped ahead like a pass */}
        {line.automatic && (
          <span className="auto-icon" role="img" aria-label="automatic" title="The only answer, taken automatically">
            <PassIcon />
          </span>
        )}
        <span className="event">
          <Words parts={line.words} />
        </span>
        {line.source && ' '}
        {line.source && (
          <span className="source-tag">
            <Words parts={[line.source]} />
          </span>
        )}
        {/* his token leads the line already */}
        <ArtStrip refs={token ? line.art.filter((ref) => ref.key !== token.key) : line.art} />
        {withFacts && line.facts.length > 0 && (
          <ul className="facts">
            {line.facts.map((fact, index) => (
              <li key={index}>
                <Words parts={fact} />
              </li>
            ))}
          </ul>
        )}
      </div>
    </li>
  );
}
