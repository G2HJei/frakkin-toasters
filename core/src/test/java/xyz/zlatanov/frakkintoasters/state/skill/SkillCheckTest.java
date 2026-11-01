package xyz.zlatanov.frakkintoasters.state.skill;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class SkillCheckTest {

    SkillCheck skillCheck             = new SkillCheck(ADMIRALS_QUARTERS);
    SkillCard  investigativeCommittee = new SkillCard(3, INVESTIGATIVE_COMMITTEE);
    SkillCard  aSecondChance          = new SkillCard(5, A_SECOND_CHANCE);
    SkillCard  restoreOrder           = new SkillCard(3, RESTORE_ORDER);
    SkillCard  juryRigged             = new SkillCard(1, JURY_RIGGED);

    @Test
    void shouldHaveNoConflictsWhenEachGroupHasSingleCard() {
        skillCheck.playedBefore(1, List.of(juryRigged, investigativeCommittee));
        skillCheck.playedBefore(2, List.of(aSecondChance));

        assertEquals(List.of(), skillCheck.playedBeforeConflicts());
    }

    @Test
    void shouldGroupCardsOfSameType() {
        skillCheck.playedBefore(1, List.of(investigativeCommittee, aSecondChance));
        skillCheck.playedBefore(2, List.of(investigativeCommittee));

        assertEquals(
                List.of(List.of(
                        new PlayedCard(1, investigativeCommittee),
                        new PlayedCard(2, investigativeCommittee))),
                skillCheck.playedBeforeConflicts());
    }

    @Test
    void shouldGroupAllRecklessAndRestoreOrderCardsTogether() {
        skillCheck.playedBefore(1, List.of(juryRigged));
        skillCheck.playedBefore(2, List.of(juryRigged));
        skillCheck.playedBefore(3, List.of(restoreOrder));

        assertEquals(
                List.of(List.of(
                        new PlayedCard(1, juryRigged),
                        new PlayedCard(2, juryRigged),
                        new PlayedCard(3, restoreOrder))),
                skillCheck.playedBeforeConflicts());
    }
}
