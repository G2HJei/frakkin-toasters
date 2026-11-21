package xyz.zlatanov.frakkintoasters.operation;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.CardsGained;
import xyz.zlatanov.frakkintoasters.engine.journal.fact.PlayerFact.CardsLost;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.PlayerPile.SKILL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class PlayerOpsGiveSkillCardsTest extends TestHarness {

    private final SkillCard repair     = new SkillCard(1, REPAIR);
    private final SkillCard buildNuke  = new SkillCard(2, BUILD_NUKE);
    private final SkillCard launchScout = new SkillCard(3, LAUNCH_SCOUT);

    @Test
    void theCardsMoveFromOneHandToTheOther() {
        player(1).gainSkillCards(repair, buildNuke);

        ops.player(1).giveSkillCards(2, List.of(buildNuke));

        assertSkillCards(1, repair);
        assertSkillCards(2, buildNuke);
        assertEquals(List.of(new CardsLost(1, SKILL, List.of(buildNuke)), new CardsGained(2, SKILL, List.of(buildNuke))),
                journal.facts());
    }

    @Test
    void givingNothingRecordsNothing() {
        ops.player(1).giveSkillCards(2, List.of());

        assertEquals(List.of(), journal.facts());
    }

    @Test
    void takesAsManyRandomCardsAsHeHoldsUpToTheCount() {
        player(2).gainSkillCards(repair, buildNuke);

        ops.player(1).takeRandomSkillCards(2, 3);

        assertEquals(2, player(1).skillCards().size());
        assertNoSkillCards(2);
    }

    @Test
    void takesTheCountWhenHeHoldsMore() {
        player(2).gainSkillCards(repair, buildNuke, launchScout);

        ops.player(1).takeRandomSkillCards(2, 2);

        val taken = player(1).skillCards().cards();
        assertEquals(2, taken.size());
        assertEquals(1, player(2).skillCards().size());
    }
}
