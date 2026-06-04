package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import java.util.List;

public class EngineRoomEventProcessor extends EventProcessor<EngineRoomEvent> {

    @Override
    public boolean isValid() {
        val discardCard1 = event.discardCard1();
        val discardCard2 = event.discardCard2();
        val hand = player().skillCards().cards();
        return hand.contains(discardCard1) && hand.contains(discardCard2)
                && !discardCard1.equals(discardCard2);
    }

    @Override
    public Followup process() {
        val cards = List.of(event.discardCard1(), event.discardCard2());
        player().skillCards().remove(cards);
        game.decks().discard(cards);
        galacticaBoard.engineRoomActivated(true);
        return Followup.NONE;
    }
}
