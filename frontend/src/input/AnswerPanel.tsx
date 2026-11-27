import {useState} from 'react';
import {ArtImg, ArtStrip, CardBack} from '../art/Art';
import {type ArtRef, CARD_ENUMS, skillCardArt} from '../art/images';
import {choiceArt, unique} from '../art/refs';
import {actionArt, isNoOp, moveArt, partValueArt, staysOnEachAnswer} from './answerArt';
import {ArtSlot, NextIcon} from './ArtSlot';
import {constantLabel, optionEventParts, skillCardLabel, typeLabel, valueLabel} from '../labels/describe';
import {partArt, Words} from '../art/Words';
import {useTable} from '../table/tableContext';
import {sortedPositions} from '../table/skillCards';
import {characterType, enumValues, recordNamed, type TypeRef} from '../wire/schema';
import type {Pick, SkillCard} from '../wire/types';
import {
  type Answer,
  canonical,
  decisionAction,
  fillPick,
  groupMoves,
  type Move,
  type MoveGroup,
  moves,
  narrow,
  pickElements,
  varyingComponents
} from './answers';
import {
  groupTitle,
  moveDetail,
  moveSentence,
  partHint,
  partName,
  pickHint,
  pickName,
  shownComponents
} from './moveLabels';
import {
  allows,
  distinct,
  fitting,
  isProduct,
  optionGroups,
  type Part,
  type PartPicks,
  partsOf,
  settled,
  shared,
  sizes
} from './parts';
import {useSelection} from './selection';

export interface AnswerPanelProps {
    onMove(move: Move, event: Answer | null): void;
}

export function AnswerPanel({onMove}: AnswerPanelProps) {
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
            {constraints.length > 0 &&
                <p className="constraint">{constraints.map((constraint) => constantLabel('EventConstraint', constraint)).join(', ')}</p>}
            {picking ? (
                <div className="selection-bar">
                    <button type="button" className="link" onClick={() => useSelection.getState().startPick(null)}>
                        Back to the answers
                    </button>
                </div>
            ) : (
                picked.length > 0 && <SelectionBar count={left.length}/>
            )}
            {picking ? (
                <PickWidget move={picking} onSend={(event) => send(picking, event)}/>
            ) : input?.kind === 'Choose' ? (
                <ChoiceAnswers moves={left} narrowed={picked.length > 0} onMove={send}/>
            ) : (
                <Groups moves={left} onMove={send}/>
            )}
        </div>
    );
}

function SelectionBar({count}: { count: number }) {
    return (
        <div className="selection-bar">
            <span
                className="muted">{count === 0 ? 'Nothing fits these picks' : `${count} answer${count === 1 ? '' : 's'} left`}</span>
            <button type="button" className="link" onClick={() => useSelection.getState().back()}>
                Back
            </button>
            <button type="button" className="link" onClick={() => useSelection.getState().clear()}>
                Clear
            </button>
        </div>
    );
}

function ChoiceAnswers({moves: left, narrowed, onMove}: {
    moves: Move[];
    narrowed: boolean;
    onMove(move: Move): void
}) {
    const table = useTable();
    // the Pass comes last
    const listed = [...new Set(left.map((move) => move.option as number))];
    const passes = (option: number) => left.some((move) => move.option === option && isNoOp(move));
    const options = [...listed.filter((option) => !passes(option)), ...listed.filter(passes)];
    const [open, setOpen] = useState<number | null>(null);
    // what every option names shows once above them (the Destination Cards drawn), not on each
    const common = new Set(choiceArt(table.game?.input?.kind === 'Choose' ? (table.game.input.options ?? []) : []).map((ref) => ref.key));
    const products = productOptions(left);
    return (
        <div className="options">
            {options.map((option) => {
                // options that are a product of their parts, e.g. every pair of damage tokens, are picked part by part where
                // the first of them is
                const product = products.find((candidate) => candidate.options.has(option));
                if (product && product.first === option && product.group.kind && options.length > product.options.size) {
                    // among other options it is a row that opens it
                    const kind = product.group.kind;
                    return <GroupRow key={option} group={product.group}
                                     art={groupArt(product.group.moves.map((move) => moveArt(move, table)))}
                                     onOpen={() => pickKind(kind)}/>;
                }
                if (product) {
                    return product.first === option ? (
                        <div key={option} className="answer-group" data-kind={product.group.kind ?? undefined}>
                            <div className="group-title">
                                <span>{groupTitle(product.group, table)}</span>
                            </div>
                            <PartsWidget group={product.group} parts={product.parts} onMove={onMove}/>
                        </div>
                    ) : null;
                }
                const own = left.filter((move) => move.option === option);
                const action = decisionAction(own[0]);
                if (own[0]?.submits && action) {
                    const expanded = narrowed || open === option;
                    return (
                        <section key={option} className={`answer-section ${expanded ? 'open' : ''}`.trim()}>
                            <button type="button" className="option answer-row section-head" aria-expanded={expanded}
                                    aria-label={typeLabel(action)} onClick={() => setOpen(expanded ? null : option)}>
                                <ArtSlot refs={actionArt(own[0])}/>
                                <span>{typeLabel(action)}</span>
                            </button>
                            {expanded && <Groups moves={own} onMove={onMove} section={typeLabel(action)}/>}
                        </section>
                    );
                }
                const move = own[0];
                // an option that names only what shows above keeps the first of it, e.g. the card it gives away
                const named = moveArt(move, table);
                const ownArt = named.filter((ref) => !common.has(ref.key));
                const art = ownArt.length > 0 ? ownArt : named.slice(0, 1);
                const pass = isNoOp(move);
                return (
                    <button key={option} type="button" className={`option answer-row${pass ? ' pass' : ''}`}
                            aria-label={moveSentence(move, table)} onClick={() => onMove(move)}>
                        <ArtSlot refs={art} pass={pass}/>
                        <span>{move.event ? <Words parts={optionEventParts(move.event, {
                            ...table,
                            artOf: partArt
                        })}/> : moveSentence(move, table)}</span>
                    </button>
                );
            })}
        </div>
    );
}

interface ProductOptions {
    group: MoveGroup;
    parts: Part[];
    options: Set<number>;
    first: number;
}

function productOptions(left: Move[]): ProductOptions[] {
    return optionGroups(left).flatMap((group): ProductOptions[] => {
        const parts = partsOf(group);
        const shown = distinct(group.moves, parts);
        if (!isProduct(shown, parts)) {
            return [];
        }
        const options = new Set(group.moves.map((move) => move.option as number));
        return [{group: {...group, moves: shown}, parts, options, first: Math.min(...options)}];
    });
}

// the default way to open a group: its kind is picked, so the panel shows only it and the board lights its targets
const pickKind = (kind: string) => useSelection.getState().pick({type: 'kind', kind});

export function Groups({moves: left, onMove, section, onOpen = pickKind, openInPlace = false}: {
    moves: Move[];
    onMove(move: Move): void;
    section?: string;
    onOpen?(kind: string): void;
    // opens a group here instead, where the picks don't narrow the answers (the anytime panel)
    openInPlace?: boolean;
}) {
    const table = useTable();
    const [opened, setOpened] = useState<string | null>(null);
    const listed = groupMoves(left);
    const passing = listed.flatMap((group) => passesOf(group, table).map((move) => ({move, title: groupTitle(group, table)})));
    // a group keeps what told its answers apart from its pass, e.g. the card of an interrupt card's play
    const groups = listed
        .map((group) => {
            const kept = group.moves.filter((move) => !passing.some((pass) => pass.move === move));
            return {group: {...group, moves: kept}, varying: kept.length < group.moves.length ? varyingComponents(group) : undefined};
        })
        .filter(({group}) => group.moves.length > 0);
    const named = new Set(passing.map((pass) => pass.title)).size > 1;
    const inPlace = openInPlace ? groups.find(({group}) => group.key === opened) : undefined;
    if (inPlace) {
        return (
            <div className="answer-groups">
                <div className="selection-bar">
                    <button type="button" className="link" onClick={() => setOpened(null)}>
                        Back to the answers
                    </button>
                </div>
                <GroupView group={inPlace.group} varying={inPlace.varying} onMove={onMove}/>
            </div>
        );
    }
    const open = (group: MoveGroup) => (openInPlace ? setOpened(group.key) : group.kind && onOpen(group.kind));
    return (
        <div className="answer-groups">
            {groups.map(({group, varying}) => (
                <GroupView
                    key={group.key}
                    group={group}
                    varying={varying}
                    onMove={onMove}
                    untitled={groups.length === 1 && groupTitle(group, table) === section}
                    onOpen={groups.length > 1 ? () => open(group) : undefined}
                />
            ))}
            {passing.map(({move, title}, index) => {
                const label = named && title !== 'Pass' ? `Pass: ${title}` : 'Pass';
                return (
                    <button key={`pass:${index}`} type="button" className="option answer-row pass" aria-label={label}
                            title={moveSentence(move, table)} onClick={() => onMove(move)}>
                        <ArtSlot refs={[]} pass/>
                        <span>
                            Pass
                            {label !== 'Pass' && <small className="detail">{title}</small>}
                        </span>
                    </button>
                );
            })}
        </div>
    );
}

/**
 * The answers of a group that pass: a `NoOpEvent`, or one that names none of what its group's other answers name, e.g.
 * no interrupt card. A draw's counts and the parts of a product keep theirs, which they pick among.
 */
function passesOf(group: MoveGroup, table: ReturnType<typeof useTable>): Move[] {
    if (group.kind === 'NoOpEvent') {
        return group.moves;
    }
    const varying = varyingComponents(group);
    const parts = partsOf(group);
    if (countsComponent(group, varying) || isProduct(distinct(group.moves, parts), parts)) {
        return [];
    }
    if (group.moves.length === 1) {
        return moveDetail(group.moves[0], shownComponents(group.moves[0]), table) === 'Pass' ? group.moves : [];
    }
    return group.moves.filter((move) => moveDetail(move, varying, table) === 'Pass');
}

function GroupView({group: listed, varying: before, onMove, untitled = false, onOpen}: {
    group: MoveGroup;
    varying?: string[];
    onMove(move: Move): void;
    untitled?: boolean;
    onOpen?(): void;
}) {
    const table = useTable();
    // a pair of cards in either order is one answer
    const parts = partsOf(listed);
    const group = {...listed, moves: distinct(listed.moves, parts)};
    const product = isProduct(group.moves, parts);
    const title = groupTitle(group, table);
    const varying = before ?? varyingComponents(group);
    const arts = group.moves.map((move) => moveArt(move, table));
    // what every answer shows goes with the group's name, but the miracle token and a move's step stay on each button,
    // and all of it stays where an answer would be left without an image (e.g. the card of "Calculations" beside each
    // change of the roll)
    const shared = arts[0]?.filter((ref) => !staysOnEachAnswer(ref) && arts.every((refs) => refs.some((candidate) => candidate.key === ref.key))) ?? [];
    const bare = arts.some((refs) => refs.every((ref) => shared.some((candidate) => candidate.key === ref.key)));
    const common = bare ? [] : shared;
    const own = arts.map((refs) => refs.filter((ref) => !common.some((candidate) => candidate.key === ref.key)));
    const start = (move: Move) => (move.pick ? useSelection.getState().startPick(move) : onMove(move));

    // a lone answer that does nothing, e.g. no cards into a skill check, says so under the group's name; one whose pass
    // went last says what told them apart
    const lone = group.moves.length === 1 ? moveDetail(group.moves[0], before ?? shownComponents(group.moves[0]), table) : null;
    if (group.moves.length === 1 && lone !== 'Pass') {
        const move = group.moves[0];
        const shown = lone || null;
        return (
            <button
                type="button"
                className="option answer answer-row"
                data-kind={group.kind ?? undefined}
                aria-label={shown ? `${title}: ${shown}` : title}
                title={moveSentence(move, table)}
                onClick={() => start(move)}
            >
                <ArtSlot refs={arts[0] ?? []}/>
                <span>
                    {title}
                    {shown && <small className="detail">{shown}</small>}
                </span>
            </button>
        );
    }
    if (onOpen) {
        return <GroupRow group={group} art={groupArt(arts)} onOpen={onOpen}/>;
    }
    const counts = countsComponent(group, varying);
    const pools = characterPools(group, varying);
    // the characters to choose stand as large as their portraits, side by side; every other answer is a row
    const grid = pools !== null && distinctArt(own);
    const chip = (move: Move, index: number) => {
        const label = (lone ?? moveDetail(move, varying, table)) || title;
        return (
            <button key={index} type="button" className={grid ? 'chip' : 'chip answer-row'} aria-label={label}
                    title={moveSentence(move, table)} onClick={() => start(move)}>
                {grid ? <ArtStrip refs={own[index]}/> : <ArtSlot refs={own[index]}/>}
                <span>{label}</span>
            </button>
        );
    };
    const chipsClass = grid ? 'chips card-grid' : 'chips';
    return (
        <div className="answer-group" data-kind={group.kind ?? undefined}>
            {!untitled && (
                <div className="group-title">
                    <ArtStrip refs={common}/>
                    <span>{title}</span>
                </div>
            )}
            {counts ? (
                <CountsWidget group={group} component={counts} onMove={onMove}/>
            ) : product ? (
                <PartsWidget group={group} parts={parts} onMove={start}/>
            ) : pools ? (
                pools.map(({pool, at}) => (
                    <div key={pool} className="pool" role="group" aria-label={pool}>
                        <p className="pool-name">{pool}</p>
                        <div className={chipsClass}>{at.map((index) => chip(group.moves[index], index))}</div>
                    </div>
                ))
            ) : (
                <div className={chipsClass}>{group.moves.map(chip)}</div>
            )}
        </div>
    );
}

function GroupRow({group, art, onOpen}: { group: MoveGroup; art: ArtRef[]; onOpen(): void }) {
    const table = useTable();
    const title = groupTitle(group, table);
    return (
        <button type="button" className="option answer-row group-row" data-kind={group.kind ?? undefined}
                aria-label={title} title={`Open its ${group.moves.length} answers`} onClick={onOpen}>
            <ArtSlot refs={art}/>
            <span>{title}</span>
            <NextIcon/>
        </button>
    );
}

function groupArt(arts: ArtRef[][]): ArtRef[] {
    const everyOne = arts[0]?.filter((ref) => arts.every((refs) => refs.some((candidate) => candidate.key === ref.key))) ?? [];
    return everyOne.length > 0 ? everyOne : unique(arts.flatMap((refs) => refs.slice(0, 1))).slice(0, 3);
}

// the pools of character types, as the setup deals them out
const POOLS = ['MILITARY_LEADER', 'POLITICAL_LEADER', 'PILOT', 'SUPPORT', 'CYLON_LEADER'];

/**
 * The characters to choose from at the start and after an execution, by the type their sheets print: the pool's name
 * and the positions of its moves. A choice among the players' characters, e.g. the new President, is rows.
 */
function characterPools(group: MoveGroup, varying: string[]): { pool: string; at: number[] }[] | null {
    const component = recordNamed(group.kind ?? '')?.components.find((candidate) => candidate.name === varying[0]);
    if (group.kind !== 'SelectCharacterEvent' || varying.length !== 1 || component?.type.t !== 'enum' || component.type.name !== 'Character') {
        return null;
    }
    const types = group.moves.map((move) => characterType(move.event?.[varying[0]] as string | null) ?? '');
    // a type outside the pools comes last rather than going missing
    return [...new Set([...POOLS, ...types])]
        .map((type) => ({
            pool: type ? constantLabel('CharacterType', type) : 'Other',
            at: types.flatMap((candidate, at) => (candidate === type ? [at] : []))
        }))
        .filter(({at}) => at.length > 0);
}

function distinctArt(own: ArtRef[][]): boolean {
    return own.length > 2 && own.every((refs) => refs.length > 0) && new Set(own.map((refs) => refs.map((ref) => ref.key).join())).size === own.length;
}

function countsComponent(group: MoveGroup, varying: string[]): string | null {
    if (varying.length !== 1 || group.moves.some((move) => move.pick)) {
        return null;
    }
    const component = recordNamed(group.kind ?? '')?.components.find((candidate) => candidate.name === varying[0]);
    return component?.type.t === 'map' && component.type.key?.t === 'enum' ? varying[0] : null;
}

/**
 * A count per key, e.g. the Skill Cards of each color to draw, held to what some answer still allows. What every answer
 * has is picked from the start, e.g. the one-color options of a skill set in the Receive Skills step, and a key every
 * answer has as many of is fixed and comes first, so what is left to pick are the keys that share a count, e.g. a
 * "1xL/E" option. A click on a key takes one more, and once it has the most an answer leaves it beside the other
 * counts, it goes back to what every answer has. A key no answer has isn't offered, and one the other counts leave no
 * room for is disabled in its place. Sent when the counts are one of the answers.
 */
function CountsWidget({group, component, onMove}: { group: MoveGroup; component: string; onMove(move: Move): void }) {
    const record = recordNamed(group.kind ?? '');
    const keyEnum = record?.components.find((candidate) => candidate.name === component)?.type.key?.name ?? '';
    const answers = group.moves.map((move) => (move.event?.[component] ?? {}) as Record<string, number>);
    const offered = enumValues(keyEnum).filter((key) => answers.some((answer) => (answer[key] ?? 0) > 0));
    const least = (key: string) => Math.min(...answers.map((answer) => answer[key] ?? 0));
    const fixed = (key: string) => least(key) === Math.max(...answers.map((answer) => answer[key] ?? 0));
    const keys = [...offered.filter(fixed), ...offered.filter((key) => !fixed(key))];
    const given = Object.fromEntries(offered.filter((key) => least(key) > 0).map((key) => [key, least(key)]));
    const [counts, setCounts] = useState<Record<string, number>>(given);
    const room = (key: string) =>
        Math.max(0, ...answers.filter((answer) => Object.entries(counts).every(([other, count]) => other === key || count <= (answer[other] ?? 0))).map((answer) => answer[key] ?? 0));
    const exact = group.moves.find((move) => canonical(move.event?.[component] ?? {}) === canonical(counts));
    const total = Object.values(counts).reduce((sum, count) => sum + count, 0);
    const most = Math.max(...answers.map((answer) => Object.values(answer).reduce((sum, count) => sum + count, 0)));
    const cycle = (key: string) => {
        const count = counts[key] ?? 0;
        const next = {...counts};
        if (count < room(key)) {
            next[key] = count + 1;
        } else if (least(key) > 0) {
            next[key] = least(key);
        } else {
            delete next[key];
        }
        setCounts(next);
    };
    return (
        <div className="counts-widget">
            <div className="count-keys">
                {keys.map((key) => {
                    const count = counts[key] ?? 0;
                    const limit = room(key);
                    const floor = least(key);
                    const name = constantLabel(keyEnum, key);
                    // at what every answer has, and the other counts leave it no more
                    const closed = count <= floor && limit <= floor;
                    const hint = fixed(key)
                        ? `${name}: ${floor} in every answer`
                        : closed
                            ? `${name}: no room ${count > 0 ? 'for more ' : ''}beside the others`
                            : count < limit
                                ? `${name}: take ${count + 1}`
                                : `${name}: back to ${floor > 0 ? floor : 'none'}`;
                    return (
                        <button
                            key={key}
                            type="button"
                            className={`tile card-pick count-key ${count > 0 ? 'picked' : ''} ${fixed(key) ? 'fixed' : ''}`.trim()}
                            data-key={key}
                            // the next click puts it back to what every answer has
                            data-full={(!closed && count >= limit) || undefined}
                            aria-label={name}
                            aria-pressed={count > 0}
                            disabled={closed}
                            title={hint}
                            onClick={() => cycle(key)}
                        >
                            {keyEnum === 'SkillCardColor' ?
                                <CardBack color={key} count={count} size="sm" label={hint}/> :
                                <b className="count-value">{count}</b>}
                            <span className="count-name">{name}</span>
                            {/* how many it holds of the most it can take, what every answer has apart */}
                            <span className="pips" aria-hidden="true">
                {Array.from({length: limit}, (_, at) => (
                    <i key={at} className={at < floor ? 'pip on given' : at < count ? 'pip on' : 'pip'}/>
                ))}
              </span>
                        </button>
                    );
                })}
            </div>
            <div className="actions">
        <span className={exact ? 'count-ok' : 'count-off'}>
          {total} of {most}
        </span>
                <button type="button" className="primary" disabled={!exact} onClick={() => exact && onMove(exact)}>
                    {typeLabel(group.kind ?? '')}
                </button>
                <button type="button" className="link" disabled={canonical(counts) === canonical(given)}
                        onClick={() => setCounts(given)}>
                    Clear
                </button>
            </div>
        </div>
    );
}

/**
 * A group whose answers are a product of their parts, e.g. two cards of the hand or a target, a destination and a
 * drawer: a row per part, each value open while an answer still fits the picks, and the answer the picks name in full
 * sent as the engine listed it. A part that every answer left agrees on shows its value as implied; answers that differ
 * only in a shape stay to choose from.
 */
function PartsWidget({group, parts, onMove}: { group: MoveGroup; parts: Part[]; onMove(move: Move): void }) {
    const table = useTable();
    const [positions, setPositions] = useState<Record<string, number[]>>({});
    const picks: PartPicks = Object.fromEntries(parts.map((part) => [part.key, (positions[part.key] ?? []).map((at) => part.values[at])]));
    const left = fitting(group.moves, parts, picks);
    const done = settled(group.moves, parts, picks);
    const toggle = (part: Part, at: number) => {
        const own = positions[part.key] ?? [];
        const next = own.includes(at) ? own.filter((candidate) => candidate !== at) : part.many ? [...own, at] : [at];
        setPositions({...positions, [part.key]: next});
    };
    const title = groupTitle(group, table);
    return (
        <div className="parts-widget">
            {parts.map((part) => {
                const own = positions[part.key] ?? [];
                const implied = own.length === 0 ? impliedPositions(part, shared(left, part)) : [];
                const order = isSkillCard(part.type) ? cardOrder(part.values) : part.values.map((_, at) => at);
                const tiles = order.map((at) => ({at, ...valueTile(part.values[at], part.type, table, part.key)}));
                // cards and tokens lie as tiles, one without an image too; anything else is a row
                const asTiles = isCardLike(part.type);
                const name = partName(part);
                return (
                    <div key={part.key} className="part" role="group" aria-label={name}>
                        <p className="part-name">
                            {name}
                            {part.many && <small
                                className="detail">{partHint(part, sizes(left.length > 0 ? left : group.moves, part))}</small>}
                        </p>
                        <div className={asTiles ? 'picker' : 'chips'}>
                            {tiles.map(({at, arts, label}) => {
                                const pressed = own.includes(at);
                                return (
                                    <button
                                        key={at}
                                        type="button"
                                        className={[asTiles ? 'tile card-pick' : 'chip answer-row', 'part-value', pressed ? 'picked' : '', implied.includes(at) ? 'implied' : ''].filter((name) => name).join(' ')}
                                        aria-pressed={pressed}
                                        aria-label={label}
                                        disabled={!pressed && !allows(group.moves, parts, picks, part, part.values[at])}
                                        onClick={() => toggle(part, at)}
                                    >
                                        {asTiles ? arts[0] && <ArtImg art={arts[0]}/> :
                                            <ArtSlot refs={arts} pass={part.values[at] === null}/>}
                                        <span className={asTiles ? 'pick-label' : undefined}>{label}</span>
                                    </button>
                                );
                            })}
                        </div>
                    </div>
                );
            })}
            {done.length > 1 && (
                // the picks name these in full: they differ in what a shape picks next
                <div className="chips part-moves">
                    {done.map((move, index) => {
                        const label = moveDetail(move, varyingComponents({...group, moves: done}), table) || title;
                        return (
                            <button key={index} type="button" className="chip answer-row" aria-label={label}
                                    title={moveSentence(move, table)} onClick={() => onMove(move)}>
                                <ArtSlot refs={moveArt(move, table)}/>
                                <span>{label}</span>
                            </button>
                        );
                    })}
                </div>
            )}
            <div className="actions">
                <button type="button" className="primary" disabled={done.length !== 1}
                        onClick={() => done.length === 1 && onMove(done[0])}>
                    {title}
                </button>
                <button type="button" className="link"
                        disabled={Object.values(positions).every((own) => own.length === 0)}
                        onClick={() => setPositions({})}>
                    Clear
                </button>
            </div>
        </div>
    );
}

function impliedPositions(part: Part, found: { value: unknown } | null): number[] {
    if (!found) {
        return [];
    }
    const wanted = part.many ? (found.value as unknown[]) : [found.value];
    const taken: number[] = [];
    for (const element of wanted) {
        const at = part.values.findIndex((value, position) => !taken.includes(position) && canonical(value) === canonical(element));
        if (at >= 0) {
            taken.push(at);
        }
    }
    return taken;
}

// the cards sorted as a hand lies, none first
function cardOrder(values: unknown[]): number[] {
    const at = values.map((_, position) => position);
    const cards = at.filter((position) => values[position] !== null);
    const sorted = sortedPositions(cards.map((position) => values[position] as SkillCard)).map((index) => cards[index]);
    return [...at.filter((position) => values[position] === null), ...sorted];
}

function isSkillCard(type: TypeRef): boolean {
    return type.t === 'ref' && type.name === 'SkillCard';
}

function PickWidget({move, onSend}: { move: Move; onSend(event: Answer | null): void }) {
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
                    const detail = moveDetail({
                        ...move,
                        pick: null
                    }, shownComponents(move).filter((name) => name !== pick.component), table);
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
                    const {arts: [art], label} = valueTile(element, pickType(pick), table, pick.component ?? '');
                    return (
                        <button
                            key={position}
                            type="button"
                            className={['tile', 'card-pick', at >= 0 ? 'picked' : ''].join(' ').trim()}
                            aria-pressed={at >= 0}
                            aria-label={label}
                            onClick={() => toggle(position)}
                        >
                            {art && <ArtImg art={art}/>}
                            <span className="pick-label">{label}</span>
                            {ordered && at >= 0 && <b className="order">{at + 1}</b>}
                        </button>
                    );
                })}
            </div>
            <div className="actions">
                <button type="button" className="primary" disabled={!ok}
                        onClick={() => onSend(fillPick(move, picked.map((position) => elements[position])))}>
                    Send
                </button>
                <button type="button" className="link" disabled={picked.length === 0} onClick={() => setPicked([])}>
                    Clear
                </button>
            </div>
        </div>
    );
}

function pickType(pick: Pick): TypeRef {
    switch (pick.kind) {
        case 'SkillCardPick':
            return {t: 'ref', name: 'SkillCard', nullable: false};
        case 'QuorumCardPick':
            return {t: 'enum', name: 'QuorumCard', nullable: false};
        case 'ShipPick':
            return {t: 'int', role: 'ship', nullable: false};
    }
}

function isCardLike(type: TypeRef): boolean {
    return isSkillCard(type) || (type.t === 'enum' && ((CARD_ENUMS as readonly string[]).includes(type.name ?? '') || /Damage$/.test(type.name ?? '')));
}

function valueTile(value: unknown, type: TypeRef, table: ReturnType<typeof useTable>, name: string): {
    arts: ArtRef[];
    label: string
} {
    if (value === null || value === undefined) {
        return {arts: [], label: 'none'};
    }
    if (isSkillCard(type)) {
        const art = skillCardArt(value as SkillCard);
        return {arts: art ? [art] : [], label: skillCardLabel(value as SkillCard)};
    }
    const arts = partValueArt(value, name, type, table);
    if (type.t === 'enum' && typeof value === 'string') {
        return {arts, label: constantLabel(type.name ?? '', value)};
    }
    return {arts, label: valueLabel(value, type, table)};
}
