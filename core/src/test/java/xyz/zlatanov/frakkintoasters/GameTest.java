package xyz.zlatanov.frakkintoasters;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.ObjectiveCard.KOBOL;

class GameTest {

    Game game = new Game(KOBOL);

    @Test
    void shouldStartWithKobol() {
        assertEquals(KOBOL, game.objective());
    }

    @Test
    void shouldStartWithFood() {
        assertEquals(8, game.food());
    }

    @Test
    void shouldStartWithMorale() {
        assertEquals(10, game.morale());
    }

    @Test
    void shouldStartWithPopulation() {
        assertEquals(12, game.population());
    }
}