package xyz.zlatanov.frakkintoasters.event.skillcheck;

import lombok.RequiredArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.board.DecreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.board.IncreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.board.PlaceCenturionsEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateCylonBasestarsEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateHeavyRaidersAndCenturionsEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.ActivateRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.crisis.KeepCrisisCardInPlayEvent;
import xyz.zlatanov.frakkintoasters.event.deck.ShuffleTreacheryIntoDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextPresidentEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.*;
import xyz.zlatanov.frakkintoasters.event.ship.*;
import xyz.zlatanov.frakkintoasters.event.skillcheck.result.*;
import xyz.zlatanov.frakkintoasters.state.board.Location;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.skill.SkillCheckType;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.*;
import static xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEventProcessor.canBecomePresident;
import static xyz.zlatanov.frakkintoasters.event.ship.DamageVipersEvent.Area.*;
import static xyz.zlatanov.frakkintoasters.state.board.Location.*;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.*;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.POLITICS;
import static xyz.zlatanov.frakkintoasters.state.skill.SkillCardColor.TREACHERY;

/**
 * Builds the followup of the active skill check's result, i.e. the pass, partial pass or fail effects printed on the
 * card. Effects shared by several skill checks use generic events (e.g. {@link DamageGalacticaEvent}), effects unique
 * to one skill check use the specialized events in {@code event.skillcheck.result}. "No effect" results give
 * {@link Followup#NONE}. Skill checks with the same result are grouped together.
 */
@RequiredArgsConstructor
public class SkillCheckResultFollowups {

    private static final int LOW_SUPPLIES_FOOD_THRESHOLD = 6;

    private final GameView game;

    public Followup followup() {
        val skillCheck = game.activeSkillCheck();
        return switch (skillCheck.result()) {
            case PASS -> pass(skillCheck.type());
            case PARTIAL_PASS -> partialPass(skillCheck.type());
            case FAIL -> fail(skillCheck.type());
        };
    }

    private Followup pass(SkillCheckType type) {
        return switch (type) {
            // game board locations, acting on the character chosen by the location action
            case ADMIRALS_QUARTERS -> single(sendTo(Location.BRIG, target()));
            case AIRLOCK -> single(new ExecuteEvent(target()));
            // "Pass this skill check to move to any location", on Galactica only (rule 9.2.4)
            case BRIG -> inBrig(game.player(target()))
                    ? single(new PlayerDecisionEvent<>(target(), LeaveBrigEvent.class, DESTINATION_GALACTICA))
                    : NONE;
            // resources
            case COLONIAL_DAY, IN_THE_RING, JOES_BAR -> single(gain(MORALE, 1));
            case FORCED_WATER_MINING, SCOUTING_FOR_WATER, THE_BLACK_MARKET -> single(gain(FOOD, 1));
            case SCOUTING_FOR_FUEL -> single(gain(FUEL, 1));
            case REUNITE_THE_FLEET -> single(gain(POPULATION, 1));
            case SABOTAGE_INVESTIGATED -> single(lose(FOOD, 1));
            case EARTH_IN_RUINS, ENEMY_OF_MY_ENEMY -> single(lose(MORALE, 1));
            // jump preparation
            case CRIPPLED_RAIDER, NETWORK_COMPUTERS, CONSULT_THE_PRISONER, MYSTERIOUS_GUIDE, THE_PASSAGE,
                 HYBRID_IN_PANIC -> single(new IncreaseJumpPreparationEvent());
            // loyalty
            case INFORMING_THE_PUBLIC, TERRORIST_INVESTIGATIONS, CODE_BLUE ->
                    single(new PlayerDecisionEvent<>(currentPlayer(), LookAtLoyaltyCardEvent.class));
            case MANDATORY_TESTING -> single(new LookAtLoyaltyCardEvent(president(), currentPlayer()));
            // brig
            case GUILT_BY_COLLUSION ->
                    optional(currentPlayer(), new PlayerDecisionEvent<>(currentPlayer(), SendToBrigEvent.class));
            case FAMILIAR_FACE -> optional(admiral(), new PlayerDecisionEvent<>(admiral(), SendToBrigEvent.class));
            case REBUILD_TRUST -> each(this::inBrig, p -> optional(p,
                    new PlayerDecisionEvent<>(p, LeaveBrigEvent.class, DESTINATION_GALACTICA)));
            // cards
            case FULFILLER_OF_PROPHECY -> single(new DrawSkillCardsOfColorEvent(currentPlayer(), POLITICS, 1));
            case MEDICAL_BREAKTHROUGH -> each(PlayerView::isHuman,
                    p -> single(new PlayerDecisionEvent<>(p, DrawSkillCardsEvent.class, DRAW_EXACTLY_1)));
            case TEMPLE_OF_THE_FIVE -> optional(currentPlayer(),
                    new PlayerDecisionEvent<>(currentPlayer(), DrawSkillCardsEvent.class, DRAW_EXACTLY_2));
            case REACTOR_CRITICAL -> single(treachery(currentPlayer(), 2));
            case FOOTAGE_TRANSMITTED -> eachPlayer(p -> single(treachery(p, 1)));
            case CONSULT_THE_HYBRID -> all(new DrawMutinyCardEvent(currentPlayer()), new ConsultTheHybridPassEvent());
            // cylon ships
            case FLEET_MOBILIZATION -> all(new ActivateRaidersEvent(), new LaunchRaidersEvent());
            // unique to one skill check
            case ANALYZE_ENEMY_FIGHTER -> single(new AnalyzeEnemyFighterPassEvent());
            case LEGENDARY_DISCOVERY -> single(new LegendaryDiscoveryPassEvent());
            case STANDOFF_WITH_PEGASUS -> single(new StandoffWithPegasusPassEvent());
            case CYLON_GENOCIDE -> single(new CylonGenocidePassEvent());
            case MYSTERIOUS_MESSAGE -> single(new MysteriousMessagePassEvent());
            case SET_A_TRAP -> single(new SetATrapPassEvent());
            case STRANGE_BEACON -> single(new StrangeBeaconPassEvent());
            // no effect
            case ADMIRAL_GRILLED, A_TRAITOR_ACCUSED, BOMB_THREAT, CRASH_LANDING, CYLON_ACCUSATION, CYLON_SCREENINGS,
                 CYLON_TRACKING_DEVICE, CYLON_VIRUS, DETECTOR_SABOTAGE, ELECTIONS_LOOM, HANGAR_ACCIDENT,
                 JUMP_COMPUTER_FAILURE, KEEP_TABS_ON_VISITOR, LOSS_OF_A_FRIEND, LOW_SUPPLIES, MISSING_G4_EXPLOSIVES,
                 PRISONER_REVOLT, PRISON_LABOR, RESISTANCE, SECURITY_BREACH, SEND_SURVEY_TEAM, TERRORIST_BOMBER,
                 THE_OLYMPIC_CARRIER, UNEXPECTED_REUNION, UNIDENTIFIED_SHIP, WATER_SABOTAGED, WEAPONS_MALFUNCTION,
                 WITCH_HUNT, AN_OFFER_OF_PEACE, DEFENDING_A_PRISONER, REVIEW_GALACTICAS_LOGS, TRAINING_SNAFU,
                 UNSETTLING_STORIES, AIRLOCK_LEAK, CENTURION_ASSAULT, DIVISIVE_BEHAVIOR, GUILTY_CONSCIENCE,
                 HAUNTED_BY_THE_PAST, HERA_RESCUED, HIDDEN_IDENTITY, POWER_FAILURE, RAPTOR_MALFUNCTION,
                 THREAT_OF_SUPER_NOVA, UNEXPLAINED_DEATHS, UNFAIR_BIAS, A_DESPERATE_PACT, DOMESTIC_DISPUTE,
                 GALACTICA_FALLING_APART, GIVE_IN_TO_DESPAIR, INCITEMENT_TO_MUTINY, INSUBORDINATE_CREW,
                 ONE_LAST_COCKTAIL, QUORUM_IN_UPROAR, RALLYING_SUPPORT, RELIGIOUS_TURMOIL, SECRET_MEETINGS,
                 BOMB_ON_COLONIAL_1, CYLON_INTRUDERS, FIRE_ALL_MISSILES, HUMAN_PRISONER, INBOUND_NUKES, THE_FARM ->
                    NONE;
        };
    }

    private Followup partialPass(SkillCheckType type) {
        return switch (type) {
            case ELECTIONS_LOOM, WITCH_HUNT -> single(lose(MORALE, 1));
            case HANGAR_ACCIDENT, PRISONER_REVOLT, THE_OLYMPIC_CARRIER -> single(lose(POPULATION, 1));
            case RESISTANCE, THE_FARM -> single(lose(FOOD, 1));
            case GIVE_IN_TO_DESPAIR -> all(lose(FOOD, 1), treachery(currentPlayer(), 3));
            case LOSS_OF_A_FRIEND, HYBRID_IN_PANIC -> single(discard(currentPlayer(), 2));
            case REVIEW_GALACTICAS_LOGS -> single(discard(admiral(), 3));
            case AN_OFFER_OF_PEACE, INCITEMENT_TO_MUTINY -> single(new ShuffleTreacheryIntoDestinyDeckEvent(2));
            case CYLON_INTRUDERS -> single(new PlaceCenturionsEvent(1));
            case FOOTAGE_TRANSMITTED -> each(PlayerView::isRevealedCylon, p -> single(treachery(p, 2)));
            case MANDATORY_TESTING, TERRORIST_INVESTIGATIONS, MEDICAL_BREAKTHROUGH -> NONE;
            default -> throw new FrakCallTheAdmiralException(); // no partial pass result printed
        };
    }

    private Followup fail(SkillCheckType type) {
        return switch (type) {
            // resources only
            case DIVISIVE_BEHAVIOR, SECRET_MEETINGS, GUILT_BY_COLLUSION, MANDATORY_TESTING, TERRORIST_INVESTIGATIONS ->
                    single(lose(MORALE, 1));
            case COLONIAL_DAY, INFORMING_THE_PUBLIC, EARTH_IN_RUINS, GIVE_IN_TO_DESPAIR, REBUILD_TRUST ->
                    single(lose(MORALE, 2));
            case ANALYZE_ENEMY_FIGHTER, CRIPPLED_RAIDER, FULFILLER_OF_PROPHECY, UNIDENTIFIED_SHIP ->
                    single(lose(POPULATION, 1));
            case WATER_SABOTAGED -> single(lose(FOOD, 2));
            case HYBRID_IN_PANIC, REACTOR_CRITICAL -> single(lose(FUEL, 1));
            case LOW_SUPPLIES ->
                    single(lose(MORALE, game.boards().galactica().food() < LOW_SUPPLIES_FOOD_THRESHOLD ? 2 : 1));
            case PRISON_LABOR -> all(lose(MORALE, 1), lose(FOOD, 1));
            case RESISTANCE -> all(lose(FOOD, 1), lose(FUEL, 1));
            case FORCED_WATER_MINING -> all(lose(POPULATION, 1), lose(MORALE, 1));
            case THE_OLYMPIC_CARRIER, UNEXPLAINED_DEATHS -> all(lose(MORALE, 1), lose(POPULATION, 1));
            case ONE_LAST_COCKTAIL -> all(lose(FOOD, 1), lose(MORALE, 1));
            case THE_BLACK_MARKET -> all(lose(FOOD, 2), lose(MORALE, 1));
            case SABOTAGE_INVESTIGATED -> all(lose(MORALE, 1), lose(FUEL, 1), lose(FOOD, 1));
            case INBOUND_NUKES -> all(lose(FUEL, 1), lose(FOOD, 1), lose(POPULATION, 1));
            // sickbay and brig
            case CYLON_ACCUSATION -> single(sendTo(Location.BRIG, currentPlayer()));
            case CODE_BLUE, HIDDEN_IDENTITY, JOES_BAR -> all(lose(MORALE, 1), sendTo(Location.BRIG, currentPlayer()));
            case TERRORIST_BOMBER, IN_THE_RING, DOMESTIC_DISPUTE ->
                    all(lose(MORALE, 1), sendTo(SICKBAY, currentPlayer()));
            case SEND_SURVEY_TEAM -> all(sendTo(SICKBAY, currentPlayer()), new DestroyRaptorEvent());
            case AIRLOCK_LEAK -> all(new DamageGalacticaEvent(), sendTo(SICKBAY, currentPlayer()));
            case CENTURION_ASSAULT -> all(new DestroyRaptorEvent(), sendTo(SICKBAY, currentPlayer()));
            case SET_A_TRAP -> all(new PlaceCenturionsEvent(1), sendTo(SICKBAY, currentPlayer()));
            case A_TRAITOR_ACCUSED -> single(new PlayerDecisionEvent<>(currentPlayer(), SendToBrigEvent.class));
            case WITCH_HUNT -> allOf(single(lose(MORALE, 1)), chooseCharacterForSickbay(game));
            case SECURITY_BREACH -> allOf(single(lose(MORALE, 1)), sendCharactersIn(SICKBAY, COMMAND));
            case MISSING_G4_EXPLOSIVES -> allOf(single(lose(FOOD, 1)), sendCharactersIn(Location.BRIG, ARMORY));
            case WEAPONS_MALFUNCTION ->
                    allOf(single(new DamageVipersEvent(2, SPACE)), sendCharactersIn(SICKBAY, WEAPONS_CONTROL));
            case CYLON_VIRUS -> allOf(sendCharactersIn(SICKBAY, FTL_CONTROL), single(new PlaceCenturionsEvent(1)));
            case DETECTOR_SABOTAGE ->
                    allOf(sendCharactersIn(SICKBAY, RESEARCH_LAB), single(new KeepCrisisCardInPlayEvent(type)));
            case BOMB_ON_COLONIAL_1 -> allOf(single(lose(MORALE, 2)),
                    sendCharactersIn(SICKBAY, QUORUM_CHAMBER, PRESS_ROOM, PRESIDENTS_OFFICE, Location.ADMINISTRATION),
                    single(new KeepCrisisCardInPlayEvent(type)));
            case CONSULT_THE_PRISONER ->
                    allOf(eachPlayer(p -> single(discard(p, 1))), single(sendTo(Location.BRIG, currentPlayer())));
            // skill cards
            case ADMIRAL_GRILLED -> all(lose(MORALE, 1), discard(admiral(), 2));
            case ELECTIONS_LOOM -> all(lose(MORALE, 1), discard(president(), 4));
            case LOSS_OF_A_FRIEND -> all(lose(MORALE, 1), discard(currentPlayer(), 2));
            case REVIEW_GALACTICAS_LOGS -> all(lose(MORALE, 1), discard(admiral(), 5));
            case RELIGIOUS_TURMOIL -> allOf(single(lose(MORALE, 1)), eachPlayer(p -> single(discard(p, 1))));
            case MEDICAL_BREAKTHROUGH, REUNITE_THE_FLEET, UNSETTLING_STORIES ->
                    allOf(single(lose(MORALE, 1)), eachPlayerDiscardsAndDrawsTreachery());
            case UNEXPECTED_REUNION -> all(lose(MORALE, 1), new DiscardAllSkillCardsEvent(currentPlayer()));
            case FAMILIAR_FACE -> all(lose(MORALE, 1), new DiscardAllSkillCardsEvent(admiral()));
            case MYSTERIOUS_GUIDE -> all(lose(FUEL, 1), new DiscardAllSkillCardsEvent(currentPlayer()));
            case UNFAIR_BIAS -> all(new DamageGalacticaEvent(), new DiscardAllSkillCardsEvent(currentPlayer()));
            case GUILTY_CONSCIENCE -> single(new DiscardRandomSkillCardsEvent(currentPlayer(), 3));
            case HAUNTED_BY_THE_PAST -> eachPlayer(p -> single(new DiscardRandomSkillCardsEvent(p, 1)));
            case QUORUM_IN_UPROAR ->
                    all(new QuorumInUproarFailEvent(), new DiscardRandomSkillCardsEvent(president(), 2));
            case INSUBORDINATE_CREW -> allOf(single(lose(MORALE, 1)),
                    each(p -> p.mutinyCards().isEmpty(), p -> single(new DrawMutinyCardEvent(p))));
            case RALLYING_SUPPORT ->
                    all(lose(POPULATION, 1), new DrawMutinyCardEvent(currentPlayer()), treachery(currentPlayer(), 1));
            case FOOTAGE_TRANSMITTED -> allOf(each(PlayerView::isRevealedCylon, p -> single(treachery(p, 2))),
                    single(new FootageTransmittedFailEvent()));
            // destiny deck
            case AN_OFFER_OF_PEACE -> all(lose(MORALE, 1), new ShuffleTreacheryIntoDestinyDeckEvent(2));
            case CONSULT_THE_HYBRID -> all(lose(FOOD, 1), new ShuffleTreacheryIntoDestinyDeckEvent(2));
            case INCITEMENT_TO_MUTINY -> single(new ShuffleTreacheryIntoDestinyDeckEvent(4));
            // loyalty and titles
            case CYLON_SCREENINGS -> allOf(single(lose(MORALE, 1)), lookAtLoyaltyCardOfPresidentOrAdmiral());
            case PRISONER_REVOLT -> allOf(single(lose(POPULATION, 1)), presidentChoosesAnotherPresident());
            case A_DESPERATE_PACT -> all(lose(MORALE, 1), new DetermineNextPresidentEvent(president()));
            // galactica, jump preparation and boarding party
            case THREAT_OF_SUPER_NOVA -> all(lose(POPULATION, 1), new DamageGalacticaEvent());
            case GALACTICA_FALLING_APART -> all(lose(MORALE, 1), new DamageGalacticaEvent());
            case ENEMY_OF_MY_ENEMY -> all(lose(MORALE, 2), new DamageGalacticaEvent());
            case CYLON_INTRUDERS -> all(new DamageGalacticaEvent(), new PlaceCenturionsEvent(2));
            case NETWORK_COMPUTERS -> all(lose(POPULATION, 1), new PlaceCenturionsEvent(1));
            case JUMP_COMPUTER_FAILURE -> all(lose(POPULATION, 1), new DecreaseJumpPreparationEvent());
            case POWER_FAILURE, STRANGE_BEACON, TEMPLE_OF_THE_FIVE -> single(new DecreaseJumpPreparationEvent());
            // ships
            case SCOUTING_FOR_FUEL, SCOUTING_FOR_WATER -> all(lose(FUEL, 1), new DestroyRaptorEvent());
            case LEGENDARY_DISCOVERY -> all(lose(FOOD, 1), new DestroyRaptorEvent());
            case HERA_RESCUED -> all(lose(MORALE, 2), new DestroyRaptorEvent());
            case RAPTOR_MALFUNCTION -> all(new DamageGalacticaEvent(), new DestroyRaptorEvent());
            case CYLON_TRACKING_DEVICE -> all(new DestroyRaptorEvent(), new CylonTrackingDeviceFailEvent());
            case HANGAR_ACCIDENT -> all(lose(POPULATION, 1), new DamageVipersEvent(2, RESERVES));
            case STANDOFF_WITH_PEGASUS -> all(lose(POPULATION, 1), lose(MORALE, 1), new DamageVipersEvent(1, SPACE));
            case TRAINING_SNAFU -> single(new DamageVipersEvent(3, SPACE_OR_RESERVES));
            case BOMB_THREAT -> all(lose(MORALE, 1), new DrawAndDestroyCivilianShipsEvent(1));
            case FIRE_ALL_MISSILES -> single(new DrawAndDestroyCivilianShipsEvent(2));
            // cylon ships
            case MYSTERIOUS_MESSAGE -> all(new LaunchRaidersEvent(), new ActivateRaidersEvent());
            case CYLON_GENOCIDE -> all(lose(MORALE, 1), new ActivateRaidersEvent(), new LaunchRaidersEvent(),
                    new ActivateHeavyRaidersAndCenturionsEvent());
            case FLEET_MOBILIZATION -> all(lose(MORALE, 1), new ActivateRaidersEvent(),
                    new ActivateCylonBasestarsEvent(), new ActivateHeavyRaidersAndCenturionsEvent(),
                    new LaunchRaidersEvent());
            // unique to one skill check
            case CRASH_LANDING -> single(new CrashLandingFailEvent());
            case KEEP_TABS_ON_VISITOR -> single(new KeepTabsOnVisitorFailEvent());
            case DEFENDING_A_PRISONER -> all(lose(MORALE, 1), new DefendingAPrisonerFailEvent());
            case THE_PASSAGE -> single(new ThePassageFailEvent());
            case HUMAN_PRISONER -> single(new HumanPrisonerFailEvent());
            case THE_FARM -> all(lose(FOOD, 1), lose(POPULATION, 1), new KeepCrisisCardInPlayEvent(type));
            // no effect
            case ADMIRALS_QUARTERS, AIRLOCK, BRIG -> NONE;
        };
    }

    /* players */

    private int currentPlayer() {
        return game.turn().currentPlayerNumber();
    }

    private int president() {
        return game.playerNumber(game.president());
    }

    private int admiral() {
        return game.playerNumber(game.admiral());
    }

    private int target() {
        return Optional.ofNullable(game.activeSkillCheck().targetPlayerNumber())
                .orElseThrow(FrakCallTheAdmiralException::new);
    }

    private boolean inBrig(PlayerView player) {
        return game.boards().galactica().charactersIn(Location.BRIG).contains(player.character());
    }

    /**
     * All players in turn order, starting with the current player.
     */
    private List<PlayerView> playersFromCurrent() {
        return playersFrom(game, currentPlayer());
    }

    private Followup eachPlayer(IntFunction<Followup> followup) {
        return each(p -> true, followup);
    }

    private Followup each(Predicate<PlayerView> filter, IntFunction<Followup> followup) {
        return all(playersFromCurrent().stream()
                .filter(filter)
                .map(p -> followup.apply(p.number()))
                .toArray(Followup[]::new));
    }

    /* events */

    private static Event treachery(int playerNumber, int count) {
        return new DrawSkillCardsOfColorEvent(playerNumber, TREACHERY, count);
    }

    private static Followup optional(int playerNumber, Event decision) {
        return one(decision, new NoOpEvent(playerNumber));
    }

    private Event sendTo(Location destination, int playerNumber) {
        return switch (destination) {
            case SICKBAY -> new SendToSickbayEvent(playerNumber);
            case BRIG -> new SendToBrigEvent(playerNumber);
            default -> throw new FrakCallTheAdmiralException();
        };
    }

    private Followup sendCharactersIn(Location destination, Location... locations) {
        return all(Arrays.stream(locations)
                .flatMap(location -> game.boards().galactica().charactersIn(location).stream())
                .map(game::playerNumber)
                .sorted()
                .map(playerNumber -> sendTo(destination, playerNumber))
                .toArray(Event[]::new));
    }

    // Only asked when another player can take the title, so the decision has an answer.
    private Followup presidentChoosesAnotherPresident() {
        val anyoneEligible = game.players().stream().anyMatch(p -> canBecomePresident(game, p.character()));
        return anyoneEligible ? single(new PlayerDecisionEvent<>(president(), MakePresidentEvent.class)) : NONE;
    }

    private Followup eachPlayerDiscardsAndDrawsTreachery() {
        return eachPlayer(p -> all(discard(p, 1), treachery(p, 1)));
    }

    private Followup lookAtLoyaltyCardOfPresidentOrAdmiral() {
        return one(IntStream.of(president(), admiral())
                .distinct()
                .filter(target -> target != currentPlayer()) // The current player looks at a Loyalty Card of the President or the Admiral, never at his own.
                .mapToObj(target -> new LookAtLoyaltyCardEvent(currentPlayer(), target))
                .toArray(Event[]::new));
    }
}
