package xyz.zlatanov.frakkintoasters.event.location.galactica;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.JumpingTheFleetEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FTLControlResultEventProcessorTest extends EventTestHarness<FTLControlResultEvent> {

    @BeforeEach
    void setUp() {
        galacticaBoard
                .advanceJumpPreparation()
                .advanceJumpPreparation()
                .advanceJumpPreparation();
    }

    @Test
    void shouldJumpTheFleetWithoutLosingPopulation() {
        rolled(7);
        execute(new FTLControlResultEvent());
        assertEquals(12, galacticaBoard.population());
        assertFollowup(new JumpingTheFleetEvent());
    }

    @Test
    void shouldLosePopulation() {
        rolled(6);
        execute(new FTLControlResultEvent());
        assertEquals(9, galacticaBoard.population());
        assertFollowup(new JumpingTheFleetEvent());
    }

    @Test
    void shouldJumpEvenWhenPopulationReachesZero() {
        rolled(6);
        galacticaBoard
                .advanceJumpPreparation()
                .decreasePopulation(11);

        execute(new FTLControlResultEvent());

        assertEquals(0, galacticaBoard.population());
        assertFollowup(new JumpingTheFleetEvent());
    }
}
