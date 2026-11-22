package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardAllSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardRandomSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.discard;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.CHOSEN;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.HAND;
import static xyz.zlatanov.frakkintoasters.event.player.DiscardKind.RANDOM;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_DOWN_TO_1;
import static xyz.zlatanov.frakkintoasters.state.board.Location.ADMINISTRATION;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.ROMO_LAMPKIN;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.DECEITFUL;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;

class DeceitfulEventProcessorTest extends EventTestHarness<DeceitfulEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(ROMO_LAMPKIN);
        moveTo(ADMINISTRATION, ROMO_LAMPKIN);
        for (int i = 0; i < 4; i++) {
            ops.player(1).receiveSkillCard(new SkillCard(1, CONSOLIDATE_POWER));
        }
    }

    @Test
    void discardsOneFewer() {
        execute(new DeceitfulEvent(1, CHOSEN, 3));

        assertTrue(game.turn().hasUsed(1, DECEITFUL));
        assertFollowup(discard(1, 2));
    }

    // With 4 cards, "discard 5" takes 4, so he discards 3.
    @Test
    void fromWhatHeWouldDiscard() {
        execute(new DeceitfulEvent(1, RANDOM, 5));

        assertFollowup(new DiscardRandomSkillCardsEvent(1, 3));
    }

    @Test
    void aSingleCardIsntDiscarded() {
        execute(new DeceitfulEvent(1, CHOSEN, 1));
    }

    // His whole hand but 1 card, which he keeps.
    @Test
    void keepsACardOfHisHand() {
        execute(new DeceitfulEvent(1, HAND, 0));

        assertFollowup(new PlayerDecisionEvent<>(1, DiscardSkillCardsEvent.class, DISCARD_DOWN_TO_1));
    }

    // The first of his discards of a card is reduced, the others aren't, nor anyone else's.
    @Test
    void oncePerCard() {
        player(2).character(LAURA_ROSLIN);
        val discards = DeceitfulDiscards.of(game, true);

        assertEquals(discard(2, 2), discards.discard(2, 2));
        assertEquals(new DeceitfulEvent(1, HAND, 0), discards.discardAll(1));
        assertEquals(new DiscardAllSkillCardsEvent(1), discards.discardAll(1));
        assertEquals(discard(1, 2), DeceitfulDiscards.of(game, false).discard(1, 2));

        // only Romo Lampkin discards one fewer
        execute(new DeceitfulEvent(2, CHOSEN, 2));
        assertFollowup(discard(2, 2));
    }

    // Romo Lampkin was executed before his discard came, e.g. by the same "A Guilty Verdict": the player discards in
    // full (F-040758).
    @Test
    void inFullOnceRomoLampkinIsGone() {
        ops.player(1).removeCharacter();

        execute(new DeceitfulEvent(1, CHOSEN, 3));

        assertFollowup(discard(1, 3));
    }
}
