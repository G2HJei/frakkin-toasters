package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceDestinationCardAtBottomEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceDestinationCardOnTopEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;

public class HumanFleetLookAtTopDestinationCardEventProcessor extends EventProcessor<HumanFleetLookAtTopDestinationCardEvent> {
    @Override
    public Followup process() {
        val card = game.decks().destination().draw();
        val playerNumber = event.playerNumber();
        return all(
                Followup.one(new PlaceDestinationCardOnTopEvent(playerNumber, card),
                        new PlaceDestinationCardAtBottomEvent(playerNumber, card)),
                single(new PlayerDecisionEvent<>(playerNumber, DrawSkillCardsEvent.class, DRAW_EXACTLY_2))
        );
    }
}
