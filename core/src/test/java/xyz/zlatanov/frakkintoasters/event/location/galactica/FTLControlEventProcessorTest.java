package xyz.zlatanov.frakkintoasters.event.location.galactica;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.JumpingTheFleetEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.board.Location.FTL_CONTROL;
import static xyz.zlatanov.frakkintoasters.state.character.Character.FELIX_GAETA;

class FTLControlEventProcessorTest extends EventTestHarness<FTLControlEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(FELIX_GAETA);
        moveTo(FTL_CONTROL, FELIX_GAETA);
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
    void shouldJumpEvenWhenPopulationReachesZero() {
        nextRoll(6);
        galacticaBoard
                .advanceJumpPreparation()
                .decreasePopulation(11);

        execute(new FTLControlEvent(1));

        assertEquals(0, galacticaBoard.population());
        assertFollowup(new JumpingTheFleetEvent());
    }

    @Test
    void shouldNotJumpFromTheRedZone() {
        galacticaBoard.decreaseJumpPreparation();
        assertInvalid(new FTLControlEvent(1));
    }
}
