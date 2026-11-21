package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCardInPlay;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_1_OF_ANY_COLOR;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ACCEPT_PROPHECY;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;

class AcceptProphecyEventProcessorTest extends EventTestHarness<AcceptProphecyEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(ACCEPT_PROPHECY);
    }

    // It stays in play without a holder.
    @Test
    void theCardStaysInPlayAndThePresidentDrawsACardOfAnyColor() {
        execute(new AcceptProphecyEvent(1));

        assertEquals(List.of(new QuorumCardInPlay(ACCEPT_PROPHECY, null)), game.quorumCardsInPlay());
        assertEquals(List.of(), quorumDeck.discardedCards());
        assertFollowup(new PlayerDecisionEvent<>(1, DrawSkillCardsEvent.class, DRAW_EXACTLY_1_OF_ANY_COLOR));
    }
}
