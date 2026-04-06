package xyz.zlatanov.frakkintoasters.event.officials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.state.Game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.card.ObjectiveCard.KOBOL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_APOLLO_ADAMA;

class DetermineNextCagEventTest {
    Game game = new Game(KOBOL, 2);

    @BeforeEach
    void setUp() {
        game.player(1).selectCharacter(KARA_STARBUCK_THRACE);
        game.player(2).selectCharacter(LEE_APOLLO_ADAMA);

        new DetermineNextCagEvent().execute(game);
    }

    @Test
    void shouldRespectLineOfSuccession() {
        assertEquals(LEE_APOLLO_ADAMA, game.cag());
    }

    @Test
    void shouldIgnoreCharactersInBrig() {
        game.moveTo(BRIG, LEE_APOLLO_ADAMA);
        new DetermineNextCagEvent().execute(game);
        assertEquals(KARA_STARBUCK_THRACE, game.cag());
    }

    @Test
    void shouldElectNoOneWhenAllAreInBrig() {
        game.moveTo(BRIG, KARA_STARBUCK_THRACE);
        game.moveTo(BRIG, LEE_APOLLO_ADAMA);
        new DetermineNextCagEvent().execute(game);
        assertNull(game.cag());
    }
}