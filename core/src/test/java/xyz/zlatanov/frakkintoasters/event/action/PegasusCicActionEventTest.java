package xyz.zlatanov.frakkintoasters.event.action;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.DamagePegasusEvent;
import xyz.zlatanov.frakkintoasters.fake.FakeDeck;
import xyz.zlatanov.frakkintoasters.fake.FakeDie;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;
import xyz.zlatanov.frakkintoasters.state.ship.Basestar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.followWith;
import static xyz.zlatanov.frakkintoasters.state.board.Location.GALACTICA_SPACE_2_OCLOCK;
import static xyz.zlatanov.frakkintoasters.state.damage.BasestarDamage.*;
import static xyz.zlatanov.frakkintoasters.state.util.AllCardsProvider.genericDeck;

class PegasusCicActionEventTest {
    FakeDie                  die             = new FakeDie();
    FakeDeck<BasestarDamage> basestarDmgDeck = new FakeDeck<>(genericDeck(BasestarDamage.class));
    Game                     game            = Game.builder()
            .die(die)
            .decks(DecksHolder.builder()
                    .basestarDamage(basestarDmgDeck)
                    .build())
            .build();
    Basestar                 basestar        = game.cylonShips().basestar();

    @BeforeEach
    void setUp() {
        game.boards()
                .galactica()
                .place(GALACTICA_SPACE_2_OCLOCK, basestar);
    }

    @Test
    void shouldDamagePegasus() {
        die.nextRoll(3);
        val followup = new PegasusCicActionEvent(basestar.id()).execute(game);
        assertEquals(followWith(new DamagePegasusEvent()), followup);
    }

    @Test
    void shouldDamageBasestar() {
        basestarDmgDeck.nextCard(DISABLED_WEAPONS);
        die.nextRoll(5);

        new PegasusCicActionEvent(basestar.id()).execute(game);

        assertEquals(1, basestar.damage().size());
        assertTrue(basestar.damage().contains(DISABLED_WEAPONS));
        assertEquals(3, basestarDmgDeck.cards().size());

    }

    @Test
    void shouldDamageBasestarTwice() {
        basestarDmgDeck.nextCard(DISABLED_WEAPONS).nextCard(STRUCTURAL_DAMAGE);
        die.nextRoll(8);

        new PegasusCicActionEvent(basestar.id()).execute(game);

        assertEquals(2, basestar.damage().size());
        assertEquals(2, basestarDmgDeck.cards().size());

    }

    @Test
    void shouldDamageOnlyOnceIfDestroyed() {
        basestarDmgDeck.nextCard(CRITICAL_HIT);
        basestar.damage(basestarDmgDeck.draw());
        die.nextRoll(8);

        new PegasusCicActionEvent(basestar.id()).execute(game);

        assertTrue(game.boards().galactica().shipsIn(GALACTICA_SPACE_2_OCLOCK).isEmpty());
        assertTrue(basestar.damage().isEmpty());
        assertEquals(4, basestarDmgDeck.cards().size());
    }
}
