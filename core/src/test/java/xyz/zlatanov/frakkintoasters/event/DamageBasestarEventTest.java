package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.fake.FakeDeck;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.CRITICAL_HIT;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.DISABLED_HANGAR_BAY;

class DamageBasestarEventTest {
    FakeDeck<BasestarDamage> basestarDmgDeck = new FakeDeck<>();
    Game                     game            = Game.builder()
            .decks(DecksHolder.builder()
                    .basestarDamage(basestarDmgDeck)
                    .build()).build();

    @Test
    void shouldAutoDamageSingleBasestar() {
        val basestar = new Basestar();
        basestarDmgDeck.nextCard(CRITICAL_HIT);
        val galacticaBoard = game.boards().galactica();
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, basestar);

        new DamageBasestarEvent().execute(game);
        assertTrue(basestar.damage().contains(CRITICAL_HIT));
        assertFalse(game.decks().basestarDamage().cards().contains(CRITICAL_HIT));
    }

    @Test
    void shouldDestroyBasestarOn3rdHit() {
        val basestar = new Basestar().damage(DISABLED_HANGAR_BAY);
        basestarDmgDeck.nextCard(CRITICAL_HIT);
        val galacticaBoard = game.boards().galactica();
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, basestar);

        new DamageBasestarEvent().execute(game);

        assertTrue(galacticaBoard.shipsIn(GALACTICA_SPACE_2_OCLOCK).isEmpty());
        assertEquals(4, game.decks().basestarDamage().cards().size());
    }
}