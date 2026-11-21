package xyz.zlatanov.frakkintoasters.event.character;

import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.FollowupUtils;
import xyz.zlatanov.frakkintoasters.event.player.DiscardAllSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardRandomSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.CHOSEN;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.HAND;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.RANDOM;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SAUL_TIGH;
import static xyz.zlatanov.frakkintoasters.state.character.Character.SHARON_ATHENA_AGATHON;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;

class ForcedDiscardEventProcessorTest extends EventTestHarness<ForcedDiscardEvent> {

    // Without Sharon "Athena" Agathon a discard is what it always was, so those games keep their journals.
    @Test
    void plainWithoutAthena() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(SAUL_TIGH);

        assertEquals(FollowupUtils.discard(1, 2), ForcedDiscards.discard(game, 1, CHOSEN, 2));
        assertEquals(new DiscardRandomSkillCardsEvent(1, 2), ForcedDiscards.discard(game, 1, RANDOM, 2));
        assertEquals(new DiscardAllSkillCardsEvent(1), ForcedDiscards.discard(game, 1, HAND, 0));
        execute(new ForcedDiscardEvent(1, CHOSEN, 2, false));
        assertFollowup(FollowupUtils.discard(1, 2));
    }

    @Test
    void athenaIsAsked() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(SHARON_ATHENA_AGATHON);
        player(1).gainSkillCards(new SkillCard(1, CONSOLIDATE_POWER));

        assertEquals(new ForcedDiscardEvent(1, CHOSEN, 2, false), ForcedDiscards.discard(game, 1, CHOSEN, 2));
        execute(new ForcedDiscardEvent(1, CHOSEN, 2, false));

        // the option to go on names nobody, so she chooses
        assertFollowup(one(new ForLoveEvent(2, 1, CHOSEN, 2), new ForcedDiscardEvent(1, CHOSEN, 2, true)));
    }

    @Test
    void notAskedTwice() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(SHARON_ATHENA_AGATHON);
        player(1).gainSkillCards(new SkillCard(1, CONSOLIDATE_POWER));

        execute(new ForcedDiscardEvent(1, CHOSEN, 2, true));

        assertFollowup(FollowupUtils.discard(1, 2));
    }

    // Her own discards aren't hers to make smaller.
    @Test
    void notForAthenaHerself() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(SHARON_ATHENA_AGATHON);
        player(2).gainSkillCards(new SkillCard(1, CONSOLIDATE_POWER));

        assertEquals(new DiscardRandomSkillCardsEvent(2, 1), ForcedDiscards.discard(game, 2, RANDOM, 1));
        execute(new ForcedDiscardEvent(2, RANDOM, 1, false));
        assertFollowup(new DiscardRandomSkillCardsEvent(2, 1));
    }

    @Test
    void nothingToMakeSmaller() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(SHARON_ATHENA_AGATHON);

        execute(new ForcedDiscardEvent(1, CHOSEN, 2, false));

        assertFollowup(FollowupUtils.discard(1, 2));
    }
}
