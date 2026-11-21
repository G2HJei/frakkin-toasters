package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.loyalty.FinalFiveCards;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.DETECTOR_SABOTAGE;

public class LookAtAllLoyaltyCardsEventProcessor extends EventProcessor<LookAtAllLoyaltyCardsEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.targetPlayerNumber()) && event.targetPlayerNumber() != player.number();
    }

    @Override
    public Followup process() {
        if (game.crisisCardsInPlay().contains(DETECTOR_SABOTAGE)) {
            return Followup.NONE; // "Players may not look at other players' Loyalty Cards."
        }
        val seen = ops.player(player.number()).lookAtAllLoyaltyCards(event.targetPlayerNumber());
        return FinalFiveCards.seen(player.number(), event.targetPlayerNumber(), seen);
    }
}
