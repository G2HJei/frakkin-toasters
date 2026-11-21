package xyz.zlatanov.frakkintoasters.event.action;

import lombok.NoArgsConstructor;
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
            // the cards that later phases of quorum-cards-plan.md build
            case PRESIDENTIAL_ORDER, RESIGNATION, ACCEPT_PROPHECY, ARREST_ORDER, ASSIGN_ARBITRATOR,
                 ASSIGN_MISSION_SPECIALIST, ASSIGN_VICE_PRESIDENT, AUTHORIZATION_OF_BRUTAL_FORCE, ENCOURAGE_MUTINY,
                 PRESIDENTIAL_PARDON, RELEASE_CYLON_MUGSHOTS, ASSIGN_CHIEF_OF_STAFF, CIVILIAN_SELF_DEFENSE,
                 CONSULT_THE_ORACLE, EULOGY, EXECUTE_PRISONER, PROBATION, RESOURCES_FOR_GALACTICA,
                 UNSAVORY_CONNECTIONS -> false;
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
