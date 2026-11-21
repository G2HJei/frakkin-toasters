package xyz.zlatanov.frakkintoasters.event.action;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.action.title.TitleActions;
import xyz.zlatanov.frakkintoasters.event.character.CharacterMoves;
import xyz.zlatanov.frakkintoasters.event.loyalty.RevealCylonEventProcessor;
import xyz.zlatanov.frakkintoasters.event.loyalty.RevealPersonalGoalEventProcessor;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;
import xyz.zlatanov.frakkintoasters.event.player.ReturnToResurrectionShipEventProcessor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

/**
 * What a player can do now, for the Action step and everything else that gives an action: the action of his location,
 * or another action, the "Action:" of a Skill Card, a Quorum Card or a Mutiny Card he holds, or of his Title Card. A
 * pilot may activate his viper instead.
 */
@NoArgsConstructor(access = PRIVATE)
public final class Actions {

    /**
     * @return whether the player has an action to take: the action of his location, or another one
     */
    public static boolean canTakeAction(GameView game, PlayerView player) {
        return LocationEvent.canAct(game, player) || canTakeOtherAction(game, player);
    }

    /**
     * @return whether the player can take an action that isn't his location's: one printed on a Skill Card, a Quorum
     * Card (rule 8.4), a Mutiny Card (rule 5.2.3.1), his Title Card (rule 4.4), the Infiltration Card (rule 6.2.3.5) or
     * his character sheet (rule 6.2.1.2), or the reveal of a "You Are a Cylon" Loyalty Card (rule 6.1) or a Personal
     * Goal (rule 5.3.1)
     */
    public static boolean canTakeOtherAction(GameView game, PlayerView player) {
        return SkillCardPlays.canPlayAction(game, player)
                || QuorumCardPlays.canPlayAny(game, player)
                || MutinyCardPlays.canPlayAny(game, player)
                || TitleActions.canAct(game, player)
                || ReturnToResurrectionShipEventProcessor.canReturn(game, player)
                || RevealCylonEventProcessor.canReveal(game, player)
                || RevealPersonalGoalEventProcessor.canReveal(game, player)
                || CharacterActions.canUseAny(game, player);
    }

    /**
     * @return whether {@link #offerAction} would offer the player anything
     */
    public static boolean hasAnAction(GameView game, PlayerView player) {
        return game.locate(player.character()).isPresent()
                && (isPilot(game, player) || canTakeAction(game, player));
    }

    /**
     * @return the player's choice of an action or a pass, {@link Followup#NONE} when he has nothing to choose
     */
    public static Followup offerAction(GameView game, int playerNumber) {
        return choice(playerNumber, actionDecisions(game, playerNumber));
    }

    /**
     * @return the player's choice of a move or a pass, e.g. for an "Executive Order". A "Movement:" ability can't be used
     * then (rule 8.3.5)
     */
    public static Followup offerMove(GameView game, int playerNumber) {
        return choice(playerNumber, moveDecisions(game, playerNumber));
    }

    /**
     * @return the player's choice of a move, an action or a pass, e.g. for a "State of Emergency"
     */
    public static Followup offerMoveOrAction(GameView game, int playerNumber) {
        val decisions = new ArrayList<Event>(moveDecisions(game, playerNumber));
        decisions.addAll(actionDecisions(game, playerNumber));
        return choice(playerNumber, decisions);
    }

    /**
     * @return whether the player can move: not from the "Brig" (rule 9.2), nor off the board (a "Stranded" Karl "Helo"
     * Agathon)
     */
    public static boolean canMove(GameView game, PlayerView player) {
        val location = game.locate(player.character());
        return location.isPresent() && location.get() != BRIG && !CharacterMoves.hereticHeld(game, player);
    }

    /**
     * @return the player's choice in his Movement step: move, use a "Movement:" ability instead (rule 8.3.5), even in the
     * "Brig" (rule 10.4.1), or pass. {@link Followup#NONE} when he has nothing to choose
     */
    public static Followup offerMovement(GameView game, int playerNumber) {
        val player = game.player(playerNumber);
        val decisions = new ArrayList<Event>(moveDecisions(game, playerNumber));
        if (game.locate(player.character()).isPresent()
                && (SkillCardPlays.canPlayMovementAbility(game, player) || CharacterMovements.canUseAny(game, player))) {
            decisions.add(new PlayerDecisionEvent<>(playerNumber, MovementAbilityEvent.class));
        }
        return choice(playerNumber, decisions);
    }

    private static List<Event> actionDecisions(GameView game, int playerNumber) {
        val player = game.player(playerNumber);
        if (game.locate(player.character()).isEmpty()) {
            return List.of(); // a character off the board (a "Stranded" Karl "Helo" Agathon) takes no actions
        }
        if (isPilot(game, player)) {
            // A pilot activates his viper, or takes another action, e.g. plays a card (rule 11.2.1). He is in space,
            // so a location action is never his.
            return canTakeOtherAction(game, player)
                    ? List.of(new PlayerDecisionEvent<>(playerNumber, ActivateViperEvent.class),
                    new PlayerDecisionEvent<>(playerNumber, ActionEvent.class))
                    : List.of(new PlayerDecisionEvent<>(playerNumber, ActivateViperEvent.class));
        }
        // Once he chose to act, he must have a legal answer: none e.g. in "Sickbay" without a card to play.
        return canTakeAction(game, player) ? List.of(new PlayerDecisionEvent<>(playerNumber, ActionEvent.class)) : List.of();
    }

    private static List<Event> moveDecisions(GameView game, int playerNumber) {
        return canMove(game, game.player(playerNumber))
                ? List.of(new PlayerDecisionEvent<>(playerNumber, MoveEvent.class))
                : List.of();
    }

    // The decisions, or doing nothing instead.
    private static Followup choice(int playerNumber, List<Event> decisions) {
        if (decisions.isEmpty()) {
            return NONE;
        }
        val options = new ArrayList<>(decisions);
        options.add(new NoOpEvent(playerNumber));
        return one(options.toArray(Event[]::new));
    }

    private static boolean isPilot(GameView game, PlayerView player) {
        return game.boards().galactica().pilotedFighter(player.character()).isPresent();
    }
}
