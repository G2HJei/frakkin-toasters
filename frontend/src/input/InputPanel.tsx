import { ArtStrip, CardTile } from '../art/Art';
import { choiceArt } from '../art/refs';
import { partArt, Words } from '../art/Words';
import { optionLabel } from '../labels/describe';
import { pauseOf } from '../labels/pause';
import { ruleFor } from '../labels/rules';
import { RuleLink } from '../table/reference/ReferenceDrawer';
import { useGameStore } from '../live/gameStore';
import { useTable } from '../table/tableContext';
import { AnswerPanel } from './AnswerPanel';
import { moves, type Answer, type Move } from './answers';
import { EventBuilder } from './EventBuilder';
import type { EventValue } from './eventModel';

export interface InputPanelProps {
  onChoose(option: number): void;
  onSubmit(event: EventValue): void;
  // sends a move of the answers, its pick filled in
  onMove(move: Move, event: Answer | null): void;
}

/**
 * What the game waits for. The asked player gets the input's answers (`AnswerPanel`): a choice's options, a decision's
 * answers by what they do, a picker for a shape. Under "Advanced" the plain options of a choice, answered by index, and
 * the event builder for a decision. The others see who is asked, or a plain "waiting" while it is secret. What every
 * option of a choice names, e.g. the Crisis Card whose section is chosen, shows once above them, large enough to read.
 */
export function InputPanel({ onChoose, onSubmit, onMove }: InputPanelProps) {
  const table = useTable();
  const rejected = useGameStore((state) => state.rejected);
  const version = useGameStore((state) => state.version);
  // a move chose its option and answers the decision it asks next: the panel waits for it
  const sending = useGameStore((state) => state.intent !== null);
  const input = table.game?.input ?? null;
  const outcome = table.game?.outcome ?? null;
  const art = input?.kind === 'Choose' ? choiceArt(input.options ?? []) : null;

  // the viewer is asked: a Choose or a Decide only reaches the one who answers
  const yourMove = input?.kind === 'Choose' || input?.kind === 'Decide';
  // its names show their images on hover, like the log's
  const pause = pauseOf(input, { ...table, artOf: partArt });
  return (
    <section className={`panel cf input-panel ${yourMove ? 'your-move' : ''} ${sending ? 'sending' : ''}`.trim()} aria-label="input" aria-busy={sending}>
      <h2>{pause.heading}</h2>
      {(pause.line || ruleFor(input)) && (
        <p className="pause-line">
          {pause.line && <Words parts={pause.line} />}
          {/* where to read up on it */}
          {ruleFor(input) && <RuleLink article={ruleFor(input)!.article} found={ruleFor(input)} />}
        </p>
      )}
      {outcome && (
        <p className="outcome">
          The {outcome.winner === 'HUMANS' ? 'humans' : 'Cylons'} win
          {outcome.winners && outcome.winners.length > 0 ? `: ${outcome.winners.map(table.playerName).join(', ')}` : ''}.
        </p>
      )}
      {art && art.common.length > 0 && (
        <div className="choice-subject">
          {art.common.map((ref) => (
            <CardTile key={ref.key} art={ref} label={ref.label} size="lg" />
          ))}
        </div>
      )}
      {yourMove && <AnswerPanel key={version} onMove={onMove} />}
      {yourMove && (
        // the answers cover every input; this is the way around them, and the sandbox's
        <details className="advanced" open={moves(input).length === 0}>
          <summary>Advanced</summary>
          {input?.kind === 'Choose' && (
            <div className="options">
              {(input.options ?? []).map((option, index) => (
                <button key={index} type="button" className="option" onClick={() => onChoose(index)}>
                  <ArtStrip refs={art?.perOption[index] ?? []} />
                  <span>{optionLabel(option, table)}</span>
                </button>
              ))}
            </div>
          )}
          {input?.kind === 'Decide' && input.action && (
            // a new game starts a new form
            <EventBuilder
              key={`${version}-${input.action}`}
              action={input.action}
              playerNumber={input.player}
              constraints={(input.constraints ?? []).filter((constraint): constraint is NonNullable<typeof constraint> => constraint !== null)}
              onSubmit={onSubmit}
            />
          )}
        </details>
      )}
      {rejected && (
        <p className="rejected" role="alert">
          {rejected.reason === 'STALE' ? 'The game moved on: try again.' : rejected.message}
        </p>
      )}
    </section>
  );
}
