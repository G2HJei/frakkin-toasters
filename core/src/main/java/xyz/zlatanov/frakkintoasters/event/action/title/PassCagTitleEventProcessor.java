package xyz.zlatanov.frakkintoasters.event.action.title;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.officials.MakeCagEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.activate;
import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.canActivateUnmanned;
import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.isValidActivation;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.officials.MakeCagEventProcessor.canBecomeCag;

public class PassCagTitleEventProcessor extends EventProcessor<PassCagTitleEvent> {

    // The card says "activate, then give", so it needs an unmanned viper to activate and another human who can take the
    // title.
    public static boolean canAct(GameView game, PlayerView player) {
        return isCag(game, player)
                && canActivateUnmanned(game)
                && game.players().stream().anyMatch(other -> canReceive(game, player, other));
    }

    @Override
    protected boolean isValid() {
        return isCag(game, player)
                && isValidActivation(game, event.activation(), fighter -> fighter.pilot() == null, true)
                && isPlayer(event.targetPlayerNumber())
                && canReceive(game, player, game.player(event.targetPlayerNumber()));
    }

    @Override
    public Followup process() {
        return allOf(activate(ops, event.playerNumber(), event.activation()),
                single(new MakeCagEvent(game.player(event.targetPlayerNumber()).character())));
    }

    static boolean isCag(GameView game, PlayerView player) {
        return player.isHuman() && player.character() != null && player.character() == game.cag();
    }

    private static boolean canReceive(GameView game, PlayerView cag, PlayerView other) {
        // "any other human player": not an Infiltrating Cylon Leader (rule 6.2.3.3), nor a character in the "Brig"
        return other.number() != cag.number() && other.isHuman() && canBecomeCag(game, other.character());
    }
}
