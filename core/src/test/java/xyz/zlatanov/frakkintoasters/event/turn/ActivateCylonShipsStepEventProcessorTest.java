package xyz.zlatanov.frakkintoasters.event.turn;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.GameStep.ACTIVATE_CYLON_SHIPS;
import static xyz.zlatanov.frakkintoasters.state.GameStep.SETUP;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.board.Location.HANGAR_DECK;
import static xyz.zlatanov.frakkintoasters.state.GameStep.SETUP;

class ActivateCylonShipsStepEventProcessorTest extends EventTestHarness<ActivateCylonShipsStepEvent> {

    @Test
    void aHumanHasTheStep() {
        player(1).character(KARA_STARBUCK_THRACE);

        execute(new ActivateCylonShipsStepEvent());

        assertEquals(ACTIVATE_CYLON_SHIPS, game.turn().step());
    }

    // "Exceptions: Revealed Cylons do not perform this step." (rule 8.6)
    @Test
    void aRevealedCylonHasNoStep() {
        player(1).character(KARA_STARBUCK_THRACE);
        revealCylon();

        execute(new ActivateCylonShipsStepEvent());

        assertEquals(SETUP, game.turn().step());
    }

    // The turn ended early, e.g. the current player was executed (rule 7.1), so the step doesn't happen.
    @Test
    void shouldNotHappenAfterTheTurnEndedEarly() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        ops.endTurnEarly();

        execute(new ActivateCylonShipsStepEvent());

        assertEquals(SETUP, game.turn().step());
    }
}
