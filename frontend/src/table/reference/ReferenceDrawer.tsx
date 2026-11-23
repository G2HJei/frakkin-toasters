import {type ReactNode, useEffect, useMemo, useRef} from 'react';
import {create} from 'zustand';
import {ArtImg} from '../../art/Art';
import {
    artPaths,
    type ArtRef,
    CARD_ENUMS,
    cardArt,
    characterSheetArt,
    damageArt,
    locationArt,
    skillCardArt
} from '../../art/images';
import {constantLabel} from '../../labels/describe';
import {rule, type Rule, ruleHref, RULES, ruleText} from '../../labels/rules';
import {enumValues, schema} from '../../wire/schema';
import {StepIcon, type TurnStep} from '../turn/StepIcon';

type Tab = 'cards' | 'characters' | 'rules' | 'sheets';

interface ReferenceState {
    open: boolean;
    tab: Tab;
    query: string;
    // a character whose sheet to show first
    focus: string | null;

    show(tab?: Tab, focus?: string | null): void;

    setTab(tab: Tab): void;

    setQuery(query: string): void;

    close(): void;
}

export const useReference = create<ReferenceState>((set) => ({
    open: false,
    tab: 'sheets',
    query: '',
    focus: null,
    // the turn panel's link opens the cheat sheets
    show: (tab = 'sheets', focus = null) => set({open: true, tab, focus, query: ''}),
    setTab: (tab) => set({tab, focus: null}),
    setQuery: (query) => set({query}),
    close: () => set({open: false, focus: null}),
}));

/** A link to the page of the rulebook where an article is. */
export function RuleLink({article, found = rule(article)}: { article: string; found?: Rule | null }) {
    if (!found) {
        return null;
    }
    return (
        <a className="rule-link" href={ruleHref(found)} target="_blank" rel="noreferrer" title={found.name}>
            {ruleText(found)}
        </a>
    );
}

interface Deck {
    name: string;
    cards: ArtRef[];
}

// Every card of the game by deck, from the images: the card enums, the Skill Cards by type and value, the locations
// and the damage tokens.
function decks(): Deck[] {
    const of = (refs: (ArtRef | null)[]) => refs.filter((ref): ref is ArtRef => ref !== null);
    const cardDecks = CARD_ENUMS.map((enumName) => ({
        name: constantLabelOfEnum(enumName),
        cards: of(enumValues(enumName).map((card) => cardArt(enumName, card)))
    }));
    const skills = of(
        artPaths()
            .filter((path) => /^SkillCardType\/[A-Z_]+_\d+$/.test(path))
            .sort()
            .map((path) => {
                const [, type, value] = /^SkillCardType\/([A-Z_]+)_(\d+)$/.exec(path)!;
                return skillCardArt({type, value: Number(value)});
            }),
    );
    const damage = of((['GalacticaDamage', 'PegasusDamage', 'BasestarDamage'] as const).flatMap((enumName) => enumValues(enumName).map((token) => damageArt(enumName, token))));
    return [
        ...cardDecks,
        {name: 'Skill Cards', cards: skills},
        {name: 'Locations', cards: of(enumValues('Location').map(locationArt))},
        {name: 'Damage', cards: damage},
    ].filter((deck) => deck.cards.length > 0);
}

function constantLabelOfEnum(enumName: string): string {
    return {
        CrisisCard: 'Crisis Cards',
        SuperCrisisCard: 'Super Crisis Cards',
        DestinationCard: 'Destinations',
        LoyaltyCard: 'Loyalty Cards',
        MotiveCard: 'Motive Cards',
        MutinyCard: 'Mutiny Cards',
        QuorumCard: 'Quorum Cards',
        ObjectiveCard: 'Objectives'
    }[enumName] ?? enumName;
}

/** The cards whose names have every word of the query. */
export function search(cards: ArtRef[], query: string): ArtRef[] {
    const words = query.toLowerCase().split(/\s+/).filter((word) => word.length > 0);
    return words.length === 0 ? cards : cards.filter((card) => words.every((word) => card.label.toLowerCase().includes(word)));
}

/**
 * The reference beside the table: every card by deck with a search, the character sheets, the rulebook's sections with
 * their pages, and cheat sheets. It names things and points to the article; it doesn't repeat the rulebook.
 */
export function ReferenceDrawer() {
    const {open, tab, query, focus} = useReference();
    const all = useMemo(() => (open ? decks() : []), [open]);
    if (!open) {
        return null;
    }
    return (
        <aside className="reference" role="dialog" aria-label="reference">
            <header className="reference-head">
                <nav className="reference-tabs" role="tablist">
                    {(
                        [
                            ['cards', 'Cards'],
                            ['characters', 'Characters'],
                            ['rules', 'Rules'],
                            ['sheets', 'Cheat sheets'],
                        ] as [Tab, string][]
                    ).map(([id, label]) => (
                        <button key={id} type="button" role="tab" aria-selected={tab === id}
                                className={tab === id ? 'on' : ''} onClick={() => useReference.getState().setTab(id)}>
                            {label}
                        </button>
                    ))}
                </nav>
                <button type="button" className="link close" aria-label="Close the reference"
                        onClick={() => useReference.getState().close()}>
                    ✕
                </button>
            </header>
            <div className="reference-body">
                {tab === 'cards' && <Cards decks={all} query={query}/>}
                {tab === 'characters' && <Characters focus={focus} query={query}/>}
                {tab === 'rules' && <Rules/>}
                {tab === 'sheets' && <CheatSheets/>}
            </div>
        </aside>
    );
}

function SearchBox({placeholder}: { placeholder: string }) {
    const query = useReference((state) => state.query);
    return <input type="search" className="reference-search" aria-label="search" placeholder={placeholder} value={query}
                  onChange={(event) => useReference.getState().setQuery(event.target.value)}/>;
}

function Cards({decks: all, query}: { decks: Deck[]; query: string }) {
    return (
        <>
            <SearchBox placeholder="Find a card by its name"/>
            {all.map((deck) => {
                const found = search(deck.cards, query);
                return found.length === 0 ? null : (
                    <section key={deck.name} className="reference-deck">
                        <h3>
                            {deck.name} <small>{found.length}</small>
                        </h3>
                        <ol className="reference-grid">
                            {found.map((card) => (
                                <li key={card.key}>
                                    <ArtImg art={card}/>
                                    <span>{card.label}</span>
                                </li>
                            ))}
                        </ol>
                    </section>
                );
            })}
        </>
    );
}

function Characters({focus, query}: { focus: string | null; query: string }) {
    const sheets = enumValues('Character')
        .map((character) => ({character, art: characterSheetArt(character)}))
        .filter((entry): entry is { character: string; art: ArtRef } => entry.art !== null);
    const shown = sheets.filter((entry) => search([entry.art], query).length > 0 || constantLabel('Character', entry.character).toLowerCase().includes(query.toLowerCase()));
    const focused = useRef<HTMLLIElement>(null);
    useEffect(() => focused.current?.scrollIntoView?.({block: 'start'}), [focus]);
    return (
        <>
            <SearchBox placeholder="Find a character"/>
            <p className="muted">
                The sheets in detail: <RuleLink article="A2.1"/>. Abilities and miracle tokens: <RuleLink
                article="4.2"/>, <RuleLink article="4.3"/>.
            </p>
            <ol className="reference-grid sheets">
                {shown.map(({character, art}) => (
                    <li key={character} ref={character === focus ? focused : undefined}
                        className={character === focus ? 'focus' : undefined}>
                        <ArtImg art={art}/>
                        <span>{constantLabel('Character', character)}</span>
                    </li>
                ))}
            </ol>
        </>
    );
}

// The sections by what they are about.
const RULE_GROUPS: [string, string[]][] = [
    ['Winning and secrecy', ['1.3', '2.1', '2.2', '2.3', '3.1', '3.2', '16.1', '16.2', '16.3']],
    ['Loyalty and the Cylons', ['5.1', '5.2', '5.3', '5.4', '5.5', '6.1', '6.2', '7.1', 'A3.8']],
    ['Characters and titles', ['4.1', '4.2', '4.3', '4.4', 'A2.1', 'A3.7']],
    ['The turn', ['8.1', '8.2', '8.3', '8.4', '8.5', '8.6', '8.7', '8.8']],
    ['Locations', ['9.1', '9.2', '9.3', '9.4', '9.5', '9.6', '9.7']],
    ['Skill checks', ['10.1', '10.2', '10.3', '10.4']],
    ['Ships and combat', ['11.1', '11.2', '11.3', '11.4', '11.5', '11.6', '11.7', '13.5', '13.6']],
    ['The jump', ['12.1', '12.2', '12.3', '12.4', 'A5.3']],
    ['Timing and limits', ['13.1', '13.2', '13.3', '13.4']],
    ['Components and setup', ['A2.2', 'A2.3', 'A2.5', 'A2.7', 'A3.1', 'A3.9']],
];

function Rules() {
    return (
        <>
            {RULE_GROUPS.map(([title, articles]) => (
                <section key={title} className="reference-rules">
                    <h3>{title}</h3>
                    <ul>
                        {articles
                            .map((article) => RULES.find((found) => found.article === article))
                            .filter((found): found is Rule => found !== undefined)
                            .map((found) => (
                                <li key={found.article}>
                                    <span>{found.name}</span> <RuleLink article={found.article} found={found}/>
                                </li>
                            ))}
                    </ul>
                </section>
            ))}
        </>
    );
}

function Sheet({title, article, children}: { title: string; article: string; children: ReactNode }) {
    return (
        <section className="cheat-sheet">
            <h3>
                {title} <RuleLink article={article}/>
            </h3>
            {children}
        </section>
    );
}

// the steps of a turn, each in a line of our own and its article
const TURN: [TurnStep, string, string][] = [
    ['RECEIVE_SKILLS', 'Draw the Skill Cards your character sheet lists.', '8.2'],
    ['MOVEMENT', 'Move to another location, or use a "Movement:" card or ability. Moving to another ship costs a Skill Card.', '8.3'],
    ['ACTION', 'Take one action: your location, an "Action:" card, a title, a Quorum or Mutiny Card, or your sheet.', '8.4'],
    ['CRISIS', 'Draw a Crisis Card and resolve it: its skill check or its choice.', '8.5'],
    ['ACTIVATE_CYLON_SHIPS', "Activate the Cylon ships the card's icon shows.", '8.6'],
    ['PREPARE_FOR_JUMP', 'Advance the jump track if the card has the jump icon.', '8.7'],
    ['DISCARD', 'Everyone over the hand limit discards down to it.', '8.8'],
];

// what a Crisis Card's icon activates, in our words
const ICONS: [string, string][] = [
    ['ACTIVATE_RAIDERS', 'The raiders activate: each attacks or moves.'],
    ['LAUNCH_RAIDERS', 'The basestars launch raiders.'],
    ['ACTIVATE_HEAVY_RAIDERS', 'The heavy raiders and the centurions activate.'],
    ['ACTIVATE_BASESTARS', 'The basestars attack Galactica.'],
];

function shipName(name: string): string {
    return {GALACTICA: 'Galactica', ARMORY: 'Armory', CENTURION: 'Centurion'}[name] ?? constantLabel('ShipType', name);
}

function CheatSheets() {
    const attacks = (schema as unknown as {
        attackTable?: { attacker: string; target: string; from: number; to: number; effect: string }[]
    }).attackTable ?? [];
    const bonus = (schema as unknown as { structuralDamageBonus?: number }).structuralDamageBonus ?? 0;
    return (
        <>
            <Sheet title="Turn" article="8.1">
                <ol className="turn-sheet">
                    {TURN.map(([step, text, article]) => (
                        <li key={step}>
                            <StepIcon step={step}/>
                            <b>{constantLabel('GameStep', step)}</b> {text} <RuleLink article={article}/>
                        </li>
                    ))}
                </ol>
            </Sheet>
            <Sheet title="Skill check" article="10.3">
                <ol>
                    <li>The cards played before the check act (e.g. "Investigative Committee").</li>
                    <li>Two cards of the Destiny deck go in face down.</li>
                    <li>From the left of the current player, each player puts in any number of cards face down.</li>
                    <li>The cards are shuffled and sorted: the colors the check names add, the others subtract.</li>
                    <li>
                        The cards' abilities resolve, in the current player's order. <RuleLink article="10.4"/>
                    </li>
                    <li>The total against the difficulty decides: pass, partial pass where the card has one, or fail.
                    </li>
                </ol>
            </Sheet>
            <Sheet title="Attacks" article="11.6">
                <table className="attack-table">
                    <thead>
                    <tr>
                        <th>Attacker</th>
                        <th>Target</th>
                        <th>Roll</th>
                        <th>Result</th>
                    </tr>
                    </thead>
                    <tbody>
                    {attacks.map((row, index) => (
                        <tr key={index}>
                            <td>{shipName(row.attacker)}</td>
                            <td>{shipName(row.target)}</td>
                            <td>{row.from === row.to ? row.from : `${row.from}–${row.to}`}</td>
                            <td>{constantLabel('Effect', row.effect)}</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
                <p className="muted">A basestar's "Structural Damage" adds {bonus} to every attack roll on it. Cards and
                    abilities change rolls first. <RuleLink article="13.1"/></p>
            </Sheet>
            <Sheet title="Crisis Card Icons" article="8.6">
                <ul>
                    {ICONS.map(([icon, text]) => (
                        <li key={icon}>
                            <b>{constantLabel('CylonActivation', icon)}</b>: {text} <RuleLink article="11.5"/>
                        </li>
                    ))}
                    <li>
                        <b>Prepare for jump</b>: the jump track advances one space. <RuleLink article="8.7"/>
                    </li>
                </ul>
            </Sheet>
            <Sheet title="Loyalty" article="5.1">
                <ul>
                    <li>
                        A "You Are a Cylon" card can be revealed as an action. <RuleLink article="6.1"/>
                    </li>
                    <li>
                        Personal Goals, the Final Five, the Mutineer: <RuleLink article="5.3"/> <RuleLink
                        article="5.4"/> <RuleLink article="5.2"/>
                    </li>
                    <li>
                        Cylon Leaders and their Motive Cards: <RuleLink article="6.2"/> <RuleLink article="16.3"/>
                    </li>
                </ul>
            </Sheet>
        </>
    );
}
