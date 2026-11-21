package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextAdmiralEvent;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.Title.ADMIRAL;
import static xyz.zlatanov.frakkintoasters.state.Title.PRESIDENT;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.character.Character.ELLEN_TIGH;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;

// Ellen Tigh is the current player.
class ManipulativeEventProcessorTest extends EventTestHarness<ManipulativeEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(ELLEN_TIGH);
        player(2).character(SAUL_TIGH);
        player(3).character(LAURA_ROSLIN);
        moveTo(COMMAND, SAUL_TIGH);
        ops.titles().assignAdmiral(SAUL_TIGH);
        ops.titles().assignPresident(LAURA_ROSLIN);
    }

    @Test
    void sheMayTakeATitleAtTheStartOfHerTurn() {
        assertEquals(one(new ManipulativeEvent(1, ADMIRAL), new ManipulativeEvent(1, PRESIDENT), new NoOpEvent(1)),
                ManipulativeEventProcessor.offer(game));

        execute(new ManipulativeEvent(1, ADMIRAL));

        assertEquals(ELLEN_TIGH, game.admiral());
        assertEquals(Map.of(ADMIRAL, SAUL_TIGH), game.turn().borrowedTitles());
        assertEquals(0, player(1).miracleTokens());
    }

    @Test
    void itGoesBackAtTheEndOfHerTurn() {
        execute(new ManipulativeEvent(1, PRESIDENT));

        assertEquals(NONE, ManipulativeEventProcessor.giveTitlesBack(game, ops));
        assertEquals(LAURA_ROSLIN, game.president());
    }

    // Saul Tigh in the "Brig" can't be Admiral: the title goes to the next in line but her.
    @Test
    void toTheNextInLineWhenThePreviousHolderCantTakeIt() {
        execute(new ManipulativeEvent(1, ADMIRAL));
        moveTo(BRIG, SAUL_TIGH);

        assertEquals(single(new DetermineNextAdmiralEvent(1)), ManipulativeEventProcessor.giveTitlesBack(game, ops));
    }

    @Test
    void notATitleSheHolds() {
        ops.titles().assignPresident(ELLEN_TIGH);

        assertEquals(one(new ManipulativeEvent(1, ADMIRAL), new NoOpEvent(1)), ManipulativeEventProcessor.offer(game));
        assertInvalid(new ManipulativeEvent(1, PRESIDENT));
    }

    @Test
    void onceAGame() {
        ops.player(1).loseMiracleTokens();

        assertEquals(NONE, ManipulativeEventProcessor.offer(game));
        assertInvalid(new ManipulativeEvent(1, ADMIRAL));
    }
}
