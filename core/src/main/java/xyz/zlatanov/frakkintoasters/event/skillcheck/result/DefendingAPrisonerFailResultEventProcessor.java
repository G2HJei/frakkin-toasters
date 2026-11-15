package xyz.zlatanov.frakkintoasters.event.skillcheck.result;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.ExecuteEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.player.ExecuteEventProcessor.canBeExecuted;

public class DefendingAPrisonerFailResultEventProcessor extends EventProcessor<DefendingAPrisonerFailResultEvent> {

    @Override
    public Followup process() {
        val current = game.turn().currentPlayerNumber();
        // A revealed Cylon resolving the card at "Caprica" ignores the execution (rule 6.1.2.1).
        return game.turn().dieRoll().result() <= 4 && canBeExecuted(game, current)
                ? single(new ExecuteEvent(current))
                : NONE;
    }
}
