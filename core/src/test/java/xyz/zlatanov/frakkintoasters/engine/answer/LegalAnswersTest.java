package xyz.zlatanov.frakkintoasters.engine.answer;

import lombok.val;
import org.junit.jupiter.api.Test;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.engine.EventCatalog;
import xyz.zlatanov.frakkintoasters.engine.ProcessorRegistry;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.AnytimeEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.PlayerEvent;
import xyz.zlatanov.frakkintoasters.event.action.character.ReorderTopQuorumCardsEvent;
import xyz.zlatanov.frakkintoasters.event.action.mutiny.SetTheAgendaChoiceEvent;
import xyz.zlatanov.frakkintoasters.event.action.quorum.AuthorizationOfBrutalForceEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.interrupt.PlayInterruptCardEvent;
import xyz.zlatanov.frakkintoasters.event.location.galactica.ResearchLabEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.RevealMotiveCardEvent;
import xyz.zlatanov.frakkintoasters.event.loyalty.RevealPersonalGoalEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.sandbox.SandboxEvent;
import xyz.zlatanov.frakkintoasters.event.skillcheck.PlayCardsIntoSkillCheckEvent;
import xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard;
import xyz.zlatanov.frakkintoasters.state.card.MotiveCard;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCard;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardType.*;

class LegalAnswersTest extends TestHarness {

    private final SkillCard investigation = new SkillCard(3, INVESTIGATIVE_COMMITTEE);
    private final SkillCard planning      = new SkillCard(4, STRATEGIC_PLANNING);
    private final SkillCard repair        = new SkillCard(5, REPAIR);
    private final SkillCard maneuvers     = new SkillCard(2, EVASIVE_MANOEUVRES);

    @Test
    void aMoveOnItsShipNeedsNoDiscardAndAMoveToAnotherShipPicksOneCardOfTheHand() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(ADMIRALS_QUARTERS, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(investigation, planning);

        val answers = answers(1, MoveEvent.class);

        assertTrue(answers.events().contains(new MoveEvent(1, RESEARCH_LAB, null)));
        assertFalse(answers.events().contains(new MoveEvent(1, ADMIRALS_QUARTERS, null)), "his own location");
        assertFalse(answers.events().contains(new MoveEvent(1, PRESIDENTS_OFFICE, null)), "Colonial One costs a card");
        val toColonialOne = shapeOf(answers, MoveEvent.class, event -> ((MoveEvent) event).destination() == PRESIDENTS_OFFICE);
        assertEquals(new Pick.SkillCardPick("discardCard", List.of(investigation, planning), 1, 1, false, 0), toColonialOne.pick());
        assertTrue(answers.covers(new MoveEvent(1, PRESIDENTS_OFFICE, planning)));
        assertFalse(answers.covers(new MoveEvent(1, PRESIDENTS_OFFICE, repair)), "not in his hand");
    }

    @Test
    void aMoveFromTheBrigHasNoAnswer() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(BRIG, KARA_STARBUCK_THRACE);

        assertTrue(answers(1, MoveEvent.class).isEmpty());
    }

    @Test
    void theActionOfTheLocationListsItsChoices() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(RESEARCH_LAB, KARA_STARBUCK_THRACE);

        val answers = answers(1, ActionEvent.class);

        val locationActions = answers.events().stream().filter(LocationEvent.class::isInstance).toList();
        assertEquals(List.of(new ResearchLabEvent(1, SkillCardColor.ENGINEERING), new ResearchLabEvent(1, SkillCardColor.TACTICS)),
                sorted(locationActions));
        assertFalse(answers.covers(new ResearchLabEvent(1, SkillCardColor.POLITICS)));
    }

    @Test
    void aDrawOfAnyColorListsEveryColorWithCardsLeft() {
        player(1).character(KARA_STARBUCK_THRACE);
        treacheryDeck.clear();

        val answers = answers(1, DrawSkillCardsEvent.class, DRAW_EXACTLY_1_OF_ANY_COLOR);

        val colors = answers.events().stream()
                .map(event -> ((DrawSkillCardsEvent) event).selection().keySet().iterator().next())
                .collect(java.util.stream.Collectors.toCollection(TreeSet::new));
        assertEquals(new TreeSet<>(List.of(SkillCardColor.POLITICS, SkillCardColor.LEADERSHIP, SkillCardColor.TACTICS,
                SkillCardColor.PILOTING, SkillCardColor.ENGINEERING)), colors);
        assertTrue(answers.shapes().isEmpty());
        assertFalse(answers.covers(draw(SkillCardColor.TREACHERY, DRAW_EXACTLY_1_OF_ANY_COLOR)), "an empty deck");
        // a color at 0 draws nothing, as a color left out does
        val withAZero = new EnumMap<SkillCardColor, Integer>(SkillCardColor.class);
        withAZero.put(SkillCardColor.TACTICS, 1);
        withAZero.put(SkillCardColor.ENGINEERING, 0);
        assertTrue(answers.covers(new DrawSkillCardsEvent(1, withAZero, DRAW_EXACTLY_1_OF_ANY_COLOR)));
    }

    @Test
    void aDiscardOfExactlyTwoPicksTwoCardsOfTheHand() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(investigation, planning, repair, maneuvers);

        val answers = answers(1, DiscardSkillCardsEvent.class, DISCARD_EXACTLY_2);

        assertEquals(List.of(), answers.events());
        assertEquals(1, answers.shapes().size());
        assertEquals(new Pick.SkillCardPick("cards", List.of(investigation, planning, repair, maneuvers), 2, 2, false, 0),
                answers.shapes().getFirst().pick());
        assertTrue(answers.covers(new DiscardSkillCardsEvent(1, List.of(maneuvers, investigation), DISCARD_EXACTLY_2)));
        assertFalse(answers.covers(new DiscardSkillCardsEvent(1, List.of(maneuvers), DISCARD_EXACTLY_2)));
    }

    @Test
    void aDiscardDownToTheHandLimitPicksTheCardsOverIt() {
        player(1).character(KARA_STARBUCK_THRACE);
        for (int i = 0; i < 12; i++) {
            player(1).gainSkillCards(new SkillCard(i % 5 + 1, REPAIR));
        }

        val answers = answers(1, DiscardSkillCardsEvent.class, DISCARD_DOWN_TO_HAND_LIMIT);

        val pick = answers.shapes().getFirst().pick();
        assertEquals(List.of(2, 2), List.of(pick.min(), pick.max()));
    }

    // "Command Authority" can leave a hand of more than 30 cards (F-5945fa).
    @Test
    void aDiscardOfMoreThanTwentyCardsIsAPickToo() {
        player(1).character(KARA_STARBUCK_THRACE);
        for (int i = 0; i < 34; i++) {
            player(1).gainSkillCards(new SkillCard(i % 5 + 1, REPAIR));
        }

        val pick = answers(1, DiscardSkillCardsEvent.class, DISCARD_DOWN_TO_HAND_LIMIT).shapes().getFirst().pick();

        assertEquals(List.of(24, 24), List.of(pick.min(), pick.max()));
    }

    @Test
    void cardsIntoACheckAreNoneOrAnyNumberOfTheHand() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(RESEARCH_LAB, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(investigation, planning, repair);
        ops.skillCheck().start(SkillCheckType.WATER_SABOTAGED);

        val answers = answers(1, PlayCardsIntoSkillCheckEvent.class);

        assertEquals(List.of(new PlayCardsIntoSkillCheckEvent(1, List.of())), answers.events());
        val pick = answers.shapes().getFirst().pick();
        assertEquals(List.of(1, 3), List.of(pick.min(), pick.max()));
    }

    @Test
    void aCharacterInTheBrigPutsOneCardIntoACheck() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(BRIG, KARA_STARBUCK_THRACE);
        player(1).gainSkillCards(investigation, planning, repair);
        ops.skillCheck().start(SkillCheckType.WATER_SABOTAGED);

        val pick = answers(1, PlayCardsIntoSkillCheckEvent.class).shapes().getFirst().pick();

        assertEquals(List.of(1, 1), List.of(pick.min(), pick.max()));
    }

    // "Impulsive": none, or 2 and more.
    @Test
    void anImpulsiveCharacterPutsNoneOrTwoAndMoreIntoACheck() {
        player(1).character(CALLANDRA_CALLY_TYROL);
        moveTo(RESEARCH_LAB, CALLANDRA_CALLY_TYROL);
        player(1).gainSkillCards(investigation, planning, repair);
        ops.skillCheck().start(SkillCheckType.WATER_SABOTAGED);

        val answers = answers(1, PlayCardsIntoSkillCheckEvent.class);

        assertEquals(List.of(new PlayCardsIntoSkillCheckEvent(1, List.of())), answers.events());
        assertEquals(1, answers.shapes().size());
        val pick = answers.shapes().getFirst().pick();
        assertEquals(List.of(2, 3), List.of(pick.min(), pick.max()));
        assertFalse(answers.covers(new PlayCardsIntoSkillCheckEvent(1, List.of(planning))));
    }

    @Test
    void theCharacterChoiceListsTheCharactersLeft() {
        player(1).character(KARA_STARBUCK_THRACE);

        val answers = answers(2, SelectCharacterEvent.class);

        assertTrue(answers.events().contains(new SelectCharacterEvent(2, WILLIAM_ADAMA)));
        assertFalse(answers.covers(new SelectCharacterEvent(2, KARA_STARBUCK_THRACE)), "taken");
        assertFalse(answers.covers(new SelectCharacterEvent(2, null)));
    }

    @Test
    void setTheAgendaPutsTwoOfThePresidentsCardsInOrder() {
        player(1).character(LAURA_ROSLIN);
        player(2).character(KARA_STARBUCK_THRACE);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(QuorumCard.INSPIRATIONAL_SPEECH, QuorumCard.ENCOURAGE_MUTINY, QuorumCard.AUTHORIZATION_OF_BRUTAL_FORCE);

        val answers = answers(2, SetTheAgendaChoiceEvent.class);

        val pick = (Pick.QuorumCardPick) answers.shapes().getFirst().pick();
        assertEquals(List.of(2, 2), List.of(pick.min(), pick.max()));
        assertTrue(pick.ordered());
        assertEquals(3, pick.cards().size());
    }

    @Test
    void influentialPutsTheTopFiveQuorumCardsInOrder() {
        player(1).character(KARA_STARBUCK_THRACE);

        val answers = answers(1, ReorderTopQuorumCardsEvent.class);

        val pick = (Pick.QuorumCardPick) answers.shapes().getFirst().pick();
        assertEquals(List.of(5, 5), List.of(pick.min(), pick.max()));
        assertTrue(pick.ordered());
    }

    // "Selfish": discards of 20 strength, tried strongest first.
    @Test
    void selfishPicksCardsOfTwentyStrength() {
        player(1).character(KARA_STARBUCK_THRACE);
        player(1).loyaltyCards().addOnTop(LoyaltyCard.NOT_CYLON_SELFISH);
        player(1).gainSkillCards(new SkillCard(3, REPAIR), new SkillCard(6, REPAIR), new SkillCard(4, REPAIR),
                new SkillCard(6, EVASIVE_MANOEUVRES), new SkillCard(5, REPAIR));

        val answers = answers(1, RevealPersonalGoalEvent.class);

        val pick = (Pick.SkillCardPick) answers.shapes().getFirst().pick();
        assertEquals(List.of(4, 5, 20), List.of(pick.min(), pick.max(), pick.minStrength()));
        assertTrue(answers.covers(new RevealPersonalGoalEvent(1, LoyaltyCard.NOT_CYLON_SELFISH,
                List.of(new SkillCard(6, REPAIR), new SkillCard(6, EVASIVE_MANOEUVRES), new SkillCard(5, REPAIR), new SkillCard(4, REPAIR)))));
        assertFalse(answers.covers(new RevealPersonalGoalEvent(1, LoyaltyCard.NOT_CYLON_SELFISH,
                List.of(new SkillCard(3, REPAIR), new SkillCard(6, REPAIR), new SkillCard(4, REPAIR), new SkillCard(5, REPAIR)))), "18");
    }

    // 3 raiders from any area, or a heavy raider.
    @Test
    void authorizationOfBrutalForcePicksThreeRaidersOrAHeavyRaider() {
        player(1).character(LAURA_ROSLIN);
        president(LAURA_ROSLIN);
        presidentHand.addOnTop(QuorumCard.AUTHORIZATION_OF_BRUTAL_FORCE);
        val raiders = List.of(raiderAt(GALACTICA_SPACE_2_OCLOCK).id(), raiderAt(GALACTICA_SPACE_2_OCLOCK).id(),
                raiderAt(GALACTICA_SPACE_8_OCLOCK).id(), raiderAt(GALACTICA_SPACE_8_OCLOCK).id());
        val heavyRaider = heavyRaiderAt(GALACTICA_SPACE_10_OCLOCK).id();

        val answers = answers(1, AuthorizationOfBrutalForceEvent.class);

        val picks = answers.shapes().stream().map(ShapedAnswer::pick).toList();
        assertEquals(List.of(new Pick.ShipPick("shipIds", List.of(heavyRaider), 1, 1),
                new Pick.ShipPick("shipIds", raiders.stream().sorted().toList(), 3, 3)), sortedPicks(picks));
        assertFalse(answers.covers(new AuthorizationOfBrutalForceEvent(1, raiders.subList(0, 2))));
    }

    @Test
    void anOptionHasTheAnswersOfTheDecisionItAsksFirst() {
        player(1).character(KARA_STARBUCK_THRACE);
        moveTo(RESEARCH_LAB, KARA_STARBUCK_THRACE);
        val decision = new PlayerDecisionEvent<>(1, ActionEvent.class);

        assertEquals(LegalAnswers.of(game, decision), LegalAnswers.forOption(game, Followup.single(decision)));
        assertEquals(decision, LegalAnswers.decisionOf(Followup.all(decision, new NoOpEvent(1))));
        assertEquals(LegalAnswers.NONE, LegalAnswers.forOption(game, Followup.single(new NoOpEvent(1))));
    }

    @Test
    void aCylonLeaderMayRevealTheMotiveCardsWhoseRequirementIsMet() {
        player(1).character(CAVIL);
        player(1).motiveCards().addOnTop(MotiveCard.END_THE_CHASE, MotiveCard.SAVOR_THEIR_DEMISE);
        galacticaBoard.damage(FTL_CONTROL);

        val answers = answers(1, AnytimeEvent.class);

        assertEquals(answers, LegalAnswers.anytime(game, 1));
        assertEquals(List.of(new RevealMotiveCardEvent(1, MotiveCard.END_THE_CHASE)), reveals(answers));
        assertFalse(answers.covers(new RevealMotiveCardEvent(1, MotiveCard.SAVOR_THEIR_DEMISE)), "it waits for the end");
        assertTrue(reveals(answers(2, AnytimeEvent.class)).isEmpty(), "not his cards");
    }

    @Test
    void anAnytimeEventAnswersNoDecision() {
        assertFalse(LegalAnswers.answerTypes(PlayerEvent.class).contains(RevealMotiveCardEvent.class));
        assertTrue(LegalAnswers.answerTypes(AnytimeEvent.class).contains(RevealMotiveCardEvent.class));
    }

    // A record with a list that no source covers would never be offered: it has to be one that only a choice offers,
    // which lists the concrete events itself.
    @Test
    void everyListOfAnAnswerHasASource() {
        val main = GameEngineCodeSource.of(LegalAnswers.class);
        player(1).character(KARA_STARBUCK_THRACE);
        val asked = new Asked(game, new PlayerDecisionEvent<>(1, Event.class));
        val withoutSource = new TreeSet<String>();
        for (val type : EventCatalog.types()) {
            if (!type.isRecord() || !ProcessorRegistry.hasProcessor(type) || SandboxEvent.class.isAssignableFrom(type)
                    || type == PlayerDecisionEvent.class
                    || !main.equals(GameEngineCodeSource.of(type))) {
                continue;
            }
            val picked = Shapes.pickedComponent(type);
            if (picked == -2 || picked >= 0 && Shapes.of(asked, type, type.getRecordComponents()[picked]) == null) {
                withoutSource.add(type.getSimpleName());
            }
        }

        // the reveal power's pairs of damage tokens, and the tiebreak of an interrupt window: options of a choice
        assertEquals(new TreeSet<>(List.of("ChooseGalacticaDamageTokensEvent", "TiebreakInterruptCardsEvent")), withoutSource);
    }

    @Test
    void calculationsIsTriedWithOneMoreAndOneLess() {
        player(1).character(KARA_STARBUCK_THRACE);
        val modifier = java.util.Arrays.stream(PlayInterruptCardEvent.class.getRecordComponents())
                .filter(component -> component.getName().equals("calculationsModifier"))
                .findFirst()
                .orElseThrow();

        assertEquals(java.util.Arrays.asList(null, -1, 1), Domains.of(new Asked(game,
                new PlayerDecisionEvent<>(1, PlayInterruptCardEvent.class)), PlayInterruptCardEvent.class, modifier, null));
    }

    @Test
    void aComponentWithoutValuesHasNoDomain() {
        player(1).character(KARA_STARBUCK_THRACE);
        val name = Pick.SkillCardPick.class.getRecordComponents()[0];

        assertNull(Domains.of(new Asked(game, new PlayerDecisionEvent<>(1, Event.class)), Pick.SkillCardPick.class, name,
                null));
    }

    @Test
    void aTiebreakKeepsOneCardOfEachGroup() {
        val picks = Shapes.oneOfEach(List.of(List.of("a", "b"), List.of("c", "d", "e")));

        assertEquals(6, picks.size());
        assertTrue(picks.stream().allMatch(pick -> pick.size() == 2
                && List.of("a", "b").contains(pick.get(0)) && List.of("c", "d", "e").contains(pick.get(1))));
    }

    @Test
    void theWitnessesOfASizeAreTheFirstAndTheLastElements() {
        assertEquals(List.of(List.of(0, 1), List.of(2, 3)), AnswerSearch.witnesses(4, 2));
        assertEquals(List.of(List.of()), AnswerSearch.witnesses(4, 0));
        assertEquals(List.of(List.of(0, 1, 2)), AnswerSearch.witnesses(3, 3));
    }

    // The classes of the rules, not of the tests, which the catalog finds on the classpath too.
    private static final class GameEngineCodeSource {
        static String of(Class<?> type) {
            val source = type.getProtectionDomain().getCodeSource();
            return source == null ? "" : source.getLocation().toString();
        }
    }

    private LegalAnswers answers(int player, Class<? extends Event> action, EventConstraint... constraints) {
        val decision = new PlayerDecisionEvent<>(player, action, constraints);
        val problems = new ArrayList<String>();
        val answers = LegalAnswers.of(game, decision, new LegalAnswers.Listener() {
            @Override
            public void crashed(Class<?> type, Event candidate, RuntimeException thrown) {
                problems.add(type.getSimpleName() + " threw " + thrown + " on " + candidate);
            }

            @Override
            public void unsupported(Class<?> type, String component, String why) {
                problems.add(type.getSimpleName() + "." + component + ": " + why);
            }
        });
        assertEquals(List.of(), problems);
        // Sound: every answer, and every example of a shape, is one the game takes.
        answers.events().forEach(this::assertTaken);
        answers.shapes().forEach(shape -> {
            assertTaken(shape.example());
            assertTrue(answers.covers(shape.example()), "a shape covers its example");
        });
        return answers;
    }

    // The test fixtures add an anytime event of their own.
    private static List<Event> reveals(LegalAnswers answers) {
        return answers.events().stream().filter(RevealMotiveCardEvent.class::isInstance).toList();
    }

    private void assertTaken(Event event) {
        assertDoesNotThrow(() -> ProcessorRegistry.create(event).prepare(game, event), event.toString());
    }

    private static ShapedAnswer shapeOf(LegalAnswers answers, Class<?> type, java.util.function.Predicate<Event> which) {
        return answers.shapes().stream()
                .filter(shape -> type.isInstance(shape.example()) && which.test(shape.example()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("no shape in " + answers));
    }

    private static DrawSkillCardsEvent draw(SkillCardColor color, EventConstraint constraint) {
        Map<SkillCardColor, Integer> selection = new EnumMap<>(SkillCardColor.class);
        selection.put(color, 1);
        return new DrawSkillCardsEvent(1, selection, constraint);
    }

    private static List<Event> sorted(List<Event> events) {
        return events.stream().sorted(java.util.Comparator.comparing(Object::toString)).toList();
    }

    private static List<Pick> sortedPicks(List<Pick> picks) {
        return picks.stream().sorted(java.util.Comparator.comparing(pick -> pick.elements().size())).toList();
    }
}
