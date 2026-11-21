package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ASSIGN_CHIEF_OF_STAFF;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class UseChiefOfStaffEventProcessorTest extends EventTestHarness<UseChiefOfStaffEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        ops.titles().putInPlay(ASSIGN_CHIEF_OF_STAFF, 2);
        ops.skillCheck().start(ADMIRALS_QUARTERS);
    }

    @Test
    void politicsCountsPositiveAndTheCardIsDiscarded() {
        execute(new UseChiefOfStaffEvent(2));

        assertTrue(game.activeSkillCheck().politicsCountPositive());
        assertEquals(List.of(), game.quorumCardsInPlay());
    }

    @Test
    void onlyTheChiefOfStaff() {
        assertInvalid(new UseChiefOfStaffEvent(1));
    }
}
