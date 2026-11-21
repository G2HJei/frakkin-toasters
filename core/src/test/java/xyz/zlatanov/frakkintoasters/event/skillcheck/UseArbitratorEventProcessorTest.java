package xyz.zlatanov.frakkintoasters.event.skillcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ASSIGN_ARBITRATOR;
import static xyz.zlatanov.frakkintoasters.state.character.Character.LAURA_ROSLIN;
import static xyz.zlatanov.frakkintoasters.state.character.Character.WILLIAM_ADAMA;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType.ADMIRALS_QUARTERS;

class UseArbitratorEventProcessorTest extends EventTestHarness<UseArbitratorEvent> {

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        ops.titles().putInPlay(ASSIGN_ARBITRATOR, 2);
        ops.skillCheck().start(ADMIRALS_QUARTERS);
    }

    @Test
    void theArbitratorLowersTheDifficultyAndDiscardsTheCard() {
        execute(new UseArbitratorEvent(2, -3));

        assertEquals(-3, game.activeSkillCheck().difficultyAdjustment());
        assertEquals(List.of(), game.quorumCardsInPlay());
        assertEquals(List.of(ASSIGN_ARBITRATOR), quorumDeck.discardedCards());
    }

    @Test
    void onlyBy3() {
        assertInvalid(new UseArbitratorEvent(2, 2));
    }

    @Test
    void onlyTheArbitrator() {
        assertInvalid(new UseArbitratorEvent(1, 3));
    }
}
