package xyz.zlatanov.frakkintoasters.event.board;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.JumpingTheFleetEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.state.track.JumpPreparation.MINUS_1;

public class IncreaseJumpPreparationEventProcessor extends EventProcessor<IncreaseJumpPreparationEvent> {

    @Override
    public Followup process() {
        // "Auto Jump" comes after MINUS_1, and the fleet jumps when the token gets there (rule 12.1). The track has no
        // space for it: the token goes back to the start, as the jump would move it anyway.
        //todo add AUTO_JUMP to the track and handle it that way
        val autoJump = galacticaBoard.jumpPreparation() == MINUS_1;
        ops.galactica().increaseJumpPreparation();
        return autoJump ? single(new JumpingTheFleetEvent()) : NONE;
    }
}
