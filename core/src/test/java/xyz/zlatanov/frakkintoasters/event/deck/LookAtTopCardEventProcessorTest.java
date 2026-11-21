package xyz.zlatanov.frakkintoasters.event.deck;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.CardsSeen;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnBottomEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCrisisCardOnTopEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceDestinationCardAtBottomEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceDestinationCardOnTopEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.DEEP_SPACE;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.DETENTE;
import static xyz.zlatanov.frakkintoasters.state.deck.DeckId.*;

class LookAtTopCardEventProcessorTest extends EventTestHarness<LookAtTopCardEvent> {

    // Only the player learns the card.
    @Test
    void theTopCrisisCardGoesBackOnTopOrBottom() {
        crisisDeck.nextCard(DETENTE);

        execute(new LookAtTopCardEvent(1, CRISIS));

        assertFollowup(one(new PlaceCrisisCardOnTopEvent(1, DETENTE), new PlaceCrisisCardOnBottomEvent(1, DETENTE)));
        assertTrue(journal.facts().contains(new CardsSeen(1, CRISIS, List.of(DETENTE))));
    }

    @Test
    void theTopDestinationCardGoesBackOnTopOrBottom() {
        destinationDeck.nextCard(DEEP_SPACE);

        execute(new LookAtTopCardEvent(1, DESTINATION));

        assertFollowup(one(new PlaceDestinationCardOnTopEvent(1, DEEP_SPACE),
                new PlaceDestinationCardAtBottomEvent(1, DEEP_SPACE)));
    }

    @Test
    void onlyTheCrisisOrTheDestinationDeck() {
        assertInvalid(new LookAtTopCardEvent(1, POLITICS));
    }
}
