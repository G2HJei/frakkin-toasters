package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.character.CharacterPenalties;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;

public class RevealCylonOnExecutionEventProcessor extends EventProcessor<RevealCylonOnExecutionEvent> {

    @Override
    protected boolean isValid() {
        return event.card() != null && event.card().isCylon() && player.loyaltyCards().cards().contains(event.card());
    }

    @Override
    public Followup process() {
        ops.player(player.number()).revealLoyaltyCard(event.card());
        // Rule 7.1: the normal procedure for a revealed Cylon, without a Super Crisis Card.
        return allOf(CharacterPenalties.cylonCardRevealed(game), // "Lies and Secrets"
                single(new FinishCylonRevealEvent(player.number(), false)));
    }
}
