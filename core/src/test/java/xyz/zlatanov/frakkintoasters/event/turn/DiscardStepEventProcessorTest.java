package xyz.zlatanov.frakkintoasters.event.turn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardQuorumCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_DOWN_TO_HAND_LIMIT;
import static xyz.zlatanov.frakkintoasters.state.GameStep.DISCARD;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.REPAIR;

class DiscardStepEventProcessorTest extends EventTestHarness<DiscardStepEvent> {

    private int nextCardId = 100;

    @BeforeEach
    void setUp() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(WILLIAM_ADAMA);
        player(3).character(CHIEF_GALEN_TYROL);
        president(LAURA_ROSLIN);
    }

    @Test
    void nobodyOverTheLimitDiscardsNothing() {
        giveSkillCards(1, 10);
        giveSkillCards(3, 8);

        execute(new DiscardStepEvent());

        assertEquals(DISCARD, game.turn().step());
    }

    // "If a player has more than 10 Skill Cards or 10 Quorum Cards in his hand at the end of any player's turn, he must
    // discard cards (of his choice) until he has only 10." (rule 8.8)
    @Test
    void everyPlayerOverTheLimitDiscardsClockwiseFromTheCurrentPlayer() {
        game.turn().currentPlayerNumber(2);
        giveSkillCards(1, 11);
        giveSkillCards(2, 15);
        giveSkillCards(3, 9); // "Reckless - Your hand limit is 8 (instead of 10)."

        execute(new DiscardStepEvent());

        assertFollowup(all(discardSkillCards(2), discardSkillCards(3), discardSkillCards(1)));
    }

    @Test
    void thePresidentDiscardsQuorumCardsOverTheLimit() {
        presidentHand.addOnTop(Arrays.asList(QuorumCard.values()).subList(0, 11));

        execute(new DiscardStepEvent());

        assertFollowup(new PlayerDecisionEvent<>(1, DiscardQuorumCardsEvent.class, DISCARD_DOWN_TO_HAND_LIMIT));
    }

    @Test
    void theLimitCountsSkillAndQuorumCardsApart() {
        giveSkillCards(1, 10);
        presidentHand.addOnTop(Arrays.asList(QuorumCard.values()).subList(0, 10));

        execute(new DiscardStepEvent());

        assertEquals(DISCARD, game.turn().step());
    }

    private void giveSkillCards(int playerNumber, int count) {
        player(playerNumber).gainSkillCards(IntStream.range(0, count)
                .mapToObj(i -> new SkillCard(nextCardId++, REPAIR))
                .toArray(SkillCard[]::new));
    }

    private static Event discardSkillCards(int playerNumber) {
        return new PlayerDecisionEvent<>(playerNumber, DiscardSkillCardsEvent.class, DISCARD_DOWN_TO_HAND_LIMIT);
    }
}
