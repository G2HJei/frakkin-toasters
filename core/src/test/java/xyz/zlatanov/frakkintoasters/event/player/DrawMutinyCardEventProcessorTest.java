package xyz.zlatanov.frakkintoasters.event.player;

import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.CONSOLIDATE_POWER;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LEE_ADAMA;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.deck.Discard1MutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.Location.PRESIDENTS_OFFICE;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.MUTINEER;
import static xyz.zlatanov.frakkintoasters.state.card.MutinyCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;

class DrawMutinyCardEventProcessorTest extends EventTestHarness<DrawMutinyCardEvent> {

    private static final Followup DISCARD_1 = single(new PlayerDecisionEvent<>(1, Discard1MutinyCardEvent.class));

    @Test
    void shouldDrawMutinyCard() {
        player(1).character(LAURA_ROSLIN);
        mutinyDeck.nextCard(PANIC);

        execute(new DrawMutinyCardEvent(1));

        assertEquals(PANIC, player(1).mutinyCards().cards().getFirst());
        assertEquals(1, player(1).mutinyCards().size());
    }

    @Test
    void shouldNotDrawForRevealedCylon() {
        player(1).character(LAURA_ROSLIN);
        revealCylon();

        execute(new DrawMutinyCardEvent(1));

        assertTrue(player(1).mutinyCards().isEmpty());
    }

    @Test
    void shouldNotDrawForCylonLeaderWhoIsNotInfiltrating() {
        player(1).character(CAPRICA_SIX);

        execute(new DrawMutinyCardEvent(1));

        assertTrue(player(1).mutinyCards().isEmpty());
    }

    @Test
    void shouldDrawForInfiltratingCylonLeader() {
        player(1).character(CAPRICA_SIX).infiltrateGalactica();

        execute(new DrawMutinyCardEvent(1));

        assertEquals(1, player(1).mutinyCards().size());
    }

    // "When a player who already has a Mutiny Card draws a second Mutiny Card, he must immediately move to the "Brig"
    // [...] Any time a player with more than one Mutiny Card is in the "Brig," he chooses one Mutiny Card to keep and
    // discards the rest." (rule 5.2.3.2)
    @Test
    void theSecondCardSendsThePlayerToTheBrigToKeepOne() {
        holdingOneMutinyCard();
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);

        execute(new DrawMutinyCardEvent(1));

        assertEquals(2, player(1).mutinyCards().size());
        assertFollowup(all(single(new SendToBrigEvent(1)), DISCARD_1));
    }

    // "If a player in the "Brig" already has a Mutiny Card and draws a second, he must immediately choose one to keep
    // and discard the other." (rule 5.2.3.2)
    @Test
    void inTheBrigTheSecondCardOnlyHasToBeDiscarded() {
        holdingOneMutinyCard();
        moveTo(BRIG, LAURA_ROSLIN);

        execute(new DrawMutinyCardEvent(1));

        assertFollowup(DISCARD_1);
    }

    // "The Mutineer does not move to the "Brig" when he draws a second Mutiny Card. Instead, when the Mutineer draws a
    // third Mutiny Card, he must immediately move to the "Brig"" (rule 5.2.3.3)
    @Test
    void theMutineerKeepsTheSecondCard() {
        holdingOneMutinyCard();
        player(1).loyaltyCards().addOnTop(MUTINEER);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);

        execute(new DrawMutinyCardEvent(1));

        assertEquals(2, player(1).mutinyCards().size());
    }

    // "Any time the Mutineer has more than two Mutiny Cards while in the "Brig," he must choose two Mutiny Cards to keep
    // and discard the rest." (rule 5.2.3.3)
    @Test
    void theThirdCardSendsTheMutineerToTheBrigToKeepTwo() {
        holdingOneMutinyCard();
        player(1).mutinyCards().addOnTop(WEAPONS_ARMED);
        player(1).loyaltyCards().addOnTop(MUTINEER);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);

        execute(new DrawMutinyCardEvent(1));

        assertFollowup(all(single(new SendToBrigEvent(1)), DISCARD_1));
    }

    @Test
    void everyCardOverTheLimitIsDiscarded() {
        holdingOneMutinyCard();
        player(1).mutinyCards().addOnTop(WEAPONS_ARMED);
        moveTo(BRIG, LAURA_ROSLIN);

        execute(new DrawMutinyCardEvent(1));

        assertFollowup(all(DISCARD_1, DISCARD_1));
    }

    // "If Helo receives a second Mutiny Card [...] while he's "Stranded", he discards down to one Mutiny Card [...], but
    // does not move to the "Brig"." (rule A2.1.1.10)
    @Test
    void aStrandedHeloDiscardsWithoutMovingToTheBrig() {
        player(1).character(KARL_HELO_AGATHON).mutinyCards().addOnTop(FEED_THE_PEOPLE);

        execute(new DrawMutinyCardEvent(1));

        assertEquals(Optional.empty(), game.locate(KARL_HELO_AGATHON));
        assertFollowup(DISCARD_1);
    }

    private void holdingOneMutinyCard() {
        player(1).character(LAURA_ROSLIN).mutinyCards().addOnTop(FEED_THE_PEOPLE);
        mutinyDeck.nextCard(PANIC);
    }

    // "Moral Dilemma": Lee Adama discards 2 Skill Cards for the Mutiny Card he draws.
    @Test
    void leeAdamaDiscardsForTheMutinyCard() {
        player(1).character(LEE_ADAMA);
        player(1).skillCards().addOnTop(new SkillCard(1, CONSOLIDATE_POWER));
        mutinyDeck.nextCard(PANIC);

        execute(new DrawMutinyCardEvent(1));

        assertFollowup(new PlayerDecisionEvent<>(1, DiscardSkillCardsEvent.class, DISCARD_EXACTLY_2));
    }

    // The "Brig" and the discard before "Moral Dilemma": a "Bait" it discards draws another Mutiny Card, which has
    // to see them done (F-4985f5).
    @Test
    void leeAdamaGoesToTheBrigBeforeHeDiscardsForTheSecondCard() {
        player(1).character(LEE_ADAMA).mutinyCards().addOnTop(FEED_THE_PEOPLE);
        moveTo(COMMAND, LEE_ADAMA);
        player(1).skillCards().addOnTop(new SkillCard(1, CONSOLIDATE_POWER));
        mutinyDeck.nextCard(PANIC);

        execute(new DrawMutinyCardEvent(1));

        assertFollowup(all(all(single(new SendToBrigEvent(1)), DISCARD_1),
                single(new PlayerDecisionEvent<>(1, DiscardSkillCardsEvent.class, DISCARD_EXACTLY_2))));
    }
}
