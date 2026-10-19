package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;
import java.util.Map;

import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class TiebreakCardsBeforeSkillCheckEventProcessorTest extends EventTestHarness<TiebreakCardsBeforeSkillCheckEvent> {
    SkillCard atAnyCOst     = new SkillCard(0, AT_ANY_COST);
    SkillCard aSecondChance = new SkillCard(0, A_SECOND_CHANCE);
    SkillCard restoreOrder  = new SkillCard(0, RESTORE_ORDER);
    SkillCard juryRigged    = new SkillCard(0, JURY_RIGGED);
    SkillCard juryRigged2   = new SkillCard(1, JURY_RIGGED);

    @Test
    void shouldReturnExtraCardsToPlayers() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
        game.activeSkillCheck().playedBefore(1, List.of(aSecondChance, restoreOrder, juryRigged, atAnyCOst));
        game.activeSkillCheck().playedBefore(2, List.of(restoreOrder, juryRigged));
        game.activeSkillCheck().playedBefore(3, List.of(aSecondChance, juryRigged2));

        execute(new TiebreakCardsBeforeSkillCheckEvent(Map.of(
                JURY_RIGGED, 1,
                RESTORE_ORDER, 2,
                A_SECOND_CHANCE, 3)));

        assertSkillCards(1, aSecondChance, restoreOrder);
        assertSkillCards(2, juryRigged);
        assertSkillCards(3, juryRigged2);
    }

}