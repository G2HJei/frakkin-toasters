package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.fake.FakeDeck;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.card.DestinationCard;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.card.DestinationCard.LIONS_HEAD_NEBULA;
import static xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider.genericDeck;

class HumanFleetLookAtTopDestinationCardPlayerEventTest {

    FakeDeck<DestinationCard> destinationDeck = new FakeDeck<>(genericDeck(DestinationCard.class));
    Game                      game            = Game.builder()
            .decks(DecksHolder.builder()
                    .destination(destinationDeck)
                    .build())
            .build();

    @Test
    void shouldDrawCardAndFollowUpWithPlacementChoiceAndSkillDraw() {
        destinationDeck.nextCard(LIONS_HEAD_NEBULA);
        val initialSize = destinationDeck.size();

        val followups = new HumanFleetLookAtTopDestinationCardPlayerEvent(1).execute(game);

        assertEquals(initialSize - 1, destinationDeck.size());
        assertEquals(all(
                        one(new PlaceDestinationCardOnTopEvent(1, LIONS_HEAD_NEBULA),
                                new PlaceDestinationCardOnBottomEvent(1, LIONS_HEAD_NEBULA)),
                        single(new PlayerDecisionEvent<>(1, ReceiveSkillCardsEvent.class, DRAW_EXACTLY_2))
                ),
                followups);
    }
}
