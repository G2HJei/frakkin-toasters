package xyz.zlatanov.frakkintoasters.event.turn;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.*;
import xyz.zlatanov.frakkintoasters.event.placeholder.PlayerDecisionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.LocationEvent.canAct;
import static xyz.zlatanov.frakkintoasters.state.GameStep.ACTION;

public class ActionStepEventProcessor extends EventProcessor<ActionStepEvent> {

    // A pilot activates their viper, anyone else their location, and either may do nothing. A character off the
    // board (a "Stranded" Karl "Helo" Agathon) takes no actions, and neither does one whose location has no action
    // he can take, e.g. in "Sickbay": once he chose to act, he would have no legal answer.
    @Override
    public Followup process() {
        ops.changeStep(ACTION);
        val current = game.currentPlayer();
        if (game.locate(current.character()).isEmpty()) {
            return NONE;
        }
        val playerNumber = current.number();
        if (galacticaBoard.pilotedFighter(current.character()).isPresent()) {
            return one(new ActivateViperEvent(playerNumber), new NoOpEvent(playerNumber));
        }
        //todo activating a viper is also an action event as well as any component action - character sheet, cards, locations - aanything with "Action:" on it.
        return canAct(game, current)
                ? one(new PlayerDecisionEvent<>(playerNumber, ActionEvent.class), new NoOpEvent(playerNumber))
                : NONE;
    }
}
