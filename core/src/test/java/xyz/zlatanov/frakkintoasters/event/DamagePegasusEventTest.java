package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.fake.FakeDeck;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage.PEGASUS_CIC;
import static xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider.genericDeck;

class DamagePegasusEventTest {
    FakeDeck<PegasusDamage> pegasusDamageDeck = new FakeDeck<>(genericDeck(PegasusDamage.class));
    Game                    game              = Game.builder()
            .decks(DecksHolder.builder()
                    .pegasusDamage(pegasusDamageDeck)
                    .build())
            .build();

    @Test
    void shouldDamagePegasus() {
        pegasusDamageDeck.nextCard(PEGASUS_CIC);

        new DamagePegasusEvent().execute(game);

        assertTrue(game.boards().pegasus().damagedLocations().contains(Location.PEGASUS_CIC));
        assertEquals(3, game.decks().pegasusDamage().size());
    }
}