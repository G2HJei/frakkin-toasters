import { constantLabel, skillCardLabel } from '../../labels/describe';
import { useTable } from '../tableContext';

/** The turn: its number, current and acting player, step, Crisis Cards, the die roll and an open interrupt window. */
export function TurnPanel() {
  const table = useTable();
  const turn = table.game?.turn;
  if (!turn) {
    return null;
  }
  return (
    <section className="panel turn" aria-label="turn">
      <h2>
        Turn {turn.number} · {constantLabel('GameStep', turn.step)}
      </h2>
      <p>
        Current: {table.playerName(turn.currentPlayer)}
        {turn.actingPlayer !== turn.currentPlayer ? ` · acting: ${table.playerName(turn.actingPlayer)}` : ''}
        {turn.endedEarly ? ' · ended early' : ''}
        {turn.returnsToCrisisStep ? ' · returns to the Crisis step' : ''}
      </p>
      {(turn.crisisCards ?? []).length > 0 && (
        <p className="row">
          <span className="row-name">Crisis Cards</span>
          {(turn.crisisCards ?? []).map((card, index) => (
            <span key={index} className="tile crisis">
              {constantLabel('CrisisCard', card)}
            </span>
          ))}
        </p>
      )}
      {(table.game?.crisisCardsInPlay ?? []).length > 0 && (
        <p className="row">
          <span className="row-name">In play</span>
          {(table.game?.crisisCardsInPlay ?? []).map((type, index) => (
            <span key={index} className="tile crisis">
              {constantLabel('SkillCheckType', type)}
            </span>
          ))}
        </p>
      )}
      {turn.dieRoll && (
        <p>
          Die roll: {turn.dieRoll.value ?? 'not rolled yet'}
          {turn.dieRoll.value !== null ? ` → ${turn.dieRoll.result}` : ''}
          {(turn.dieRoll.cards ?? []).length > 0 ? ` (${(turn.dieRoll.cards ?? []).map((card) => constantLabel('SkillCardType', card)).join(', ')}, ${turn.dieRoll.modifier >= 0 ? '+' : ''}${turn.dieRoll.modifier})` : ''}
        </p>
      )}
      {turn.interruptWindow && (
        <p>
          Interrupt window open: {constantLabel('InterruptWindow', turn.interruptWindow.kind)}
          {(turn.windowCards ?? []).map((card, index) => (
            <span key={index} className="tile">
              {table.playerName(card.playerNumber)}: {skillCardLabel(card.card)}
            </span>
          ))}
        </p>
      )}
    </section>
  );
}

/** The skill check: type, target, difficulty, the cards played before it, the piles, the strength and the result. */
export function SkillCheckPanel() {
  const table = useTable();
  const check = table.game?.skillCheck;
  if (!check) {
    return null;
  }
  return (
    <section className="panel skill-check" aria-label="skill check">
      <h2>Skill check: {constantLabel('SkillCheckType', check.type)}</h2>
      <p>
        Difficulty {check.difficulty}
        {check.targetPlayer !== null ? ` · about ${table.playerName(check.targetPlayer)}` : ''}
        {check.emergencyDeclared ? ' · emergency declared' : ''}
      </p>
      {(check.playedBefore ?? []).map((played) => (
        <p key={played.player} className="row">
          <span className="row-name">Before, {table.playerName(played.player)}</span>
          {played.cards?.cards
            ? played.cards.cards.map((card, index) => (
                <span key={index} className="tile">
                  {skillCardLabel(card)}
                </span>
              ))
            : `${played.cards?.count} face down`}
        </p>
      ))}
      <p className="row">
        <span className="row-name">Face down</span> {check.cards?.count ?? 0}
        {check.cards?.cards ? ` (${check.cards.cards.map(skillCardLabel).join(', ')})` : ''}
      </p>
      <p className="row">
        <span className="row-name">Matching</span> {(check.matchingPile ?? []).map(skillCardLabel).join(', ') || '—'}
      </p>
      <p className="row">
        <span className="row-name">Not matching</span> {(check.nonMatchingPile ?? []).map(skillCardLabel).join(', ') || '—'}
      </p>
      {check.totalStrength !== null && (
        <p>
          Strength {check.totalStrength}
          {check.result ? ` · ${constantLabel('SkillCheckResult', check.result)}` : ''}
          {check.passResultReplaced ? ' · pass replaced' : ''}
          {check.failResultPrevented ? ' · fail prevented' : ''}
        </p>
      )}
    </section>
  );
}
