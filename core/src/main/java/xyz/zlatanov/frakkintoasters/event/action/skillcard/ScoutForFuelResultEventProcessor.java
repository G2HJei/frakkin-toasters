package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyRaptorEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.gain;
import static xyz.zlatanov.frakkintoasters.state.board.Resource.FUEL;

public class ScoutForFuelResultEventProcessor extends EventProcessor<ScoutForFuelResultEvent> {

    private static final int LOWEST_ROLL_FOR_FUEL = 4;

    @Override
    public Followup process() {
        return game.turn().dieRoll().result() >= LOWEST_ROLL_FOR_FUEL
                ? single(gain(FUEL, 1))
                : single(new DestroyRaptorEvent());
    }
}
