package xyz.zlatanov.frakkintoasters.event.action.title;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.GrantEvent;
import xyz.zlatanov.frakkintoasters.state.view.GameView;
import xyz.zlatanov.frakkintoasters.state.view.PlayerView;

import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.activate;
import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.canActivateUnmanned;
import static xyz.zlatanov.frakkintoasters.event.ActivateViperEventProcessor.isValidActivation;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.allOf;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.Grant.ACTION;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.CAG_TITLE;
import static xyz.zlatanov.frakkintoasters.event.action.title.PassCagTitleEventProcessor.isCag;
import static xyz.zlatanov.frakkintoasters.state.TurnLimit.CAG_PILOT_ACTION;

public class CagPilotActionEventProcessor extends EventProcessor<CagPilotActionEvent> {

    public static boolean canAct(GameView game, PlayerView player) {
        return isCag(game, player)
                && game.boards().galactica().pilotedFighter(player.character()).isPresent()
                && !game.turn().hasUsed(player.number(), CAG_PILOT_ACTION)
                && canActivateUnmanned(game);
    }

    @Override
    protected boolean isValid() {
        return canAct(game, player)
                && isValidActivation(game, event.activation(), fighter -> fighter.pilot() == null, true);
    }

    @Override
    public Followup process() {
        ops.useTurnLimit(event.playerNumber(), CAG_PILOT_ACTION);
        // The other action is offered once the unmanned viper's activation is over, as the game stands then.
        return allOf(activate(ops, event.playerNumber(), event.activation()),
                single(new GrantEvent(event.playerNumber(), ACTION, CAG_TITLE)));
    }
}
