package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.endgame.CylonsWinEvent;

import java.util.stream.IntStream;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.playersFrom;

public class EndTurnEventProcessor extends EventProcessor<EndTurnEvent> {

    @Override
    public Followup process() {
        ops.discardCrisisCards(); // the Crisis Cards drawn this turn (rule 8.8)
        if (resourceDepleted()) {
            return single(new CylonsWinEvent());
        }
        // The current player token passes to the player on the left (rule 8.1).
        ops.changeCurrentPlayer(playersFrom(game, game.turn().currentPlayerNumber()).get(1).number());
        return single(new StartTurnEvent());
    }

    // The only resource check (rule 16.1). A resource can reach 0 during a turn and recover before the turn ends.
    private boolean resourceDepleted() {
        return IntStream.of(galacticaBoard.fuel(), galacticaBoard.food(), galacticaBoard.morale(), galacticaBoard.population())
                .anyMatch(resource -> resource <= 0);
    }
}
