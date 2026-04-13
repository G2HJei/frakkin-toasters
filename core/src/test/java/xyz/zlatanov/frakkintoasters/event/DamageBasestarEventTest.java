package xyz.zlatanov.frakkintoasters.event;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.player.AssignBasestarDamage;
import xyz.zlatanov.frakkintoasters.fake.FakeDeck;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.followWith;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.*;
import static xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider.genericDeck;

class DamageBasestarEventTest {
    FakeDeck<BasestarDamage> basestarDmgDeck = new FakeDeck<>(genericDeck(BasestarDamage.class));
    Game                     game            = Game.builder()
            .decks(DecksHolder.builder()
                    .basestarDamage(basestarDmgDeck)
                    .build()).build();

    @Test
    void shouldAutoDamageSingleBasestar() {
        val basestar = game.cylonShips().basestar();
        basestarDmgDeck.nextCard(CRITICAL_HIT);
        val galacticaBoard = game.boards().galactica();
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, basestar);

        new DamageBasestarEvent().execute(game);
        assertTrue(basestar.damage().contains(CRITICAL_HIT));
        assertFalse(game.decks().basestarDamage().cards().contains(CRITICAL_HIT));
    }

    @Test
    void shouldDestroyBasestarOn3rdHit() {
        basestarDmgDeck.nextCard(STRUCTURAL_DAMAGE).draw();
        basestarDmgDeck.nextCard(DISABLED_HANGAR_BAY).draw();
        val basestar = game.cylonShips().basestar()
                .damage(STRUCTURAL_DAMAGE)
                .damage(DISABLED_HANGAR_BAY);
        basestarDmgDeck.nextCard(DISABLED_WEAPONS);
        val galacticaBoard = game.boards().galactica();
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, basestar);

        new DamageBasestarEvent().execute(game);

        assertTrue(galacticaBoard.shipsIn(GALACTICA_SPACE_2_OCLOCK).isEmpty());
        assertEquals(4, game.decks().basestarDamage().cards().size());
        assertEquals(2, game.cylonShips().basestars().size());
    }

    @Test
    void shouldCountCriticalHitAs2Hits() {
        basestarDmgDeck.nextCard(DISABLED_HANGAR_BAY).draw();
        val basestar = game.cylonShips().basestar()
                .damage(DISABLED_HANGAR_BAY);
        basestarDmgDeck.nextCard(CRITICAL_HIT);
        val galacticaBoard = game.boards().galactica();
        galacticaBoard.place(GALACTICA_SPACE_2_OCLOCK, basestar);

        new DamageBasestarEvent().execute(game);

        assertTrue(galacticaBoard.shipsIn(GALACTICA_SPACE_2_OCLOCK).isEmpty());
        assertEquals(4, game.decks().basestarDamage().cards().size());
    }

    @Test
    void shouldLetCurrentPlayerDistributeDamage() {
        val basestar1 = game.cylonShips().basestar();
        val basestar2 = game.cylonShips().basestar();
        basestarDmgDeck.nextCard(DISABLED_WEAPONS);
        game.boards().galactica().place(GALACTICA_SPACE_2_OCLOCK, List.of(basestar1, basestar2));

        val followup = new DamageBasestarEvent().execute(game);

        assertEquals(1, followup.size());
        assertTrue(followup.getFirst().events()
                .containsAll(List.of(
                        new AssignBasestarDamage(1, DISABLED_WEAPONS, basestar1.id()),
                        new AssignBasestarDamage(1, DISABLED_WEAPONS, basestar2.id()))
                ));
    }
}
