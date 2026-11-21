package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;

class ResignationEventProcessorTest extends EventTestHarness<ResignationEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(RESIGNATION, EULOGY, ARREST_ORDER);
    }

    // The drawn cards and the hand go to the new President with the title.
    @Test
    void thePresidentRedrawsThenGivesAwayTheTitle() {
        execute(new ResignationEvent(1, List.of(EULOGY, ARREST_ORDER)));

        assertEquals(List.of(EULOGY, ARREST_ORDER), quorumDeck.discardedCards());
        assertFollowup(all(new DrawQuorumCardEvent(), new DrawQuorumCardEvent(),
                new PlayerDecisionEvent<>(1, MakePresidentEvent.class), new DiscardPlayedQuorumCardEvent(RESIGNATION)));
    }

    @Test
    void withoutDiscards() {
        execute(new ResignationEvent(1, List.of()));

        assertFollowup(all(new PlayerDecisionEvent<>(1, MakePresidentEvent.class),
                new DiscardPlayedQuorumCardEvent(RESIGNATION)));
    }

    // "Resignation" itself has left the hand.
    @Test
    void notTheResignationItself() {
        assertInvalid(new ResignationEvent(1, List.of(RESIGNATION)));
    }

    @Test
    void notWithoutAnotherPossiblePresident() {
        revealCylonPlayer2();

        assertInvalid(new ResignationEvent(1, List.of()));
    }

    private void revealCylonPlayer2() {
        player(2).loyaltyCards()
                .addOnTop(xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG)
                .reveal(xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG);
    }
}
