package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCenturionEvent;
import xyz.zlatanov.frakkintoasters.state.ship.Centurion;
import xyz.zlatanov.frakkintoasters.state.track.BoardingParty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.STRATEGIC_PLANNING;

class ArmoryResultEventProcessorTest extends EventTestHarness<ArmoryResultEvent> {

    Centurion centurion;

    @BeforeEach
    void setUp() {
        centurion = centurion();
        galacticaBoard.boardGalactica(centurion);
    }

    @Test
    void shouldDoNothingBelow7() {
        rolled(6);

        execute(new ArmoryResultEvent(centurion.id()));

        assertEquals(BoardingParty.START, galacticaBoard.boardingPartyTrack().get(centurion));
    }

    // "Major Victory": "Play after you destroy a basestar or a centurion on the Boarding Party track."
    @Test
    void shouldDestroyTheCenturionOn7Or8() {
        rolled(7);

        execute(new ArmoryResultEvent(centurion.id()));

        assertFollowup(new DestroyCenturionEvent(centurion.id()));
    }

    @Test
    void shouldDestroyTheChosenCenturion() {
        val other = centurion();
        galacticaBoard.boardGalactica(other);
        rolled(8);

        execute(new ArmoryResultEvent(other.id()));

        assertFollowup(new DestroyCenturionEvent(other.id()));
    }

    @Test
    void shouldReadTheModifiedResult() {
        ops.startDieRoll();
        ops.playOnDieRoll(STRATEGIC_PLANNING, 2);
        nextRoll(5);
        ops.rollDie();

        execute(new ArmoryResultEvent(centurion.id()));

        assertFollowup(new DestroyCenturionEvent(centurion.id()));
    }
}
