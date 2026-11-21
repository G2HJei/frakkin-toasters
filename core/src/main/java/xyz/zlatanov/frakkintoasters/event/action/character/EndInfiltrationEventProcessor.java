package xyz.zlatanov.frakkintoasters.event.action.character;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.ReturnToResurrectionShipEventProcessor;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.board.Location.RESURRECTION_SHIP;

public class EndInfiltrationEventProcessor extends EventProcessor<EndInfiltrationEvent> {

    // Nothing to end when something already did, e.g. a Final Five card that executed her. Ended from the "Brig", she
    // discards down to 3 Skill Cards, as the return action does (rule 6.2.3.5).
    @Override
    public Followup process() {
        if (!player.isInfiltrating()) {
            return NONE;
        }
        return ReturnToResurrectionShipEventProcessor.endInfiltration(game, ops, player, RESURRECTION_SHIP);
    }
}
