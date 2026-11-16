package xyz.zlatanov.frakkintoasters.event.loyalty;

import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class ChooseExtraLoyaltyCardEventProcessor extends EventProcessor<ChooseExtraLoyaltyCardEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.targetPlayerNumber()) && game.player(event.targetPlayerNumber()).isHuman();
    }

    @Override
    public Followup process() {
        return single(new DealLoyaltyCardsEvent(event.targetPlayerNumber(), 1, true));
    }
}
