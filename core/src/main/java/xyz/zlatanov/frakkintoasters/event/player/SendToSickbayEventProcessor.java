package xyz.zlatanov.frakkintoasters.event.player;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.board.Location.SICKBAY;

public class SendToSickbayEventProcessor extends EventProcessor<SendToSickbayEvent> {

    @Override
    public Followup process() {
        ops.player(event.targetPlayerNumber()).sendTo(SICKBAY);
        return Followup.NONE;
    }
}
