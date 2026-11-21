package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

// "the ships are activated one entire space area at a time in the order of the current player's choice" (rule 11.5)
class ActivateRaiderAreasEventProcessorTest extends EventTestHarness<ActivateRaiderAreasEvent> {

    @Test
    void theCurrentPlayerChoosesTheFirstSpaceArea() {
        val raider1 = raiderAt(GALACTICA_SPACE_2_OCLOCK);
        val raider2 = raiderAt(GALACTICA_SPACE_6_OCLOCK);
        val raiders = List.of(raider1.id(), raider2.id());

        execute(new ActivateRaiderAreasEvent(raiders));

        assertFollowup(one(
                new ActivateRaiderAreaEvent(GALACTICA_SPACE_2_OCLOCK, raiders),
                new ActivateRaiderAreaEvent(GALACTICA_SPACE_6_OCLOCK, raiders)));
    }

    // With one area there is nothing to choose.
    @Test
    void oneSpaceAreaActivatesAtOnce() {
        val raider1 = raiderAt(GALACTICA_SPACE_2_OCLOCK);
        val raider2 = raiderAt(GALACTICA_SPACE_2_OCLOCK);
        val raiders = List.of(raider1.id(), raider2.id());

        execute(new ActivateRaiderAreasEvent(raiders));

        assertFollowup(new ActivateRaiderAreaEvent(GALACTICA_SPACE_2_OCLOCK, raiders));
    }

    // A raider destroyed in the meantime doesn't activate.
    @Test
    void nothingIsLeftOnceTheRaidersAreGone() {
        execute(new ActivateRaiderAreasEvent(List.of(99)));
    }
}
