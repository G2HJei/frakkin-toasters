package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.QUICK_THINKING;

public class QuickThinkingEventProcessor extends EventProcessor<QuickThinkingEvent> {

    private static final int MAX_STRENGTH = 3;

    // A card to take, or none to decline.
    @Override
    protected boolean isValid() {
        val card = event.cardToRemove();
        if (card == null) {
            return true;
        }
        val skillCheck = game.activeSkillCheck();
        return card.type() != QUICK_THINKING
                && skillCheck.dividedCards().contains(card)
                && skillCheck.cardValue(card) <= MAX_STRENGTH;
    }

    @Override
    public Followup process() {
        if (event.cardToRemove() != null) {
            ops.skillCheck().giveCardToPlayer(event.cardToRemove(), game.turn().currentPlayerNumber());
        }
        return Followup.NONE;
    }
}
