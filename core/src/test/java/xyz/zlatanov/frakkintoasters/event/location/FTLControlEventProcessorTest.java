package xyz.zlatanov.frakkintoasters.event.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.CylonVictoryEvent;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.JumpingTheFleetEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FTLControlEventProcessorTest extends EventTestHarness<FTLControlEvent> {

    @BeforeEach
    void setUp() {
        galacticaBoard
                .advanceJumpPreparation()
                .advanceJumpPreparation()
                .advanceJumpPreparation();
    }

    @Test
    void shouldJumpTheFleetWithoutLosingPopulation() {
        nextRoll(7);
        execute(new FTLControlEvent(1));
        assertFollowup(new JumpingTheFleetEvent());
    }

    @Test
    void shouldLosePopulation() {
        nextRoll(6);
        execute(new FTLControlEvent(1));
        assertEquals(9, galacticaBoard.population());
        assertFollowup(new JumpingTheFleetEvent());
    }

    @Test
    void shouldEndGame() {
        nextRoll(6);
        galacticaBoard
                .advanceJumpPreparation()
                .decreasePopulation(11);

        execute(new FTLControlEvent(1));
        assertFollowup(new CylonVictoryEvent());
    }
}