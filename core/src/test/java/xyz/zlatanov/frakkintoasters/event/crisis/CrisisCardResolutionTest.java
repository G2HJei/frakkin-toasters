package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.PlaceCivilianShipEvent;
import xyz.zlatanov.frakkintoasters.event.player.SelectCharacterEvent;
import xyz.zlatanov.frakkintoasters.event.player.ChooseCharacterForBrigEvent;
import xyz.zlatanov.frakkintoasters.event.player.ChooseCharacterForBrigEventProcessor;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.damage.GalacticaDamage;
import xyz.zlatanov.frakkintoasters.state.ship.CivilianShip;
import xyz.zlatanov.frakkintoasters.state.ship.Raptor;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.card.LoyaltyCard.NOT_CYLON;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.track.JumpPreparation.START;

/**
 * Resolves Crisis Cards from {@link ResolveCrisisCardEvent} to the last followup through the real processors, the way
 * the engine will, so a section can't end in an event that has no processor. The top of a skill check crisis only has to
 * start the skill check, which {@code LocationSkillCheckTest} and the skill check tests run.
 */
class CrisisCardResolutionTest extends TestHarness {

    private static final int CURRENT   = 1;
    private static final int PRESIDENT = 2;
    private static final int ADMIRAL   = 3;
    private static final int CAG       = 4;

    private CrisisCard card;

    @BeforeEach
    void setUp() {
        setUpGame(4);
        player(CURRENT).character(FELIX_GAETA);
        player(PRESIDENT).character(LAURA_ROSLIN);
        player(ADMIRAL).character(WILLIAM_ADAMA);
        player(CAG).character(LEE_APOLLO_ADAMA);
        moveTo(COMMAND, FELIX_GAETA);
        moveTo(PRESIDENTS_OFFICE, LAURA_ROSLIN);
        moveTo(ADMIRALS_QUARTERS, WILLIAM_ADAMA);
        moveTo(HANGAR_DECK, LEE_APOLLO_ADAMA);
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        cag(LEE_APOLLO_ADAMA);
        loyaltyNotCylonDeck.nextCard(NOT_CYLON); // for an execution
    }

    static Stream<CrisisCard> allCards() {
        return Arrays.stream(CrisisCard.values());
    }

    static Stream<CrisisCard> cardsWithAChooser() {
        return Arrays.stream(CrisisCard.values()).filter(card -> card.chooser() != null);
    }

    @ParameterizedTest
    @MethodSource("allCards")
    void shouldResolveTheTopSectionOfEveryCard(CrisisCard card) {
        resolve(card, TOP);
    }

    @ParameterizedTest
    @MethodSource("cardsWithAChooser")
    void shouldResolveTheBottomSectionOfEveryCardWithAChooser(CrisisCard card) {
        resolve(card, BOTTOM);
    }

    @Test
    void shouldStartTheSkillCheckOfACardWithoutAlternative() {
        resolve(WITCH_HUNT, TOP);
        assertEquals(WITCH_HUNT.skillCheck(), game.activeSkillCheck().type());
    }

    @Test
    void shouldLetTheCurrentPlayerChooseBetweenTheSkillCheckAndTheAlternative() {
        val choice = (Followup.OneOf) execute(new ResolveCrisisCardEvent(WATER_SABOTAGED));
        assertEquals(Followup.one(new ChooseCrisisSectionEvent(CURRENT, WATER_SABOTAGED, TOP),
                new ChooseCrisisSectionEvent(CURRENT, WATER_SABOTAGED, BOTTOM)), choice);
        assertNull(game.activeSkillCheck());
    }

    @Test
    void shouldStartTheSkillCheckOnTheTopOfACardWithAlternative() {
        resolve(WATER_SABOTAGED, TOP);
        assertEquals(WATER_SABOTAGED.skillCheck(), game.activeSkillCheck().type());
        assertEquals(8, galacticaBoard.food());
    }

    @Test
    void shouldCarryOutTheAlternativeInstead() {
        resolve(WATER_SABOTAGED, BOTTOM);
        assertEquals(7, galacticaBoard.food());
        assertNull(game.activeSkillCheck());
    }

    @Test
    void shouldSendTheTurnBackToTheCrisisStep() {
        draw(CURRENT, 2);
        resolve(FULFILLER_OF_PROPHECY, BOTTOM);
        assertEquals(1, player(CURRENT).skillCards().size());
        assertTrue(game.turn().returnsToCrisisStep());
    }

    @Test
    void shouldLoseTheResourceOnALowRoll() {
        nextRoll(6);
        resolve(GALACTICA_FALLING_APART, BOTTOM);
        assertEquals(7, galacticaBoard.food());
    }

    @Test
    void shouldLoseNothingOnAHighRoll() {
        nextRoll(7);
        resolve(GALACTICA_FALLING_APART, BOTTOM);
        assertEquals(8, galacticaBoard.food());
    }

    @Test
    void shouldRollForEachSectionOfStarvationInDogsville() {
        nextRoll(4);
        resolve(STARVATION_IN_DOGSVILLE, TOP);
        assertEquals(List.of(7, 11), List.of(galacticaBoard.food(), galacticaBoard.population()));
        nextRoll(4);
        resolve(STARVATION_IN_DOGSVILLE, BOTTOM);
        assertEquals(5, galacticaBoard.food());
    }

    @Test
    void shouldLoseTheFoodBeforeTheRoll() {
        nextRoll(8);
        resolve(REQUISITION_FOR_DEMETRIUS, TOP);
        assertEquals(7, galacticaBoard.food());
    }

    @Test
    void shouldReturnTheVipersAndAdvanceThePursuit() {
        val unmanned = viperAt(GALACTICA_SPACE_4_OCLOCK);
        val piloted = viperAt(GALACTICA_SPACE_6_OCLOCK);
        ops.player(CAG).pilot(piloted.id());
        resolve(DETENTE, TOP);
        assertTrue(galacticaBoard.reserves().contains(unmanned));
        assertTrue(galacticaBoard.reserves().contains(piloted));
        assertEquals(HANGAR_DECK, locate(LEE_APOLLO_ADAMA));
        assertEquals(xyz.zlatanov.frakkintoasters.state.track.Pursuit.ONE_CIVILIAN_SHIP, cylonFleetBoard.pursuitTrack());
    }

    @Test
    void shouldPlaceShipsInFrontOfAndBehindGalactica() {
        resolve(TRACKED_BY_RADIATION, TOP);
        assertShipCount(GALACTICA_SPACE_8_OCLOCK, xyz.zlatanov.frakkintoasters.state.ship.Basestar.class, 1);
        assertShipCount(GALACTICA_SPACE_8_OCLOCK, xyz.zlatanov.frakkintoasters.state.ship.Raider.class, 3);
        assertShipCount(GALACTICA_SPACE_2_OCLOCK, CivilianShip.class, 2);
    }

    @Test
    void shouldDamageTheVipersInTheReserves() {
        val before = galacticaBoard.damagedShips().size();
        resolve(TRAINING_A_ROOKIE, BOTTOM);
        assertEquals(before + 2, galacticaBoard.damagedShips().size());
    }

    @Test
    void shouldDiscardANukeToken() {
        resolve(ABANDON_GALACTICA, TOP);
        assertEquals(1, game.nukes());
    }

    @Test
    void shouldOnlyOfferTheBottomWithoutNukeTokens() {
        game.discardNuke();
        game.discardNuke();
        assertEquals(Followup.single(new ChooseCrisisSectionEvent(ADMIRAL, ABANDON_GALACTICA, BOTTOM)),
                new CrisisCardFollowups(game).choice(ABANDON_GALACTICA));
    }

    @Test
    void shouldExecuteTheCurrentPlayer() {
        resolve(A_GUILTY_VERDICT, TOP);
        assertEquals(9, galacticaBoard.morale());
        assertEquals(SAUL_TIGH, player(CURRENT).character());
        assertTrue(game.removedComponents().contains(FELIX_GAETA));
        assertTrue(game.turn().endedEarly());
    }

    @Test
    void shouldGiveTheAdmiralThePresidentTitle() {
        resolve(DECLARE_MARTIAL_LAW, TOP);
        assertEquals(WILLIAM_ADAMA, president());
        assertEquals(9, galacticaBoard.morale());
    }

    @Test
    void shouldChooseAnotherPlayerForTheMiracleToken() {
        player(CAG).exhaustMiracleToken(); // everybody else still has one
        resolve(AN_AMBITIOUS_OPERATION, TOP);
        assertEquals(7, galacticaBoard.fuel());
        assertTrue(player(CAG).hasMiracleToken());
    }

    @Test
    void shouldKeepACardInPlayOutOfTheDiscardPile() {
        ops.playCrisisCard(DETECTOR_SABOTAGE);
        ops.keepCrisisCardInPlay(DETECTOR_SABOTAGE.skillCheck());
        ops.playCrisisCard(ADMIRAL_GRILLED);

        ops.discardCrisisCards();

        assertEquals(List.of(ADMIRAL_GRILLED), crisisDeck.discardedCards());
        assertEquals(List.of(), game.turn().crisisCards());
    }

    private void draw(int playerNumber, int count) {
        ops.player(playerNumber).drawSkillCards(Collections.<SkillCardColor>nCopies(count, POLITICS));
    }

    private long raptorsInReserves() {
        return galacticaBoard.reserves().stream().filter(Raptor.class::isInstance).count();
    }

    @Test
    void shouldLoseAndGainResources() {
        resolve(RESCUE_CAPRICA_SURVIVORS, TOP);
        assertEquals(7, galacticaBoard.fuel());
        assertEquals(7, galacticaBoard.food());
        assertEquals(13, galacticaBoard.population());
    }

    @Test
    void shouldDamageGalacticaTwice() {
        galacticaDamage.nextCard(GalacticaDamage.ARMORY, GalacticaDamage.WEAPONS_CONTROL);
        resolve(CONTROVERSIAL_MANUSCRIPT, BOTTOM);
        assertEquals(11, galacticaBoard.morale());
        assertTrue(galacticaBoard.damagedLocations().containsAll(List.of(ARMORY, WEAPONS_CONTROL)));
    }

    @Test
    void shouldMoveTheFleetTokenBack() {
        galacticaBoard.advanceJumpPreparation();
        resolve(LABOR_DISPUTE, BOTTOM);
        assertEquals(7, galacticaBoard.fuel());
        assertEquals(START, galacticaBoard.jumpPreparation());
    }

    @Test
    void shouldDestroyARaptorAndSendTheCurrentPlayerToSickbay() {
        resolve(HIDDEN_EXPLOSIVES, TOP);
        assertEquals(3, raptorsInReserves());
        assertEquals(SICKBAY, locate(FELIX_GAETA));
    }

    @Test
    void shouldLandTheCurrentPlayerWhenSentToSickbay() {
        val viper = viperAt(GALACTICA_SPACE_2_OCLOCK);
        ops.player(CURRENT).pilot(viper.id());
        resolve(RESCUE_MISSION_RAIDER, TOP);
        assertEquals(9, galacticaBoard.morale());
        assertEquals(SICKBAY, locate(FELIX_GAETA));
        assertTrue(galacticaBoard.reserves().contains(viper));
    }

    @Test
    void shouldDiscardTheWholeHandOfThePresident() {
        draw(PRESIDENT, 4);
        resolve(AMBUSHED_BY_THE_PRESS, BOTTOM);
        assertNoSkillCards(PRESIDENT);
    }

    @Test
    void shouldDiscardFromThePresidentThenTheCurrentPlayer() {
        draw(PRESIDENT, 3);
        draw(CURRENT, 4);
        resolve(FOOD_SHORTAGE, BOTTOM);
        assertEquals(7, galacticaBoard.food());
        assertEquals(1, player(PRESIDENT).skillCards().size());
        assertEquals(1, player(CURRENT).skillCards().size());
    }

    @Test
    void shouldDiscard5WhenThePresidentIsTheCurrentPlayer() {
        president(FELIX_GAETA);
        draw(CURRENT, 6);
        resolve(WATER_SHORTAGE_RAIDERS, BOTTOM);
        assertEquals(1, player(CURRENT).skillCards().size());
    }

    @Test
    void shouldDiscard6WhenTheCagIsTheAdmiral() {
        cag(WILLIAM_ADAMA);
        draw(ADMIRAL, 7);
        resolve(RAIDERS_INBOUND, BOTTOM);
        assertEquals(1, player(ADMIRAL).skillCards().size());
    }

    @Test
    void shouldAllowTheSectionWhenItCantBeFulfilled() {
        draw(PRESIDENT, 1);
        resolve(WATER_SHORTAGE_RAIDERS, BOTTOM);
        assertNoSkillCards(PRESIDENT);
        assertNoSkillCards(CURRENT);
    }

    @Test
    void shouldLeaveARevealedCylonWhereTheyAre() {
        moveTo(CAPRICA, FELIX_GAETA);
        draw(CURRENT, 3);
        revealCylon();
        resolve(HIDDEN_EXPLOSIVES, TOP);
        resolve(WATER_SHORTAGE_RAIDERS, BOTTOM);
        assertEquals(3, raptorsInReserves());
        assertEquals(CAPRICA, locate(FELIX_GAETA));
        assertEquals(3, player(CURRENT).skillCards().size());
    }

    private void resolve(CrisisCard card, Section section) {
        this.card = card;
        ops.playCrisisCard(card); // as the Crisis step does, so the card is the one being resolved
        val followup = execute(new ResolveCrisisCardEvent(card));
        if (card.chooser() == null) {
            assertEquals(card.skillCheck(), game.activeSkillCheck().type()); // a skill check and nothing else
            return;
        }
        val option = ((Followup.OneOf) followup).options().stream()
                .filter(o -> ((Followup.Single) o).event() instanceof ChooseCrisisSectionEvent e && e.section() == section)
                .findFirst()
                .orElseThrow();
        if (section == TOP && card.skillCheck() != null) {
            execute(((Followup.Single) option).event());
            assertEquals(card.skillCheck(), game.activeSkillCheck().type());
            return;
        }
        run(option);
    }

    private void run(Followup followup) {
        switch (followup) {
            case Followup.None ignored -> {
            }
            case Followup.Single single -> run(single.event() instanceof PlayerDecisionEvent<?> decision
                    ? execute(answer(decision))
                    : execute(single.event()));
            case Followup.AllOf all -> all.followups().forEach(this::run);
            // The optional ones ("may") have a NoOpEvent to decline with.
            case Followup.OneOf oneOf -> run(oneOf.options().stream()
                    .filter(o -> ((Followup.Single) o).event() instanceof NoOpEvent)
                    .findFirst()
                    .orElse(oneOf.options().iterator().next()));
        }
    }

    private Event answer(PlayerDecisionEvent<?> decision) {
        val player = decision.playerNumber();
        val action = decision.action();
        if (action == DiscardSkillCardsEvent.class) {
            return discardFirstCards(decision);
        } else if (action == PlaceCivilianShipEvent.class) {
            val free = xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA_SPACE.locations().stream()
                    .filter(location -> galacticaBoard.shipsIn(location, CivilianShip.class).isEmpty())
                    .findFirst()
                    .orElseThrow();
            return new PlaceCivilianShipEvent(player, free);
        } else if (action == ChooseCrisisTargetEvent.class) {
            return new ChooseCrisisTargetEvent(player, card, ChooseCrisisTargetEventProcessor.legalTargets(game, card).getLast()); // the last: not the current player, who would then hold 2 Mutiny Cards
        } else if (action == ChooseCharacterForBrigEvent.class) {
            return new ChooseCharacterForBrigEvent(player, game.players().stream()
                    .filter(p -> ChooseCharacterForBrigEventProcessor.canBeChosen(game, p.number()))
                    .findFirst()
                    .orElseThrow()
                    .number());
        } else if (action == SelectCharacterEvent.class) {
            return new SelectCharacterEvent(player, SAUL_TIGH); // after an execution
        }
        throw new AssertionError("A section asks for " + decision);
    }

    // A player with fewer cards than the limit discards all of them.
    private Event discardFirstCards(PlayerDecisionEvent<?> decision) {
        val limit = decision.eventConstraints().getFirst();
        val count = Integer.parseInt(limit.name().substring(limit.name().lastIndexOf('_') + 1));
        val hand = player(decision.playerNumber()).skillCards().cards();
        val cards = hand.subList(0, Math.min(count, hand.size()));
        return new DiscardSkillCardsEvent(decision.playerNumber(), cards, limit);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    private <E extends Event> Followup execute(E event) {
        val processorClass = Class.forName(event.getClass().getName() + "Processor");
        val processor = (EventProcessor<E>) processorClass.getDeclaredConstructor().newInstance();
        return processor.prepare(game, event).run(ops);
    }
}
