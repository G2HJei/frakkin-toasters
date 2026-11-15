package xyz.zlatanov.frakkintoasters.event.location.galactica;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.ship.Centurion;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ARMORY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAMUEL_T_ANDERS;

class ArmoryEventProcessorTest extends EventTestHarness<ArmoryEvent> {

    Centurion centurion;

    @BeforeEach
    void setUp() {
        player(1).character(SAMUEL_T_ANDERS);
        moveTo(ARMORY, SAMUEL_T_ANDERS);
        centurion = centurion();
        galacticaBoard.boardGalactica(centurion);
    }

    @Test
    void shouldRollToAttackTheCenturion() {
        execute(new ArmoryEvent(1, centurion.id()));

        assertFollowup(rollDie(new ArmoryResultEvent(centurion.id())));
    }

    @Test
    void shouldAttackACenturionOnTheBoardingPartyTrack() {
        assertInvalid(new ArmoryEvent(1, centurion().id()));
    }
}
