package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class DrawDestinationCardsEventProcessor extends EventProcessor<DrawDestinationCardsEvent> {

    @Override
    public Followup process() {
        val admiral = game.playerNumber(game.admiral());
        val cards = ops.galactica().drawDestinationCards(2);
        // The cards are in no pile until the Admiral chooses. The other one is secret: the describers have to hide it.
        return one(new ChooseDestinationEvent(admiral, cards.get(0), cards.get(1)),
                new ChooseDestinationEvent(admiral, cards.get(1), cards.get(0)));
    }
}
