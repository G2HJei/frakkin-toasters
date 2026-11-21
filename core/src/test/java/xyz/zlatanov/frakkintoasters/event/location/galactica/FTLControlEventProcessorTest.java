package xyz.zlatanov.frakkintoasters.event.location.galactica;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.AfterFtlControlRoll;
import xyz.zlatanov.frakkintoasters.state.interrupt.InterruptWindow.BeforeDieRoll;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
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
    void shouldRollForTheJump() {
        execute(new FTLControlEvent(1));

        assertFollowup(rollDie(new BeforeDieRoll(), new AfterFtlControlRoll(), new FTLControlResultEvent()));
    }

    @Test
    void shouldNotJumpFromTheRedZone() {
        galacticaBoard.decreaseJumpPreparation();
        assertInvalid(new FTLControlEvent(1));
    }
}
