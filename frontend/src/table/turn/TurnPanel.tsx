import type { ReactNode } from 'react';
import { cardArt, dieArt, skillCardArt, skillCheckArt, skillTypeArt } from '../../art/images';
import { ArtImg, CardBack, CardTile, SkillCardBack } from '../../art/Art';
import { constantLabel, skillCardLabel, windowLabel } from '../../labels/describe';
import type { ConnectionState } from '../../live/GameSocket';
import type { SkillCard, SkillHand } from '../../wire/types';
import { useRevealHandlers, type RevealSection } from '../reveal';
import { useTable } from '../tableContext';
import { StepIcon, type TurnStep } from './StepIcon';
import { useChanges } from '../changes';
import { useReference } from '../reference/ReferenceDrawer';
import { NotifyButton } from '../notify';
import { ProblemLink } from '../problem/ProblemDrawer';

const STEPS: TurnStep[] = ['RECEIVE_SKILLS', 'MOVEMENT', 'ACTION', 'CRISIS', 'ACTIVATE_CYLON_SHIPS', 'PREPARE_FOR_JUMP', 'DISCARD'];

export function TurnPanel({ name, connection }: { name: string; connection: ConnectionState }) {
  const table = useTable();
  const game = table.game;
  if (!game) {
    return null;
  }
  const turn = game.turn;
  const otherActs = !!turn && turn.actingPlayer !== turn.currentPlayer;
  const turnName = (playerNumber: number) => table.playerName(playerNumber);
  const die = dieArt(turn?.dieRoll?.value);
  const crisisCards = turn?.crisisCards ?? [];
  // a skill check about none of the turn's Crisis Cards lies beside them with its own card: its location, a Super Crisis Card
  const check = game.skillCheck;
  const checkCard =
    check && !crisisCards.some((card) => card === check.type) ? { art: skillCheckArt(check.type), label: constantLabel('SkillCheckType', check.type) } : null;
  const { changes, version } = useChanges();
  return (
    <section className="panel cf turn" aria-label="turn">
      <div className="tab">
        <img src="/logo.svg" alt="" className="logo" />
        <span title={name}>{name || 'Game'}</span>
        {connection !== 'open' && <small className={`connection ${connection}`}>{connection}</small>}
        <span className="tab-links">
          <button type="button" className="link reference-open" aria-keyshortcuts="R" onClick={() => useReference.getState().show()}>
            Reference <kbd aria-hidden="true">R</kbd>
          </button>
          <ProblemLink />
          {!table.sandbox && <NotifyButton />}
        </span>
      </div>
      {turn && (
        <div className="turn-row">
          {/* red names the character acting now: the current one, or the other one beside him, who is then amber. The
              lines say only "T4" and the names, so the titles and the heading's name say what they are, and the titles
              name the character too where his line cuts the name short */}
          <h2
            className="turn-lines"
            aria-label={`Turn ${turn.number}: ${turnName(turn.currentPlayer)}${otherActs ? `, acting: ${turnName(turn.actingPlayer)}` : ''}`}
          >
            <span
              className={`turn-line ${otherActs ? '' : 'acting'}`.trim()}
              title={`Turn ${turn.number}, ${otherActs ? 'current player' : 'current and acting player'}: ${turnName(turn.currentPlayer)}`}
            >
              <span>T{turn.number}</span>
              <strong>{turnName(turn.currentPlayer)}</strong>
            </span>
            {otherActs && (
              <span className="turn-line acting" title={`acting player: ${turnName(turn.actingPlayer)}`}>
                <strong>{turnName(turn.actingPlayer)}</strong>
              </span>
            )}
          </h2>
          <Steps step={turn.step} />
        </div>
      )}
      {turn && (turn.endedEarly || turn.returnsToCrisisStep) && (
        <p className="note">
          {turn.endedEarly ? 'The turn ended early. ' : ''}
          {turn.returnsToCrisisStep ? 'The turn returns to the Crisis step.' : ''}
        </p>
      )}
      {/* the cards at full size, side by side, each card naming itself; the rest beside them */}
      <div className="turn-body">
        {(crisisCards.length > 0 || checkCard) && (
          <div className="turn-cards" role="group" aria-label="cards of the turn">
            {crisisCards.map((card, index) => (
              <CardTile key={index} art={cardArt('CrisisCard', card)} label={constantLabel('CrisisCard', card)} size="full" className="crisis" />
            ))}
            {/* a Super Crisis Card's scan is trimmed like a Crisis Card's, a location's isn't */}
            {checkCard && <CardTile art={checkCard.art} label={checkCard.label} size="full" className={checkCard.art?.shape === 'card' ? 'crisis' : ''} />}
          </div>
        )}
        <div className="turn-details">
          <SkillCheck />
          {(game.crisisCardsInPlay ?? []).length > 0 && (
            <Block label="Crisis Cards in play">
              <span className="cards">
                {(game.crisisCardsInPlay ?? []).map((type, index) => (
                  <CardTile key={index} art={skillCheckArt(type)} label={constantLabel('SkillCheckType', type)} size="xs" />
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
                    {/* a new roll tumbles once, keyed by the game message */}
                    <span key={changes.rolled ? `rolled-${version}` : 'die'} className={changes.rolled ? 'die-tumble' : undefined}>
                      {die ? <ArtImg art={die} className="die-face" /> : <span className="die">{turn.dieRoll.value}</span>}
                    </span>
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
            <Block label="Interrupt cards played this turn">
              <span className="cards">
                {(turn.resolvedInterruptCards ?? []).map((type, index) => (
                  <CardTile key={index} art={skillTypeArt(type)} label={constantLabel('SkillCardType', type)} size="xs" />
                ))}
              </span>
            </Block>
          )}
        </div>
      </div>
    </section>
  );
}

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
    <Block label={`Interrupt cards ${windowLabel(turn?.interruptWindow?.kind)}`}>
      <span className="cards" {...handlers}>
        {cards.length === 0 ? <span className="muted">no cards yet</span> : table.sandbox ? <CardBack count={cards.length} size="xs" /> : cards.map((card, index) => (
          <CardTile key={index} art={skillCardArt(card.card)} label={skillCardLabel(card.card)} caption={table.playerName(card.playerNumber)} size="xs" />
        ))}
      </span>
    </Block>
  );
}

function SkillCheck() {
  const table = useTable();
  const check = table.game?.skillCheck;
  if (!check) {
    return null;
  }
  return (
    <div className="block skill-check" role="group" aria-label="skill check">
      <small>Skill check</small>
      <div className="block-body">
        <span className="block-text">
          {check.targetPlayer !== null && <span>Targets {table.playerName(check.targetPlayer)}</span>}
          {check.emergencyDeclared && <span>Emergency declared</span>}
          {check.politicsCountPositive && <span>Politics counts positive</span>}
          {check.zeroStrengthColor && <span>{constantLabel('SkillCardColor', check.zeroStrengthColor)} counts 0</span>}
          <span className="check-figure">
            Difficulty <strong>{check.difficulty}</strong>
          </span>
          {check.totalStrength !== null && (
            <span className={`check-figure check-result ${(check.result ?? '').toLowerCase()}`}>
              Strength <strong>{check.totalStrength}</strong>
              {check.result ? ` · ${RESULTS[check.result] ?? constantLabel('SkillCheckResult', check.result)}` : ''}
              {check.passResultReplaced ? ' · its pass is replaced' : ''}
              {check.failResultPrevented ? ' · its fail is prevented' : ''}
            </span>
          )}
        </span>
      </div>
      {(check.playedBefore ?? []).map((played) => (
        <Row key={played.player} label={`Played before by ${table.playerName(played.player)}`}>
          <Cards
            id={`before-${played.player}`}
            title={`Played before the check by ${table.playerName(played.player)}`}
            hand={played.cards}
            faceUp={check.playedBeforeRevealed}
          />
        </Row>
      ))}
      {(check.shownCards ?? []).map((shown) => (
        <Row key={`shown-${shown.player}`} label={`Face up from ${table.playerName(shown.player)}`}>
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

const RESULTS: Record<string, string> = { PASS: 'passes', PARTIAL_PASS: 'partially passes', FAIL: 'fails' };

function Row({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="row">
      <span className="row-name">{label}</span>
      <span className="hand">{children}</span>
    </div>
  );
}

// Cards face up for who may see them in a live game, otherwise their backs, which a sandbox opens on hover. Cards
// revealed to everyone (faceUp) lie face up in a sandbox too.
function Cards({ id, title, hand, faceUp = false }: { id: string; title: string; hand: SkillHand | null; faceUp?: boolean }) {
  const table = useTable();
  const sections: RevealSection[] = [{ title: 'Cards', cards: (hand?.cards ?? []).map((card) => ({ art: skillCardArt(card), label: skillCardLabel(card) })), hidden: true }];
  const handlers = useRevealHandlers(id, title, sections);
  if (!hand || hand.count === 0) {
    return <span className="muted">—</span>;
  }
  if (hand.cards && (faceUp || !table.sandbox)) {
    return <CardList cards={hand.cards} />;
  }
  // a face down card doesn't show its color
  return (
    <span className="hidden-cards" {...handlers}>
      <SkillCardBack count={hand.count} size="xs" />
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
