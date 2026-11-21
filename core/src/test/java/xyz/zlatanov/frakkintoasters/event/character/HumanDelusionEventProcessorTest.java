package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.CAPRICA;
import static xyz.zlatanov.frakkintoasters.state.character.Character.CAPRICA_SIX;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.HUMAN_DELUSION;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.EXECUTIVE_ORDER;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.REPAIR;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.INBOUND_NUKES;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.WATER_SABOTAGED;

class HumanDelusionEventProcessorTest extends EventTestHarness<HumanDelusionEvent> {

    private final SkillCard leadership  = new SkillCard(3, EXECUTIVE_ORDER);
    private final SkillCard engineering = new SkillCard(4, REPAIR);

    @BeforeEach
    void setUp() {
        player(1).character(CAPRICA_SIX);
        moveTo(CAPRICA, CAPRICA_SIX);
        player(1).gainSkillCards(leadership, engineering);
    }

    // After the reveal, any number of her cards go in, face up, each on the pile its color belongs to.
    @Test
    void addsCardsAfterTheReveal() {
        ops.skillCheck().start(WATER_SABOTAGED);
        ops.skillCheck().shuffleAndDivideCards();

        execute(new HumanDelusionEvent(1, List.of(leadership, engineering)));

        assertTrue(game.activeSkillCheck().dividedCards().containsAll(List.of(leadership, engineering)));
        assertNoSkillCards(1);
        assertEquals(0, player(1).miracleTokens());
        assertTrue(game.turn().hasUsed(1, HUMAN_DELUSION));
    }

    @Test
    void notOnASuperCrisisCheck() {
        ops.skillCheck().start(INBOUND_NUKES);

        assertEquals(NONE, HumanDelusionEventProcessor.offer(game));
        assertInvalid(new HumanDelusionEvent(1, List.of(leadership)));
    }

    @Test
    void notWithoutCards() {
        ops.skillCheck().start(WATER_SABOTAGED);
        val stranger = new SkillCard(5, EXECUTIVE_ORDER);

        assertInvalid(new HumanDelusionEvent(1, List.of(stranger)));
    }

    @Test
    void notNoCards() {
        ops.skillCheck().start(WATER_SABOTAGED);

        assertInvalid(new HumanDelusionEvent(1, List.of()));
    }
}
