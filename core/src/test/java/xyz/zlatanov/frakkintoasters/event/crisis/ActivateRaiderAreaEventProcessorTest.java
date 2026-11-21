package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;

class ActivateRaiderAreaEventProcessorTest extends EventTestHarness<ActivateRaiderAreaEvent> {

    // The raiders of the chosen area activate, then the current player chooses among the areas left.
    @Test
    void theRaidersOfTheAreaActivateThenTheOthers() {
        val raider1 = raiderAt(GALACTICA_SPACE_2_OCLOCK);
        val raider2 = raiderAt(GALACTICA_SPACE_6_OCLOCK);
        val raider3 = raiderAt(GALACTICA_SPACE_6_OCLOCK);

        execute(new ActivateRaiderAreaEvent(GALACTICA_SPACE_6_OCLOCK, List.of(raider1.id(), raider2.id(), raider3.id())));

        assertFollowup(all(
                new ActivateRaiderEvent(raider2.id()),
                new ActivateRaiderEvent(raider3.id()),
                new ActivateRaiderAreasEvent(List.of(raider1.id()))));
    }

    @Test
    void theLastAreaEndsTheActivation() {
        val raider = raiderAt(GALACTICA_SPACE_2_OCLOCK);

        execute(new ActivateRaiderAreaEvent(GALACTICA_SPACE_2_OCLOCK, List.of(raider.id())));

        assertFollowup(new ActivateRaiderEvent(raider.id()));
    }

    @Test
    void notAnAreaWithoutRaidersOfTheActivation() {
        val raider = raiderAt(GALACTICA_SPACE_2_OCLOCK);

        assertInvalid(new ActivateRaiderAreaEvent(GALACTICA_SPACE_6_OCLOCK, List.of(raider.id())));
    }
}
