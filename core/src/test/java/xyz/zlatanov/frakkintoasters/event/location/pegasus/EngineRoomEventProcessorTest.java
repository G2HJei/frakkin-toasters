package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.A_BETTER_MACHINE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.GUTS_AND_INITIATIVE;

class EngineRoomEventProcessorTest extends EventTestHarness<EngineRoomEvent> {

    @Test
    void shouldActivateEngineRoom() {
        val skillCard1 = new SkillCard(0, GUTS_AND_INITIATIVE);
        val skillCard2 = new SkillCard(1, A_BETTER_MACHINE);
        player(1).gainSkillCards(skillCard1, skillCard2);

        execute(new EngineRoomEvent(1, skillCard1, skillCard2));

        assertTrue(pegasusBoard.engineRoomActivated());
    }

}