package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMUNICATIONS;
import static xyz.zlatanov.frakkintoasters.state.character.Character.ANASTASIA_DEE_DUALLA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.WATER_SABOTAGED;

class FastLearnerIntoCheckEventProcessorTest extends EventTestHarness<FastLearnerIntoCheckEvent> {

    private final List<SkillCard> cards = List.of(new SkillCard(1, EXECUTIVE_ORDER), new SkillCard(2, EXECUTIVE_ORDER));

    @BeforeEach
    void setUp() {
        player(1).character(ANASTASIA_DEE_DUALLA);
        moveTo(COMMUNICATIONS, ANASTASIA_DEE_DUALLA);
        player(1).gainSkillCards(cards.toArray(SkillCard[]::new));
        ops.skillCheck().createDestinyDeck().start(WATER_SABOTAGED).playFromDestinyDeck(2);
    }

    @Test
    void theCardsGoIntoTheCheckFaceDown() {
        execute(new FastLearnerIntoCheckEvent(1, cards));

        assertTrue(game.activeSkillCheck().cards().cards().containsAll(cards));
        assertNoSkillCards(1);
    }

    @Test
    void onlyHers() {
        player(2).character(LAURA_ROSLIN);
        player(2).gainSkillCards(cards.toArray(SkillCard[]::new));

        assertInvalid(new FastLearnerIntoCheckEvent(2, cards));
    }

    @Test
    void notOnceTotaled() {
        ops.skillCheck().shuffleAndDivideCards();
        ops.skillCheck().totalStrength(0);

        assertInvalid(new FastLearnerIntoCheckEvent(1, cards));
    }
}
