package xyz.zlatanov.frakkintoasters.event.action.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class CrypticMessageEventProcessorTest extends EventTestHarness<CrypticMessageEvent> {

    private static final SkillCard ORDER  = new SkillCard(1, EXECUTIVE_ORDER);
    private static final SkillCard SCOUT  = new SkillCard(2, LAUNCH_SCOUT);
    private static final SkillCard REPAIRS = new SkillCard(3, REPAIR);

    @BeforeEach
    void setUp() {
        player(1).character(LEOBEN_CONOY);
        player(2).character(WILLIAM_ADAMA);
        moveTo(HUMAN_FLEET, LEOBEN_CONOY);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
    }

    @Test
    void swapsTheirHands() {
        player(1).gainSkillCards(ORDER);
        player(2).gainSkillCards(SCOUT, REPAIRS);

        execute(new CrypticMessageEvent(1, 2));

        assertSkillCards(1, SCOUT, REPAIRS);
        assertSkillCards(2, ORDER);
    }

    @Test
    void notWithHimself() {
        assertInvalid(new CrypticMessageEvent(1, 1));
    }
}
