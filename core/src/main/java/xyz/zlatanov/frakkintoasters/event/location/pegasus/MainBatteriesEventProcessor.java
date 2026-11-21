package xyz.zlatanov.frakkintoasters.event.location.pegasus;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.location.LocationActionProcessor;

import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;
import static xyz.zlatanov.frakkintoasters.event.location.pegasus.MainBatteriesResultEventProcessor.isSpaceArea;

public class MainBatteriesEventProcessor extends LocationActionProcessor<MainBatteriesEvent> {

    @Override
    protected boolean isValidAction() {
        return isSpaceArea(event.spaceLocation());
    }

    @Override
    protected Followup act() {
        return rollDie(new MainBatteriesResultEvent(event.spaceLocation()));
    }
}
