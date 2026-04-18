package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.deck.Deck;
import xyz.zlatanov.frakkintoasters.state.deck.DecksHolder;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.AT_ANY_COST;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BAIT;

@ExtendWith(MockitoExtension.class)
class ReceiveSkillCardsEventTest {

    @SuppressWarnings("unchecked")
    Deck<SkillCard> leadershipDeck = mock(Deck.class);
    @SuppressWarnings("unchecked")
    Deck<SkillCard> treacheryDeck  = mock(Deck.class);
    SkillCard leadershipCard = new SkillCard(0, AT_ANY_COST);
    SkillCard treacheryCard  = new SkillCard(0, BAIT);
    Game      game           = Game.builder(4)
            .decks(DecksHolder.builder()
                    .leadership(leadershipDeck)
                    .treachery(treacheryDeck)
                    .build()).build();

    @BeforeEach
    void setUp() {
        game.player(1).selectCharacter(KARA_STARBUCK_THRACE);
    }

    @Test
    void shouldReceiveCardsWithinSkillSet() {
        when(leadershipDeck.draw()).thenReturn(leadershipCard);
        new ReceiveSkillCardsEvent(1, Map.of(LEADERSHIP, 1)).execute(game);
        assertEquals(List.of(leadershipCard), game.player(1).skillCards().cards());
    }

    @Test
    void shouldNotReceiveOutsideOfSkillSet() {
        val action = new ReceiveSkillCardsEvent(1, Map.of(TREACHERY, 1));
        assertFalse(action.isValid(game));
    }

    @Test
    void shouldReceiveAnyColorWhenRevealedCylon() {
        revealCylon();
        when(leadershipDeck.draw()).thenReturn(leadershipCard);
        when(treacheryDeck.draw()).thenReturn(treacheryCard);

        new ReceiveSkillCardsEvent(1, Map.of(LEADERSHIP, 1, TREACHERY, 1)).execute(game);

        assertEquals(List.of(leadershipCard, treacheryCard), game.player(1).skillCards().cards());
    }

    @Test
    void shouldNotAllowDoubleSelectionForRevealedCylon() {
        revealCylon();
        assertFalse(new ReceiveSkillCardsEvent(1, Map.of(TREACHERY, 2)).isValid(game));
    }

    @ParameterizedTest
    @MethodSource("cylonLeaderSelection")
    void shouldValidateCylonLeaderSelection(Character cylonLeader, Map<SkillCardColor, Integer> selection) {
        game.player(2).selectCharacter(cylonLeader);
        assertTrue(new ReceiveSkillCardsEvent(2, selection).isValid(game));
    }

    public static Stream<Arguments> cylonLeaderSelection() {
        return Stream.of(
                arguments(CAPRICA_SIX, Map.of(LEADERSHIP, 1, ENGINEERING, 1)),
                arguments(CAPRICA_SIX, Map.of(LEADERSHIP, 1, TREACHERY, 1)),

                arguments(DANNA_BIERS, Map.of(LEADERSHIP, 1, TREACHERY, 1)),
                arguments(DANNA_BIERS, Map.of(POLITICS, 1, TREACHERY, 1)),
                arguments(DANNA_BIERS, Map.of(POLITICS, 1, ENGINEERING, 1)),
                arguments(DANNA_BIERS, Map.of(POLITICS, 1, ENGINEERING, 1))
        );
    }

    @ParameterizedTest
    @MethodSource("infiltratingCylonLeaderSelection")
    void shouldAllowExtraCardForInfiltratingCylonLeader(Character cylonLeader, Map<SkillCardColor, Integer> selection) {
        game.player(2)
                .selectCharacter(cylonLeader)
                .infiltrateGalactica();
        assertTrue(new ReceiveSkillCardsEvent(2, selection).isValid(game));
    }

    public static Stream<Arguments> infiltratingCylonLeaderSelection() {
        return Stream.of(
                arguments(CAPRICA_SIX, Map.of(LEADERSHIP, 1, TREACHERY, 1, ENGINEERING, 1)),
                arguments(CAPRICA_SIX, Map.of(LEADERSHIP, 2, ENGINEERING, 1)),
                arguments(CAPRICA_SIX, Map.of(LEADERSHIP, 2, TREACHERY, 1)),

                arguments(DANNA_BIERS, Map.of(LEADERSHIP, 1, TREACHERY, 1, ENGINEERING, 1)),
                arguments(DANNA_BIERS, Map.of(POLITICS, 1, TREACHERY, 1, ENGINEERING, 1)),
                arguments(DANNA_BIERS, Map.of(LEADERSHIP, 1, POLITICS, 1, ENGINEERING, 1)),
                arguments(DANNA_BIERS, Map.of(LEADERSHIP, 1, POLITICS, 1, TREACHERY, 1))
        );
    }

    @Test
    void shouldValidateDrawExactly2ConstraintWhenTotalIs2() {
        revealCylon();
        val action = new ReceiveSkillCardsEvent(1, Map.of(LEADERSHIP, 1, TREACHERY, 1), DRAW_EXACTLY_2);
        assertTrue(action.isValid(game));
    }

    @Test
    void shouldFailDrawExactly2ConstraintWhenTotalIsNot2() {
        val action = new ReceiveSkillCardsEvent(1, Map.of(LEADERSHIP, 1), DRAW_EXACTLY_2);
        assertFalse(action.isValid(game));
    }

    private void revealCylon() {
        game.player(1).loyaltyCards().add(CYLON_SEND_TO_BRIG);
        game.player(1).loyaltyCards().reveal(CYLON_SEND_TO_BRIG);
    }
}