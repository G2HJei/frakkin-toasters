package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.AGGRESSIVE_TACTICS;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.EXECUTIVE_ORDER;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.EXPERT_PILOT;
import static xyz.zlatanov.frakkintoasters.event.action.GrantEvent.GrantedBy.MAINTENANCE_ENGINEER;

public class GrantEventProcessor extends EventProcessor<GrantEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.playerNumber()) && event.grant() != null && event.grantedBy() != null;
    }

    @Override
    public Followup process() {
        if (event.grantedBy() == EXECUTIVE_ORDER && (game.turn().endedEarly() || !player.isHuman())) {
            // A Cylon may not benefit from it: a target who revealed himself as his first action loses the second
            // (A2.3.8.4). It stops when the current player is executed or revealed, unlike a "State of Emergency"
            // (A2.3.8.6).
            return NONE;
        }
        if ((event.grantedBy() == EXPERT_PILOT || event.grantedBy() == AGGRESSIVE_TACTICS
                || event.grantedBy() == MAINTENANCE_ENGINEER) && game.turn().endedEarly()) {
            return NONE; // the actions of his own turn, which is over
        }
        int playerNumber = event.playerNumber();
        if (game.turn().actingPlayerNumber() != playerNumber) {
            ops.actingPlayer(playerNumber);
        }
        return switch (event.grant()) {
            case ACTION -> Actions.offerAction(game, playerNumber);
            case MOVE -> Actions.offerMove(game, playerNumber);
            case MOVE_OR_ACTION -> Actions.offerMoveOrAction(game, playerNumber);
        };
    }
}
