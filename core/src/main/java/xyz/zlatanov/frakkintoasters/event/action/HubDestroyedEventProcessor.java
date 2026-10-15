package xyz.zlatanov.frakkintoasters.event.action;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.state.board.Location.CYLON_FLEET;

public class HubDestroyedEventProcessor extends EventProcessor<HubDestroyedEvent> {

    @Override
    public Followup process() {
        ops.player(event.playerNumber())
                .discard(event.discardCard1(),
                        event.discardCard2(),
                        event.discardCard3())
                .drawSuperCrisisCard()
                .moveTo(CYLON_FLEET);
        return Followup.NONE;
    }
}
