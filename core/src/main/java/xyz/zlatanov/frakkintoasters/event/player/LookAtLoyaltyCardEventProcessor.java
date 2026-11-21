package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.loyalty.FinalFiveCards;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.DETECTOR_SABOTAGE;

public class LookAtLoyaltyCardEventProcessor extends EventProcessor<LookAtLoyaltyCardEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.targetPlayerNumber());
    }

    @Override
    public Followup process() {
        if (game.crisisCardsInPlay().contains(DETECTOR_SABOTAGE)) {
            return Followup.NONE; // "Players may not look at other players' Loyalty Cards."
        }
        val seen = ops.player(player.number()).lookAtLoyaltyCard(event.targetPlayerNumber());
        return seen.map(card -> FinalFiveCards.seen(player.number(), event.targetPlayerNumber(), List.of(card)))
                .orElse(Followup.NONE);
    }
}
