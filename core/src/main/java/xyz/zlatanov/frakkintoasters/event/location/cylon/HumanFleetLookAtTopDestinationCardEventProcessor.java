package xyz.zlatanov.frakkintoasters.event.location.cylon;

import java.util.List;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceDestinationCardAtBottomEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceDestinationCardOnTopEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;

public class HumanFleetLookAtTopDestinationCardEventProcessor extends LocationActionProcessor<HumanFleetLookAtTopDestinationCardEvent> {
    @Override
    public Followup process() {
        val playerNumber = event.playerNumber();
        val card = ops.player(playerNumber).drawDestinationCard();
        ops.player(playerNumber).see(DeckId.DESTINATION, List.of(card));
        return all(
                one(
                        new PlaceDestinationCardOnTopEvent(playerNumber, card),
                        new PlaceDestinationCardAtBottomEvent(playerNumber, card)),
                single(
                        new PlayerDecisionEvent<>(playerNumber, DrawSkillCardsEvent.class, DRAW_EXACTLY_2))
        );
    }
}
