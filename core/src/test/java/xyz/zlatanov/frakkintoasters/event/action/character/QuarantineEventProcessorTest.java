package xyz.zlatanov.frakkintoasters.event.action.character;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SHERMAN_DOC_COTTLE;

class QuarantineEventProcessorTest extends EventTestHarness<QuarantineEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(SHERMAN_DOC_COTTLE);
        moveTo(RESEARCH_LAB, SHERMAN_DOC_COTTLE);
    }

    @Test
    void looksAtEveryCivilianShipThenChoosesOne() {
        val first = civilianShipAt(GALACTICA_SPACE_3_OCLOCK).id();
        val second = civilianShipAt(GALACTICA_SPACE_5_OCLOCK).id();

        execute(new QuarantineEvent(1));

        assertEquals(Set.of(first, second), player(1).revealedCivilianShipIds());
        assertFollowup(one(new QuarantineChoiceEvent(1, first), new QuarantineChoiceEvent(1, second)));
    }

    @Test
    void notWithoutACivilianShipInSpace() {
        assertInvalid(new QuarantineEvent(1));
    }
}
