package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

public class GrantEventProcessor extends EventProcessor<GrantEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.playerNumber()) && event.grant() != null && event.grantedBy() != null;
    }

    @Override
    public Followup process() {
        int playerNumber = event.playerNumber();
        if (game.turn().actingPlayerNumber() != playerNumber) {
            ops.actingPlayer(playerNumber);
        }
        return switch (event.grant()) {
            case ACTION -> Actions.offerAction(game, playerNumber);
        };
    }
}
