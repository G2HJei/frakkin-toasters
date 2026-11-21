package xyz.zlatanov.frakkintoasters.event.action.quorum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsOfColorEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCardInPlay;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ASSIGN_ARBITRATOR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;

class AssignArbitratorEventProcessorTest extends EventTestHarness<AssignArbitratorEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(KARA_STARBUCK_THRACE);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(ASSIGN_ARBITRATOR);
    }

    @Test
    void anotherPlayerHoldsTheCardAndThePresidentDrawsPolitics() {
        execute(new AssignArbitratorEvent(1, 2));

        assertEquals(List.of(new QuorumCardInPlay(ASSIGN_ARBITRATOR, 2)), game.quorumCardsInPlay());
        assertEquals(Optional.of(2), game.quorumCardHolder(ASSIGN_ARBITRATOR));
        assertFollowup(new DrawSkillCardsOfColorEvent(1, POLITICS, 2));
    }

    @Test
    void notThePresidentHimself() {
        assertInvalid(new AssignArbitratorEvent(1, 1));
    }

    // Revealed Cylons lose them anyway (rule 6.1.1).
    @Test
    void notARevealedCylon() {
        player(3).loyaltyCards()
                .addOnTop(xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG)
                .reveal(xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG);

        assertInvalid(new AssignArbitratorEvent(1, 3));
    }
}
