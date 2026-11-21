package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.action.MutinyCardEventProcessor;
import xyz.zlatanov.frakkintoasters.event.player.SendToSickbayEvent;
import xyz.zlatanov.frakkintoasters.state.card.MutinyCard;

import static xyz.zlatanov.frakkintoasters.event.Followup.all;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.FollowupUtils.rollDie;

public class PeacefulResistanceEventProcessor extends MutinyCardEventProcessor<PeacefulResistanceEvent> {

    @Override
    public MutinyCard card() {
        return MutinyCard.PEACEFUL_RESISTANCE;
    }

    // SendToSickbayEvent leaves a character in the "Brig" where he is (rule A2.3.3.1).
    @Override
    protected Followup resolve() {
        return all(single(new SendToSickbayEvent(event.playerNumber())),
                rollDie(new PeacefulResistanceResultEvent()),
                single(thenDiscard()));
    }
}
