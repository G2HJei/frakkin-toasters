package xyz.zlatanov.frakkintoasters.event.jump;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;

import java.util.ArrayList;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class GasCloudEventProcessor extends EventProcessor<GasCloudEvent> {

    private static final int CARDS = 3;

    @Override
    protected boolean isValid() {
        return game.playerNumber(game.admiral()) == event.playerNumber();
    }

    @Override
    public Followup process() {
        // The cards are in no pile until the Admiral places each one. Fewer are drawn when the deck has fewer left.
        val cards = new ArrayList<CrisisCard>();
        for (int i = 0; i < CARDS && game.decks().crisisCardsLeft() > 0; i++) {
            cards.add(ops.player(event.playerNumber()).drawCrisisCard());
        }
        return single(new GasCloudPlaceEvent(event.playerNumber(), cards));
    }
}
