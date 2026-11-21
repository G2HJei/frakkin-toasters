package xyz.zlatanov.frakkintoasters.event.action;

import lombok.NoArgsConstructor;
import lombok.val;
import xyz.zlatanov.frakkintoasters.event.ActionEvent;
import xyz.zlatanov.frakkintoasters.event.ActivateViperEvent;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.LocationEvent;
import xyz.zlatanov.frakkintoasters.event.NoOpEvent;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static lombok.AccessLevel.PRIVATE;
import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;

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
     * @return the player's choice of an action or a pass, {@link Followup#NONE} when he has nothing to choose
     */
    public static Followup offerAction(GameView game, int playerNumber) {
        val player = game.player(playerNumber);
        if (game.locate(player.character()).isEmpty()) {
            return NONE; // a character off the board (a "Stranded" Karl "Helo" Agathon) takes no actions
        }
        if (game.boards().galactica().pilotedFighter(player.character()).isPresent()) {
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
}
