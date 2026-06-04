package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.event.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.state.GameStep.RECEIVE_SKILLS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.CYLON_SEND_TO_BRIG;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.AT_ANY_COST;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BAIT;

class DrawSkillCardsEventProcessorTest extends EventTestHarness<DrawSkillCardsEvent> {

    SkillCard leadershipCard = new SkillCard(0, AT_ANY_COST);
    SkillCard treacheryCard  = new SkillCard(0, BAIT);


    @BeforeEach
    void setUp() {
        setUpGame(4);
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
    }

    @Test
    void shouldReceiveCardsWithinSkillSet() {
        leadershipDeck.nextCard(leadershipCard);
        execute(new DrawSkillCardsEvent(1, Map.of(LEADERSHIP, 1)));
        assertSkillCards(1, leadershipCard);
    }

    @Test
    void shouldNotReceiveOutsideOfSkillSet() {
        assertInvalid(new DrawSkillCardsEvent(1, Map.of(TREACHERY, 1)));
    }

    @Test
    void shouldReceiveAnyColorWhenRevealedCylon() {
        revealCylon();
        leadershipDeck.nextCard(leadershipCard);
        treacheryDeck.nextCard(treacheryCard);

        execute(new DrawSkillCardsEvent(1, Map.of(LEADERSHIP, 1, TREACHERY, 1)));

        assertSkillCards(1, treacheryCard, leadershipCard);
    }

    @Test
    void shouldNotAllowDoubleSelectionForRevealedCylon() {
        revealCylon();
        assertInvalid(new DrawSkillCardsEvent(1, Map.of(TREACHERY, 2)));
    }

    @ParameterizedTest
    @MethodSource("cylonLeaderSelection")
    void shouldValidateCylonLeaderSelection(Character cylonLeader, Map<SkillCardColor, Integer> selection) {
        player(2).character(cylonLeader);
        assertDoesNotThrow(() -> execute(new DrawSkillCardsEvent(2, selection)));
    }

    public static Stream<Arguments> cylonLeaderSelection() {
        return Stream.of(
                arguments(CAPRICA_SIX, Map.of(LEADERSHIP, 1, ENGINEERING, 1)),
                arguments(CAPRICA_SIX, Map.of(LEADERSHIP, 1, TREACHERY, 1)),

                arguments(DANNA_BIERS, Map.of(LEADERSHIP, 1, TREACHERY, 1)),
                arguments(DANNA_BIERS, Map.of(POLITICS, 1, TREACHERY, 1)),
                arguments(DANNA_BIERS, Map.of(POLITICS, 1, ENGINEERING, 1)),
                arguments(DANNA_BIERS, Map.of(LEADERSHIP, 1, ENGINEERING, 1))
        );
    }

    @ParameterizedTest
    @MethodSource("infiltratingCylonLeaderSelection")
    void shouldAllowExtraCardForInfiltratingCylonLeader(Character cylonLeader, Map<SkillCardColor, Integer> selection) {
        player(2).character(cylonLeader).infiltrateGalactica();
        assertDoesNotThrow(() -> execute(new DrawSkillCardsEvent(2, selection)));
    }

    public static Stream<Arguments> infiltratingCylonLeaderSelection() {
        return Stream.of(
                arguments(CAPRICA_SIX, Map.of(LEADERSHIP, 1, TREACHERY, 1, ENGINEERING, 1)),
                arguments(CAPRICA_SIX, Map.of(LEADERSHIP, 2, ENGINEERING, 1)),
                arguments(CAPRICA_SIX, Map.of(LEADERSHIP, 1, TREACHERY, 2)),

                arguments(DANNA_BIERS, Map.of(LEADERSHIP, 1, TREACHERY, 1, ENGINEERING, 1)),
                arguments(DANNA_BIERS, Map.of(POLITICS, 1, TREACHERY, 1, ENGINEERING, 1)),
                arguments(DANNA_BIERS, Map.of(LEADERSHIP, 1, POLITICS, 1, ENGINEERING, 1)),
                arguments(DANNA_BIERS, Map.of(LEADERSHIP, 1, POLITICS, 1, TREACHERY, 1))
        );
    }

    @Test
    void shouldValidateDrawExactly2ConstraintWhenTotalIs2() {
        revealCylon();
        assertDoesNotThrow(() -> execute(new DrawSkillCardsEvent(1, Map.of(LEADERSHIP, 1, TREACHERY, 1), DRAW_EXACTLY_2)));
    }

    @Test
    void shouldFailDrawExactly2ConstraintWhenTotalIsNot2() {
        assertInvalid(new DrawSkillCardsEvent(1, Map.of(LEADERSHIP, 1), DRAW_EXACTLY_2));
    }

    @Test
    void shouldDrawOnly1CardWhenInSickbay() {
        moveTo(SICKBAY, KARA_STARBUCK_THRACE);
        game.step(RECEIVE_SKILLS);

        execute(new DrawSkillCardsEvent(1, Map.of(TACTICS, 1)));

        assertInvalid(new DrawSkillCardsEvent(1, Map.of(TACTICS, 2)));
    }

    @Test
    void shouldDrawOnly1CardWhenInResurrectionShip() {
        moveTo(RESURRECTION_SHIP, KARA_STARBUCK_THRACE);
        game.step(RECEIVE_SKILLS);

        execute(new DrawSkillCardsEvent(1, Map.of(TACTICS, 1)));

        assertInvalid(new DrawSkillCardsEvent(1, Map.of(TACTICS, 2)));
    }

    @Test
    void shouldNotDrawWhenInHubDestroyed() {
        galacticaBoard.destroyResurrectionShip();
        moveTo(HUB_DESTROYED, KARA_STARBUCK_THRACE);
        game.step(RECEIVE_SKILLS);

        execute(new DrawSkillCardsEvent(1, Map.of()));

        assertInvalid(new DrawSkillCardsEvent(1, Map.of(TACTICS, 1)));
    }

    @Test
    void shouldNotApplyReceiveSkillGameStepRestrictionOnOtherSteps() {
        assertDoesNotThrow(() -> execute(new DrawSkillCardsEvent(1, Map.of(TACTICS, 2))));

    }

    private void revealCylon() {
        player(1).loyaltyCards()
                .addOnTop(CYLON_SEND_TO_BRIG)
                .reveal(CYLON_SEND_TO_BRIG);
    }
}