package xyz.zlatanov.frakkintoasters.event.jump;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.track.JumpPreparation.START;

class ResetJumpPreparationEventProcessorTest extends EventTestHarness<ResetJumpPreparationEvent> {

    @Test
    void theFleetTokenGoesBackToTheStart() {
        galacticaBoard.advanceJumpPreparation().advanceJumpPreparation().advanceJumpPreparation();

        execute(new ResetJumpPreparationEvent());

        assertEquals(START, galacticaBoard.jumpPreparation());
    }
}
