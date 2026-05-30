package xyz.zlatanov.frakkintoasters.event.player;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.zlatanov.frakkintoasters.event.EventTest;
import xyz.zlatanov.frakkintoasters.state.Game;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.GameStep.RECEIVE_SKILLS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.AT_ANY_COST;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BAIT;

@ExtendWith(MockitoExtension.class)
class DrawSkillCardsEventTest extends EventTest {

    SkillCard leadershipCard = new SkillCard(0, AT_ANY_COST);
    SkillCard treacheryCard  = new SkillCard(0, BAIT);


    @BeforeEach
    void setUp() {
        setUpGame(Game.builder(4).build());
        player(1).selectCharacter(KARA_STARBUCK_THRACE);
    }

    @Test
    void shouldReceiveCardsWithinSkillSet() {
        leadershipDeck.nextCard(leadershipCard);
        execute(new DrawSkillCardsEvent(1, Map.of(LEADERSHIP, 1)));
        assertEquals(List.of(leadershipCard), player(1).skillCards().cards());
    }

    @Test
    void shouldNotReceiveOutsideOfSkillSet() {
        assertFalse(isValid(new DrawSkillCardsEvent(1, Map.of(TREACHERY, 1))));
    }

    @Test
    void shouldReceiveAnyColorWhenRevealedCylon() {
        revealCylon();
        leadershipDeck.nextCard(leadershipCard);
        treacheryDeck.nextCard(treacheryCard);

        execute(new DrawSkillCardsEvent(1, Map.of(LEADERSHIP, 1, TREACHERY, 1)));

        assertEquals(List.of(leadershipCard, treacheryCard), player(1).skillCards().cards());
    }

    @Test
    void shouldNotAllowDoubleSelectionForRevealedCylon() {
        revealCylon();
        assertFalse(isValid(new DrawSkillCardsEvent(1, Map.of(TREACHERY, 2))));
    }

    @ParameterizedTest
    @MethodSource("cylonLeaderSelection")
    void shouldValidateCylonLeaderSelection(Character cylonLeader, Map<SkillCardColor, Integer> selection) {
        player(2).selectCharacter(cylonLeader);
        assertTrue(isValid(new DrawSkillCardsEvent(2, selection)));
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
        player(2)
                .selectCharacter(cylonLeader)
                .infiltrateGalactica();
        assertTrue(isValid(new DrawSkillCardsEvent(2, selection)));
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
        val action = new DrawSkillCardsEvent(1, Map.of(LEADERSHIP, 1, TREACHERY, 1), DRAW_EXACTLY_2);
        assertTrue(action.isValidConstraint(game, DRAW_EXACTLY_2));
    }

    @Test
    void shouldFailDrawExactly2ConstraintWhenTotalIsNot2() {
        val action = new DrawSkillCardsEvent(1, Map.of(LEADERSHIP, 1), DRAW_EXACTLY_2);
        assertFalse(action.isValidConstraint(game, DRAW_EXACTLY_2));
    }

    @Test
    void shouldDrawOnly1CardWhenInSickbay() {
        game.moveTo(SICKBAY, KARA_STARBUCK_THRACE)
                .step(RECEIVE_SKILLS);
        assertTrue(isValid(new DrawSkillCardsEvent(1, Map.of(TACTICS, 1))));
        assertFalse(isValid(new DrawSkillCardsEvent(1, Map.of(TACTICS, 2))));
    }

    @Test
    void shouldDrawOnly1CardWhenInResurrectionShip() {
        game.moveTo(RESURRECTION_SHIP, KARA_STARBUCK_THRACE)
                .step(RECEIVE_SKILLS);
        assertTrue(isValid(new DrawSkillCardsEvent(1, Map.of(TACTICS, 1))));
        assertFalse(isValid(new DrawSkillCardsEvent(1, Map.of(TACTICS, 2))));
    }

    @Test
    void shouldNotDrawWhenInHubDestroyed() {
        galacticaBoard.destroyResurrectionShip();
        game.moveTo(HUB_DESTROYED, KARA_STARBUCK_THRACE)
                .step(RECEIVE_SKILLS);
        assertTrue(isValid(new DrawSkillCardsEvent(1, Map.of())));
        assertFalse(isValid(new DrawSkillCardsEvent(1, Map.of(TACTICS, 1))));
    }

    @Test
    void shouldNotApplyReceiveSkillGameStepRestrictionOnOtherSteps() {
        game.moveTo(SICKBAY, KARA_STARBUCK_THRACE);
        assertTrue(isValid(new DrawSkillCardsEvent(1, Map.of(TACTICS, 2))));

    }

    private void revealCylon() {
        player(1).loyaltyCards().add(CYLON_SEND_TO_BRIG);
        player(1).loyaltyCards().reveal(CYLON_SEND_TO_BRIG);
    }
}