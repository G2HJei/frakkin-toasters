package xyz.zlatanov.frakkintoasters.event.player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.EventTestHarness;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_1;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_4;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DRAW_EXACTLY_5;
import static xyz.zlatanov.frakkintoasters.state.GameStep.MOVEMENT;
import static xyz.zlatanov.frakkintoasters.state.GameStep.RECEIVE_SKILLS;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.AT_ANY_COST;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.BAIT;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.LAUNCH_SCOUT;

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
    void shouldValidateDrawExactly1ConstraintWhenTotalIs1() {
        leadershipDeck.nextCard(leadershipCard);
        execute(new DrawSkillCardsEvent(1, Map.of(LEADERSHIP, 1), DRAW_EXACTLY_1));
        assertSkillCards(1, leadershipCard);
    }

    @Test
    void shouldValidateDrawExactly4Constraint() {
        execute(new DrawSkillCardsEvent(1, Map.of(TACTICS, 2, PILOTING, 2), DRAW_EXACTLY_4));
        assertEquals(4, player(1).skillCards().size());
    }

    @Test
    void shouldFailDrawExactly4ConstraintWhenTotalIsNot4() {
        assertInvalid(new DrawSkillCardsEvent(1, Map.of(TACTICS, 2, PILOTING, 1), DRAW_EXACTLY_4));
    }

    @Test
    void shouldFailDrawExactly1ConstraintWhenTotalIsNot1() {
        assertInvalid(new DrawSkillCardsEvent(1, Map.of(LEADERSHIP, 1, TACTICS, 1), DRAW_EXACTLY_1));
    }

    @ParameterizedTest
    @MethodSource("hazardousLocationLimits")
    void shouldDrawLessCardsWhenInHazardousLocation(Location location, int limit) {
        moveTo(location, KARA_STARBUCK_THRACE);
        game.turn().step(RECEIVE_SKILLS);

        execute(new DrawSkillCardsEvent(1, Map.of(TACTICS, limit)));
    }

    public static Stream<Arguments> hazardousLocationLimits() {
        return Stream.of(
                arguments(SICKBAY, 1),
                arguments(RESURRECTION_SHIP, 1)
        );
    }

    @ParameterizedTest
    @MethodSource("hazardousLocationLimits")
    void shouldNotDrawMoreThanHazardousLocationLimit(Location location, int limit) {
        moveTo(location, KARA_STARBUCK_THRACE);
        game.turn().step(RECEIVE_SKILLS);

        assertInvalid(new DrawSkillCardsEvent(1, Map.of(TACTICS, ++limit)));
    }

    @Test
    void shouldNotApplyHazardousLocationLimitToCharacterNotOnTheBoard() {
        player(2).character(KARL_HELO_AGATHON);
        game.turn().step(RECEIVE_SKILLS);

        assertDoesNotThrow(() -> execute(new DrawSkillCardsEvent(2, Map.of(LEADERSHIP, 2))));
    }

    @Test
    void shouldNotApplyReceiveSkillGameStepRestrictionOnOtherSteps() {
        assertDoesNotThrow(() -> execute(new DrawSkillCardsEvent(1, Map.of(TACTICS, 2))));

    }

    // Options can share a color: "2xL, 2xTa, 1xTa/L".
    @Test
    void shouldFillOptionsThatShareAColor() {
        player(2).character(HELENA_CAIN);

        execute(new DrawSkillCardsEvent(2, Map.of(LEADERSHIP, 3, TACTICS, 2), DRAW_EXACTLY_5));

        assertEquals(5, player(2).skillCards().size());
    }

    @Test
    void shouldNotOverfillOptionsThatShareAColor() {
        player(2).character(HELENA_CAIN);
        assertInvalid(new DrawSkillCardsEvent(2, Map.of(LEADERSHIP, 4, TACTICS, 1), DRAW_EXACTLY_5));
    }

    // "Players will be unable to draw cards of that type until some of that type of cards have been discarded."
    @Test
    void shouldDrawFewerWhenADeckAndItsDiscardPileAreEmpty() {
        tacticsDeck.clear();

        execute(new DrawSkillCardsEvent(1, Map.of(PILOTING, 2, LEADERSHIP, 1), DRAW_EXACTLY_5));

        assertEquals(3, player(1).skillCards().size());
    }

    @Test
    void shouldNotDrawFromAnEmptyDeck() {
        tacticsDeck.clear();
        assertInvalid(new DrawSkillCardsEvent(1, Map.of(TACTICS, 1, PILOTING, 2, LEADERSHIP, 1), DRAW_EXACTLY_4));
    }

    @Test
    void shouldDrawFromTheDiscardPileOfAnEmptyDeck() {
        tacticsDeck.clear();
        game.decks().discard(new SkillCard(1, LAUNCH_SCOUT));

        execute(new DrawSkillCardsEvent(1, Map.of(TACTICS, 1), DRAW_EXACTLY_1));

        assertSkillCards(1, new SkillCard(1, LAUNCH_SCOUT));
    }

    @Test
    void shouldDrawTheOtherColorOfAnOptionWhenOneRunsOut() {
        leadershipDeck.clear();
        assertInvalid(new DrawSkillCardsEvent(1, Map.of(TACTICS, 2, PILOTING, 2), DRAW_EXACTLY_5));
    }

    // Cylon Leaders draw from their skill set (rule 6.2.1.1): "1xL, 1xTr/E".
    @Test
    void shouldNotLetACylonLeaderDrawOutsideHisSkillSet() {
        player(2).character(CAPRICA_SIX);
        assertInvalid(new DrawSkillCardsEvent(2, Map.of(POLITICS, 1, TACTICS, 1), DRAW_EXACTLY_2));
    }

    @Test
    void shouldGiveAnInfiltratingCylonLeaderNoExtraCardOutsideTheReceiveSkillsStep() {
        player(2).character(CAPRICA_SIX).infiltrateGalactica();
        game.turn().step(MOVEMENT);
        assertInvalid(new DrawSkillCardsEvent(2, Map.of(LEADERSHIP, 2), DRAW_EXACTLY_2));
    }

    @Test
    void shouldNotDrawANegativeCount() {
        assertInvalid(new DrawSkillCardsEvent(1, Map.of(TACTICS, 3, PILOTING, -1)));
    }
}
