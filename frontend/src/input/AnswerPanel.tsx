import { useState } from 'react';
import { ArtImg, ArtStrip, CardBack } from '../art/Art';
import { cardArt, shipArt, skillCardArt, type ArtRef } from '../art/images';
import { recordArt, unique } from '../art/refs';
import { constantLabel, eventParts, skillCardLabel, typeLabel } from '../labels/describe';
import { partArt, Words } from '../art/Words';
import { useTable } from '../table/tableContext';
import { sortedPositions } from '../table/skillCards';
import { enumValues, recordNamed } from '../wire/schema';
import type { SkillCard } from '../wire/types';
import { canonical, decisionAction, fillPick, groupMoves, moves, narrow, pickElements, varyingComponents, type Answer, type Move, type MoveGroup } from './answers';
import { groupTitle, moveDetail, moveSentence, pickHint, pickName, shownComponents } from './moveLabels';
import { useSelection } from './selection';

export interface AnswerPanelProps {
  // sends a move, with its pick filled in
  onMove(move: Move, event: Answer | null): void;
}

/**
 * The answers of the pending input, for the viewer it asks: a choice's options, an option that asks a decision unfolded
 * into its answers, and a decision's answers grouped by what they do. A shape opens its picker. What the board and
 * the mats picked narrows them, and "Back" and "Clear" take picks away.
 */
export function AnswerPanel({ onMove }: AnswerPanelProps) {
  const table = useTable();
  const input = table.game?.input;
  const picked = useSelection((state) => state.picked);
  const picking = useSelection((state) => state.picking);
  const all = moves(input);
  const left = narrow(all, picked);
  if (all.length === 0) {
    return null;
  }
  const send = (move: Move, event: Answer | null = move.event) => {
    useSelection.getState().clear();
    onMove(move, event);
  };
  const constraints = input?.kind === 'Decide' ? (input.constraints ?? []).filter((constraint) => constraint !== null) : [];
  return (
    <div className="answers" role="group" aria-label="answers">
      {constraints.length > 0 && <p className="constraint">{constraints.map((constraint) => constantLabel('EventConstraint', constraint)).join(', ')}</p>}
      {picking ? (
        <div className="selection-bar">
          <button type="button" className="link" onClick={() => useSelection.getState().startPick(null)}>
            Back to the answers
          </button>
        </div>
      ) : (
        picked.length > 0 && <SelectionBar count={left.length} />
      )}
      {picking ? (
        <PickWidget move={picking} onSend={(event) => send(picking, event)} />
      ) : input?.kind === 'Choose' ? (
        <ChoiceAnswers moves={left} narrowed={picked.length > 0} onMove={send} />
      ) : (
        <Groups moves={left} onMove={send} />
      )}
    </div>
  );
}

function SelectionBar({ count }: { count: number }) {
  return (
    <div className="selection-bar">
      <span className="muted">{count === 0 ? 'Nothing fits these picks' : `${count} answer${count === 1 ? '' : 's'} left`}</span>
      <button type="button" className="link" onClick={() => useSelection.getState().back()}>
        Back
      </button>
      <button type="button" className="link" onClick={() => useSelection.getState().clear()}>
        Clear
      </button>
    </div>
  );
}

// The options in their order: an option that asks a decision is a section of its answers, the first one open.
function ChoiceAnswers({ moves: left, narrowed, onMove }: { moves: Move[]; narrowed: boolean; onMove(move: Move): void }) {
  const table = useTable();
  const options = [...new Set(left.map((move) => move.option as number))];
  const sections = options.filter((option) => left.some((move) => move.option === option && move.submits));
  const [open, setOpen] = useState<number | null>(sections[0] ?? null);
  // every option names the same thing (the card a section is chosen of): the panel shows it once above them
  const shared = sharedArt(options.map((option) => left.find((move) => move.option === option && !move.submits)?.event ?? null));
  return (
    <div className={optionsClass(left, options)}>
      {options.map((option) => {
        const own = left.filter((move) => move.option === option);
        const action = decisionAction(own[0]);
        if (own[0]?.submits && action) {
          const expanded = narrowed || open === option;
          return (
            <section key={option} className={`answer-section ${expanded ? 'open' : ''}`.trim()}>
              <button type="button" className="option section-head" aria-expanded={expanded} onClick={() => setOpen(expanded ? null : option)}>
                {typeLabel(action)}
              </button>
              {expanded && <Groups moves={own} onMove={onMove} section={typeLabel(action)} />}
            </section>
          );
        }
        const move = own[0];
        const art = answerArt(move).filter((ref) => !shared.has(ref.key));
        return (
          <button key={option} type="button" className="option" aria-label={moveSentence(move, table)} onClick={() => onMove(move)}>
            <ArtStrip refs={art} />
            {/* a card it names shows on hover */}
            <span>{move.event ? <Words parts={eventParts(move.event, { ...table, artOf: partArt })} /> : moveSentence(move, table)}</span>
          </button>
        );
      })}
    </div>
  );
}

// Options that each name their own card lie as a grid of cards.
function optionsClass(left: Move[], options: number[]): string {
  const plain = options.map((option) => left.find((move) => move.option === option && !move.submits)).filter((move) => move !== undefined);
  const perOption = plain.map((move) => (move.event ? recordArt(move.event) : []));
  const distinct = plain.length > 2 && plain.length === options.length && perOption.every((refs) => refs.length > 0) && new Set(perOption.map((refs) => refs.map((ref) => ref.key).join())).size === plain.length;
  return distinct ? 'options card-grid' : 'options';
}

function sharedArt(events: (Answer | null)[]): Set<string> {
  const refs = events.map((event) => (event ? recordArt(event) : []));
  if (refs.length < 2) {
    return new Set();
  }
  return new Set(refs[0].filter((ref) => refs.every((other) => other.some((candidate) => candidate.key === ref.key))).map((ref) => ref.key));
}

// A section's lone group doesn't repeat the section's name.
export function Groups({ moves: left, onMove, section }: { moves: Move[]; onMove(move: Move): void; section?: string }) {
  const table = useTable();
  const groups = groupMoves(left);
  return (
    <div className="answer-groups">
      {groups.map((group) => (
        <GroupView
          key={group.key}
          group={group}
          onMove={onMove}
          untitled={groups.length === 1 && groupTitle(group, table) === section}
          narrowable={groups.length > 1}
        />
      ))}
    </div>
  );
}

// The board shows the locations: an answer names one without its card. A shape's example of its pick isn't what will
// be picked: it shows nothing.
function answerArt(move: Move | undefined): ArtRef[] {
  const event = move?.event;
  if (!event) {
    return [];
  }
  const shown = move.pick?.component ? { ...event, [move.pick.component]: null } : event;
  return recordArt(shown).filter((ref) => ref.shape !== 'location');
}

function GroupView({ group, onMove, untitled = false, narrowable = false }: { group: MoveGroup; onMove(move: Move): void; untitled?: boolean; narrowable?: boolean }) {
  const table = useTable();
  const title = groupTitle(group, table);
  const varying = varyingComponents(group);
  const arts = group.moves.map((move) => answerArt(move));
  const common = arts[0]?.filter((ref) => arts.every((refs) => refs.some((candidate) => candidate.key === ref.key))) ?? [];
  const own = arts.map((refs) => refs.filter((ref) => !common.some((candidate) => candidate.key === ref.key)));
  const start = (move: Move) => (move.pick ? useSelection.getState().startPick(move) : onMove(move));

  // a lone answer that does nothing, e.g. no cards into a skill check, says so under the group's name
  const lone = group.moves.length === 1 ? moveDetail(group.moves[0], shownComponents(group.moves[0]), table) : null;
  if (group.moves.length === 1 && lone !== 'Pass') {
    const move = group.moves[0];
    const detail = lone;
    const shown = detail && detail !== 'Pass' ? detail : null;
    return (
      <button
        type="button"
        className="option answer"
        data-kind={group.kind ?? undefined}
        aria-label={shown ? `${title}: ${shown}` : title}
        title={moveSentence(move, table)}
        onClick={() => start(move)}
      >
        <ArtStrip refs={unique(arts[0] ?? [])} />
        <span>
          {title}
          {shown && <small className="detail">{shown}</small>}
        </span>
      </button>
    );
  }
  const counts = countsComponent(group, varying);
  return (
    <div className="answer-group" data-kind={group.kind ?? undefined}>
      {!untitled &&
        (narrowable && group.kind ? (
          // keeps this kind only, so the board lights its targets
          <button type="button" className="group-title link" title="Show only these, on the board too" onClick={() => useSelection.getState().pick({ type: 'kind', kind: group.kind! })}>
            <ArtStrip refs={common} />
            <span>{title}</span>
          </button>
        ) : (
          <div className="group-title">
            <ArtStrip refs={common} />
            <span>{title}</span>
          </div>
        ))}
      {counts ? (
        <CountsWidget group={group} component={counts} onMove={onMove} />
      ) : (
        <div className={distinctArt(own) ? 'chips card-grid' : 'chips'}>
          {group.moves.map((move, index) => {
            const label = (lone ?? moveDetail(move, varying, table)) || title;
            return (
              <button key={index} type="button" className="chip" aria-label={label} title={moveSentence(move, table)} onClick={() => start(move)}>
                <ArtStrip refs={own[index]} />
                <span>{label}</span>
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}

function distinctArt(own: ArtRef[][]): boolean {
  return own.length > 2 && own.every((refs) => refs.length > 0) && new Set(own.map((refs) => refs.map((ref) => ref.key).join())).size === own.length;
}

// A draw: the moves differ only in a count per color.
function countsComponent(group: MoveGroup, varying: string[]): string | null {
  if (varying.length !== 1 || group.moves.some((move) => move.pick)) {
    return null;
  }
  const component = recordNamed(group.kind ?? '')?.components.find((candidate) => candidate.name === varying[0]);
  return component?.type.t === 'map' && component.type.key?.t === 'enum' ? varying[0] : null;
}

/**
 * A count per key, held to what some answer still allows: a "+" only while an answer has more of it, sent when the
 * counts are one of the answers.
 */
function CountsWidget({ group, component, onMove }: { group: MoveGroup; component: string; onMove(move: Move): void }) {
  const record = recordNamed(group.kind ?? '');
  const keyEnum = record?.components.find((candidate) => candidate.name === component)?.type.key?.name ?? '';
  const answers = group.moves.map((move) => (move.event?.[component] ?? {}) as Record<string, number>);
  const keys = enumValues(keyEnum).filter((key) => answers.some((answer) => (answer[key] ?? 0) > 0));
  const [counts, setCounts] = useState<Record<string, number>>({});
  const fits = (next: Record<string, number>) => answers.some((answer) => keys.every((key) => (next[key] ?? 0) <= (answer[key] ?? 0)));
  const exact = group.moves.find((move) => canonical(move.event?.[component] ?? {}) === canonical(counts));
  const total = Object.values(counts).reduce((sum, count) => sum + count, 0);
  const most = Math.max(...answers.map((answer) => Object.values(answer).reduce((sum, count) => sum + count, 0)));
  const change = (key: string, by: number) => setCounts({ ...counts, [key]: Math.max(0, (counts[key] ?? 0) + by) });
  return (
    <div className="counts-widget">
      {keys.map((key) => {
        const count = counts[key] ?? 0;
        const name = constantLabel(keyEnum, key);
        return (
          <div key={key} className={`count-row ${count > 0 ? 'on' : ''}`.trim()} data-key={key}>
            {keyEnum === 'SkillCardColor' ? <CardBack color={key} count={count} size="xs" /> : <span className="tile">{count}</span>}
            <span className="count-name">{name}</span>
            <button type="button" className="step" aria-label={`fewer ${name}`} disabled={count === 0} onClick={() => change(key, -1)}>
              −
            </button>
            <button type="button" className="step" aria-label={`more ${name}`} disabled={!fits({ ...counts, [key]: count + 1 })} onClick={() => change(key, 1)}>
              +
            </button>
          </div>
        );
      })}
      <div className="actions">
        <span className={exact ? 'count-ok' : 'count-off'}>
          {total} of {most}
        </span>
        <button type="button" className="primary" disabled={!exact} onClick={() => exact && onMove(exact)}>
          {typeLabel(group.kind ?? '')}
        </button>
        <button type="button" className="link" disabled={total === 0} onClick={() => setCounts({})}>
          Clear
        </button>
      </div>
    </div>
  );
}

/**
 * A shape's part to pick: its cards or ships as tiles to toggle, how many are picked against the pick's bounds, the
 * strength against its minimum, and the order of the clicks when the order counts.
 */
function PickWidget({ move, onSend }: { move: Move; onSend(event: Answer | null): void }) {
  const table = useTable();
  const pick = move.pick!;
  const elements = pickElements(pick);
  const [picked, setPicked] = useState<number[]>([]);
  const cards = pick.kind === 'SkillCardPick' ? (elements as SkillCard[]) : null;
  const order = cards ? sortedPositions(cards) : elements.map((_, position) => position);
  const strength = cards ? picked.reduce((sum, position) => sum + cards[position].value, 0) : 0;
  const minStrength = pick.kind === 'SkillCardPick' ? pick.minStrength : 0;
  const ok = picked.length >= pick.min && picked.length <= pick.max && strength >= minStrength;
  const toggle = (position: number) => {
    if (picked.includes(position)) {
      setPicked(picked.filter((candidate) => candidate !== position));
    } else if (picked.length < pick.max) {
      setPicked([...picked, position]);
    }
  };
  const ordered = 'ordered' in pick && pick.ordered;
  const group = groupMoves([move])[0];
  return (
    <div className="pick-widget">
      <p className="group-title">
        {groupTitle(group, table)}
        {(() => {
          const detail = moveDetail({ ...move, pick: null }, shownComponents(move).filter((name) => name !== pick.component), table);
          return detail && detail !== 'Pass' ? <small className="detail">{detail}</small> : null;
        })()}
      </p>
      <p className="constraint">
        {pickName(pick)}: {pickHint(pick)}
        <span className={ok ? 'count-ok' : 'count-off'}>
          {' · '}
          {picked.length} picked
          {minStrength > 0 && `, strength ${strength} of ${minStrength}`}
        </span>
      </p>
      <div className="picker">
        {order.map((position) => {
          const element = elements[position];
          const at = picked.indexOf(position);
          const { art, label } = elementArt(pick.kind, element, table);
          return (
            <button
              key={position}
              type="button"
              className={['tile', 'card-pick', at >= 0 ? 'picked' : ''].join(' ').trim()}
              aria-pressed={at >= 0}
              aria-label={label}
              onClick={() => toggle(position)}
            >
              {art && <ArtImg art={art} />}
              <span>{label}</span>
              {ordered && at >= 0 && <b className="order">{at + 1}</b>}
            </button>
          );
        })}
      </div>
      <div className="actions">
        <button type="button" className="primary" disabled={!ok} onClick={() => onSend(fillPick(move, picked.map((position) => elements[position])))}>
          Send
        </button>
        <button type="button" className="link" disabled={picked.length === 0} onClick={() => setPicked([])}>
          Clear
        </button>
      </div>
    </div>
  );
}

function elementArt(kind: string, element: unknown, table: ReturnType<typeof useTable>): { art: ArtRef | null; label: string } {
  switch (kind) {
    case 'SkillCardPick':
      return { art: skillCardArt(element as SkillCard), label: skillCardLabel(element as SkillCard) };
    case 'QuorumCardPick':
      return { art: cardArt('QuorumCard', element as string), label: constantLabel('QuorumCard', element as string) };
    default:
      return { art: shipArt(table.ship(element as number)?.type), label: table.shipName?.(element as number) ?? `ship #${element}` };
  }
}
