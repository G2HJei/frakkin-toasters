package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.player.SendToBrigEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;
import static xyz.zlatanov.frakkintoasters.event.player.SendToBrigEventProcessor.canBeSentToBrig;

public class PeacefulResistanceResultEventProcessor extends EventProcessor<PeacefulResistanceResultEvent> {

    static final int HIGHEST_ROLL_TO_ARREST = 4;

    @Override
    protected boolean isValid() {
        return game.turn().dieRoll() != null;
    }

    @Override
    public Followup process() {
        val admiral = game.playerNumber(game.admiral());
        return game.turn().dieRoll().result() <= HIGHEST_ROLL_TO_ARREST && canBeSentToBrig(game, admiral)
                ? single(new SendToBrigEvent(admiral))
                : NONE;
    }
}
