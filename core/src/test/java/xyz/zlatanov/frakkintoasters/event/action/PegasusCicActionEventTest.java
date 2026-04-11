package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.fake.FakeDeck;
import xyz.zlatanov.frakkintoasters.fake.FakeDie;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.*;
import static xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage.PEGASUS_CIC;
import static xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider.genericDeck;

class PegasusCicActionEventTest {
    FakeDie                   die                 = new FakeDie();
    FakeDeck<PegasusDamage>   pegasusDamageDeck   = new FakeDeck<>(genericDeck(PegasusDamage.class));
    FakeDeck<GalacticaDamage> galacticaDamageDeck = new FakeDeck<>(genericDeck(GalacticaDamage.class));
    FakeDeck<BasestarDamage>  basestarDamageDeck  = new FakeDeck<>(genericDeck(BasestarDamage.class));
    Game                      game                = Game.builder()
            .die(die)
            .decks(DecksHolder.builder()
                    .galacticaDamage(galacticaDamageDeck)
                    .pegasusDamage(pegasusDamageDeck)
                    .basestarDamage(basestarDamageDeck)
                    .build())
            .build();

    @Test
    void shouldDamagePegasus() {
        die.nextRoll = 3;
        pegasusDamageDeck.nextCard(PEGASUS_CIC);

        new PegasusCicActionEvent().execute(game);

        assertTrue(game.boards().pegasus().damagedLocations().contains(Location.PEGASUS_CIC));
        assertEquals(3, game.decks().pegasusDamage().size());
    }

    @Test
    void shouldDamageGalactica() {
        die.nextRoll = 4;
        galacticaDamageDeck.nextCard(WEAPONS_CONTROL);

        new PegasusCicActionEvent().execute(game);

        assertTrue(game.boards().galactica().damagedLocations().contains(Location.WEAPONS_CONTROL));
        assertEquals(7, game.decks().galacticaDamage().size());
    }

    @Test
    void shouldDamageGalacticaFood() {
        die.nextRoll = 5;
        galacticaDamageDeck.nextCard(FOOD);

        new PegasusCicActionEvent().execute(game);

        assertEquals(7, game.boards().galactica().food());
        assertEquals(7, game.decks().galacticaDamage().size());
    }

    @Test
    void shouldDamageGalacticaFuel() {
        die.nextRoll = 6;
        galacticaDamageDeck.nextCard(FUEL);

        new PegasusCicActionEvent().execute(game);

        assertEquals(7, game.boards().galactica().fuel());
        assertEquals(7, game.decks().galacticaDamage().size());
    }
}