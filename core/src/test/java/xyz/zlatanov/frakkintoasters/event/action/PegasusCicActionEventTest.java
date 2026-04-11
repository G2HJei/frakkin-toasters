package xyz.zlatanov.frakkintoasters.event.action;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.fake.FakeDie;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage.*;
import static xyz.zlatanov.frakkintoasters.state.damage.PegasusDamage.PEGASUS_CIC;

class PegasusCicActionEventTest {
    FakeDie               die                 = new FakeDie();
    Deck<PegasusDamage>   pegasusDamageDeck   = new Deck<>();
    Deck<GalacticaDamage> galacticaDamageDeck = new Deck<>();
    Deck<BasestarDamage>  basestarDamageDeck  = new Deck<>();
    Game                  game                = Game.builder()
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
        pegasusDamageDeck.add(PEGASUS_CIC);

        new PegasusCicActionEvent().execute(game);

        assertTrue(game.boards().pegasus().damagedLocations().contains(Location.PEGASUS_CIC));
        //  assertEquals(4, game.decks().pegasusDamage().size());
    }

    @Test
    void shouldDamageGalactica() {
        die.nextRoll = 4;
        galacticaDamageDeck.add(WEAPONS_CONTROL);

        new PegasusCicActionEvent().execute(game);

        assertTrue(game.boards().galactica().damagedLocations().contains(Location.WEAPONS_CONTROL));
        //  assertEquals(7, game.decks().galacticaDamage().size());
    }

    @Test
    void shouldDamageGalacticaFood() {
        die.nextRoll = 5;
        galacticaDamageDeck.add(FOOD);

        new PegasusCicActionEvent().execute(game);

        assertEquals(7, game.boards().galactica().food());
        //  assertEquals(8, game.decks().galacticaDamage().size());
    }

    @Test
    void shouldDamageGalacticaFuel() {
        die.nextRoll = 6;
        galacticaDamageDeck.add(FUEL);

        new PegasusCicActionEvent().execute(game);

        assertEquals(7, game.boards().galactica().fuel());
        //   assertEquals(8, game.decks().galacticaDamage().size());
    }
}