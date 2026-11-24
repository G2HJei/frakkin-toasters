import { CardTile } from '../art/Art';
import { choiceArt } from '../art/refs';
import { partArt, Words } from '../art/Words';
import { pauseOf } from '../labels/pause';
import { ruleFor } from '../labels/rules';
import { RuleLink } from '../table/reference/ReferenceDrawer';
import { useGameStore } from '../live/gameStore';
import { useTable } from '../table/tableContext';
import { AnswerPanel } from './AnswerPanel';
import type { Answer, Move } from './answers';

export interface InputPanelProps {
  // sends a move of the answers, its pick filled in
  onMove(move: Move, event: Answer | null): void;
}

/**
 * What the game waits for. The asked player gets the input's answers (`AnswerPanel`): a choice's options, a decision's
 * answers by what they do, a picker for a shape. The others see who is asked, or a plain "waiting" while it is secret.
 * What every option of a choice names, e.g. the Crisis Card whose section is chosen, shows once above them, large
 * enough to read.
 */
export function InputPanel({ onMove }: InputPanelProps) {
  const table = useTable();
  const rejected = useGameStore((state) => state.rejected);
  const version = useGameStore((state) => state.version);
  // a move chose its option and answers the decision it asks next: the panel waits for it
  const sending = useGameStore((state) => state.intent !== null);
  const input = table.game?.input ?? null;
  const outcome = table.game?.outcome ?? null;
  const art = input?.kind === 'Choose' ? choiceArt(input.options ?? []) : [];

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
      {art.length > 0 && (
        <div className="choice-subject">
          {art.map((ref) => (
            <CardTile key={ref.key} art={ref} label={ref.label} size="lg" />
          ))}
        </div>
      )}
      {yourMove && <AnswerPanel key={version} onMove={onMove} />}
      {rejected && (
        <p className="rejected" role="alert">
          {rejected.reason === 'STALE' ? 'The game moved on: try again.' : rejected.message}
        </p>
      )}
    </section>
  );
}
