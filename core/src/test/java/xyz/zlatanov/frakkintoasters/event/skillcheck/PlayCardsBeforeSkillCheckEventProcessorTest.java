package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class PlayCardsBeforeSkillCheckEventProcessorTest extends EventTestHarness<PlayCardsBeforeSkillCheckEvent> {

    SkillCard restoreOrder = new SkillCard(0, RESTORE_ORDER);
    SkillCard recklessCard = new SkillCard(0, JURY_RIGGED);

    @Test
    void shouldTrackCardsPlayedBeforeSkillCheck() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
        player(1).gainSkillCards(restoreOrder);

        execute(new PlayCardsBeforeSkillCheckEvent(1, restoreOrder));

        assertEquals(List.of(restoreOrder), game.activeSkillCheck().playedBeforeSkillCheck().get(1));
    }

    @Test
    void shouldNotAllowInvalidCards() {
        assertInvalid(new PlayCardsBeforeSkillCheckEvent(1, new SkillCard(0, DOGFIGHT)));
    }

    @Test
    void shouldNotAllowDuplicates() {
        assertInvalid(new PlayCardsBeforeSkillCheckEvent(1, restoreOrder, restoreOrder));
    }

    @Test
    void shouldNotAllowRecklessAndRestoreOrder() {
        assertInvalid(new PlayCardsBeforeSkillCheckEvent(1, restoreOrder, recklessCard));
    }
}