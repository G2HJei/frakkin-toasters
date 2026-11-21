package xyz.zlatanov.frakkintoasters.event.action.skillcard;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.damageGalactica;

public class TestTheLimitsResultEventProcessor extends EventProcessor<TestTheLimitsResultEvent> {

    private static final int HIGHEST_ROLL_TO_DAMAGE = 5;

    @Override
    public Followup process() {
        return game.turn().dieRoll().result() <= HIGHEST_ROLL_TO_DAMAGE ? damageGalactica() : NONE;
    }
}
