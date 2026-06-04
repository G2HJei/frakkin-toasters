package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.board.Location.CYLON_FLEET;

public class HubDestroyedEventProcessor extends EventProcessor<HubDestroyedEvent> {

    @Override
    public Followup process() {
        for (val card : List.of(event.discardCard1(), event.discardCard2(), event.discardCard3())) {
            player.skillCards().remove(card);
            game.decks().discard(card);
        }
        player.superCrisisCards().addOnTop(game.decks().superCrisis().draw());
        game.moveTo(CYLON_FLEET, player.character());
        return Followup.NONE;
    }
}
