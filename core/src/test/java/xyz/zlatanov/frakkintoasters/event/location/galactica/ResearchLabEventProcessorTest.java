package xyz.zlatanov.frakkintoasters.event.location.galactica;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESEARCH_LAB;
import static xyz.zlatanov.frakkintoasters.state.character.Character.GAIUS_BALTAR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARL_HELO_AGATHON;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BUILD_NUKE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_SCOUT;

class ResearchLabEventProcessorTest extends EventTestHarness<ResearchLabEvent> {

    @BeforeEach
    void setUpAtLocation() {
        player(1).character(GAIUS_BALTAR);
        moveTo(RESEARCH_LAB, GAIUS_BALTAR);
    }

    @Test
    void shouldNotAllowInvalidSkillCardColor() {
        assertInvalid(new ResearchLabEvent(1, PILOTING));
    }

    @Test
    void shouldNotAllowMissingSkillCardColor() {
        assertInvalid(new ResearchLabEvent(1, null));
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

    @Test
    void shouldOnlyBeUsedAtTheLocation() {
        moveTo(COMMAND, GAIUS_BALTAR);
        assertInvalid(new ResearchLabEvent(1, TACTICS));
    }

    @Test
    void shouldNotBeUsedByACharacterOffTheBoard() {
        player(2).character(KARL_HELO_AGATHON); // "Stranded"
        assertInvalid(new ResearchLabEvent(2, TACTICS));
    }

    @Test
    void shouldNotDrawAColorWithNoCardsLeft() {
        tacticsDeck.clear();
        assertInvalid(new ResearchLabEvent(1, TACTICS));
    }
}
