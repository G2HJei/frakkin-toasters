package xyz.zlatanov.frakkintoasters.event.board;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.BoardFact.JumpPreparationDecreased;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.track.JumpPreparation.POSITION_1;
import static xyz.zlatanov.frakkintoasters.state.track.JumpPreparation.START;

class DecreaseJumpPreparationEventProcessorTest extends EventTestHarness<DecreaseJumpPreparationEvent> {

    @Test
    void shouldMoveTheFleetTokenBack() {
        galacticaBoard.advanceJumpPreparation().advanceJumpPreparation();

        execute(new DecreaseJumpPreparationEvent());

        assertEquals(POSITION_1, galacticaBoard.jumpPreparation());
        assertEquals(List.of(new JumpPreparationDecreased()), journal.facts());
    }

    @Test
    void shouldStayOnTheStartSpace() {
        execute(new DecreaseJumpPreparationEvent());

        assertEquals(START, galacticaBoard.jumpPreparation());
    }
}
