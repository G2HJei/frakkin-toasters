package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;

public class FootageTransmittedFailEventProcessor extends EventProcessor<FootageTransmittedFailEvent> {

    @Override
    public Followup process() {
        // Each Cylon player, a Cylon Leader who isn't Infiltrating included (rule 6.2.1), while a card is left to draw.
        game.players().stream()
                .filter(p -> !p.isHuman())
                .forEach(p -> {
                    val deck = game.decks().deck(DeckId.SUPER_CRISIS);
                    if (deck.size() + deck.discardSize() > 0) {
                        ops.player(p.number()).drawSuperCrisisCard();
                    }
                });
        return Followup.NONE;
    }
}
