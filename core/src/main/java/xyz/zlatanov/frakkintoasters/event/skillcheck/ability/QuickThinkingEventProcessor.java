package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.ENGINEERING;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.ESTABLISH_NETWORK;

public class QuickThinkingEventProcessor extends EventProcessor<QuickThinkingEvent> {
    @Override
    protected boolean isValid() {
        return event.cardToRemove() == null ||
                cardValue() <= 3;
    }

    @Override
    public Followup process() {
        if (event.cardToRemove() != null) {
            ops.skillCheck().giveCardToPlayer(event.cardToRemove(), game.currentPlayerNumber());
        }
        return Followup.NONE;
    }

    private int cardValue() {
        val faceValue = event.cardToRemove().value();
        val valueDoubled = event.cardToRemove().type().color() == ENGINEERING &&
                game.activeSkillCheck().resolvedAbilities().contains(ESTABLISH_NETWORK);
        val valueMultiplier = valueDoubled ? 2 : 1;
        return faceValue * valueMultiplier;
    }
}
