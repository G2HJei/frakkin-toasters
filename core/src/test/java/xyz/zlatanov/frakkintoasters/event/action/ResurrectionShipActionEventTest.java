package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.fake.FakeDeck;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.crisis.SuperCrisisCard.THE_FARM;
import static xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider.genericDeck;

class ResurrectionShipActionEventTest {

    FakeDeck<SuperCrisisCard> superCrisisDeck = new FakeDeck<>(genericDeck(SuperCrisisCard.class));
    Game                      game            = Game.builder()
            .decks(DecksHolder.builder()
                    .superCrisis(superCrisisDeck)
                    .build())
            .build();

    @Test
    void shouldDrawSuperCrisisCard() {
        superCrisisDeck.nextCard(THE_FARM);

        new ResurrectionShipActionEvent(1).execute(game);

        val hand = game.player(1).superCrisisCards().cards();
        assertEquals(List.of(THE_FARM), hand);
    }
}
