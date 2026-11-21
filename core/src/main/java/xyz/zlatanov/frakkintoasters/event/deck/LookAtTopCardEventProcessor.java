package xyz.zlatanov.frakkintoasters.event.deck;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnBottomEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnTopEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceDestinationCardAtBottomEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceDestinationCardOnTopEvent;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.CRISIS;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.DESTINATION;

public class LookAtTopCardEventProcessor extends EventProcessor<LookAtTopCardEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.playerNumber()) && (event.deck() == CRISIS || event.deck() == DESTINATION);
    }

    // Only the player learns the card, so it is a CardsSeen until it goes back.
    @Override
    public Followup process() {
        val playerOps = ops.player(event.playerNumber());
        int n = event.playerNumber();
        if (event.deck() == CRISIS) {
            val card = playerOps.drawCrisisCard();
            playerOps.see(CRISIS, List.of(card));
            return one(new PlaceCrisisCardOnTopEvent(n, card), new PlaceCrisisCardOnBottomEvent(n, card));
        }
        val card = playerOps.drawDestinationCard();
        playerOps.see(DESTINATION, List.of(card));
        return one(new PlaceDestinationCardOnTopEvent(n, card), new PlaceDestinationCardAtBottomEvent(n, card));
    }
}
