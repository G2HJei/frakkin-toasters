import type { ReactNode } from 'react';
import { cardArt, characterArt, dieArt, shipArt, skillBackArt, skillCardArt, skillCheckArt, skillTypeArt } from '../../art/images';
import { ArtImg, CardBack, CardTile } from '../../art/Art';
import { constantLabel, skillCardLabel } from '../../labels/describe';
import type { ConnectionState } from '../../live/GameSocket';
import type { SkillCard, SkillHand } from '../../wire/types';
import { useRevealHandlers, type RevealSection } from '../reveal';
import { useTable } from '../tableContext';
import { StepIcon, type TurnStep } from './StepIcon';

// the steps of a player's turn, in order
const STEPS: TurnStep[] = ['RECEIVE_SKILLS', 'MOVEMENT', 'ACTION', 'CRISIS', 'ACTIVATE_CYLON_SHIPS', 'PREPARE_FOR_JUMP', 'DISCARD'];

/**
 * The game, named after the logo on the panel's tab, with the connection beside it while it isn't open, and the turn
 * once one has started: the turn's number with the current character and the acting one when it's another, the steps
 * as icons (the step the turn is at named beside its icon), the turn's Crisis Cards, the skill check, the die, an open
 * interrupt window, and what left the game.
 */
export function TurnPanel({ name, connection }: { name: string; connection: ConnectionState }) {
  const table = useTable();
  const game = table.game;
  if (!game) {
    return null;
  }
  const turn = game.turn;
  const die = dieArt(turn?.dieRoll?.value);
  return (
    <section className="panel cf turn" aria-label="turn">
      <div className="tab">
        <img src="/logo.png" alt="" className="logo" />
        <span title={name}>{name || 'Game'}</span>
        {connection !== 'open' && <small className={`connection ${connection}`}>{connection}</small>}
      </div>
      {turn && (
        <>
          <h2 className="turn-line">
            <span className="turn-no">Turn {turn.number}</span> · <span title="current player">{table.playerName(turn.currentPlayer)}</span>
            {turn.actingPlayer !== turn.currentPlayer && (
              <span className="acting" title="acting player">
                {' · '}
                <small>acting</small> {table.playerName(turn.actingPlayer)}
              </span>
            )}
          </h2>
          <Steps step={turn.step} />
        </>
      )}
      {turn && (turn.endedEarly || turn.returnsToCrisisStep) && (
        <p className="note">
          {turn.endedEarly ? 'The turn ended early. ' : ''}
          {turn.returnsToCrisisStep ? 'The turn returns to the Crisis step.' : ''}
        </p>
      )}
      {turn && (turn.crisisCards ?? []).length > 0 && (
        <Block label="Crisis">
          <span className="cards">
            {(turn.crisisCards ?? []).map((card, index) => (
              <CardTile key={index} art={cardArt('CrisisCard', card)} label={constantLabel('CrisisCard', card)} size="md" className="crisis" />
            ))}
          </span>
          <span className="block-text">
            <b>{(turn.crisisCards ?? []).map((card) => constantLabel('CrisisCard', card)).join(', ')}</b>
          </span>
        </Block>
      )}
      <SkillCheck />
      {(game.crisisCardsInPlay ?? []).length > 0 && (
        <Block label="In play">
          <span className="cards">
            {(game.crisisCardsInPlay ?? []).map((type, index) => (
              <CardTile key={index} art={skillCheckArt(type)} label={constantLabel('SkillCheckType', type)} size="xs" className="crisis" />
            ))}
          </span>
        </Block>
      )}
      {turn?.dieRoll && (
        <Block label="Die roll">
          <span className="die-roll">
            {turn.dieRoll.value === null ? (
              <span className="muted">not rolled yet</span>
            ) : (
              <>
                {die ? <ArtImg art={die} className="die-face" /> : <span className="die">{turn.dieRoll.value}</span>}
                {turn.dieRoll.modifier !== 0 && (
                  <span className="modifier">
                    {turn.dieRoll.modifier > 0 ? '+' : '−'}
                    {Math.abs(turn.dieRoll.modifier)}
                  </span>
                )}
                <span className="arrow">→</span>
                <span className="die result" title="the result">
                  {turn.dieRoll.result}
                </span>
              </>
            )}
            {(turn.dieRoll.cards ?? []).map((type, index) => (
              <CardTile key={index} art={skillTypeArt(type)} label={constantLabel('SkillCardType', type)} size="xs" />
            ))}
          </span>
        </Block>
      )}
      {turn?.interruptWindow && <InterruptWindow />}
      {turn && (turn.resolvedInterruptCards ?? []).length > 0 && (
        <Block label="Resolved this turn">
          <span className="cards">
            {(turn.resolvedInterruptCards ?? []).map((type, index) => (
              <CardTile key={index} art={skillTypeArt(type)} label={constantLabel('SkillCardType', type)} size="xs" />
            ))}
          </span>
        </Block>
      )}
      <OutOfTheGame />
    </section>
  );
}

// The steps as icons, the step the turn is at named beside its icon. Hovering lists every step with its name.
function Steps({ step }: { step: string | null }) {
  const at = STEPS.findIndex((stepName) => stepName === step);
  const sections: RevealSection[] = [
    { title: '', cards: STEPS.map((stepName) => ({ art: null, label: constantLabel('GameStep', stepName), icon: <StepIcon step={stepName} /> })) },
  ];
  const handlers = useRevealHandlers('turn-steps', 'Steps of a turn', sections);
  return (
    <ol className="steps" {...handlers}>
      {STEPS.map((stepName, index) => (
        <li key={stepName} className={index < at ? 'done' : index === at ? 'now' : ''} aria-current={index === at ? 'step' : undefined}>
          <StepIcon step={stepName} />
          {/* only the step the turn is at shows its name, the others keep it for screen readers */}
          <span className="step-label">{constantLabel('GameStep', stepName)}</span>
        </li>
      ))}
    </ol>
  );
}

function Block({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="block">
      <small>{label}</small>
      <div className="block-body">{children}</div>
    </div>
  );
}

// The cards played into an interrupt window lie face down until it closes: the others count them.
function InterruptWindow() {
  const table = useTable();
  const turn = table.game?.turn;
  const cards = turn?.windowCards ?? [];
  const sections: RevealSection[] = [
    { title: 'Played', cards: cards.map((card) => ({ art: skillCardArt(card.card), label: `${table.playerName(card.playerNumber)}: ${skillCardLabel(card.card)}` })), hidden: true },
  ];
  const handlers = useRevealHandlers('interrupt-window', 'Interrupt window', sections);
  return (
    <Block label={`Interrupt: ${constantLabel('InterruptWindow', turn?.interruptWindow?.kind)}`}>
      <span className="cards" {...handlers}>
        {cards.length === 0 ? <span className="muted">no cards yet</span> : table.sandbox ? <CardBack count={cards.length} size="xs" /> : cards.map((card, index) => (
          <CardTile key={index} art={skillCardArt(card.card)} label={skillCardLabel(card.card)} caption={table.playerName(card.playerNumber)} size="xs" />
        ))}
      </span>
    </Block>
  );
}

function OutOfTheGame() {
  const game = useTable().game;
  const characters = game?.removedCharacters ?? [];
  const ships = game?.destroyedShips ?? [];
  if (characters.length === 0 && ships.length === 0) {
    return null;
  }
  return (
    <Block label="Out of the game">
      <span className="cards">
        {characters.map((character) => {
          const art = characterArt(character);
          return art ? <ArtImg key={character} art={art} className="removed" /> : <span key={character}>{constantLabel('Character', character)}</span>;
        })}
        {ships.map((ship) => {
          const art = shipArt(ship.type);
          return (
            <span key={ship.id} className="ship-chip" title={`${constantLabel('ShipType', ship.type)} #${ship.id}`}>
              {art && <ArtImg art={art} />}#{ship.id}
            </span>
          );
        })}
      </span>
    </Block>
  );
}

/**
 * The skill check: its card, the difficulty, who played before it, the cards an "Investigative Committee" shows face
 * up, the face-down pile (counted; a sandbox shows it on hover), the piles once revealed, the strength and the result.
 */
function SkillCheck() {
  const table = useTable();
  const check = table.game?.skillCheck;
  if (!check) {
    return null;
  }
  const name = constantLabel('SkillCheckType', check.type);
  // a Crisis Card's check is about the card the Crisis block shows just above, a location's check shows its location
  const fromCrisis = (table.game?.turn?.crisisCards ?? []).some((card) => card === check.type);
  const subject = fromCrisis ? null : skillCheckArt(check.type);
  return (
    <div className="block skill-check" role="group" aria-label="skill check">
      <small>Skill check</small>
      <div className="block-body">
        {subject && <CardTile art={subject} label={name} size="md" />}
        <span className="block-text">
          {!fromCrisis && <b>{name}</b>}
          {check.targetPlayer !== null && <span>about {table.playerName(check.targetPlayer)}</span>}
          {check.emergencyDeclared && <span>emergency declared</span>}
          {check.politicsCountPositive && <span>Politics counts positive</span>}
          {check.zeroStrengthColor && <span>{constantLabel('SkillCardColor', check.zeroStrengthColor)} counts 0</span>}
          <span className="check-figure">
            Difficulty <strong>{check.difficulty}</strong>
          </span>
          {check.totalStrength !== null && (
            <span className={`check-figure check-result ${(check.result ?? '').toLowerCase()}`}>
              Strength <strong>{check.totalStrength}</strong>
              {check.result ? ` · ${constantLabel('SkillCheckResult', check.result)}` : ''}
              {check.passResultReplaced ? ' · pass replaced' : ''}
              {check.failResultPrevented ? ' · fail prevented' : ''}
            </span>
          )}
        </span>
      </div>
      {(check.playedBefore ?? []).map((played) => (
        <Row key={played.player} label={`Before the check: ${table.playerName(played.player)}`}>
          <Cards id={`before-${played.player}`} title={`Played before the check by ${table.playerName(played.player)}`} hand={played.cards} />
        </Row>
      ))}
      {(check.shownCards ?? []).map((shown) => (
        <Row key={`shown-${shown.player}`} label={`Face up: ${table.playerName(shown.player)}`}>
          <CardList cards={shown.cards ?? []} />
        </Row>
      ))}
      <Row label="Face down">
        <Cards id="check-face-down" title="The skill check's face-down cards" hand={check.cards} />
      </Row>
      {(check.matchingPile ?? []).length > 0 && (
        <Row label="Matching">
          <CardList cards={check.matchingPile ?? []} />
        </Row>
      )}
      {(check.nonMatchingPile ?? []).length > 0 && (
        <Row label="Not matching">
          <CardList cards={check.nonMatchingPile ?? []} />
        </Row>
      )}
      {(check.resolvedAbilities ?? []).length > 0 && (
        <Row label="Abilities resolved">
          {(check.resolvedAbilities ?? []).map((type, index) => (
            <CardTile key={index} art={skillTypeArt(type)} label={constantLabel('SkillCardType', type)} size="xs" />
          ))}
        </Row>
      )}
    </div>
  );
}

function Row({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="row">
      <span className="row-name">{label}</span>
      <span className="hand">{children}</span>
    </div>
  );
}

// Cards face up for who may see them in a live game, otherwise their backs, which a sandbox opens on hover.
function Cards({ id, title, hand }: { id: string; title: string; hand: SkillHand | null }) {
  const table = useTable();
  const sections: RevealSection[] = [{ title: 'Cards', cards: (hand?.cards ?? []).map((card) => ({ art: skillCardArt(card), label: skillCardLabel(card) })), hidden: true }];
  const handlers = useRevealHandlers(id, title, sections);
  if (!hand || hand.count === 0) {
    return <span className="muted">—</span>;
  }
  if (hand.cards && !table.sandbox) {
    return <CardList cards={hand.cards} />;
  }
  const colors = Object.entries(hand.colors ?? {});
  const back = skillBackArt();
  return (
    <span className="hidden-cards" {...handlers}>
      {colors.length > 0 ? (
        colors.map(([color, count]) => <CardBack key={color} color={color} count={count ?? 0} size="xs" />)
      ) : (
        <span className="tile back card-back size-xs" title={`${hand.count} face down`}>
          {back && <ArtImg art={back} />}
          <span className="back-count">×{hand.count}</span>
        </span>
      )}
    </span>
  );
}

function CardList({ cards }: { cards: SkillCard[] }) {
  return (
    <>
      {cards.map((card, index) => (
        <CardTile key={index} art={skillCardArt(card)} label={skillCardLabel(card)} size="xs" />
      ))}
    </>
  );
}
