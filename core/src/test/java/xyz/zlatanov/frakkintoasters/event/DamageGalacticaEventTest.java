package xyz.zlatanov.frakkintoasters.event;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.fake.FakeDeck;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.*;
import static xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider.genericDeck;

class DamageGalacticaEventTest {

    FakeDeck<GalacticaDamage> galacticaDamageDeck = new FakeDeck<>(genericDeck(GalacticaDamage.class));
    Game                      game                = Game.builder()
            .decks(DecksHolder.builder()
                    .galacticaDamage(galacticaDamageDeck)
                    .build())
            .build();

    @Test
    void shouldDamageGalactica() {
        galacticaDamageDeck.nextCard(WEAPONS_CONTROL);

        new DamageGalacticaEvent().execute(game);

        assertTrue(game.boards().galactica().damagedLocations().contains(Location.WEAPONS_CONTROL));
        assertEquals(7, game.decks().galacticaDamage().size());
    }

    @Test
    void shouldDamageGalacticaFood() {
        galacticaDamageDeck.nextCard(FOOD);

        new DamageGalacticaEvent().execute(game);

        assertEquals(7, game.boards().galactica().food());
        assertEquals(7, game.decks().galacticaDamage().size());
    }

    @Test
    void shouldDamageGalacticaFuel() {
        galacticaDamageDeck.nextCard(FUEL);

        new DamageGalacticaEvent().execute(game);

        assertEquals(7, game.boards().galactica().fuel());
        assertEquals(7, game.decks().galacticaDamage().size());
    }
}