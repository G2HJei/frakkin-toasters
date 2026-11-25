import { useMemo, useState } from 'react';
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
export function LogPanel() {
  const table = useTable();
  const log = useGameStore((state) => state.log);
  const players = table.game?.players;
  // a player named in a line shows his portrait and his sheet on hover
  const words: FactContext = useMemo(
    () => ({
      ...table,
      artOf: partArt,
      iconOf: partIcon,
      characterOf: (playerNumber: number) => players?.find((player) => player.number === playerNumber)?.character ?? null,
      playerOf: (character: string) => players?.find((player) => player.character === character)?.number ?? null,
    }),
    [table, players],
  );
  const turns = useMemo(() => logStory(log, words), [log, words]);
  const { scrollRef, contentRef, following, unseen, onScroll, toBottom } = useFollowBottom(log);
  const [factsShown, setFactsShown] = useState(false);
  const withFacts = table.sandbox && factsShown;
  return (
    <section className="panel cf log" aria-label="log">
      {/* over the top right of the lines, which scroll behind it */}
      {table.sandbox && (
        <button
          type="button"
          className={factsShown ? 'log-facts cf on' : 'log-facts cf'}
          aria-pressed={factsShown}
          title={factsShown ? 'Hide the facts under each line' : 'Show the facts under each line'}
          onClick={() => setFactsShown(!factsShown)}
        >
          Facts
        </button>
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
                      <Line key={line.key} line={line} withFacts={withFacts} />
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
