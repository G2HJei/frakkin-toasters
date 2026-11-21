package xyz.zlatanov.frakkintoasters.event.action.mutiny;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;
import xyz.zlatanov.frakkintoasters.event.ship.DestroyCenturionEvent;

import static xyz.zlatanov.frakkintoasters.event.Followup.NONE;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class SendAMessageResultEventProcessor extends EventProcessor<SendAMessageResultEvent> {

    static final int BONUS                  = 2;
    static final int LOWEST_ROLL_TO_DESTROY = 7; // the attack table's row for a centurion, as at the "Armory"

    @Override
    protected boolean isValid() {
        return game.turn().dieRoll() != null;
    }

    @Override
    public Followup process() {
        val onTrack = galacticaBoard.boardingPartyTrack().keySet().stream()
                .anyMatch(centurion -> centurion.id() == event.centurionId());
        return onTrack && game.turn().dieRoll().result() + BONUS >= LOWEST_ROLL_TO_DESTROY
                ? single(new DestroyCenturionEvent(event.centurionId()))
                : NONE;
    }
}
