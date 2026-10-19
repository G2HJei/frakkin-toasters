package xyz.zlatanov.frakkintoasters.event.skillcheck.ability;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CALCULATIONS;

class InstallUpgradesEventProcessorTest extends EventTestHarness<InstallUpgradesEvent> {

    SkillCard card1 = new SkillCard(1, CALCULATIONS);
    SkillCard card2 = new SkillCard(2, CALCULATIONS);

    @Test
    void shouldDraw2WhenPassed() {
        engineeringDeck.nextCard(card1, card2);
        execute(new InstallUpgradesEvent(true));
        assertSkillCards(1, card1, card2);
    }

    @Test
    void shouldDraw1WhenFailed() {
        engineeringDeck.nextCard(card1);
        execute(new InstallUpgradesEvent(false));
        assertSkillCards(1, card1);
    }

}