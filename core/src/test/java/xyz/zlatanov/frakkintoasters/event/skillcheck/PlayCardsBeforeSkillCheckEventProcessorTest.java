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

        assertEquals(List.of(restoreOrder), game.activeSkillCheck().playedBeforeSkillCheck(1));
    }

    @Test
    void shouldNotAllowInvalidCards() {
        player(1).gainSkillCards(new SkillCard(0, DOGFIGHT));
        assertInvalid(new PlayCardsBeforeSkillCheckEvent(1, new SkillCard(0, DOGFIGHT)));
    }

    @Test
    void shouldNotAllowDuplicates() {
        player(1).gainSkillCards(restoreOrder, restoreOrder);
        assertInvalid(new PlayCardsBeforeSkillCheckEvent(1, restoreOrder, restoreOrder));
    }

    @Test
    void shouldNotAllowRecklessAndRestoreOrder() {
        player(1).gainSkillCards(restoreOrder, recklessCard);
        assertInvalid(new PlayCardsBeforeSkillCheckEvent(1, restoreOrder, recklessCard));
    }

    @Test
    void shouldPlayCardsInHand() {
        game.startSkillCheck(ADMIRALS_QUARTERS);
        assertInvalid(new PlayCardsBeforeSkillCheckEvent(1, restoreOrder));
    }
}
