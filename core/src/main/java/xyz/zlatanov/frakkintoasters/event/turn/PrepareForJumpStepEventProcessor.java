package xyz.zlatanov.frakkintoasters.event.turn;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.GameStep.PREPARE_FOR_JUMP;

public class PrepareForJumpStepEventProcessor extends EventProcessor<PrepareForJumpStepEvent> {

    // Not skipped on a revealed Cylon's turn: a Crisis Card chosen at "Caprica" can have the icon (rule 6.1.3.4).
    @Override
    public Followup process() {
        ops.changeStep(PREPARE_FOR_JUMP);
        // todo advance the Jump Preparation track for each "prepare for jump" icon on the Crisis Cards drawn this turn
        return NONE;
    }
}
