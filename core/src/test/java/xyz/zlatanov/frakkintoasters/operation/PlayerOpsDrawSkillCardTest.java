package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.ENGINEERING;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TACTICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BUILD_NUKE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_SCOUT;

class PlayerOpsDrawSkillCardTest extends TestHarness {

    @Test
    void shouldGiveDrawnCardToPlayer() {
        val card = new SkillCard(1, BUILD_NUKE);
        engineeringDeck.nextCard(card);

        ops.player(1).drawSkillCard(ENGINEERING);

        assertSkillCards(1, card);
    }

    @Test
    void shouldGiveDrawnCardOnlyToTheSelectedPlayer() {
        val card = new SkillCard(2, LAUNCH_SCOUT);
        tacticsDeck.nextCard(card);

        ops.player(2).drawSkillCard(TACTICS);

        assertSkillCards(2, card);
        assertNoSkillCards(1);
    }
}
