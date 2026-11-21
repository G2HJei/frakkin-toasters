package xyz.zlatanov.frakkintoasters.event.crisis;

import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor;
import xyz.zlatanov.frakkintoasters.event.DrawQuorumCardEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.board.AdvancePursuitTrackEvent;
import xyz.zlatanov.frakkintoasters.event.board.DecreaseJumpPreparationEvent;
import xyz.zlatanov.frakkintoasters.event.board.DiscardNukeTokenEvent;
import xyz.zlatanov.frakkintoasters.event.board.PlaceCenturionsEvent;
import xyz.zlatanov.frakkintoasters.event.character.CharacterAbilities;
import xyz.zlatanov.frakkintoasters.event.character.ChooseADifferentPathEvent;
import xyz.zlatanov.frakkintoasters.event.character.DeceitfulDiscards;
import xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section;
import xyz.zlatanov.frakkintoasters.event.deck.ShuffleTreacheryIntoDestinyDeckEvent;
import xyz.zlatanov.frakkintoasters.event.officials.DetermineNextPresidentEvent;
import xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.DrawMutinyCardEvent;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;
import xyz.zlatanov.frakkintoasters.event.player.LaunchViperEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveFromBrigEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageBattlestarEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DamageVipersEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyRaptorEvent;
import xyz.zlatanov.frakkintoasters.event.ship.DrawAndDestroyCivilianShipsEvent;
import xyz.zlatanov.frakkintoasters.event.ship.LaunchRaidersEvent;
import xyz.zlatanov.frakkintoasters.event.ship.PlaceShipsEvent;
import xyz.zlatanov.frakkintoasters.event.ship.ReturnVipersToReservesEvent;
import xyz.zlatanov.frakkintoasters.state.crisis.CrisisCard;
import xyz.zlatanov.frakkintoasters.state.crisis.CylonActivation;
import xyz.zlatanov.frakkintoasters.state.exception.FrakCallTheAdmiralException;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.HumanFighterView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import static xyz.zlatanov.frakkintoasters.event.Followup.*;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.*;
import static xyz.zlatanov.frakkintoasters.event.constraint.EventConstraint.MUST_LAUNCH_IN_VIPER;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.BOTTOM;
import static xyz.zlatanov.frakkintoasters.event.crisis.ChooseCrisisSectionEvent.Section.TOP;
import static xyz.zlatanov.frakkintoasters.event.officials.MakePresidentEventProcessor.canBecomePresident;
import static xyz.zlatanov.frakkintoasters.event.player.SendToBrigEventProcessor.canBeSentToBrig;
import static xyz.zlatanov.frakkintoasters.event.ship.DamageVipersEvent.Area.RESERVES;
import static xyz.zlatanov.frakkintoasters.event.ship.DamageVipersEvent.Area.SPACE_OR_RESERVES;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;
import static xyz.zlatanov.frakkintoasters.state.board.Location.COMMAND;
import static xyz.zlatanov.frakkintoasters.state.board.LocationsArea.GALACTICA;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.*;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.AMORAL;
import static xyz.zlatanov.frakkintoasters.state.character.CharacterAbility.CHOOSE_A_DIFFERENT_PATH;
import static xyz.zlatanov.frakkintoasters.state.crisis.CrisisChooser.CURRENT_PLAYER;

/**
 * Builds the followups of the sections of a Crisis Card (rule 8.5). The two sections of an event crisis are its top and
 * bottom. A skill check crisis has the skill check on top, and its bottom is the alternate instruction that its chooser
 * may pick instead (rule 8.5.1.1).
 */
public class CrisisCardFollowups {

    private final GameView          game;
    // "Deceitful": one of Romo Lampkin's discards is smaller, once per card. A builder serves one card.
    private final DeceitfulDiscards deceitful;

    public CrisisCardFollowups(GameView game) {
        this.game = game;
        deceitful = DeceitfulDiscards.of(game, true);
    }


    /**
     * @return the chooser's pick of a section. A card that can't be fulfilled doesn't offer the section it says so about
     */
    public Followup choice(CrisisCard card) {
        val chooser = chooser(card);
        val sections = Stream.of(TOP, BOTTOM).filter(section -> canChoose(game, card, section)).toList();
        // "Amoral": Tory Foster as current player takes the first option a "current player chooses" card lists.
        val amoral = card.chooser() == CURRENT_PLAYER && game.player(chooser).hasAbility(AMORAL);
        val options = new ArrayList<Event>((amoral ? sections.subList(0, 1) : sections).stream()
                .map(section -> new ChooseCrisisSectionEvent(chooser, card, section))
                .toList());
        if (CharacterAbilities.canUseMiracle(game, game.player(chooser), CHOOSE_A_DIFFERENT_PATH)) {
            options.add(new ChooseADifferentPathEvent(chooser, card));
        }
        return one(options.toArray(Event[]::new));
    }

    /**
     * @return "Choose a Different Path"'s result: "the current player discards 5 Skill Cards"
     */
    public Followup differentPath() {
        return onCurrentPlayer(discard(currentPlayer(), 5));
    }

    /**
     * @return what the Cylon ship activation icon on the card does (rule 11.5)
     */
    public static Event activation(CylonActivation activation) {
        return switch (activation) {
            case ACTIVATE_RAIDERS -> new ActivateRaidersEvent();
            case LAUNCH_RAIDERS -> new LaunchRaidersEvent();
            case ACTIVATE_HEAVY_RAIDERS -> new ActivateHeavyRaidersAndCenturionsEvent();
            case ACTIVATE_BASESTARS -> new ActivateCylonBasestarsEvent();
        };
    }

    public Followup followup(CrisisCard card, Section section) {
        return switch (section) {
            case TOP -> top(card);
            case BOTTOM -> bottom(card);
        };
    }

    // "Discard 1 nuke token. If you do not have any nuke tokens, you cannot choose this option." Otherwise a player can
    // always choose a section, whether or not it can be fulfilled (rule 8.5.2).
    public static boolean canChoose(GameView game, CrisisCard card, Section section) {
        val needsANuke = section == TOP && (card == CrisisCard.ABANDON_GALACTICA || card == CrisisCard.BUILD_CYLON_DETECTOR);
        return !needsANuke || game.nukes() > 0;
    }

    // The top of an event crisis. The top of a skill check crisis is its skill check, which the section's processor starts.
    private Followup top(CrisisCard card) {
        return switch (card) {
            // resources only
            case AMBUSHED_BY_THE_PRESS, CONTROVERSIAL_MANUSCRIPT, QUESTION_PROCEDURE -> single(lose(MORALE, 1));
            case LABOR_DISPUTE -> single(lose(MORALE, 2));
            case WATER_SHORTAGE_BASESTAR_AND_JUMP, WATER_SHORTAGE_BASESTAR_NO_JUMP, WATER_SHORTAGE_RAIDERS ->
                    single(lose(FOOD, 1));
            case FOOD_SHORTAGE, WIDESPREAD_STARVATION -> single(lose(FOOD, 2));
            case RIOTS_JUMP, RIOTS_NO_JUMP -> all(lose(FOOD, 1), lose(MORALE, 1));
            case RESCUE_CAPRICA_SURVIVORS -> all(lose(FUEL, 1), lose(FOOD, 1), gain(POPULATION, 1));
            case RESCUE_THE_FLEET -> single(lose(POPULATION, 2));
            // galactica
            case RAIDERS_INBOUND -> all(lose(POPULATION, 1), new DamageBattlestarEvent());
            // sickbay
            case RESCUE_MISSION_BASESTAR, RESCUE_MISSION_RAIDER ->
                    allOf(single(lose(MORALE, 1)), onCurrentPlayer(new SendToSickbayEvent(currentPlayer())));
            case HIDDEN_EXPLOSIVES ->
                    allOf(single(new DestroyRaptorEvent()), onCurrentPlayer(new SendToSickbayEvent(currentPlayer())));
            case SLEEP_DEPRIVATION ->
                    allOf(single(new ReturnVipersToReservesEvent()), onCurrentPlayer(new SendToSickbayEvent(currentPlayer())));
            // Cylon Fleet
            case DETENTE -> all(new ReturnVipersToReservesEvent(), new AdvancePursuitTrackEvent());
            case RETURN_TO_DUTY -> allOf(eachPilotLaunches(), single(new LaunchRaidersEvent()));
            case REVIEW_CAMERA_FOOTAGE -> allOf(all(new DamageVipersEvent(2, RESERVES), new AdvancePursuitTrackEvent()),
                    activateUnmannedViper(cag()));
            case TRACKED_BY_RADIATION -> single(new PlaceShipsEvent(1, 3, 2));
            case TRAINING_A_ROOKIE -> allOf(activateUnmannedViper(cag()), single(new ActivateRaidersEvent()));
            // Daybreak
            case ABANDON_GALACTICA, BUILD_CYLON_DETECTOR -> single(new DiscardNukeTokenEvent());
            case AN_AMBITIOUS_OPERATION -> allOf(single(lose(FUEL, 1)), chooseTarget(card));
            case DANGEROUS_PLOTS -> all(new DrawMutinyCardEvent(admiral()), new DrawMutinyCardEvent(president()));
            case DISHONEST_TACTICS -> allOf(single(lose(MORALE, 1)), presidentMovesSomeoneFromTheBrig());
            case REQUISITION_FOR_DEMETRIUS -> allOf(single(lose(FOOD, 1)), rollDie(new ResolveCrisisRollEvent(card, TOP)));
            case STARVATION_IN_DOGSVILLE -> rollDie(new ResolveCrisisRollEvent(card, TOP));
            // Exodus
            case APPOINT_HEAD_OF_SECURITY ->
                    all(new ReturnVipersToReservesEvent(), discardRandom(admiral(), 2));
            case INTERROGATION, THE_CIRCLE -> chooseTarget(card);
            case REQUESTED_RESIGNATION -> all(discard(president(), 2), discard(admiral(), 2));
            case MEDAL_OF_DISTINCTION -> allOf(single(gain(MORALE, 1)), placeCivilianShips(game, 2),
                    single(new ActivateRaidersEvent()));
            case TRUTH_AND_RECONCILIATION -> allOf(single(lose(MORALE, 1)), chooseCharacterForBrig(president()));
            case UNWELCOME_FACES ->
                    allOf(single(discardAll(admiral())), chooseCharacterForBrig(admiral()));
            // Galactica
            case DECLARE_MARTIAL_LAW -> allOf(single(lose(MORALE, 1)), admiralBecomesPresident());
            // Pegasus
            case A_GUILTY_VERDICT -> allOf(onCurrentPlayer(new ExecuteEvent(currentPlayer())), single(discard(admiral(), 3)));
            case ASSASSINATION_PLOT -> allOf(all(discard(admiral(), 3), treachery(admiral(), 3)),
                    onCurrentPlayer(all(discard(currentPlayer(), 3), treachery(currentPlayer(), 3))));
            case CIVILIAN_SHIP_NUKED -> allOf(single(new DrawAndDestroyCivilianShipsEvent(1)),
                    eachHuman(player -> all(discard(player, 1), treachery(player, 1))));
            case FOOD_HOARDING_IN_THE_FLEET -> allOf(single(lose(MORALE, 1)), rollDie(new ResolveCrisisRollEvent(card, TOP)));
            case PRESSURE_SUPPLY_SHIPS ->
                    all(gain(FOOD, 1), lose(MORALE, 1), discard(admiral(), 2), treachery(admiral(), 2));
            case SUSPICIOUS_ELECTION_RESULTS -> single(new DetermineNextPresidentEvent(president()));
            // the Crisis Cards with a skill check
            default -> throw new FrakCallTheAdmiralException("The top of " + card + " is its skill check");
        };
    }

    // The bottom of an event crisis, or the alternate instruction of a skill check crisis.
    private Followup bottom(CrisisCard card) {
        return switch (card) {
            // resources only
            case HIDDEN_EXPLOSIVES, RESCUE_CAPRICA_SURVIVORS, MEDAL_OF_DISTINCTION, SLEEP_DEPRIVATION,
                 FAMILIAR_FACE, HERA_RESCUED, MYSTERIOUS_GUIDE, ADMIRAL_GRILLED, COLONIAL_DAY, UNSETTLING_STORIES ->
                    single(lose(MORALE, 1));
            case TRACKED_BY_RADIATION -> single(lose(FUEL, 1));
            case FOOD_HOARDING_IN_THE_FLEET, PRESSURE_SUPPLY_SHIPS -> single(lose(FOOD, 2));
            case SCOUTING_FOR_WATER, WATER_SABOTAGED -> single(lose(FOOD, 1));
            case RIOTS_JUMP, RIOTS_NO_JUMP -> all(lose(POPULATION, 1), lose(FUEL, 1));
            case WIDESPREAD_STARVATION -> all(lose(FOOD, 1), lose(POPULATION, 1));
            case ABANDON_GALACTICA -> all(lose(FOOD, 1), treachery(admiral(), 2));
            case NETWORK_COMPUTERS -> all(lose(POPULATION, 1), new DecreaseJumpPreparationEvent());
            case EARTH_IN_RUINS -> allOf(single(lose(FOOD, 1)), onCurrentPlayer(new DrawMutinyCardEvent(currentPlayer())));
            // galactica and jump preparation
            case CONTROVERSIAL_MANUSCRIPT ->
                    all(gain(MORALE, 1), new DamageBattlestarEvent(), new DamageBattlestarEvent());
            case LABOR_DISPUTE -> all(lose(FUEL, 1), new DecreaseJumpPreparationEvent());
            case APPOINT_HEAD_OF_SECURITY, UNWELCOME_FACES -> all(lose(MORALE, 1), new DamageBattlestarEvent());
            case A_GUILTY_VERDICT, ENEMY_OF_MY_ENEMY -> all(new DamageBattlestarEvent(), new DamageBattlestarEvent());
            case RESCUE_THE_FLEET -> all(lose(MORALE, 1), new PlaceShipsEvent(1, 3, 3));
            // ships
            case RESCUE_MISSION_BASESTAR, RESCUE_MISSION_RAIDER -> all(lose(FUEL, 1), new DestroyRaptorEvent());
            case TRAINING_A_ROOKIE -> single(new DamageVipersEvent(2, SPACE_OR_RESERVES));
            case CIVILIAN_SHIP_NUKED -> single(new DrawAndDestroyCivilianShipsEvent(2));
            // Cylon ships
            case DETENTE -> all(new ActivateCylonBasestarsEvent(), new ActivateRaidersEvent(),
                    new ActivateHeavyRaidersAndCenturionsEvent());
            case RETURN_TO_DUTY, MYSTERIOUS_MESSAGE -> single(new ActivateCylonBasestarsEvent());
            // skill cards
            case AMBUSHED_BY_THE_PRESS -> single(discardAll(president()));
            case QUESTION_PROCEDURE -> all(new DamageBattlestarEvent(), discard(president(), 3));
            case RAIDERS_INBOUND -> all(discard(cag(), 3), discard(admiral(), 3));
            case WATER_SHORTAGE_BASESTAR_AND_JUMP, WATER_SHORTAGE_BASESTAR_NO_JUMP, WATER_SHORTAGE_RAIDERS,
                 THE_CIRCLE, TRUTH_AND_RECONCILIATION -> presidentThenCurrentPlayerDiscard();
            case FOOD_SHORTAGE -> allOf(single(lose(FOOD, 1)), presidentThenCurrentPlayerDiscard());
            case REVIEW_CAMERA_FOOTAGE -> holderThenCurrentPlayerDiscard(cag(), 2);
            case INTERROGATION, CONSULT_THE_PRISONER -> holderThenCurrentPlayerDiscard(admiral(), 2);
            case DANGEROUS_PLOTS -> allOf(single(lose(MORALE, 1)), onCurrentPlayer(discard(currentPlayer(), 3)));
            case BUILD_CYLON_DETECTOR -> all(lose(MORALE, 1), discard(admiral(), 2));
            case DECLARE_MARTIAL_LAW -> all(lose(POPULATION, 1), discard(admiral(), 2));
            case REQUESTED_RESIGNATION -> chooseTarget(card);
            case A_DESPERATE_PACT -> allOf(single(discard(president(), 3)),
                    onCurrentPlayer(new DrawMutinyCardEvent(currentPlayer())));
            case A_TRAITOR_ACCUSED -> onCurrentPlayer(discard(currentPlayer(), 5));
            case KEEP_TABS_ON_VISITOR -> onCurrentPlayer(discardRandom(currentPlayer(), 4));
            case REUNITE_THE_FLEET -> onCurrentPlayer(all(discardRandom(currentPlayer(), 2),
                    treachery(currentPlayer(), 2)));
            case FULFILLER_OF_PROPHECY ->
                    allOf(onCurrentPlayer(discard(currentPlayer(), 1)), single(new ReturnToCrisisStepEvent()));
            case REQUISITION_FOR_DEMETRIUS -> all(new DrawMutinyCardEvent(admiral()), treachery(admiral(), 2));
            case SUSPICIOUS_ELECTION_RESULTS -> all(discardRandom(admiral(), 1), treachery(admiral(), 1));
            // each player
            case CYLON_SCREENINGS -> eachHuman(player -> single(discard(player, 2)));
            case CODE_BLUE -> eachHuman(player -> all(discard(player, 2), treachery(player, 2)));
            case THE_BLACK_MARKET -> allOf(single(lose(FOOD, 1)),
                    eachHuman(player -> all(discard(player, 1), treachery(player, 1))));
            case FORCED_WATER_MINING -> allOf(all(gain(FOOD, 1), lose(MORALE, 1)),
                    eachHuman(player -> single(discardRandom(player, 1))));
            case ASSASSINATION_PLOT -> single(new ExecuteEvent(admiral()));
            case DISHONEST_TACTICS -> all(lose(FUEL, 1), new DrawQuorumCardEvent(), new DrawQuorumCardEvent());
            case SECRET_MEETINGS -> allOf(onCurrentPlayer(new DrawMutinyCardEvent(currentPlayer())), chooseTarget(card));
            // the die rolls
            case AN_AMBITIOUS_OPERATION, STARVATION_IN_DOGSVILLE, GALACTICA_FALLING_APART, ONE_LAST_COCKTAIL,
                 RELIGIOUS_TURMOIL, CYLON_GENOCIDE, THE_PASSAGE, SET_A_TRAP, ANALYZE_ENEMY_FIGHTER, BOMB_THREAT,
                 CRIPPLED_RAIDER, INFORMING_THE_PUBLIC, SCOUTING_FOR_FUEL, SEND_SURVEY_TEAM ->
                    rollDie(new ResolveCrisisRollEvent(card, BOTTOM));
            case RAPTOR_MALFUNCTION -> onCurrentPlayer(new SendToSickbayEvent(currentPlayer()));
            default -> throw new FrakCallTheAdmiralException();
        };
    }

    /**
     * @return what a die roll of a section does, the roll being its result after all modifiers
     */
    public Followup rollResult(CrisisCard card, Section section, int roll) {
        return switch (card) {
            case AN_AMBITIOUS_OPERATION -> roll <= 4 ? single(lose(FUEL, 1)) : NONE;
            case GALACTICA_FALLING_APART -> roll <= 6 ? single(lose(FOOD, 1)) : NONE;
            case ONE_LAST_COCKTAIL ->
                    roll <= 6 ? all(lose(MORALE, 1), new SendToSickbayEvent(president())) : NONE;
            case RELIGIOUS_TURMOIL -> roll <= 4 ? all(lose(FOOD, 1), lose(POPULATION, 1)) : NONE;
            case REQUISITION_FOR_DEMETRIUS -> roll <= 6 ? single(new ShuffleTreacheryIntoDestinyDeckEvent(2)) : NONE;
            case STARVATION_IN_DOGSVILLE -> roll > 4 ? NONE
                    : section == TOP ? all(lose(POPULATION, 1), lose(FOOD, 1)) : single(lose(FOOD, 2));
            case CYLON_GENOCIDE -> roll <= 4 ? onCurrentPlayerToTheBrig() : NONE;
            case THE_PASSAGE -> roll <= 6 ? onCurrentPlayer(new SendToSickbayEvent(currentPlayer())) : NONE;
            case SET_A_TRAP -> roll <= 4 ? single(new PlaceCenturionsEvent(1)) : NONE;
            case ANALYZE_ENEMY_FIGHTER ->
                    roll <= 4 ? allOf(single(lose(POPULATION, 1)), onCurrentPlayer(discard(currentPlayer(), 2))) : NONE;
            case BOMB_THREAT -> roll <= 4 ? all(lose(MORALE, 1), new DrawAndDestroyCivilianShipsEvent(1)) : NONE;
            case CRIPPLED_RAIDER -> roll <= 4 ? single(new PlaceShipsEvent(0, 3, 1)) : NONE;
            case INFORMING_THE_PUBLIC -> roll <= 4 ? all(lose(MORALE, 1), lose(POPULATION, 1)) : NONE;
            case SCOUTING_FOR_FUEL -> roll <= 4 ? single(lose(FUEL, 1)) : NONE;
            case SEND_SURVEY_TEAM -> roll <= 5 ? single(lose(FUEL, 1)) : NONE;
            case FOOD_HOARDING_IN_THE_FLEET -> roll <= 3 ? single(new DrawAndDestroyCivilianShipsEvent(1)) : NONE;
            default -> throw new FrakCallTheAdmiralException();
        };
    }

    /* players */

    private int chooser(CrisisCard card) {
        return switch (card.chooser()) {
            case CURRENT_PLAYER -> currentPlayer();
            case PRESIDENT -> president();
            case ADMIRAL -> admiral();
            case CAG -> cag();
            case null -> throw new FrakCallTheAdmiralException(card + " has no chooser");
        };
    }

    private int currentPlayer() {
        return game.turn().currentPlayerNumber();
    }

    private int president() {
        return game.playerNumber(game.president());
    }

    private int admiral() {
        return game.playerNumber(game.admiral());
    }

    private int cag() {
        return game.playerNumber(game.cag());
    }

    // Every human, clockwise from the current player. A revealed Cylon ignores what a crisis does to him (rule 6.1.2.1).
    private Followup eachHuman(IntFunction<Followup> followup) {
        return all(playersFrom(game, currentPlayer()).stream()
                .filter(PlayerView::isHuman)
                .map(player -> followup.apply(player.number()))
                .toArray(Followup[]::new));
    }

    /* events */

    private Event discard(int playerNumber, int count) {
        return deceitful.discard(playerNumber, count);
    }

    private Event discardRandom(int playerNumber, int count) {
        return deceitful.discardRandom(playerNumber, count);
    }

    private Event discardAll(int playerNumber) {
        return deceitful.discardAll(playerNumber);
    }

    // A revealed Cylon ignores what a crisis does to them (rule 6.1.2.1). Only the current player can be one, when
    // resolving a card at "Caprica": nobody who holds a title is a revealed Cylon.
    private Followup onCurrentPlayer(Event effect) {
        return onCurrentPlayer(single(effect));
    }

    private Followup onCurrentPlayer(Followup effect) {
        return game.currentPlayer().isHuman() ? effect : NONE;
    }

    private Followup onCurrentPlayerToTheBrig() {
        return canBeSentToBrig(game, currentPlayer()) ? single(new SendToBrigEvent(currentPlayer())) : NONE;
    }

    // A President who is also the current player discards 5 Skill Cards (rule A2.3.9.1).
    private Followup presidentThenCurrentPlayerDiscard() {
        return holderThenCurrentPlayerDiscard(president(), 2);
    }

    private Followup holderThenCurrentPlayerDiscard(int holder, int holderCards) {
        return allOf(single(discard(holder, holderCards)), onCurrentPlayer(discard(currentPlayer(), 3)));
    }

    // The player the card names picks one, if there is anyone to pick.
    private Followup chooseTarget(CrisisCard card) {
        return ChooseCrisisTargetEventProcessor.legalTargets(game, card).isEmpty() ? NONE
                : single(new PlayerDecisionEvent<>(ChooseCrisisTargetEventProcessor.chooser(game, card),
                ChooseCrisisTargetEvent.class));
    }

    // "must choose a character to send to the "Brig"": any character that can go there (rule 9.2.3).
    private Followup chooseCharacterForBrig(int chooser) {
        val anyone = game.players().stream().anyMatch(player -> canBeSentToBrig(game, player.number()));
        return anyone ? single(new PlayerDecisionEvent<>(chooser, SendToBrigEvent.class)) : NONE;
    }

    // "The President may choose 1 player to move from the "Brig" to "Command.""
    private Followup presidentMovesSomeoneFromTheBrig() {
        val president = president();
        val options = new ArrayList<Event>(playersFrom(game, president).stream()
                .filter(player -> game.boards().galactica().charactersIn(BRIG).contains(player.character()))
                .map(player -> new MoveFromBrigEvent(player.number(), COMMAND))
                .toList());
        if (options.isEmpty()) {
            return NONE;
        }
        options.add(new NoOpEvent(president)); // "may"
        return one(options.toArray(Event[]::new));
    }

    // "Any character on Galactica with piloting in his skill set may immediately launch himself in a viper." Each may
    // decline, starting with the current player. Not from the "Brig", where a character may not move (rule 9.2).
    private Followup eachPilotLaunches() {
        if (game.boards().galactica().reserves().stream().noneMatch(HumanFighterView.class::isInstance)) {
            return NONE;
        }
        return all(playersFrom(game, currentPlayer()).stream()
                .filter(this::canLaunchHimself)
                .map(player -> optional(player.number(),
                        new PlayerDecisionEvent<>(player.number(), LaunchViperEvent.class, MUST_LAUNCH_IN_VIPER)))
                .toArray(Followup[]::new));
    }

    private boolean canLaunchHimself(PlayerView player) {
        // A character piloting a viper is in a space area, not on Galactica.
        return player.isHuman() && player.character().canPilot()
                && game.locate(player.character()).filter(l -> GALACTICA.locations().contains(l) && l != BRIG).isPresent();
    }

    // "The CAG may then activate 1 unmanned viper." A pilot activates his own viper, so a CAG who is piloting has none to
    // activate here.
    private Followup activateUnmannedViper(int playerNumber) {
        val piloting = game.boards().galactica().pilotedFighter(game.player(playerNumber).character()).isPresent();
        return piloting || !ActivateViperEventProcessor.canActivateUnmanned(game) ? NONE
                : optional(playerNumber, new PlayerDecisionEvent<>(playerNumber, ActivateViperEvent.class));
    }

    private Followup admiralBecomesPresident() {
        return canBecomePresident(game, game.admiral()) ? single(new MakePresidentEvent(game.admiral())) : NONE;
    }
}
