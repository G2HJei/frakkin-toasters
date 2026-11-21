package xyz.zlatanov.frakkintoasters.event.action;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.Event;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.event.player.MoveEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import java.util.ArrayList;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.state.board.Location.BRIG;

/**
 * What a player can do now, for the Action step and everything else that gives an action: the action of his location,
 * or the "Action:" of a Skill Card he holds. A pilot may activate his viper instead.
 */
@NoArgsConstructor(access = PRIVATE)
public final class Actions {

    /**
     * @return whether the player has an action to take: the action of his location, or a Skill Card's
     */
    public static boolean canTakeAction(GameView game, PlayerView player) {
        return LocationEvent.canAct(game, player) || SkillCardPlays.canPlayAction(game, player);
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
        val player = game.player(playerNumber);
        if (game.locate(player.character()).isEmpty()) {
            return NONE; // a character off the board (a "Stranded" Karl "Helo" Agathon) takes no actions
        }
        if (isPilot(game, player)) {
            // A pilot activates his viper, or takes another action, e.g. plays a Skill Card (rule 11.2.1). He is in
            // space, so a location action is never his.
            return SkillCardPlays.canPlayAction(game, player)
                    ? one(new PlayerDecisionEvent<>(playerNumber, ActivateViperEvent.class),
                    new PlayerDecisionEvent<>(playerNumber, ActionEvent.class),
                    new NoOpEvent(playerNumber))
                    : one(new PlayerDecisionEvent<>(playerNumber, ActivateViperEvent.class), new NoOpEvent(playerNumber));
        }
        // Once he chose to act, he must have a legal answer: none e.g. in "Sickbay" without a card to play.
        return canTakeAction(game, player)
                ? one(new PlayerDecisionEvent<>(playerNumber, ActionEvent.class), new NoOpEvent(playerNumber))
                : NONE;
    }

    /**
     * @return whether the player can move: not from the "Brig" (rule 9.2), nor off the board (a "Stranded" Karl "Helo"
     * Agathon)
     */
    public static boolean canMove(GameView game, PlayerView player) {
        val location = game.locate(player.character());
        return location.isPresent() && location.get() != BRIG;
    }

    /**
     * @return the player's choice in his Movement step: move, use a "Movement:" ability instead (rule 8.3.5), even in the
     * "Brig" (rule 10.4.1), or pass. {@link Followup#NONE} when he has nothing to choose
     */
    public static Followup offerMovement(GameView game, int playerNumber) {
        val player = game.player(playerNumber);
        val options = new ArrayList<Event>();
        if (canMove(game, player)) {
            options.add(new PlayerDecisionEvent<>(playerNumber, MoveEvent.class));
        }
        if (game.locate(player.character()).isPresent() && SkillCardPlays.canPlayMovementAbility(game, player)) {
            options.add(new PlayerDecisionEvent<>(playerNumber, MovementAbilityEvent.class));
        }
        if (options.isEmpty()) {
            return NONE;
        }
        options.add(new NoOpEvent(playerNumber));
        return one(options.toArray(Event[]::new));
    }

    private static boolean isPilot(GameView game, PlayerView player) {
        return game.boards().galactica().pilotedFighter(player.character()).isPresent();
    }
}
