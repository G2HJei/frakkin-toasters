package xyz.zlatanov.frakkintoasters.event.location;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BUILD_NUKE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_SCOUT;

class ResearchLabEventProcessorTest extends EventTestHarness<ResearchLabEvent> {

    @Test
    void shouldNotAllowInvalidSkillCardColor() {
        assertInvalid(new ResearchLabEvent(1, PILOTING));
    }

    @Test
    void shouldDrawEngineeringCard() {
        val testCard = new SkillCard(1, BUILD_NUKE);
        engineeringDeck.nextCard(testCard);

        execute(new ResearchLabEvent(1, ENGINEERING));

        assertSkillCards(1, testCard);
    }

    @Test
    void shouldDrawTacticsCard() {
        val testCard = new SkillCard(1, LAUNCH_SCOUT);
        tacticsDeck.nextCard(testCard);

        execute(new ResearchLabEvent(1, TACTICS));

        assertSkillCards(1, testCard);
    }
}