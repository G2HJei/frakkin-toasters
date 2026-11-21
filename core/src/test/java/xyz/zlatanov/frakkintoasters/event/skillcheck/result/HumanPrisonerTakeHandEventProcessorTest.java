package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class HumanPrisonerTakeHandEventProcessorTest extends EventTestHarness<HumanPrisonerTakeHandEvent> {

    private final SkillCard executiveOrder = new SkillCard(2, EXECUTIVE_ORDER);
    private final SkillCard repair         = new SkillCard(1, REPAIR);

    @BeforeEach
    void setUpCylonAndHuman() {
        player(1).character(FELIX_GAETA);
        player(2).character(LAURA_ROSLIN);
        revealCylon();
        player(2).gainSkillCards(executiveOrder, repair);
        ops.skillCheck().startSuperCrisis(SkillCheckType.HUMAN_PRISONER, 1);
    }

    @Test
    void shouldTakeTheWholeHandAndSendTheHumanToSickbay() {
        execute(new HumanPrisonerTakeHandEvent(1, 2));

        assertSkillCards(1, executiveOrder, repair);
        assertNoSkillCards(2);
        assertFollowup(new SendToSickbayEvent(2));
    }

    @Test
    void shouldRejectAPlayerWhoDidNotPlayTheCard() {
        assertInvalid(new HumanPrisonerTakeHandEvent(2, 2));
    }

    @Test
    void shouldRejectACylonTarget() {
        assertInvalid(new HumanPrisonerTakeHandEvent(1, 1));
    }

    @Test
    void shouldRejectAnUnknownTarget() {
        assertInvalid(new HumanPrisonerTakeHandEvent(1, 9));
    }
}
