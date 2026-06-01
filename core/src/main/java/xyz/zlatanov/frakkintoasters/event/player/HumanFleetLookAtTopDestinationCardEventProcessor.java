package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;

public class HumanFleetLookAtTopDestinationCardEventProcessor extends EventProcessor<HumanFleetLookAtTopDestinationCardEvent> {
    @Override
    public Followup process() {
        val card = game.decks().destination().draw();
        val playerNumber = event.playerNumber();
        return all(
                one(new PlaceDestinationCardOnTopEvent(playerNumber, card),
                        new PlaceDestinationCardOnBottomEvent(playerNumber, card)),
                single(new PlayerDecisionEvent<>(playerNumber, DrawSkillCardsEvent.class, DRAW_EXACTLY_2))
        );
    }
}
