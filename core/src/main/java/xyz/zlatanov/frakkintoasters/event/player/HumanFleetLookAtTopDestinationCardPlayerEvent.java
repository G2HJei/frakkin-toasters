package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.Game;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;

public record HumanFleetLookAtTopDestinationCardPlayerEvent(int playerNumber) implements PlayerEvent {

    @Override
    public Followup apply(Game game) {
        val card = game.decks().destination().draw();
        return all(
                one(new PlaceDestinationCardOnTopEvent(playerNumber, card),
                        new PlaceDestinationCardOnBottomEvent(playerNumber, card)),
                single(new PlayerDecisionEvent<>(playerNumber, DrawSkillCardsEvent.class, DRAW_EXACTLY_2))
        );
    }
}
