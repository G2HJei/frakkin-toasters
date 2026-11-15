package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

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
        ops.player(player.number()).lookAtLoyaltyCard(event.targetPlayerNumber());
        return Followup.NONE;
    }
}
