package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.player.SendToBrigEventProcessor.canBeSentToBrig;

public class RuinedReputationResultEventProcessor extends EventProcessor<RuinedReputationResultEvent> {

    static final int HIGHEST_ROLL_TO_ARREST = 4;

    @Override
    protected boolean isValid() {
        return game.turn().dieRoll() != null && isPlayer(event.targetPlayerNumber());
    }

    @Override
    public Followup process() {
        val target = event.targetPlayerNumber();
        return game.turn().dieRoll().result() <= HIGHEST_ROLL_TO_ARREST && canBeSentToBrig(game, target)
                ? single(new SendToBrigEvent(target))
                : NONE;
    }
}
