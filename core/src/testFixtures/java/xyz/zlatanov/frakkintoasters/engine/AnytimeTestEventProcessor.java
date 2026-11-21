package xyz.zlatanov.frakkintoasters.engine;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;

public class AnytimeTestEventProcessor extends EventProcessor<AnytimeTestEvent> {

    @Override
    protected boolean isValid() {
        return player.hasMiracleToken();
    }

    @Override
    public Followup process() {
        ops.player(player.number()).loseMiracleTokens();
        return NONE;
    }
}
