import { cardArt, characterArt, skillCardArt, skillCheckArt, skillTypeArt } from '../../art/images';
import { ArtImg, CardBack, CardTile } from '../../art/Art';
import { constantLabel, skillCardLabel } from '../../labels/describe';
import type { SkillCard, SkillHand } from '../../wire/types';
import { useTable } from '../tableContext';

/** The turn: its number, current and acting player, step, Crisis Cards, the die roll and an open interrupt window. */
export function TurnPanel() {
  const table = useTable();
  const turn = table.game?.turn;
  if (!turn) {
    return null;
  }
  const characterOf = (playerNumber: number) => table.game?.players?.find((player) => player.number === playerNumber)?.character ?? null;
  const current = characterArt(characterOf(turn.currentPlayer));
  const acting = turn.actingPlayer !== turn.currentPlayer ? characterArt(characterOf(turn.actingPlayer)) : null;
  return (
    <section className="panel turn" aria-label="turn">
      <h2>
        Turn {turn.number} · {constantLabel('GameStep', turn.step)}
      </h2>
      <p className="who">
        {current && <ArtImg art={current} className="avatar small" />}
        <span>
          Current: {table.playerName(turn.currentPlayer)}
          {turn.actingPlayer !== turn.currentPlayer ? ` · acting: ${table.playerName(turn.actingPlayer)}` : ''}
          {turn.endedEarly ? ' · ended early' : ''}
          {turn.returnsToCrisisStep ? ' · returns to the Crisis step' : ''}
        </span>
        {acting && <ArtImg art={acting} className="avatar small" />}
      </p>
      {(turn.crisisCards ?? []).length > 0 && (
        <div className="row">
          <span className="row-name">Crisis Cards</span>
          {(turn.crisisCards ?? []).map((card, index) => (
            <CardTile key={index} art={cardArt('CrisisCard', card)} label={constantLabel('CrisisCard', card)} size="lg" className="crisis" />
          ))}
        </div>
      )}
      {(table.game?.crisisCardsInPlay ?? []).length > 0 && (
        <div className="row">
          <span className="row-name">In play</span>
          {(table.game?.crisisCardsInPlay ?? []).map((type, index) => (
            <CardTile key={index} art={skillCheckArt(type)} label={constantLabel('SkillCheckType', type)} className="crisis" />
          ))}
        </div>
      )}
      {turn.dieRoll && (
        <div className="row die-roll">
          <span className="row-name">Die roll</span>
          {turn.dieRoll.value === null ? (
            <span className="muted">not rolled yet</span>
          ) : (
            <>
              <span className="die" title="the die">
                {turn.dieRoll.value}
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
        </div>
      )}
      {turn.interruptWindow && (
        <div className="row interrupt">
          <span className="row-name">Interrupt: {constantLabel('InterruptWindow', turn.interruptWindow.kind)}</span>
          {(turn.windowCards ?? []).map((card, index) => (
            <CardTile key={index} art={skillCardArt(card.card)} label={skillCardLabel(card.card)} caption={table.playerName(card.playerNumber)} />
          ))}
        </div>
      )}
      {(turn.resolvedInterruptCards ?? []).length > 0 && (
        <div className="row">
          <span className="row-name">Resolved this turn</span>
          {(turn.resolvedInterruptCards ?? []).map((type, index) => (
            <CardTile key={index} art={skillTypeArt(type)} label={constantLabel('SkillCardType', type)} size="xs" />
          ))}
        </div>
      )}
    </section>
  );
}

/** The skill check: its card, target, difficulty, the cards played before it, the piles, the strength and the result. */
export function SkillCheckPanel() {
  const table = useTable();
  const check = table.game?.skillCheck;
  if (!check) {
    return null;
  }
  const subject = skillCheckArt(check.type);
  return (
    <section className="panel skill-check" aria-label="skill check">
      <h2>Skill check: {constantLabel('SkillCheckType', check.type)}</h2>
      <div className="check-head">
        {subject && <CardTile art={subject} label={constantLabel('SkillCheckType', check.type)} size="md" />}
        <div className="check-numbers">
          <p>
            Difficulty <strong>{check.difficulty}</strong>
            {check.targetPlayer !== null ? ` · about ${table.playerName(check.targetPlayer)}` : ''}
            {check.emergencyDeclared ? ' · emergency declared' : ''}
          </p>
          {check.totalStrength !== null && (
            <p className={`check-result ${(check.result ?? '').toLowerCase()}`}>
              Strength <strong>{check.totalStrength}</strong>
              {check.result ? ` · ${constantLabel('SkillCheckResult', check.result)}` : ''}
              {check.passResultReplaced ? ' · pass replaced' : ''}
              {check.failResultPrevented ? ' · fail prevented' : ''}
            </p>
          )}
        </div>
      </div>
      {(check.playedBefore ?? []).map((played) => (
        <div key={played.player} className="row">
          <span className="row-name">Before, {table.playerName(played.player)}</span>
          <Cards hand={played.cards} />
        </div>
      ))}
      <div className="row">
        <span className="row-name">Face down</span>
        <Cards hand={check.cards} />
      </div>
      <div className="row">
        <span className="row-name">Matching</span>
        <CardList cards={check.matchingPile ?? []} />
      </div>
      <div className="row">
        <span className="row-name">Not matching</span>
        <CardList cards={check.nonMatchingPile ?? []} />
      </div>
      {(check.resolvedAbilities ?? []).length > 0 && (
        <div className="row">
          <span className="row-name">Abilities resolved</span>
          {(check.resolvedAbilities ?? []).map((type, index) => (
            <CardTile key={index} art={skillTypeArt(type)} label={constantLabel('SkillCardType', type)} size="xs" />
          ))}
        </div>
      )}
    </section>
  );
}

// Cards face up for who may see them, otherwise their backs by color.
function Cards({ hand }: { hand: SkillHand | null }) {
  if (!hand || hand.count === 0) {
    return <span className="muted">—</span>;
  }
  if (hand.cards) {
    return <CardList cards={hand.cards} />;
  }
  const colors = Object.entries(hand.colors ?? {});
  return colors.length > 0 ? (
    <span className="hand">
      {colors.map(([color, count]) => (
        <CardBack key={color} color={color} count={count ?? 0} />
      ))}
    </span>
  ) : (
    <CardBack count={hand.count} />
  );
}

function CardList({ cards }: { cards: SkillCard[] }) {
  if (cards.length === 0) {
    return <span className="muted">—</span>;
  }
  return (
    <span className="hand">
      {cards.map((card, index) => (
        <CardTile key={index} art={skillCardArt(card)} label={skillCardLabel(card)} />
      ))}
    </span>
  );
}
