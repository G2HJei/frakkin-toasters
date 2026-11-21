package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.MoveFromBrigEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.REPAIR;

class AttorneyEventProcessorTest extends EventTestHarness<AttorneyEvent> {

    private static final SkillCard ORDER   = new SkillCard(1, EXECUTIVE_ORDER);
    private static final SkillCard REPAIRS = new SkillCard(2, REPAIR);

    @BeforeEach
    void setUp() {
        player(1).character(ROMO_LAMPKIN);
        player(2).character(WILLIAM_ADAMA);
        moveTo(ADMINISTRATION, ROMO_LAMPKIN);
        moveTo(BRIG, WILLIAM_ADAMA);
        player(2).gainSkillCards(ORDER, REPAIRS);
    }

    @Test
    void freesAPrisonerAndTakesHisSkillCards() {
        execute(new AttorneyEvent(1, 2, COMMAND));

        assertSkillCards(1, ORDER, REPAIRS);
        assertNoSkillCards(2);
        assertFollowup(new MoveFromBrigEvent(2, COMMAND));
    }

    @Test
    void himselfKeepsHisCards() {
        moveTo(BRIG, ROMO_LAMPKIN);
        player(1).gainSkillCards(ORDER);

        execute(new AttorneyEvent(1, 1, COMMAND));

        assertSkillCards(1, ORDER);
        assertFollowup(new MoveFromBrigEvent(1, COMMAND));
    }

    @Test
    void notToAHazardousLocation() {
        assertInvalid(new AttorneyEvent(1, 2, SICKBAY));
    }

    @Test
    void notACharacterOutOfTheBrig() {
        assertInvalid(new AttorneyEvent(1, 1, COMMAND));
    }
}
