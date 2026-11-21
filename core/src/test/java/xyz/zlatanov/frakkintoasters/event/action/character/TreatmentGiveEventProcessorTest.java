package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class TreatmentGiveEventProcessorTest extends EventTestHarness<TreatmentGiveEvent> {

    private static final SkillCard ORDER   = new SkillCard(1, EXECUTIVE_ORDER);
    private static final SkillCard SCOUT   = new SkillCard(2, LAUNCH_SCOUT);
    private static final SkillCard REPAIRS = new SkillCard(3, REPAIR);

    @BeforeEach
    void setUp() {
        player(1).character(SHERMAN_DOC_COTTLE);
        player(2).character(WILLIAM_ADAMA);
        player(1).gainSkillCards(ORDER, SCOUT, REPAIRS);
    }

    @Test
    void givesTheCards() {
        execute(new TreatmentGiveEvent(1, 2, List.of(ORDER, SCOUT)));

        assertSkillCards(1, REPAIRS);
        assertSkillCards(2, ORDER, SCOUT);
    }

    @Test
    void notOneCard() {
        assertInvalid(new TreatmentGiveEvent(1, 2, List.of(ORDER)));
    }
}
