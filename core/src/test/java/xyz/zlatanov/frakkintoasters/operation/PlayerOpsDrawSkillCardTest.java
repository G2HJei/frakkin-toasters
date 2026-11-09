package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.ENGINEERING;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TACTICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BUILD_NUKE;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_SCOUT;

class PlayerOpsDrawSkillCardTest extends TestHarness {

    @Test
    void shouldGiveDrawnCardToPlayer() {
        val card = new SkillCard(1, BUILD_NUKE);
        engineeringDeck.nextCard(card);

        ops.player(1).drawSkillCards(ENGINEERING);

        assertSkillCards(1, card);
    }

    @Test
    void shouldGiveDrawnCardOnlyToTheSelectedPlayer() {
        val card = new SkillCard(2, LAUNCH_SCOUT);
        tacticsDeck.nextCard(card);

        ops.player(2).drawSkillCards(TACTICS);

        assertSkillCards(2, card);
        assertNoSkillCards(1);
    }

    // A card discarded after a draw emptied the deck is shuffled back in before the next draw.
    @Test
    void shouldReshuffleTheDiscardPileOfAnEmptyDeckBeforeDrawing() {
        val card = new SkillCard(2, LAUNCH_SCOUT);
        tacticsDeck.clear();
        tacticsDeck.discard(card);

        ops.player(1).drawSkillCards(TACTICS);

        assertSkillCards(1, card);
        assertEquals(0, tacticsDeck.discardSize());
    }
}
