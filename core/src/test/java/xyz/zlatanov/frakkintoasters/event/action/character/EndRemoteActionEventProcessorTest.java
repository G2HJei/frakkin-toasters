package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESEARCH_LAB;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LOUIS_HOSHI;

class EndRemoteActionEventProcessorTest extends EventTestHarness<EndRemoteActionEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LOUIS_HOSHI);
    }

    @Test
    void endsTheActionFromAnywhere() {
        ops.allowRemoteAction(1, RESEARCH_LAB);

        execute(new EndRemoteActionEvent(1));

        assertEquals(Optional.empty(), game.turn().remoteActionLocation(1));
    }
}
