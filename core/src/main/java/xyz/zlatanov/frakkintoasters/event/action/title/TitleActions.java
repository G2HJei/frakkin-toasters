package xyz.zlatanov.frakkintoasters.event.action.title;

import lombok.NoArgsConstructor;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static lombok.AccessLevel.PRIVATE;

/**
 * The actions printed on the Title Cards (rule 4.4): the President's, the Admiral's nuke and the CAG's two.
 */
@NoArgsConstructor(access = PRIVATE)
public final class TitleActions {

    public static boolean canAct(GameView game, PlayerView player) {
        return PresidentTitleActionEventProcessor.canAct(game, player)
                || LaunchNukeEventProcessor.canAct(game, player)
                || PassCagTitleEventProcessor.canAct(game, player)
                || CagPilotActionEventProcessor.canAct(game, player);
    }
}
