package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class DiscardAllSkillCardsEventProcessorTest extends EventTestHarness<DiscardAllSkillCardsEvent> {

    @Test
    void shouldDiscardWholeHand() {
        val politics = new SkillCard(1, CONSOLIDATE_POWER);
        val piloting = new SkillCard(2, EVASIVE_MANOEUVRES);
        player(1).gainSkillCards(politics, piloting);
        player(2).gainSkillCards(new SkillCard(3, REPAIR));

        execute(new DiscardAllSkillCardsEvent(1));

        assertNoSkillCards(1);
        assertEquals(1, player(2).skillCards().size());
        assertEquals(List.of(politics), politicsDeck.discardedCards());
        assertEquals(List.of(piloting), pilotingDeck.discardedCards());
    }

    @Test
    void shouldNotDrawMutinyCardForDiscardingBait() {
        player(1).gainSkillCards(new SkillCard(1, BAIT), new SkillCard(2, DRADIS_CONTACT));

        execute(new DiscardAllSkillCardsEvent(1));

        assertNoSkillCards(1);
    }

    @Test
    void shouldDoNothingWithEmptyHand() {
        execute(new DiscardAllSkillCardsEvent(1));

        assertNoSkillCards(1);
    }
}
