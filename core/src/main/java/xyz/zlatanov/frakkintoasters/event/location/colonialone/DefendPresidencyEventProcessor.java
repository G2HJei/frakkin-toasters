package xyz.zlatanov.frakkintoasters.event.location.colonialone;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.state.card.QuorumCard.ACCEPT_PROPHECY;

public class DefendPresidencyEventProcessor extends EventProcessor<DefendPresidencyEvent> {

    @Override
    protected boolean isValid() {
        return player.character() != null
                && player.character() == game.president()
                && game.inPlay(ACCEPT_PROPHECY)
                && isPlayer(event.administrationPlayerNumber());
    }

    @Override
    public Followup process() {
        if (event.discardAcceptProphecy()) {
            ops.titles().discardFromPlay(ACCEPT_PROPHECY);
            return NONE;
        }
        return ReplaceMutinousPresidentEventProcessor.replacement(game, event.administrationPlayerNumber());
    }
}
