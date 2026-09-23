package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.DANNA_BIERS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.KARA_STARBUCK_THRACE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheck.ADMIRALS_QUARTERS;

class PlaySkillsEventProcessorTest extends EventTestHarness<PlaySkillsEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(HANGAR_DECK, KARA_STARBUCK_THRACE);
        game.startSkillCheck(ADMIRALS_QUARTERS);
    }

    @Test
    void shouldAllowUpToOneCardForCharactersInBrig() {
        moveTo(BRIG, KARA_STARBUCK_THRACE);
        assertInvalid(new PlaySkillsEvent(1,
                new SkillCard(1, DOGFIGHT),
                new SkillCard(2, EXECUTIVE_ORDER)));
    }

    @Test
    void shouldAllowUpToOneCardForRevealedCylon() {
        revealCylon();
        assertInvalid(new PlaySkillsEvent(1,
                new SkillCard(1, DOGFIGHT),
                new SkillCard(2, EXECUTIVE_ORDER)));
    }

    @Test
    void shouldAllowUpToTwoCardsForInfiltrator() {
        player(2).character(DANNA_BIERS).infiltrateGalactica();
        moveTo(ADMINISTRATION, DANNA_BIERS);
        assertInvalid(new PlaySkillsEvent(2,
                new SkillCard(1, DOGFIGHT),
                new SkillCard(2, EXECUTIVE_ORDER),
                new SkillCard(3, FULL_THROTTLE)));
    }

    @Test
    void shouldContributeToActiveSkillCheck() {
        val contribution = new SkillCard(0, CALCULATIONS);
        execute(new PlaySkillsEvent(1, contribution));
        assertTrue(game.activeSkillCheck().cards().cards().contains(contribution));
    }
}