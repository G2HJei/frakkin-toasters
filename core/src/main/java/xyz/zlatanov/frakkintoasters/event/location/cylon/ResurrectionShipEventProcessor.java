package xyz.zlatanov.frakkintoasters.event.location.cylon;

import lombok.val;
import xyz.zlatanov.frakkintoasters.state.deck.DeckId;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;

public class ResurrectionShipEventProcessor extends LocationActionProcessor<ResurrectionShipEvent> {

    // A Super Crisis Card left to draw, in the deck or its discard pile, which is shuffled back in (F-b894d2).
    public static boolean canAct(GameView game) {
        val deck = game.decks().deck(DeckId.SUPER_CRISIS);
        return deck.size() + deck.discardSize() > 0;
    }

    @Override
    protected Followup act() {
        ops.player(player.number()).drawSuperCrisisCard();
        return Followup.NONE;
    }
}
