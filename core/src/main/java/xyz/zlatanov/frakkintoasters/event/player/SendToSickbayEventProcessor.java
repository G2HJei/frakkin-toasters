package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;

public class SendToSickbayEventProcessor extends EventProcessor<SendToSickbayEvent> {

    @Override
    public Followup process() {
        if (game.locate(game.player(event.targetPlayerNumber()).character()).isPresent()) {
            // A character who isn't on the board can't be moved, e.g. a "Stranded" Karl "Helo" Agathon.
            ops.player(event.targetPlayerNumber()).sendTo(SICKBAY);
        }
        return Followup.NONE;
    }
}
