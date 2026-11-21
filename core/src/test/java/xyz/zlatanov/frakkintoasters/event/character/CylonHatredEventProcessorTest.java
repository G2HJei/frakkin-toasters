package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.CYLON_HATRED;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.BRIG;

class CylonHatredEventProcessorTest extends EventTestHarness<CylonHatredEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(SAUL_TIGH);
        moveTo(COMMAND, SAUL_TIGH);
    }

    @Test
    void lowersTheAdmiralsQuartersByThree() {
        ops.skillCheck().start(ADMIRALS_QUARTERS, 2);
        assertEquals(one(new CylonHatredEvent(1), new NoOpEvent(1)), CylonHatredEventProcessor.offer(game));

        execute(new CylonHatredEvent(1));

        assertEquals(-3, game.activeSkillCheck().difficultyAdjustment());
        assertTrue(game.turn().hasUsed(1, CYLON_HATRED));
    }

    @Test
    void onlyTheAdmiralsQuarters() {
        ops.skillCheck().start(BRIG, 2);

        assertEquals(NONE, CylonHatredEventProcessor.offer(game));
        assertInvalid(new CylonHatredEvent(1));
    }
}
