package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.deck.LookAtTopCardEvent;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.DESTINATION;

public class HumanFleetLookAtTopDestinationCardEventProcessor extends LocationActionProcessor<HumanFleetLookAtTopDestinationCardEvent> {

    // The look is LookAtTopCardEvent's, which "Armed Resistance" and "Betrayal of Trust" share.
    @Override
    protected Followup act() {
        val playerNumber = event.playerNumber();
        return all(
                new LookAtTopCardEvent(playerNumber, DESTINATION),
                new PlayerDecisionEvent<>(playerNumber, DrawSkillCardsEvent.class, DRAW_EXACTLY_2));
    }
}
