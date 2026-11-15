package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import xyz.zlatanov.frakkintoasters.TestHarness;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.board.AdvancePursuitTrackEvent;
import xyz.zlatanov.frakkintoasters.event.board.DecreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.board.GainResourceEvent;
import xyz.zlatanov.frakkintoasters.event.board.LoseResourceEvent;
import xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardAllSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DiscardSkillCardsEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageGalacticaEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyRaptorEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DrawAndDestroyCivilianShipsEvent;
import xyz.zlatanov.frakkintoasters.event.ship.LaunchRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipsEvent;
import xyz.zlatanov.frakkintoasters.event.ship.ReturnVipersToReservesEvent;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.board.Resource;
import xyz.zlatanov.frakkintoasters.state.character.Character;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.CylonActivation;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_EXACTLY_2;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.DISCARD_EXACTLY_3;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.*;
import static xyz.zlatanov.frakkintoasters.state.character.Character.*;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard.*;

class CrisisCardFollowupsTest extends TestHarness {

    private static final int CURRENT   = 1;
    private static final int PRESIDENT = 2;
    private static final int ADMIRAL   = 3;
    private static final int CAG       = 4;

    @BeforeEach
    void setUp() {
        setUpPlayers(FELIX_GAETA);
    }

    private void setUpPlayers(Character currentPlayer) {
        setUpGame(4);
        player(CURRENT).character(currentPlayer);
        player(PRESIDENT).character(LAURA_ROSLIN);
        player(ADMIRAL).character(WILLIAM_ADAMA);
        player(CAG).character(LEE_APOLLO_ADAMA);
        president(LAURA_ROSLIN);
        admiral(WILLIAM_ADAMA);
        cag(LEE_APOLLO_ADAMA);
    }

    static Stream<CrisisCard> cardsWithAChooser() {
        return Arrays.stream(CrisisCard.values()).filter(card -> card.chooser() != null);
    }

    @ParameterizedTest
    @MethodSource("cardsWithAChooser")
    void shouldBuildTheBottomSectionOfEveryCardWithAChooser(CrisisCard card) {
        assertNotNull(followup(card, BOTTOM));
    }

    // The top of a skill check crisis is the skill check, which its section's processor starts.
    @ParameterizedTest
    @MethodSource("cardsWithAChooser")
    void shouldBuildTheTopSectionOfEveryEventCrisis(CrisisCard card) {
        if (card.skillCheck() == null) {
            assertNotNull(followup(card, TOP));
        } else {
            assertThrows(FrakCallTheAdmiralException.class, () -> followup(card, TOP));
        }
    }

    @Test
    void shouldNotOfferAChoiceOnASkillCheckCardWithoutAlternative() {
        assertThrows(FrakCallTheAdmiralException.class, () -> new CrisisCardFollowups(game).choice(WITCH_HUNT));
    }

    @Test
    void shouldLetTheCurrentPlayerChooseTheSkillCheckOrTheAlternative() {
        assertEquals(Followup.one(new ChooseCrisisSectionEvent(CURRENT, WATER_SABOTAGED, TOP),
                new ChooseCrisisSectionEvent(CURRENT, WATER_SABOTAGED, BOTTOM)), new CrisisCardFollowups(game).choice(WATER_SABOTAGED));
    }

    // "Discard 1 nuke token. If you do not have any nuke tokens, you cannot choose this option."
    @ParameterizedTest
    @EnumSource(value = CrisisCard.class, names = {"ABANDON_GALACTICA", "BUILD_CYLON_DETECTOR"})
    void shouldOnlyOfferTheTopWithANukeToken(CrisisCard card) {
        val followups = new CrisisCardFollowups(game);
        assertEquals(Followup.one(new ChooseCrisisSectionEvent(ADMIRAL, card, TOP),
                new ChooseCrisisSectionEvent(ADMIRAL, card, BOTTOM)), followups.choice(card));

        game.discardNuke();
        game.discardNuke();
        assertEquals(single(new ChooseCrisisSectionEvent(ADMIRAL, card, BOTTOM)), followups.choice(card));
    }

    @Test
    void shouldActivateTheShipsOfEachIcon() {
        assertEquals(new ActivateRaidersEvent(), CrisisCardFollowups.activation(CylonActivation.ACTIVATE_RAIDERS));
        assertEquals(new LaunchRaidersEvent(), CrisisCardFollowups.activation(CylonActivation.LAUNCH_RAIDERS));
        assertEquals(new ActivateHeavyRaidersAndCenturionsEvent(),
                CrisisCardFollowups.activation(CylonActivation.ACTIVATE_HEAVY_RAIDERS));
        assertEquals(new ActivateCylonBasestarsEvent(), CrisisCardFollowups.activation(CylonActivation.ACTIVATE_BASESTARS));
    }

    /* the Cylon Fleet crises */

    @Test
    void shouldResolveDetente() {
        assertEquals(all(new ReturnVipersToReservesEvent(), new AdvancePursuitTrackEvent()), followup(DETENTE, TOP));
        assertEquals(all(new ActivateCylonBasestarsEvent(), new ActivateRaidersEvent(),
                new ActivateHeavyRaidersAndCenturionsEvent()), followup(DETENTE, BOTTOM));
    }

    @Test
    void shouldResolveTrackedByRadiation() {
        assertEquals(single(new PlaceShipsEvent(1, 3, 2)), followup(TRACKED_BY_RADIATION, TOP));
        assertEquals(single(lose(FUEL, 1)), followup(TRACKED_BY_RADIATION, BOTTOM));
    }

    // "Any character on Galactica with piloting in his skill set may immediately launch himself in a viper": each pilot
    // may, then the raiders launch. Himself, so not an unmanned viper.
    @Test
    void shouldLetEveryPilotLaunchBeforeTheRaidersLaunch() {
        setUpPlayers(KARA_STARBUCK_THRACE);
        moveTo(Location.HANGAR_DECK, KARA_STARBUCK_THRACE);
        moveTo(Location.COMMAND, LEE_APOLLO_ADAMA);

        assertEquals(all(
                all(Followup.one(launchHimself(CURRENT), new NoOpEvent(CURRENT)),
                        Followup.one(launchHimself(CAG), new NoOpEvent(CAG))),
                single(new LaunchRaidersEvent())), followup(RETURN_TO_DUTY, TOP));
    }

    private static Event launchHimself(int playerNumber) {
        return new PlayerDecisionEvent<>(playerNumber, LaunchViperEvent.class, EventConstraint.MUST_LAUNCH_IN_VIPER);
    }

    /* alternate instructions and die rolls */

    @Test
    void shouldResolveTheAlternateInstructionsOnTheCurrentPlayer() {
        assertEquals(all(single(discard(PRESIDENT, DISCARD_EXACTLY_3)),
                single(new DrawMutinyCardEvent(CURRENT))), followup(A_DESPERATE_PACT, BOTTOM));
        assertEquals(single(new SendToSickbayEvent(CURRENT)), followup(RAPTOR_MALFUNCTION, BOTTOM));
        assertEquals(single(discard(CURRENT, EventConstraint.DISCARD_EXACTLY_5)), followup(A_TRAITOR_ACCUSED, BOTTOM));
    }

    @Test
    void shouldNotResolveAnAlternateInstructionOnARevealedCylon() {
        revealCylon();
        assertEquals(Followup.NONE, followup(RAPTOR_MALFUNCTION, BOTTOM));
        assertEquals(Followup.NONE, followup(A_TRAITOR_ACCUSED, BOTTOM));
    }

    @Test
    void shouldAskEachHumanToDiscardOnCylonScreenings() {
        assertEquals(all(discard(CURRENT, DISCARD_EXACTLY_2), discard(PRESIDENT, DISCARD_EXACTLY_2),
                discard(ADMIRAL, DISCARD_EXACTLY_2), discard(CAG, DISCARD_EXACTLY_2)), followup(CYLON_SCREENINGS, BOTTOM));
        revealCylon();
        assertEquals(all(discard(PRESIDENT, DISCARD_EXACTLY_2), discard(ADMIRAL, DISCARD_EXACTLY_2),
                discard(CAG, DISCARD_EXACTLY_2)), followup(CYLON_SCREENINGS, BOTTOM));
    }

    @Test
    void shouldCheckTheRollOfTheSection() {
        val followups = new CrisisCardFollowups(game);
        assertEquals(single(lose(FOOD, 1)), followups.rollResult(GALACTICA_FALLING_APART, BOTTOM, 6));
        assertEquals(Followup.NONE, followups.rollResult(GALACTICA_FALLING_APART, BOTTOM, 7));
        assertEquals(single(lose(FUEL, 1)), followups.rollResult(SEND_SURVEY_TEAM, BOTTOM, 5));
        assertEquals(Followup.NONE, followups.rollResult(SEND_SURVEY_TEAM, BOTTOM, 6));
        assertEquals(single(new DrawAndDestroyCivilianShipsEvent(1)), followups.rollResult(FOOD_HOARDING_IN_THE_FLEET, TOP, 3));
        assertEquals(Followup.NONE, followups.rollResult(FOOD_HOARDING_IN_THE_FLEET, TOP, 4));
    }

    @Test
    void shouldRollForTheSectionThatSaysSo() {
        assertEquals(rollDie(new ResolveCrisisRollEvent(STARVATION_IN_DOGSVILLE, TOP)), followup(STARVATION_IN_DOGSVILLE, TOP));
        assertEquals(rollDie(new ResolveCrisisRollEvent(STARVATION_IN_DOGSVILLE, BOTTOM)),
                followup(STARVATION_IN_DOGSVILLE, BOTTOM));
        assertEquals(all(single(lose(FOOD, 1)), rollDie(new ResolveCrisisRollEvent(REQUISITION_FOR_DEMETRIUS, TOP))),
                followup(REQUISITION_FOR_DEMETRIUS, TOP));
    }

    @Test
    void shouldAskTheChooserForTheTargetOfACard() {
        assertEquals(single(new PlayerDecisionEvent<>(PRESIDENT, ChooseCrisisTargetEvent.class)), followup(THE_CIRCLE, TOP));
    }

    // Nobody to choose, so nobody is asked: everybody still has a miracle token.
    @Test
    void shouldNotAskForTheMiracleTokenWhenEverybodyHasOne() {
        assertEquals(single(lose(FUEL, 1)), followup(AN_AMBITIOUS_OPERATION, TOP));
    }

    @Test
    void shouldAskForTheMiracleTokenWhenSomebodyLacksOne() {
        player(CAG).exhaustMiracleToken();
        assertEquals(all(single(lose(FUEL, 1)), single(new PlayerDecisionEvent<>(ADMIRAL, ChooseCrisisTargetEvent.class))),
                followup(AN_AMBITIOUS_OPERATION, TOP));
    }

    @Test
    void shouldSendTheCurrentPlayerBackToTheCrisisStep() {
        assertEquals(all(single(discard(CURRENT, EventConstraint.DISCARD_EXACTLY_1)), single(new ReturnToCrisisStepEvent())),
                followup(FULFILLER_OF_PROPHECY, BOTTOM));
    }

    /* resources */

    @Test
    void shouldResolveWidespreadStarvation() {
        assertEquals(single(lose(FOOD, 2)), followup(WIDESPREAD_STARVATION, TOP));
        assertEquals(all(lose(FOOD, 1), lose(POPULATION, 1)), followup(WIDESPREAD_STARVATION, BOTTOM));
    }

    @Test
    void shouldResolveRescueCapricaSurvivors() {
        assertEquals(all(lose(FUEL, 1), lose(FOOD, 1), new GainResourceEvent(POPULATION, 1)),
                followup(RESCUE_CAPRICA_SURVIVORS, TOP));
        assertEquals(single(lose(MORALE, 1)), followup(RESCUE_CAPRICA_SURVIVORS, BOTTOM));
    }

    @ParameterizedTest
    @EnumSource(value = CrisisCard.class, names = {"RIOTS_JUMP", "RIOTS_NO_JUMP"})
    void shouldResolveRiots(CrisisCard card) {
        assertEquals(all(lose(FOOD, 1), lose(MORALE, 1)), followup(card, TOP));
        assertEquals(all(lose(POPULATION, 1), lose(FUEL, 1)), followup(card, BOTTOM));
    }

    /* galactica and jump preparation */

    @Test
    void shouldResolveControversialManuscript() {
        assertEquals(single(lose(MORALE, 1)), followup(CONTROVERSIAL_MANUSCRIPT, TOP));
        assertEquals(all(new GainResourceEvent(MORALE, 1), new DamageGalacticaEvent(), new DamageGalacticaEvent()),
                followup(CONTROVERSIAL_MANUSCRIPT, BOTTOM));
    }

    @Test
    void shouldResolveLaborDispute() {
        assertEquals(single(lose(MORALE, 2)), followup(LABOR_DISPUTE, TOP));
        assertEquals(all(lose(FUEL, 1), new DecreaseJumpPreparationEvent()), followup(LABOR_DISPUTE, BOTTOM));
    }

    /* ships and sickbay */

    @Test
    void shouldResolveHiddenExplosives() {
        assertEquals(all(new DestroyRaptorEvent(), new SendToSickbayEvent(CURRENT)), followup(HIDDEN_EXPLOSIVES, TOP));
        assertEquals(single(lose(MORALE, 1)), followup(HIDDEN_EXPLOSIVES, BOTTOM));
    }

    @ParameterizedTest
    @EnumSource(value = CrisisCard.class, names = {"RESCUE_MISSION_BASESTAR", "RESCUE_MISSION_RAIDER"})
    void shouldResolveRescueMission(CrisisCard card) {
        assertEquals(all(lose(MORALE, 1), new SendToSickbayEvent(CURRENT)), followup(card, TOP));
        assertEquals(all(lose(FUEL, 1), new DestroyRaptorEvent()), followup(card, BOTTOM));
    }

    /* skill cards */

    @Test
    void shouldResolveAmbushedByThePress() {
        assertEquals(single(lose(MORALE, 1)), followup(AMBUSHED_BY_THE_PRESS, TOP));
        assertEquals(single(new DiscardAllSkillCardsEvent(PRESIDENT)), followup(AMBUSHED_BY_THE_PRESS, BOTTOM));
    }

    @Test
    void shouldResolveQuestionProcedure() {
        assertEquals(single(lose(MORALE, 1)), followup(QUESTION_PROCEDURE, TOP));
        assertEquals(all(new DamageGalacticaEvent(), discard(PRESIDENT, DISCARD_EXACTLY_3)),
                followup(QUESTION_PROCEDURE, BOTTOM));
    }

    @Test
    void shouldResolveRaidersInbound() {
        assertEquals(all(lose(POPULATION, 1), new DamageGalacticaEvent()), followup(RAIDERS_INBOUND, TOP));
        assertEquals(all(discard(CAG, DISCARD_EXACTLY_3), discard(ADMIRAL, DISCARD_EXACTLY_3)),
                followup(RAIDERS_INBOUND, BOTTOM));
    }

    @Test
    void shouldLetOnePlayerDiscardTwiceWhenTheyAreCagAndAdmiral() {
        cag(WILLIAM_ADAMA);

        assertEquals(all(discard(ADMIRAL, DISCARD_EXACTLY_3), discard(ADMIRAL, DISCARD_EXACTLY_3)),
                followup(RAIDERS_INBOUND, BOTTOM));
    }

    @Test
    void shouldResolveFoodShortage() {
        assertEquals(single(lose(FOOD, 2)), followup(FOOD_SHORTAGE, TOP));
        assertEquals(all(single(lose(FOOD, 1)), presidentThenCurrentPlayerDiscard(PRESIDENT)),
                followup(FOOD_SHORTAGE, BOTTOM));
    }

    @ParameterizedTest
    @EnumSource(value = CrisisCard.class,
            names = {"WATER_SHORTAGE_BASESTAR_AND_JUMP", "WATER_SHORTAGE_BASESTAR_NO_JUMP", "WATER_SHORTAGE_RAIDERS"})
    void shouldResolveWaterShortage(CrisisCard card) {
        assertEquals(single(lose(FOOD, 1)), followup(card, TOP));
        assertEquals(presidentThenCurrentPlayerDiscard(PRESIDENT), followup(card, BOTTOM));
    }

    @Test
    void shouldLetAPresidentWhoIsTheCurrentPlayerDiscardBoth() {
        president(FELIX_GAETA);

        assertEquals(presidentThenCurrentPlayerDiscard(CURRENT), followup(WATER_SHORTAGE_RAIDERS, BOTTOM));
    }

    /* revealed Cylons */

    @ParameterizedTest
    @EnumSource(value = CrisisCard.class, names = {"RESCUE_MISSION_BASESTAR", "RESCUE_MISSION_RAIDER"})
    void shouldNotSendARevealedCylonToSickbayOnRescueMission(CrisisCard card) {
        revealCylon();

        assertEquals(single(lose(MORALE, 1)), followup(card, TOP));
    }

    @Test
    void shouldNotSendARevealedCylonToSickbayOnHiddenExplosives() {
        revealCylon();

        assertEquals(single(new DestroyRaptorEvent()), followup(HIDDEN_EXPLOSIVES, TOP));
    }

    @Test
    void shouldNotMakeARevealedCylonDiscardOnFoodShortage() {
        revealCylon();

        assertEquals(all(lose(FOOD, 1), discard(PRESIDENT, DISCARD_EXACTLY_2)), followup(FOOD_SHORTAGE, BOTTOM));
    }

    @Test
    void shouldNotMakeARevealedCylonDiscardOnWaterShortage() {
        revealCylon();

        assertEquals(single(discard(PRESIDENT, DISCARD_EXACTLY_2)), followup(WATER_SHORTAGE_RAIDERS, BOTTOM));
    }

    @Test
    void shouldTreatACylonLeaderAsARevealedCylon() {
        setUpPlayers(AARON_DORAL);

        assertEquals(single(new DestroyRaptorEvent()), followup(HIDDEN_EXPLOSIVES, TOP));
    }

    @Test
    void shouldTreatAnInfiltratingCylonLeaderAsHuman() {
        setUpPlayers(AARON_DORAL);
        ops.player(CURRENT).infiltrateGalactica(Location.COMMAND);

        assertEquals(all(new DestroyRaptorEvent(), new SendToSickbayEvent(CURRENT)), followup(HIDDEN_EXPLOSIVES, TOP));
    }

    private Followup followup(CrisisCard card, Section section) {
        return new CrisisCardFollowups(game).followup(card, section);
    }

    private static Followup presidentThenCurrentPlayerDiscard(int president) {
        return all(discard(president, DISCARD_EXACTLY_2), discard(CURRENT, DISCARD_EXACTLY_3));
    }

    private static Event lose(Resource resource, int amount) {
        return new LoseResourceEvent(resource, amount);
    }

    private static Event discard(int playerNumber, EventConstraint limit) {
        return new PlayerDecisionEvent<>(playerNumber, DiscardSkillCardsEvent.class, limit);
    }
}
