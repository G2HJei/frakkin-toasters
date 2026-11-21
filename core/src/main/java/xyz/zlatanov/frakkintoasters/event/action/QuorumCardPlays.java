package xyz.zlatanov.frakkintoasters.event.action;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.event.action.quorum.*;
import xyz.zlatanov.frakkintoasters.state.card.QuorumCard;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static lombok.AccessLevel.PRIVATE;

/**
 * Which Quorum Cards a player can play now. A card is offered only when its effect can happen, as a Skill Card is
 * ({@link SkillCardPlays}), so a decision to play one always has an answer.
 */
@NoArgsConstructor(access = PRIVATE)
public final class QuorumCardPlays {

    /**
     * @return whether the player could play the card now, if he held it. Each card's own rules are in its processor
     */
    public static boolean canPlay(GameView game, PlayerView player, QuorumCard card) {
        // Revealed Cylons can't use them (rule 6.1.2.3).
        if (!player.isHuman()) {
            return false;
        }
        return switch (card) {
            case ENACT_PRODUCTION_QUOTAS, ESTABLISH_DOGSVILLE, FOOD_RATIONING, INSPIRATIONAL_SPEECH -> true;
            case ARREST_ORDER -> ArrestOrderEventProcessor.canPlay(game);
            case EXECUTE_PRISONER -> ExecutePrisonerEventProcessor.canPlay(game);
            case PRESIDENTIAL_PARDON -> PresidentialPardonEventProcessor.canPlay(game, player);
            case RELEASE_CYLON_MUGSHOTS -> ReleaseCylonMugshotsEventProcessor.canPlay(game, player);
            case ENCOURAGE_MUTINY -> EncourageMutinyEventProcessor.canPlay(game, player);
            case PRESIDENTIAL_ORDER -> PresidentialOrderEventProcessor.canPlay(game);
            case EULOGY -> EulogyEventProcessor.canPlay(game);
            case AUTHORIZATION_OF_BRUTAL_FORCE -> AuthorizationOfBrutalForceEventProcessor.canPlay(game);
            case CIVILIAN_SELF_DEFENSE -> CivilianSelfDefenseEventProcessor.canPlay(game);
            case RESOURCES_FOR_GALACTICA -> ResourcesForGalacticaEventProcessor.canPlay(game);
            case UNSAVORY_CONNECTIONS -> true;
            case CONSULT_THE_ORACLE -> ConsultTheOracleEventProcessor.canPlay(game);
            case RESIGNATION -> ResignationEventProcessor.canPlay(game, player);
            case ACCEPT_PROPHECY -> AcceptProphecyEventProcessor.canPlay(game);
            case ASSIGN_ARBITRATOR -> AssignArbitratorEventProcessor.canPlay(game, player);
            case ASSIGN_CHIEF_OF_STAFF -> AssignChiefOfStaffEventProcessor.canPlay(game, player);
            case ASSIGN_MISSION_SPECIALIST -> AssignMissionSpecialistEventProcessor.canPlay(game, player);
            case ASSIGN_VICE_PRESIDENT -> AssignVicePresidentEventProcessor.canPlay(game, player);
            case PROBATION -> ProbationEventProcessor.canPlay(game);
        };
    }

    /**
     * @return the event that plays the card, for a decision that takes only that card, e.g. after "Popular Influence"
     */
    public static Class<? extends QuorumCardEvent> eventClass(QuorumCard card) {
        return switch (card) {
            case ACCEPT_PROPHECY -> AcceptProphecyEvent.class;
            case ARREST_ORDER -> ArrestOrderEvent.class;
            case ASSIGN_ARBITRATOR -> AssignArbitratorEvent.class;
            case ASSIGN_CHIEF_OF_STAFF -> AssignChiefOfStaffEvent.class;
            case ASSIGN_MISSION_SPECIALIST -> AssignMissionSpecialistEvent.class;
            case ASSIGN_VICE_PRESIDENT -> AssignVicePresidentEvent.class;
            case AUTHORIZATION_OF_BRUTAL_FORCE -> AuthorizationOfBrutalForceEvent.class;
            case CIVILIAN_SELF_DEFENSE -> CivilianSelfDefenseEvent.class;
            case CONSULT_THE_ORACLE -> ConsultTheOracleEvent.class;
            case ENACT_PRODUCTION_QUOTAS -> EnactProductionQuotasEvent.class;
            case ENCOURAGE_MUTINY -> EncourageMutinyEvent.class;
            case ESTABLISH_DOGSVILLE -> EstablishDogsvilleEvent.class;
            case EULOGY -> EulogyEvent.class;
            case EXECUTE_PRISONER -> ExecutePrisonerEvent.class;
            case FOOD_RATIONING -> FoodRationingEvent.class;
            case INSPIRATIONAL_SPEECH -> InspirationalSpeechEvent.class;
            case PRESIDENTIAL_ORDER -> PresidentialOrderEvent.class;
            case PRESIDENTIAL_PARDON -> PresidentialPardonEvent.class;
            case PROBATION -> ProbationEvent.class;
            case RELEASE_CYLON_MUGSHOTS -> ReleaseCylonMugshotsEvent.class;
            case RESIGNATION -> ResignationEvent.class;
            case RESOURCES_FOR_GALACTICA -> ResourcesForGalacticaEvent.class;
            case UNSAVORY_CONNECTIONS -> UnsavoryConnectionsEvent.class;
        };
    }

    /**
     * @return whether the player is President and holds a Quorum Card he can play now
     */
    public static boolean canPlayAny(GameView game, PlayerView player) {
        return player.character() != null
                && player.character() == game.president()
                && game.presidentHand().cards().stream().distinct().anyMatch(card -> canPlay(game, player, card));
    }
}
