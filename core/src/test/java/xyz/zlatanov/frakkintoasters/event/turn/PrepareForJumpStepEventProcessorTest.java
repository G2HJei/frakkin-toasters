package xyz.zlatanov.frakkintoasters.event.turn;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.GameStep.PREPARE_FOR_JUMP;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;

class PrepareForJumpStepEventProcessorTest extends EventTestHarness<PrepareForJumpStepEvent> {

    // "This step is not skipped on Cylon turns." (rule 6.1.3.4)
    @Test
    void aRevealedCylonHasTheStepToo() {
        player(1).character(KARA_STARBUCK_THRACE);
        revealCylon();

        execute(new PrepareForJumpStepEvent());

        assertEquals(PREPARE_FOR_JUMP, game.turn().step());
    }
}
