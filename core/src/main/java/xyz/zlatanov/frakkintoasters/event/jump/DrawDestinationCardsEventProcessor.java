package xyz.zlatanov.frakkintoasters.event.jump;

import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;

public class DrawDestinationCardsEventProcessor extends EventProcessor<DrawDestinationCardsEvent> {

    @Override
    public Followup process() {
        val admiral = game.playerNumber(game.admiral());
        val cards = ops.galactica().drawDestinationCards(2);
        // The cards are in no pile until the Admiral chooses, and only he sees them: the other one stays secret.
        ops.player(admiral).see(DeckId.DESTINATION, cards);
        return one(new ChooseDestinationEvent(admiral, cards.get(0), cards.get(1)),
                new ChooseDestinationEvent(admiral, cards.get(1), cards.get(0)));
    }
}
