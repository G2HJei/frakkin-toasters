package xyz.zlatanov.frakkintoasters.event.character;

import lombok.val;
import xyz.zlatanov.frakkintoasters.EventProcessor;
import xyz.zlatanov.frakkintoasters.event.Followup;

import static xyz.zlatanov.frakkintoasters.event.Followup.one;
import static xyz.zlatanov.frakkintoasters.event.Followup.single;

public class ForcedDiscardEventProcessor extends EventProcessor<ForcedDiscardEvent> {

    @Override
    protected boolean isValid() {
        return isPlayer(event.discarderPlayerNumber()) && event.discard() != null && event.count() >= 0;
    }

    @Override
    public Followup process() {
        int discarder = event.discarderPlayerNumber();
        if (!event.forLoveAsked()) {
            // Who can use it is known only now, e.g. not twice in a turn.
            val athena = ForLoveEventProcessor.athenaFor(game, discarder, event.discard(), event.count());
            if (athena.isPresent()) {
                return one(new ForLoveEvent(athena.get().number(), discarder, event.discard(), event.count()),
                        new ForcedDiscardEvent(discarder, event.discard(), event.count(), true));
            }
        }
        return single(ForcedDiscards.plain(discarder, event.discard(), event.count()));
    }
}
