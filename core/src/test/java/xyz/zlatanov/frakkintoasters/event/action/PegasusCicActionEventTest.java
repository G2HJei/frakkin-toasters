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

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        pegasusDamageDeck.add(PegasusDamage.PEGASUS_CIC);

        new PegasusCicActionEvent().execute(game);

        assertTrue(game.boards().pegasus().damagedLocations().contains(Location.PEGASUS_CIC));
    }
}