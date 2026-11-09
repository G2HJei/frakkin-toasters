package xyz.zlatanov.frakkintoasters.event.board;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.JumpPreparationIncreased;
import xyz.zlatanov.frakkintoasters.event.JumpingTheFleetEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.track.JumpPreparation.*;

class IncreaseJumpPreparationEventProcessorTest extends EventTestHarness<IncreaseJumpPreparationEvent> {

    @Test
    void shouldMoveTheFleetTokenAhead() {
        execute(new IncreaseJumpPreparationEvent());

        assertEquals(POSITION_1, galacticaBoard.jumpPreparation());
        assertEquals(List.of(new JumpPreparationIncreased()), journal.facts());
    }

    // "Auto Jump: If the fleet token is moved to the "Auto Jump" space of the Jump Preparation track" (rule 12.1)
    @Test
    void theFleetJumpsWhenTheTokenReachesAutoJump() {
        galacticaBoard.advanceJumpPreparation().advanceJumpPreparation().advanceJumpPreparation().advanceJumpPreparation();
        assertEquals(MINUS_1, galacticaBoard.jumpPreparation());

        execute(new IncreaseJumpPreparationEvent());

        assertEquals(START, galacticaBoard.jumpPreparation());
        assertFollowup(new JumpingTheFleetEvent());
    }
}
